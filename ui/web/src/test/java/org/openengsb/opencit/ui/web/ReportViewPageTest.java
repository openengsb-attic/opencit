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

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Page;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.domains.report.ReportDomain;
import org.openengsb.domains.report.model.Report;
import org.openengsb.domains.report.model.SimpleReportPart;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class ReportViewPageTest extends AbstractCitPageTest {

    private Report testReport;

    private Project testProject;

    @Override
    protected List<Object> getBeansForAppContext() {
        return Arrays.asList(new Object[]{ Mockito.mock(ReportDomain.class), Mockito.mock(ProjectManager.class) });
    }

    @Before
    public void setUp() {
        testReport = new Report("foo");
        testProject = new Project("bar");
    }

    @Test
    public void testReportViewHeaderPresent_shouldWork() {
        Page reportView = getTester().startPage(new ReportViewPage(testProject, testReport));
        getTester().assertContains(reportView.getString("reportView.title"));
    }

    @Test
    public void testProjectIdPresent_shouldWork() {
        getTester().startPage(new ReportViewPage(testProject, testReport));
        getTester().assertContains(testProject.getId());
    }

    @Test
    public void testReportNamePresent_shouldWork() {
        getTester().startPage(new ReportViewPage(testProject, testReport));
        getTester().assertContains(testReport.getName());
    }

    @Test
    public void testBackLink_shouldWork() {
        getTester().startPage(new ReportViewPage(testProject, testReport));
        getTester().clickLink("back");
        String expectedPage = ProjectDetails.class.getName();
        assertThat(getTester().getLastRenderedPage().getClass().getName(), is(expectedPage));
    }

    @Test
    public void testPartsPanel_shouldWork() {
        SimpleReportPart reportPart = new SimpleReportPart("part1", "text/plain", "content1".getBytes());
        testReport.addPart(reportPart);
        getTester().startPage(new ReportViewPage(testProject, testReport));
        getTester().assertContains("part1");
        getTester().assertContains("content1");
    }

}
