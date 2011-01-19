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

import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.domain.scm.ScmDomain;

public class PollTask extends TimerTask {

    private Log log = LogFactory.getLog(PollTask.class);

    private ScmDomain scm;

    private String projectId;

    private WorkflowService workflowService;

    private ContextCurrentService contextService;

    private void runFlow() throws WorkflowException {
        workflowService.startFlow("ci");
    }

    @Override
    public void run() {
        try {
            log.info("running pollertask");
            contextService.setThreadLocalContext(projectId);
            if (scm.poll()) {
                log.info("running flow");
                runFlow();
            }
        } catch (Exception e) {
            log.error("error when polling scm ", e);
        }
        log.info("run done");
    }

}
