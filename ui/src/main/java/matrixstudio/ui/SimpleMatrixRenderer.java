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
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Shell;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class SimpleMatrixRenderer implements MatrixRenderer {

	public static boolean _rendering3d = false;
	float dx3D,  dy3D,  dz3D;
	public void render(GC gc, RendererContext context, Matrix matrix, int mouseZ, boolean draw3D, float dx3D, float dy3D, float dz3D, float angleX3D, float angleY3D, Shell shell3D, GLCanvas gl_canvas, int renderMode, int program) {
		this.dx3D = dx3D;
		this.dy3D = dy3D;
		this.dz3D = dz3D;

		if(draw3D == true && _rendering3d == false) {
			_rendering3d = true;
			render3D(matrix, dx3D, dy3D, dz3D, angleX3D, angleY3D, shell3D, gl_canvas, renderMode, program);
			_rendering3d = false;
		}
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

    // 3D rendering
	private void render3D(Matrix matrix, float dx3D, float dy3D, float dz3D, float angleX3D, float angleY3D, Shell shell3D, GLCanvas gl_canvas, int renderMode, int program) {
		// Update 3D view
		if(gl_canvas.isDisposed() == false) {
			gl_canvas.setCurrent();
			// Full window rendering
			GL11.glViewport(0, 0, shell3D.getSize().x, shell3D.getSize().y);

			// Init position
			GL11.glLoadIdentity();

			// Frustum (replace GLUT which is unavailable in LWJGL 3 (WTF!!!))
			perspectiveGL(95.0f, 1.0f*shell3D.getSize().x/shell3D.getSize().y,0.01f,1500.0f);
			// Go forward in z to put (then see) all the cubes
			GL11.glTranslatef(dx3D,dy3D,-1.3f+dz3D);
			// Rotate according to Y
			GL11.glRotatef(angleY3D,0.0f,1.0f,0.0f);
			GL11.glRotatef(angleX3D,1.0f,0.0f,0.0f);

			// Draw the cubes (can be very very very long...) => optimization to find with OpenGL / OpenCL exchange
			int SX = matrix.safeGetSizeXValue(), SY = matrix.safeGetSizeYValue(), SZ = matrix.safeGetSizeZValue();
			int SX2 = SX/2, SY2 = SY/2, SZ2 = SZ/2;
			float SXexp1 = (1.0f/SX);

			if(matrix instanceof MatrixInteger) {
                MatrixInteger matrixInteger = (MatrixInteger) matrix;
                for (int i = SX - 1; i >= 0; i--) {
                    for (int j = SY - 1; j >= 0; j--) {
                        for (int k = SZ - 1; k >= 0; k--) {
                            Integer value = matrixInteger.getMatrix()[k*SX*SY + j*SX + i];
                            int r, g, b, a;
                            r = (value & 255);
                            g = (value >> 8) & 255;
                            b = (value >> 16) & 255;
                            a = (value >> 24) & 255;
                            if(r == 0 && g == 0 && b == 0) a=0;
                            if( (r > 0 || g > 0 || b > 0) && a == 0) a=50;

                            if (a > 0) {
                                if (renderMode == 0)
                                    drawPoint(i - SX2, j - SY2, k - SZ2, r, g, b, a, SXexp1);
								if (renderMode == 1)
									drawPoint(i - SX2, j - SY2, k - SZ2, r, g, b, a/10, SXexp1);
								if (renderMode == 2)
									drawPoint(i - SX2, j - SY2, k - SZ2, r, g, b, a/100, SXexp1);
								if (renderMode == 3 && a == 255)
									drawWiredCube3D(i - SX2, j - SY2, k - SZ2, r, g, b, 255, SXexp1, 0.95f);
								if (renderMode == 4 && a == 255)
									drawCube3D(i - SX2, j - SY2, k - SZ2, r, g, b, a, SXexp1, 0.95f);
								if (renderMode == 5 && a == 255)
                                    drawCube3D(i - SX2, j - SY2, k - SZ2, r, g, b, a, SXexp1, 0.4f);
                            }
                        }
                    }
                }
            }
			if(matrix instanceof MatrixULong) {
				MatrixULong matrixULong = (MatrixULong) matrix;
				for (int i = SX - 1; i >= 0; i--) {
					for (int j = SY - 1; j >= 0; j--) {
						for (int k = SZ - 1; k >= 0; k--) {
							long value = matrixULong.getMatrix()[k*SX*SY + j*SX + i];
							int r, g, b, a;
							r = (int)(value & 255);
							g = (int)(value >> 8) & 255;
							b = (int)(value >> 16) & 255;
							a = (int)(value >> 24) & 255;
							if(r == 0 && g == 0 && b == 0) a=0;
							if( (r > 0 || g > 0 || b > 0) && a == 0) a=50;

							if (a > 0) {
								if (renderMode == 0)
									drawPoint(i - SX2, j - SY2, k - SZ2, r, g, b, a, SXexp1);
								if (renderMode == 1)
									drawPoint(i - SX2, j - SY2, k - SZ2, r, g, b, a/10, SXexp1);
								if (renderMode == 2)
									drawPoint(i - SX2, j - SY2, k - SZ2, r, g, b, a/100, SXexp1);
								if (renderMode == 3 && a == 255)
									drawWiredCube3D(i - SX2, j - SY2, k - SZ2, r, g, b, 255, SXexp1, 0.95f);
								if (renderMode == 4 && a == 255)
									drawCube3D(i - SX2, j - SY2, k - SZ2, r, g, b, a, SXexp1, 0.95f);
								if (renderMode == 5 && a == 255)
									drawCube3D(i - SX2, j - SY2, k - SZ2, r, g, b, a, SXexp1, 0.4f);
							}
						}
					}
				}
			}
            if(matrix instanceof MatrixFloat) {
                MatrixFloat matrixFloat = (MatrixFloat) matrix;
                for (int i=SX-1; i>=0; i--) {
                    for ( int j=SY-1; j>=0; j--) {
                        for (int k=SZ-1; k>=0; k--) {
                            Float value = matrixFloat.getMatrix()[k*SX*SY + j*SX + i];
                            float h,s,bf;
                            h = 0.0f; // Hue
                            s = 1.0f; // Saturation
                            bf = 0.0f; // Brightness (min = 100)

                            // Int value used to Brightness
                            int pe = (int) (Math.floor(value.floatValue()));
                            bf = 1.0f;//pe/10000.0f;

							h = value.floatValue() - pe;
							RGB rgb = new RGB(h*360.0f, s, bf);
                            // Float value used to Hue
                            int r, g, b, a;
							r = rgb.red;
							g = rgb.green;
							b = rgb.blue;
                            if(value > 0) {
	                            drawPoint(i - SX2, j - SY2, k - SZ2, r, g, b, 5, SXexp1);
                            }
                        }
                    }
                }
            }

			// Draw the surrounding mesh cube
			GL11.glLineWidth(1.0f);
			GL11.glBegin(GL11.GL_LINES);
                GL11.glColor3f( 0.51f, 0.51f, 0.51f);
                GL11.glVertex3f(-0.51f, -0.51f, -0.51f); // BACK
                GL11.glVertex3f(0.51f, -0.51f, -0.51f);
                GL11.glVertex3f(-0.51f, -0.51f, -0.51f);
                GL11.glVertex3f(-0.51f, 0.51f, -0.51f);
                GL11.glVertex3f(-0.51f, 0.51f, -0.51f);
                GL11.glVertex3f(0.51f, 0.51f, -0.51f);
                GL11.glVertex3f(0.51f, 0.51f, -0.51f);
                GL11.glVertex3f(0.51f, -0.51f, -0.51f);
                GL11.glVertex3f(-0.51f, -0.51f, 0.51f); // FRONT
                GL11.glVertex3f(0.51f, -0.51f, 0.51f);
                GL11.glVertex3f(-0.51f, -0.51f, 0.51f);
                GL11.glVertex3f(-0.51f, 0.51f, 0.51f);
                GL11.glVertex3f(-0.51f, 0.51f, 0.51f);
                GL11.glVertex3f(0.51f, 0.51f, 0.51f);
                GL11.glVertex3f(0.51f, 0.51f, 0.51f);
                GL11.glVertex3f(0.51f, -0.51f, 0.51f);
			GL11.glEnd();
			// Display the drawing to the screen
			gl_canvas.swapBuffers();
		}

	}
	// Replaces gluPerspective. Sets the frustum to perspective mode.
	// fov     - Field of vision in degrees in the y direction
	// aspect   - Aspect ratio of the viewport
	// zNear    - The near clipping distance
	// zFar     - The far clipping distance
	private static void perspectiveGL(float fov, float aspect, float zNear, float zFar) {
		float fH = (float) Math.tan(fov / 360 * Math.PI) * zNear;
		float fW = fH * aspect;
		GL11.glFrustum( -fW, fW, -fH, fH, zNear, zFar );
	}
	Random rnd = new Random();
	private void drawPoint(int xx, int yy, int zz, int r, int g, int b, int a , float SXexp1) {
		float x = SXexp1*xx, y = SXexp1*yy, z = SXexp1*zz;
		float size = SXexp1;///2.0f;
		float xxx,yyy,zzz;

		int nb = a/4; //1000 - (px*px + py*py + pz*pz);

		GL11.glColor3f(r/255.0f,g/255.0f,b/255.0f);
		GL11.glBegin(GL11.GL_POINTS);
		for(int i=nb; i>=0; i--) {
			xxx = x + size*rnd.nextFloat() - size/2;
			yyy = y + size*rnd.nextFloat() - size/2;
			zzz = z + size*rnd.nextFloat() - size/2;
			GL11.glVertex3f(xxx, yyy, zzz);
		}
		GL11.glEnd();

		/*if(a == 255) {
			// Englobing lines
			size = SXexp1/2.0f;
			GL11.glBegin(GL11.GL_LINE_STRIP);

			// Upper side
			GL11.glVertex3f(x + size, y + size, z - size); // 1
			GL11.glVertex3f(x - size, y + size, z - size); // 2
			GL11.glVertex3f(x - size, y + size, z + size); // 3
			GL11.glVertex3f(x + size, y + size, z + size); // 4
			GL11.glVertex3f(x + size, y + size, z - size); // 5
			// Right side
			GL11.glVertex3f(x + size, y + size, z + size); // 6
			GL11.glVertex3f(x + size, y - size, z + size); // 7
			GL11.glVertex3f(x + size, y - size, z - size); // 8
			GL11.glVertex3f(x + size, y + size, z - size); // 9
			// Back side
			GL11.glVertex3f(x + size, y - size, z - size); // 10
			GL11.glVertex3f(x - size, y - size, z - size); // 11
			GL11.glVertex3f(x - size, y + size, z - size); // 12
			GL11.glVertex3f(x + size, y + size, z - size); // 13
			// Left side
			GL11.glVertex3f(x - size, y + size, z - size); // 14
			GL11.glVertex3f(x - size, y - size, z - size); // 15
			GL11.glVertex3f(x - size, y - size, z + size); // 16
			GL11.glVertex3f(x - size, y + size, z + size); // 17
			//
			GL11.glVertex3f(x - size, y - size, z + size); // 18
			GL11.glVertex3f(x + size, y - size, z + size); // 19
			GL11.glEnd();
		}*/


	}
	private void drawCube3D(int xx, int yy, int zz, int r, int g, int b, int a, float SXexp1, float s) {
		float x = SXexp1*xx, y = SXexp1*yy, z = SXexp1*zz;
		float size = s*SXexp1/2.0f;
        GL11.glColor4ub((byte)r,(byte)g,(byte)b,(byte)a);

        // top
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x-size, y-size, z+size);
        GL11.glVertex3f(x+size, y-size, z+size);
        GL11.glVertex3f(x+size, y+size, z+size);
        GL11.glVertex3f(x-size, y+size, z+size);
        GL11.glEnd();
        //left
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x-size, y-size, z-size);
        GL11.glVertex3f(x-size, y-size, z+size);
        GL11.glVertex3f(x-size, y+size, z+size);
        GL11.glVertex3f(x-size, y+size, z-size);
        GL11.glEnd();
        //right
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x+size, y-size, z-size);
        GL11.glVertex3f(x+size, y+size, z-size);
        GL11.glVertex3f(x+size, y+size, z+size);
        GL11.glVertex3f(x+size, y-size, z+size);
        GL11.glEnd();
        //front
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x-size, y-size, z-size);
        GL11.glVertex3f(x+size, y-size, z-size);
        GL11.glVertex3f(x+size, y-size, z+size);
        GL11.glVertex3f(x-size, y-size, z+size);
        GL11.glEnd();
        //back
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x+size, y+size, z-size);
        GL11.glVertex3f(x-size, y+size, z-size);
        GL11.glVertex3f(x-size, y+size, z+size);
        GL11.glVertex3f(x+size, y+size, z+size);
        GL11.glEnd();
        //bottom
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(x+size, y-size, z-size);
        GL11.glVertex3f(x-size, y-size, z-size);
        GL11.glVertex3f(x-size, y+size, z-size);
        GL11.glVertex3f(x+size, y+size, z-size);
        GL11.glEnd();
	}
	private void drawWiredCube3D(int xx, int yy, int zz, int r, int g, int b, int a, float SXexp1, float s) {
		float x = SXexp1*xx, y = SXexp1*yy, z = SXexp1*zz;
		float size = s*SXexp1/2.0f;
		GL11.glColor4ub((byte)r,(byte)g,(byte)b,(byte)a);
            // top
        GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(x-size, y-size, z+size);
            GL11.glVertex3f(x+size, y-size, z+size);
            GL11.glVertex3f(x+size, y+size, z+size);
            GL11.glVertex3f(x-size, y+size, z+size);
        GL11.glEnd();
            //left
        GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(x-size, y-size, z-size);
            GL11.glVertex3f(x-size, y-size, z+size);
            GL11.glVertex3f(x-size, y+size, z+size);
            GL11.glVertex3f(x-size, y+size, z-size);
        GL11.glEnd();
            //right
        GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(x+size, y-size, z-size);
            GL11.glVertex3f(x+size, y+size, z-size);
            GL11.glVertex3f(x+size, y+size, z+size);
            GL11.glVertex3f(x+size, y-size, z+size);
        GL11.glEnd();
            //front
        GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(x-size, y-size, z-size);
            GL11.glVertex3f(x+size, y-size, z-size);
            GL11.glVertex3f(x+size, y-size, z+size);
            GL11.glVertex3f(x-size, y-size, z+size);
        GL11.glEnd();
            //back
        GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(x+size, y+size, z-size);
            GL11.glVertex3f(x-size, y+size, z-size);
            GL11.glVertex3f(x-size, y+size, z+size);
            GL11.glVertex3f(x+size, y+size, z+size);
        GL11.glEnd();
            //bottom
        GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3f(x+size, y-size, z-size);
            GL11.glVertex3f(x-size, y-size, z-size);
            GL11.glVertex3f(x-size, y+size, z-size);
            GL11.glVertex3f(x+size, y+size, z-size);
    	GL11.glEnd();

        // Englobing lines
		size = SXexp1/2.0f;
		GL11.glBegin(GL11.GL_LINE_STRIP);
			GL11.glColor4ub((byte)60,(byte)60,(byte)60,(byte)a);
			// Upper side
			GL11.glVertex3f(x+size, y+size,z-size); // 1
			GL11.glVertex3f(x-size, y+size,z-size); // 2
			GL11.glVertex3f(x-size, y+size,z+size); // 3
			GL11.glVertex3f(x+size, y+size,z+size); // 4
			GL11.glVertex3f(x+size, y+size,z-size); // 5
			// Right side
			GL11.glVertex3f(x+size,y+size,z+size); // 6
			GL11.glVertex3f(x+size,y-size,z+size); // 7
			GL11.glVertex3f(x+size,y-size,z-size); // 8
			GL11.glVertex3f(x+size,y+size,z-size); // 9
			// Back side
			GL11.glVertex3f(x+size,y-size,z-size); // 10
			GL11.glVertex3f(x-size,y-size,z-size); // 11
			GL11.glVertex3f(x-size,y+size,z-size); // 12
			GL11.glVertex3f(x+size,y+size,z-size); // 13
			// Left side
			GL11.glVertex3f(x-size,y+size,z-size); // 14
			GL11.glVertex3f(x-size,y-size,z-size); // 15
			GL11.glVertex3f(x-size,y-size,z+size); // 16
			GL11.glVertex3f(x-size,y+size,z+size); // 17
			//
			GL11.glVertex3f(x-size,y-size,z+size); // 18
			GL11.glVertex3f(x+size,y-size,z+size); // 19
		GL11.glEnd();
	}

}
