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

package org.openengsb.opencit.ui.web.model;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.report.Report;

@SuppressWarnings("serial")
public class ReportModel extends LoadableDetachableModel<Report> {
    private String reportName;
    private String projectId;
    private WiringService ws;

    public ReportModel(String projectId, String reportName, WiringService ws) {
        this.projectId = projectId;
        this.reportName = reportName;
        this.ws = ws;
    }

    public ReportModel(String categoryName, Report report, WiringService ws) {
        this.projectId = categoryName;
        this.reportName = report.getName();
        this.ws = ws;
        setObject(report);
    }

    @Override
    protected Report load() {
        ReportDomain reportDomain;
        reportDomain = ws.getDomainEndpoint(ReportDomain.class, "report");

        ContextHolder.get().setCurrentContextId(projectId);
        List<Report> reports = reportDomain.getAllReports(projectId);
        for (Report report : reports) {
            if (report.getName().equals(reportName)) {
                return report;
            }
        }
        throw new RuntimeException("No report with name '" + reportName + "' in project '" + projectId + "' found.");
    }
}
