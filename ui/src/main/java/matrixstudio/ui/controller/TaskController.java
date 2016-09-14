package matrixstudio.ui.controller;

import matrixstudio.model.Kernel;
import matrixstudio.model.Task;
import org.xid.basics.error.Diagnostic;
import org.xid.basics.error.Validator;
import org.xid.basics.progress.ActionMonitor;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.action.Action;
import org.xid.basics.ui.controller.Controller;
import org.xid.basics.ui.field.ChoiceField;
import org.xid.basics.ui.field.CompositeField;
import org.xid.basics.ui.field.Field;
import org.xid.basics.ui.field.ListField;
import org.xid.basics.ui.field.TextField;

import java.util.Arrays;
import java.util.List;


public class TaskController extends Controller<Task> {

	private TextField repetitionField;

	private ListField<Kernel> kernelListField;
	private ChoiceField<Kernel> kernelChoiceField;

	private TextField positionField;
	private TextField globalWorkSizeXField;
	private TextField globalWorkSizeYField;
	private TextField globalWorkSizeZField;

	private CompositeField compositeField;
	
	@Override
	public CompositeField createFields() {

        repetitionField = new TextField("Repetition", BasicsUI.NONE);
        repetitionField.setValidator(new Validator.Stub<String>(Diagnostic.ERROR, "Invalid repetition") {
            public boolean isValid(String value) {
                if ( value == null ) return false;
                return value.matches("[0-9][0-9]*");
            }
        });

		kernelListField = new ListField<Kernel>("Kernels", BasicsUI.NONE) {
			@Override
			public String getText(Kernel element) {
				return element.getName();
			}
		};
		kernelListField.setTooltip("Task's kernel list.");
        kernelListField.setNbLines(3);
		kernelListField.setValidator(new Validator.Stub<List<Kernel>>(Diagnostic.ERROR, "Task's kernel list can't be empty.") {
			public boolean isValid(List<Kernel> value) {
				return value != null && value.size() > 0;
			}
		});

        kernelChoiceField = new ChoiceField<Kernel>("Kernel", BasicsUI.NONE) {
            @Override
            public String getText(Kernel element) {
                return element.getName();
            }
        };

        kernelListField.addAction(new Action.Stub("+", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {

            @Override
            public String getTooltip() {
                return "Adds a kernel to the task.";
            }

            @Override
            public int getVisibility() {
                return kernelChoiceField.getValue() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
            }

            @Override
            public int run(ActionMonitor monitor) {
                getSubject().addKernel(kernelChoiceField.getValue());
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
                getSubject().removeKernel(kernelListField.getSingleSelection());
                return Action.STATUS_OK;
            }
        });

        kernelListField.addAction(new Action.Stub("\u2191", Action.STYLE_BUTTON) {

            @Override
            public String getTooltip() {
                return "Move kernel up.";
            }

            @Override
            public int getVisibility() {
                return  kernelListField.getSingleSelection() != null &&
                        kernelListField.getSingleSelectionIndex() > 0
                            ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
            }

            @Override
            public int run(ActionMonitor monitor) {
                getSubject().upKernel(kernelListField.getSingleSelectionIndex());
                return Action.STATUS_OK;
            }
        });

        kernelListField.addAction(new Action.Stub("\u2193",Action.STYLE_BUTTON) {

            @Override
            public String getTooltip() {
                return "Move kernel down.";
            }

            @Override
            public int getVisibility() {
                return  kernelListField.getSingleSelection() != null &&
                        kernelListField.getSingleSelectionIndex() < kernelListField.getValue().size() - 1
                            ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
            }

            @Override
            public int run(ActionMonitor monitor) {
                getSubject().downKernel(kernelListField.getSingleSelectionIndex());
                return Action.STATUS_OK;
            }
        });


        globalWorkSizeXField = new TextField("Global work size X", BasicsUI.NONE);
		globalWorkSizeXField.setValidator(new Validator.Stub<String>(Diagnostic.ERROR, "Invalid size") {
			public boolean isValid(String value) {
				if ( value == null ) return false;
				return value.matches("[0-9][0-9]*");
			}
		});
		globalWorkSizeYField = new TextField("Global work size Y", BasicsUI.NONE);
		globalWorkSizeYField.setValidator(new Validator.Stub<String>(Diagnostic.ERROR, "Invalid size") {
			public boolean isValid(String value) {
				if ( value == null ) return false;
				return value.matches("[0-9][0-9]*");
			}
		});
		globalWorkSizeZField = new TextField("Global work size Z", BasicsUI.NONE);
		globalWorkSizeZField.setValidator(new Validator.Stub<String>(Diagnostic.ERROR, "Invalid size") {
			public boolean isValid(String value) {
				if ( value == null ) return false;
				return value.matches("[0-9][0-9]*");
			}
		});
		positionField = new TextField("Position", BasicsUI.READ_ONLY);
		
		compositeField = new CompositeField("Task", BasicsUI.GROUP, repetitionField, kernelListField, kernelChoiceField, globalWorkSizeXField, globalWorkSizeYField, globalWorkSizeZField);
		return compositeField;
		
	}

	
	@Override
	public void refreshFields() {
		if ( getSubject() == null ) {
			compositeField.setEnable(false);
		} else {
			compositeField.setEnable(true);

            String repetition = getSubject().getRepetition() <= 0 ? "" : Integer.toString(getSubject().getRepetition());
            repetitionField.setValue(repetition);

            kernelListField.setValue(getSubject().getKernelList());
            kernelChoiceField.setValue(kernelListField.getSingleSelection());

            List<Kernel> allKernels = getSubject().getScheduler().getModel().getKernelList();
            kernelChoiceField.setRange(allKernels);
            if (kernelChoiceField.getValue() == null && allKernels.size() > 0) {
                kernelChoiceField.setValue(allKernels.get(0));
            }

			globalWorkSizeXField.setValue(""+getSubject().getGlobalWorkSizeX());
			globalWorkSizeYField.setValue(""+getSubject().getGlobalWorkSizeY());
			globalWorkSizeZField.setValue(""+getSubject().getGlobalWorkSizeZ());
			StringBuilder position = new StringBuilder();
			position.append(Arrays.toString(getSubject().getPosition()));
			positionField.setValue(position.toString());
		}
	}
	
	@Override
	public boolean updateSubject(Field field) {
        if ( field == repetitionField ) {
            getSubject().setRepetition(repetitionField.getIntValue());
            return true;
        }
	    if (field == kernelChoiceField && kernelListField.getSingleSelection() != null) {
            getSubject().setKernel(kernelListField.getSingleSelectionIndex(), kernelChoiceField.getValue());
	        return true;
        }
		if ( field == globalWorkSizeXField ) {
			getSubject().setGlobalWorkSizeX(globalWorkSizeXField.getIntValue());
			return true;
		}
		if ( field == globalWorkSizeYField ) {
			getSubject().setGlobalWorkSizeY(globalWorkSizeYField.getIntValue());
			return true;
		}
		if ( field == globalWorkSizeZField ) {
			getSubject().setGlobalWorkSizeZ(globalWorkSizeZField.getIntValue());
			return true;
		}
		return super.updateSubject(field);
	}
}
