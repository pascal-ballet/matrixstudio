package matrixstudio.model;

import org.xid.basics.serializer.BoostUtil;
import org.xid.basics.serializer.BoostObject;
import org.xid.basics.serializer.Boost;
import org.xid.basics.model.ModelObject;
import org.xid.basics.model.ChangeRecorder;
import org.xid.basics.geometry.Geometry;
import matrixstudio.model.Scheduler;
import matrixstudio.model.ModelVisitor;
import matrixstudio.model.Kernel;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;


public class Task implements ModelObject, BoostObject {

	private Scheduler scheduler;

	private int globalWorkSizeX = 512;

	private int globalWorkSizeY = 256;

	private int globalWorkSizeZ = 1;

	private float[] position = new float[] { 50f, 50f };

	/**
	 * <p>taskIn field.</p>
	 */
	private final List<matrixstudio.model.Task> taskInList = new ArrayList<matrixstudio.model.Task>();

	/**
	 * <p>taskOut field.</p>
	 */
	private final List<matrixstudio.model.Task> taskOutList = new ArrayList<matrixstudio.model.Task>();

	private Kernel kernel;

	public Task() {
	}

	protected Task(Boost boost) {
		boost.register(this);
		scheduler = boost.readObject(Scheduler.class);
		globalWorkSizeX = boost.readInt();
		globalWorkSizeY = boost.readInt();
		globalWorkSizeZ = boost.readInt();
		position = BoostUtil.readFloatArray(boost);
		for ( matrixstudio.model.Task oneChild : BoostUtil.readObjectList(boost, matrixstudio.model.Task.class) ) {
			taskInList.add(oneChild);
		}
		for ( matrixstudio.model.Task oneChild : BoostUtil.readObjectList(boost, matrixstudio.model.Task.class) ) {
			taskOutList.add(oneChild);
		}
		kernel = boost.readObject(Kernel.class);
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
	public List<matrixstudio.model.Task> getTaskInList() {
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
	public matrixstudio.model.Task getTaskIn(int index) {
		if ( index < 0 || index >= getTaskInCount() ) { return null; }
		return taskInList.get(index);
	}

	/**
	 * <p>Adds an object in taskIn.</p>
	 */
	public void addTaskIn(matrixstudio.model.Task newValue) {
		addTaskIn(getTaskInCount(), newValue);
	}

	/**
	 * <p>Adds an object in taskIn at given index.</p>
	 */
	public void addTaskIn(int index, matrixstudio.model.Task newValue) {
		getChangeRecorder().recordAddObject(this, "taskIn", index);
		taskInList.add(index, newValue);
	}

	/**
	 * <p>Replaces an object in taskIn at given index. Returns the old value.</p>
	 */
	public matrixstudio.model.Task setTaskIn(int index, matrixstudio.model.Task newValue) {
		matrixstudio.model.Task oldValue = taskInList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "taskIn", index, oldValue);
		getChangeRecorder().recordAddObject(this, "taskIn", index);
		return oldValue;
	}

	/**
	 * <p>Adds a collection of objects in taskIn.</p>
	 */
	public void addAllTaskIn(Collection<matrixstudio.model.Task> toAddList) {
		for (matrixstudio.model.Task newValue : toAddList) {
			addTaskIn(getTaskInCount(), newValue);
		}
	}

	/**
	 * <p>Removes given object from taskIn.</p>
	 */
	public void removeTaskIn(matrixstudio.model.Task value) {
		int index = taskInList.indexOf(value);
		if (index >= 0 ) {
			removeTaskIn(index);
		}
	}

	/**
	 * <p>Removes object from taskIn at given index.</p>
	 */
	public void removeTaskIn(int index) {
		matrixstudio.model.Task oldValue = taskInList.get(index);
		getChangeRecorder().recordRemoveObject(this, "taskIn", index, oldValue);
		taskInList.remove(index);
	}

	/**
	 * <p>Returns all values of taskOut.</p>
	 */
	public List<matrixstudio.model.Task> getTaskOutList() {
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
	public matrixstudio.model.Task getTaskOut(int index) {
		if ( index < 0 || index >= getTaskOutCount() ) { return null; }
		return taskOutList.get(index);
	}

	/**
	 * <p>Adds an object in taskOut.</p>
	 */
	public void addTaskOut(matrixstudio.model.Task newValue) {
		addTaskOut(getTaskOutCount(), newValue);
	}

	/**
	 * <p>Adds an object in taskOut at given index.</p>
	 */
	public void addTaskOut(int index, matrixstudio.model.Task newValue) {
		getChangeRecorder().recordAddObject(this, "taskOut", index);
		taskOutList.add(index, newValue);
	}

	/**
	 * <p>Replaces an object in taskOut at given index. Returns the old value.</p>
	 */
	public matrixstudio.model.Task setTaskOut(int index, matrixstudio.model.Task newValue) {
		matrixstudio.model.Task oldValue = taskOutList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "taskOut", index, oldValue);
		getChangeRecorder().recordAddObject(this, "taskOut", index);
		return oldValue;
	}

	/**
	 * <p>Adds a collection of objects in taskOut.</p>
	 */
	public void addAllTaskOut(Collection<matrixstudio.model.Task> toAddList) {
		for (matrixstudio.model.Task newValue : toAddList) {
			addTaskOut(getTaskOutCount(), newValue);
		}
	}

	/**
	 * <p>Removes given object from taskOut.</p>
	 */
	public void removeTaskOut(matrixstudio.model.Task value) {
		int index = taskOutList.indexOf(value);
		if (index >= 0 ) {
			removeTaskOut(index);
		}
	}

	/**
	 * <p>Removes object from taskOut at given index.</p>
	 */
	public void removeTaskOut(int index) {
		matrixstudio.model.Task oldValue = taskOutList.get(index);
		getChangeRecorder().recordRemoveObject(this, "taskOut", index, oldValue);
		taskOutList.remove(index);
	}

	/**
	 * <p>Gets kernel.</p>
	 */
	public Kernel getKernel() {
		return kernel;
	}

	/**
	 * <p>Sets kernel.</p>
	 */
	public void setKernel(Kernel newValue) {
		if (kernel == null ? newValue != null : (kernel.equals(newValue) == false)) {
			getChangeRecorder().recordChangeAttribute(this, "kernel", this.kernel);
			this.kernel= newValue;
		}
	}

	/**
	 * <p><b>getPositionCopy</b>: Returns a copy of position.</p>
	 */
	public float[] getPositionCopy() {
		return Geometry.copyPoints(position);
	}

	public void writeToBoost(Boost boost) {
		boost.writeObject(scheduler);
		boost.writeInt(globalWorkSizeX);
		boost.writeInt(globalWorkSizeY);
		boost.writeInt(globalWorkSizeZ);
		BoostUtil.writeFloatArray(boost, position);
		BoostUtil.writeObjectCollection(boost, taskInList);
		BoostUtil.writeObjectCollection(boost, taskOutList);
		boost.writeObject(kernel);
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

