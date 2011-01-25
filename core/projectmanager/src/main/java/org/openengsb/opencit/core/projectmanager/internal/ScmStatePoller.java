/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.opencit.core.projectmanager.internal;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.domain.scm.ScmDomain;

public class ScmStatePoller {

    private Log log = LogFactory.getLog(this.getClass());

    public class PollTask extends TimerTask {
        private void runFlow() {
            try {
                workflowService.startFlow("ci");
            } catch (WorkflowException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            try {
                contextService.setThreadLocalContext(projectId);
                log.debug("polling");
                if (scm.poll()) {
                    runFlow();
                }
            } catch (Exception e) { // just swallow it here for now
                log.error("error when polling scm ", e);
            }
        }
    }

    private ScmDomain scm;

    private String projectId;

    private WorkflowService workflowService;

    private ContextCurrentService contextService;

    private long timeout;

    private PollTask task;

    private Timer timer = new Timer();

    public void start() {
        task = new PollTask();
        timer.schedule(task, 0, timeout);
    }

    public void stop() {
        task.cancel();
    }

    public void setContextService(ContextCurrentService contextService) {
        this.contextService = contextService;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setScm(ScmDomain scm) {
        this.scm = scm;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
