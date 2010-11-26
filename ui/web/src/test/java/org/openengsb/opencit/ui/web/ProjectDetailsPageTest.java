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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.report.model.Report;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.Project.State;

public class ProjectDetailsPageTest extends AbstractCitPageTest {

    private ReportDomain reportDomain;
    private IModel<Project> testProjectModel;
    private WorkflowService workflowService;

    @Override
    protected Map<String, Object> getBeansForAppContextAsMap() {
        Map<String, Object> mockedBeansMap = new HashMap<String, Object>();
        reportDomain = mock(ReportDomain.class);
        mockedBeansMap.put("contextCurrentService", mock(ContextCurrentService.class));
        mockedBeansMap.put("projectManager", mock(ProjectManager.class));
        mockedBeansMap.put("reportDomain", reportDomain);
        workflowService = mock(WorkflowService.class);
        mockedBeansMap.put("workflowService", workflowService);

        return mockedBeansMap;
    }

    @Before
    @SuppressWarnings("serial")
    public void setUp() {
        testProjectModel = new LoadableDetachableModel<Project>() {
            @Override
            protected Project load() {
                Project testProject = new Project("test");
                testProject.setState(State.OK);
                return testProject;
            }
        };
    }

    @Test
    public void testProjectDetailsHeaderPresent_shouldWork() {
        Page detailPage = getTester().startPage(new ProjectDetails(testProjectModel));
        getTester().assertContains(detailPage.getString("projectDetail.title"));
    }

    @Test
    public void testProjectIdLabelPresent_shouldWork() {
        Page detailPage = getTester().startPage(new ProjectDetails(testProjectModel));
        getTester().assertContains(detailPage.getString("projectId.label"));
    }

    @Test
    public void testProjectIdPresent_shouldWork() {
        getTester().startPage(new ProjectDetails(testProjectModel));
        getTester().assertContains(testProjectModel.getObject().getId());
    }

    @Test
    public void testProjectStatePresent_shouldWork() {
        getTester().startPage(new ProjectDetails(testProjectModel));
        Image image = (Image) getTester().getComponentFromLastRenderedPage("project.state");
        assertThat(image.isVisible(), is(true));
    }

    @Test
    public void testBackLink_shouldWork() {
        getTester().startPage(new ProjectDetails(testProjectModel));
        getTester().clickLink("back");
        String expectedPage = Index.class.getName();
        assertThat(getTester().getLastRenderedPage().getClass().getName(), is(expectedPage));
    }

    @Test
    public void testRunFlow_shouldWork() throws WorkflowException {
        WicketTester tester = getTester();
        tester.startPage(new ProjectDetails(testProjectModel));
        tester.executeAjaxEvent("workflowForm:flowButton", "onclick");
        Mockito.verify(workflowService).startFlow("ci");
    }

    @Test
    public void testNoReports_shouldShowLabel() {
        Page detailPage = getTester().startPage(new ProjectDetails(testProjectModel));
        getTester().assertContains(detailPage.getString("noReportsAvailable"));
    }

    @Test
    public void testReportPanel_shouldWork() {
        List<Report> reports = Arrays.asList(new Report[]{ new Report("rep1") });
        when(reportDomain.getAllReports(testProjectModel.getObject().getId())).thenReturn(reports);
        getTester().startPage(new ProjectDetails(testProjectModel));
        getTester().assertContains("rep1");
        String item = "reportsPanel:reportlist:0";
        Link<?> link = (Link<?>) getTester().getComponentFromLastRenderedPage(item + ":report.link");
        assertThat(link.isVisible(), is(true));
        getTester().clickLink(item + ":report.link");
        String expectedPage = ReportViewPage.class.getName();
        assertThat(getTester().getLastRenderedPage().getClass().getName(), is(expectedPage));
    }

}
