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

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.ops4j.pax.wicket.api.ApplicationLifecycleListener;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class LoginPageTest {

    private WicketTester tester;
    private ApplicationContextMock contextMock;

    class DummyListener implements ApplicationLifecycleListener {
        @Override
        public void onDestroy(WebApplication wicketApplication) {
        }

        @Override
        public void onInit(WebApplication wicketApplication) {
        }
    }

    @Before
    public void setUp() {
        contextMock = new ApplicationContextMock();
        mockAuthentication();
        contextMock.putBean("projectManager", mock(ProjectManager.class));
        contextMock.putBean(mock(ContextCurrentService.class));
        contextMock.putBean("scheduler", mock(SchedulingService.class));
        ApplicationLifecycleListener listener = new DummyListener();

        WebApplication app = new WicketApplication(listener) {
            @Override
            protected void addInjector() {
                addComponentInstantiationListener(new PaxWicketSpringBeanComponentInjector(this, contextMock, true));
            }
        };
        tester = new WicketTester(app);

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
        contextMock.putBean("authenticationManager", authManager);
    }

    @Test
    public void testLoginPageIsDisplayed() throws Exception {
        tester.startPage(LoginPage.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testRedirectToLogin() throws Exception {
        tester.startPage(Index.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testEnterLogin() throws Exception {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("username", "test");
        formTester.setValue("password", "password");
        formTester.submit();
        tester.assertNoErrorMessage();
        tester.assertRenderedPage(Index.class);
    }

    @Test
    public void testLogout() throws Exception {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("username", "test");
        formTester.setValue("password", "password");
        formTester.submit();
        tester.clickLink("logout");
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testInvalidLogin() throws Exception {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("username", "test");
        formTester.setValue("password", "wrongpassword");
        formTester.submit();
        tester.assertRenderedPage(LoginPage.class);
        List<Serializable> messages = tester.getMessages(FeedbackMessage.ERROR);
        assertFalse(messages.isEmpty());
    }
}
