package org.openengsb.opencit.core.projectmanager.util;

import java.util.List;

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

}
