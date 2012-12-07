package org.openengsb.opencit.core.projectmanager.model;

@SuppressWarnings("serial")
public class ScmUpdate extends BuildReason {
    private String commitId;

    public ScmUpdate(String commitId) {
        this.commitId = commitId;
    }
    
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitId() {
        return commitId;
    }

    @Override
    public String getDescription() {
        /* FIXME: Internationalization */
        return "SCM Update: " + commitId;
    }
}
