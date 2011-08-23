package org.openengsb.opencit.core.projectmanager.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class ConnectorUtil {
    private OsgiUtilsService osgiUtilsService;
    private ConnectorManager connectorManager;
    private static Log log = LogFactory.getLog(ConnectorUtil.class);

    public ConnectorUtil(OsgiUtilsService utils, ConnectorManager cm) {
        osgiUtilsService = utils;
        connectorManager = cm;
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

    private ConnectorProvider getConnectorProvider(String domain, String id) {
        List<ConnectorProvider> connectors = findConnectorsForDomain(domain);

        for(ConnectorProvider c : connectors) {
            if(c.getId().equals(id)) return c;
        }

        log.error("Cannot find ConnectorProvider with id " + id);
        return null;
    }

    public void createConnector(Project p, String domain, String connectorId,
            Map<String, String> attributeValues) throws ConnectorValidationFailedException {

        ConnectorProvider connector = getConnectorProvider(domain, connectorId);
        ConnectorId id = ConnectorId.generate(domain, connector.getId());
        ConnectorDescription desc = new ConnectorDescription();
        Map<String, Object> props = new HashMap<String, Object>();

        props.put("location." + p.getId(), domain);
        desc.setAttributes(attributeValues);
        desc.setProperties(props);
        connectorManager.create(id, desc);
        p.addService(domain, id);
    }

    public List<AttributeDefinition> buildAttributeList(String domain, String id) {
        ConnectorProvider service = getConnectorProvider(domain, id);
        ServiceDescriptor descriptor = service.getDescriptor();
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        attributes.addAll(descriptor.getAttributes());
        return attributes;
    }

}
