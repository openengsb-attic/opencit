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

package org.openengsb.opencit.ui.web;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.resource.ContextRelativeResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.domains.report.ReportDomain;
import org.openengsb.domains.report.model.Report;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.ReportModel;
import org.openengsb.opencit.ui.web.util.StateUtil;

public class ProjectDetails extends BasePage {

    private IModel<Project> projectModel;

    @SpringBean
    private ContextCurrentService contextService;

    @SpringBean
    private ReportDomain reportDomain;

    public ProjectDetails(IModel<Project> projectModel) {
        this.projectModel = projectModel;

        add(new Label("project.id", projectModel.getObject().getId()));
        add(new Image("project.state", new ContextRelativeResource(StateUtil.getImage(projectModel.getObject()))));
        add(new BookmarkablePageLink<Index>("back", Index.class));

        initReportPanel();
    }

    private void initReportPanel() {
        WebMarkupContainer reportsPanel = new WebMarkupContainer("reportsPanel");
        reportsPanel.setOutputMarkupId(true);

        @SuppressWarnings("serial")
        IModel<List<Report>> reportsModel = new LoadableDetachableModel<List<Report>>() {
            @Override
            protected List<Report> load() {
                String projectId = projectModel.getObject().getId();
                contextService.setThreadLocalContext(projectId);
                return reportDomain.getAllReports(projectId);
            }
        };

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
    private ListView<Report> createReportListView(IModel<List<Report>> projectsModel,
            String id) {
        return new ListView<Report>(id, projectsModel) {

            @Override
            protected void populateItem(ListItem<Report> item) {
                Report report = item.getModelObject();
                item.add(new Label("report.name", report.getName()));
                item.add(new Link<Report>("report.link", item.getModel()) {
                    @Override
                    public void onClick() {
                        IModel<Report> reportModel = new ReportModel(projectModel.getObject()
                            .getId(), getModelObject());
                        setResponsePage(new ReportViewPage(projectModel, reportModel));
                    }
                });
            }

        };
    }

}
