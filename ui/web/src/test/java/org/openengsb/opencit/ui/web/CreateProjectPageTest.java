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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.PassThroughStringLocalizer;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.openengsb.core.services.internal.ConnectorManagerImpl;

public class CreateProjectPageTest extends AbstractCitPageTest {

    private WicketTester tester;
    private ProjectManager projectManager;
    private ContextCurrentService contextSerice;
    private ConnectorManager connectorManager;

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
        projectManager = Mockito.mock(ProjectManager.class);
        mockedBeansMap.put("contextCurrentService", contextSerice);
        mockedBeansMap.put("projectManager", projectManager);
        mockedBeansMap.put("reportDomain", mock(ReportDomain.class));
        SchedulingService scheduler = mock(SchedulingService.class);
        mockedBeansMap.put("scheduler", scheduler);
        connectorManager = new ConnectorManagerImpl();
        mockedBeansMap.put("connectorManager", connectorManager);
        return mockedBeansMap;
    }

    private void mockScmDomain(Page page) {
        createDomainProviderMock(ScmDomain.class, "scm");

        ConnectorProvider scm1 = createConnectorProviderMock("foo", "scm");
        ServiceDescriptor desc1 = scm1.getDescriptor();
        List<AttributeDefinition> attribs1 = new LinkedList<AttributeDefinition>();
        AttributeDefinition fooAttrib =
            AttributeDefinition.builder(new PassThroughStringLocalizer()).id("foo").defaultValue("foo_default")
            .name("foo_name").build();
        attribs1.add(fooAttrib);
        when(desc1.getAttributes()).thenReturn(attribs1);

        ConnectorProvider scm2 = createConnectorProviderMock("bar", "scm");
        ServiceDescriptor desc2 = scm2.getDescriptor();
        List<AttributeDefinition> attribs2 = new LinkedList<AttributeDefinition>();
        AttributeDefinition barAttrib =
            AttributeDefinition.builder(new PassThroughStringLocalizer()).id("bar").defaultValue("bar_default")
            .name("bar_name").build();
        attribs2.add(barAttrib);
        when(desc2.getAttributes()).thenReturn(attribs2);
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
        mockScmDomain(createProject);

        tester.startPage(new CreateProject());
        tester.assertContains("bar");
        
        FormTester newFormTester = tester.newFormTester("form");
        newFormTester.select("domainList:0:connector", 1);
        tester.executeAjaxEvent("form:domainList:0:connector", "onchange");

        tester.assertContains("foo");
    }
}
