package matrixstudio.ui.controller;

import matrixstudio.model.MatrixInteger;

import org.xid.basics.error.Diagnostic;
import org.xid.basics.error.Validator;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.controller.Controller;
import org.xid.basics.ui.field.CheckboxField;
import org.xid.basics.ui.field.CompositeField;
import org.xid.basics.ui.field.Field;
import org.xid.basics.ui.field.TextField;


public class MatrixIntegerController extends Controller<MatrixInteger> {

	private TextField nameField;
	private TextField widthField;
	private TextField heightField;
	private TextField depthField;
	private CheckboxField isRandomField;
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
		widthField.setValidator(new Validator.Stub<String>(Diagnostic.ERROR, "Invalid size") {
			public boolean isValid(String value) {
				if ( value == null ) return false;
				return value.matches("[0-9][0-9]*");
			}
		});
		heightField = new TextField("Size Y", BasicsUI.NONE);
		heightField.setValidator(new Validator.Stub<String>(Diagnostic.ERROR, "Invalid size") {
			public boolean isValid(String value) {
				if ( value == null ) return false;
				return value.matches("[0-9][0-9]*");
			}
		});
		depthField = new TextField("Size Z", BasicsUI.NONE);
		depthField.setValidator(new Validator.Stub<String>(Diagnostic.ERROR, "Invalid size") {
			public boolean isValid(String value) {
				if ( value == null ) return false;
				return value.matches("[0-9][0-9]*");
			}
		});
		isRandomField  = new CheckboxField("Random", BasicsUI.NONE);
		
		compositeField = new CompositeField("Integer Matrix", BasicsUI.GROUP,  nameField, widthField, heightField, depthField, isRandomField);
		return compositeField;
	}

	
	@Override
	public void refreshFields() {
		if ( getSubject() == null ) {
			compositeField.setEnable(false);
		} else {
			compositeField.setEnable(true);
			nameField.setValue(getSubject().getName());
			widthField.setValue(""+getSubject().getSizeX());
			heightField.setValue(""+getSubject().getSizeY());
			depthField.setValue(""+getSubject().getSizeZ());
			isRandomField.setValue(getSubject().isRandom());
		}
	}
	
	@Override
	public boolean updateSubject(Field field) {
		if ( field == nameField ) {
			getSubject().setName(nameField.getValue());
			return true;
		}
		if ( field == widthField ) {
			getSubject().setSizeX(widthField.getIntValue());
			getSubject().initBlank();
			return true;
		}
		if ( field == heightField ) {
			getSubject().setSizeY(heightField.getIntValue());
			getSubject().initBlank();
			return true;
		}
		if ( field == depthField ) {
			getSubject().setSizeZ(depthField.getIntValue());
			getSubject().initBlank();
			return true;
		}
		if ( field == isRandomField ) {
			getSubject().setRandom(isRandomField.getValue());
			getSubject().initBlank();
			return true;
		}
		return super.updateSubject(field);
	}
}
