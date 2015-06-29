package matrixstudio.ui.diagram;

import matrixstudio.model.Kernel;
import matrixstudio.model.Model;
import matrixstudio.model.Scheduler;
import matrixstudio.model.Task;
import matrixstudio.ui.MSResources;
import matrixstudio.ui.StudioContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.xid.basics.geometry.Geometry;
import org.xid.basics.progress.ActionMonitor;
import org.xid.basics.ui.Resources;
import org.xid.basics.ui.action.Action;
import org.xid.basics.ui.diagram.Diagram;
import org.xid.basics.ui.diagram.DiagramContext;
import org.xid.basics.ui.diagram.DiagramController;
import org.xid.basics.ui.diagram.Element;
import org.xid.basics.ui.diagram.gc.GC;
import org.xid.basics.ui.diagram.gc.GcUtils;

import java.util.List;


public class SchedulerDiagram extends Diagram<Scheduler> {

    private final StudioContext context;

    public SchedulerDiagram(StudioContext context) {
        this.context = context;
    }

    public StudioContext getContext() {
        return context;
    }

    @Override
	public void build() {
		clearElements();
		if ( getModel() != null ) {
			
			// adds tasks and their connections.
			for ( Task task : getModel().getTaskList() ) {
				TaskElement element = new TaskElement(task);
				addElement(element);
				
				for ( Task target : task.getTaskOutList() ) {
					addElement(new ConnectionElement(task, target)); 
				}
			}
			
			// adds begin and end elements.
			addElement(0, new LimitElement(true));
			addElement(0, new LimitElement(false));
		}
		
		invalidateBounds();
	}

	public float getRight() {
		float left = Float.MAX_VALUE;
		float right = 0f;
		
		for ( Element element : getElements() ) {
			if ( element instanceof TaskElement ) {
				float x = element.getPoint()[0];
				if ( x < left ) left = x;
				if ( x > right ) right = x;
			}
			
		}
		// right is symmetrical to left
		return left + right;
	}
	
	
	@Override
	public void displayBackground(GC gc, DiagramContext context) {
		final Canvas canvas = (Canvas) context.getControl();
		final float[] bounds = GcUtils.fromSWTRectangle(canvas.getClientArea());
		final float[] location = GcUtils.fromSWTPoint(canvas.getLocation());
		Geometry.translatePointsBy(bounds, location[0], location[1]);
		
		float minX = Math.round((bounds[0]) / 120f) * 120f - 120f;
		float maxX = Math.round((bounds[2]) / 120f) * 120f + 120f;
		float minY = Math.round((bounds[1]) / 60f) * 60f - 60f;
		float maxY = Math.round((bounds[3]) / 60f) * 60f + 60f;

		gc.setBackground(getBackground(context));
		
		gc.fillRectangle(gc.getClipping());
		
		RGB rgb = new RGB(230, 230, 230);
		gc.setForeground(context.getResources().getColor(rgb));
		gc.setLineWidth(0);
		gc.setLineStyle(SWT.LINE_DOT);
		
		float current = minX;
		while ( current <= maxX ) {
			GcUtils.drawLine(gc, current, minY, current, maxY);
			current += 120f;
		}
		
		current = minY;
		while ( current <= maxY ) {
			GcUtils.drawLine(gc, minX, current, maxX, current);
			current += 60f;
		}
	}
	
	@Override
	public Class<? extends Resources> getResourcesClass() {
		return MSResources.class;
	}

	
	@Override
	public void computeActions(final List<Action> result, final DiagramContext context) {
		result.add(new Action.Stub(
					"Add a Task", 
					context.getResources().getImage("add_task.png"), 
					Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL
				) {
			
			@Override
			public String getTooltip() {
				return "Create a Task.";
			}

			@Override
			public int getVisibility() {
				return getModel() == null ? VISIBILITY_DISABLE : VISIBILITY_ENABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				final Task task = new Task();
				
				final Model root = model.getModel();
				final List<Kernel> kernelList = root.getKernelList();
				if ( kernelList.size() > 0) {
					task.setKernel(kernelList.get(0));
				}
				task.setPosition(new float[] { 100f, 100f });
				getModel().addTaskAndOpposite(task);
				return Action.STATUS_OK;
			}
		});

		result.add(new Action.Stub(
					"Delete", 
					context.getResources().getImage("eclipse/delete.gif"), 
					Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL
				) {
			
			@Override
			public String getTooltip() {
				return "Delete selected elements.";
			}
			
			@Override
			public int getVisibility() {
				return context.getSelectedElements().isEmpty() ? VISIBILITY_DISABLE : VISIBILITY_ENABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				for ( Element element : context.getSelectedElements() ) {
					if ( element instanceof TaskElement ) {
						final Task task = (Task) element.getModel();
						getModel().removeTaskAndOpposite(task);
						for ( Task in : task.getTaskInList() ) {
							in.removeTaskOut(task);
						}
						for ( Task out : task.getTaskOutList() ) {
							out.removeTaskIn(task);
						}
					} else if ( element instanceof ConnectionElement ) {
						final ConnectionElement connection = (ConnectionElement) element;
						connection.getFrom().removeTaskOut(connection.getTo());
						connection.getTo().removeTaskIn(connection.getFrom());
					}
				}
				return Action.STATUS_OK;
			}
		});
		
		result.add(new Action.Stub(Action.STYLE_DEFAULT | Action.STYLE_SEPARATOR));
		result.add(((DiagramController)context).createSaveImageAction());
	}
}
