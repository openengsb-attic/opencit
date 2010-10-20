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

package org.openengsb.opencit.core.projectmanager.internal;

import java.util.List;

import org.openengsb.core.persistence.PersistenceException;
import org.openengsb.core.persistence.PersistenceManager;
import org.openengsb.core.persistence.PersistenceService;
import org.openengsb.domains.report.ReportDomain;
import org.openengsb.domains.report.model.Report;
import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class ProjectManagerImpl implements ProjectManager, BundleContextAware {

    private PersistenceManager persistenceManager;

    private PersistenceService persistence;

    private BundleContext bundleContext;

    private ReportDomain reportService;

    public void init() {
        this.persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    @Override
    public void createProject(Project project) throws ProjectAlreadyExistsException {
        checkId(project.getId());
        try {
            reportService.createCategory(project.getId());
            persistence.create(project);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkId(String id) throws ProjectAlreadyExistsException {
        List<Project> projects = persistence.query(new Project(null));
        for (Project project : projects) {
            if (project.getId().equals(id)) {
                throw new ProjectAlreadyExistsException("Project with id '" + id + "' already exists.");
            }
        }
    }

    @Override
    public List<Project> getAllProjects() {
        List<Project> projects = persistence.query(new Project(null));
        for (Project project : projects) {
            addReports(project);
        }
        return projects;
    }

    private void addReports(Project project) {
        List<Report> reports = reportService.getAllReports(project.getId());
        project.setReports(reports);
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setReportService(ReportDomain reportService) {
        this.reportService = reportService;
    }

}
