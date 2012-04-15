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
import java.util.concurrent.Callable;

import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.security.BundleAuthenticationToken;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.openengsb.opencit.core.projectmanager.model.BuildReason;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CITTask implements Callable<Boolean> {

    private static final long TIMEOUT = 3 * 60 * 60 * 1000; // 3 hours
    private WorkflowService workflowService;
    private SchedulingService scheduler;
    private long pid;
    private String projectId;
    private AuthenticationManager authenticationManager;
    private BuildReason reason;

    public CITTask(String projectId, BuildReason reason) {
        this.projectId = projectId;
        this.reason = reason;
    }

    @Override
    public Boolean call() throws WorkflowException, InterruptedException {
        authenticate();

        ContextHolder.get().setCurrentContextId(projectId);
        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("buildReason", reason);
            
            pid = workflowService.startFlow("ci", parameterMap);
            workflowService.waitForFlowToFinish(pid, TIMEOUT);
        } finally {
            scheduler.resumeScmPoller(projectId);
        }
        return true;
    }

    private void authenticate() {
        SecurityContextHolder.clearContext();
        Authentication token =
            authenticationManager.authenticate(new BundleAuthenticationToken(
                "opencit-core-projectmanager", ""));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public void cancel() throws WorkflowException {
        workflowService.cancelFlow(pid);
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setScheduler(SchedulingService scheduler) {
        this.scheduler = scheduler;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
