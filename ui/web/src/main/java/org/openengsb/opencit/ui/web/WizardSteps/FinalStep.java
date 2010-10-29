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

package org.openengsb.opencit.ui.web.WizardSteps;

import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardStep;
import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class FinalStep extends DynamicWizardStep {
    public FinalStep(DynamicWizardStep prev, Project project) {
        super(prev, new ResourceModel("final.title"), new ResourceModel("final.summary"), new Model<Project>(project));
        add(new Label("projectId.confirm", new PropertyModel(project, "id")));
    }

    @Override
    public boolean isLastStep() {
        return true;
    }

    @Override
    public IDynamicWizardStep next() {
        return null;
    }
}

