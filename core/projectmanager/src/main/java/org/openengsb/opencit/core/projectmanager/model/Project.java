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

package org.openengsb.opencit.core.projectmanager.model;

import java.util.List;

import org.openengsb.domains.report.model.Report;

public class Project {

    private String id;

    private List<Report> reports;

    public Project(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public List<Report> getReports() {
        return reports;
    }

}
