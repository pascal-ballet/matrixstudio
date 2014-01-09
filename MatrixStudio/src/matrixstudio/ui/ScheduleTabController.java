package matrixstudio.ui;

import java.util.List;

import matrixstudio.model.Model;
import matrixstudio.model.Scheduler;
import matrixstudio.ui.diagram.SchedulerDiagram;

import org.xid.basics.notification.Notification;
import org.xid.basics.notification.NotificationListener;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.controller.Controller;
import org.xid.basics.ui.diagram.Element;
import org.xid.basics.ui.field.CompositeField;
import org.xid.basics.ui.field.DiagramField;

public class ScheduleTabController extends Controller<Model> {

	private final MatrixStudio matrixStudio;

	private CompositeField scheduleComposite;
	private DiagramField<Scheduler> scheduleDiagramField;

	public ScheduleTabController(MatrixStudio matrixStudio) {
		this.matrixStudio = matrixStudio;
	}

	@Override
	public CompositeField createFields() {
		scheduleDiagramField = new DiagramField<Scheduler>(new SchedulerDiagram());
		scheduleDiagramField.getController().addListener(new NotificationListener() {
			public void notified(Notification notification) {
				if ( BasicsUI.NOTIFICATION_SELECTION.equals(notification.name) ) {
					List<Element> selectedElements = scheduleDiagramField.getController().getSelectedElements();
					if ( selectedElements.size() == 0 ) {
						matrixStudio.setSelection(getSubject().getScheduler());
					} else if ( selectedElements.size() == 1 ) {
						matrixStudio.setSelection(selectedElements.get(0).getModel());
					}
				}
			}
		});
		
		scheduleComposite = new CompositeField("SchedulerTest", scheduleDiagramField);
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
