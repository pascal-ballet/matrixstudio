package matrixstudio.ui.diagram;

import fr.minibilles.basics.geometry.Geometry;
import fr.minibilles.basics.ui.diagram.DiagramContext;
import fr.minibilles.basics.ui.diagram.Element;
import fr.minibilles.basics.ui.diagram.gc.GC;
import fr.minibilles.basics.ui.diagram.gc.GcUtils;
import fr.minibilles.basics.ui.diagram.interaction.Handle;
import fr.minibilles.basics.ui.diagram.interaction.InteractionLine;
import fr.minibilles.basics.ui.diagram.interaction.InteractionObject;
import java.text.ParseException;
import java.util.List;
import matrixstudio.formula.EvaluationException;
import matrixstudio.formula.FormulaCache;
import matrixstudio.model.Kernel;
import matrixstudio.model.Task;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;


public class TaskElement extends Element.Stub implements Element, RectangleElement {

	private final RGB backgroundRGB = new RGB(181,213,255);
	
	private Task task;
	private float[] point;

	private Font smallFont = null;
	
	public TaskElement(Task task) {
		this.task = task;
		this.point = task.getPositionCopy();
	}
	
	public Task getModel() {
		return task;
	}

	public float[] getPoint() {
		return point;
	}
	
	public void computeRectangle(float[] rectangle) {
	    int halfLength = (getName().length() *8)/2 + 5;
        if (halfLength < 50) halfLength = 50;
		rectangle[0] = point[0] - halfLength;
		rectangle[1] = point[1] - 25f;
		rectangle[2] = point[0] + halfLength;
		rectangle[3] = point[1] + 25f;
	}

	public void computeBounds(float[] result, DiagramContext context) {
		computeRectangle(result);
		Geometry.expandRectangle(result, 4f, 4f);
	}

	private Color getColor(DiagramContext context) {
		return context.getResources().getColor(backgroundRGB);
	}

	private int computeFormula(String formula) throws ParseException, EvaluationException {
        return FormulaCache.SHARED.computeValue(formula, getModel().getScheduler().getModel());
    }

	public void display(GC gc, DiagramContext context) {
		
		if ( smallFont == null ) {
			FontData data = gc.getFont().getFontData()[0];
			data.height = 10f;
			// FIXME: data.style doesn't exist on windows, sure strange thing.
			//data.style = SWT.ITALIC;
			smallFont = context.getResources().getFont(data);
		}
		
		float[] rectangle = new float[4];
		computeRectangle(rectangle);

		// draws connection to limit (if any) 
		if ( task.getTaskInCount() == 0 ) {
			final float[] westPoint = Geometry.getRectanglePoint(rectangle, Geometry.WEST);
			gc.setForeground(context.getResources().getSystemColor(SWT.COLOR_DARK_GRAY));
			GcUtils.drawLine(gc, 5f, westPoint[1], westPoint[0], westPoint[1]);
			GcUtils.drawTriangle2(gc, westPoint[0], westPoint[1], 5f, 5f, Geometry.EAST, false);
		}
		
		if ( task.getTaskOutCount() == 0 ) {
			float right = ((SchedulerDiagram) context.getDiagram()).getRight();
			final float[] eastPoint = Geometry.getRectanglePoint(rectangle, Geometry.EAST);
			gc.setForeground(context.getResources().getSystemColor(SWT.COLOR_DARK_GRAY));
			GcUtils.drawLine(gc, right, eastPoint[1], eastPoint[0], eastPoint[1]);
			
			GcUtils.drawTriangle2(gc, right, eastPoint[1], 5f, 5f, Geometry.EAST, false);
		}
		
		
		// draws shadow
		Geometry.translatePointsBy(rectangle, 3f, 3f);
		gc.setForeground(context.getResources().getSystemColor(SWT.COLOR_GRAY));
		gc.setBackground(context.getResources().getSystemColor(SWT.COLOR_GRAY));
		GcUtils.drawRoundRectangle(gc, rectangle, 10f, 10f, true);

		// draws box
		Geometry.translatePointsBy(rectangle, -3f, -3f);
		gc.setForeground(context.getResources().getSystemColor(SWT.COLOR_BLACK));
		gc.setBackground(getColor(context));
		GcUtils.drawRoundRectangle(gc, rectangle, 10f, 10f, true);
		
		// draws kernel name
        if (getModel().getKernelCount() > 0) {
            GcUtils.drawStringAligned(gc, getName() + " ["+getDurations()+"ns]", point[0], point[1] - 8f, Geometry.CENTER);
        } else {
            //GcUtils.drawImageAligned(gc, context.getResources().getImage("error_tsk.gif"), point[0] - 40f, point[1] - 14f, Geometry.NORTH_WEST);
            GcUtils.drawStringAligned(gc, "[empty]", point[0], point[1] - 8f, Geometry.CENTER);
        }

		Font oldFont = gc.getFont();
		gc.setFont(smallFont);

        // prints repetition if needed
        try {
            int repetition = computeFormula(task.getRepetition());
            if (repetition > 1) {
                gc.setBackground(context.getResources().getSystemColor(SWT.COLOR_WHITE));
                gc.fillOval((int) rectangle[0] + 3, (int) rectangle[3] - 15, (repetition >= 100) ? 30 : 15, 12);
                GcUtils.drawStringAligned(gc, Integer.toString(repetition), rectangle[0] + 5f, rectangle[3] - 15f, Geometry.NORTH_WEST);
            }
        } catch (ParseException | EvaluationException e) {
            GcUtils.drawImageAligned(gc, context.getResources().getImage("error_tsk.gif"), rectangle[0] + 3, rectangle[3] - 15, Geometry.NORTH_WEST);
        }

        try {
            int gx = computeFormula(task.getGlobalWorkSizeX());
            int gy = computeFormula(task.getGlobalWorkSizeY());
            int gz = computeFormula(task.getGlobalWorkSizeZ());

            // prints info at bottom
            final StringBuilder info = new StringBuilder();
            info.append("(");
            info.append(gx);
            info.append(",");
            info.append(gy);
            info.append(",");
            info.append(gz);
            info.append(")");
            
            GcUtils.drawStringAligned(gc, info.toString(), point[0], point[1]+12f, Geometry.CENTER);

            gc.setFont(oldFont);


        } catch (ParseException | EvaluationException e) {
            GcUtils.drawImageAligned(gc, context.getResources().getImage("error_tsk.gif"), point[0], point[1]+5f, Geometry.NORTH);
        }

		// draws create connection icon.
		GcUtils.drawImageAligned(gc, context.getResources().getImage("create_connection.gif"), rectangle[2] - 20f, rectangle[1] + 2f, Geometry.NORTH_WEST);
	}

    private String getName() {
        StringBuilder name = new StringBuilder();
        for (Kernel kernel : task.getKernelList()) {
            if (name.length() > 0) name.append(",");
            name.append(kernel.getName());
        }
        return name.toString();
    }

    private String getDurations() {
        StringBuilder durations = new StringBuilder();
        for (Kernel kernel : task.getKernelList()) {
            if (durations.length() > 0) durations.append(",");
            durations.append(kernel.getDuration());
        }
        return durations.toString();
    }
    
    @Override
	public void hitTesting(List<Element> result, float[] detectionPoint, float[] detectionRectangle, int type, DiagramContext context) {
		if ( type == DiagramContext.HIT_SELECTION ) {
			float[] rectangle = new float[4];
			computeRectangle(rectangle);
			if ( detectionRectangle != null ) {
				if ( Geometry.rectangleIntersectsRectangle(detectionRectangle, rectangle) ) {
					result.add(this);
				}
			} else {
				if ( Geometry.rectangleContainsPoint(rectangle, detectionPoint) ) {
					result.add(this);
				}
			}
		}
	}
	
	@Override
	public void computeInteractionObjects(List<InteractionObject> result, int type, DiagramContext context) {
		if ( type == DiagramContext.HIT_SELECTION ) {
		    float[] rectangle = new float[4];
		    computeRectangle(rectangle);
			Handle.addRectangleHandles(result, this, rectangle, true, false);
		}
	}
	
	private InteractionLine feedbackConnector;
	
	private boolean isLinkMove(DiagramContext context) {
		float [] linkRectangle = new float[4];
        computeRectangle(linkRectangle);
		linkRectangle[0] = linkRectangle[2] - 20f;
		linkRectangle[1] = linkRectangle[1];
		linkRectangle[2] = linkRectangle[2];
		linkRectangle[3] = linkRectangle[1] + 20f;
		return Geometry.rectangleContainsPoint(linkRectangle, context.getClickedMousePoint());
	}
	

	@Override
	public boolean move(float[] movePoint, InteractionObject interaction, int step, DiagramContext context) {
		boolean move = false;
		if ( isLinkMove(context) ) {
			final float[] rectangle = new float[4];
			computeRectangle(rectangle);

			final float[] mousePoint = context.getMousePoint();
			final float[] source = Geometry.copyPoints(mousePoint);
			Geometry.projectPointOnRectangleAlt(source, rectangle);

			if ( step == 0 ) {
				feedbackConnector = new InteractionLine(this, 200, Geometry.CENTER, source, mousePoint);
				context.addInteractionObject(feedbackConnector);
			} else if ( step == -1 ) {
				context.invalidate(feedbackConnector);
				context.removeInteractionObject(feedbackConnector);
				feedbackConnector = null;
				
				final TaskElement target = context.findElementUnder(DiagramContext.HIT_SELECTION, mousePoint, TaskElement.class);
				if ( target != null && target != this ) {
					final Task targetTask = target.getModel();
					task.addTaskOut(targetTask);
					targetTask.addTaskIn(task);
					move = true;
				}
				
			} else {
				if ( feedbackConnector != null ) {
					context.invalidate(feedbackConnector);
					feedbackConnector.setSource(source);
					feedbackConnector.setTarget(mousePoint);
					context.invalidate(feedbackConnector);
				}
			}
			
		} else {
			// prevents to go off screen (on MacOS X).
			if ( movePoint[0] < 60f ) movePoint[0] = 60f;
			if ( movePoint[1] < 30f ) movePoint[1] = 30f;

			Geometry.gridPoints(movePoint, 10f, 10f);
			
			// resets bounds to move the end line.
			context.getDiagram().invalidateBounds();
			
			context.invalidate(this);
			Geometry.copyPoints(movePoint, point);
			context.invalidate(this);
			context.invalidateRectangle(null);
			
			if ( step == -1 ) {
				task.setPosition(point);
				move = true;
			}
		}
		return move;
	}
}
