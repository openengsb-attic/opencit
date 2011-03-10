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

package org.openengsb.opencit.core.projectmanager.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.security.BundleAuthenticationToken;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyPersistence;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.Project.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class ProjectManagerImplTest extends AbstractOsgiMockServiceTest {

    private ProjectManagerImpl projectManager;
    private ContextCurrentService contextMock;
    private DummyPersistence persistence;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        projectManager = new ProjectManagerImpl();
        projectManager.setBundleContext(bundleContext);

        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        BundleAuthenticationToken bundleAuthenticationToken =
            new BundleAuthenticationToken("", "", new ArrayList<GrantedAuthority>());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(bundleAuthenticationToken);
        projectManager.setAuthenticationManager(authenticationManager);

        contextMock = Mockito.mock(ContextCurrentService.class);

        Mockito.when(contextMock.getThreadLocalContext()).thenReturn("test");

        PersistenceManager persistenceManagerMock = Mockito.mock(PersistenceManager.class);
        persistence = new DummyPersistence();
        Mockito.when(persistenceManagerMock.getPersistenceForBundle(Mockito.any(Bundle.class))).thenReturn(
            persistence);
        projectManager.setPersistenceManager(persistenceManagerMock);
        projectManager.setContextService(contextMock);
        projectManager.setScmDomain(Mockito.mock(ScmDomain.class));
        projectManager.setReportDomain(Mockito.mock(ReportDomain.class));
        projectManager.init();
    }

    private void addTestData() throws PersistenceException {
        Project project = new Project("test");
        project.setState(State.OK);
        persistence.create(project);
    }

    @Test
    public void createProject_shouldWork() throws Exception {
        Project project = new Project("foo");
        projectManager.createProject(project);
        List<Project> allProjects = projectManager.getAllProjects();
        assertThat(allProjects, hasItem(project));
    }

    @Test(expected = ProjectAlreadyExistsException.class)
    public void createProjectTwice_shouldFail() throws Exception {
        addTestData();
        Project project = new Project("test");
        projectManager.createProject(project);
    }

    @Test
    public void getAllProjects_shouldWork() throws Exception {
        addTestData();
        List<Project> allProjects = projectManager.getAllProjects();
        assertThat(allProjects.size(), is(1));
        assertThat(allProjects.get(0).getId(), is("test"));
    }

    @Test
    public void getProject_souldWork() throws Exception {
        addTestData();
        Project project = projectManager.getProject("test");
        assertThat(project.getId(), is("test"));
        assertThat(project.getState(), is(State.OK));
    }

    @Test(expected = NoSuchProjectException.class)
    public void getProject_souldFail() throws NoSuchProjectException {
        projectManager.getProject("test");
    }

    @Test
    public void updateProject_shouldWork() throws NoSuchProjectException, PersistenceException {
        addTestData();
        Project project = new Project("test");
        project.setState(State.IN_PROGRESS);
        projectManager.updateProject(project);
        List<Project> allProjects = projectManager.getAllProjects();
        assertThat(allProjects, hasItem(project));
    }

    @Test(expected = NoSuchProjectException.class)
    public void updateProject_souldFail() throws NoSuchProjectException {
        projectManager.updateProject(new Project("test"));
    }

    @Test
    public void deleteProject_souldWork() throws NoSuchProjectException, PersistenceException {
        addTestData();
        projectManager.deleteProject("test");
        List<Project> allProjects = projectManager.getAllProjects();
        assertThat(allProjects.isEmpty(), is(true));
    }

    @Test(expected = NoSuchProjectException.class)
    public void deleteProject_souldFail() throws NoSuchProjectException {
        projectManager.deleteProject("test");
    }

    @Test
    public void updateCurrentContextProjectState_shouldWork() throws Exception {
        addTestData();
        projectManager.updateCurrentContextProjectState(State.FAILURE);
        Project project = projectManager.getProject("test");
        assertThat(project.getState(), is(State.FAILURE));
    }

    @Test(expected = NoSuchProjectException.class)
    public void updateCurrentContextProjectState_shouldFail() throws NoSuchProjectException {
        projectManager.updateCurrentContextProjectState(State.FAILURE);
    }

    @Test(timeout = 10000)
    public void createProjectShouldStartPoller() throws Exception {
        Project project = new Project("test2");
        project.setNotificationRecipient("test@test.com");

        ScmDomain scmMock = mockDomain(ScmDomain.class);
        projectManager.setScmDomain(scmMock);
        when(scmMock.poll()).thenReturn(false);
        projectManager.createProject(project);
        Thread.sleep(200);
        ScheduledFuture<?> scheduledFuture = projectManager.pollers.get("test2");
        while (scheduledFuture.getDelay(TimeUnit.SECONDS) <= 0) {
            /* sorry for the busy-waiting */
            Thread.yield();
        }
        System.out.println(scheduledFuture.isDone());
        verify(scmMock).poll();
    }

    private <T> T mockDomain(Class<T> domainClass) throws InvalidSyntaxException {
        T mock = mock(domainClass);
        String id = "my" + domainClass.getSimpleName();
        registerService(mock, id, Domain.class, domainClass);

        when(contextMock.getValue("/domain/" + domainClass.getSimpleName() + "/defaultConnector/id")).thenReturn(id);
        return mock;
    }
}
