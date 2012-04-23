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
import java.util.Date;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.MessageProducer;

import org.openengsb.core.api.model.ConnectorId;

@SuppressWarnings("serial")
public class Project implements Serializable {

    public enum State {
        OK,
        FAILURE;
    }

    private ProjectPersist persistent;

    private Destination topic;
    private MessageProducer producer;

    public Project() {
        persistent = new ProjectPersist();
    }

    public Project(String id) {
        persistent = new ProjectPersist(id);
    }

    public Project(ProjectPersist p) {
        persistent = p;
    }

    public String getId() {
        return persistent.getId();
    }

    public void setNotificationRecipient(String notificationRecipient) {
        persistent.setNotificationRecipient(notificationRecipient);
    }

    public String getNotificationRecipient() {
        return persistent.getNotificationRecipient();
    }

    public State getState() {
        return persistent.getState();
    }

    public void setState(State state) {
        persistent.setState(state);
    }

    public Date getLastScmPollDate() {
        return persistent.getLastScmPollDate();
    }

    public void setLastScmPollDate(Date lastScmPollDate) {
        persistent.setLastScmPollDate(lastScmPollDate);
    }

    /**
     *
     * the created services, key is the type, and value the id of the service
     */
    public Map<String, ConnectorId> getServices() {
        return persistent.getServices();
    }

    public void addService(String type, ConnectorId connectorId) {
        persistent.addService(type, connectorId);
    }

    public Map<String, ConnectorConfig> getConnectorConfigs() {
        return persistent.getConnectorConfigs();
    }

    public void addConnectorConfig(String type, ConnectorConfig attribs) {
        persistent.addConnectorConfig(type, attribs);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Project)) {
            return false;
        }
        Project other = (Project) obj;

        return persistent.equals(other.getPersitentPart());
    }

    public ProjectPersist getPersitentPart() {
        return persistent;
    }

    @Override
    public int hashCode() {
        return persistent.hashCode();
    }

    public void setTopic(Destination topic) {
        this.topic = topic;
    }

    public void addDependency(DependencyProperties dependency) {
        persistent.addDependency(dependency);
    }

    public Destination getTopic() {
        return topic;
    }

    public void setProducer(MessageProducer producer) {
        this.producer = producer;
    }

    public MessageProducer getProducer() {
        return producer;
    }
}
