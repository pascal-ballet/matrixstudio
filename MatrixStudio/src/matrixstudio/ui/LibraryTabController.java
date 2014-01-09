package matrixstudio.ui;

import matrixstudio.kernel.Simulator;
import matrixstudio.model.Library;
import matrixstudio.model.Model;

import org.xid.basics.progress.ActionMonitor;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.action.Action;
import org.xid.basics.ui.controller.Controller;
import org.xid.basics.ui.field.BorderField;
import org.xid.basics.ui.field.CompositeField;
import org.xid.basics.ui.field.Field;
import org.xid.basics.ui.field.ListField;

public class LibraryTabController extends Controller<Model> {

	private final MatrixStudio matrixStudio;

	private final MSResources resources;

	private BorderField librariesComposite;
	private ListField<Library> librariesListField;
	private SourceCodeField librariesSourceCodeField;
	
	public LibraryTabController(MatrixStudio matrixStudio) {
		this.matrixStudio = matrixStudio;
		this.resources = matrixStudio.getResources();
	}

	@Override
	public CompositeField createFields() {
		librariesListField = new ListField<Library>(null, BasicsUI.NO_INFO) {
			@Override
			public String getText(Library element) {
				return element.getName() == null ? "" : element.getName();
			}
		};
		
		librariesListField.addAction(new Action.Stub("+", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {
			
			@Override
			public String getTooltip() {
				return "Creates a new library.";
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				Library library = new Library();
				library.setName(NameUtils.availableName("Library", getSubject().getCodeList()));
				getSubject().addCodeAndOpposite(library);
				return Action.STATUS_OK;
			}
		});
		librariesListField.addAction(new Action.Stub("-", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {
			
			@Override
			public String getTooltip() {
				return "Removes library '" + librariesListField.getSingleSelection().getName() + "'.";
			}
			
			@Override
			public int getVisibility() {
				return librariesListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				Library library = librariesListField.getSingleSelection();
				getSubject().removeCodeAndOpposite(library);
				return Action.STATUS_OK;
			}
		});

		librariesListField.addAction(new Action.Stub(null, resources.getImage("bouton_up.png"), Action.STYLE_BUTTON) {
			
			@Override
			public String getTooltip() {
				return "Move library up.";
			}
			
			@Override
			public int getVisibility() {
				return librariesListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				getSubject().upLibrary(librariesListField.getSingleSelection());
				return Action.STATUS_OK;
			}
		});
		
		librariesListField.addAction(new Action.Stub(null, resources.getImage("bouton_down.png"), Action.STYLE_BUTTON) {
			
			@Override
			public String getTooltip() {
				return "Move library down.";
			}
			
			@Override
			public int getVisibility() {
				return librariesListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				getSubject().downLibrary(librariesListField.getSingleSelection());
				return Action.STATUS_OK;
			}
		});			

		librariesSourceCodeField = new SourceCodeField(null, BasicsUI.NO_INFO);
		librariesComposite = new BorderField("Libraries", BasicsUI.NONE, librariesSourceCodeField);
		librariesComposite.setWest(librariesListField, 20);
		librariesComposite.addAction(new Action.Stub(null, resources.getImage("bouton_compile.png"), Action.STYLE_BUTTON){
			
			@Override
			public int getVisibility() {
				final Simulator simulator = matrixStudio.getSimulator();
				return simulator.canCompile() ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public String getTooltip() {
				return "Compile the code.";
			}
			@Override
			public int run(ActionMonitor monitor) {				
				final Simulator simulator = matrixStudio.getSimulator();
				if ( matrixStudio.saveModel(false) ) { 
					final boolean succeeded = simulator.compileKernelCode();
					matrixStudio.setCompiled(succeeded);
					return Action.STATUS_OK;
				} else {
					matrixStudio.warning("Not compiled, file not saved.");
					return Action.STATUS_CANCEL;
				}
			}
		});

		return librariesComposite;
	}

	@Override
	public void refreshFields() {
		if ( getSubject() == null ) {
			librariesComposite.setEnable(false);
		} else {
			librariesComposite.setEnable(true);
			librariesListField.setValue(getSubject().getLibraryList());
			librariesListField.refresh();
			librariesSourceCodeField.setCode(librariesListField.getSingleSelection());
			matrixStudio.setSelection(librariesListField.getSingleSelection());
		}

	}

	@Override
	public boolean updateSubject(Field field) {
		if ( field == librariesListField ) {
			librariesSourceCodeField.setCode(librariesListField.getSingleSelection());
			matrixStudio.setSelection(librariesListField.getSingleSelection());
			return false;
		}
		return false;
	}
	
	public void openFindAndReplaceDialog() {
		librariesSourceCodeField.openSearchAndReplaceShell();
	}

}
