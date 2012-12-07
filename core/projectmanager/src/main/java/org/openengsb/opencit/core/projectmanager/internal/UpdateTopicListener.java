package org.openengsb.opencit.core.projectmanager.internal;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.UpdateNotification;

public class UpdateTopicListener implements MessageListener {
    private Project project;
    private String dependency;
    private ProjectManagerImpl projectManager;

    private static Log log = LogFactory.getLog(ProjectManagerImpl.class);

    @Override
    public void onMessage(Message message) {
        if (!(message instanceof ObjectMessage)) {
            log.error("Received message is not an instance of ObjectMessage: " + message);
            return;
        }
        ObjectMessage objectMessage = (ObjectMessage) message;
        Serializable object;
        try {
            object = objectMessage.getObject();
        } catch (JMSException e) {
            log.error("Failed to de-serialize the JMS object", e);
            return;
        }
        if (!(object instanceof UpdateNotification)) {
            log.error("Received object is not an instance of UpdateNotification: " + object);
            return;
        }
        UpdateNotification notification = (UpdateNotification) object;
        projectManager.processDependencyUpdate(project, dependency, notification);
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public String getDependency() {
        return dependency;
    }

    public void setProjectManager(ProjectManagerImpl projectManager) {
        this.projectManager = projectManager;
    }

    public ProjectManagerImpl getProjectManager() {
        return projectManager;
    }
}
