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

package org.openengsb.opencit.core.config;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.util.file.File;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.Project.State;

public class OpenCitConfigurator {

    private RuleManager ruleManager;

    public void init() {
        addGlobalsAndImports();
        addWorkflow();
        addRules();
    }

    private void addGlobalsAndImports() {
        try {
            ruleManager.addImport(ProjectManager.class.getName());
            ruleManager.addImport(Project.class.getName());
            ruleManager.addImport(State.class.getName());
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Global, "projectManager");
            ruleManager.add(id, ProjectManager.class.getName());
        } catch (RuleBaseException e) {
            throw new RuntimeException(e);
        }
    }

    private void addWorkflow() {
        try {
            File workflowFile = new File(ClassLoader.getSystemResource("ci.rf").toURI());
            String citWorkflow = FileUtils.readFileToString(workflowFile);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, "ci");
            ruleManager.add(id, citWorkflow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addRules() {
        List<String> rules =
            Arrays.asList(new String[]{ "updateStateOnFlowStart", "updateStateOnFlowSuccess",
                "updateStateOnFlowFailure" });

        for (String rule : rules) {
            addRule(rule);
        }
    }

    private void addRule(String rule) {
        try {
            File ruleFile = new File(ClassLoader.getSystemResource(rule + ".rule").toURI());
            String ruleText = FileUtils.readFileToString(ruleFile);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, rule);
            ruleManager.add(id, ruleText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

}
