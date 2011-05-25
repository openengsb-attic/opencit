package org.openengsb.opencit.ui.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jnlp.ServiceManager;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.LocalizableString;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;

public class CreateProjectPageTest extends AbstractCitPageTest {

    private WicketTester tester;
    private ProjectManager projectManager;
    private ContextCurrentService contextSerice;
    private OsgiUtilsService osgiUtilsService;

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
        osgiUtilsService = mock(OsgiUtilsService.class);
        projectManager = Mockito.mock(ProjectManager.class);
        mockedBeansMap.put("contextCurrentService", contextSerice);
        mockedBeansMap.put("osgiUtilsService", osgiUtilsService);
        mockedBeansMap.put("projectManager", projectManager);
        mockedBeansMap.put("reportDomain", mock(ReportDomain.class));
        SchedulingService scheduler = mock(SchedulingService.class);
        mockedBeansMap.put("scheduler", scheduler);
        return mockedBeansMap;
    }

    private List<ServiceManager> mockScmDomain(Page page) {
        List<ServiceManager> scmManagers = new LinkedList<ServiceManager>();
        ServiceManager mockScm1 = mock(ServiceManager.class);
        scmManagers.add(mockScm1);
        ServiceDescriptor d = mock(ServiceDescriptor.class);
        when(mockScm1.getDescriptor()).thenReturn(d);
        LocalizableString l = mock(LocalizableString.class);
        when(d.getName()).thenReturn(l);
        when(l.getString(new Locale("en"))).thenReturn("Funny Mock SCM");

        AttributeDefinition.Builder builder = AttributeDefinition
                .builder(new WicketStringLocalizer(page));
        AttributeDefinition fooAttrib = builder.id("Foo").name("attribute.foo.name")
                .description("attribute.foo.description").required().build();
        List<AttributeDefinition> attribList = new LinkedList<AttributeDefinition>();
        attribList.add(0, fooAttrib);
        when(d.getAttributes()).thenReturn(attribList);

        ServiceManager mockScm2 = mock(ServiceManager.class);
        scmManagers.add(mockScm2);
        d = mock(ServiceDescriptor.class);
        when(mockScm2.getDescriptor()).thenReturn(d);
        l = mock(LocalizableString.class);
        when(d.getName()).thenReturn(l);
        when(l.getString(new Locale("en"))).thenReturn("A crappy SCM");

        builder = AttributeDefinition.builder(new WicketStringLocalizer(page));
        AttributeDefinition barAttrib = builder.id("Bar").name("attribute.bar.name")
                .description("attribute.bar.description").required().build();
        attribList = new LinkedList<AttributeDefinition>();
        attribList.add(barAttrib);
        when(d.getAttributes()).thenReturn(attribList);

        return scmManagers;
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

    @Test
    public void test_Ajax() {
        Page createProject = new CreateProject();
        List<ServiceManager> m = mockScmDomain(createProject);
        when(domainService.serviceManagersForDomain(ScmDomain.class)).thenReturn(m);

        tester.startPage(new CreateProject());
        tester.assertContains(createProject.getString("attribute.bar.name"));

        FormTester newFormTester = tester.newFormTester("form");
        newFormTester.select("domainList:0:connector", 1);
        tester.executeAjaxEvent("form:domainList:0:connector", "onchange");

        tester.assertContains(createProject.getString("attribute.foo.name"));
    }
}
