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
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.DependencyProperties;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.util.ConnectorUtil;
import org.openengsb.ui.common.editor.ServiceEditorPanel;
import org.ops4j.pax.wicket.api.PaxWicketBean;

public class AddDependency extends BasePage {
    @PaxWicketBean
    private ProjectManager projectManager;
    @PaxWicketBean
    private ConnectorUtil connectorUtil;

    private static Log log = LogFactory.getLog(AddDependency.class);
    
    private final String dependencyDomain = "dependency";

    private Form<DependencyProperties> dependencyForm;
    private DependencyProperties dependency = new DependencyProperties();

    public AddDependency() {
        init();
    }

    @SuppressWarnings("serial")
    private void init() {
        dependencyForm = new Form<DependencyProperties>("form");

        dependencyForm.setModel(new CompoundPropertyModel<DependencyProperties>(dependency));
        dependencyForm.add(new RequiredTextField<String>("id"));
        dependencyForm.add(new RequiredTextField<String>("topic"));

        DropDownChoice<String> dropdown = addConnectorDropdown(dependencyDomain, "connector");
        dependencyForm.add(dropdown);
        ServiceEditorPanel panel;
        panel = addDomainSelection(dependencyDomain, "editor", dependency.getConnector(),
                dependency.getConfig());
        dependencyForm.add(panel);
        MyAjaxFormComponentUpdatingBehavior update = new MyAjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                ServiceEditorPanel panel2 = addDomainSelection(dependencyDomain, "editor",
                        dependency.getConnector(), dependency.getConfig());
                panel.replaceWith(panel2);
                target.addComponent(panel2);
                panel = panel2;
            }
        };
        
        update.panel = panel;
        dropdown.add(update);

        Button ok = new Button("okButton") {
            public void onSubmit() {
                AddDependency.this.onSubmit();
            }
        };
        dependencyForm.add(ok);
        Button cancel = new Button("cancelButton") {
            public void onSubmit() {
                onCancel();
            }
        };
        cancel.setDefaultFormProcessing(false);
        dependencyForm.add(cancel);

        add(dependencyForm);
    }

    private void onCancel() {
        log.info("Cancel was pressed!");
        setResponsePage(ProjectDetails.class);
    }

    private void onSubmit() {
        log.info("Adding dependency " + dependency.getId());

        Project project = projectManager.getCurrentContextProject();
        try {
            projectManager.addProjectDependency(project, dependency);
        } catch (ConnectorValidationFailedException e) {
            // FIXME: Properly report this to the user
            log.error("Failed to add dependency: ", e);
        }

        setResponsePage(ProjectDetails.class);
    }

    private DropDownChoice<String> addConnectorDropdown(String domain, String dropdown) {
        List<ConnectorProvider> connectors = connectorUtil.findConnectorsForDomain(domain);
        List<String> names = new LinkedList<String>();
        String[] queue_to_back = { "composite-connector", "external-connector-proxy" };

        for (ConnectorProvider c : connectors) {
            names.add(c.getId());
        }

        Collections.sort(names);
        for(String s : queue_to_back) {
            int index = names.indexOf(s);
            if (index == -1) {
                continue;
            }
            names.remove(index);
            names.add(s);
        }

        /* The unit tests do not have mocked connectors for all domains, so be prepared
         * for an empty list
         */
        if (names.size() > 0) {
            dependency.setConnector(names.get(0));
        }
        ConnectorModel model = new ConnectorModel();
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
            properties, dependencyForm);
        panel.setOutputMarkupId(true);
        return panel;
    }

    @SuppressWarnings("serial")
    class ConnectorModel implements IModel<String> {
        @Override
        public String getObject() {
            return dependency.getConnector();
        }

        @Override
        public void setObject(String arg0) {
            dependency.setConnector(arg0);
        }

        @Override
        public void detach() {
            // Do nothing
        }

    }

    @SuppressWarnings("serial")
    abstract class MyAjaxFormComponentUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {
        public MyAjaxFormComponentUpdatingBehavior(String event) {
            super(event);
        }
        ServiceEditorPanel panel;
    }
}
