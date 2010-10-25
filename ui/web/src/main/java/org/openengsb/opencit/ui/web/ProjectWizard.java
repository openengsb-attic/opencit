package org.openengsb.opencit.ui.web;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class ProjectWizard extends Wizard {

    @SpringBean
    private ContextCurrentService contextSerice;

    private Project project;


    private Log log = LogFactory.getLog(this.getClass());

    private final class CreateProject extends WizardStep {

        public CreateProject() {
            super(new ResourceModel("newProject.title"), new ResourceModel("newProject.summary"),
                new Model<Project>(project));
            add(new RequiredTextField<String>("project.id"));
        }

        @Override
        public void applyState() {
            super.applyState();
            getDefaultModelObject();
        }
    }

    private final class SetSCM extends WizardStep {

        public SetSCM() {
            super(new ResourceModel("setUpSCM.title"), new ResourceModel("newProject.summary"));

        }
    }


    private final class FinalStep extends WizardStep {
        public FinalStep() {
            super(new ResourceModel("final.title"),new ResourceModel("final.summary"), new Model<Project>(project));
            add(new Label("projectId.confirm", new PropertyModel(project,"id")));
        }
    }


    public ProjectWizard(String id) {
        super(id);
        project = new Project();
        setDefaultModel(new CompoundPropertyModel<ProjectWizard>(this));
        WizardModel model = new WizardModel();
        model.add(new CreateProject());
        model.add(new FinalStep());
        init(model);
    }

    @Override
    public void onCancel() {
        setResponsePage(Index.class);
    }

    /**
     * @see org.apache.wicket.extensions.wizard.Wizard#onFinish()
     */
    @Override
    public void onFinish() {
        contextSerice.createContext(project.getId());
        setResponsePage(Index.class);
    }

    public Project getProject() {
        return project;
    }


    public void setProject(Project project) {
        this.project = project;
    }
}

