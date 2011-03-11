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

package org.openengsb.opencit.ui.web.model;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.model.LoadableDetachableModel;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.service.DomainService;

public class ManagerMapModel extends LoadableDetachableModel<Map<String, ServiceManager>> {

    private SpringBeanProvider<DomainService> domainServiceProvider;

    private Class<? extends Domain> domain;

    private Locale locale;

    public ManagerMapModel(Class<? extends Domain> domain, Locale locale) {
        this.domain = domain;
        this.locale = locale;
    }

    @Override
    protected Map<String, ServiceManager> load() {
        List<ServiceManager> serviceManagers =
            domainServiceProvider.getSpringBean().serviceManagersForDomain(domain);
        Map<String, ServiceManager> managersMap = new HashMap<String, ServiceManager>();
        for (ServiceManager sm : serviceManagers) {
            managersMap.put(sm.getDescriptor().getName().getString(locale), sm);
        }
        return managersMap;
    }

    public void setDomainServiceProvider(SpringBeanProvider<DomainService> domainServiceProvider) {
        this.domainServiceProvider = domainServiceProvider;
    }

}
