package org.openengsb.opencit.core.config;

import org.openengsb.opencit.core.projectmanager.model.BuildReason;

public class TestBuildReason extends BuildReason {
    @Override
    public String getDescription() {
        return "Unit tests";
    }
}
