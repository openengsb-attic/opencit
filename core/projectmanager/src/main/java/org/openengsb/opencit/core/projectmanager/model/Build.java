package org.openengsb.opencit.core.projectmanager.model;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.ObjectUtils;

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

    private boolean objectEquals(Object o1, Object o2) {
        /* Needed for the persistence service */
        if (o1 == null || o2 == null) {
            return true;
        }
        return ObjectUtils.equals(o1, o2);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Build)) {
            return false;
        }
        Build other = (Build) obj;

        if (!objectEquals(id, other.id)) return false;
        if (!objectEquals(projectId, other.projectId)) return false;
        if (!objectEquals(reason, other.reason)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash += 31 * ObjectUtils.hashCode(id);
        hash += 31 * ObjectUtils.hashCode(projectId);
        hash += 31 * ObjectUtils.hashCode(reason);
        return hash;
    }
}
