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
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.domain.build.BuildDomain;
import org.openengsb.domain.deploy.DeployDomain;
import org.openengsb.domain.notification.NotificationDomain;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.domain.test.TestDomain;
import org.openengsb.opencit.core.config.OpenCitConfigurator;
import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.ProjectProperties;
import org.openengsb.ui.common.wicket.editor.ServiceEditorPanel;

public class CreateProject extends BasePage {

    @SpringBean
    private DomainService domainService;
    @SpringBean
    private ProjectManager projectManager;

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

        public Class<? extends Domain> domain;
        ServiceEditorPanel panel;
    }

    @SuppressWarnings("serial")
    class ConnectorModel implements IModel<String> {

        private Class<? extends Domain> domain;

        public ConnectorModel(Class<? extends Domain> d) {
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
        Form<ProjectProperties> projectForm = new Form<ProjectProperties>("form");

        projectForm.setModel(new CompoundPropertyModel<ProjectProperties>(project));
        projectForm.add(new RequiredTextField<String>("id"));
        projectForm.add(new RequiredTextField<String>("notificationRecipient"));

        ListView<Class<? extends Domain>> list;
        list = new ListView<Class<? extends Domain>>("domainList", OpenCitConfigurator.getRequiredServices()) {

            @Override
            protected void populateItem(ListItem<Class<? extends Domain>> arg0) {
                Class<? extends Domain> domain = arg0.getModelObject();

                arg0.add(new Label("domainName", getDomainName(domain)));
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

    private void createConnector(Project p, Class<? extends Domain> domain, String connector,
            Map<String, String> attributeValues) {
        String domainName = domain.getCanonicalName();
        ServiceManager serviceManager = findConnectorsForDomain(domain).get(connector);

        String serviceId = p.getId() + "-" + domainName;
        attributeValues.put("id", serviceId);

        serviceManager.update(serviceId, attributeValues);
        p.addService(domain, serviceId);
    }

    private void onSubmit() {
        log.info("OK was pressed!");

        Project p = new Project(project.getId());
        p.setNotificationRecipient(project.getNotificationRecipient());

        for (Class<? extends Domain >c : OpenCitConfigurator.getRequiredServices()) {
            createConnector(p, c, project.getDomainConnector(c), project.getDomainConfig(c));
        }

        try {
            projectManager.createProject(p);
            setResponsePage(getApplication().getHomePage());
        } catch (ProjectAlreadyExistsException e) {
            // FIXME
            log.error("This project already exists");
        }
    }

    private List<AttributeDefinition> buildAttributeList(ServiceManager service) {
        ServiceDescriptor descriptor = service.getDescriptor();
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        attributes.addAll(descriptor.getAttributes());
        return attributes;
    }

    private DropDownChoice<String> addConnectorDropdown(Class<? extends Domain> domain, String dropdown) {
        Map<String, ServiceManager> connectors = findConnectorsForDomain(domain);
        List<String> names = new ArrayList<String>(connectors.keySet());

        /* The unit tests do not have mocked connectors for all domains, so be prepared
         * for an empty list
         */
        if (names.size() > 0) {
            project.setDomainConnector(domain, names.get(0));
        }
        ConnectorModel model = new ConnectorModel(domain);
        DropDownChoice<String> ret = new DropDownChoice<String>(dropdown, model, names);
        return ret;
    }

    private ServiceEditorPanel addDomainSelection(Class<? extends Domain> domain, String proped,
            String curValue, Map<String, String> valueStore) {
        List<AttributeDefinition> attribs;
        Map<String, ServiceManager> connectors = findConnectorsForDomain(domain);
        if (curValue == null) {
            attribs = new LinkedList<AttributeDefinition>();
        } else {
            attribs = buildAttributeList(connectors.get(curValue));
        }
        ServiceEditorPanel panel = new ServiceEditorPanel(proped, attribs, valueStore);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Map<String, ServiceManager> findConnectorsForDomain(Class<? extends Domain> domain) {
        Map<String, ServiceManager> ret = new HashMap<String, ServiceManager>();

        List<ServiceManager> serviceManagers =
            domainService.serviceManagersForDomain(domain);

        for (ServiceManager sm : serviceManagers) {
            ret.put(sm.getDescriptor().getName().getString(getLocale()), sm);
        }

        return ret;
    }

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

}
