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

package org.openengsb.opencit.core.config;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyPersistence;
import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;
import org.openengsb.domain.build.BuildDomain;
import org.openengsb.domain.build.BuildStartEvent;
import org.openengsb.domain.build.BuildSuccessEvent;
import org.openengsb.domain.deploy.DeployDomain;
import org.openengsb.domain.deploy.DeployFailEvent;
import org.openengsb.domain.notification.NotificationDomain;
import org.openengsb.domain.notification.model.Notification;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.domain.test.TestDomain;
import org.openengsb.domain.test.TestSuccessEvent;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.osgi.framework.BundleContext;

import com.google.common.collect.ImmutableMap;

public class FlowExecutionTest extends AbstractOsgiMockServiceTest {

    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File("data"));
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("data"));
    }

    private BundleContext bundleContext;
    private ScmDomain scmMock = mock(ScmDomain.class);
    private BuildDomain buildMock = mock(BuildDomain.class);
    private TestDomain testMock = mock(TestDomain.class);
    private DeployDomain deployMock = mock(DeployDomain.class);
    private NotificationDomain notificationMock = mock(NotificationDomain.class);
    private ReportDomain reportMock = mock(ReportDomain.class);
    private ProjectManager projectManagerMock = mock(ProjectManager.class);

    @Test
    public void testExecuteWorkflow() throws Exception {
        PersistenceRuleManager directoryRuleSource = new PersistenceRuleManager();
        directoryRuleSource.setPersistence(new DummyPersistence());

        WorkflowServiceImpl service = new WorkflowServiceImpl();
        service.setRulemanager(directoryRuleSource);

        ContextHolder.get().setCurrentContextId("foo");

        service.setBundleContext(bundleContext);

        
        Dictionary<String, Object> scmProps = new Hashtable<String, Object>(ImmutableMap.of("location.foo", "scm"));
        registerService(scmMock, scmProps, ScmDomain.class);
        Dictionary<String, Object> buildProps = new Hashtable<String, Object>(ImmutableMap.of("location.foo", "build"));
        registerService(buildMock, buildProps, BuildDomain.class);
        Dictionary<String, Object> testProps = new Hashtable<String, Object>(ImmutableMap.of("location.foo", "test"));
        registerService(testMock, testProps, TestDomain.class);
        Dictionary<String, Object> deployProps = new Hashtable<String, Object>(ImmutableMap.of("location.foo", "deploy"));
        registerService(deployMock, deployProps, DeployDomain.class);
        Dictionary<String, Object> notificationProps = new Hashtable<String, Object>(ImmutableMap.of("location.foo", "notification"));
        registerService(notificationMock, notificationProps, NotificationDomain.class);

        Dictionary<String, Object> reportProps = new Hashtable<String, Object>(ImmutableMap.of("location.foo", "report"));
        registerService(reportMock, reportProps, ReportDomain.class);
        when(reportMock.generateReport(anyString(), anyString(), anyString())).thenReturn(new TestReport("testreport"));

        /*mockDomain(TestDomain.class, "test");
        reportDomain = mockDomain(ReportDomain.class, "report");
        notificationDomain = mockDomain(NotificationDomain.class, "notification");
        mockDomain(DeployDomain.class, "deploy");
        mockDomain(BuildDomain.class, "build");
        mockDomain(ScmDomain.class, "scm");*/
        
        Dictionary<String, Object> managerProps = new Hashtable<String, Object>(ImmutableMap.of("location.foo", "projectManager"));
        registerService(projectManagerMock, managerProps, ProjectManager.class);
        Project projectMock = mock(Project.class);
        when(projectManagerMock.getCurrentContextProject()).thenReturn(projectMock);

        OpenCitConfigurator configurator = new OpenCitConfigurator();
        configurator.setRuleManager(directoryRuleSource);
        configurator.init();

        long pid = service.startFlow("ci");

        service.processEvent(new BuildStartEvent(pid));
        service.processEvent(new BuildSuccessEvent(pid, "output"));
        service.processEvent(new TestSuccessEvent(pid, "testoutput"));
        service.processEvent(new DeployFailEvent(pid, "deployoutput"));

        service.waitForFlowToFinish(pid);
        verify(notificationMock).notify(any(Notification.class));
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        this.bundleContext = bundleContext;
    }
}
