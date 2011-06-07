package org.openengsb.opencit.ui.web.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ProjectProperties implements Serializable {
    private String id;
    private String notificationRecipient;

    private Map<String, Map<String, String>> cfgs =
        new HashMap<String, Map<String, String>>();
    private Map<String, String> connectors =
        new HashMap<String, String>();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDomainConnector(String domain, String con) {
        connectors.put(domain, con);
    }

    public String getDomainConnector(String domain) {
        return connectors.get(domain);
    }

    public void setDomainConfig(String domain, Map<String, String> cfg) {
        cfgs.put(domain, cfg);
    }

    public Map<String, String> getDomainConfig(String domain) {
        Map<String, String> ret = cfgs.get(domain);
        if (ret == null) {
            ret = new HashMap<String, String>();
            cfgs.put(domain, ret);
        }
        return ret;
    }

    public void setNotificationRecipient(String notificationRecipient) {
        this.notificationRecipient = notificationRecipient;
    }

    public String getNotificationRecipient() {
        return notificationRecipient;
    }
}
