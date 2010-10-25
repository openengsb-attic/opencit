package org.openengsb.opencit.ui.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.extensions.wizard.WizardButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.domains.report.ReportDomain;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class ProjectWizardTest extends AbstractCitPageTest {

    private WicketTester tester;
    private ProjectManager projectManager;
    private ContextCurrentService contextSerice;

    @Before
    public void setUp() {
        tester = getTester();
    }

    @Override
    protected List<Object> getBeansForAppContext() {
        contextSerice = mock(ContextCurrentService.class);
        projectManager = mock(ProjectManager.class);
        return Arrays.asList(new Object[]{projectManager, mock(ReportDomain.class), contextSerice});
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

        String nextFulltBtnPath = "wizard:form:buttons:next";
        tester.assertComponent(nextFulltBtnPath, WizardButton.class);
        WizardButton nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        formTester.submit();
        nextButton.onSubmit();
        tester.debugComponentTrees();
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


    @Ignore
    @Test
    public void netStep() {
        String nextFulltBtnPath = "panel:form:buttons:next";
        tester.assertComponent(nextFulltBtnPath, WizardButton.class);
        WizardButton nextButton = (WizardButton) tester.getComponentFromLastRenderedPage(nextFulltBtnPath);
        nextButton.onSubmit();

        tester.debugComponentTrees();
        Label newHeader = (Label) tester.getComponentFromLastRenderedPage("panel:form:header:title");
        String o = newHeader.getDefaultModelObject().toString();
        assertThat(o, is("Set up SCM"));
    }
}
