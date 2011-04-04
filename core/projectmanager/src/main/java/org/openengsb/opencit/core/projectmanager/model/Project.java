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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.common.Domain;

@SuppressWarnings("serial")
public class Project implements Serializable {

    private Map<String, String> services;

    public enum State {
            OK,
            FAILURE;
    }

    private State state;
    private String id;
    private String notificationRecipient;
    private Date lastScmPollDate;

    public Project() {

    }

    public Project(String id) {
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
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
    public Map<Class<? extends Domain>, String> getServices() {
        if (services == null) {
            return null;
        }
        Map<Class<? extends Domain>, String> map = new HashMap<Class<? extends Domain>, String>(services.size());
        for (Entry<String, String> entry : services.entrySet()) {
            map.put(getDomainClass(entry.getKey()), entry.getValue());
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Domain> getDomainClass(String key) {
        try {
            return (Class<? extends Domain>) getClass().getClassLoader().loadClass(key);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }

    public void addService(Class<? extends Domain> type, String id) {
        if (services == null) {
            services = new HashMap<String, String>();
        }
        services.put(type.getName(), id);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Project)) {
            return false;
        }
        Project other = (Project) obj;
        return ObjectUtils.equals(id, other.id) && ObjectUtils.equals(state, other.state)
                && ObjectUtils.equals(notificationRecipient, other.notificationRecipient)
                && ObjectUtils.equals(services, other.services);
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
}
