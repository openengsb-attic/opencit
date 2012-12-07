/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.opencit.core.projectmanager.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.api.model.ConnectorId;

@SuppressWarnings("serial")
public class ProjectPersist implements Serializable {

    // Connectors
    private Map<String, ConnectorId> services;
    private Map<String, ConnectorConfig> connectorConfigs;

    // Project state
    private Project.State state;
    private String id;
    private String notificationRecipient;
    private Date lastScmPollDate;

    // Dependencies
    private Map<String, DependencyProperties> dependencies = new HashMap<String, DependencyProperties>();

    public ProjectPersist() {

    }

    public ProjectPersist(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setNotificationRecipient(String notificationRecipient) {
        this.notificationRecipient = notificationRecipient;
    }

    public String getNotificationRecipient() {
        return notificationRecipient;
    }

    public Project.State getState() {
        return state;
    }

    public void setState(Project.State state) {
        this.state = state;
    }

    public Date getLastScmPollDate() {
        return this.lastScmPollDate;
    }

    public void setLastScmPollDate(Date lastScmPollDate) {
        this.lastScmPollDate = lastScmPollDate;
    }

    /**
     *
     * the created services, key is the type, and value the id of the service
     */
    public Map<String, ConnectorId> getServices() {
        return services;
    }

    public void addService(String type, ConnectorId connectorId) {
        if (services == null) {
            services = new HashMap<String, ConnectorId>();
        }
        services.put(type, connectorId);
    }

    public Map<String, ConnectorConfig> getConnectorConfigs() {
        return connectorConfigs;
    }

    public void addConnectorConfig(String type, ConnectorConfig attribs) {
        if (connectorConfigs == null) {
            connectorConfigs = new HashMap<String, ConnectorConfig>();
        }
        connectorConfigs.put(type, attribs);
    }

    public void addDependency(DependencyProperties dep) {
        dependencies.put(dep.getId(), dep);
    }

    public Collection<DependencyProperties> getDependencies() {
        return dependencies.values();
    }

    private boolean objectEquals(Object o1, Object o2) {
        /* Needed for the persistence service */
        if (o1 == null || o2 == null) {
            return true;
        }
        return ObjectUtils.equals(o1, o2);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProjectPersist)) {
            return false;
        }
        ProjectPersist other = (ProjectPersist) obj;

        if (!objectEquals(id, other.id)) return false;
        if (!objectEquals(notificationRecipient, other.notificationRecipient)) return false;
        if (!objectEquals(services, other.services)) return false;
        if (!objectEquals(state, other.state)) return false;
        if (!objectEquals(connectorConfigs, other.connectorConfigs)) return false;
        /* Ignore the last poll date, it will go away soon */

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash += 31 * ObjectUtils.hashCode(id);
        hash += 31 * ObjectUtils.hashCode(state);
        hash += 31 * ObjectUtils.hashCode(notificationRecipient);
        hash += 31 * ObjectUtils.hashCode(services);
        return hash;
    }

    public DependencyProperties getDependency(String name) {
        return dependencies.get(name);
    }
}
