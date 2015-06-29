package matrixstudio.ui;

import matrixstudio.kernel.MjpegEncoder;
import matrixstudio.kernel.Simulator;
import matrixstudio.model.Matrix;
import matrixstudio.model.MatrixFloat;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.MatrixULong;
import matrixstudio.model.Model;
import matrixstudio.ui.controller.MatrixController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.FileDialog;
import org.xid.basics.notification.Notification;
import org.xid.basics.notification.NotificationListener;
import org.xid.basics.progress.ActionMonitor;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.action.Action;
import org.xid.basics.ui.controller.Controller;
import org.xid.basics.ui.field.BorderField;
import org.xid.basics.ui.field.CompositeField;
import org.xid.basics.ui.field.ListField;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;

public class MatrixTabController extends Controller<Model>{

	private final StudioContext studioContext;

	private final MSResources resources;
	
	private final Simulator simulator;

	private BorderField matricesComposite;
	private ListField<Matrix> matricesListField;
	private MatrixController matrixController;

	private Runnable matrixesRefresh = new Runnable() {
		public void run() {
			if ( simulator.isStarted() == true ) {
				if ( simulator.isRunning() ) {
					matrixController.refreshFields();
					if(recordingMPEG == true) recordMPEG();
				}
				studioContext.asynchronousRun(60, this);
			}
		}
	};
	
	private ActionMonitor simulationMonitor = new ActionMonitor() {
		public void setTaskName(String name) {}
		public void begin(int remaining) {}
		public void worked(int workIncrement, int remaining) {}
		
		public void done() {
            studioContext.asynchronousRun(0, matrixesFinalRefresh);
		}

		public void canceled() {
            studioContext.asynchronousRun(0, matrixesFinalRefresh);
        }
	};
	
	private Runnable matrixesFinalRefresh = new Runnable() {
		public void run() {
			matrixController.refreshFields();
		}
	};
	
	public MatrixTabController(StudioContext studioContext) {
		this.studioContext = studioContext;
		this.simulator = studioContext.getSimulator();
		this.resources = studioContext.getResources();
	}
	
	@Override
	public CompositeField createFields() {
		matricesListField = new ListField<Matrix>(null, BasicsUI.NO_INFO ) {
			@Override
			public String getText(Matrix element) {
				return element.getName() == null ? "" : element.getName();
			}
		};
		matricesListField.setTooltip("List of model matrices. The selected one is shown on the right.");
		matricesListField.addAction(new Action.Container("+", 
				new Action.Stub("Float", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {
					public int run(ActionMonitor monitor) {
						Matrix matrix = new MatrixFloat();
						matrix.setName(NameUtils.availableName("Matrix1", getSubject().getMatrixList()));
						matrix.initBlank();
						getSubject().addMatrixAndOpposite(matrix);
						matricesListField.setSingleSelection(matrix);
						return Action.STATUS_OK;
					}
				},
				new Action.Stub("Integer", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {
					public int run(ActionMonitor monitor) {
						Matrix matrix = new MatrixInteger();
						matrix.setName(NameUtils.availableName("Matrix1", getSubject().getMatrixList()));
						matrix.initBlank();
						getSubject().addMatrixAndOpposite(matrix);
						matricesListField.setSingleSelection(matrix);
						return Action.STATUS_OK;
					}
				},
				new Action.Stub("ULong", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {
					public int run(ActionMonitor monitor) {
						Matrix matrix = new MatrixULong();
						matrix.setName(NameUtils.availableName("Matrix1", getSubject().getMatrixList()));
						matrix.initBlank();
						getSubject().addMatrixAndOpposite(matrix);
						matricesListField.setSingleSelection(matrix);
						return Action.STATUS_OK;
					}
				}
		));
		matricesListField.addAction(new Action.Stub("-", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {
			
			@Override
			public String getTooltip() {
				return "Removes matrix '" + matricesListField.getSingleSelection().getName() + "'.";
			}
			
			@Override
			public int getVisibility() {
				return matricesListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				getSubject().removeMatrixAndOpposite(matricesListField.getSingleSelection());
				return Action.STATUS_OK;
			}
		});
		matricesListField.addAction(new Action.Stub(null, resources.getImage("bouton_up.png"), Action.STYLE_BUTTON) {
			
			@Override
			public String getTooltip() {
				return "Move matrix up.";
			}
			
			@Override
			public int getVisibility() {
				return matricesListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				int selIndex = matricesListField.getValue().indexOf(matricesListField.getSingleSelection());
				if(selIndex > 0) {
					getSubject().swapMatrices(selIndex, selIndex-1);
					return Action.STATUS_OK;
				} else {
					return Action.STATUS_CANCEL;
				}
			}
		});
		
		matricesListField.addAction(new Action.Stub(null, resources.getImage("bouton_down.png"), Action.STYLE_BUTTON) {
			
			@Override
			public String getTooltip() {
				return "Move matrix down.";
			}
			
			@Override
			public int getVisibility() {
				return matricesListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				int selIndex = matricesListField.getValue().indexOf(matricesListField.getSingleSelection());
				if(selIndex < getSubject().getMatrixCount()-1) {
					getSubject().swapMatrices(selIndex, selIndex+1);
					return Action.STATUS_OK;
				} else {
					return Action.STATUS_CANCEL;
				}
			}
		});		
		
		matricesListField.addAction(new Action.Stub(null, resources.getImage("paint_stylo.png"), Action.STYLE_BUTTON){			
			@Override
			public String getTooltip() {
				return "Put an image to the matrix '" + matricesListField.getSingleSelection().getName() + "'.";
			}
			
			@Override
			public int getVisibility() {
				return matricesListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				// File chooser
				FileDialog fd = new FileDialog(studioContext.getShell(), SWT.OPEN);
		        fd.setText("Open");
		        fd.setFilterPath("C:/");
		        String[] filterExt = { "*.png", "*.*" };
		        fd.setFilterExtensions(filterExt);
		        String selected = fd.open();
		        if(selected==null)return Action.STATUS_OK;
				File f = new File(selected);
				if(f.getPath().equals("")) return Action.STATUS_OK;
				Matrix mat = matricesListField.getSingleSelection();
				// Stretch the image into the matrix size
				ImageData imgdata = new ImageData(selected);
				imgdata = imgdata.scaledTo(mat.getSizeX(), mat.getSizeY());
				// Convert RVB image pixels to values in the matrix (int or float)
				for(int i=0; i<mat.getSizeX(); i++) {
					for(int j=0; j<mat.getSizeY(); j++) {
						if(mat instanceof MatrixInteger) {
							int value = imgdata.getPixel(i, j);
							int R = value & 0x0000FF;
							int G = (value & 0x00FF00) >> 8;
							int B = (value & 0xFF0000) >> 16;
							int val = (R << 16) + (G << 8) + B;
							((MatrixInteger)mat).setValueAt    (i, j, 0, val);
							((MatrixInteger)mat).setInitValueAt(i, j, 0, val);
						}
						if(mat instanceof MatrixFloat) {
							((MatrixFloat)mat).setValueAt    (i, j, 0, imgdata.getPixel(i, j)/256f);
							((MatrixFloat)mat).setInitValueAt(i, j, 0, imgdata.getPixel(i, j)/256f);
						}					
					}
				}
				
				return Action.STATUS_OK;
			}
		});
		
		matricesListField.addListener(new NotificationListener() {
			public void notified(Notification notification) {
				if ( BasicsUI.NOTIFICATION_SELECTION.equals(notification.name) ) {
					if ( matrixController.getSubject() != matricesListField.getSingleSelection() ) {
						matrixController.setSubject(matricesListField.getSingleSelection());
						refreshFields();
					}
					studioContext.setSelection(matricesListField.getSingleSelection());
				}
			}
		});
		
		matrixController = new MatrixController(simulator);

		matricesComposite = new BorderField("Matrices", BasicsUI.NONE, matrixController.createFields());
		matricesComposite.setWest(matricesListField, 25);
		
		matricesComposite.addAction(new Action.Stub(null, resources.getImage("bouton_compile.png"), Action.STYLE_BUTTON){
			
			@Override
			public int getVisibility() {
				return simulator.canCompile() ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public String getTooltip() {
				return "Compile the code.";
			}
			@Override
			public int run(ActionMonitor monitor) {
				if ( studioContext.saveModel(false) ) {
					final boolean succeeded = simulator.compileKernelCode();
					studioContext.setCompiled(succeeded);
					return Action.STATUS_OK;
				} else {
					studioContext.warning("Not compiled, file not saved.");
					return Action.STATUS_CANCEL;
				}
				
			}
		});
		matricesComposite.addAction(new Action.Stub(null, resources.getImage("run_tool.gif"), Action.STYLE_BUTTON){
			
			@Override
			public int getVisibility() {
				return simulator.canRun() ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}

			@Override
			public String getTooltip() {
				return "Execute the code.";
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				if(studioContext.isCompiled()==false) {
					if ( studioContext.saveModel(false) ) {
						final boolean succeeded = simulator.compileKernelCode();
						studioContext.setCompiled(succeeded);
					} else {
						studioContext.warning("Not compiled, file not saved.");
						return Action.STATUS_CANCEL;
					}
				}
				if(studioContext.isCompiled() == true) {
					simulator.start(simulationMonitor);
                    studioContext.asynchronousRun(60, matrixesRefresh);
				}
				return Action.STATUS_OK;
			}
		});

		matricesComposite.addAction(new Action.Stub(null, resources.getImage("bouton_step.png"), Action.STYLE_BUTTON){
			
			@Override
			public int getVisibility() {
				return ((simulator.isStarted() && !simulator.isRunning()) || simulator.canRun()) ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}

			@Override
			public String getTooltip() {
				return "Execute the code for 1 STEP.";
			}
			@Override
			public int run(ActionMonitor monitor) {
				if(studioContext.isCompiled()==false) {
					studioContext.setCompiled(simulator.compileKernelCode());
				}
				
				if(studioContext.isCompiled()==true) {
					if(recordingMPEG == true) recordMPEG();
					if ( simulator.executeStep(0, 0, 0) ) {
						simulator.GetResultCL(getSubject().getMatrixList());
					} else {
						simulator.stop();
					}
					matrixController.refreshFields();
				}
				
				return Action.STATUS_OK;
			}
		});		
		
		matricesComposite.addAction(new Action.Stub(){
			
			@Override
			public Image getImage() {
				if ( !simulator.isStarted() || simulator.isRunning() ) {
					return resources.getImage("bouton_pause.png");
				} else {
					return resources.getImage("run_tool.gif");
				}
			}
			
			@Override
			public int getVisibility() {
				return simulator.isStarted() ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}

			@Override
			public String getTooltip() {
				return simulator.isRunning() ? "Pauses simulation." : "Resumes simulation.";
			}
			@Override
			public int run(ActionMonitor monitor) {
				simulator.setRunning(!simulator.isRunning());
				return Action.STATUS_OK;
			}
		});
		matricesComposite.addAction(new Action.Stub(null, resources.getImage("stop.gif"), Action.STYLE_BUTTON){
			
			@Override
			public int getVisibility() {
				return simulator.isStarted() ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public String getTooltip() {
				return "Stop the execution.";
			}
			@Override
			public int run(ActionMonitor monitor) {
				simulator.stop();
				studioContext.setCompiled(false);
				return Action.STATUS_OK;
			}
		});

		matricesComposite.addAction(new Action.Stub("+"){
			
			@Override
			public int getVisibility() {
				if ( simulator.getRefreshStep() < 2 ) return VISIBILITY_DISABLE;
				else return VISIBILITY_ENABLE;
			}
			
			@Override
			public String getTooltip() {
				return "Increase refresh rate.";
			}
			@Override
			public int run(ActionMonitor monitor) {
				int current = simulator.getRefreshStep();
				if ( current >= 200 ) {
					current -= 100;
				} else if ( current >= 20 ) {
					current -= 10;
				} else {
					current -= 1;
				}
				simulator.setRefreshStep(current); 
				studioContext.log("Refresh step is " + simulator.getRefreshStep());
				return Action.STATUS_OK;
			}
		});
		
		matricesComposite.addAction(new Action.Stub("-"){
			
			@Override
			public int getVisibility() {
				if ( simulator.getRefreshStep() > 900 ) return VISIBILITY_DISABLE;
				else return VISIBILITY_ENABLE;
			}
			
			@Override
			public String getTooltip() {
				return "Decrease refresh rate.";
			}
			@Override
			public int run(ActionMonitor monitor) {
				int current = simulator.getRefreshStep();
				if ( current >= 100 ) {
					current += 100;
				} else if ( current >= 10 ) {
					current += 10;
				} else {
					current += 1;
				}
				simulator.setRefreshStep(current); 
				studioContext.log("Refresh step is " + simulator.getRefreshStep());
				return Action.STATUS_OK;
			}
		});
		
		matricesComposite.addAction(new Action.Stub("Video"){
			private String currentPath = new File(".").getAbsolutePath();

			@Override
			public int getVisibility() {
				return matricesListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public String getTooltip() {
				return "Record the current matrix into an AVI file.";
			}
			@Override
			public int run(ActionMonitor monitor) {
				if(recordingMPEG == false) {
					FileDialog dialog = new FileDialog(studioContext.getShell(), SWT.SAVE);
			        dialog.setText(getTooltip());
			        dialog.setFilterExtensions(new String[] { "*.avi", "*.*" });
			        dialog.setFilterNames(new String[] { "AVI", "Other files" });
			        dialog.setFilterPath(currentPath);
			        
			        String resultFilename = dialog.open();
			        if ( resultFilename == null ) return Action.STATUS_CANCEL;
			        
			        if( !resultFilename.endsWith(".avi") ) resultFilename = resultFilename.concat(".avi");

			        File file = new File(resultFilename);
			        currentPath = file.getParent();
					try {
						startRecordMPEG(file);
						studioContext.log("Animation will be saved to file '" + resultFilename + "'.");
					} catch (Exception e) {
						studioContext.log("Can't save animation to file '" + resultFilename + "'.\n" + " Exception:" + e.getMessage());
						e.printStackTrace();
					}
					return Action.STATUS_OK;					
				
				} else {
				       stopRecordMPEG();
				}
				return Action.STATUS_OK;
			}
		});

		matricesComposite.addAction(new Action.Stub("Img", Action.STYLE_BUTTON | Action.STYLE_BOOLEAN_STATE){
			private String currentPath = new File(".").getAbsolutePath();

			@Override
			public int getVisibility() {
				return matricesListField.getNbLines() > 0 ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public String getTooltip() {
				return "Record all the matrices into PNG files.";
			}
			
			@Override
			public boolean getState() {
				return recordingPNG;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				if(recordingPNG == false) {
					FileDialog dialog = new FileDialog(studioContext.getShell(), SWT.SAVE);
			        dialog.setText(getTooltip());
			        dialog.setFilterExtensions(new String[] { "*.png", "*.*" });
			        dialog.setFilterNames(new String[] { "PNG", "Other files" });
			        dialog.setFilterPath(currentPath);
			        
			        String resultFilename = dialog.open();
			        if ( resultFilename == null ) return Action.STATUS_CANCEL;
			        
			        if( !resultFilename.endsWith(".png") ) resultFilename = resultFilename.concat(".png");
			        resultFilename = resultFilename.substring(0, resultFilename.length()-1-3); // We suppress the .png
			        File file = new File(resultFilename);
			        currentPath = file.getParent();
					try {
						startRecordPNG(file);
						studioContext.log("Images will be saved to file '" + resultFilename + "_matrixName_timeStep.png'.");
					} catch (Exception e) {
						studioContext.log("Can't save animation to file '" + resultFilename + "'.\n" + " Exception:" + e.getMessage());
						e.printStackTrace();
					}
					return Action.STATUS_OK;					
				
				} else {
				       stopRecordPNG();
				}
				return Action.STATUS_OK;
			}
		});		
		return matricesComposite;
	}

	@Override
	public void refreshFields() {		
		if ( getSubject() == null ) {
			matricesListField.setEnable(false);
		} else {
			matricesListField.setEnable(true);
			matricesListField.setValue(getSubject().getMatrixList());
			matricesListField.refresh();
			studioContext.setSelection(matricesListField.getSingleSelection());

			matrixController.refreshFields();
			matricesComposite.refreshButtonBar();
		}
	}
	
	public void dispose() {
		if(recordingMPEG == true) stopRecordMPEG();
	}
	
	
	private MjpegEncoder mpeg = null;
	private boolean recordingMPEG = false;
	private int mpegSizeX = 512, mpegSizeY = 256;
	private void startRecordMPEG(File f) {
		recordingMPEG = true; simulator.recordingMPEG = true;
	       double framerate = 30.0;
	       int numFrames = 1;
	       try {
	    	   mpeg = new MjpegEncoder(f, mpegSizeX, mpegSizeY, framerate, numFrames);
	       } catch (Exception ex) {
	    	   System.out.println(ex.getMessage());
	    	   return;
	       }
	}
	public void recordMPEG() {
	       mpeg.numFrames++;
	       // Create the Image to be saved into the MPEG
           java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(mpegSizeX,mpegSizeY,java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
           java.awt.Graphics g = img.getGraphics();
	       // Get the current displayed matrix
	       Matrix mat = matricesListField.getSingleSelection();
	       if(mat != null) { // Fill the image with the matrix data
	    	   for(int xx = 0; xx < mpegSizeX; xx++) {
		    	   for(int yy = 0; yy < mpegSizeY; yy++) {
		    		    int mxx = (xx*mat.getSizeX()) / mpegSizeX; 
		    		    int myy = (yy*mat.getSizeY()) / mpegSizeY; 
		    		    int B = (mat.getValueAt(mxx, myy, 0).intValue() >> 16) & 0xFF;
		    	   		int G = (mat.getValueAt(mxx, myy, 0).intValue() >> 8) & 0xFF;
		    	   		int R = (mat.getValueAt(mxx, myy, 0).intValue()     ) & 0xFF;
		    		   g.setColor(new Color(R,G,B));
			           g.drawLine(xx, yy, xx, yy);
		    	   }
	    	   }
	    	   g.setColor(Color.CYAN);
	    	   if(simulator.getInitialSimulationTime()<0)
					g.drawString("Step="+simulator.getNbSteps() + "   Time=0", 0,10);
					else g.drawString("Step="+simulator.getNbSteps() + "   Time="+(System.currentTimeMillis()-simulator.getInitialSimulationTime())/1000, 0,10);
	    	   
	       } else {} // If not matrix selected, we keep it blank
	           try {
	        	   mpeg.addImage(img);
	           } catch (Exception ex) {
	        	   System.out.println(ex.getMessage());
	           }
		}
	
	private void stopRecordMPEG() {
		try {
			recordingMPEG = false; simulator.recordingMPEG = false;
    	   mpeg.finishAVI();
		} catch (Exception ex) {
			System.out.println("in recordingMPEG::stopRecordMPEG"+ex.toString());
			System.out.println(ex.getMessage());
		}
	}
	
	private boolean recordingPNG = false;
	private File recordPNGFileProto = null;
	private void startRecordPNG(File f) {
		recordingPNG = true; simulator.recordingPNG = true;
		recordPNGFileProto = f;
	}

	public void recordPNG() {
		for(int m=0; m<matricesListField.getValue().size(); m++) {
	       // Create the Image to be saved into the PNG
			Matrix mat = matricesListField.getValue().get(m);
           java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(mat.getSizeX(),mat.getSizeY(),java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
           java.awt.Graphics g = img.getGraphics();
	       // Get the current displayed matrix
    	   String fileName = recordPNGFileProto.getAbsolutePath()+"_"+mat.getName()+"_"+simulator.getNbSteps()+".png";
	       if(mat != null) { // Fill the image with the matrix data
	    	   for(int xx = 0; xx < mpegSizeX; xx++) {
		    	   for(int yy = 0; yy < mpegSizeY; yy++) {
		    		    int mxx = (xx*mat.getSizeX()) / mpegSizeX; 
		    		    int myy = (yy*mat.getSizeY()) / mpegSizeY; 
		    		    int B = (mat.getValueAt(mxx, myy, 0).intValue() >> 16) & 0xFF;
		    	   		int G = (mat.getValueAt(mxx, myy, 0).intValue() >> 8) & 0xFF;
		    	   		int R = (mat.getValueAt(mxx, myy, 0).intValue()     ) & 0xFF;
		    		   g.setColor(new Color(R,G,B));
			           g.drawLine(xx, yy, xx, yy);
		    	   }
	    	   }
	    	   g.setColor(Color.CYAN);
	    	   
	       }
	       System.out.println("processing file "+fileName);
	       try {
	    	   ImageIO.write(img, "png", new File(fileName));
           } catch (Exception ex) {
        	   System.out.println(ex.getMessage());
           }
		}
	}
	
	private void stopRecordPNG() {
		recordingPNG = false; simulator.recordingPNG = false;
	}
	
	
	public void stopAllExports() {
       if(recordingMPEG == true)
    	   stopRecordMPEG();
       if(recordingPNG == true)
    	   stopRecordPNG();
    }

}
