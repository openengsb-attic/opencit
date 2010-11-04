package org.openengsb.opencit.ui.web.WizardSteps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardStep;
import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.domain.notification.NotificationDomain;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class NotificationDomainStep extends DynamicWizardStep {
    @SpringBean
    private DomainService domainService;
    private Project project;
    private Map<String, ServiceManager> managersMap = new HashMap<String, ServiceManager>();

    public NotificationDomainStep(Project project) {
        super(new SetSCMStep(project), new ResourceModel("notificationAttribute.title"),
            new ResourceModel("notificationAttribute.summary"), new Model<Project>(project));
        this.project = project;
        List<ServiceManager> manager = domainService.serviceManagersForDomain(NotificationDomain.class);
        DropDownChoice<ServiceDescriptor> descriptorDropDownChoice = initSCMDomains(manager, "project.notificationDescriptor");
        add(descriptorDropDownChoice);
    }

    private DropDownChoice<ServiceDescriptor> initSCMDomains(List<ServiceManager> managers, String dropDownID) {
        final List<ServiceDescriptor> descritors = new ArrayList<ServiceDescriptor>();

        for (ServiceManager sm : managers) {
            managersMap.put(sm.getDescriptor().getName().getString(getLocale()), sm);
            descritors.add(sm.getDescriptor());
        }

        IModel<List<ServiceDescriptor>> dropDownModel = new LoadableDetachableModel<List<ServiceDescriptor>>() {
            @Override
            protected List<ServiceDescriptor> load() {
                return descritors;
            }
        };


        DropDownChoice<ServiceDescriptor> descriptorDropDownChoice = new DropDownChoice<ServiceDescriptor>(dropDownID,
            dropDownModel, new IChoiceRenderer<ServiceDescriptor>() {

                public String getDisplayValue(ServiceDescriptor object) {
                    return object.getName().getString(getLocale());
                }

                public String getIdValue(ServiceDescriptor object, int index) {
                    return object.getImplementationType().getSimpleName();
                }
            });

        return descriptorDropDownChoice;
    }

    @Override
    public boolean isLastStep() {
        return false;
    }

    @Override
    public IDynamicWizardStep next() {
        return new FinalStep(this, project);
    }
}