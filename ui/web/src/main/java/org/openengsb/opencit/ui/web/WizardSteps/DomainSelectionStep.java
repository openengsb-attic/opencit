/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.opencit.ui.web.WizardSteps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardStep;
import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.openengsb.core.common.Domain;
import org.openengsb.domain.build.BuildDomain;
import org.openengsb.domain.deploy.DeployDomain;
import org.openengsb.domain.notification.NotificationDomain;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.domain.test.TestDomain;
import org.openengsb.opencit.core.config.OpenCitConfigurator;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class DomainSelectionStep extends DynamicWizardStep {

    private String domainDropDown = "";

    private Project project;
    private Map<String, Class<? extends Domain>> managersMap = new HashMap<String, Class<? extends Domain>>();

    private static Map<Class<? extends Domain>, String> nameMap = new HashMap<Class<? extends Domain>, String>();
    static {
        nameMap.put(ScmDomain.class, "SCM Domain");
        nameMap.put(NotificationDomain.class, "Notification Domain");
        nameMap.put(BuildDomain.class, "Build Domain");
        nameMap.put(TestDomain.class, "Test Domain");
        nameMap.put(DeployDomain.class, "Deploy Domain");
        nameMap.put(ReportDomain.class, "Report Domain");
    }

    private String getDomainName(Class<? extends Domain> clazz) {
        if (nameMap.containsKey(clazz)) {
            return nameMap.get(clazz);
        } else {
            return clazz.getCanonicalName();
        }
    }

    public DomainSelectionStep(Project project) {
        super(new CreateProjectStep(project), new ResourceModel("selectDomain.title"),
            new ResourceModel("selectDomain.summary"), new Model<Project>(project));
        this.project = project;

        Map<Class<? extends Domain>, String> services = project.getServices();
        for (Class<? extends Domain> i : OpenCitConfigurator.getRequiredServices()) {
            if (!services.containsKey(i)) {
                managersMap.put(getDomainName(i), i);
            }
        }

        domainDropDown = managersMap.keySet().iterator().next();

        DropDownChoice<String> descriptorDropDownChoice = initSCMDomains();
        add(descriptorDropDownChoice);
    }

    private DropDownChoice<String> initSCMDomains() {

        IModel<List<String>> dropDownModel = new LoadableDetachableModel<List<String>>() {
            @Override
            protected List<String> load() {
                Set<String> domains = new HashSet<String>(managersMap.keySet());
                return new ArrayList<String>(domains);
            }
        };

        DropDownChoice<String> descriptorDropDownChoice =
            new DropDownChoice<String>("domainDropDown", new IModel<String>() {
                @Override
                public String getObject() {
                    return domainDropDown;
                }

                @Override
                public void setObject(String object) {
                    domainDropDown = object;
                }

                @Override
                public void detach() {
                }
            }, dropDownModel);
        return descriptorDropDownChoice;
    }

    @Override
    public boolean isLastStep() {
        return false;
    }

    @Override
    public IDynamicWizardStep next() {
        Class<? extends Domain> domain = managersMap.get(domainDropDown);
        return new SelectServiceStep(project, domain);
    }

    @Override
    public boolean isComplete() {
        return domainDropDown != null && !"".equals(domainDropDown);
    }
}
