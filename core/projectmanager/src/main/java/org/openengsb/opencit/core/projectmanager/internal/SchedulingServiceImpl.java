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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;

public class SchedulingServiceImpl implements SchedulingService {

    private WorkflowService workflowService;
    private AuthenticationManager authenticationManager;
    private ProjectManager projectManager;
    private OsgiUtilsService osgiUtilsService;

    private ScheduledExecutorService scmScheduler = Executors.newScheduledThreadPool(1);
    private Map<String, PollTask> pollTasks = new HashMap<String, PollTask>();
    private Map<String, ScheduledFuture<?>> pollFutures = new HashMap<String, ScheduledFuture<?>>();

    private ExecutorService buildScheduler = Executors.newFixedThreadPool(1);
    private Map<String, Future<Boolean>> buildFutures = new HashMap<String, Future<Boolean>>();

    private long pollInterval = 30000L;

    @Override
    public void setupAndStartScmPoller(final Project project) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                SecurityContextHolder.clearContext();
                PollTask task = createPollTask(project);
                pollTasks.put(project.getId(), task);
                resumeScmPoller(project.getId());
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void suspendScmPoller(String projectId) {
        pollFutures.get(projectId).cancel(false);
    }

    @Override
    public void resumeScmPoller(String projectId) {
        PollTask task = pollTasks.get(projectId);
        ScheduledFuture<?> future =
            scmScheduler.scheduleWithFixedDelay(task, 0, pollInterval, TimeUnit.MILLISECONDS);
        pollFutures.put(projectId, future);
    }

    @Override
    public boolean isProjectPolling(String projectid) {
        if (!pollFutures.containsKey(projectid)) {
            return false;
        }
        ScheduledFuture<?> future = pollFutures.get(projectid);
        return !future.isDone();
    }

    @Override
    public void scheduleProjectForBuild(String projectId) {
        CITTask citTask = new CITTask(projectId);
        citTask.setWorkflowService(workflowService);
        citTask.setScheduler(this);
        citTask.setAuthenticationManager(authenticationManager);

        suspendScmPoller(projectId);
        Future<Boolean> future = buildScheduler.submit(citTask);
        buildFutures.put(projectId, future);
    }

    @Override
    public void cancelProjectBuild(String projectId) {
        buildFutures.get(projectId).cancel(true);
    }

    @Override
    public boolean isProjectBuilding(String projectId) {
        if (!buildFutures.containsKey(projectId)) {
            return false;
        }
        Future<Boolean> future = buildFutures.get(projectId);
        return !future.isDone();
    }

    private PollTask createPollTask(Project project) {
        WiringService ws = getOsgiUtilsService().getOsgiServiceProxy(WiringService.class);
        ScmDomain scm = ws.getDomainEndpoint(ScmDomain.class, "scm", project.getId());
        PollTask pollTask = new PollTask(project.getId());

        pollTask.setAuthenticationManager(authenticationManager);
        pollTask.setScheduler(this);
        pollTask.setProjectManager(projectManager);
        pollTask.setScm(scm);
        return pollTask;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setProjectManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public void setOsgiUtilsService(OsgiUtilsService osgiUtilsService) {
        this.osgiUtilsService = osgiUtilsService;
    }

    public OsgiUtilsService getOsgiUtilsService() {
        return osgiUtilsService;
    }

}
