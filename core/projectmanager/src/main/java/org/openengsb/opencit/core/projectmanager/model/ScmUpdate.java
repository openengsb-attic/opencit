package org.openengsb.opencit.core.projectmanager.model;

import java.util.List;

import org.openengsb.domain.scm.CommitRef;

@SuppressWarnings("serial")
public class ScmUpdate extends BuildReason {
    private List<CommitRef> commits;

    public ScmUpdate(List<CommitRef> commits) {
        this.commits = commits;
    }
    
    public void setCommits(List<CommitRef> commits) {
        this.commits = commits;
    }

    public List<CommitRef> getCommits() {
        return commits;
    }

    @Override
    public String getDescription() {
        /* FIXME: Internationalization */
        return "SCM Update: " + commits;
    }
}
