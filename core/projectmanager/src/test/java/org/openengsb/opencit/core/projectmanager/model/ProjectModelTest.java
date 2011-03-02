/**
 * Copyright 2011 OpenEngSB Division, Vienna University of Technology
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

package org.openengsb.opencit.core.projectmanager.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.Domain;
import org.openengsb.domain.notification.NotificationDomain;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;

public class ProjectModelTest {

    private Project p;

    @Before
    public void setUp() {
        p = new Project();
    }

    @Test
    public void addService_shouldWork() {
        p.addService(ScmDomain.class, "Scm1234");
        p.addService(ReportDomain.class, "Report9876");

        Map<Class<? extends Domain>, String> services = p.getServices();
        assertEquals(2, services.size());
        assertTrue(services.containsKey(ScmDomain.class));
        assertEquals(services.get(ScmDomain.class), "Scm1234");
        assertTrue(services.containsKey(ReportDomain.class));
        assertEquals(services.get(ReportDomain.class), "Report9876");
        assertFalse(services.containsKey(NotificationDomain.class));
    }

    @Test
    public void getServices_shouldWork() {
        Map<Class<? extends Domain>, String> services = p.getServices();
        assertEquals(null, services);
    }

    @Test
    public void addService_overwrite_shouldWork() {
        p.addService(ScmDomain.class, "Scm1234");
        p.addService(ScmDomain.class, "Scm0000");
        Map<Class<? extends Domain>, String> services = p.getServices();
        assertEquals(1, services.size());
        assertTrue(services.containsKey(ScmDomain.class));
        assertEquals(services.get(ScmDomain.class), "Scm0000");
    }
}
