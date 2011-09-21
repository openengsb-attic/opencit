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
package org.openengsb.opencit.ui.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.opencit.core.config.OpenCitConfigurator;
import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.ConnectorConfig;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.util.ConnectorUtil;
import org.openengsb.opencit.ui.web.model.ProjectProperties;
import org.openengsb.ui.common.editor.ServiceEditorPanel;

public class CreateProject extends BasePage {

    @SpringBean
    private ProjectManager projectManager;
    @SpringBean
    private ConnectorUtil connectorUtil;

    ProjectProperties project = new ProjectProperties();
    private static Log log = LogFactory.getLog(CreateProject.class);
    private Form<ProjectProperties> projectForm;

    public CreateProject() {
        init();
    }

    @SuppressWarnings("serial")
    abstract class MyAjaxFormComponentUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {
        public MyAjaxFormComponentUpdatingBehavior(String event) {
            super(event);
        }

        public String domain;
        ServiceEditorPanel panel;
    }

    @SuppressWarnings("serial")
    class ConnectorModel implements IModel<String> {

        private String domain;

        public ConnectorModel(String d) {
            domain = d;
        }

        @Override
        public String getObject() {
            return project.getDomainConnector(domain);
        }

        @Override
        public void setObject(String arg0) {
            project.setDomainConnector(domain, arg0);
        }

        @Override
        public void detach() {
            // Do nothing
        }

    }

    @SuppressWarnings("serial")
    private void init() {
        projectForm = new Form<ProjectProperties>("form");

        projectForm.setModel(new CompoundPropertyModel<ProjectProperties>(project));
        projectForm.add(new RequiredTextField<String>("id"));
        projectForm.add(new RequiredTextField<String>("notificationRecipient"));

        ListView<String> list;
        list = new ListView<String>("domainList", OpenCitConfigurator.getRequiredServices()) {

            @Override
            protected void populateItem(ListItem<String> arg0) {
                String domain = arg0.getModelObject();

                arg0.add(new Label("domainName", connectorUtil.getDomainName(domain)));
                DropDownChoice<String> dropdown = addConnectorDropdown(domain, "connector");
                arg0.add(dropdown);
                ServiceEditorPanel panel;
                panel = addDomainSelection(domain, "editor", project.getDomainConnector(domain),
                    project.getDomainConfig(domain));
                arg0.add(panel);
                MyAjaxFormComponentUpdatingBehavior update = new MyAjaxFormComponentUpdatingBehavior("onchange") {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        ServiceEditorPanel panel2 = addDomainSelection(domain, "editor",
                            project.getDomainConnector(domain), project.getDomainConfig(domain));
                        panel.replaceWith(panel2);
                        target.addComponent(panel2);
                        panel = panel2;
                    }
                };
                
        update.domain = domain;
                update.panel = panel;
                dropdown.add(update);
            }
        };
        projectForm.add(list);

        Button ok = new Button("okButton") {
            public void onSubmit() {
                CreateProject.this.onSubmit();
            }
        };
        projectForm.add(ok);
        Button cancel = new Button("cancelButton") {
            public void onSubmit() {
                onCancel();
            }
        };
        cancel.setDefaultFormProcessing(false);
        projectForm.add(cancel);

        add(projectForm);
    }

    private void onCancel() {
        log.info("Cancel was pressed!");
        setResponsePage(getApplication().getHomePage());
    }

    private void onSubmit() {
        log.info("OK was pressed!");

        Project p = new Project(project.getId());
        p.setNotificationRecipient(project.getNotificationRecipient());

        for (String c : OpenCitConfigurator.getRequiredServices()) {
            String connector = project.getDomainConnector(c);
            p.addConnectorConfig(c, new ConnectorConfig(connector, project.getDomainConfig(c)));
        }

        try {
            projectManager.createProject(p);
            setResponsePage(getApplication().getHomePage());
        } catch (ProjectAlreadyExistsException e) {
            // FIXME
            log.error("This project already exists", e);
        } catch (ConnectorValidationFailedException e) {
            // FIXME
            log.error("Connector failed validation", e);
        }
    }

    private DropDownChoice<String> addConnectorDropdown(String domain, String dropdown) {
        List<ConnectorProvider> connectors = connectorUtil.findConnectorsForDomain(domain);
        List<String> names = new LinkedList<String>();

        for(ConnectorProvider c : connectors) {
            names.add(c.getId());
        }
        
        /* The unit tests do not have mocked connectors for all domains, so be prepared
         * for an empty list
         */
        if (names.size() > 0) {
            project.setDomainConnector(domain, names.get(0));
        }
        ConnectorModel model = new ConnectorModel(domain);
        Collections.sort(names);
        DropDownChoice<String> ret = new DropDownChoice<String>(dropdown, model, names);
        return ret;
    }

    private ServiceEditorPanel addDomainSelection(String domain, String proped,
            String curValue, Map<String, String> valueStore) {
        List<AttributeDefinition> attribs;
        Map<String, Object> properties;
        if (curValue == null) {
            attribs = new LinkedList<AttributeDefinition>();
            properties = new HashMap<String, Object>(); 
        } else {
            attribs = connectorUtil.buildAttributeList(domain, curValue);
            properties = new HashMap<String, Object>();
        }

        ServiceEditorPanel panel = new ServiceEditorPanel(proped, attribs, valueStore,
            properties, projectForm);
        panel.setOutputMarkupId(true);
        return panel;
    }
}
