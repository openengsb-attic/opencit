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

package org.openengsb.opencit.ui.web.model;

import org.apache.wicket.model.LoadableDetachableModel;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;

@SuppressWarnings("serial")
public class ProjectModel extends LoadableDetachableModel<Project> {

    private SpringBeanProvider<ProjectManager> projectManagerProvider;

    private String projectId;

    public ProjectModel(String projectId) {
        this.projectId = projectId;
    }

    public ProjectModel(Project project) {
        this.projectId = project.getId();
        setObject(project);
    }

    @Override
    protected Project load() {
        try {
            return projectManagerProvider.getSpringBean().getProject(projectId);
        } catch (NoSuchProjectException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setProjectManagerProvider(SpringBeanProvider<ProjectManager> projectManagerProvider) {
        this.projectManagerProvider = projectManagerProvider;
    }
}