package matrixstudio.ui;

import fr.minibilles.basics.error.Diagnostic;
import fr.minibilles.basics.error.Validator;
import fr.minibilles.basics.progress.ActionMonitor;
import fr.minibilles.basics.ui.BasicsUI;
import fr.minibilles.basics.ui.action.Action;
import fr.minibilles.basics.ui.controller.Controller;
import fr.minibilles.basics.ui.field.CompositeField;
import fr.minibilles.basics.ui.field.Field;
import fr.minibilles.basics.ui.field.ListField;
import fr.minibilles.basics.ui.field.TextField;
import java.text.ParseException;
import java.util.List;
import matrixstudio.formula.EvaluationException;
import matrixstudio.formula.FormulaCache;
import matrixstudio.model.Matrix;
import matrixstudio.model.Model;
import matrixstudio.model.Parameter;
import matrixstudio.ui.controller.FormulaValidator;

/**
 * Controller for a model
 */
public class ModelController extends Controller<Model> {

    private CompositeField compositeField;

    private ListField<Parameter> parametersField;

    private TextField nameField;
    private TextField formulaField;
    private CompositeField editionField;

    @Override
    public CompositeField createFields() {
        parametersField = new ListField<Parameter>("Parameters", BasicsUI.NONE) {
            @Override
            public String getText(Parameter element) {
                StringBuilder text = new StringBuilder();
                text.append(element.getName());
                text.append(" (");
                try {
                    int result = FormulaCache.SHARED.computeValue(element.getFormula(), getSubject());
                    text.append(result);
                } catch (ParseException | EvaluationException e) {
                    text.append(e.getMessage());
                }
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
                        message = parameter.getName()+" is null";
                    } else {
                        Exception e = FormulaCache.SHARED.isFormulaValid(formula);
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
        formulaField.setValidator(new FormulaValidator());

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
                // initialize matrices in case that their size may change
                for (Matrix matrix : getSubject().getMatrixList()) {
                    matrix.initBlank(false);
                }
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
