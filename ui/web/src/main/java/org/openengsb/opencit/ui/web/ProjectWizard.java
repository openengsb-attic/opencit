package org.openengsb.opencit.ui.web;

import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.openengsb.opencit.ui.web.model.ProjectModel;

public class ProjectWizard extends Wizard {

    ProjectModel project;


    private final class CreateProject extends WizardStep {

        public CreateProject() {
            super(new ResourceModel("newProject.title"), new ResourceModel("newProject.summary"));
            add(new RequiredTextField("project.projectId"));
        }
    }

    public ProjectWizard(String id) {
        super(id);
        setDefaultModel(new CompoundPropertyModel<ProjectWizard>(this));
        WizardModel model = new WizardModel();
        model.add(new CreateProject());
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
        setResponsePage(Index.class);
    }

    public ProjectModel getProject() {
        return project;
    }

    public void setProject(ProjectModel project) {
        this.project = project;
    }
}

