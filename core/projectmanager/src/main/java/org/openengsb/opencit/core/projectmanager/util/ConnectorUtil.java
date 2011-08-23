package org.openengsb.opencit.core.projectmanager.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.OsgiUtilsService;

public class ConnectorUtil {
    private OsgiUtilsService osgiUtilsService;

    public ConnectorUtil(OsgiUtilsService utils) {
        osgiUtilsService = utils;
    }
    
    public List<ConnectorProvider> findConnectorsForDomain(String domain) {
        List<ConnectorProvider> ret;
        ret = osgiUtilsService.listServices(ConnectorProvider.class, "(domain=" + domain + ")");
        return ret;
    }

    private static Map<String, String> nameMap = new HashMap<String, String>();
    static {
        nameMap.put("scm", "SCM Domain");
        nameMap.put("notification", "Notification Domain");
        nameMap.put("build", "Build Domain");
        nameMap.put("test", "Test Domain");
        nameMap.put("deploy", "Deploy Domain");
        nameMap.put("report", "Report Domain");
    }

    public String getDomainName(String domain) {
        if (nameMap.containsKey(domain)) {
            return nameMap.get(domain);
        } else {
            return domain;
        }
    }

}
