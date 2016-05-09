package matrixstudio.ui;

import matrixstudio.model.Matrix;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Shell;


/**
 * A {@link MatrixRenderer} is able to create an {@link Image} from a {@link Matrix}.
 */
public interface MatrixRenderer {

	void render(GC gc, RendererContext context, Matrix matrix, boolean draw3D, float dx3D, float dy3D, float dz3D, float angleX3D, float angleY3D, Shell shell3D, GLCanvas gl_canvas, int renderMode);
	
}
