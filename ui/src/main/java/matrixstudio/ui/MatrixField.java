package matrixstudio.ui;

import matrixstudio.kernel.Simulator;
import matrixstudio.kernel.Simulator.UserInputProvider;
import matrixstudio.model.Matrix;
import matrixstudio.model.MatrixFloat;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.MatrixULong;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.xid.basics.notification.Notification;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.Resources;
import org.xid.basics.ui.field.AbstractField;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_POLYGON_OFFSET_LINE;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glPolygonOffset;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class MatrixField extends AbstractField implements RendererContext, UserInputProvider {

	private Map<Class<? extends Matrix>, MatrixRenderer> renderers = new HashMap<Class<? extends Matrix>, MatrixRenderer>();
	private Canvas canvas;

	private Simulator simulator;
	private Matrix matrix;
	
	private int mouseX = -1;
	private int mouseY = -1;
    private int mouseZ = 0;
	private int button = 0;
	private int keyDown = 0;
	private int renderMode = 0;
	public MatrixField(Simulator simulator, String label, int style) {
		super(label, style);
		this.simulator = simulator;
		SimpleMatrixRenderer renderer = new SimpleMatrixRenderer();
		addRenderer(MatrixInteger.class, renderer);
		addRenderer(MatrixFloat.class, renderer);
		addRenderer(MatrixULong.class, renderer);

		// Test 3D
		//renderer.render3D(null, null);
	}
	
	public boolean activate() {
		if ( canvas != null ) return canvas.setFocus();
		return false;
	}
	
	public void createWidget(Composite parent) {
		createLabel(parent);
		createInfo(parent);
		createCanvas(parent);
		createButtonBar(parent);
		if(simulator.Embedded == false) {
			createShell3D();
		}
	}

	private GLCanvas gl_canvas;
	private Shell shell3D;
	private boolean mouseDownLeft3D = false, mouseDownMid3D = false, mouseDownRight3D = false;
	private float angleX3D = 0.0f, angleY3D = 45.0f;
	private float dx3D = 0.0f, dy3D = 0.0f, dz3D = 0.0f;
	private int clickedX3D, clickedY3D;
	private boolean draw3D = true;

	int program;

	////////////////////////////////////////
	Matrix4f viewProjMatrix = new Matrix4f();
	FloatBuffer fb = BufferUtils.createFloatBuffer(16);

	long window;
	int width = 800;
	int height = 800;
	Object lock = new Object();
	boolean destroyed;
	long lastTime;
	int matLocation;
	int colorLocation;
	Quaternionf q;

	void renderCube() {
		glBegin(GL_QUADS);
		glVertex3f(  0.5f, -0.5f, -0.5f );
		glVertex3f( -0.5f, -0.5f, -0.5f );
		glVertex3f( -0.5f,  0.5f, -0.5f );
		glVertex3f(  0.5f,  0.5f, -0.5f );

		glVertex3f(  0.5f, -0.5f,  0.5f );
		glVertex3f(  0.5f,  0.5f,  0.5f );
		glVertex3f( -0.5f,  0.5f,  0.5f );
		glVertex3f( -0.5f, -0.5f,  0.5f );

		glVertex3f(  0.5f, -0.5f, -0.5f );
		glVertex3f(  0.5f,  0.5f, -0.5f );
		glVertex3f(  0.5f,  0.5f,  0.5f );
		glVertex3f(  0.5f, -0.5f,  0.5f );

		glVertex3f( -0.5f, -0.5f,  0.5f );
		glVertex3f( -0.5f,  0.5f,  0.5f );
		glVertex3f( -0.5f,  0.5f, -0.5f );
		glVertex3f( -0.5f, -0.5f, -0.5f );

		glVertex3f(  0.5f,  0.5f,  0.5f );
		glVertex3f(  0.5f,  0.5f, -0.5f );
		glVertex3f( -0.5f,  0.5f, -0.5f );
		glVertex3f( -0.5f,  0.5f,  0.5f );

		glVertex3f(  0.5f, -0.5f, -0.5f );
		glVertex3f(  0.5f, -0.5f,  0.5f );
		glVertex3f( -0.5f, -0.5f,  0.5f );
		glVertex3f( -0.5f, -0.5f, -0.5f );
		glEnd();
	}

	void renderGrid() {
		glBegin(GL_LINES);
		for (float i = -4.0f; i <= 4.0f; i+=0.1f) {
			glVertex3f(-4.0f, 	-0.5f, i);
			glVertex3f( 4.0f, 	-0.5f, i);
			glVertex3f(i, 		-0.5f, -4.0f);
			glVertex3f(i, 		-0.5f,  4.0f);
		}
		glEnd();
	}

	void initOpenGLAndRenderInAnotherThread() {
		// Create a simple shader program
		int program = glCreateProgram();
		int vs = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs,
				"uniform mat4 viewProjMatrix;" +
						"void main(void) {" +
						"  gl_Position = viewProjMatrix * gl_Vertex;" +
						"}");
		glCompileShader(vs);
		glAttachShader(program, vs);
		int fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs,
				"uniform vec3 color;" +
						"void main(void) {" +
						"  gl_FragColor = vec4(color, 1.0);" +
						"}");
		glCompileShader(fs);
		glAttachShader(program, fs);
		glLinkProgram(program);
		glUseProgram(program);

		// Obtain uniform location
		matLocation = glGetUniformLocation(program, "viewProjMatrix");
		colorLocation = glGetUniformLocation(program, "color");
		lastTime = System.nanoTime();

        /* Quaternion to rotate the cube */
		q = new Quaternionf();

	}

	private void renderShader() {
		/*GL** glUseProgram(program);

		long thisTime = System.nanoTime();
		float dt = (thisTime - lastTime) / 1E9f;
		lastTime = thisTime;

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		// Render the grid
		glUniform3f(colorLocation, 0.1f, 0.1f, 0.1f);
		GL11.glColor3f( 0.2f, 0.2f, 0.2f);
		renderGrid();

		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glEnable(GL_POLYGON_OFFSET_LINE);
		glPolygonOffset(-1.f,-1.f);
		glUniform3f(colorLocation, 0.0f, 0.0f, 0.0f);
		renderCube();
		glDisable(GL_POLYGON_OFFSET_LINE);
		*/
		//glUseProgram(0); // No more shader (to uncomment in the future?)

	}

	////////////////////////////////////////
	private void createShell3D() {
		/*GL**
		// Create a basic SWT window
		shell3D = new Shell();
        shell3D.setText("Matrix Studio 3D View");

		shell3D.setLayout(new FillLayout());
		// Disable the close button
		shell3D.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				e.doit = false;
			}
		});
		shell3D.addShellListener(new ShellAdapter() {
			public void shellIconified(ShellEvent e) {
				draw3D = false;
			}
		});
		shell3D.addShellListener(new ShellAdapter() {
			public void shellDeiconified(ShellEvent e) {
				draw3D = true;
				canvas.redraw();
			}
		});
		shell3D.addShellListener(new ShellAdapter() {
			public void shellResized(ShellEvent e) {
				draw3D = true;
				canvas.redraw();
			}
		});
		GLData data = new GLData();
		data.doubleBuffer = true;

		gl_canvas = new GLCanvas(shell3D, SWT.NONE, data);
		gl_canvas.addListener(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				keyDown 	= event.keyCode;
				if(keyDown == SWT.TAB) {
					renderMode = (renderMode + 1) % 6; // [0,2] =points, 3=small cubes, 4=full cubes, 5=cubes+lines
					canvas.redraw();
				}
			}
		});
		gl_canvas.addListener(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				keyDown 	= 0;
			}
		});
		// Mouse down
		gl_canvas.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(event.button == 1) {
					mouseDownLeft3D = true;
				} else if(event.button == 3) {
					mouseDownRight3D = true;
				} else if(event.button == 2) {
					mouseDownMid3D = true;
				}
				clickedX3D = event.x;
				clickedY3D = event.y;
			}
		});
		// Mouse up
		gl_canvas.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				mouseDownLeft3D 	= false;
				mouseDownMid3D 		= false;
				mouseDownRight3D 	= false;
				//clickedX3D = event.x;
				//clickedY3D = event.y;
			}
		});
		// Mouse move
		gl_canvas.addListener(SWT.MouseMove, new Listener() {
			@Override
			public void handleEvent(Event event) {
				//System.err.println(""+keyDown+","+SWT.CTRL);
				if(mouseDownLeft3D == true && keyDown == 0){
					angleY3D += (event.x - clickedX3D) / 5.0f;
					angleX3D += (event.y - clickedY3D) / 5.0f;
					canvas.redraw();
					shell3D.redraw();
				} else if(mouseDownMid3D == true || (mouseDownLeft3D == true && (keyDown & SWT.CTRL) == SWT.CTRL)){
					dz3D += (event.y - clickedY3D) 	/ 1000.0f;
					canvas.redraw();
				} else if(mouseDownRight3D == true || (mouseDownLeft3D == true && (keyDown & SWT.ALT) == SWT.ALT)){
					dx3D += (event.x - clickedX3D) 	/ 2000.0f;
					dy3D += -(event.y - clickedY3D) / 2000.0f;
					canvas.redraw();
				}
				clickedX3D = event.x;
				clickedY3D = event.y;
			}

		});
		gl_canvas.setCurrent();
		*/
		// LWJGL init
		/*GL** GLCapabilities swtCapabilities = GL.createCapabilities();
		if (!swtCapabilities.GL_ARB_shader_objects)
			throw new UnsupportedOperationException("This demo requires the ARB_shader_objects extension");
		if (!swtCapabilities.GL_ARB_vertex_shader)
			throw new UnsupportedOperationException("This demo requires the ARB_vertex_shader extension");
		if (!swtCapabilities.GL_ARB_fragment_shader)
			throw new UnsupportedOperationException("This demo requires the ARB_fragment_shader extension");*/
		// OpenGL init
        //material colors
        float[] matambient={0.1f,0.1f,0.1f,0f};
        float[] matdiffuse={0.5f,0.5f,0.5f,0};
        float[] matemission={0.1f,0.1f,0.1f,0};

        ByteBuffer temp = ByteBuffer.allocateDirect(16);
        temp.order(ByteOrder.nativeOrder());
		//GL** GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT,   (FloatBuffer)temp.asFloatBuffer().put(matambient).flip());   // 0.2f, 0.2f, 0.2f, 1f
        //GL** GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE,   (FloatBuffer)temp.asFloatBuffer().put(matdiffuse).flip());  // 0.8f, 0.8f, 0.8f, 1f
        //GL** GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_EMISSION,  (FloatBuffer)temp.asFloatBuffer().put(matemission).flip());   // 0,0,0,1

        //GL** GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        //GL** GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing

        //GL** GL11.glMatrixMode(GL11.GL_MODELVIEW);
        //GL** GL11.glMatrixMode(GL11.GL_PROJECTION);

        // Background color
		//GL** GL11.glClearColor(0.0f, 0.0f, 0.2f, 0.0f);
		//GL** GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);


		/// SHADERS
		//GL** initOpenGLAndRenderInAnotherThread();




		//GL** shell3D.setSize(800,800);

		//GL** shell3D.open();
///		gl_canvas.swapBuffers();
	}

	private void createCanvas(Composite parent) {
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);//new Shell();// Canvas(parent, SWT.DOUBLE_BUFFERED);

		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				//gc.setBackground(resources.getSystemColor(SWT.COLOR_BLACK));
				//gc.fillRectangle(gc.getClipping());
				if(simulator.Embedded == false) {
					renderShader();
				}
				// don't draw matrix if value is null or if x or y is invalid
				if ( matrix == null || matrix.safeGetSizeXValue() <= 0 || matrix.safeGetSizeYValue() <= 0 ) return;
				MatrixRenderer renderer = renderers.get((Class<? extends Matrix>) matrix.getClass());
				if ( renderer != null ) {
					renderer.render(gc, MatrixField.this, matrix, mouseZ, draw3D, dx3D, dy3D, dz3D, angleX3D, angleY3D, shell3D, gl_canvas,renderMode, program);
				}

				// Draw information texts about current simulation (time, execution state and recording state).
				gc.setForeground(resources.getSystemColor(SWT.COLOR_CYAN));
				if(simulator.getInitialSimulationTime()<0)
				gc.drawString("Step="+simulator.getNbSteps() + "   Time=0", 0,0, true);
				else gc.drawString("Step="+simulator.getNbSteps() + "   Time="+(System.currentTimeMillis()-simulator.getInitialSimulationTime())/1000, 0,0, true);
				if(simulator.isStarted() == false) {
					gc.drawString("STOP", 0, 16, true);
				} else {
					if(simulator.isRunning() == false) {
						gc.drawString("PAUSE", 0, 16, true);
					} else {
						gc.drawString("EXEC", 0,16, true);
					}
				}
				if(simulator.recordingMPEG == true) {
					gc.setForeground(resources.getSystemColor(SWT.COLOR_RED));
					gc.drawString("REC", 0,32, true);					
				}

			}
		});

		canvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if(matrix == null || canvas == null) return;

                int xSize = matrix.safeGetSizeXValue();
                int ySize = matrix.safeGetSizeYValue();
                int zSize = matrix.safeGetSizeZValue();
                if(xSize <= 0 || ySize <= 0 || zSize <= 0) return;

				mouseX = (e.x * xSize )/canvas.getSize().x;
				mouseY = matrix.safeGetSizeYValue()-(e.y * ySize )/canvas.getSize().y - 1;
				String name = matrix.getName();
				
				final StringBuilder builder = new StringBuilder();
				builder.append(name);
				builder.append("[");
				builder.append(mouseX);
				builder.append(",");
				builder.append(mouseY);
                builder.append(",");
                builder.append(mouseZ);
                builder.append("] = ");
				final Number valueAt = matrix.getValueAt(mouseX, mouseY,  mouseZ);
				builder.append(valueAt);
				if ( valueAt instanceof Integer ) {
					builder.append("\n0x");
					builder.append(Integer.toHexString(valueAt.intValue()).toUpperCase());
				}
				
				tooltip = builder.toString();

            }
		});
		
		canvas.addMouseListener(new MouseListener() {
			
			public void mouseUp(MouseEvent e) {
				button = 0;
			}
			
			public void mouseDown(MouseEvent e) {
				button = e.button;
			}
			
			public void mouseDoubleClick(MouseEvent e) {
				// nothing
			}
		});

        canvas.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseScrolled(MouseEvent mouseEvent) {
                if(matrix == null || canvas == null) return;
                int dz = mouseEvent.count;
                System.err.println(">" + dz);
                mouseZ += dz;
                if(mouseZ < 0) mouseZ = 0;
                int z = matrix.safeGetSizeZValue();
                if(mouseZ >= z) mouseZ = z-1;
                canvas.redraw();
            }
        });

		canvas.addListener(SWT.MouseExit, new Listener() {
			
			public void handleEvent(Event event) {
				button = 0;
				mouseX = -1;
				mouseY = -1;
			}
		});

		attachFieldToWidget(canvas);
		fireWidgetCreation(canvas);
		
		// sets layout
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, grabExcessVerticalSpace());
		data.minimumWidth = 80;
		data.minimumHeight = 80;
		data.horizontalSpan = fieldHorizontalSpan();
		data.verticalSpan = 1;
		canvas.setLayoutData(data);
	}
	
	public boolean grabExcessVerticalSpace() {
		return true;
	}
	
	/** @return the field's value */
	public Matrix getValue() {
		return matrix;
	}
	
	public void setValue(Matrix value) {
		setValue(value, Notification.TYPE_API);
	}

	protected void setValue(Matrix value, int type) {
		Matrix oldValue = this.matrix;
		this.matrix = value;
		if ( canvas != null && type != Notification.TYPE_UI ) {
			canvas.redraw();
		}
		notificationSupport.fireValueNotification(type, BasicsUI.NOTIFICATION_VALUE, value, oldValue);
	}
	
	public void refresh() {
		if ( canvas != null && canvas.isDisposed() == false && canvas.getDisplay() != null) canvas.redraw();
	}
	
	public void addRenderer(Class<? extends Matrix> clazz, MatrixRenderer renderer) {
		renderers.put(clazz, renderer);
	}
	
	@Override
	public Resources getResources() {
		return super.getResources();
	}
	
	public int getButton() {
		return button;
	}
	
	public int getMouseX() {
		return mouseX;
	}
	
	public int getMouseY() {
		return mouseY;
	}
}	
