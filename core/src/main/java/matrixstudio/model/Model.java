package matrixstudio.model;

import fr.minibilles.basics.model.ModelChangeRecorder;
import fr.minibilles.basics.model.ModelObject;
import fr.minibilles.basics.serializer.Boost;
import fr.minibilles.basics.serializer.BoostObject;
import fr.minibilles.basics.serializer.BoostUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import matrixstudio.kernel.MSBoost;

public class Model implements ModelObject, BoostObject {

	/**
	 * <p>code field.</p>
	 */
	private final List<Code> codeList = new ArrayList<>();

	/**
	 * <p>matrix field.</p>
	 */
	private final List<Matrix> matrixList = new ArrayList<>();

    /**
     * <p>parameter field.</p>
     */
    private final List<Parameter> parameterList = new ArrayList<>();

	private Scheduler scheduler;

	public Model() {
	}

	protected Model(MSBoost boost) {
		boost.register(this);
        int version = boost.getFileVersion();
        for ( Code oneChild : BoostUtil.readObjectList(boost, Code.class) ) {
			codeList.add(oneChild);
		}
		if (boost.isSafeRead()) {
			boost.endObject();
			scheduler = new Scheduler();
			scheduler.setModel(this);
		} else {
			for (Matrix oneChild : BoostUtil.readObjectList(boost, Matrix.class)) {
				matrixList.add(oneChild);
			}
			if (version >= 3) {
				for (Parameter oneChild : BoostUtil.readObjectList(boost, Parameter.class)) {
					parameterList.add(oneChild);
				}
			}
			scheduler = boost.readObject(Scheduler.class);
		}
	}

	/**
	 * <p>Returns all values of code.</p>
	 */
	public List<Code> getCodeList() {
		return Collections.unmodifiableList(codeList);
	}

	/**
	 * <p>Gets code object count.</p>
	 */
	public int getCodeCount() {
		return codeList.size();
	}

	/**
	 * <p>Gets code at given index.</p>
	 */
	public Code getCode(int index) {
		if ( index < 0 || index >= getCodeCount() ) { return null; }
		return codeList.get(index);
	}

	/**
	 * <p>Adds an object in code.</p>
	 */
	public void addCode(Code newValue) {
		addCode(getCodeCount(), newValue);
	}

	/**
	 * <p>Adds an object in code at given index.</p>
	 */
	public void addCode(int index, Code newValue) {
		getChangeRecorder().recordAddObject(this, "code", index);
		codeList.add(index, newValue);
	}

	/**
	 * <p>Replaces an object in code at given index. Returns the old value.</p>
	 */
	public Code setCode(int index, Code newValue) {
		Code oldValue = codeList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "code", index, oldValue);
		getChangeRecorder().recordAddObject(this, "code", index);
		return oldValue;
	}

	/**
	 * <p>Adds a collection of objects in code.</p>
	 */
	public void addAllCode(Collection<Code> toAddList) {
		for (Code newValue : toAddList) {
			addCode(getCodeCount(), newValue);
		}
	}

	/**
	 * <p>Removes given object from code.</p>
	 */
	public void removeCode(Code value) {
		int index = codeList.indexOf(value);
		if (index >= 0 ) {
			removeCode(index);
		}
	}

	/**
	 * <p>Removes object from code at given index.</p>
	 */
	public void removeCode(int index) {
		Code oldValue = codeList.get(index);
		getChangeRecorder().recordRemoveObject(this, "code", index, oldValue);
		codeList.remove(index);
	}

	/**
	 * <p>Adds object to code and sets the corresponding model.</p>
	 */
	public void addCodeAndOpposite(Code newValue) {
		addCode(newValue);
		if ( newValue != null ) {
			newValue.setModel(this);
		}
	}

	/**
	 * <p>Adds a collection of objects to code and sets the corresponding model.</p>
	 */
	public void addAllCodeAndOpposite(Collection<Code> toAddList) {
		for (Code newValue : toAddList) {
			addCodeAndOpposite(getCodeCount(), newValue);
		}
	}

	/**
	 * <p>Adds object to code at given index and sets the corresponding model.</p>
	 */
	public void addCodeAndOpposite(int index, Code newValue) {
		addCode(index, newValue);
		if ( newValue != null ) {
			newValue.setModel(this);
		}
	}

	/**
	 * <p>Replaces an object in code at given index. Returns the old value.</p>
	 */
	public Code setCodeAndOpposite(int index, Code newValue) {
		Code oldValue = codeList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "code", index, oldValue);
		getChangeRecorder().recordAddObject(this, "code", index);
		if ( newValue != null ) {
			newValue.setModel(this);
		}
		return oldValue;
	}

	/**
	 * <p>Removes object from code and resets the corresponding model.</p>
	 */
	public void removeCodeAndOpposite(Code removed) {
		removeCode(removed);
		if ( removed != null ) {
			removed.setModel(null);
		}
	}

	/**
	 * <p>Removes object at given index from code and resets the corresponding model.</p>
	 */
	public void removeCodeAndOpposite(int index) {
		Code removed = codeList.get(index);
		removeCode(index);
		if ( removed != null ) {
			removed.setModel(null);
		}
	}

	/**
	 * <p>Returns all values of matrix.</p>
	 */
	public List<Matrix> getMatrixList() {
		return Collections.unmodifiableList(matrixList);
	}

	/**
	 * <p>Gets matrix object count.</p>
	 */
	public int getMatrixCount() {
		return matrixList.size();
	}

	/**
	 * <p>Gets matrix at given index.</p>
	 */
	public Matrix getMatrix(int index) {
		if ( index < 0 || index >= getMatrixCount() ) { return null; }
		return matrixList.get(index);
	}

	/**
	 * <p>Adds an object in matrix.</p>
	 */
	public void addMatrix(Matrix newValue) {
		addMatrix(getMatrixCount(), newValue);
	}

	/**
	 * <p>Adds an object in matrix at given index.</p>
	 */
	public void addMatrix(int index, Matrix newValue) {
		getChangeRecorder().recordAddObject(this, "matrix", index);
		matrixList.add(index, newValue);
	}

	/**
	 * <p>Replaces an object in matrix at given index. Returns the old value.</p>
	 */
	public Matrix setMatrix(int index, Matrix newValue) {
		Matrix oldValue = matrixList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "matrix", index, oldValue);
		getChangeRecorder().recordAddObject(this, "matrix", index);
		return oldValue;
	}

	/**
	 * <p>Adds a collection of objects in matrix.</p>
	 */
	public void addAllMatrix(Collection<Matrix> toAddList) {
		for (Matrix newValue : toAddList) {
			addMatrix(getMatrixCount(), newValue);
		}
	}

	/**
	 * <p>Removes given object from matrix.</p>
	 */
	public void removeMatrix(Matrix value) {
		int index = matrixList.indexOf(value);
		if (index >= 0 ) {
			removeMatrix(index);
		}
	}

	/**
	 * <p>Removes object from matrix at given index.</p>
	 */
	public void removeMatrix(int index) {
		Matrix oldValue = matrixList.get(index);
		getChangeRecorder().recordRemoveObject(this, "matrix", index, oldValue);
		matrixList.remove(index);
	}

	/**
	 * <p>Adds object to matrix and sets the corresponding model.</p>
	 */
	public void addMatrixAndOpposite(Matrix newValue) {
		addMatrix(newValue);
		if ( newValue != null ) {
			newValue.setModel(this);
		}
	}

	/**
	 * <p>Adds a collection of objects to matrix and sets the corresponding model.</p>
	 */
	public void addAllMatrixAndOpposite(Collection<Matrix> toAddList) {
		for (Matrix newValue : toAddList) {
			addMatrixAndOpposite(getMatrixCount(), newValue);
		}
	}

	/**
	 * <p>Adds object to matrix at given index and sets the corresponding model.</p>
	 */
	public void addMatrixAndOpposite(int index, Matrix newValue) {
		addMatrix(index, newValue);
		if ( newValue != null ) {
			newValue.setModel(this);
		}
	}

	/**
	 * <p>Replaces an object in matrix at given index. Returns the old value.</p>
	 */
	public Matrix setMatrixAndOpposite(int index, Matrix newValue) {
		Matrix oldValue = matrixList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "matrix", index, oldValue);
		getChangeRecorder().recordAddObject(this, "matrix", index);
		if ( newValue != null ) {
			newValue.setModel(this);
		}
		return oldValue;
	}

	/**
	 * <p>Removes object from matrix and resets the corresponding model.</p>
	 */
	public void removeMatrixAndOpposite(Matrix removed) {
		removeMatrix(removed);
		if ( removed != null ) {
			removed.setModel(null);
		}
	}

	/**
	 * <p>Removes object at given index from matrix and resets the corresponding model.</p>
	 */
	public void removeMatrixAndOpposite(int index) {
		Matrix removed = matrixList.get(index);
		removeMatrix(index);
		if ( removed != null ) {
			removed.setModel(null);
		}
	}

	/**
	 * <p>Gets scheduler.</p>
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * <p>Sets scheduler.</p>
	 */
	public void setScheduler(Scheduler newValue) {
		if (scheduler == null ? newValue != null : (scheduler.equals(newValue) == false)) {
			getChangeRecorder().recordChangeAttribute(this, "scheduler", this.scheduler);
			this.scheduler= newValue;
		}
	}

	/**
	 * <p>Sets scheduler and sets the corresponding model.</p>
	 */
	public void setSchedulerAndOpposite(Scheduler newValue) {
		if ( scheduler != null ) {
			scheduler.setModel(null);
		}
		if ( newValue != null ) {
			newValue.setModel(this);
		}
		setScheduler(newValue);
	}

	public void swapMatrices(int i0, int i1) {
		Matrix tmp = getMatrix(i0);
		setMatrix(i0, getMatrix(i1));
		setMatrix(i1, tmp);
	}

	public void swapCode(int i0, int i1) {
		Code tmp = getCode(i0);
		setCode(i0, getCode(i1));
		setCode(i1, tmp);
	}

	public List<Kernel> getKernelList() {
		final List<Kernel> result = new ArrayList<Kernel>();
		for  (Code code : getCodeList() ) {
			if (code instanceof Kernel) {
				result.add((Kernel) code);
			}
		}
		return result;
	}

	public List<Library> getLibraryList() {
		final List<Library> result = new ArrayList<Library>();
		for  (Code code : getCodeList() ) {
			if (code instanceof Library) {
				result.add((Library) code);
			}
		}
		return result;
	}

	public void upLibrary(Library library) {
		int index = codeList.indexOf(library);
		if ( index <= 0 ) return;
		
		// searches for a library before
		for (int i=index-1; i>=0; i--) {
			if ( getCode(i) instanceof Library ) {
				// swaps i and index
				setCode(index, getCode(i));
				setCode(i, library);
				return;
			}
		}
		
		// no library to swap with found
	}

	public void downLibrary(Library library) {
		int index = codeList.indexOf(library);
		if ( index < 0 || index >= codeList.size() -1 ) return;
		
		// searches for a library before
		for (int i=index+1; i < codeList.size(); i++) {
			if ( getCode(i) instanceof Library ) {
				// swaps i and index
				setCode(index, getCode(i));
				setCode(i, library);
				return;
			}
		}
		
		// no library to swap with found
	}

	public void upKernel(Kernel kernel) {
		int index = codeList.indexOf(kernel);
		if ( index <= 0 ) return;
		
		// searches for a kernel before
		for (int i=index-1; i>=0; i--) {
			if ( getCode(i) instanceof Kernel ) {
				// swaps i and index
				setCode(index, getCode(i));
				setCode(i, kernel);
				return;
			}
		}
		
		// no kernel to swap with found
	}

	public void downKernel(Kernel kernel) {
		int index = codeList.indexOf(kernel);
		if ( index < 0 || index >= codeList.size() -1 ) return;
		
		// searches for a kernel before
		for (int i=index+1; i < codeList.size(); i++) {
			if ( getCode(i) instanceof Kernel ) {
				// swaps i and index
				setCode(index, getCode(i));
				setCode(i, kernel);
				return;
			}
		}
		
		// no kernel to swap with found
	}

    /**
     * <p>Returns all values of parameter.</p>
     */
    public List<Parameter> getParameterList() {
        return Collections.unmodifiableList(parameterList);
    }

    /**
     * <p>Gets parameter object count.</p>
     */
    public int getParameterCount() {
        return parameterList.size();
    }

    /**
     * <p>Gets parameter at given index.</p>
     */
    public Parameter getParameter(int index) {
        if ( index < 0 || index >= getParameterCount() ) { return null; }
        return parameterList.get(index);
    }

    /**
     * <p>Adds an object in parameter.</p>
     */
    public void addParameter(Parameter newValue) {
        addParameter(getParameterCount(), newValue);
    }

    /**
     * <p>Adds an object in parameter at given index.</p>
     */
    public void addParameter(int index, Parameter newValue) {
        getChangeRecorder().recordAddObject(this, "parameter", index);
        parameterList.add(index, newValue);
    }

    /**
     * <p>Replaces an object in parameter at given index. Returns the old value.</p>
     */
    public Parameter setParameter(int index, Parameter newValue) {
        Parameter oldValue = parameterList.set(index, newValue);
        getChangeRecorder().recordRemoveObject(this, "parameter", index, oldValue);
        getChangeRecorder().recordAddObject(this, "parameter", index);
        return oldValue;
    }

    /**
     * <p>Adds a collection of objects in parameter.</p>
     */
    public void addAllParameter(Collection<Parameter> toAddList) {
        for (Parameter newValue : toAddList) {
            addParameter(getParameterCount(), newValue);
        }
    }

    /**
     * <p>Removes given object from parameter.</p>
     */
    public void removeParameter(Parameter value) {
        int index = parameterList.indexOf(value);
        if (index >= 0 ) {
            removeParameter(index);
        }
    }

    /**
     * <p>Removes object from parameter at given index.</p>
     */
    public void removeParameter(int index) {
        Parameter oldValue = parameterList.get(index);
        getChangeRecorder().recordRemoveObject(this, "parameter", index, oldValue);
        parameterList.remove(index);
    }

    /**
     * <p>Adds object to parameter and sets the corresponding model.</p>
     */
    public void addParameterAndOpposite(Parameter newValue) {
        addParameter(newValue);
        if ( newValue != null ) {
            newValue.setModel(this);
        }
    }

    /**
     * <p>Adds a collection of objects to parameter and sets the corresponding model.</p>
     */
    public void addAllParameterAndOpposite(Collection<Parameter> toAddList) {
        for (Parameter newValue : toAddList) {
            addParameterAndOpposite(getParameterCount(), newValue);
        }
    }

    /**
     * <p>Adds object to parameter at given index and sets the corresponding model.</p>
     */
    public void addParameterAndOpposite(int index, Parameter newValue) {
        addParameter(index, newValue);
        if ( newValue != null ) {
            newValue.setModel(this);
        }
    }

    /**
     * <p>Replaces an object in parameter at given index. Returns the old value.</p>
     */
    public Parameter setParameterAndOpposite(int index, Parameter newValue) {
        Parameter oldValue = parameterList.set(index, newValue);
        getChangeRecorder().recordRemoveObject(this, "parameter", index, oldValue);
        getChangeRecorder().recordAddObject(this, "parameter", index);
        if ( newValue != null ) {
            newValue.setModel(this);
        }
        return oldValue;
    }

    /**
     * <p>Removes object from parameter and resets the corresponding model.</p>
     */
    public void removeParameterAndOpposite(Parameter removed) {
        removeParameter(removed);
        if ( removed != null ) {
            removed.setModel(null);
        }
    }

    /**
     * <p>Removes object at given index from parameter and resets the corresponding model.</p>
     */
    public void removeParameterAndOpposite(int index) {
        Parameter removed = parameterList.get(index);
        removeParameter(index);
        if ( removed != null ) {
            removed.setModel(null);
        }
    }

	public void writeToBoost(Boost boost) {
        int version = boost.getFileVersion();
		BoostUtil.writeObjectCollection(boost, codeList);
		BoostUtil.writeObjectCollection(boost, matrixList);
        if (version >= 3) {
            BoostUtil.writeObjectCollection(boost, parameterList);
        }
		boost.writeObject(scheduler);
    }

	/**
	 * Visitor accept method.
	 */
	public void accept(ModelVisitor visitor) {
		visitor.visitModel(this);
	}

	private ModelChangeRecorder changeHandler = 	new ModelChangeRecorder()
;

	public ModelChangeRecorder getChangeRecorder() {
		return changeHandler;
	}

}

