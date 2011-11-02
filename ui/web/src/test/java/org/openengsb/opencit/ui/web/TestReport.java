package org.openengsb.opencit.ui.web;

import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.domain.report.model.Report;
import org.openengsb.domain.report.model.ReportPart;

public class TestReport implements Report {
    private String name;
    private List<ReportPart> parts;

    public TestReport(String name) {
        this.name = name; 
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ReportPart> getParts() {
        return parts;
    }

    @Override
    public void setName(String arg0) {
        name = arg0;
    }

    @Override
    public void setParts(List<ReportPart> arg0) {
        parts = arg0;
    }

    @Override
    public void addOpenEngSBModelEntry(OpenEngSBModelEntry arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<OpenEngSBModelEntry> getOpenEngSBModelEntries() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeOpenEngSBModelEntry(String arg0) {
        // TODO Auto-generated method stub

    }

}
