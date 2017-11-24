package matrixstudio.ui;

import fr.minibilles.basics.progress.ActionMonitor;
import fr.minibilles.basics.ui.BasicsUI;
import fr.minibilles.basics.ui.action.Action;
import fr.minibilles.basics.ui.controller.Controller;
import fr.minibilles.basics.ui.field.BorderField;
import fr.minibilles.basics.ui.field.CompositeField;
import fr.minibilles.basics.ui.field.Field;
import fr.minibilles.basics.ui.field.ListField;
import matrixstudio.kernel.Simulator;
import matrixstudio.model.Library;
import matrixstudio.model.Model;

public class LibraryTabController extends Controller<Model> {

	private final StudioContext studioContext;

	private final MSResources resources;

	private BorderField librariesComposite;
	private ListField<Library> librariesListField;
	private SourceCodeField librariesSourceCodeField;
	
	public LibraryTabController(StudioContext studioContext) {
		this.studioContext = studioContext;
		this.resources = studioContext.getResources();
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
				final Simulator simulator = studioContext.getSimulator();
				return simulator.canCompile() ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public String getTooltip() {
				return "Compile the code.";
			}
			@Override
			public int run(ActionMonitor monitor) {				
				final Simulator simulator = studioContext.getSimulator();
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
			studioContext.setSelection(librariesListField.getSingleSelection());
		}

	}

	@Override
	public boolean updateSubject(Field field) {
		if ( field == librariesListField ) {
			librariesSourceCodeField.setCode(librariesListField.getSingleSelection());
			studioContext.setSelection(librariesListField.getSingleSelection());
			return false;
		}
		return false;
	}
	
	public void openFindAndReplaceDialog() {
		librariesSourceCodeField.openSearchAndReplaceShell();
	}

}
