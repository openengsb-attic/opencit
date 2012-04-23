package org.openengsb.opencit.ui.web.model;

import org.openengsb.opencit.core.projectmanager.model.BuildReason;

@SuppressWarnings("serial")
public class ManualBuildReason extends BuildReason {
    @Override
    public String getDescription() {
        /* FIXME: Internationalization */
        return "Manual rebuild via WebUI";
    }
}
