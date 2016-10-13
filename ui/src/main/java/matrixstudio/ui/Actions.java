package matrixstudio.ui;

import matrixstudio.model.Kernel;
import matrixstudio.model.Matrix;
import matrixstudio.model.Task;
import matrixstudio.ui.controller.FormulaValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.xid.basics.Basics;
import org.xid.basics.error.Diagnostic;
import org.xid.basics.error.Validator;
import org.xid.basics.model.ChangeHandler;
import org.xid.basics.notification.Notification;
import org.xid.basics.notification.NotificationListener;
import org.xid.basics.progress.ActionMonitor;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.action.Action;
import org.xid.basics.ui.action.KeyCode;
import org.xid.basics.ui.dialog.FieldDialog;
import org.xid.basics.ui.field.CompositeField;
import org.xid.basics.ui.field.ListField;
import org.xid.basics.ui.field.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Actions {

	private final MatrixStudio ui;
	private final Map<String, Action> actions = new HashMap<String, Action>();
	
	private final Action separator = new Action.Stub(Action.STYLE_DEFAULT | Action.STYLE_SEPARATOR);

	public Actions(MatrixStudio ui) {
		this.ui = ui;
		createMenuBarActions();
	}
	
	private Image getImage(String filename) {
		return ui.getResources().getImage(filename);
	}
	
	private ChangeHandler getChangeRecorder() {
		return ui.getModel().getChangeRecorder();
	}
	
	public Action getAction(String id) {
		return actions.get(id);
	}
	
	public Action getMenuBarAction() {
		return getAction("MenuBar");
	}

	private void addAction(String id, Action action) {
		actions.put(id, action);
	}
	
	private void createMenuBarActions() {
		createFileAction();
		createMatrixAction();
		createTaskAction();
		createEditAction();

        addAction("MenuBar",
                new Action.Container("MenuBar",
                        getAction("File"),
                        getAction("Edit"),
                        getAction("Matrix"),
						getAction("Task")
                        )
                );

	}
	
	private void createFileAction() {
		
		KeyCode newSimulationCode = new KeyCode(KeyCode.MOD1, 'n');
		addAction("NewSimulation", new Action.Stub("New Simulation", getImage("eclipse/new.gif"), newSimulationCode) {
			@Override
			public String getTooltip() {
				return "Create a new simulation.";
			}	
			@Override
			public int run(ActionMonitor monitor) {
				MessageBox box = new MessageBox(ui.getShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				box.setText("MatrixStudio");
				box.setMessage("You are about to reset your simulation, are you sure ?");
				if ( box.open() == SWT.OK ) {
					ui.stopAllExports();
					ui.getSimulator().stop();
					ui.resetModel();
					ui.refreshFields();
					return Action.STATUS_OK;
				}
				return Action.STATUS_CANCEL;
			}
		});

		KeyCode openSimulationCode = new KeyCode(KeyCode.MOD1, 'o');
		addAction("OpenSimulation", new Action.Stub("Open Simulation\u2026", getImage("eclipse/open_edit.gif"), openSimulationCode){
			@Override
			public String getTooltip() {
				return "Open a simulation.";
			}				
			@Override
			public int run(ActionMonitor monitor) {
				ui.loadModel(false);
				return Action.STATUS_OK;
			}
		});

		addAction("SafeOpenSimulation", new Action.Stub("Safe Open Simulation \u2026", getImage("eclipse/open_edit.gif")){
			@Override
			public String getTooltip() {
				return "Open a simulation in a safe mode.";
			}
			@Override
			public int run(ActionMonitor monitor) {
				ui.loadModel(true);
				return Action.STATUS_OK;
			}
		});

		KeyCode saveSimulationCode = new KeyCode(KeyCode.MOD1, 's');
		addAction("SaveSimulation", new Action.Stub("Save Simulation", getImage("eclipse/save_edit.gif"), saveSimulationCode){
			@Override
			public String getTooltip() {
				return "Save the simulation";
			}
			@Override
			public int run(ActionMonitor monitor) {
				ui.saveModel(false);
				return Action.STATUS_OK;
			}
		});

		KeyCode saveAsSimulationCode = new KeyCode(KeyCode.MOD1, KeyCode.SHIFT, 's');
		addAction("SaveSimulationAs", new Action.Stub("Save Simulation As\u2026", getImage("eclipse/save_edit.gif"), saveAsSimulationCode){
			@Override
			public String getTooltip() {
				return "Save the simulation as...";
			}
			@Override
			public int run(ActionMonitor monitor) {
				ui.saveModel(true);
				return Action.STATUS_OK;
			}
		});
		
		addAction("About", new Action.Stub("About MatrixStudio\u2026"){
			public int run(ActionMonitor monitor) {
				String title = "Matrix Studio";
				TextField field = new TextField(null, BasicsUI.NO_INFO | BasicsUI.READ_ONLY);
				field.setValue("Matrix Studio 2009-2013.");
				FieldDialog dialog = new FieldDialog("MatrixStudio", title, BasicsUI.NO_AUTOMATIC_VALIDATION, field);
				dialog.setInitialMessage("Pascal Ballet - Lab-STICC - European University of Brittany - UBO.\nJean-Charles Roger.");
				dialog.setBannerImage(Actions.this.getImage("MatrixStudio-128.png"));
				dialog.setButtonLabel(new String[] { "Ok" });
				dialog.open();
				return STATUS_OK;
			}
		});
		
		
		addAction("Quit", new Action.Stub("Quit"){
			public int run(ActionMonitor monitor) {
				ui.getShell().dispose();
				return STATUS_OK;
			}
		});
		
		if ( Basics.isMac() ) {
			addAction("File", new Action.Container("File", 
					getAction("NewSimulation"), 
					separator,
					getAction("OpenSimulation"), 
					getAction("SafeOpenSimulation"),
					separator,
					getAction("SaveSimulation"),
					getAction("SaveSimulationAs")
				));
		} else {
			addAction("File", new Action.Container("File",
					getAction("NewSimulation"),
					separator,
					getAction("OpenSimulation"),
					getAction("SafeOpenSimulation"),
					separator,
					getAction("SaveSimulation"),
					getAction("SaveSimulationAs"),
					separator,
					getAction("About"),
					separator,
					getAction("Quit")
					));
			
		}
	}

	private void createEditAction() {
		
		KeyCode undoCode = new KeyCode(KeyCode.MOD1, 'z');
		addAction("Undo", new Action.Stub("Undo", getImage("eclipse/undo_edit.gif"), undoCode) {
			@Override
			public int getVisibility() {
				return getChangeRecorder().canUndo() ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				getChangeRecorder().undo();
				return STATUS_OK;
			}
		});
		
		KeyCode redoCode = new KeyCode(KeyCode.MOD1, KeyCode.SHIFT, 'z');
		addAction("Redo", new Action.Stub("Redo", getImage("eclipse/redo_edit.gif"), redoCode) {
			@Override
			public int getVisibility() {
				return getChangeRecorder().canRedo()  ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				getChangeRecorder().redo();
				return STATUS_OK;
			}
		});
		
		KeyCode findAndReplaceCode = new KeyCode(KeyCode.MOD1, 'f');
		addAction("FindAndReplace", new Action.Stub("Find/Replace", findAndReplaceCode) {
			@Override
			public int getVisibility() {
				return ui.isTextEditing() ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			
			@Override
			public int run(ActionMonitor monitor) {
				ui.openFindAndReplaceDialog();
				return STATUS_OK;
			}
		});
		
		addAction("Edit", new Action.Container("Edit", getAction("Undo"), getAction("Redo"), separator, getAction("FindAndReplace")));
		
		
	}

	private void createMatrixAction() {

		addAction("Resize", new Action.Stub("Global resize\u2026", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {

			@Override
			public int getVisibility() {
				return ui.getModel().getMatrixCount() > 0 ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			@Override
			public int run(ActionMonitor monitor) {
				final Shell parent = ui.getShell();

				final ListField<Matrix> matrixField = new ListField<Matrix>(null, ui.getModel().getMatrixList(), BasicsUI.CHECK) {
                    public String getText(Matrix element) {
                        StringBuilder text = new StringBuilder();
                        text.append(element.getName() == null ? "" : element.getName());
                        text.append(" [");
                        text.append(element.getSizeX());
                        text.append(",");
                        text.append(element.getSizeY());
                        text.append(",");
                        text.append(element.getSizeZ());
                        text.append("]");
                        return text.toString();
                    }

                };
				matrixField.setValidator(new Validator.Stub<List<Matrix>>(Diagnostic.ERROR, "You must check at least one matrix") {
					public boolean isValid(List<Matrix> value) {
						return matrixField.getChecked().size() > 0;
					}
				});
                matrixField.setChecked(ui.getModel().getMatrixList());

				final TextField xField = new TextField("New X");
				xField.setValidator(new FormulaValidator());
				xField.setEnable(false);
				final Action.Stub xFieldEnable = new Action.Stub(Action.STYLE_BUTTON | Action.STYLE_BOOLEAN_STATE) {
					@Override
					public boolean getState() {
						return xField.isEnable();
					}

					@Override
					public int run(ActionMonitor monitor) {
						xField.setEnable(!xField.isEnable());
						return STATUS_OK;
					}
				};
				xField.addAction(xFieldEnable);

				final TextField yField = new TextField("New Y");
				yField.setValidator(new FormulaValidator());
				yField.setEnable(false);
				final Action.Stub yFieldEnable = new Action.Stub(Action.STYLE_BUTTON | Action.STYLE_BOOLEAN_STATE) {
					@Override
					public boolean getState() {
						return yField.isEnable();
					}

					@Override
					public int run(ActionMonitor monitor) {
						yField.setEnable(!yField.isEnable());
						return STATUS_OK;
					}
				};
				yField.addAction(yFieldEnable);


				final TextField zField = new TextField("New Z");
				zField.setValidator(new FormulaValidator());
				zField.setEnable(false);

				final Action.Stub zFieldEnable = new Action.Stub(Action.STYLE_BUTTON | Action.STYLE_BOOLEAN_STATE) {
					@Override
					public boolean getState() {
						return zField.isEnable();
					}

					@Override
					public int run(ActionMonitor monitor) {
						zField.setEnable(!zField.isEnable());
						return STATUS_OK;
					}
				};
				zField.addAction(zFieldEnable);

				matrixField.addListener(new NotificationListener() {

					public void notified(Notification notification) {
						if ( matrixField.getChecked().size() > 0 ) {
							Matrix matrix = matrixField.getChecked().get(0);
							if ( xField.getValue() == null ) xField.setValue(matrix.getSizeX());
							if ( yField.getValue() == null ) yField.setValue(matrix.getSizeY());
							if ( zField.getValue() == null ) zField.setValue(matrix.getSizeZ());
						}
					}
				});

				CompositeField sizeField = new CompositeField("Size", BasicsUI.GROUP, xField, yField, zField);
				CompositeField mainField = new CompositeField(matrixField, sizeField);

				FieldDialog dialog = new FieldDialog(parent, parent.getText(), "Resize multiple matrices", BasicsUI.NONE, mainField);
				if ( dialog.open() != 0 ) return STATUS_CANCEL;


				final String xFieldValue = xField.getValue();
				final String yFieldValue = yField.getValue();
				final String zFieldValue = zField.getValue();
				for ( Matrix matrix : matrixField.getChecked() ) {
					boolean changed = false;
					if (xField.isEnable() && !matrix.getSizeX().equals(xFieldValue)) {
						changed = true;
						matrix.setSizeX(xFieldValue);
					}
					if (yField.isEnable() && !matrix.getSizeY().equals(yFieldValue)) {
						changed = true;
						matrix.setSizeY(yFieldValue);
					}
					if (zField.isEnable() && !matrix.getSizeZ().equals(zFieldValue)) {
						changed = true;
						matrix.setSizeZ(zFieldValue);
					}
					if (changed) matrix.initBlank(false);
				}

				return STATUS_OK;
			}
		});

		addAction("Matrix", new Action.Container("Matrix",
				getAction("Resize")
		));

	}

	private void createTaskAction() {
		
		addAction("Resize", new Action.Stub("Global resize\u2026", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {
			
			@Override
			public int getVisibility() {
				return ui.getModel().getScheduler().getTaskCount() > 0 ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
			}
			@Override
			public int run(ActionMonitor monitor) {
				final Shell parent = ui.getShell();
				
				final ListField<Task> taskField = new ListField<Task>(null, ui.getModel().getScheduler().getTaskList(), BasicsUI.CHECK) {
					public String getText(Task element) {
                        StringBuilder name = new StringBuilder();
                        if (element.getKernelCount() > 0) {
                            for (Kernel kernel : element.getKernelList()) {
                                if (name.length() > 0) name.append(",");
                                name.append(kernel.getName());
                            }

                        } else {
                            name.append("[No kernel]");
                        }

                        StringBuilder text = new StringBuilder();
						text.append(name);
						text.append(" [");
						text.append(element.getGlobalWorkSizeX());
						text.append(",");
						text.append(element.getGlobalWorkSizeY());
						text.append(",");
						text.append(element.getGlobalWorkSizeZ());
						text.append("]");
						return text.toString();
					}
				};
				taskField.setValidator(new Validator.Stub<List<Task>>(Diagnostic.ERROR, "You must check at least one task") {
					public boolean isValid(List<Task> value) {
						return taskField.getChecked().size() > 0;
					}
				});
                taskField.setChecked(ui.getModel().getScheduler().getTaskList());

                final TextField xField = new TextField("New Workspace X");
                xField.setValidator(new FormulaValidator());
				xField.setEnable(false);
				final Action.Stub xFieldEnable = new Action.Stub(Action.STYLE_BUTTON | Action.STYLE_BOOLEAN_STATE) {
					@Override
					public boolean getState() {
						return xField.isEnable();
					}

					@Override
					public int run(ActionMonitor monitor) {
						xField.setEnable(!xField.isEnable());
						return STATUS_OK;
					}
				};
				xField.addAction(xFieldEnable);

				final TextField yField = new TextField("New Workspace Y");
                yField.setValidator(new FormulaValidator());
				yField.setEnable(false);
				final Action.Stub yFieldEnable = new Action.Stub(Action.STYLE_BUTTON | Action.STYLE_BOOLEAN_STATE) {
					@Override
					public boolean getState() {
						return yField.isEnable();
					}

					@Override
					public int run(ActionMonitor monitor) {
						yField.setEnable(!yField.isEnable());
						return STATUS_OK;
					}
				};
				yField.addAction(yFieldEnable);


				final TextField zField = new TextField("New Workspace Z");
                zField.setValidator(new FormulaValidator());
				zField.setEnable(false);
				
				final Action.Stub zFieldEnable = new Action.Stub(Action.STYLE_BUTTON | Action.STYLE_BOOLEAN_STATE) {
					@Override
					public boolean getState() {
						return zField.isEnable();
					}
					
					@Override
					public int run(ActionMonitor monitor) {
						zField.setEnable(!zField.isEnable());
						return STATUS_OK;
					}
				};
				zField.addAction(zFieldEnable);
				
				taskField.addListener(new NotificationListener() {
					
					public void notified(Notification notification) {
						if ( taskField.getChecked().size() > 0 ) {
							Task task = taskField.getChecked().get(0);
							if ( xField.getValue() == null ) xField.setValue(task.getGlobalWorkSizeX());
							if ( yField.getValue() == null ) yField.setValue(task.getGlobalWorkSizeY());
							if ( zField.getValue() == null ) zField.setValue(task.getGlobalWorkSizeZ());
						}
					}
				});
				
				CompositeField sizeField = new CompositeField("Size", BasicsUI.GROUP, xField, yField, zField);
				CompositeField mainField = new CompositeField(taskField, sizeField);

				FieldDialog dialog = new FieldDialog(parent, parent.getText(), "Resize multiple tasks", BasicsUI.NONE, mainField);
				if ( dialog.open() != 0 ) return STATUS_CANCEL;


                for ( Task task : taskField.getChecked() ) {
                    boolean changed = false;
					if (xField.isEnable() && task.getGlobalWorkSizeX() != xField.getValue()) {
                        changed = true;
                        task.setGlobalWorkSizeX(xField.getValue());
                    }
					if (yField.isEnable() && task.getGlobalWorkSizeY() != yField.getValue()) {
                        changed = true;
                        task.setGlobalWorkSizeY(yField.getValue());
                    }
					if (zField.isEnable() && task.getGlobalWorkSizeZ() != zField.getValue()) {
                        changed = true;
                        task.setGlobalWorkSizeZ(zField.getValue());
                    }
                    //if (changed) task.initBlank();
				}
				
				return STATUS_OK;
			}
		});

		addAction("Task", new Action.Container("Task",
				getAction("Resize")
			));


	}
}
