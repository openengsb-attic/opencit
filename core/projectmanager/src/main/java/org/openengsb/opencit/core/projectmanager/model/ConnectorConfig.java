package org.openengsb.opencit.core.projectmanager.model;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("serial")
public class ConnectorConfig implements Serializable {

    private String connector;
    private Map<String, String> attributeValues;

    public ConnectorConfig(String c, Map<String, String> attributeValues) {
        this.connector = c;
        this.attributeValues = attributeValues;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public String getConnector() {
        return connector;
    }

    public void setAttributeValues(Map<String, String> attributeValues) {
        this.attributeValues = attributeValues;
    }

    public Map<String, String> getAttributeValues() {
        return attributeValues;
    }
}
