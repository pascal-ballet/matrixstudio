package matrixstudio.ui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import matrixstudio.kernel.SimulatorContext;
import matrixstudio.kernel.CLUtil;
import matrixstudio.kernel.Simulator;
import matrixstudio.kernel.Tools;
import matrixstudio.model.Model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.xid.basics.model.ChangeRecorder;
import org.xid.basics.notification.Notification;
import org.xid.basics.notification.NotificationListener;
import org.xid.basics.progress.ActionMonitor;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.PlatformUtil;
import org.xid.basics.ui.action.Action;
import org.xid.basics.ui.action.ActionExecuter;
import org.xid.basics.ui.dialog.FieldShellToolkit;
import org.xid.basics.ui.field.BorderField;
import org.xid.basics.ui.field.CompositeField;
import org.xid.basics.ui.field.ConsoleField;
import org.xid.basics.ui.field.MultiTabField;
import org.xid.basics.ui.field.PropertiesField;


public class MatrixStudio implements SimulatorContext, StudioContext {

	private String version;
	private String date;

	/** Stores last path to select files. */
	private String currentPath = new File(".").getAbsolutePath();
	private File modelFile;
	private Model model;
	
	private Display display;
	private Shell shell;
	private FieldShellToolkit toolkit;
	private Actions actions;
	
	private MSResources resources;
	
	private BorderField mainField;
	
	private MultiTabField centerField;
	
	private MatrixTabController matrixTabController;
	private ScheduleTabController schedulerTabController;
	private KernelTabController kernelTabController;
	private LibraryTabController libraryTabController;

	private BorderField southField;
	private CompositeField consoleComposite;
	private ConsoleField consoleField;
	
	private CompositeField propertiesComposite;
	private PropertiesField propertiesField;

	private final Simulator simulator;
	
	private ActionExecuter executer = new ActionExecuter() {
		public void executeAction(Action action) {
			boolean transactional = action.hasStyle(Action.STYLE_TRANSACTIONNAL);
			if ( transactional ) {
				model.getChangeRecorder().newOperation();
			}

			int status = action.run(ActionMonitor.empty);
			if ( transactional && status == Action.STATUS_CANCEL ) {
				model.getChangeRecorder().undo();
			}
			if ( shell.isDisposed() == false ) refreshFields();
		}
	};

	public MatrixStudio() {
		initVersion();
		model = Tools.createEmptyModel();
		simulator = new Simulator(this);
	}
	
	private void initVersion() {
		try {
			Properties properties = new Properties();
			properties.load(getClass().getResourceAsStream("version.properties"));
			version = properties.getProperty("version");
			date = properties.getProperty("date");
		} catch (Exception e) {
			version="unknown";
			date="unknown";
		}
	}
	
	private boolean compiled=false;

	public CompositeField createFields() {
		centerField = new MultiTabField();
		
		createMatricesTab();
		createSchedulerTab();
		createKernelsTab();
		createLibrariesTab();
		
		centerField.addStructureListener(new NotificationListener() {
			public void notified(Notification n) {
				if (BasicsUI.NOTIFICATION_SELECTION.equals(n.name) ) {
					setSelection(null);
					refreshFields();
				}
			}
		});

		createConsoleTab();
		createPropertiesTab();
		
		southField = new BorderField(BasicsUI.NONE, consoleComposite);
		southField.setWest(propertiesComposite, 60);
		
		mainField = new BorderField(BasicsUI.NONE, centerField);
		mainField.setSouth(southField, 30);
		mainField.setActionExecuter(executer);
		
		return mainField;
	}

	private void createMatricesTab() {
		matrixTabController = new MatrixTabController(this);
		matrixTabController.setSubject(model);
		centerField.addTab(matrixTabController, resources.getImage("eclipse/prop_ps.gif"), false);
	}

	private void createSchedulerTab() {
		schedulerTabController = new ScheduleTabController(this);
		schedulerTabController.setSubject(model);
		centerField.addTab(schedulerTabController, resources.getImage("eclipse/filter_ps.gif"), false);
	}

	private void createKernelsTab() {
		kernelTabController = new KernelTabController(this);
		kernelTabController.setSubject(model);
		centerField.addTab(kernelTabController, resources.getImage("eclipse/editor_area.gif"), false);
	}

	private void createLibrariesTab() {
		libraryTabController = new LibraryTabController(this);
		libraryTabController.setSubject(model);
		centerField.addTab(libraryTabController, resources.getImage("eclipse/outline_co.gif"), false);
	}
	
	private void createConsoleTab() {
		consoleField = new ConsoleField();
		consoleComposite = new CompositeField("Console", consoleField);
	}

	private void createPropertiesTab() {
		propertiesField = new PropertiesField("matrixstudio.ui.controller");
		propertiesField.setSelected(0);
		propertiesField.setActionExecuter(executer);
		propertiesComposite = new CompositeField("Properties", propertiesField);
	}

	public MultiTabField getCenterField() {
		return centerField;
	}
	
	public boolean isTextEditing() {
		// Careful is tab order changes.
		int selected = centerField.getSelected();
		return selected >= 2;
	}

	public void refreshFields() {
		if ( model == null ) {
			mainField.setEnable(false);
		} else {
			mainField.setEnable(true);
			
			// refresh field depending on selected tab.
			// Careful is tab order changes.
			int selected = centerField.getSelected();
			switch (selected) {
			case 0:
				refreshMatrices();
				break;
			case 1:
				refreshSchedule();
				break;
			case 2:
				refreshKernels();
				break;
			case 3:
				refreshLibrary();
				break;
			}
			
			if (toolkit != null ) toolkit.validateAll();
		}
	}

	public void refreshMatrices() {
		matrixTabController.setSubject(model);
		matrixTabController.refreshFields();
	}
	
	public void refreshSchedule() {
		schedulerTabController.setSubject(model);
		schedulerTabController.refreshFields();
	}

	public void refreshKernels() {
		kernelTabController.setSubject(model);
		kernelTabController.refreshFields();
	}

	public void refreshLibrary() {
		libraryTabController.setSubject(model);
		libraryTabController.refreshFields();
	}

    @Override
    public void asynchronousRun(int milliseconds, Runnable runnable) {
        if ( display.isDisposed() ) return;

        if ( milliseconds <= 0 ) {
            display.asyncExec(runnable);
        } else {
            display.timerExec(milliseconds, runnable);
        }
    }

    public Display getDisplay() {
		return display;
	}

    @Override
	public Shell getShell() {
		return shell;
	}
	
	@Override
    public Object getSelection() {
		return propertiesField.getSelected();
	}
	
	@Override
    public void setSelection(Object selection) {
		if ( propertiesField != null ) {
			propertiesField.setSubject(selection);
		}
	}

	public File getModelFile() {
		return modelFile;
	}
	
	public Model getModel() {
		return model;
	}
	
	@Override
    public Simulator getSimulator() {
		return simulator;
	}
	
	@Override
    public boolean isCompiled() {
		return compiled;
	}
	
	@Override
    public void setCompiled(boolean compiled) {
		this.compiled = compiled;
	}
	
	@Override
    public MSResources getResources() {
		return resources;
	}
	
	public ChangeRecorder getChangeRecorder() {
		return model.getChangeRecorder();
	}
	
	public void resetModel() {
		model = Tools.createEmptyModel();
		modelFile = null;
	}
	
	public boolean loadModel() {
		final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Open a simulation.");
        dialog.setFilterExtensions(new String[] { "*.mss", "*.*" });
        dialog.setFilterNames(new String[] { "Matrix Studio Simulation", "Other files" });
        dialog.setFilterPath(currentPath);
        String resultFilename = dialog.open();
        if( resultFilename==null ) return false;
        
		File file = new File(resultFilename);
		if( !file.exists() ) return false;
	
		currentPath = file.getParent();
		stopAllExports();

		try {
			// loads model
			model = Tools.load(file);
			modelFile = file;
		} catch (IOException e) {
			error("Cannot load simulation from file '"+ file +"': " + e.getMessage());
			return false;
		}
		log("Simulation successfully loaded from file '"+ file +"'.");
		return true;
	}
	
	@Override
    public boolean saveModel(boolean forceDialog) {
		if ( model == null ) return false;

		
		File file = modelFile;
		if (forceDialog || modelFile == null ) {
			// selects a file where to save
			final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
	        dialog.setText("Save the simulation as...");
	        dialog.setFilterExtensions(new String[] { "*.mss", "*.*" });
	        dialog.setFilterNames(new String[] { "Matrix Studio Simulation", "Other files" });
	        dialog.setFilterPath(currentPath);
	        
	        String resultFilename = dialog.open();
	        if ( resultFilename == null ) return false;
	        
	        if( !resultFilename.endsWith(".mss") ) {
	        	resultFilename = resultFilename.concat(".mss");
	        }
	        
	        file = new File(resultFilename);
	        currentPath = file.getParent();
		}
		
		try {
			// saves model
			Tools.save(model, file);
			modelFile = file;
		} catch (IOException e) {
			error("Cannot save simulation to file '"+ file +"': " + e.getMessage());
			return false;
		}
		log("Simulation successfully saved to file '"+ file + "'.");
		return true;	
	}
	
	
	public void openFindAndReplaceDialog() {
		// Careful is tab order changes.
		int selected = centerField.getSelected();
		if ( selected == 2 ) {
			kernelTabController.openFindAndReplaceDialog();
		} else if ( selected == 3 ) {
			libraryTabController.openFindAndReplaceDialog();
		}
	
	}
	
	public void log(final String message) {
		display.asyncExec(new Runnable() {
			public void run() {
				consoleField.log(message + "\n");
				consoleField.scrollToTheEnd();
			}
		});
	}
	
	public void warning(final String message) {
		display.asyncExec(new Runnable() {
			public void run() {
				consoleField.log(message + "\n", SWT.COLOR_DARK_YELLOW, SWT.ITALIC);
				consoleField.scrollToTheEnd();
			}
		});
	}
	
	public void error(final String message) {
		display.asyncExec(new Runnable() {
			public void run() {
				consoleField.log(message + "\n", SWT.COLOR_DARK_RED, SWT.BOLD);
				consoleField.scrollToTheEnd();
			}
		});
	}
	
	/** Sets output redirection to console. */
	private void initStdoutRedirection() {
		PrintStream out = new PrintStream(new OutputStream() {

			private StringBuilder buffer = null;

			private void log(final String message) {
				if ( !display.isDisposed() ) {
					display.asyncExec(new Runnable() {
						public void run() {
							MatrixStudio.this.log(message);
						}
					});
				}
			}
			@Override
			public void write(int code) throws IOException {
				if ( buffer == null ) {
					buffer = new StringBuilder();
				}
				char toPrint = (char) code;
				if ( toPrint == '\n' ) {
					log(buffer.toString());
					buffer = null;
				} else {
					buffer.append(toPrint);
				}
			}
			
			@Override
			public void flush() throws IOException {
				log(buffer.toString());
				buffer = null;
			}
			
		}) ;
		System.setOut(out);
	}
	
	public void open() {
		
		initStdoutRedirection();
		
		display = new Display();
		resources = MSResources.getInstance(MSResources.class);
		
		CompositeField mainField = createFields();
		refreshFields();
		shell = FieldShellToolkit.createShell(display, "Matrix Studio");

		actions = new Actions(this);
		
		String title = createHeaderTitle();
		
		toolkit = new FieldShellToolkit(shell, title , BasicsUI.SHOW_HINTS | BasicsUI.NO_AUTOMATIC_VALIDATION, mainField);
		toolkit.setInitialMessage("Pascal Ballet - Lab-STICC - European University of Brittany - UBO.\nJean-Charles Roger.");
		toolkit.setBannerImage(resources.getImage("MatrixStudio-64.png"));
		toolkit.setActionExecuter(executer);
		toolkit.setMenuActions(actions.getMenuBarAction().getActions());
		toolkit.init();

		PlatformUtil.registerCocoaNameAboutAndPreference("MatrixStudio", actions.getAction("About"),new Action.Stub());
		
		shell.setSize(1024, 768);
		shell.open();
		try {
			while ( !shell.isDisposed() ) {
				try {
					if ( !shell.getDisplay().readAndDispatch() ) {
						shell.getDisplay().sleep();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
			dispose();
		}
	}

	/**
	 * MatrixStudio header title construction.
	 * It uses {@link CLUtil#isClPresent()} to print the information about CL.
	 * @return a string.
	 */
	private String createHeaderTitle() {
        return
                "M A T R I X   S T U D I O" + " - " +
                "Version " + version + " of " + date + "\n\n" +
                "OpenCL device(s) " + (CLUtil.isClPresent() ? "FOUND." : "NOT FOUND. " +
                "There are 2 possibilities:\n" +
                "1) Your computer have no OpenCL device\n"+
                "2) You have to update your OpenCL device driver.");
	}
	
	public void recordMPEG() {
		matrixTabController.recordMPEG();
	}

	public void recordPNG() {
		matrixTabController.recordPNG();
	}
	
	public void stopAllExports() {
		matrixTabController.stopAllExports();
	}
	
	private void dispose() {
		matrixTabController.dispose();
		MSResources.releaseInstance(MSResources.class);
		simulator.stop();
	}
	
	public static void main(String[] args) {
		MatrixStudio ui = new MatrixStudio();
		ui.open();
	}
}
