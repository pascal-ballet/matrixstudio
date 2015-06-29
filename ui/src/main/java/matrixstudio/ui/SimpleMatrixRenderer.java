package matrixstudio.ui;

import matrixstudio.model.Matrix;
import matrixstudio.model.MatrixFloat;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.MatrixULong;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;



public class SimpleMatrixRenderer implements MatrixRenderer {

	public void render(GC gc, RendererContext context, Matrix matrix) {
		ImageData imageData = null;
		
		if ( matrix instanceof MatrixInteger ) {
			MatrixInteger matrixInteger = (MatrixInteger) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(matrix.getSizeX(), matrix.getSizeY(), 32, palette);
			if(matrix.getSizeZ() <= 1) {
				imageData.setPixels(0, 0, matrixInteger.getMatrix().length, matrixInteger.getMatrix(), 0);
			} else {
				// 3D Render (very simple: fill from far z to close z
				for (int k=matrix.getSizeZ()-1; k>=0; k--) {
					for (int i=0; i<matrix.getSizeX(); i++) {
						for ( int j=0; j<matrix.getSizeY(); j++) {
							Integer value = matrixInteger.getMatrix()[k*matrix.getSizeX()*matrix.getSizeY() + j*matrix.getSizeX() + i];
							int r,g,b;
							r = (value & 255);
							g = (value >> 8) & 255;
							b = (value >> 16) & 255;
							int nr,ng,nb;
							nr = (r + imageData.getPixel(i, j) & 255)/2 ; 		if(nr > 255) nr = 255;
							ng = (g + (imageData.getPixel(i, j)>>8) & 255)/2; 	if(ng > 255) ng = 255;
							nb = (b + (imageData.getPixel(i, j)>>16) & 255)/2;	if(nb > 255) nb = 255;
							RGB rgb = new RGB(nr, ng, nb);
							imageData.setPixel(i, j, palette.getPixel(rgb));
						}
					}
				}
			}
		}
		
		if ( matrix instanceof MatrixULong ) {
			MatrixULong matrixULong = (MatrixULong) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(matrix.getSizeX(), matrix.getSizeY(), 32, palette);
			for (int i=0; i<matrix.getSizeX(); i++) {
				for ( int j=0; j<matrix.getSizeY(); j++) {
					long value = matrixULong.getMatrix()[j*matrix.getSizeX() + i];
					float h,s,b; 
					h = 0.0f; // Hue
					s = 1.0f; // Saturation
					b = 0.0f; // Brightness (min = 100)
					
					// first int value used to Brightness
					int fi = (int) value;
					b = fi/10000.0f; 
					if(b < 0.32f) b = 0.3f; 
					if(b > 1.0f) b = 1.0f;
					
					// second int value used to Hue
					int si = (int) (value >> 32);
					h = si/10000.0f; 
					if(h < 0.32f) h = 0.3f; 
					if(h > 1.0f) h = 1.0f;
					RGB rgb = new RGB(h*360.0f, s, b);
					imageData.setPixel(i, j, palette.getPixel(rgb));
				}
			}
		}
		
		if ( matrix instanceof MatrixFloat ) {
			MatrixFloat matrixFloat = (MatrixFloat) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(matrix.getSizeX(), matrix.getSizeY(), 32, palette);
			for (int i=0; i<matrix.getSizeX(); i++) {
				for ( int j=0; j<matrix.getSizeY(); j++) {
					Float value = matrixFloat.getMatrix()[j*matrix.getSizeX() + i];
					float h,s,b; 
					h = 0.0f; // Hue
					s = 1.0f; // Saturation
					b = 0.0f; // Brightness (min = 100)
					
					// Int value used to Brightness
					int pe = (int) (Math.floor(value.floatValue()));
					b = pe/10000.0f; 
					if(b < 0.32f) b = 0.3f; 
					if(b > 1.0f) b = 1.0f;
					
					// Float value used to Hue
					h = value.floatValue() - pe;
					RGB rgb = new RGB(h*360.0f, s, b);
					imageData.setPixel(i, j, palette.getPixel(rgb));
				}
			}
		}
		
		if ( imageData != null ){
			Image image = new Image(gc.getDevice(), imageData);
			Rectangle rect = gc.getClipping();
			gc.drawImage(image, 0, 0, matrix.getSizeX(), matrix.getSizeY(), 0, 0, rect.width, rect.height);
			image.dispose();
		}
	}
}
