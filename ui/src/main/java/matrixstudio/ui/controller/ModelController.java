package matrixstudio.ui.controller;

import matrixstudio.model.Model;
import org.xid.basics.ui.controller.Controller;
import org.xid.basics.ui.field.CompositeField;

/**
 * Controller for a model
 */
public class ModelController extends Controller<Model> {

    private CompositeField compositeField;

    @Override
    public CompositeField createFields() {
        compositeField = new CompositeField("Parameters");
        return compositeField;
    }
}
