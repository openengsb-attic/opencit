/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.opencit.core.projectmanager.internal;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.security.BundleAuthenticationToken;
import org.openengsb.domain.scm.CommitRef;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.ScmUpdate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class PollTask implements Runnable {

    private Log log = LogFactory.getLog(PollTask.class);

    private AuthenticationManager authenticationManager;
    private SchedulingService scheduler;
    private ProjectManager projectManager;
    private ScmDomain scm;

    private String projectId;

    public PollTask(String projectId) {
        this.projectId = projectId;
    }

    public PollTask() {
    }

    @Override
    public void run() {
        try {
            authenticate();
            doRun();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    private void authenticate() {
        Authentication token =
            authenticationManager.authenticate(new BundleAuthenticationToken(
                "opencit-core-projectmanager", ""));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private void doRun() {
        log.info("running pollertask");
        ContextHolder.get().setCurrentContextId(projectId);
        log.debug("ContextHolder now has " + ContextHolder.get().getCurrentContextId());
        List<CommitRef> updates = scm.update();
        if (updates != null && !updates.isEmpty()) {
            String commitId = updates.get(0).getStringRepresentation();
            scheduler.scheduleProjectForBuild(projectId, new ScmUpdate(commitId));
        }

        try {
            Project project = projectManager.getCurrentContextProject();
            project.setLastScmPollDate(new Date());
            projectManager.updateProject(project);
        } catch (NoSuchProjectException e) {
            throw new IllegalStateException("Project, this task is for does not exist", e);
        }
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setScm(ScmDomain scm) {
        this.scm = scm;
    }

    public void setScheduler(SchedulingService scheduler) {
        this.scheduler = scheduler;
    }

    public void setProjectManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

}
