package org.openengsb.opencit.core.projectmanager.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class BuildReason implements Serializable {
    public abstract String getDescription();
}
