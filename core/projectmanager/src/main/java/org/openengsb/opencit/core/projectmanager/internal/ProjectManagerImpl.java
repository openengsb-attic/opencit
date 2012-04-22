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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.Context;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.notification.Notification;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectAlreadyExistsException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.openengsb.opencit.core.projectmanager.model.Build;
import org.openengsb.opencit.core.projectmanager.model.BuildReason;
import org.openengsb.opencit.core.projectmanager.model.ConnectorConfig;
import org.openengsb.opencit.core.projectmanager.model.DependencyProperties;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.ProjectPersist;
import org.openengsb.opencit.core.projectmanager.model.Project.State;
import org.openengsb.opencit.core.projectmanager.util.ConnectorUtil;
import org.osgi.framework.BundleContext;

public class ProjectManagerImpl implements ProjectManager {

    private PersistenceManager persistenceManager;
    private PersistenceService persistence;
    private ContextCurrentService contextService;
    private SchedulingService scheduler;
    private BundleContext bundleContext;
    private ConnectorUtil connectorUtil;
    private OsgiUtilsService osgiUtilsService;

    private Connection connection;
    private Session session = null;
    private static final String URL = "tcp://127.0.0.1:6549";

    private static Log log = LogFactory.getLog(ProjectManagerImpl.class);

    private List<Project> projects = new ArrayList<Project>();

    private void initJms() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private void startProject(Project project) {
        scheduler.setupAndStartScmPoller(project);

        if (session == null) return;

        try {
            Destination topic;
            topic = session.createQueue(project.getId());
            MessageProducer producer = session.createProducer(topic);
            project.setTopic(topic);
            project.setProducer(producer);
        } catch (JMSException e) {
            log.error("Failed to create JMS topic for project " + project.getId(), e);
        }
    }

    private void stopProject(Project project) {
        scheduler.suspendScmPoller(project.getId());

        MessageProducer producer = project.getProducer();
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException e) {
                log.error("Failed to close JMS topic for project " + project.getId(), e);
            }
        }
        project.setProducer(null);
        project.setTopic(null);
    }

    public void init() {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
        List<ProjectPersist> dbRead = persistence.query(new ProjectPersist(null));
        for (ProjectPersist dbProject : dbRead) {
            Project project = new Project(dbProject);
            projects.add(project);
            startProject(project);
        }

        try {
            initJms();
        } catch(JMSException e) {
            /* Not a critical issue. Cascading CI&T will not work, but local operation will
             * be fine.
             */
            log.info("Failed to init JMS", e);
        }
    }

    @Override
    public void createProject(Project project) 
        throws ProjectAlreadyExistsException, ConnectorValidationFailedException {
        checkId(project.getId());
        try {
            projects.add(project);
            persistence.create(project.getPersitentPart());
            setupProject(project);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    private void createConnectors(Project project) throws ConnectorValidationFailedException {
        Map<String, ConnectorConfig> connectorConfigs = project.getConnectorConfigs();

        /* Mainly for the tests */
        if (connectorConfigs == null) {
            return;
        }

        for (Entry<String, ConnectorConfig> e : connectorConfigs.entrySet()) {
            String domain = e.getKey();
            ConnectorConfig cfg = e.getValue();
            ConnectorId id = getConnectorUtil().createConnector(project, domain, cfg.getConnector(),
                cfg.getAttributeValues());
            project.addService(domain, id);
        }
    }

    private void setupProject(Project project) throws ConnectorValidationFailedException {
        createAndSetContext(project);
        createConnectors(project);
        setDefaultConnectors(project);
        startProject(project);
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
        String oldCtx = ContextHolder.get().getCurrentContextId();
        ContextHolder.get().setCurrentContextId(project.getId());
        Context context = contextService.getContext();

        for (Entry<String, ConnectorId> entry : services.entrySet()) {
            String domain = entry.getKey();
            String id = entry.getValue().getInstanceId();
            context.put(domain, id);
        }
        context.put("AuditingDomain", "auditing");

        ContextHolder.get().setCurrentContextId(oldCtx);
    }

    private void checkId(String id) throws ProjectAlreadyExistsException {
        try {
            getProject(id);
            throw new ProjectAlreadyExistsException("Project with id '" + id + "' already exists.");
        } catch (NoSuchProjectException e) {
            return;
        }
    }

    @Override
    public List<Project> getAllProjects() {
        return new ArrayList<Project>(projects);
    }

    @Override
    public Project getProject(String projectId) throws NoSuchProjectException {
        for (Project p : projects) {
            if (p.getId().equals(projectId)) {
                return p;
            }
        }
        throw new NoSuchProjectException("No project with id '" + projectId + "' found.");
    }

    @Override
    public void updateProject(Project project) throws NoSuchProjectException {
        getProject(project.getId());
        try {
            persistence.update(new ProjectPersist(project.getId()), project.getPersitentPart());
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

        WiringService ws = osgiUtilsService.getService(WiringService.class);
        reportDomain = ws.getDomainEndpoint(ReportDomain.class, "report");
        stopProject(project);
        reportDomain.removeCategory(projectId);
        projects.remove(project);
        try {
            persistence.delete(project.getPersitentPart());
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

    public void setConnectorUtil(ConnectorUtil connectorUtil) {
        this.connectorUtil = connectorUtil;
    }

    public ConnectorUtil getConnectorUtil() {
        return connectorUtil;
    }

    @Override
    public Notification createNotification() {
        return ModelUtils.createEmptyModelObject(Notification.class);
    }

    public void setOsgiUtilsService(OsgiUtilsService osgiUtilsService) {
        this.osgiUtilsService = osgiUtilsService;
    }

    @Override
    public boolean isRemotingAvailable() {
        return session != null;
    }

    @Override
    public void storeBuild(Project project, BuildReason reason) {
        Build build = new Build(project.getId(), reason);
        persistence.create(build);
    }

    @Override
    public void addProjectDependency(Project project,
            DependencyProperties dependency) {
        // TODO Auto-generated method stub
        
    }
}
