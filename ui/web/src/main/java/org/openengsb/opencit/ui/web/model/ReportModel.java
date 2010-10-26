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

package org.openengsb.opencit.ui.web.model;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.domains.report.ReportDomain;
import org.openengsb.domains.report.model.Report;

@SuppressWarnings("serial")
public class ReportModel extends LoadableDetachableModel<Report> {
    @SpringBean
    private ReportDomain reportDomain;

    private String reportName;

    private String categoryName;

    public ReportModel(String categoryName, String reportName) {
        this.categoryName = categoryName;
        this.reportName = reportName;
    }

    public ReportModel(String categoryName, Report report) {
        this.categoryName = categoryName;
        this.reportName = report.getName();
        setObject(report);
    }

    @Override
    protected Report load() {
        List<Report> reports = reportDomain.getAllReports(categoryName);
        for (Report report : reports) {
            if (report.getName().equals(reportName)) {
                return report;
            }
        }
        throw new RuntimeException("No report with name '" + reportName + "' in project '" + categoryName + "' found.");
    }
}
