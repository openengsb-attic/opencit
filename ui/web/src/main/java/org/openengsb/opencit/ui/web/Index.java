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

package org.openengsb.opencit.ui.web;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.resource.ContextRelativeResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.ProjectModel;
import org.openengsb.opencit.ui.web.model.SpringBeanProvider;
import org.openengsb.opencit.ui.web.util.StateUtil;

public class Index extends BasePage implements SpringBeanProvider<ProjectManager> {

    @SpringBean
    private ProjectManager projectManager;
    private Label noProjects;
    private WebMarkupContainer projectListPanel;
    private ListView<Project> projectListView;

    public Index() {
        projectListPanel = new WebMarkupContainer("projectlistPanel");
        projectListPanel.setOutputMarkupId(true);
        add(projectListPanel);

        @SuppressWarnings("serial")
        IModel<List<Project>> projectsModel = new LoadableDetachableModel<List<Project>>() {
            @Override
            protected List<Project> load() {
                List<Project> projects = projectManager.getAllProjects();
                return projects;
            }
        };

        noProjects = new Label("noProjects", new StringResourceModel("noProjectsAvailable", this, null));
        noProjects.setOutputMarkupId(true);
        noProjects.setVisible(projectsModel.getObject().isEmpty());
        projectListPanel.add(noProjects);

        projectListView = createProjectListView(projectsModel, "projectlist");
        projectListView.setOutputMarkupId(true);
        projectListPanel.add(projectListView);

        this.add(new WizardLink("newProject", ProjectWizard.class));
    }

    @Override
    public ProjectManager getSpringBean() {
        return projectManager;
    }

    @SuppressWarnings("serial")
    private ListView<Project> createProjectListView(IModel<List<Project>> projectsModel,
            String id) {
        return new ListView<Project>(id, projectsModel) {

            @Override
            protected void populateItem(ListItem<Project> item) {
                Project project = item.getModelObject();
                item.add(new Label("project.name", project.getId()));
                String imageName = StateUtil.getImage(project);
                item.add(new Image("project.state", new ContextRelativeResource(imageName)));
                item.add(new Link<Project>("project.details", item.getModel()) {
                    @Override
                    public void onClick() {
                        ProjectModel projectModel = new ProjectModel(getModelObject());
                        projectModel.setProjectManagerProvider(Index.this);
                        setResponsePage(new ProjectDetails(projectModel));
                    }
                });
                ProjectModel projectModel = new ProjectModel(project);
                projectModel.setProjectManagerProvider(Index.this);
                item.add(new AjaxLink<Project>("deleteProject",
                        projectModel) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Project project = getModelObject();
                        getList().remove(project);
                        try {
                            projectManager.deleteProject(project.getId());
                        } catch (NoSuchProjectException e) {
                            throw new RuntimeException(e);
                        }
                        noProjects.setVisible(getList().size() <= 0);
                        target.addComponent(projectListPanel);
                        target.addComponent(noProjects);
                    }
                });
            }

        };
    }
}
