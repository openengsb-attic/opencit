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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.domains.report.model.Report;
import org.openengsb.domains.report.model.ReportPart;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class ReportViewPage extends BasePage {

    private Project project;

    private Report report;

    public ReportViewPage(Project project, Report report) {
        this.project = project;
        this.report = report;
        add(new Label("project.id", project.getId()));
        add(new Label("report.name", report.getName()));
        createBackLink();
        initReportPartsPanel();
    }

    @SuppressWarnings("serial")
    private void createBackLink() {
        add(new Link<Project>("back") {
            @Override
            public void onClick() {
                setResponsePage(new ProjectDetails(project));
            }
        });
    }

    private void initReportPartsPanel() {
        WebMarkupContainer reportPartsPanel = new WebMarkupContainer("reportPartsPanel");
        reportPartsPanel.setOutputMarkupId(true);

        @SuppressWarnings("serial")
        IModel<List<ReportPart>> reportPartsModel = new LoadableDetachableModel<List<ReportPart>>() {
            @Override
            protected List<ReportPart> load() {
                return report.getParts();
            }
        };

        Label noReportParts =
            new Label("noReportParts", new StringResourceModel("noReportPartsAvailable", this, null));
        noReportParts.setVisible(false);
        noReportParts.setOutputMarkupId(true);

        reportPartsPanel.add(createPartsListView(reportPartsModel, "partslist"));
        reportPartsPanel.add(noReportParts);

        add(reportPartsPanel);
        if (reportPartsModel.getObject().isEmpty()) {
            noReportParts.setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    private ListView<ReportPart> createPartsListView(IModel<List<ReportPart>> projectsModel,
            String id) {
        return new ListView<ReportPart>(id, projectsModel) {

            @Override
            protected void populateItem(ListItem<ReportPart> item) {
                ReportPart part = item.getModelObject();
                item.add(new Label("part.name", part.getPartName()));
                item.add(new Label("part.content", new String(part.getContent())));
            }

        };
    }
}
