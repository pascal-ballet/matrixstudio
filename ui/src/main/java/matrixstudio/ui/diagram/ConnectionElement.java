package matrixstudio.ui.diagram;

import fr.minibilles.basics.geometry.Geometry;
import fr.minibilles.basics.geometry.Polyline;
import fr.minibilles.basics.ui.diagram.DiagramContext;
import fr.minibilles.basics.ui.diagram.Element;
import fr.minibilles.basics.ui.diagram.gc.GC;
import fr.minibilles.basics.ui.diagram.gc.GcUtils;
import fr.minibilles.basics.ui.diagram.interaction.Handle;
import fr.minibilles.basics.ui.diagram.interaction.InteractionObject;
import java.util.List;
import matrixstudio.model.Task;


public class ConnectionElement extends Element.Stub implements Element {

	private final Task from;
	private final Task to;
	
	private float[] polyline;
	
	public ConnectionElement(Task from, Task to) {
		this.from = from;
		this.to = to;
	}
	
	public Task getFrom() {
		return from;
	}
	
	public Task getTo() {
		return to;
	}
	
	public Task getModel() {
		return from;
	}

	public Task getModel2() {
		return to;
	}
	
	public float[] getPoint() {
		if ( from != null ) return from.getPositionCopy();
		if ( to != null ) return to.getPositionCopy();
		return new float[] { 0f, 0f };
	}

		
	private float[] computePolyline(DiagramContext context) {
		final RectangleElement fromElement = context.getDiagram().findElement(RectangleElement.class, from);
		final RectangleElement toElement = context.getDiagram().findElement(RectangleElement.class, to);
		
		final float[] fromRectangle = new float[4];
		fromElement.computeRectangle(fromRectangle);
		
		final float[] toRectangle = new float[4];
		toElement.computeRectangle(toRectangle);
	
		float[] fromPoint = Geometry.copyPoints(fromElement.getPoint());
		float[] toPoint = Geometry.copyPoints(toElement.getPoint());
		
		Geometry.projectPointOnRectangleAlt(toPoint, fromRectangle);
		Geometry.projectPointOnRectangleAlt(fromPoint, toRectangle);
		
		polyline = new float[4];
		polyline[0] = toPoint[0];
		polyline[1] = toPoint[1];
		polyline[2] = fromPoint[0];
		polyline[3] = fromPoint[1];
		return polyline;
	}

	public void computeBounds(float[] result, DiagramContext context) {
		computePolyline(context);
		
		Geometry.setNullRectangle(result);
		Geometry.rectangleMergeWithPoint(result, Geometry.getPoint(polyline, 0));
		Geometry.rectangleMergeWithPoint(result, Geometry.getPoint(polyline, 1));
		Geometry.expandRectangle(result, 6f, 6f);
	}

	public void display(GC gc, DiagramContext context) {
		computePolyline(context);
		GcUtils.drawPolyline(gc, polyline, false);
		gc.setBackground(gc.getForeground());
		drawArrow(gc, polyline, 5f, false);
	}
	
	@Override
	public void hitTesting(List<Element> result, float[] detectionPoint, float[] detectionRectangle, int type, DiagramContext context) {
		if ( from == null || to == null ) return;
		
		if ( type == DiagramContext.HIT_SELECTION ) {
			computePolyline(context);
			
			if ( detectionRectangle == null ) {
				// creates a detection rectangle from the point
				detectionRectangle = Geometry.rectangleFromPoints(detectionPoint, detectionPoint);
				Geometry.expandRectangle(detectionRectangle, 5f, 5f);
			}
			
			if ( Polyline.intersectsRectangle(polyline, detectionRectangle) ) {
				result.add(this);
			}
		}
	}
	
	@Override
	public void computeInteractionObjects(List<InteractionObject> result, int type, DiagramContext context) {
		if ( from == null || to == null ) return;
		
		if ( type == DiagramContext.HIT_SELECTION ) {
			computePolyline(context);
			Handle.addPolylineHandles(result, this, polyline, false, true);
		}
	}
	
	
	/** Displays an arrow tip at the end of the given polyline. */
	public static void drawArrow( GC gc, float[] line, float size, boolean fill) {
		if ( line[2] == line[0] && line[3] == line[1] ) return;
		
		float vx = line[2] - line[0]; 
		float vy = line[3] - line[1]; 
		float d = (float) Math.sqrt((double) vx*vx + vy*vy);
		vx = (vx*size) / d;
		vy = (vy*size) / d;

		// vx and vy is the unit vector times arrow size that represents the last segment direction
		int trianglePoints [] = new int[] { 
				(int) (line[2] - vx  - vy),
				(int) (line[3] - vy  + vx),
				(int) line[2],
				(int) line[3],
				(int) (line[2] - vx  + vy),
				(int) (line[3] - vy  - vx)
		};
		
		if ( fill ) {
			gc.drawPolygon(trianglePoints);
			gc.fillPolygon(trianglePoints);
		} else {
			gc.drawPolyline(trianglePoints);
		}
	}

	
}
