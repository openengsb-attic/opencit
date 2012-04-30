package org.openengsb.opencit.core.projectmanager.model;

@SuppressWarnings("serial")
public class DepUpdateBuildReason extends BuildReason {

    private String depName;
    private UpdateNotification update;

    public DepUpdateBuildReason(UpdateNotification update, String depName) {
        this.update = update;
        this.depName = depName;
    }

    @Override
    public String getDescription() {
        // TODO Internationalization
        return "New build " + update.getBuildId() + " of dependency " + depName + ".";
    }

}
