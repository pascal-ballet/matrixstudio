package matrixstudio.model;

import org.xid.basics.serializer.BoostObject;
import org.xid.basics.serializer.Boost;
import org.xid.basics.model.ModelObject;
import org.xid.basics.model.ChangeRecorder;
import matrixstudio.model.Named;
import matrixstudio.model.ModelVisitor;
import matrixstudio.model.Model;
import matrixstudio.kernel.Tools;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;


public abstract class Code implements ModelObject, BoostObject, Named {

	private Model model;

	private String contents = "";

	private String name;

	public Code() {
	}

	protected Code(Boost boost) {
		boost.register(this);
		model = boost.readObject(Model.class);
		contents = boost.readString();
		name = boost.readString();
	}

	/**
	 * <p>Gets model.</p>
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * <p>Sets model.</p>
	 */
	public void setModel(Model newValue) {
		if (model == null ? newValue != null : (model.equals(newValue) == false)) {
			getChangeRecorder().recordChangeAttribute(this, "model", this.model);
			this.model= newValue;
		}
	}

	/**
	 * <p>Gets contents.</p>
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * <p>Sets contents.</p>
	 */
	public void setContents(String newValue) {
		if (contents == null ? newValue != null : (contents.equals(newValue) == false)) {
			getChangeRecorder().recordChangeAttribute(this, "contents", this.contents);
			this.contents= newValue;
		}
	}

	/**
	 * <p>Gets name.</p>
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Sets name.</p>
	 */
	public void setName(String newValue) {
		if (name == null ? newValue != null : (name.equals(newValue) == false)) {
			getChangeRecorder().recordChangeAttribute(this, "name", this.name);
			this.name= newValue;
		}
	}

	public String getWholeContents() {
		// TODO implement getWholeContents(...)
		throw new UnsupportedOperationException();
	}

	public void setWholeContents(String newContents) {
		// TODO implement setWholeContents(...)
		throw new UnsupportedOperationException();
	}

	/**
	 * <p><b>getNumberOfLines</b>: Returns the number of lines in {@link #getWholeContents()}.</p>
	 */
	public int getNumberOfLines() {
		if ( contents == null ) return 0;
		int count = 0;
				
		String contents = getWholeContents();
		int current = contents.indexOf('\n');
		while (current != -1 ) {
			count ++;
			current = contents.indexOf('\n', current+1);
		}
		return count;
	}

	/**
	 * <p><b>getGlobalLineIndex</b>: Returns the line number where is included this inside the whole OpenCL code.</p>
	 */
	public int getGlobalLineIndex() {
		int count = 1;
		for ( Code code : model.getCodeList() ) {
			if ( code instanceof Library ) {
				if ( code == this ) return count;	
				count += code.getNumberOfLines();
			}
		}
		for ( Code code : model.getCodeList() ) {
			if ( code instanceof Kernel ) {
				if ( code == this ) return count;	
				count += code.getNumberOfLines();
			}
		}
		return count;
	}

	/**
	 * <p><b>getFunctionNames</b>: Returns the list of function names in this.</p>
	 */
	public List<String> getFunctionNames() {
		List<String> functionNames = new ArrayList<String>();
		if ( getContents() != null ) {
			for ( String line : getContents().split("\n") ) {
				Matcher matcher = Tools.functionPrototypePattern.matcher(line);
				if ( matcher.matches() ) {
					functionNames.add(matcher.group(2));
				}
			}
		}
		return functionNames;
	}

	public void writeToBoost(Boost boost) {
		boost.writeObject(model);
		boost.writeString(contents);
		boost.writeString(name);
	}

	/**
	 * Visitor accept method.
	 */
	public abstract void accept(ModelVisitor visitor);
	

	public ChangeRecorder getChangeRecorder() {
		if ( getModel() != null ) {
			return getModel().getChangeRecorder();
		}
		return ChangeRecorder.Stub;
	}

}

