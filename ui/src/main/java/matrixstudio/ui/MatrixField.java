package matrixstudio.ui;

import matrixstudio.kernel.Simulator;
import matrixstudio.kernel.Simulator.UserInputProvider;
import matrixstudio.model.Matrix;
import matrixstudio.model.MatrixFloat;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.MatrixULong;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.*;

import org.xid.basics.notification.Notification;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.Resources;
import org.xid.basics.ui.field.AbstractField;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

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
		createShell3D();
	}

	private GLCanvas gl_canvas;
	private Shell shell3D;
	private boolean mouseDownLeft3D = false, mouseDownMid3D = false, mouseDownRight3D = false;
	private float angleX3D = 0.0f, angleY3D = 45.0f;
	private float dx3D = 0.0f, dy3D = 0.0f, dz3D = 0.0f;
	private int clickedX3D, clickedY3D;
	private boolean draw3D = true;
	private void createShell3D() {
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
					renderMode = (renderMode + 1) % 4; // 0=points, 1=small cubes, 2=full cubes, 3=cubes+lines
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

		// LWJGL init
		GLCapabilities swtCapabilities = GL.createCapabilities();
		// OpenGL init
        //material colors
        float[] matambient={0.1f,0.1f,0.1f,0f};
        float[] matdiffuse={0.5f,0.5f,0.5f,0};
        float[] matemission={0.1f,0.1f,0.1f,0};

        ByteBuffer temp = ByteBuffer.allocateDirect(16);
        temp.order(ByteOrder.nativeOrder());
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_AMBIENT,   (FloatBuffer)temp.asFloatBuffer().put(matambient).flip());   // 0.2f, 0.2f, 0.2f, 1f
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_DIFFUSE,   (FloatBuffer)temp.asFloatBuffer().put(matdiffuse).flip());  // 0.8f, 0.8f, 0.8f, 1f
        GL11.glMaterialfv(GL11.GL_FRONT, GL11.GL_EMISSION,  (FloatBuffer)temp.asFloatBuffer().put(matemission).flip());   // 0,0,0,1

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glMatrixMode(GL11.GL_PROJECTION);

        // Background color
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        shell3D.setSize(800,800);

		shell3D.open();
		gl_canvas.swapBuffers();
	}

	private void createCanvas(Composite parent) {
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);//new Shell();// Canvas(parent, SWT.DOUBLE_BUFFERED);

		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				//gc.setBackground(resources.getSystemColor(SWT.COLOR_BLACK));
				//gc.fillRectangle(gc.getClipping());
				
				// don't draw matrix if value is null or if x or y is invalid
				if ( matrix == null || matrix.getSizeX() <= 0 || matrix.getSizeY() <= 0 ) return;
				
				MatrixRenderer renderer = renderers.get((Class<? extends Matrix>) matrix.getClass());
				if ( renderer != null ) {
					renderer.render(gc, MatrixField.this, matrix, mouseZ, draw3D, dx3D, dy3D, dz3D, angleX3D, angleY3D, shell3D, gl_canvas,renderMode);
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
				if(matrix.getSizeX() <= 0 || matrix.getSizeY() <= 0 || matrix.getSizeZ() <= 0) return;
				mouseX = (e.x * matrix.getSizeX() )/canvas.getSize().x;
				mouseY = matrix.getSizeY()-(e.y * matrix.getSizeY() )/canvas.getSize().y - 1;
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
                if(mouseZ >= matrix.getSizeZ()) mouseZ = matrix.getSizeZ()-1;
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
