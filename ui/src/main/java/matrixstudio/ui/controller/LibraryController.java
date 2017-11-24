package matrixstudio.ui.controller;

import fr.minibilles.basics.error.Diagnostic;
import fr.minibilles.basics.error.Validator;
import fr.minibilles.basics.ui.BasicsUI;
import fr.minibilles.basics.ui.controller.Controller;
import fr.minibilles.basics.ui.field.CompositeField;
import fr.minibilles.basics.ui.field.Field;
import fr.minibilles.basics.ui.field.TextField;
import matrixstudio.model.Library;


public class LibraryController extends Controller<Library> {

	private TextField nameField;
	private CompositeField compositeField;
	
	@Override
	public CompositeField createFields() {

		nameField = new TextField("Name", BasicsUI.NONE);
		nameField.setValidator(new Validator.Stub<String>(Diagnostic.ERROR, "Invalid name") {
			public boolean isValid(String value) {
				if ( value == null ) return false;
				return value.matches("[a-zA-Z_][a-zA-Z0-9_]*");
			}
		});
		compositeField = new CompositeField("Library", BasicsUI.GROUP, nameField);
		return compositeField;
	}

	
	@Override
	public void refreshFields() {
		if ( getSubject() == null ) {
			compositeField.setEnable(false);
		} else {
			compositeField.setEnable(true);
			nameField.setValue(getSubject().getName());
		}
	}
	
	@Override
	public boolean updateSubject(Field field) {
		if ( field == nameField ) {
			getSubject().setName(nameField.getValue());
			return true;
		}
		return false;
	}
}
