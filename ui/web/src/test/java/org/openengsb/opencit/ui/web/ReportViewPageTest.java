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

import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.openengsb.domains.report.ReportDomain;
import org.openengsb.opencit.core.projectmanager.ProjectManager;

public class ReportViewPageTest extends AbstractCitPageTest {

    @Override
    protected List<Object> getBeansForAppContext() {
        return Arrays.asList(new Object[]{ Mockito.mock(ReportDomain.class), Mockito.mock(ProjectManager.class) });
    }

}
