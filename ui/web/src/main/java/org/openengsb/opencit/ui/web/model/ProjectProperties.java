package org.openengsb.opencit.ui.web.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.common.Domain;

@SuppressWarnings("serial")
public class ProjectProperties implements Serializable {
    private String id;
    private String notificationRecipient;

    private Map<Class<? extends Domain>, Map<String, String>> cfgs =
        new HashMap<Class<? extends Domain>, Map<String, String>>();
    private Map<Class<? extends Domain>, String> connectors =
        new HashMap<Class<? extends Domain>, String>();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDomainConnector(Class<? extends Domain> domain, String con) {
        connectors.put(domain, con);
    }

    public String getDomainConnector(Class<? extends Domain> domain) {
        return connectors.get(domain);
    }

    public void setDomainConfig(Class<? extends Domain> domain, Map<String, String> cfg) {
        cfgs.put(domain, cfg);
    }

    public Map<String, String> getDomainConfig(Class<? extends Domain> domain) {
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
