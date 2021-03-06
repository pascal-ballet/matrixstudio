package matrixstudio.ui;

import fr.minibilles.basics.model.ChangeRecorder;
import fr.minibilles.basics.notification.Notification;
import fr.minibilles.basics.notification.NotificationListener;
import fr.minibilles.basics.progress.ActionMonitor;
import fr.minibilles.basics.ui.BasicsUI;
import fr.minibilles.basics.ui.PlatformUtil;
import fr.minibilles.basics.ui.action.Action;
import fr.minibilles.basics.ui.action.ActionExecuter;
import fr.minibilles.basics.ui.dialog.FieldShellToolkit;
import fr.minibilles.basics.ui.field.BorderField;
import fr.minibilles.basics.ui.field.CompositeField;
import fr.minibilles.basics.ui.field.ConsoleField;
import fr.minibilles.basics.ui.field.MultiTabField;
import fr.minibilles.basics.ui.field.PropertiesField;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import matrixstudio.kernel.CLUtil;
import matrixstudio.kernel.SExpModel;
import matrixstudio.kernel.Simulator;
import matrixstudio.kernel.SimulatorContext;
import matrixstudio.kernel.Tools;
import matrixstudio.model.Matrix;
import matrixstudio.model.Model;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class MatrixStudio implements SimulatorContext, StudioContext {

	private String version;
	private String date;

	/**
	 * Stores last path to select files.
	 */
	private Path currentPath = Paths.get(".");
	private Path modelPath;
	private Model model;

	private Display display;
	private Shell shell;

	private SimpleMatrixRenderer matrixRenderer = new SimpleMatrixRenderer();
	private Map<Matrix, Shell> matrixShells = new HashMap<>();

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

	private ModelController modelController;

	private MultiTabField southEastField;
	private CompositeField consoleComposite;
	private ConsoleField consoleField;
	private int consoleIndex;

	private CompositeField propertiesComposite;
	private PropertiesField propertiesField;

	private final Simulator simulator;

	private ActionExecuter executer = new ActionExecuter() {
		public void executeAction(Action action) {
			boolean transactional = action.hasStyle(Action.STYLE_TRANSACTIONNAL);
			if (transactional) {
				model.getChangeRecorder().newOperation();
			}

			int status = action.run(ActionMonitor.empty);
			if (transactional && status == Action.STATUS_CANCEL) {
				model.getChangeRecorder().undo();
			}
			if (!shell.isDisposed())
				refreshFields();
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
			version = "unknown";
			date = "unknown";
		}
	}

	public boolean GetEmbedded() {
		return simulator.Embedded;
	}

	public void SetEmbedded(boolean b) {
		simulator.Embedded = b;
	}

	private boolean compiled = false;

	public CompositeField createFields() {
		centerField = new MultiTabField();

		createMatricesTab();
		createSchedulerTab();
		createKernelsTab();
		createLibrariesTab();

		centerField.addStructureListener(new NotificationListener() {
			public void notified(Notification n) {
				if (BasicsUI.NOTIFICATION_SELECTION.equals(n.name)) {
					setSelection(null);
					refreshFields();
				}
			}
		});

		createSouthEastTab();
		createPropertiesTab();

		southField = new BorderField(BasicsUI.NONE, southEastField);
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

	private void createSouthEastTab() {
		southEastField = new MultiTabField();
		southEastField.setActionExecuter(executer);

		modelController = new ModelController();
		modelController.setSubject(model);
		southEastField.addTab(modelController, resources.getImage("eclipse/file_obj.gif"), false);

		consoleField = new ConsoleField();
		consoleComposite = new CompositeField("Console", consoleField);
		southEastField.addTab(consoleComposite, resources.getImage("eclipse/console_view.gif"), false);

		consoleIndex = southEastField.indexOf(consoleComposite);
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
		if (model == null) {
			mainField.setEnable(false);
		}
		else {
			mainField.setEnable(true);

			refreshMatrixShells();

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

			refreshModel();

			if (toolkit != null)
				toolkit.validateAll();
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

	public void refreshModel() {
		modelController.setSubject(model);
		modelController.refreshFields();
	}

	public void redrawMatrixShells() {
		for (Shell shell : matrixShells.values()) {
			if (!shell.isDisposed())
				shell.redraw();
		}
	}

	public void refreshMatrixShells() {
		List<Matrix> toClose = new ArrayList<>();
		for (Map.Entry<Matrix, Shell> entry : matrixShells.entrySet()) {
			Matrix matrix = entry.getKey();
			Shell shell = entry.getValue();
			if (model.getMatrixList().contains(matrix) && shell != null && !shell.isDisposed()) {
				if (!matrix.getName().equals(shell.getText())) {
					shell.setText(matrix.getName());
				}
				Point size = shell.getSize();
				if (size.x != matrix.safeGetSizeXValue() || size.y != matrix.safeGetSizeYValue()) {
					shell.setSize(matrix.safeGetSizeXValue(), matrix.safeGetSizeYValue());
				}
				shell.getChildren()[0].redraw();
			}
			else {
				toClose.add(matrix);
			}
		}

		for (Matrix matrix : toClose) {
			closeShellForMatrix(matrix);
		}
	}

	@Override
	public void asynchronousRun(int milliseconds, Runnable runnable) {
		if (display.isDisposed())
			return;

		if (milliseconds <= 0) {
			display.asyncExec(runnable);
		}
		else {
			display.timerExec(milliseconds, runnable);
		}
	}

	@Override
	public void simulationRefresh() {
		refreshMatrixShells();
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
		if (propertiesField != null) {
			propertiesField.setSubject(selection);
		}
	}

	public Path getModelPath() {
		return modelPath;
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
		modelPath = null;
	}

	public boolean loadModel(boolean safeRead) {
		final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Open a simulation.");
		dialog.setFilterExtensions(new String[] { "*.mss", SExpModel.MATRIXSTUDIO_SIMULATION, "*.*" });
		dialog.setFilterNames(new String[] { "Matrix Studio Simulation", "Exploded Matrix Studio Simulation", "Other files" });
		dialog.setFilterPath(currentPath.toString());
		String resultFilename = dialog.open();
		if (resultFilename == null)
			return false;

		Path path = Paths.get(resultFilename);
		if (!Files.exists(path))
			return false;

		currentPath = path.getParent();
		stopAllExports();

		try {
			// loads model
			model = Tools.load(path);
			modelPath = path;
		} catch (IOException e) {
			error("Cannot file simulation from file '" + path + "': " + e.getMessage());
			return false;
		}
		log("Simulation successfully loaded from file '" + path + "'.");
		return true;
	}

	@Override
	public boolean saveModel(boolean forceDialog) {
		if (model == null)
			return false;

		Path path = modelPath;
		if (forceDialog || modelPath == null) {
			// selects a file where to file
			final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
			dialog.setText("Save the simulation as...");
			dialog.setFilterExtensions(new String[] { "*.mss", SExpModel.MATRIXSTUDIO_SIMULATION, "*.*" });
			dialog.setFilterNames(new String[] { "Matrix Studio Simulation", "Exploded Matrix Studio Simulation", "Other files" });
			dialog.setFilterPath(currentPath.toString());

			String resultFilename = dialog.open();
			if (resultFilename == null)
				return false;

			path = Paths.get(resultFilename);
			if (dialog.getFilterIndex() == 1) {
				if (Files.isDirectory(path)) {
					path = path.resolve(SExpModel.MATRIXSTUDIO_SIMULATION);
				}
				else {
					path = path.getParent().resolve(SExpModel.MATRIXSTUDIO_SIMULATION);
				}
			}
			else if (dialog.getFilterIndex() == 1 && !resultFilename.endsWith(".mss")) {
				path = path.getParent().resolve(path.getFileName().toString() + ".mss");
			}

			currentPath = path.getParent();
		}

		try {
			// saves model
			Tools.save(model, path);
			modelPath = path;
		} catch (IOException e) {
			error("Cannot save simulation to file '" + path + "': " + e.getMessage());
			return false;
		}
		log("Simulation successfully saved to file '" + path + "'.");
		return true;
	}

	public void openFindAndReplaceDialog() {
		// Careful is tab order changes.
		int selected = centerField.getSelected();
		if (selected == 2) {
			kernelTabController.openFindAndReplaceDialog();
		}
		else if (selected == 3) {
			libraryTabController.openFindAndReplaceDialog();
		}

	}

	public void log(final String message) {
		display.asyncExec(() -> {
			int index = southEastField.getSelected();
			if (index != consoleIndex)
				southEastField.setSelected(consoleIndex);
			consoleField.log(message + "\n");
			consoleField.scrollToTheEnd();
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

	/**
	 * Sets output redirection to console.
	 */
	private void initStdoutRedirection() {
		PrintStream out = new PrintStream(new OutputStream() {

			private StringBuilder buffer = null;

			private void log(final String message) {
				if (!display.isDisposed()) {
					display.asyncExec(new Runnable() {
						public void run() {
							MatrixStudio.this.log(message);
						}
					});
				}
			}

			@Override
			public void write(int code) throws IOException {
				if (buffer == null) {
					buffer = new StringBuilder();
				}
				char toPrint = (char) code;
				if (toPrint == '\n') {
					log(buffer.toString());
					buffer = null;
				}
				else {
					buffer.append(toPrint);
				}
			}

			@Override
			public void flush() throws IOException {
				log(buffer.toString());
				buffer = null;
			}

		});
		System.setOut(out);
	}

	public void open() {

		initStdoutRedirection();

		display = new Display();
		resources = MSResources.getInstance(MSResources.class);

		CompositeField mainField = createFields();
		refreshFields();
		shell = FieldShellToolkit.createShell(display, createHeaderTitle());

		actions = new Actions(this);

		String title = createHeaderTitle();

		toolkit = new FieldShellToolkit(shell, title, BasicsUI.SHOW_HINTS | BasicsUI.NO_AUTOMATIC_VALIDATION | BasicsUI.NO_HEADER,
				mainField);
		toolkit.setBannerImage(resources.getImage("MatrixStudio-32.png"));
		toolkit.setActionExecuter(executer);
		toolkit.setMenuActions(actions.getMenuBarAction().getActions());
		toolkit.init();

		PlatformUtil.registerCocoaNameAboutAndPreference("MatrixStudio", actions.getAction("About"), new Action.Stub());

		shell.setSize(1024, 768);
		if (simulator.Embedded == false) {
			shell.open();
			try {
				while (!shell.isDisposed()) {
					try {
						if (!shell.getDisplay().readAndDispatch()) {
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
	}

	public Boolean isShellOpenForMatrix(Matrix matrix) {
		Shell shell = matrixShells.get(matrix);
		return shell != null && !shell.isDisposed();
	}

	public void openShellForMatrix(Matrix matrix) {
		Shell shell = matrixShells.get(matrix);
		if (shell == null || shell.isDisposed()) {
			shell = new Shell(display, SWT.CLOSE | SWT.TITLE);
			shell.setText(matrix.getName());
			shell.setSize(matrix.safeGetSizeXValue(), matrix.safeGetSizeYValue());
			shell.setLayout(new FillLayout());
			Canvas canvas = new Canvas(shell, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
			canvas.addPaintListener(e -> matrixRenderer.render(e.gc, this, matrix, 0));

			shell.open();
			matrixShells.put(matrix, shell);
		}
	}

	public void closeShellForMatrix(Matrix matrix) {
		Shell shell = matrixShells.get(matrix);
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
		}
		matrixShells.remove(matrix);
	}

	/**
	 * MatrixStudio header title construction.
	 * It uses {@link CLUtil#isClPresent()} to print the information about CL.
	 *
	 * @return a string.
	 */
	private String createHeaderTitle() {
		return "M A T R I X   S T U D I O" + " - " + "Version " + version + " of " + date + " - " + "OpenCL device(s) " + (CLUtil
				.isClPresent() ? "FOUND." : "NOT FOUND. ");
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
