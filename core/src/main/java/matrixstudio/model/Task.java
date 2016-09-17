package matrixstudio.model;

import org.xid.basics.geometry.Geometry;
import org.xid.basics.model.ChangeRecorder;
import org.xid.basics.model.ModelObject;
import org.xid.basics.serializer.Boost;
import org.xid.basics.serializer.BoostObject;
import org.xid.basics.serializer.BoostUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class Task implements ModelObject, BoostObject {

	private Scheduler scheduler;

    private int repetition = 1;

	private int globalWorkSizeX = 512;

	private int globalWorkSizeY = 256;

	private int globalWorkSizeZ = 1;

	private float[] position = new float[] { 50f, 50f };

	/**
	 * <p>taskIn field.</p>
	 */
	private final List<Task> taskInList = new ArrayList<>();

	/**
	 * <p>taskOut field.</p>
	 */
	private final List<Task> taskOutList = new ArrayList<>();

    /**
     * <p>Kernel list field.</p>
     */
	private final List<Kernel> kernelList = new ArrayList<>();

	public Task() {
	}

	protected Task(Boost boost) {
        boost.register(this);
        int version = boost.getFileVersion();
        scheduler = boost.readObject(Scheduler.class);
        repetition = version >= 2 ? boost.readInt() : 1;
		globalWorkSizeX = boost.readInt();
		globalWorkSizeY = boost.readInt();
		globalWorkSizeZ = boost.readInt();
		position = BoostUtil.readFloatArray(boost);
		for ( Task oneChild : BoostUtil.readObjectList(boost, Task.class) ) {
			taskInList.add(oneChild);
		}
		for ( Task oneChild : BoostUtil.readObjectList(boost, Task.class) ) {
			taskOutList.add(oneChild);
		}
        if (version >= 2) {
            for ( Kernel oneChild : BoostUtil.readObjectList(boost, Kernel.class) ) {
                kernelList.add(oneChild);
            }
        } else {
            kernelList.add(boost.readObject(Kernel.class));
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
     * <p>Gets repetition.</p>
     */
    public int getRepetition() {
        return repetition;
    }

    /**
     * <p>Sets repetition.</p>
     */
    public void setRepetition(int newValue) {
        if (repetition != newValue) {
            getChangeRecorder().recordChangeAttribute(this, "repetition", this.repetition);
            this.repetition= newValue;
        }
    }

    /**
	 * <p>Gets globalWorkSizeX.</p>
	 */
	public int getGlobalWorkSizeX() {
		return globalWorkSizeX;
	}

	/**
	 * <p>Sets globalWorkSizeX.</p>
	 */
	public void setGlobalWorkSizeX(int newValue) {
		if (globalWorkSizeX != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "globalWorkSizeX", this.globalWorkSizeX);
			this.globalWorkSizeX= newValue;
		}
	}

	/**
	 * <p>Gets globalWorkSizeY.</p>
	 */
	public int getGlobalWorkSizeY() {
		return globalWorkSizeY;
	}

	/**
	 * <p>Sets globalWorkSizeY.</p>
	 */
	public void setGlobalWorkSizeY(int newValue) {
		if (globalWorkSizeY != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "globalWorkSizeY", this.globalWorkSizeY);
			this.globalWorkSizeY= newValue;
		}
	}

	/**
	 * <p>Gets globalWorkSizeZ.</p>
	 */
	public int getGlobalWorkSizeZ() {
		return globalWorkSizeZ;
	}

	/**
	 * <p>Sets globalWorkSizeZ.</p>
	 */
	public void setGlobalWorkSizeZ(int newValue) {
		if (globalWorkSizeZ != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "globalWorkSizeZ", this.globalWorkSizeZ);
			this.globalWorkSizeZ= newValue;
		}
	}

	/**
	 * <p>Gets position.</p>
	 */
	public float[] getPosition() {
		return position;
	}

	/**
	 * <p>Sets position.</p>
	 */
	public void setPosition(float[] newValue) {
		if (position != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "position", this.position);
			this.position= newValue;
		}
	}

	/**
	 * <p>Returns all values of taskIn.</p>
	 */
	public List<Task> getTaskInList() {
		return Collections.unmodifiableList(taskInList);
	}

	/**
	 * <p>Gets taskIn object count.</p>
	 */
	public int getTaskInCount() {
		return taskInList.size();
	}

	/**
	 * <p>Gets taskIn at given index.</p>
	 */
	public Task getTaskIn(int index) {
		if ( index < 0 || index >= getTaskInCount() ) { return null; }
		return taskInList.get(index);
	}

	/**
	 * <p>Adds an object in taskIn.</p>
	 */
	public void addTaskIn(Task newValue) {
		addTaskIn(getTaskInCount(), newValue);
	}

	/**
	 * <p>Adds an object in taskIn at given index.</p>
	 */
	public void addTaskIn(int index, Task newValue) {
		getChangeRecorder().recordAddObject(this, "taskIn", index);
		taskInList.add(index, newValue);
	}

	/**
	 * <p>Replaces an object in taskIn at given index. Returns the old value.</p>
	 */
	public Task setTaskIn(int index, Task newValue) {
		Task oldValue = taskInList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "taskIn", index, oldValue);
		getChangeRecorder().recordAddObject(this, "taskIn", index);
		return oldValue;
	}

	/**
	 * <p>Adds a collection of objects in taskIn.</p>
	 */
	public void addAllTaskIn(Collection<Task> toAddList) {
		for (Task newValue : toAddList) {
			addTaskIn(getTaskInCount(), newValue);
		}
	}

	/**
	 * <p>Removes given object from taskIn.</p>
	 */
	public void removeTaskIn(Task value) {
		int index = taskInList.indexOf(value);
		if (index >= 0 ) {
			removeTaskIn(index);
		}
	}

	/**
	 * <p>Removes object from taskIn at given index.</p>
	 */
	public void removeTaskIn(int index) {
		Task oldValue = taskInList.get(index);
		getChangeRecorder().recordRemoveObject(this, "taskIn", index, oldValue);
		taskInList.remove(index);
	}

	/**
	 * <p>Returns all values of taskOut.</p>
	 */
	public List<Task> getTaskOutList() {
		return Collections.unmodifiableList(taskOutList);
	}

	/**
	 * <p>Gets taskOut object count.</p>
	 */
	public int getTaskOutCount() {
		return taskOutList.size();
	}

	/**
	 * <p>Gets taskOut at given index.</p>
	 */
	public Task getTaskOut(int index) {
		if ( index < 0 || index >= getTaskOutCount() ) { return null; }
		return taskOutList.get(index);
	}

	/**
	 * <p>Adds an object in taskOut.</p>
	 */
	public void addTaskOut(Task newValue) {
		addTaskOut(getTaskOutCount(), newValue);
	}

	/**
	 * <p>Adds an object in taskOut at given index.</p>
	 */
	public void addTaskOut(int index, Task newValue) {
		getChangeRecorder().recordAddObject(this, "taskOut", index);
		taskOutList.add(index, newValue);
	}

	/**
	 * <p>Replaces an object in taskOut at given index. Returns the old value.</p>
	 */
	public Task setTaskOut(int index, Task newValue) {
		Task oldValue = taskOutList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "taskOut", index, oldValue);
		getChangeRecorder().recordAddObject(this, "taskOut", index);
		return oldValue;
	}

	/**
	 * <p>Adds a collection of objects in taskOut.</p>
	 */
	public void addAllTaskOut(Collection<Task> toAddList) {
		for (Task newValue : toAddList) {
			addTaskOut(getTaskOutCount(), newValue);
		}
	}

	/**
	 * <p>Removes given object from taskOut.</p>
	 */
	public void removeTaskOut(Task value) {
		int index = taskOutList.indexOf(value);
		if (index >= 0 ) {
			removeTaskOut(index);
		}
	}

	/**
	 * <p>Removes object from taskOut at given index.</p>
	 */
	public void removeTaskOut(int index) {
		Task oldValue = taskOutList.get(index);
		getChangeRecorder().recordRemoveObject(this, "taskOut", index, oldValue);
		taskOutList.remove(index);
	}

    // -----

    /**
     * <p>Returns all values of kernel.</p>
     */
    public List<Kernel> getKernelList() {
        return Collections.unmodifiableList(kernelList);
    }

    /**
     * <p>Gets kernel object count.</p>
     */
    public int getKernelCount() {
        return kernelList.size();
    }

    /**
     * <p>Gets kernel at given index.</p>
     */
    public Kernel getKernel(int index) {
        if ( index < 0 || index >= getKernelCount() ) { return null; }
        return kernelList.get(index);
    }

    /**
     * <p>Adds an object in kernel.</p>
     */
    public void addKernel(Kernel newValue) {
        addKernel(getKernelCount(), newValue);
    }

    /**
     * <p>Adds an object in kernel at given index.</p>
     */
    public void addKernel(int index, Kernel newValue) {
        getChangeRecorder().recordAddObject(this, "kernel", index);
        kernelList.add(index, newValue);
    }

    /**
     * <p>Replaces an object in kernel at given index. Returns the old value.</p>
     */
    public Kernel setKernel(int index, Kernel newValue) {
        Kernel oldValue = kernelList.set(index, newValue);
        getChangeRecorder().recordRemoveObject(this, "kernel", index, oldValue);
        getChangeRecorder().recordAddObject(this, "kernel", index);
        return oldValue;
    }

    /**
     * <p>Adds a collection of objects in kernel.</p>
     */
    public void addAllKernel(Collection<Kernel> toAddList) {
        for (Kernel newValue : toAddList) {
            addKernel(getKernelCount(), newValue);
        }
    }

    /**
     * <p>Removes given object from kernel.</p>
     */
    public void removeKernel(Kernel value) {
        int index = kernelList.indexOf(value);
        if (index >= 0 ) {
            removeKernel(index);
        }
    }

    /**
     * <p>Removes object from kernel at given index.</p>
     */
    public void removeKernel(int index) {
        Kernel oldValue = kernelList.get(index);
        getChangeRecorder().recordRemoveObject(this, "kernel", index, oldValue);
        kernelList.remove(index);
    }

    public void upKernel(int index) {
        if ( index <= 0 && index >= kernelList.size() ) return;

        Kernel kernel = getKernel(index);
        setKernel(index, getKernel(index-1));
        setKernel(index-1, kernel);
    }

    public void downKernel(int index) {
        if ( index < 0 && index >= kernelList.size() - 1 ) return;

        Kernel kernel = getKernel(index);
        setKernel(index, getKernel(index+1));
        setKernel(index+1, kernel);
    }


    /**
	 * <p><b>getPositionCopy</b>: Returns a copy of position.</p>
	 */
	public float[] getPositionCopy() {
		return Geometry.copyPoints(position);
	}

	public void writeToBoost(Boost boost) {
        int version = boost.getFileVersion();
		boost.writeObject(scheduler);
        if (version >= 2) boost.writeInt(repetition);
		boost.writeInt(globalWorkSizeX);
		boost.writeInt(globalWorkSizeY);
		boost.writeInt(globalWorkSizeZ);
		BoostUtil.writeFloatArray(boost, position);
		BoostUtil.writeObjectCollection(boost, taskInList);
		BoostUtil.writeObjectCollection(boost, taskOutList);
        if (version >= 2) {
		    BoostUtil.writeObjectCollection(boost, kernelList);
        } else {
            if (kernelList.size() > 0) {
                boost.writeObject(kernelList.get(0));
            } else {
                boost.writeObject(null);
            }
        }
	}

	/**
	 * Visitor accept method.
	 */
	public void accept(ModelVisitor visitor) {
		visitor.visitTask(this);
	}

	public ChangeRecorder getChangeRecorder() {
		if ( getScheduler() != null ) {
			return getScheduler().getChangeRecorder();
		}
		return ChangeRecorder.Stub;
	}

}

