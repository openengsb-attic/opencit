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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.report.model.Report;
import org.openengsb.domain.report.model.SimpleReportPart;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.ProjectModel;

public class ReportViewPageTest extends AbstractCitPageTest {

    private IModel<Report> testReportModel;

    private ProjectModel testProjectModel;

    private ProjectManager projectManager;

    private ContextCurrentService contextService;

    private Project testProject;
    
    private ReportDomain reportMock = mock(ReportDomain.class);

    @Override
    protected Map<String, Object> getBeansForAppContextAsMap() {
        Map<String, Object> mockedBeansMap = new HashMap<String, Object>();
        contextService = mock(ContextCurrentService.class);
        mockedBeansMap.put("contextCurrentService", contextService);
        mockedBeansMap.put("workflowService", mock(WorkflowService.class));
        projectManager = mock(ProjectManager.class);
        mockedBeansMap.put("projectManager", projectManager);
        mockedBeansMap.put("reportDomain", reportMock);
        SchedulingService scheduler = mock(SchedulingService.class);
        mockedBeansMap.put("scheduler", scheduler);
        return mockedBeansMap;
    }

    @Before
    @SuppressWarnings("serial")
    public void setUp() throws NoSuchProjectException {
        testProject = new Project("bar");
        testProjectModel = new ProjectModel("bar") {
            @Override
            public Project getObject() {
                return testProject;
            }
        };
        testReportModel = new LoadableDetachableModel<Report>() {
            @Override
            protected Report load() {
                return new Report("foo");
            }
        };
        when(projectManager.getProject("bar")).thenReturn(testProject);
        ContextHolder.get().setCurrentContextId("bar");
        when(projectManager.getCurrentContextProject()).thenReturn(testProject);

        WiringService wiringService = Mockito.mock(WiringService.class);
        when(wiringService.getDomainEndpoint(ReportDomain.class, "report")).thenReturn(reportMock);
        registerServiceViaId(wiringService, "wiring", WiringService.class);
    }

    @Test
    public void testReportViewHeaderPresent_shouldWork() {
        Page reportView = getTester().startPage(new ReportViewPage(testReportModel));
        getTester().assertContains(reportView.getString("reportView.title"));
    }

    @Test
    public void testProjectIdPresent_shouldWork() {
        getTester().startPage(new ReportViewPage(testReportModel));
        getTester().assertContains(testProjectModel.getObject().getId());
    }

    @Test
    public void testReportNamePresent_shouldWork() {
        getTester().startPage(new ReportViewPage(testReportModel));
        getTester().assertContains(testReportModel.getObject().getName());
    }

    @Test
    public void testBackLink_shouldWork() {
        getTester().startPage(new ReportViewPage(testReportModel));
        getTester().clickLink("back");
        String expectedPage = ProjectDetails.class.getName();
        assertThat(getTester().getLastRenderedPage().getClass().getName(), is(expectedPage));
    }

    @Test
    public void testPartsPanel_shouldWork() {
        SimpleReportPart reportPart = new SimpleReportPart("part1", "text/plain", "content1".getBytes());
        testReportModel.getObject().addPart(reportPart);
        getTester().startPage(new ReportViewPage(testReportModel));
        getTester().assertContains("part1");
        getTester().assertContains("content1");
    }

}
