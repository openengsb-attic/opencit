package org.openengsb.opencit.ui.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class ProjectWizard extends Wizard {

    Project project;

    private Log log = LogFactory.getLog(this.getClass());

    private final class CreateProject extends WizardStep {

        public CreateProject() {

            super(new ResourceModel("newProject.title"), new ResourceModel("newProject.summary"), new Model<Project>(project));
            add(new RequiredTextField<String>("project.id"));
        }

        @Override
        public void applyState() {
            super.applyState();
            log.info(getDefaultModelObject());
        }

    }

    private final class SetSCM extends WizardStep {

        public SetSCM() {
            super(new ResourceModel("setUpSCM.title"), new ResourceModel("newProject.summary"));
        }
    }


    public ProjectWizard(String id) {
        super(id);
        project = new Project();
        setDefaultModel(new CompoundPropertyModel<ProjectWizard>(this));
        WizardModel model = new WizardModel();
        model.add(new CreateProject());
        model.add(new SetSCM());
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
        log.warn(project.getId());
        setResponsePage(Index.class);
    }

    public Project getProject() {
        return project;
    }



    public void setProject(Project project) {
        this.project = project;
    }
}

