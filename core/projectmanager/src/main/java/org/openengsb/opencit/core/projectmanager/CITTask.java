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

package org.openengsb.opencit.core.projectmanager;

import org.openengsb.core.common.context.ContextHolder;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.opencit.core.projectmanager.model.ProjectStateInfo;

public class CITTask implements Runnable {

    private static final long TIMEOUT = 3 * 60 * 60 * 1000; // 3 hours
    private WorkflowService service;
    private ProjectStateInfo stateInfo;
    private long pid;
    private String projectId;

    public CITTask(WorkflowService service, String projectId, ProjectStateInfo stateInfo) {
        this.service = service;
        this.stateInfo = stateInfo;
        this.projectId = projectId;
    }

    @Override
    public void run() {
        ContextHolder.get().setCurrentContextId(projectId);
        stateInfo.setBuilding(true);
        try {
            pid = service.startFlow("ci");
        } catch (WorkflowException e) {
            throw new RuntimeException(e);
        }
        try {
            service.waitForFlowToFinish(pid, TIMEOUT);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (WorkflowException e) {
            throw new RuntimeException(e);
        }
        stateInfo.setBuilding(false);
    }

    public void cancel() throws WorkflowException {
        service.cancelFlow(pid);
    }
}
