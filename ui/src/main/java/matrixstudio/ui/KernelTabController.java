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
import matrixstudio.kernel.Tools;
import matrixstudio.model.Kernel;
import matrixstudio.model.Model;
import matrixstudio.model.Task;

public class KernelTabController extends Controller<Model> {

	private final StudioContext studioContext;

	private final MSResources resources;

	private BorderField kernelComposite;
	private ListField<Kernel> kernelListField;
	private SourceCodeField kernelField;

	public KernelTabController(StudioContext studioContext) {
		this.studioContext = studioContext;
		this.resources = studioContext.getResources();
	}

	@Override
	public CompositeField createFields() {
		kernelListField = new ListField<Kernel>(null, BasicsUI.NO_INFO) {
			@Override
			public String getText(Kernel element) {
				return element.getName() == null ? "" : element.getName();
			}
		};
		
		kernelListField.setTooltip("List of model's kernels.");
		kernelListField.addAction(new Action.Stub("+", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {
			
			@Override
			public String getTooltip() {
				return "Creates a new kernel.";
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				Kernel kernel = new Kernel();
				kernel.setName(NameUtils.availableName("Kernel1", getSubject().getCodeList()));
				kernel.setContents(Tools.INITIAL_KERNEL);
				getSubject().addCodeAndOpposite(kernel);
				return Action.STATUS_OK;
			}
		});
		kernelListField.addAction(new Action.Stub("-", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {
			
			@Override
			public String getTooltip() {
				return "Removes kernel '" + kernelListField.getSingleSelection().getName() + "'.";
			}
			
			@Override
			public int getVisibility() {
				return kernelListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				// removes kernel from model.
				Kernel kernel = kernelListField.getSingleSelection();
				getSubject().removeCodeAndOpposite(kernel);
				
				// clears tasks of given kernel.
				for ( Task task : getSubject().getScheduler().getTaskList() ) {
				    task.removeKernel(kernel);
				}
				return Action.STATUS_OK;
			}
		});

		
		
		
		kernelListField.addAction(new Action.Stub(null, resources.getImage("bouton_up.png"), Action.STYLE_BUTTON) {
			
			@Override
			public String getTooltip() {
				return "Move kernel up.";
			}
			
			@Override
			public int getVisibility() {
				return kernelListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				getSubject().upKernel(kernelListField.getSingleSelection());
				return Action.STATUS_OK;
			}
		});
		
		kernelListField.addAction(new Action.Stub(null, resources.getImage("bouton_down.png"), Action.STYLE_BUTTON) {
			
			@Override
			public String getTooltip() {
				return "Move kernel down.";
			}
			
			@Override
			public int getVisibility() {
				return kernelListField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				getSubject().downKernel(kernelListField.getSingleSelection());
				return Action.STATUS_OK;
			}
		});			
		
		
		kernelField = new SourceCodeField(null, BasicsUI.NO_INFO);
		kernelField.setEditable(true);
		
		kernelComposite = new BorderField("Kernels", BasicsUI.NONE, kernelField);
		kernelComposite.setWest(kernelListField, 25);
		kernelComposite.addAction(new Action.Stub(null, resources.getImage("bouton_compile.png"), Action.STYLE_BUTTON){
			
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

		return kernelComposite;
	}

	@Override
	public void refreshFields() {
		if ( getSubject() == null ) {
			kernelComposite.setEnable(false);
		} else {
			kernelComposite.setEnable(true);
			kernelListField.setValue(getSubject().getKernelList());
			kernelListField.refresh();
			kernelField.setCode(kernelListField.getSingleSelection());
			studioContext.setSelection(kernelListField.getSingleSelection());
		}

	}

	@Override
	public boolean updateSubject(Field field) {
		if ( field == kernelListField ) {
			kernelField.setCode(kernelListField.getSingleSelection());
			studioContext.setSelection(kernelListField.getSingleSelection());
			return false;
		}
		return false;
	}
	
	public void openFindAndReplaceDialog() {
		kernelField.openSearchAndReplaceShell();
	}
}
