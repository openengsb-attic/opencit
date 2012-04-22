package org.openengsb.opencit.core.projectmanager.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class DependencyProperties implements Serializable {
    private String id;
    private String topic;
    private String connector;
    private Map<String, String> config = new HashMap<String, String>();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public String getConnector() {
        return connector;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public Map<String, String> getConfig() {
        return config;
    }
}
