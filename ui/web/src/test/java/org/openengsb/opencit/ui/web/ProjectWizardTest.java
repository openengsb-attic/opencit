package org.openengsb.opencit.ui.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.extensions.wizard.WizardButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
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
import org.openengsb.core.common.validation.FormValidator;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;
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
        return mockedBeansMap;
    }

    @Test
    public void testFirstStep_ShouldCreateNewProject() {
        tester.startPage(new Index());
        tester.clickLink("newProject");
        tester.assertContains("newProject.title");
        FormTester formTester = tester.newFormTester("wizard:form");
        formTester.setValue("view:project.id", "testID");

        nextStep(formTester);
        ProjectWizard wizard = (ProjectWizard) tester.getComponentFromLastRenderedPage("wizard");
        Project project = wizard.getProject();
        assertThat(project.getId(), is("testID"));
    }

    @Ignore("just for testing reason, test should run")
    @Test
    public void testLastStep_ShouldCreateProjectInContext() {
        mockSetupForSCMDomains();

        tester.startPage(new Index());
        tester.clickLink("newProject");
        tester.assertContains("newProject.title");
        FormTester formTester = tester.newFormTester("wizard:form");
        formTester.setValue("view:project.id", "testID");
        // Step to setSCM
        nextStep(formTester);
        //Step to SCMEditor
        formTester = tester.newFormTester("wizard:form");
        formTester.select("view:project.scmDescriptor", 0);
        nextStep(formTester);

        // Step to Final
        formTester = tester.newFormTester("wizard:form");
        formTester.setValue("view:editor:form:validate", false);
        nextStep(formTester);

        Label titel = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        assertThat(titel.getDefaultModelObject().toString(), is("Confirmation"));

        Label id = (Label) tester.getComponentFromLastRenderedPage("wizard:form:view:projectId.confirm");
        assertThat(id.getDefaultModelObject().toString(), is("testID"));

        formTester = tester.newFormTester("wizard:form");

        String nextFulltBtnPath = "wizard:form:buttons:finish";
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
        nextStep(formTester);

        tester.debugComponentTrees();
        Label newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        String o = newHeader.getDefaultModelObject().toString();
        assertThat(o, is("Set up SCM"));

        DropDownChoice ddc = (DropDownChoice) tester
            .getComponentFromLastRenderedPage("wizard:form:view:project.scmDescriptor");
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
        nextStep(formTester);

        formTester = tester.newFormTester("wizard:form");
        Label newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        String o = newHeader.getDefaultModelObject().toString();
        assertThat(o, is("Set up SCM"));

        DropDownChoice ddc = (DropDownChoice) tester
            .getComponentFromLastRenderedPage("wizard:form:view:project.scmDescriptor");
        List choices = ddc.getChoices();
        assertThat(choices.size(), is(1));
        tester.debugComponentTrees();

        formTester = tester.newFormTester("wizard:form");
        formTester.select("view:project.scmDescriptor", 0);
        // Step to SCMEditor
        nextStep(formTester);

        formTester = tester.newFormTester("wizard:form");
        newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        o = newHeader.getDefaultModelObject().toString();
        assertThat(o, is("Attributes"));

        tester.debugComponentTrees();
        formTester.setValue("view:editor:form:fields:attributeId:row:field", "attribute1Value");


    }


    private void nextStep(FormTester formTester) {

        String nextFulltBtnPath = "wizard:form:buttons:next";
        tester.assertComponent(nextFulltBtnPath, WizardButton.class);
        WizardButton nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        nextButton.onSubmit();
    }

    private void mockSetupForSCMDomains() {
        //mock manager
        List<ServiceManager> managers = new ArrayList<ServiceManager>();
        ServiceManager serviceManager = mock(ServiceManager.class);
        managers.add(serviceManager);

        ServiceDescriptor serviceDescriptor = mock(ServiceDescriptor.class);
        LocalizableString localizableString = mock(LocalizableString.class);
        when(localizableString.getString(any(Locale.class))).thenReturn("SCMDomain");
        when(serviceDescriptor.getName()).thenReturn(localizableString);
        FormValidator formValidator = mock(FormValidator.class);
        List<String> validateFields = new ArrayList<String>();
        when(formValidator.fieldsToValidate()).thenReturn(validateFields);
        when(serviceDescriptor.getFormValidator()).thenReturn(formValidator);
        MultipleAttributeValidationResult validate = mock(MultipleAttributeValidationResult.class);
        when(validate.isValid()).thenReturn(true);

        when(formValidator.validate(Mockito.<Map<String, String>>any())).thenReturn(validate);
        LocalizableString description = mock(LocalizableString.class);
        when(description.getString(any(Locale.class))).thenReturn("SCM Description");
        when(serviceDescriptor.getDescription()).thenReturn(description);
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        AttributeDefinition attribute = mock(AttributeDefinition.class);
        LocalizableString attLocalizer = mock(LocalizableString.class);
        when(attLocalizer.getString(any(Locale.class))).thenReturn("attName");
        when(attribute.getId()).thenReturn("attributeId");
        when(attribute.getName()).thenReturn(attLocalizer);

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
