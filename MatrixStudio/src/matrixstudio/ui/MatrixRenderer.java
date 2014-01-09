package matrixstudio.ui;

import matrixstudio.model.Matrix;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;


/**
 * A {@link MatrixRenderer} is able to create an {@link Image} from a {@link Matrix}.
 */
public interface MatrixRenderer {

	void render(GC gc, RendererContext context, Matrix matrix);
	
}
