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

package org.openengsb.opencit.core.projectmanager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceManager;
import org.openengsb.core.persistence.PersistenceService;
import org.openengsb.opencit.core.projectmanager.internal.ProjectManagerImpl;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class ProjectManagerImplTest {

    private ProjectManagerImpl projectManager;
    private PersistenceService persistenceMock;

    @Before
    public void setUp() throws Exception {
        projectManager = new ProjectManagerImpl();
        projectManager.setBundleContext(Mockito.mock(BundleContext.class));

        persistenceMock = Mockito.mock(PersistenceService.class);

        PersistenceManager persistenceManagerMock = Mockito.mock(PersistenceManager.class);
        Mockito.when(persistenceManagerMock.getPersistenceForBundle(Mockito.any(Bundle.class))).thenReturn(
            persistenceMock);
        projectManager.setPersistenceManager(persistenceManagerMock);
        projectManager.init();
    }

    private void addTestData() {
        Mockito.when(persistenceMock.query(Mockito.any(Project.class))).thenReturn(
            Arrays.asList(new Project[]{ new Project("test") }));
    }

    @Test
    public void createProject_shouldWork() throws ProjectAlreadyExistsException, PersistenceException {
        Project project = new Project("foo");
        projectManager.createProject(project);
        Mockito.verify(persistenceMock).create(project);
    }

    @Test(expected = ProjectAlreadyExistsException.class)
    public void createProjectTwice_shouldFail() throws ProjectAlreadyExistsException {
        addTestData();
        Project project = new Project("test");
        projectManager.createProject(project);
    }

    @Test
    public void getAllProjects_shouldWork() {
        addTestData();
        List<Project> allProjects = projectManager.getAllProjects();
        assertThat(allProjects.size(), is(1));
        assertThat(allProjects.get(0).getId(), is("test"));
    }

}
