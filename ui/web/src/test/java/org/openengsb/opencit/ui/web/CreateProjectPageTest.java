package org.openengsb.opencit.ui.web;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;

public class CreateProjectPageTest extends AbstractCitPageTest {

    private WicketTester tester;
    private ProjectManager projectManager;
    private ContextCurrentService contextSerice;
    private DomainService domainService;

    @Before
    public void setUp() {
        Locale.setDefault(new Locale("en"));
        tester = getTester();
    }

    @Override
    protected Map<String, Object> getBeansForAppContextAsMap() {
        Map<String, Object> mockedBeansMap = new HashMap<String, Object>();
        contextSerice = mock(ContextCurrentService.class);
        projectManager = mock(ProjectManager.class);
        domainService = mock(DomainService.class);
        projectManager = Mockito.mock(ProjectManager.class);
        mockedBeansMap.put("contextCurrentService", contextSerice);
        mockedBeansMap.put("domainService", domainService);
        mockedBeansMap.put("projectManager", projectManager);
        mockedBeansMap.put("reportDomain", mock(ReportDomain.class));
        SchedulingService scheduler = mock(SchedulingService.class);
        mockedBeansMap.put("scheduler", scheduler);
        return mockedBeansMap;
    }

    @Test
    public void test_BasicRendering() {
        tester.startPage(new CreateProject());
        tester.assertContains("newProject.title");

        tester.assertContains("SCM Domain");
        tester.assertContains("Notification Domain");
        tester.assertContains("Build Domain");
        tester.assertContains("Test Domain");
        tester.assertContains("Deploy Domain");
        tester.assertContains("Report Domain");
    }
}
