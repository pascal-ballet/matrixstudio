package matrixstudio.ui.controller;

import matrixstudio.formula.FormulaParser;
import matrixstudio.model.Model;
import matrixstudio.model.Parameter;
import matrixstudio.ui.NameUtils;
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

import java.text.ParseException;

/**
 * Controller for a model
 */
public class ModelController extends Controller<Model> {

    private CompositeField compositeField;

    private ListField<Parameter> parametersField;

    private TextField editionField;

    @Override
    public CompositeField createFields() {
        parametersField = new ListField<Parameter>("Parameters", BasicsUI.ITEM_EDITABLE) {
            @Override
            public String getText(Parameter element) {
                return element.getName();
            }
        };

        editionField = new TextField("Formula", BasicsUI.NONE);
        editionField.setValidator(new Validator<String>() {

            String message = null;

            @Override
            public boolean isValid(String value) {
                try {
                    message = null;
                    if (value != null) {
                        new FormulaParser(editionField.getValue()).parse();
                    }
                    return true;
                } catch (ParseException e) {
                    message = e.getMessage();
                    return false;
                }
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
                getSubject().addParameter(parameter);
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
                // TODO remove
                return Action.STATUS_OK;
            }
        });

        parametersField.addAction(new Action.Stub("\u2191", Action.STYLE_BUTTON) {

            @Override
            public String getTooltip() {
                return "Move kernel up.";
            }

            @Override
            public int getVisibility() {
                return  parametersField.getSingleSelection() != null &&
                        parametersField.getSingleSelectionIndex() > 0
                        ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
            }

            @Override
            public int run(ActionMonitor monitor) {
                // TODO up
                return Action.STATUS_OK;
            }
        });

        parametersField.addAction(new Action.Stub("\u2193",Action.STYLE_BUTTON) {

            @Override
            public String getTooltip() {
                return "Move kernel down.";
            }

            @Override
            public int getVisibility() {
                return  parametersField.getSingleSelection() != null &&
                        parametersField.getSingleSelectionIndex() < parametersField.getValue().size() - 1
                        ? VISIBILITY_ENABLE : VISIBILITY_DISABLE;
            }

            @Override
            public int run(ActionMonitor monitor) {
                // TODO down
                return Action.STATUS_OK;
            }
        });

        compositeField = new CompositeField("Parameters", parametersField, editionField);

        return compositeField;
    }

    @Override
    public boolean updateSubject(Field field) {
        if (field == parametersField) {
            Parameter selection = parametersField.getSingleSelection();
            if (selection != null) {
               editionField.setValue(selection.getFormula());
            }
        }

        if (field == editionField) {
            Parameter selection = parametersField.getSingleSelection();
            if (selection != null) {
                selection.setFormula(editionField.getValue());
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
        }
    }
}
