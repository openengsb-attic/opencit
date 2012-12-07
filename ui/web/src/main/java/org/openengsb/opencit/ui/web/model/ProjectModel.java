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

package org.openengsb.opencit.ui.web.model;

import org.apache.wicket.model.IModel;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;

@SuppressWarnings("serial")
public class ProjectModel implements IModel<Project> {

    private SpringBeanProvider<ProjectManager> projectManagerProvider;

    private String projectId;

    public ProjectModel(String projectId) {
        this.projectId = projectId;
    }

    public ProjectModel(Project project) {
        setObject(project);
    }

    @Override
    public Project getObject() {
        try {
            return projectManagerProvider.getSpringBean().getProject(projectId);
        } catch (NoSuchProjectException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setObject(Project project) {
        this.projectId = project.getId();
    }

    @Override
    public void detach() {
        // do nothing
    }

    public void setProjectManagerProvider(SpringBeanProvider<ProjectManager> projectManagerProvider) {
        this.projectManagerProvider = projectManagerProvider;
    }
}
