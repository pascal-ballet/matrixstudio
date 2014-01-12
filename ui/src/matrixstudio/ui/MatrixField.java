package matrixstudio.ui;

import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.xid.basics.notification.Notification;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.Resources;
import org.xid.basics.ui.field.AbstractField;


public class MatrixField extends AbstractField implements RendererContext, UserInputProvider {

	private Map<Class<? extends Matrix>, MatrixRenderer> renderers = new HashMap<Class<? extends Matrix>, MatrixRenderer>();
	private Canvas canvas;
	
	private Simulator simulator;
	private Matrix matrix;
	
	private int mouseX = -1;
	private int mouseY = -1;
	private int button = 0;
	
	public MatrixField(Simulator simulator, String label, int style) {
		super(label, style);
		this.simulator = simulator;
		SimpleMatrixRenderer renderer = new SimpleMatrixRenderer();
		addRenderer(MatrixInteger.class, renderer);
		addRenderer(MatrixFloat.class, renderer);
		addRenderer(MatrixULong.class, renderer);
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
	}

	private void createCanvas(Composite parent) {
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				//gc.setBackground(resources.getSystemColor(SWT.COLOR_BLACK));
				//gc.fillRectangle(gc.getClipping());
				
				// don't draw matrix if value is null or if x or y is invalid
				if ( matrix == null || matrix.getSizeX() <= 0 || matrix.getSizeY() <= 0 ) return;
				
				MatrixRenderer renderer = renderers.get((Class<? extends Matrix>) matrix.getClass());
				if ( renderer != null ) {
					renderer.render(gc, MatrixField.this, matrix);
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
				mouseY = (e.y * matrix.getSizeY() )/canvas.getSize().y;
				String name = matrix.getName();
				
				final StringBuilder builder = new StringBuilder();
				builder.append(name);
				builder.append("[");
				builder.append(mouseX);
				builder.append(",");
				builder.append(mouseY);
				builder.append("] = ");
				final Number valueAt = matrix.getValueAt(mouseX, mouseY, 0);
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
