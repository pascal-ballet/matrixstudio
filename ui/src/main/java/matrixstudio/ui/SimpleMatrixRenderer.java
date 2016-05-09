package matrixstudio.ui;

import matrixstudio.model.Matrix;
import matrixstudio.model.MatrixFloat;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.MatrixULong;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;

import org.eclipse.swt.widgets.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.*;

import java.util.Random;
//import org.lwjgl.opengl.GLContext;
//import org.eclipse.swt.opengl.GLContext;

//import org.lwjgl.opengl.GLContext;
//import org.lwjgl.LWJGLUtil;

public class SimpleMatrixRenderer implements MatrixRenderer {


	public void render(GC gc, RendererContext context, Matrix matrix, boolean draw3D, float dx3D, float dy3D, float dz3D, float angleX3D, float angleY3D, Shell shell3D, GLCanvas gl_canvas, int renderMode) {
		if(draw3D == true) {
			render3D(matrix, dx3D, dy3D, dz3D, angleX3D, angleY3D, shell3D, gl_canvas, renderMode);
		}
		ImageData imageData = null;
		
		if ( matrix instanceof MatrixInteger ) {
			MatrixInteger matrixInteger = (MatrixInteger) matrix;
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			imageData = new ImageData(matrix.getSizeX(), matrix.getSizeY(), 32, palette);
			if(matrix.getSizeZ() <= 1) {
				imageData.setPixels(0, 0, matrixInteger.getMatrix().length, matrixInteger.getMatrix(), 0);
			} else {
				// 2D Render
				int k=0;
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
						imageData.setPixel(i, matrix.getSizeY()-j-1, palette.getPixel(rgb));
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
	//static Random rnd = new Random();

	private void render3D(Matrix matrix, float dx3D, float dy3D, float dz3D, float angleX3D, float angleY3D, Shell shell3D, GLCanvas gl_canvas, int renderMode) {
		// Update 3D view
		if(gl_canvas.isDisposed() == false && matrix instanceof MatrixInteger) {
			//float angle = 45.0f;//360.0f*( (simulator.getNbSteps()%100000)/100000.0f);
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

			gl_canvas.setCurrent();
			// Full window rendering
			GL11.glViewport(0, 0, shell3D.getSize().x, shell3D.getSize().y);
			// Surface material mirror the color.
			GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK,GL11.GL_AMBIENT_AND_DIFFUSE);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			// Init position
			GL11.glLoadIdentity();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			// Frustum (replace GLUT which is unavailable in LWJGL 3 (WTF!!!))
			perspectiveGL(95.0f, 1.0f*shell3D.getSize().x/shell3D.getSize().y,0.01f,1500.0f);
			// Go forward in z to put (then see) all the cubes
			GL11.glTranslatef(dx3D,dy3D,-1.3f+dz3D);
			// Rotate according to Y
			GL11.glRotatef(angleY3D,0.0f,1.0f,0.0f);
			GL11.glRotatef(angleX3D,1.0f,0.0f,0.0f);
			// Background color
			GL11.glColor3f(1.0f,0.5f,0.8f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			// Draw the cubes (can be very very very long...) => optimization to find with OpenGL / OpenCL exchange
			int SX = matrix.getSizeX(), SY = matrix.getSizeY(), SZ = matrix.getSizeZ();
			int SX2 = SX/2, SY2 = SY/2, SZ2 = SZ/2;
			float SXexp1 = (1.0f/SX);
			MatrixInteger matrixInteger = (MatrixInteger) matrix;
			if(matrix.getSizeZ() < 1) {
				//imageData.setPixels(0, 0, matrixInteger.getMatrix().length, matrixInteger.getMatrix(), 0);
			} else {
				// 3D Render (very simple: fill from far z to close z
				for (int i=matrix.getSizeX()-1; i>=0; i--) {
					for ( int j=matrix.getSizeY()-1; j>=0; j--) {
						for (int k=matrix.getSizeZ()-1; k>=0; k--) {
							Integer value = matrixInteger.getMatrix()[k*SX*SY + j*SX + i];
							int r,g,b,a;
							r = (value & 255);
							g = (value >> 8) & 255;
							b = (value >> 16) & 255;
							a = 255 - (value >> 24) & 255;
							if(renderMode == 1)
								drawCube3D(i-SX2, j-SY2, k-SZ2, r, g, b, a, SXexp1);
							if(renderMode == 0)
								drawPoint(i-SX2, j-SY2, k-SZ2, r, g, b, a, SXexp1);
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

	private void drawPoint(int xx, int yy, int zz, int r, int g, int b, int a , float SXexp1) {
		float x = SXexp1*xx, y = SXexp1*yy, z = SXexp1*zz;
		//float size = SXexp1/2.0f;
		GL11.glColor4ub((byte)r,(byte)g,(byte)b,(byte)a);
		GL11.glBegin(GL11.GL_POINTS);
			GL11.glVertex3f(x, y, z);
		GL11.glEnd();
	}
	private void drawCube3D(int xx, int yy, int zz, int r, int g, int b, int a, float SXexp1) {
		float x = SXexp1*xx, y = SXexp1*yy, z = SXexp1*zz;
		float size = SXexp1/2.0f;
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4ub((byte)r,(byte)g,(byte)b,(byte)a);
		GL11.glVertex3f(x+size, y+size,z-size);
		GL11.glVertex3f(x-size, y+size,z-size);
		GL11.glVertex3f(x-size, y+size,z+size);
		GL11.glVertex3f(x+size, y+size,z+size);
		//GL11.glColor4i(r,g,b,a);
		GL11.glVertex3f(x+size,y-size,z+size);
		GL11.glVertex3f(x-size,y-size,z+size);
		GL11.glVertex3f(x-size,y-size,z-size);
		GL11.glVertex3f(x+size,y-size,z-size);
		//GL11.glColor4i(r,g,b,a);
		GL11.glVertex3f(x+size,y+size,z+size);
		GL11.glVertex3f(x-size,y+size,z+size);
		GL11.glVertex3f(x-size,y-size,z+size);
		GL11.glVertex3f(x+size,y-size,z+size);
		//GL11.glColor4i(r,g,b,a);
		GL11.glVertex3f(x+size,y-size,z-size);
		GL11.glVertex3f(x-size,y-size,z-size);
		GL11.glVertex3f(x-size,y+size,z-size);
		GL11.glVertex3f(x+size,y+size,z-size);
		//GL11.glColor4i(r,g,b,a);
		GL11.glVertex3f(x-size,y+size,z+size);
		GL11.glVertex3f(x-size,y+size,z-size);
		GL11.glVertex3f(x-size,y-size,z-size);
		GL11.glVertex3f(x-size,y-size,z+size);
		//GL11.glColor4i(r,g,b,a);
		GL11.glVertex3f(x+size,y+size,z-size);
		GL11.glVertex3f(x+size,y+size,z+size);
		GL11.glVertex3f(x+size,y-size,z+size);
		GL11.glVertex3f(x+size,y-size,z-size);
		GL11.glEnd();
	}

}
