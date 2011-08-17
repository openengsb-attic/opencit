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

package org.openengsb.opencit.core.projectmanager.internal;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openengsb.core.api.context.Context;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.Project.State;
import org.osgi.framework.BundleContext;

public class ProjectManagerImpl implements ProjectManager {

    private PersistenceManager persistenceManager;

    private PersistenceService persistence;

    private ContextCurrentService contextService;

    private SchedulingService scheduler;

    private BundleContext bundleContext;

    public void init() {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
        List<Project> projects = getAllProjects();
        for (Project project : projects) {
            scheduler.setupAndStartScmPoller(project);
        }
    }

    @Override
    public void createProject(Project project) throws ProjectAlreadyExistsException {
        checkId(project.getId());
        try {
            persistence.create(project);
            setupProject(project);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupProject(Project project) {
        createAndSetContext(project);
        setDefaultConnectors(project);
        scheduler.setupAndStartScmPoller(project);
    }

    private void createAndSetContext(Project project) {
        try {
            contextService.createContext(project.getId());
        } catch (IllegalArgumentException iae) {
            // ignore - means that context already exists
        }
        ContextHolder.get().setCurrentContextId(project.getId());
    }

    private void setDefaultConnectors(Project project) {
        Map<String, ConnectorId> services = project.getServices();
        if (services == null) {
            return;
        }
        Context context = contextService.getContext(project.getId());
        for (Entry<String, ConnectorId> entry : services.entrySet()) {
            String domain = entry.getKey();
            String id = entry.getValue().getInstanceId();
            context.put(domain, id);
        }
        context.put("AuditingDomain", "auditing");
    }

    private void checkId(String id) throws ProjectAlreadyExistsException {
        List<Project> projects = persistence.query(new Project(id));
        if (!projects.isEmpty()) {
            throw new ProjectAlreadyExistsException("Project with id '" + id + "' already exists.");
        }
    }

    @Override
    public List<Project> getAllProjects() {
        return persistence.query(new Project(null));
    }

    @Override
    public Project getProject(String projectId) throws NoSuchProjectException {
        List<Project> projects = persistence.query(new Project(projectId));
        if (projects.isEmpty()) {
            throw new NoSuchProjectException("No project with id '" + projectId + "' found.");
        }
        return projects.get(0);
    }

    @Override
    public void updateProject(Project project) throws NoSuchProjectException {
        Project old = getProject(project.getId());
        try {
            persistence.update(old, project);
            setDefaultConnectors(project);
        } catch (PersistenceException e) {
            throw new RuntimeException("Could not update project", e);
        }
    }

    @Override
    public void updateCurrentContextProjectState(State state) throws NoSuchProjectException {
        String projectId = contextService.getContext().getId();
        Project project = getProject(projectId);
        project.setState(state);
        updateProject(project);
    }

    @Override
    public Project getCurrentContextProject() throws NoSuchProjectException {
        String projectId = ContextHolder.get().getCurrentContextId();
        return getProject(projectId);
    }

    @Override
    public void deleteProject(String projectId) throws NoSuchProjectException {
        Project project = getProject(projectId);
        ReportDomain reportDomain;

        reportDomain = OpenEngSBCoreServices.getWiringService().getDomainEndpoint(ReportDomain.class, "report");
        scheduler.suspendScmPoller(projectId);
        reportDomain.removeCategory(projectId);
        try {
            persistence.delete(project);
        } catch (PersistenceException e) {
            throw new RuntimeException("Could not delete project " + projectId, e);
        }
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setContextService(ContextCurrentService contextService) {
        this.contextService = contextService;
    }

    public void setScheduler(SchedulingService scheduler) {
        this.scheduler = scheduler;
    }

}
