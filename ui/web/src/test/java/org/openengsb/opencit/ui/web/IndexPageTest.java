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

package org.openengsb.opencit.ui.web;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.services.internal.ConnectorManagerImpl;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.Project.State;

public class IndexPageTest extends AbstractCitPageTest {

    private ProjectManager projectManager;
    private WicketTester wicketTester;
    private ContextCurrentService contextService;
    private Project testProject;
    private ReportDomain reportMock = mock(ReportDomain.class);
    private ConnectorManager connectorManager;

    @Before
    public void setUp() throws NoSuchProjectException {
        wicketTester = getTester();
        testProject = new Project("test");
        Project testProject = new Project("test");
        testProject.setState(State.OK);
        when(projectManager.getProject("test")).thenReturn(testProject);
        ContextHolder.get().setCurrentContextId("test");

        WiringService wiringService = Mockito.mock(WiringService.class);
        when(wiringService.getDomainEndpoint(ReportDomain.class, "report")).thenReturn(reportMock);
        registerServiceViaId(wiringService, "wiring", WiringService.class);
    }

    @Override
    protected Map<String, Object> getBeansForAppContextAsMap() {
        Map<String, Object> mockedBeansMap = new HashMap<String, Object>();
        projectManager = Mockito.mock(ProjectManager.class);

        contextService = mock(ContextCurrentService.class);
        mockedBeansMap.put("contextCurrentService", contextService);
        mockedBeansMap.put("projectManager", projectManager);
        mockedBeansMap.put("reportDomain", reportMock);
        mockedBeansMap.put("workflowService", mock(WorkflowService.class));
        SchedulingService scheduler = mock(SchedulingService.class);
        mockedBeansMap.put("scheduler", scheduler);
        connectorManager = new ConnectorManagerImpl();
        mockedBeansMap.put("connectorManager", connectorManager);
        return mockedBeansMap;
    }

    @Test
    public void testProjectlistHeaderPresent_shouldWork() {
        Page indexPage = wicketTester.startPage(new Index());
        getTester().assertContains(indexPage.getString("projectlist.title"));
    }

    @Test
    public void testNoProjects_shouldShowLabel() {
        Page indexPage = wicketTester.startPage(new Index());
        getTester().assertContains(indexPage.getString("noProjectsAvailable"));
    }

    @Test
    public void testProjectsAvailable_shouldShowProjectId() throws NoSuchProjectException {
        when(projectManager.getAllProjects()).thenReturn(Arrays.asList(new Project[]{ testProject }));
        when(projectManager.getProject("test")).thenReturn(testProject);
        when(projectManager.getCurrentContextProject()).thenReturn(testProject);
        getTester().startPage(new Index());
        getTester().assertContains("test");
        String item = "projectlistPanel:projectlist:0";
        Image image = (Image) getTester().getComponentFromLastRenderedPage(item + ":project.state");
        Link<?> link = (Link<?>) getTester().getComponentFromLastRenderedPage(item + ":project.details");
        assertThat(image.isVisible(), is(true));
        assertThat(link.isVisible(), is(true));
        getTester().clickLink(item + ":project.details");
        String expectedPage = ProjectDetails.class.getName();
        assertThat(getTester().getLastRenderedPage().getClass().getName(), is(expectedPage));
    }

    @Test
    public void testDeleteProject_shouldDeleteProject() throws NoSuchProjectException {
        when(projectManager.getAllProjects()).thenReturn(Arrays.asList(new Project[]{ new Project("test") }));
        when(projectManager.getProject("test")).thenReturn(new Project("test"));
        Page indexPage = getTester().startPage(new Index());
        when(projectManager.getAllProjects()).thenReturn(Arrays.asList(new Project[]{}));
        getTester().clickLink("projectlistPanel:projectlist:0:deleteProject", true);
        Mockito.verify(projectManager).deleteProject("test");
        getTester().assertContains(indexPage.getString("noProjectsAvailable"));
    }

    @Test
    public void testCreateProjectLink_shouldBeCreateNewProject() {
        Page indexPage = wicketTester.startPage(new Index());
        wicketTester.assertContains(indexPage.getString("newProject.title"));
    }

    @Test
    public void testCreateProjectLink_shouldReturnFirstPageForWizzard() {
        Page indexPage = wicketTester.startPage(new Index());
        wicketTester.assertContains(indexPage.getString("newProject.title"));
        wicketTester.debugComponentTrees();
        wicketTester.clickLink("newProject", true);
        wicketTester.assertContains(new CreateProject().getString("newProject.title"));
    }
}
