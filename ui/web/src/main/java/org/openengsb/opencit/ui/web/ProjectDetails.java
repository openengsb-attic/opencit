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

package org.openengsb.opencit.ui.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.resource.ContextRelativeResource;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.report.Report;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.SchedulingService;
import org.openengsb.opencit.core.projectmanager.model.BuildReason;
import org.openengsb.opencit.core.projectmanager.model.DependencyProperties;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.ManualBuildReason;
import org.openengsb.opencit.ui.web.model.ReportModel;
import org.openengsb.opencit.ui.web.model.SpringBeanProvider;
import org.openengsb.opencit.ui.web.util.StateUtil;
import org.ops4j.pax.wicket.api.PaxWicketBean;

public class ProjectDetails extends BasePage implements SpringBeanProvider<ProjectManager> {

    private static Log log = LogFactory.getLog(BasePage.class);

    @PaxWicketBean
    private ProjectManager projectManager;
    @PaxWicketBean
    private OsgiUtilsService osgiUtilsService;

    @PaxWicketBean
    private SchedulingService scheduler;

    private Image projectStateImage;

    private Button flowButton;

    private WebMarkupContainer projectPanel;

    public ProjectDetails() {
        init();
    }

    @SuppressWarnings("serial")
    private void init() {

        projectPanel = new WebMarkupContainer("projectPanel");
        projectPanel.setOutputMarkupId(true);
        add(projectPanel);

        String projectId = ContextHolder.get().getCurrentContextId();
        projectPanel.add(new Label("project.id", projectId));
        projectPanel.add(new AjaxEditableLabel<String>("project.notification", new IModel<String>() {
            @Override
            public void detach() {
                // do nothing
            }

            @Override
            public String getObject() {
                return projectManager.getCurrentContextProject().getNotificationRecipient();
            }

            @Override
            public void setObject(String recipient) {
                Project p = projectManager.getCurrentContextProject();
                p.setNotificationRecipient(recipient);
                try {
                    projectManager.updateProject(p);
                } catch (NoSuchProjectException e) {
                    log.error(e);
                }
            }
        }));
        Project project = projectManager.getCurrentContextProject();
        String image = StateUtil.getImage(project, scheduler);
        ContextRelativeResource stateResource = new ContextRelativeResource(image);
        stateResource.setCacheable(false);
        projectStateImage = new Image("project.state", stateResource);
        projectStateImage.setOutputMarkupId(true);

        projectPanel.add(projectStateImage);

        Date lastpollDate = project.getLastScmPollDate();
        String dateString;
        if (lastpollDate == null) {
            dateString = "-";
        } else {
            dateString = lastpollDate.toString();
        }

        Label pollerStateLabel =
            new Label("pollerState", new Model<String>(dateString));
        projectPanel.add(pollerStateLabel);

        projectPanel.add(new Link<Index>("back") {
            @Override
            public void onClick() {
                setResponsePage(Index.class);
            }
        });

        Form<String> form = new Form<String>("workflowForm");
        form.setModel(new Model<String>(projectId));
        form.setOutputMarkupId(true);

        flowButton = new Button("flowButton") {

            @Override
            public void onSubmit() {
                String project = ContextHolder.get().getCurrentContextId();
                BuildReason reason = new ManualBuildReason();
                scheduler.scheduleProjectForBuild(project, reason);
                setResponsePage(ProjectDetails.class);
            }

        };
        flowButton.setOutputMarkupId(true);

        flowButton.setEnabled(!scheduler.isProjectBuilding(projectId));
        form.add(flowButton);
        projectPanel.add(form);

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        projectPanel.add(feedbackPanel);

        initReportPanel();
        initDependenciesPanel();
    }

    private void initReportPanel() {
        WebMarkupContainer reportsPanel = new WebMarkupContainer("reportsPanel");
        reportsPanel.setOutputMarkupId(true);

        IModel<List<Report>> reportsModel = createReportsModel();

        Label noReports =
            new Label("noReports", new StringResourceModel("noReportsAvailable", this, null));
        noReports.setVisible(false);
        noReports.setOutputMarkupId(true);

        reportsPanel.add(createReportListView(reportsModel, "reportlist"));
        reportsPanel.add(noReports);

        add(reportsPanel);
        if (reportsModel.getObject().isEmpty()) {
            noReports.setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    private void initDependenciesPanel() {
        WebMarkupContainer dependenciesPanel = new WebMarkupContainer("dependenciesPanel");
        dependenciesPanel.setOutputMarkupId(true);

        IModel<List<DependencyProperties>> dependencyModel = createDependencyModel();

        dependenciesPanel.add(createDependencyListView(dependencyModel, "dependenciesList"));

        dependenciesPanel.add(new Link<Project>("addDependency") {
            @Override
            public void onClick() {
                setResponsePage(AddDependency.class);
            }
        });

        add(dependenciesPanel);
    }

    @SuppressWarnings("serial")
    private IModel<List<Report>> createReportsModel() {
        return new LoadableDetachableModel<List<Report>>() {
            @Override
            protected List<Report> load() {
                String projectId = ContextHolder.get().getCurrentContextId();
                ReportDomain reportDomain;
                WiringService ws = osgiUtilsService.getService(WiringService.class);
                reportDomain = ws.getDomainEndpoint(ReportDomain.class, "report");

                List<Report> reports = new ArrayList<Report>(reportDomain.getAllReports(projectId));
                Comparator<Report> comparator = Collections.reverseOrder(new Comparator<Report>() {
                    @Override
                    public int compare(Report report1, Report report2) {
                        String name1 = report1.getName();
                        String name2 = report2.getName();
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
                            Date date1 = format.parse(name1);
                            Date date2 = format.parse(name2);
                            return date1.compareTo(date2);
                        } catch (ParseException pe) {
                            return name1.compareTo(name2);
                        }
                    }
                });
                Collections.sort(reports, comparator);
                return reports;
            }
        };
    }

    @SuppressWarnings("serial")
    private ListView<Report> createReportListView(IModel<List<Report>> projectsModel,
            String id) {
        return new ListView<Report>(id, projectsModel) {

            @Override
            protected void populateItem(ListItem<Report> item) {
                Report report = item.getModelObject();

                ContextRelativeResource stateResource = new ContextRelativeResource(StateUtil.getImage(report));
                stateResource.setCacheable(false);
                Image projectStateImage = new Image("report.state", stateResource);
                projectStateImage.setOutputMarkupId(true);

                item.add(projectStateImage);

                item.add(new Label("report.name", report.getName()));
                item.add(new Link<Report>("report.link", item.getModel()) {
                    @Override
                    public void onClick() {
                        WiringService ws = osgiUtilsService.getService(WiringService.class);
                        ReportModel reportModel =
                            new ReportModel(ContextHolder.get().getCurrentContextId(), getModelObject(), ws);
                        setResponsePage(new ReportViewPage(reportModel));
                    }
                });
            }

        };
    }

    @SuppressWarnings("serial")
    private IModel<List<DependencyProperties>> createDependencyModel() {
        return new LoadableDetachableModel<List<DependencyProperties>>() {
            @Override
            protected List<DependencyProperties> load() {
                Project project = projectManager.getCurrentContextProject();

                List<DependencyProperties> dependencies = new ArrayList<DependencyProperties>(project.getDependencies());
                return dependencies;
            }
        };
    }

    @SuppressWarnings("serial")
    private ListView<DependencyProperties> createDependencyListView(IModel<List<DependencyProperties>> depModel,
            String id) {
        return new ListView<DependencyProperties>(id, depModel) {

            @Override
            protected void populateItem(ListItem<DependencyProperties> item) {
                DependencyProperties dep = item.getModelObject();
                item.add(new Label("dependency.name", dep.getId()));
            }

        };
    }

    @Override
    public ProjectManager getSpringBean() {
        return projectManager;
    }

}
