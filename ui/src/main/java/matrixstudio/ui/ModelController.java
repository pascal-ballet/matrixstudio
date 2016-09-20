package matrixstudio.ui;

import matrixstudio.model.Model;
import matrixstudio.model.Parameter;
import org.xid.basics.error.Diagnostic;
import org.xid.basics.error.Validator;
import org.xid.basics.progress.ActionMonitor;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.action.Action;
import org.xid.basics.ui.controller.Controller;
import org.xid.basics.ui.field.CompositeField;
import org.xid.basics.ui.field.Field;
import org.xid.basics.ui.field.ListField;
import org.xid.basics.ui.field.TextField;

import java.util.List;

/**
 * Controller for a model
 */
public class ModelController extends Controller<Model> {

    private final StudioContext studioContext;

    private CompositeField compositeField;

    private ListField<Parameter> parametersField;

    private TextField nameField;
    private TextField formulaField;
    private CompositeField editionField;

    public ModelController(StudioContext studioContext) {
        this.studioContext = studioContext;
    }

    @Override
    public CompositeField createFields() {
        parametersField = new ListField<Parameter>("Parameters", BasicsUI.NONE) {
            @Override
            public String getText(Parameter element) {
                StringBuilder text = new StringBuilder();
                text.append(element.getName());
                text.append(" (");
                text.append(element.getFormula());
                text.append(")");
                return text.toString();
            }
        };
        parametersField.setNbLines(3);
        parametersField.setValidator(new Validator<List<Parameter>>() {
            String message = null;

            @Override
            public boolean isValid(List<Parameter> value) {
                message = null;
                for (Parameter parameter : value) {
                    String formula = parameter.getFormula();
                    if (formula == null) {
                        message = ""+parameter.getName()+" is null";
                    } else {
                        Exception e = studioContext.getFormulaCache().isFormulaValid(formula);
                        message = e != null ? parameter.getName()+": " + e.getMessage() : null;
                    }
                    if (message != null) return false;
                }
                return true;
            }

            @Override
            public Diagnostic getDiagnostic() {
                return new Diagnostic.Stub(Diagnostic.ERROR, message);
            }
        });

        nameField = new TextField("Name", BasicsUI.NONE);
        formulaField = new TextField("Formula", BasicsUI.NONE);
        formulaField.setValidator(new Validator<String>() {

            String message = null;

            @Override
            public boolean isValid(String value) {
                if (value == null) {
                    message = "Formula can't be null";
                } else {
                    Exception e = studioContext.getFormulaCache().isFormulaValid(value);
                    message = e != null ? e.getMessage() : null;
                }
                return message == null;
            }

            @Override
            public Diagnostic getDiagnostic() {
                return new Diagnostic.Stub(Diagnostic.ERROR, message);
            }
        });

        parametersField.addAction(new Action.Stub("+", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {

            @Override
            public String getTooltip() {
                return "Adds a parameter to the simulation.";
            }

            @Override
            public int run(ActionMonitor monitor) {
                Parameter parameter = new Parameter();
                parameter.setName(NameUtils.availableName("Parameter1", getSubject().getParameterList()));
                parameter.setFormula("10");
                getSubject().addParameterAndOpposite(parameter);
                return Action.STATUS_OK;
            }
        });
        parametersField.addAction(new Action.Stub("-", Action.STYLE_DEFAULT | Action.STYLE_TRANSACTIONNAL) {

            @Override
            public String getTooltip() {
                return "Removes kernel '" + parametersField.getSingleSelection() + "'.";
            }

            @Override
            public int getVisibility() {
                return parametersField.getSingleSelection() != null ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
            }

            @Override
            public int run(ActionMonitor monitor) {
                getSubject().removeParameterAndOpposite(parametersField.getSingleSelection());
                return Action.STATUS_OK;
            }
        });

        editionField = new CompositeField("Selected parameter", BasicsUI.GROUP, nameField, formulaField);
        compositeField = new CompositeField("Parameters", parametersField, editionField);

        return compositeField;
    }

    @Override
    public boolean updateSubject(Field field) {
        if (field == parametersField) {
            Parameter selection = parametersField.getSingleSelection();
            if (selection != null) {
               formulaField.setValue(selection.getFormula());
            }
        }

        Parameter selection = parametersField.getSingleSelection();
        if (selection != null) {
            if (field == nameField) {
                selection.setName(nameField.getValue());
                return true;
            }
            if (field == formulaField) {
                selection.setFormula(formulaField.getValue());
                return true;
            }
        }
        return false;
    }

    @Override
    public void refreshFields() {
        Model subject = getSubject();
        if (subject == null) {
            compositeField.setEnable(false);
        } else {
            compositeField.setEnable(true);
            parametersField.setValue(getSubject().getParameterList());
            parametersField.refresh();

            Parameter selection = parametersField.getSingleSelection();
            editionField.setEnable(selection != null);
            if (selection != null) {
                nameField.setValue(selection.getName());
                formulaField.setValue(selection.getFormula());
            } else {
                nameField.setValue(null);
                formulaField.setValue(null);
            }
        }
    }
}
