package org.openengsb.opencit.core.projectmanager.model;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("serial")
public class Build implements Serializable {
    private UUID id;
    private String projectId;
    private BuildReason reason;
    
    public Build() {
        this.id = UUID.randomUUID();
    }

    public Build(String projectId, BuildReason reason) {
        this.id = UUID.randomUUID();
        this.setProjectId(projectId);
        this.setReason(reason);
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setReason(BuildReason reason) {
        this.reason = reason;
    }

    public BuildReason getReason() {
        return reason;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
