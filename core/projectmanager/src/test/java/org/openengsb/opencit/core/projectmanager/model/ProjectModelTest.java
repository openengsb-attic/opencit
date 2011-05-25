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

package org.openengsb.opencit.core.projectmanager.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.domain.notification.NotificationDomain;

public class ProjectModelTest {

    private Project p;

    @Before
    public void setUp() {
        p = new Project();
    }

    @Test
    public void addService_shouldWork() {
        ConnectorId scmId = new ConnectorId();
        scmId.setInstanceId("Scm1234");
        ConnectorId reportId = new ConnectorId();
        reportId.setInstanceId("Report9876");

        p.addService("scm", scmId);
        p.addService("report", reportId);

        Map<String, ConnectorId> services = p.getServices();
        assertEquals(2, services.size());
        assertTrue(services.containsKey("scm"));
        assertEquals(services.get("scm").getInstanceId(), "Scm1234");
        assertTrue(services.containsKey("report"));
        assertEquals(services.get("report").getInstanceId(), "Report9876");
        assertFalse(services.containsKey(NotificationDomain.class));
    }

    @Test
    public void getServices_shouldWork() {
        Map<String, ConnectorId> services = p.getServices();
        assertEquals(null, services);
    }

    @Test
    public void addService_overwrite_shouldWork() {
        ConnectorId scmId1 = new ConnectorId();
        scmId1.setInstanceId("Scm1234");
        ConnectorId scmId2 = new ConnectorId();
        scmId2.setInstanceId("Scm0000");

        p.addService("scm", scmId1);
        p.addService("scm", scmId2);

        Map<String, ConnectorId> services = p.getServices();
        assertEquals(1, services.size());
        assertTrue(services.containsKey("scm"));
        assertEquals(services.get("scm").getInstanceId(), "Scm0000");
    }
}
