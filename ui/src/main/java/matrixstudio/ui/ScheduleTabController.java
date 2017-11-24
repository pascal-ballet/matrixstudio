package matrixstudio.ui;

import fr.minibilles.basics.notification.Notification;
import fr.minibilles.basics.notification.NotificationListener;
import fr.minibilles.basics.ui.BasicsUI;
import fr.minibilles.basics.ui.controller.Controller;
import fr.minibilles.basics.ui.diagram.Element;
import fr.minibilles.basics.ui.field.CompositeField;
import fr.minibilles.basics.ui.field.DiagramField;
import java.util.List;
import matrixstudio.model.Model;
import matrixstudio.model.Scheduler;
import matrixstudio.ui.diagram.SchedulerDiagram;

public class ScheduleTabController extends Controller<Model> {

	private final StudioContext studioContext;

	private CompositeField scheduleComposite;
	private DiagramField<Scheduler> scheduleDiagramField;

	public ScheduleTabController(StudioContext studioContext) {
		this.studioContext = studioContext;
	}

	@Override
	public CompositeField createFields() {
		scheduleDiagramField = new DiagramField<Scheduler>(new SchedulerDiagram(studioContext));
		scheduleDiagramField.getController().addListener(new NotificationListener() {
			public void notified(Notification notification) {
				if ( BasicsUI.NOTIFICATION_SELECTION.equals(notification.name) ) {
					List<Element> selectedElements = scheduleDiagramField.getController().getSelectedElements();
					if ( selectedElements.size() == 0 ) {
						studioContext.setSelection(getSubject().getScheduler());
					} else if ( selectedElements.size() == 1 ) {
						studioContext.setSelection(selectedElements.get(0).getModel());
					}
				}
			}
		});
		
		scheduleComposite = new CompositeField("Scheduler", scheduleDiagramField);
		return scheduleComposite;
	}

	@Override
	public void refreshFields() {
		if ( getSubject() == null ) {
			scheduleComposite.setEnable(false);
		} else {
			scheduleComposite.setEnable(true);
			scheduleDiagramField.setValue(getSubject().getScheduler());
			scheduleDiagramField.refreshDiagram();
		}

	}
}
