package org.openengsb.opencit.core.projectmanager.internal;

import org.openengsb.opencit.core.projectmanager.model.BuildReason;

@SuppressWarnings("serial")
public class TestBuild extends BuildReason {

    @Override
    public String getDescription() {
        return "Unit tests";
    }
}
