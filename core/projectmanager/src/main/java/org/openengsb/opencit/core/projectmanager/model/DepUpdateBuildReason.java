package org.openengsb.opencit.core.projectmanager.model;

@SuppressWarnings("serial")
public class DepUpdateBuildReason extends BuildReason {

    private String depName;
    private UpdateNotification update;

    public DepUpdateBuildReason(UpdateNotification update, String depName) {
        this.setUpdate(update);
        this.setDependencyName(depName);
    }

    @Override
    public String getDescription() {
        // TODO Internationalization
        return "New build " + getUpdate().getBuildId() + " of dependency " + depName + ".";
    }

    public void setUpdate(UpdateNotification update) {
        this.update = update;
    }

    public UpdateNotification getUpdate() {
        return update;
    }

    public void setDependencyName(String depName) {
        this.depName = depName;
    }

    public String getDependencyName() {
        return depName;
    }

}
