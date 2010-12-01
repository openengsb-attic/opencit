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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.report.model.Report;
import org.openengsb.domain.report.model.SimpleReportPart;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.ProjectModel;

public class ReportViewPageTest extends AbstractCitPageTest {

    private IModel<Report> testReportModel;

    private ProjectModel testProjectModel;

    private ProjectManager projectManager;

    @Override
    protected Map<String, Object> getBeansForAppContextAsMap() {
        Map<String, Object> mockedBeansMap = new HashMap<String, Object>();
        mockedBeansMap.put("contextCurrentService", mock(ContextCurrentService.class));
        mockedBeansMap.put("workflowService", mock(WorkflowService.class));
        projectManager = mock(ProjectManager.class);
        mockedBeansMap.put("projectManager", projectManager);
        mockedBeansMap.put("reportDomain", mock(ReportDomain.class));
        return mockedBeansMap;
    }

    @Before
    @SuppressWarnings("serial")
    public void setUp() throws NoSuchProjectException {
        testProjectModel = new ProjectModel("bar") {
            @Override
            public Project getObject() {
                Project testProject = new Project("bar");
                return testProject;
            }
        };
        testReportModel = new LoadableDetachableModel<Report>() {
            @Override
            protected Report load() {
                return new Report("foo");
            }
        };
        when(projectManager.getProject("bar")).thenReturn(new Project("bar"));
    }

    @Test
    public void testReportViewHeaderPresent_shouldWork() {
        Page reportView = getTester().startPage(new ReportViewPage(testProjectModel, testReportModel));
        getTester().assertContains(reportView.getString("reportView.title"));
    }

    @Test
    public void testProjectIdPresent_shouldWork() {
        getTester().startPage(new ReportViewPage(testProjectModel, testReportModel));
        getTester().assertContains(testProjectModel.getObject().getId());
    }

    @Test
    public void testReportNamePresent_shouldWork() {
        getTester().startPage(new ReportViewPage(testProjectModel, testReportModel));
        getTester().assertContains(testReportModel.getObject().getName());
    }

    @Test
    public void testBackLink_shouldWork() {
        getTester().startPage(new ReportViewPage(testProjectModel, testReportModel));
        getTester().clickLink("back");
        String expectedPage = ProjectDetails.class.getName();
        assertThat(getTester().getLastRenderedPage().getClass().getName(), is(expectedPage));
    }

    @Test
    public void testPartsPanel_shouldWork() {
        SimpleReportPart reportPart = new SimpleReportPart("part1", "text/plain", "content1".getBytes());
        testReportModel.getObject().addPart(reportPart);
        getTester().startPage(new ReportViewPage(testProjectModel, testReportModel));
        getTester().assertContains("part1");
        getTester().assertContains("content1");
    }

}
