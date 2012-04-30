package org.openengsb.opencit.core.projectmanager.model;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("serial")
public class UpdateNotification implements Serializable {
    
    private UUID buildId;
    private String artifactLocation;
    private String feedbackQueue;

    public void setBuildId(UUID buildId) {
        this.buildId = buildId;
    }
    public UUID getBuildId() {
        return buildId;
    }

    public void setArtifactLocation(String artifactLocation) {
        this.artifactLocation = artifactLocation;
    }
    public String getArtifactLocation() {
        return artifactLocation;
    }

    public void setFeedbackQueue(String feedbackQueue) {
        this.feedbackQueue = feedbackQueue;
    }
    public String getFeedbackQueue() {
        return feedbackQueue;
    }
}
