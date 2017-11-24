package matrixstudio.ui.diagram;

import fr.minibilles.basics.geometry.Geometry;
import fr.minibilles.basics.ui.diagram.DiagramContext;
import fr.minibilles.basics.ui.diagram.Element;
import fr.minibilles.basics.ui.diagram.gc.GC;
import fr.minibilles.basics.ui.diagram.gc.GcUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;


public class LimitElement extends Element.Stub implements Element {

	private final boolean begin;
	
	public LimitElement(boolean begin) {
		this.begin = begin;
	}
	
	public Boolean getModel() {
		return begin;
	}

	@Override
	public float[] getPoint() {
		// not used
		return new float[] { 0f, 0f };
	}
	
	public void computeRectangle(float[] rectangle, DiagramContext context) {
		final float[] bounds = context.getDiagram().getElementsBounds(context);
		if ( begin ) {
			rectangle[0] = 0f;
			rectangle[2] = 5f;
		} else {
			float right = ((SchedulerDiagram) context.getDiagram()).getRight();
			rectangle[0] = right;
			rectangle[2] = right + 5f;
		}
		rectangle[1] = bounds[0];	
		rectangle[3] = bounds[3];	
	}
	
	public void computeBounds(float[] result, DiagramContext context) {
		computeRectangle(result, context);
		Geometry.expandRectangle(result, 2f, 2f);
	}

	public void display(GC gc, DiagramContext context) {
		final float[] rectangle = new float[4];
		computeRectangle(rectangle, context);
		
		final Color color = context.getResources().getSystemColor(SWT.COLOR_GRAY);
		gc.setForeground(color);
		gc.setBackground(color);
		gc.setAlpha(200);
		GcUtils.drawRectangle(gc, rectangle, true);
	}

	
}
