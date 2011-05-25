package org.openengsb.opencit.ui.web;

import java.util.ArrayList;
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
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.opencit.core.config.OpenCitConfigurator;
import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.ProjectProperties;
import org.openengsb.ui.common.editor.ServiceEditorPanel;

public class CreateProject extends BasePage {

    @SpringBean
    private OsgiUtilsService osgiUtilsService;
    @SpringBean
    private ProjectManager projectManager;
    @SpringBean
    private ConnectorManager connectorManager;

    ProjectProperties project = new ProjectProperties();
    private static Log log = LogFactory.getLog(CreateProject.class);

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
    class ConnectorModel implements IModel<ConnectorProvider> {

        private String domain;

        public ConnectorModel(String d) {
            domain = d;
        }

        @Override
        public ConnectorProvider getObject() {
            return project.getDomainConnector(domain);
        }

        @Override
        public void setObject(ConnectorProvider arg0) {
            project.setDomainConnector(domain, arg0);
        }

        @Override
        public void detach() {
            // Do nothing
        }

    }

    @SuppressWarnings("serial")
    private void init() {
        Form<ProjectProperties> projectForm = new Form<ProjectProperties>("form");

        projectForm.setModel(new CompoundPropertyModel<ProjectProperties>(project));
        projectForm.add(new RequiredTextField<String>("id"));
        projectForm.add(new RequiredTextField<String>("notificationRecipient"));

        ListView<String> list;
        list = new ListView<String>("domainList", OpenCitConfigurator.getRequiredServices()) {

            @Override
            protected void populateItem(ListItem<String> arg0) {
                String domain = arg0.getModelObject();

                arg0.add(new Label("domainName", getDomainName(domain)));
                DropDownChoice<ConnectorProvider> dropdown = addConnectorDropdown(domain, "connector");
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

    private void createConnector(Project p, String domain, ConnectorProvider connector,
            Map<String, String> attributeValues) throws ConnectorValidationFailedException {

        ConnectorId id = ConnectorId.generate(domain, connector.getId());
        ConnectorDescription desc = new ConnectorDescription();
        desc.setAttributes(attributeValues);
        connectorManager.create(id, desc);
        p.addService(domain, id);
    }

    private void onSubmit() {
        log.info("OK was pressed!");

        Project p = new Project(project.getId());
        p.setNotificationRecipient(project.getNotificationRecipient());

        for (String c : OpenCitConfigurator.getRequiredServices()) {
            try {
                createConnector(p, c, project.getDomainConnector(c), project.getDomainConfig(c));
            } catch (ConnectorValidationFailedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                log.error("Connector creation failed!");
                /* TODO: Clean up old connectors and tell the user he f***ed up */
            }
        }

        try {
            projectManager.createProject(p);
            setResponsePage(getApplication().getHomePage());
        } catch (ProjectAlreadyExistsException e) {
            // FIXME
            log.error("This project already exists");
        }
    }

    private List<AttributeDefinition> buildAttributeList(ConnectorProvider service) {
        ServiceDescriptor descriptor = service.getDescriptor();
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        attributes.addAll(descriptor.getAttributes());
        return attributes;
    }

    private DropDownChoice<ConnectorProvider> addConnectorDropdown(String domain, String dropdown) {
        List<ConnectorProvider> names = findConnectorsForDomain(domain);

        /* The unit tests do not have mocked connectors for all domains, so be prepared
         * for an empty list
         */
        if (names.size() > 0) {
            project.setDomainConnector(domain, names.get(0));
        }
        ConnectorModel model = new ConnectorModel(domain);
        DropDownChoice<ConnectorProvider> ret = new DropDownChoice<ConnectorProvider>(dropdown, model, names);
        return ret;
    }

    private ServiceEditorPanel addDomainSelection(String domain, String proped,
            ConnectorProvider curValue, Map<String, String> valueStore) {
        List<AttributeDefinition> attribs;
        if (curValue == null) {
            attribs = new LinkedList<AttributeDefinition>();
        } else {
            attribs = buildAttributeList(curValue);
        }
        /* FIXME!!! */
        ServiceEditorPanel panel = new ServiceEditorPanel(proped, attribs, valueStore, null, null);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private List<ConnectorProvider> findConnectorsForDomain(String domain) {
        return osgiUtilsService.listServices(ConnectorProvider.class, "(domain=" + domain + ")");
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

    private String getDomainName(String domain) {
        if (nameMap.containsKey(domain)) {
            return nameMap.get(domain);
        } else {
            return domain;
        }
    }

}
