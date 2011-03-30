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

package org.openengsb.opencit.ui.web.WizardSteps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardStep;
import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.opencit.core.config.OpenCitConfigurator;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.ServiceManagerModel;
import org.openengsb.ui.common.wicket.editor.ServiceEditorPanel;

@SuppressWarnings("serial")
public class SetAttributesStep extends DynamicWizardStep {
    private Project project;
    private Panel editor;
    private FeedbackPanel feedbackPanel;
    private ServiceManagerModel serviceManager;
    private String serviceId;
    private Map<String, String> attributeValues;

    public SetAttributesStep(final Project project,
            final ServiceManagerModel serviceManager) {
        super(new CreateProjectStep(project), new ResourceModel(
                "setAttribute.title"),
                new ResourceModel("setAttribute.summary"), new Model<Project>(
                        project));
        this.project = project;
        this.serviceManager = serviceManager;
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
        setComplete(false);

        IModel<List<AttributeDefinition>> attributes = new LoadableDetachableModel<List<AttributeDefinition>>() {
            @Override
            protected List<AttributeDefinition> load() {
                return serviceManager.getObject().getDescriptor().getAttributes();
            }
        };
        attributeValues = new HashMap<String, String>();

        editor = new ServiceEditorPanel("editor", attributes.getObject(), attributeValues);
        add(editor);
    }

    @Override
    public boolean isLastStep() {
        return false;
    }

    @Override
    public IDynamicWizardStep next() {
        Map<Class<? extends Domain>, String> configured = project.getServices();
        for (Class<? extends Domain> c : OpenCitConfigurator
                .getRequiredServices()) {
            if (configured == null || !configured.containsKey(c)) {
                return new DomainSelectionStep(project);
            }
        }
        return new FinalStep(this, project);
    }

    @Override
    public void applyState() {
        String domain = serviceManager.getObject().getDescriptor().getServiceType().getCanonicalName();
        String projName = SetAttributesStep.this.project.getId();
        serviceId = projName + "-" + domain;
        attributeValues.put("id", serviceId);
        serviceManager.getObject().update(serviceId, attributeValues);
        setComplete(true);
        project.addService(serviceManager.getObject().getDescriptor()
                .getServiceType(), serviceId);
        setComplete(true);
    }

}
