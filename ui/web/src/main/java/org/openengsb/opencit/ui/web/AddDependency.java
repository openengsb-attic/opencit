package org.openengsb.opencit.ui.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.openengsb.opencit.core.config.OpenCitConfigurator;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.util.ConnectorUtil;
import org.openengsb.opencit.ui.web.CreateProject.MyAjaxFormComponentUpdatingBehavior;
import org.openengsb.opencit.ui.web.model.DependencyProperties;
import org.openengsb.opencit.ui.web.model.ProjectProperties;
import org.openengsb.ui.common.editor.ServiceEditorPanel;
import org.ops4j.pax.wicket.api.PaxWicketBean;

public class AddDependency extends BasePage {
    @PaxWicketBean
    private ProjectManager projectManager;
    @PaxWicketBean
    private ConnectorUtil connectorUtil;

    private static Log log = LogFactory.getLog(AddDependency.class);

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
        setResponsePage(getApplication().getHomePage());
    }

    private void onSubmit() {
        // TODO Auto-generated method stub
        
    }
}
