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

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.model.OpenEngSBFileModel;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.domain.build.BuildDomain;
import org.openengsb.domain.build.BuildFailEvent;
import org.openengsb.domain.build.BuildStartEvent;
import org.openengsb.domain.build.BuildSuccessEvent;
import org.openengsb.domain.deploy.DeployDomain;
import org.openengsb.domain.deploy.DeployFailEvent;
import org.openengsb.domain.deploy.DeployStartEvent;
import org.openengsb.domain.deploy.DeploySuccessEvent;
import org.openengsb.domain.notification.NotificationDomain;
import org.openengsb.domain.notification.Attachment;
import org.openengsb.domain.notification.Notification;
import org.openengsb.domain.report.NoSuchReportException;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.report.Report;
import org.openengsb.domain.report.ReportPart;
import org.openengsb.domain.report.SimpleReportPart;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.domain.test.TestDomain;
import org.openengsb.domain.test.TestFailEvent;
import org.openengsb.domain.test.TestStartEvent;
import org.openengsb.domain.test.TestSuccessEvent;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.BuildReason;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.Project.State;

public class OpenCitConfigurator {

    private RuleManager ruleManager;

    public static List<String> getRequiredServices() {
        List<String> services = new ArrayList<String>();
        services.add("scm");
        services.add("build");
        services.add("test");
        services.add("deploy");
        services.add("notification");
        services.add("report");
        services.add("dependency");
        return services;
    }

    public void init() {
        addGlobalsAndImports();
        addWorkflow("ci");
        addWorkflow("runBuild");
        addWorkflow("runTests");
        addWorkflow("runDeploy");
        addRules();
    }

    private void addGlobalsAndImports() {
        try {
            addUtilImports();
            addScmGlobalsAndImports();
            addBuildGlobalsAndImports();
            addTestGlobalsAndImports();
            addDeployGlobalsAndImports();
            addReportGlobalsAndImports();
            addNotificationGlobalsAndImports();
            addProjectManagerGlobalsAndImports();
        } catch (RuleBaseException e) {
            throw new RuntimeException(e);
        }
    }

    private void addUtilImports() throws RuleBaseException {
        ruleManager.addImport(UUID.class.getCanonicalName());
        ruleManager.addImport(SimpleDateFormat.class.getCanonicalName());
        ruleManager.addImport(Date.class.getCanonicalName());
        ruleManager.addImport(WorkflowProcessInstance.class.getCanonicalName());
        ruleManager.addImport(List.class.getCanonicalName());
        ruleManager.addImport(Collection.class.getCanonicalName());
        ruleManager.addImport(ArrayList.class.getCanonicalName());
        ruleManager.addImport(Event.class.getCanonicalName());
        ruleManager.addImport(File.class.getCanonicalName());
        ruleManager.addImport(OpenEngSBFileModel.class.getCanonicalName());
        ruleManager.addImport(FileUtils.class.getCanonicalName());
        ruleManager.addImport(Log.class.getCanonicalName());
        ruleManager.addImport(LogFactory.class.getCanonicalName());
    }

    private void addScmGlobalsAndImports() throws RuleBaseException {
        ruleManager.addImport(ScmDomain.class.getCanonicalName());
        addGlobal(ScmDomain.class.getCanonicalName(), "scm");
    }

    private void addBuildGlobalsAndImports() throws RuleBaseException {
        ruleManager.addImport(BuildStartEvent.class.getCanonicalName());
        ruleManager.addImport(BuildSuccessEvent.class.getCanonicalName());
        ruleManager.addImport(BuildFailEvent.class.getCanonicalName());
        ruleManager.addImport(BuildDomain.class.getCanonicalName());
        addGlobal(BuildDomain.class.getCanonicalName(), "build");
    }

    private void addTestGlobalsAndImports() throws RuleBaseException {
        ruleManager.addImport(TestStartEvent.class.getCanonicalName());
        ruleManager.addImport(TestSuccessEvent.class.getCanonicalName());
        ruleManager.addImport(TestFailEvent.class.getCanonicalName());
        ruleManager.addImport(TestDomain.class.getCanonicalName());
        addGlobal(TestDomain.class.getCanonicalName(), "test");
    }

    private void addDeployGlobalsAndImports() throws RuleBaseException {
        ruleManager.addImport(DeployStartEvent.class.getCanonicalName());
        ruleManager.addImport(DeploySuccessEvent.class.getCanonicalName());
        ruleManager.addImport(DeployFailEvent.class.getCanonicalName());
        ruleManager.addImport(DeployDomain.class.getCanonicalName());
        addGlobal(DeployDomain.class.getCanonicalName(), "deploy");
    }

    private void addReportGlobalsAndImports() throws RuleBaseException {
        ruleManager.addImport(ReportDomain.class.getCanonicalName());
        ruleManager.addImport(Report.class.getCanonicalName());
        ruleManager.addImport(ReportPart.class.getCanonicalName());
        ruleManager.addImport(NoSuchReportException.class.getCanonicalName());
        ruleManager.addImport(SimpleReportPart.class.getCanonicalName());
        addGlobal(ReportDomain.class.getCanonicalName(), "report");
    }

    private void addNotificationGlobalsAndImports() throws RuleBaseException {
        ruleManager.addImport(NotificationDomain.class.getCanonicalName());
        ruleManager.addImport(Notification.class.getCanonicalName());
        ruleManager.addImport(Attachment.class.getCanonicalName());
        ruleManager.addImport(StringUtils.class.getCanonicalName());
        addGlobal(NotificationDomain.class.getCanonicalName(), "notification");
    }

    private void addProjectManagerGlobalsAndImports() throws RuleBaseException {
        ruleManager.addImport(ProjectManager.class.getCanonicalName());
        ruleManager.addImport(Project.class.getCanonicalName());
        ruleManager.addImport(State.class.getCanonicalName());
        ruleManager.addImport(BuildReason.class.getCanonicalName());
        addGlobal(ProjectManager.class.getCanonicalName(), "projectManager");
    }

    private void addWorkflow(String name) {
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream(name + ".rf");
            String citWorkflow = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, name);
            if (isPresent(id)) {
                ruleManager.update(id, citWorkflow);
            } else {
                ruleManager.add(id, citWorkflow);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private boolean isPresent(RuleBaseElementId id) {
        Collection<RuleBaseElementId> elements = ruleManager.list(id.getType());
        for (RuleBaseElementId element : elements) {
            if (element.equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void addRules() {
        List<String> rules =
            Arrays.asList(new String[]{ "reportEvent", });
        for (String rule : rules) {
            addRule(rule);
        }
    }

    private void addRule(String rule) {
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream(rule + ".rule");
            String ruleText = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, rule);
            if (isPresent(id)) {
                ruleManager.update(id, ruleText);
            } else {
                ruleManager.add(id, ruleText);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private void addGlobal(String clazz, String name) throws RuleBaseException {
        if (isGlobalPresent(name)) {
            checkGlobal(clazz, name);
        } else {
            ruleManager.addGlobal(clazz, name);
        }
    }

    private void checkGlobal(String clazz, String name) throws RuleBaseException {
        Map<String, String> globals = ruleManager.listGlobals();
        String oldClazz = globals.get(name);
        if (!oldClazz.equals(clazz)) {
            throw new IllegalStateException("Uncompatible global with name '" + name + "' former global class '"
                    + oldClazz + "' new global class '" + clazz + "'.");
        }
    }

    private boolean isGlobalPresent(String global) {
        Map<String, String> globals = ruleManager.listGlobals();
        return globals.containsKey(global);
    }

    public void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

}
