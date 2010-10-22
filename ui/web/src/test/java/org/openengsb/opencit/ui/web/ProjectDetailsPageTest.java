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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.domains.report.ReportDomain;
import org.openengsb.domains.report.model.Report;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.Project.State;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class ProjectDetailsPageTest {

    private WicketTester tester;
    private ReportDomain reportDomain;
    private ApplicationContextMock appContext;
    private Project testProject;

    @Before
    public void setup() {
        reportDomain = mock(ReportDomain.class);
        appContext = new ApplicationContextMock();
        appContext.putBean(reportDomain);
        appContext.putBean(mock(ProjectManager.class));
        mockAuthentication();
        tester = new WicketTester(new WebApplication() {

            @Override
            protected void init() {
                super.init();
                addComponentInstantiationListener(new SpringComponentInjector(this, appContext, false));
            }

            @Override
            public Class<? extends Page> getHomePage() {
                return Index.class;
            }

            @Override
            public Session newSession(Request request, Response response) {
                return new WicketSession(request);
            }
        });
        testProject = new Project("test");
        testProject.setState(State.IN_PROGRESS);
    }

    private void mockAuthentication() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        when(authManager.authenticate(any(Authentication.class))).thenAnswer(new Answer<Authentication>() {
            @Override
            public Authentication answer(InvocationOnMock invocation) {
                Authentication auth = (Authentication) invocation.getArguments()[0];
                if (auth.getCredentials().equals("password")) {
                    return new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
                        authorities);
                }
                throw new BadCredentialsException("wrong password");
            }
        });
        appContext.putBean("authenticationManager", authManager);
    }

    @Test
    public void testProjectDetailsHeaderPresent_shouldWork() {
        Page detailPage = tester.startPage(new ProjectDetails(testProject));
        tester.assertContains(detailPage.getString("projectDetail.title"));
    }

    @Test
    public void testProjectIdPresent_shouldWork() {
        tester.startPage(new ProjectDetails(testProject));
        tester.assertContains(testProject.getId());
    }

    @Test
    public void testProjectStatePresent_shouldWork() {
        tester.startPage(new ProjectDetails(testProject));
        Image image = (Image) tester.getComponentFromLastRenderedPage("project.state");
        assertThat(image.isVisible(), is(true));
    }

    @Test
    public void testBackLink_shouldWork() {
        tester.startPage(new ProjectDetails(testProject));
        tester.clickLink("back");
        String expectedPage = Index.class.getName();
        assertThat(tester.getLastRenderedPage().getClass().getName(), is(expectedPage));
    }

    @Test
    public void testNoReports_shouldShowLabel() {
        Page detailPage = tester.startPage(new ProjectDetails(testProject));
        tester.assertContains(detailPage.getString("noReportsAvailable"));
    }

    @Test
    public void testReportPanel_shouldWork() {
        List<Report> reports = Arrays.asList(new Report[]{ new Report("rep1") });
        when(reportDomain.getAllReports(testProject.getId())).thenReturn(reports);
        tester.startPage(new ProjectDetails(testProject));
        tester.assertContains("rep1");
        String item = "reportsPanel:reportlist:0";
        Link<?> link = (Link<?>) tester.getComponentFromLastRenderedPage(item + ":report.link");
        assertThat(link.isVisible(), is(true));
        tester.clickLink(item + ":report.link");
        String expectedPage = ReportViewPage.class.getName();
        assertThat(tester.getLastRenderedPage().getClass().getName(), is(expectedPage));
    }

}
