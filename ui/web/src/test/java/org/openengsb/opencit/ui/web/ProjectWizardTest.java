package org.openengsb.opencit.ui.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.extensions.wizard.WizardButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.LocalizableString;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.domains.report.ReportDomain;
import org.openengsb.domains.scm.ScmDomain;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class ProjectWizardTest extends AbstractCitPageTest {

    private WicketTester tester;
    private ProjectManager projectManager;
    private ContextCurrentService contextSerice;
    private DomainService domainService;

    @Before
    public void setUp() {
        tester = getTester();
    }

    @Override
    protected List<Object> getBeansForAppContext() {
        contextSerice = mock(ContextCurrentService.class);
        projectManager = mock(ProjectManager.class);
        domainService = mock(DomainService.class);
        return Arrays.asList(new Object[]{projectManager, mock(ReportDomain.class), contextSerice, domainService});
    }

    @Test
    public void testFirstStep_ShouldCreateNewProject() {
        tester.startPage(new Index());
        tester.clickLink("newProject");
        tester.assertContains("newProject.title");
        FormTester formTester = tester.newFormTester("wizard:form");
        formTester.setValue("view:project.id", "testID");

        String nextFulltBtnPath = "wizard:form:buttons:next";
        tester.assertComponent(nextFulltBtnPath, WizardButton.class);
        WizardButton finishButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        finishButton.onSubmit();
        ProjectWizard wizard = (ProjectWizard) tester.getComponentFromLastRenderedPage("wizard");
        Project project = wizard.getProject();
        assertThat(project.getId(), is("testID"));


    }

    @Test
    public void testLastStep_ShouldCreateProjectInContext() {
        tester.startPage(new Index());
        tester.clickLink("newProject");
        tester.assertContains("newProject.title");
        FormTester formTester = tester.newFormTester("wizard:form");
        formTester.setValue("view:project.id", "testID");
        // Step to SCM
        String nextFulltBtnPath = "wizard:form:buttons:next";
        tester.assertComponent(nextFulltBtnPath, WizardButton.class);
        WizardButton nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        nextButton.onSubmit();
        //Step to Final
        formTester = tester.newFormTester("wizard:form");
        nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        nextButton.onSubmit();
        // Step to SCMEditor
        formTester = tester.newFormTester("wizard:form");
        nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        nextButton.onSubmit();

        Label titel = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        assertThat(titel.getDefaultModelObject().toString(), is("Confirmation"));

        Label id = (Label) tester.getComponentFromLastRenderedPage("wizard:form:view:projectId.confirm");
        assertThat(id.getDefaultModelObject().toString(), is("testID"));

        formTester = tester.newFormTester("wizard:form");

        nextFulltBtnPath = "wizard:form:buttons:finish";
        tester.assertComponent(nextFulltBtnPath, WizardButton.class);
        WizardButton finishButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        finishButton.onSubmit();
        Mockito.verify(contextSerice, Mockito.times(1)).createContext("testID");

    }


    @Test
    public void testSCMStep_ShouldShowDropDownWithPossibleSCM() {
        mockSetupForSCMDomains();
        tester.startPage(new Index());
        tester.clickLink("newProject");
        tester.assertContains("newProject.title");
        FormTester formTester = tester.newFormTester("wizard:form");
        formTester.setValue("view:project.id", "testID");

        // Step to SCM
        String nextFulltBtnPath = "wizard:form:buttons:next";
        tester.assertComponent(nextFulltBtnPath, WizardButton.class);
        WizardButton nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        nextButton.onSubmit();

        tester.debugComponentTrees();
        Label newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        String o = newHeader.getDefaultModelObject().toString();
        assertThat(o, is("Set up SCM"));

        DropDownChoice ddc = (DropDownChoice) tester
            .getComponentFromLastRenderedPage("wizard:form:view:scmDescriptor");
        List choices = ddc.getChoices();
        assertThat(choices.size(), is(1));
    }

    @Test
    public void testSCMSetupStep_ShouldShowSomeInputFieldsForSCMSetup() {
        mockSetupForSCMDomains();
        tester.startPage(new Index());
        tester.clickLink("newProject");
        tester.assertContains("newProject.title");

        // Step to SCM
        FormTester formTester = tester.newFormTester("wizard:form");
        formTester.setValue("view:project.id", "testID");
        String nextFulltBtnPath = "wizard:form:buttons:next";
        tester.assertComponent(nextFulltBtnPath, WizardButton.class);
        WizardButton nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        nextButton.onSubmit();

        formTester = tester.newFormTester("wizard:form");
        Label newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        String o = newHeader.getDefaultModelObject().toString();
        assertThat(o, is("Set up SCM"));

        DropDownChoice ddc = (DropDownChoice) tester
            .getComponentFromLastRenderedPage("wizard:form:view:scmDescriptor");
        List choices = ddc.getChoices();
        assertThat(choices.size(), is(1));
        tester.debugComponentTrees();
        
        formTester.select("view:scmDescriptor", 0);

        // Step to SCMEditor
        nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        nextButton.onSubmit();

        newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        o = newHeader.getDefaultModelObject().toString();
        assertThat(o, is("Attributes"));
    }


    private void mockSetupForSCMDomains() {
        //mock manager
        List<ServiceManager> managers = new ArrayList<ServiceManager>();
        ServiceManager serviceManager = mock(ServiceManager.class);
        managers.add(serviceManager);

        ServiceDescriptor serviceDescriptor = mock(ServiceDescriptor.class);
        LocalizableString localizableString = mock(LocalizableString.class);
        when(localizableString.getString(Mockito.any(Locale.class))).thenReturn("SCMDomain");
        when(serviceDescriptor.getName()).thenReturn(localizableString);
        LocalizableString description = mock(LocalizableString.class);
        when(description.getString(Mockito.any(Locale.class))).thenReturn("SCM Description");
        when(serviceDescriptor.getDescription()).thenReturn(description);
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        AttributeDefinition attribute = mock(AttributeDefinition.class);
        attributes.add(attribute);
        when(serviceDescriptor.getAttributes()).thenReturn(attributes);

        when(serviceDescriptor.getImplementationType()).thenAnswer(new Answer<Class>() {
            @Override
            public Class answer(InvocationOnMock invocation) throws Throwable {
                return ScmDomain.class;
            }
        });

        when(serviceManager.getDescriptor()).thenReturn(serviceDescriptor);
        when(domainService.serviceManagersForDomain(ScmDomain.class)).thenReturn(managers);
    }



}
