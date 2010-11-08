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
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.LocalizableString;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.validation.FormValidator;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.domain.notification.NotificationDomain;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class ProjectWizardTest extends AbstractCitPageTest {

    private WicketTester tester;
    private ProjectManager projectManager;
    private ContextCurrentService contextSerice;
    private DomainService domainService;
    private ServiceManager scmServiceManager;

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

    @Test
    public void testSCMStep_ShouldShowDropDownWithPossibleSCM() {
        mockSetupForWizard();
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
            .getComponentFromLastRenderedPage("wizard:form:view:scmDescriptor");
        List choices = ddc.getChoices();
        assertThat(choices.size(), is(1));
    }

    @Test
    public void testSCMSetupStep_ShouldShowSomeInputFieldsForSCMSetup() {
        mockSetupForWizard();
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
            .getComponentFromLastRenderedPage("wizard:form:view:scmDescriptor");
        List choices = ddc.getChoices();
        assertThat(choices.size(), is(1));
        tester.debugComponentTrees();

        formTester = tester.newFormTester("wizard:form");
        formTester.select("view:scmDescriptor", 0);
        // Step to SCMEditor
        nextStep(formTester);

        formTester = tester.newFormTester("wizard:form");
        newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        o = newHeader.getDefaultModelObject().toString();
        assertThat(o, is("Attributes"));
        tester.debugComponentTrees();

        SimpleFormComponentLabel attributName = (SimpleFormComponentLabel) tester
            .getComponentFromLastRenderedPage("wizard:form:view:editor:form:fields:2:row:name");
        assertThat(attributName.getDefaultModelObjectAsString(), is("attName"));
        formTester.setValue("view:editor:form:fields:1:row:field", "ID1");
        formTester.setValue("view:editor:form:fields:2:row:field", "attribute1Value1");
        tester.submitForm("wizard:form:view:editor:form");

        //Step to notification domain
        nextStep(formTester);
        newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        tester.debugComponentTrees();
        assertThat(newHeader.getDefaultModelObjectAsString(), is("Define a notification domain"));
    }


    @Test
    public void testNotificationStep_ShouldShowADropdownchoiceForNotificationdomains() {
        mockSetupForWizard();
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
            .getComponentFromLastRenderedPage("wizard:form:view:scmDescriptor");
        List choices = ddc.getChoices();
        assertThat(choices.size(), is(1));
        tester.debugComponentTrees();

        formTester = tester.newFormTester("wizard:form");
        formTester.select("view:scmDescriptor", 0);
        // Step to SCMEditor
        nextStep(formTester);

        formTester = tester.newFormTester("wizard:form");
        newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        o = newHeader.getDefaultModelObject().toString();
        assertThat(o, is("Attributes"));

        SimpleFormComponentLabel attributName = (SimpleFormComponentLabel) tester
            .getComponentFromLastRenderedPage("wizard:form:view:editor:form:fields:2:row:name");
        assertThat(attributName.getDefaultModelObjectAsString(), is("attName"));
        formTester.setValue("view:editor:form:fields:1:row:field", "ID1");
        formTester.setValue("view:editor:form:fields:2:row:field", "attribute1Value1");
        tester.submitForm("wizard:form:view:editor:form");

        //Step to notification domain
        nextStep(formTester);
        newHeader = (Label) tester.getComponentFromLastRenderedPage("wizard:form:header:title");
        tester.debugComponentTrees();
        assertThat(newHeader.getDefaultModelObjectAsString(), is("Define a notification domain"));

        DropDownChoice notificationDDc = (DropDownChoice) tester.getComponentFromLastRenderedPage("wizard:form:view:notificationDescriptor");
        choices = notificationDDc.getChoices();
        assertThat(choices.size(), is(1));

    }

    private void nextStep(FormTester formTester) {

        String nextFulltBtnPath = "wizard:form:buttons:next";
        tester.assertComponent(nextFulltBtnPath, WizardButton.class);
        WizardButton nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        nextButton.onSubmit();
    }

    private void mockSetupForWizard() {
        ServiceManager scmServiceManager = mock(ServiceManager.class);
        ServiceManager notificationServiceManager = mock(ServiceManager.class);

        List<ServiceManager> scmManagers = new ArrayList<ServiceManager>();
        scmManagers.add(scmServiceManager);

        List<ServiceManager> notificationManagers = new ArrayList<ServiceManager>();
        notificationManagers.add(scmServiceManager);

        ServiceDescriptor scmDescriptor = mockingSetupForConnector("SCM", ScmDomain.class);
        ServiceDescriptor notificationDescriptor = mockingSetupForConnector("Notification", NotificationDomain.class);

        when(scmServiceManager.getDescriptor()).thenReturn(scmDescriptor);
        when(notificationServiceManager.getDescriptor()).thenReturn(notificationDescriptor);
        when(domainService.serviceManagersForDomain(ScmDomain.class)).thenReturn(scmManagers);
        when(domainService.serviceManagersForDomain(NotificationDomain.class)).thenReturn(notificationManagers);
    }

    private ServiceDescriptor mockingSetupForConnector(String type, final Class<? extends Domain> scmDomainClass) {

        ServiceDescriptor serviceDescriptor = mock(ServiceDescriptor.class);
        LocalizableString localizableString = mock(LocalizableString.class);
        when(localizableString.getString(any(Locale.class))).thenReturn(type + "MDomain");
        when(serviceDescriptor.getName()).thenReturn(localizableString);
        FormValidator formValidator = mock(FormValidator.class);
        List<String> validateFields = new ArrayList<String>();
        when(formValidator.fieldsToValidate()).thenReturn(validateFields);
        when(serviceDescriptor.getFormValidator()).thenReturn(formValidator);
        MultipleAttributeValidationResult validate = mock(MultipleAttributeValidationResult.class);
        when(validate.isValid()).thenReturn(true);

        when(formValidator.validate(Mockito.<Map<String, String>>any())).thenReturn(validate);
        LocalizableString description = mock(LocalizableString.class);
        when(description.getString(any(Locale.class))).thenReturn(type + " Description");
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
                return scmDomainClass;
            }
        });
        return serviceDescriptor;
    }


}
