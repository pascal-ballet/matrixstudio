package matrixstudio.ui.controller;

import fr.minibilles.basics.error.Diagnostic;
import fr.minibilles.basics.error.Validator;
import fr.minibilles.basics.ui.BasicsUI;
import fr.minibilles.basics.ui.controller.Controller;
import fr.minibilles.basics.ui.field.CheckboxField;
import fr.minibilles.basics.ui.field.CompositeField;
import fr.minibilles.basics.ui.field.Field;
import fr.minibilles.basics.ui.field.TextField;
import matrixstudio.model.MatrixInteger;


public class MatrixIntegerController extends Controller<MatrixInteger> {

	private TextField nameField;
	private TextField widthField;
	private TextField heightField;
	private TextField depthField;
	private CheckboxField isRandomField;
	private CheckboxField isARGB;
	private CheckboxField isRainbow;
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
		widthField = new TextField("Size X", BasicsUI.NONE);
        widthField.setValidator(new FormulaValidator());

        heightField = new TextField("Size Y", BasicsUI.NONE);
        heightField.setValidator(new FormulaValidator());

        depthField = new TextField("Size Z", BasicsUI.NONE);
        heightField.setValidator(new FormulaValidator());

        isRandomField  	= new CheckboxField("Random", BasicsUI.NONE);
		isARGB  		= new CheckboxField("ARGB", BasicsUI.NONE);
		isRainbow  		= new CheckboxField("Rainbow", BasicsUI.NONE);

		compositeField = new CompositeField("Integer Matrix", BasicsUI.GROUP,  nameField, widthField, heightField, depthField, isRandomField, isARGB, isRainbow);
		return compositeField;
	}

	
	@Override
	public void refreshFields() {
		if ( getSubject() == null ) {
			compositeField.setEnable(false);
		} else {
			compositeField.setEnable(true);
			nameField.setValue(getSubject().getName());
			widthField.setValue(getSubject().getSizeX());
			heightField.setValue(getSubject().getSizeY());
			depthField.setValue(getSubject().getSizeZ());
			isRandomField.setValue(getSubject().isRandom());
			isARGB.setValue(getSubject().isARGB());
			isRainbow.setValue(getSubject().isRainbow());
		}
	}
	
	@Override
	public boolean updateSubject(Field field) {
		if ( field == nameField ) {
			getSubject().setName(nameField.getValue());
			return true;
		}
		if ( field == widthField ) {
			getSubject().setSizeX(widthField.getValue());
			getSubject().initBlank(false);
			return true;
		}
		if ( field == heightField ) {
			getSubject().setSizeY(heightField.getValue());
			getSubject().initBlank(false);
			return true;
		}
		if ( field == depthField ) {
			getSubject().setSizeZ(depthField.getValue());
			getSubject().initBlank(false);
			return true;
		}
		if ( field == isRandomField ) {
			getSubject().setRandom(isRandomField.getValue());
			getSubject().initBlank(true);
			return true;
		}
		if ( field == isARGB ) {
			getSubject().setARGB(isARGB.getValue());
			return true;
		}
		if ( field == isRainbow) {
			getSubject().setRainbow(isRainbow.getValue());
			return true;
		}
		return super.updateSubject(field);
	}
}
