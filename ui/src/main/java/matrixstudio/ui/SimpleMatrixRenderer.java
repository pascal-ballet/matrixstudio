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

	public void render(GC gc, RendererContext context, Matrix matrix, int mouseZ) {
		ImageData imageData = null;

        int k = mouseZ; //(int)Math.floor(matrix.getSizeX()/2);
        int sizeX = matrix.safeGetSizeXValue();
        int sizeY = matrix.safeGetSizeYValue();
        if ( matrix instanceof MatrixInteger ) {
			MatrixInteger matrixInteger = (MatrixInteger) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(sizeX, sizeY, 32, palette);
			if(matrixInteger.isARGB() == false) {
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						Integer value = matrixInteger.getMatrix()[k * sizeX * sizeY + j * sizeX + i];
						int r, g, b;
						g = (value >> 8) & 255;
						r = (value & 255);
						b = (value >> 16) & 255;
						RGB rgb = new RGB(r, g, b);
						imageData.setPixel(i, sizeY - j - 1, palette.getPixel(rgb));
					}
				}
			} else {
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						Integer value = matrixInteger.getMatrix()[k * sizeX * sizeY + j * sizeX + i];
						int r, g, b;
						g = (value >> 8) & 255;
						b = (value & 255);
						r = (value >> 16) & 255;
						RGB rgb = new RGB(r, g, b);
						imageData.setPixel(i, sizeY - j - 1, palette.getPixel(rgb));
					}
				}
			}
		}
		
		if ( matrix instanceof MatrixULong ) {
			MatrixULong matrixULong = (MatrixULong) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(sizeX, sizeY, 32, palette);
            for (int i = 0; i< sizeX; i++) {
				for (int j = 0; j< sizeY; j++) {
					long value = matrixULong.getMatrix()[k* sizeX * sizeY + j* sizeX + i];
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
					imageData.setPixel(i, sizeY -j-1, palette.getPixel(rgb));
				}
			}
		}
		
		if ( matrix instanceof MatrixFloat ) {
			MatrixFloat matrixFloat = (MatrixFloat) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(sizeX, sizeY, 32, palette);
			for (int i = 0; i< sizeX; i++) {
				for (int j = 0; j< sizeY; j++) {
					Float value = matrixFloat.getMatrix()[k* sizeX * sizeY + j* sizeX + i];
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
					if(value == 0.0f) { h=0.0f; s=0.0f; b=0.0f;}
					RGB rgb = new RGB(h*360.0f, s, b);
					imageData.setPixel(i, sizeY -j-1, palette.getPixel(rgb));
				}
			}
		}

		if ( imageData != null ){
			Image image = new Image(gc.getDevice(), imageData);
			Rectangle rect = gc.getClipping();
			gc.drawImage(image, 0, 0, sizeX, sizeY, 0, 0, rect.width, rect.height);
			image.dispose();
		}
	}
}
