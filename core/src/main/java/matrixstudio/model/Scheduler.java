package matrixstudio.model;

import fr.minibilles.basics.model.ChangeRecorder;
import fr.minibilles.basics.model.ModelObject;
import fr.minibilles.basics.serializer.Boost;
import fr.minibilles.basics.serializer.BoostObject;
import fr.minibilles.basics.serializer.BoostUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class Scheduler implements ModelObject, BoostObject {

	private Model model;

	/**
	 * <p>task field.</p>
	 */
	private final List<Task> taskList = new ArrayList<Task>();

	private Device device = Device.ANY;

	private int deviceOrder = 1;

	public Scheduler() {
	}

	protected Scheduler(Boost boost) {
		boost.register(this);
		model = boost.readObject(Model.class);
		for ( Task oneChild : BoostUtil.readObjectList(boost, Task.class) ) {
			taskList.add(oneChild);
		}
		device = boost.readEnum(Device.class);
		deviceOrder = boost.readInt();
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
	 * <p>Returns all values of task.</p>
	 */
	public List<Task> getTaskList() {
		return Collections.unmodifiableList(taskList);
	}

	/**
	 * <p>Gets task object count.</p>
	 */
	public int getTaskCount() {
		return taskList.size();
	}

	/**
	 * <p>Gets task at given index.</p>
	 */
	public Task getTask(int index) {
		if ( index < 0 || index >= getTaskCount() ) { return null; }
		return taskList.get(index);
	}

	/**
	 * <p>Adds an object in task.</p>
	 */
	public void addTask(Task newValue) {
		addTask(getTaskCount(), newValue);
	}

	/**
	 * <p>Adds an object in task at given index.</p>
	 */
	public void addTask(int index, Task newValue) {
		getChangeRecorder().recordAddObject(this, "task", index);
		taskList.add(index, newValue);
	}

	/**
	 * <p>Replaces an object in task at given index. Returns the old value.</p>
	 */
	public Task setTask(int index, Task newValue) {
		Task oldValue = taskList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "task", index, oldValue);
		getChangeRecorder().recordAddObject(this, "task", index);
		return oldValue;
	}

	/**
	 * <p>Adds a collection of objects in task.</p>
	 */
	public void addAllTask(Collection<Task> toAddList) {
		for (Task newValue : toAddList) {
			addTask(getTaskCount(), newValue);
		}
	}

	/**
	 * <p>Removes given object from task.</p>
	 */
	public void removeTask(Task value) {
		int index = taskList.indexOf(value);
		if (index >= 0 ) {
			removeTask(index);
		}
	}

	/**
	 * <p>Removes object from task at given index.</p>
	 */
	public void removeTask(int index) {
		Task oldValue = taskList.get(index);
		getChangeRecorder().recordRemoveObject(this, "task", index, oldValue);
		taskList.remove(index);
	}

	/**
	 * <p>Adds object to task and sets the corresponding scheduler.</p>
	 */
	public void addTaskAndOpposite(Task newValue) {
		addTask(newValue);
		if ( newValue != null ) {
			newValue.setScheduler(this);
		}
	}

	/**
	 * <p>Adds a collection of objects to task and sets the corresponding scheduler.</p>
	 */
	public void addAllTaskAndOpposite(Collection<Task> toAddList) {
		for (Task newValue : toAddList) {
			addTaskAndOpposite(getTaskCount(), newValue);
		}
	}

	/**
	 * <p>Adds object to task at given index and sets the corresponding scheduler.</p>
	 */
	public void addTaskAndOpposite(int index, Task newValue) {
		addTask(index, newValue);
		if ( newValue != null ) {
			newValue.setScheduler(this);
		}
	}

	/**
	 * <p>Replaces an object in task at given index. Returns the old value.</p>
	 */
	public Task setTaskAndOpposite(int index, Task newValue) {
		Task oldValue = taskList.set(index, newValue);
		getChangeRecorder().recordRemoveObject(this, "task", index, oldValue);
		getChangeRecorder().recordAddObject(this, "task", index);
		if ( newValue != null ) {
			newValue.setScheduler(this);
		}
		return oldValue;
	}

	/**
	 * <p>Removes object from task and resets the corresponding scheduler.</p>
	 */
	public void removeTaskAndOpposite(Task removed) {
		removeTask(removed);
		if ( removed != null ) {
			removed.setScheduler(null);
		}
	}

	/**
	 * <p>Removes object at given index from task and resets the corresponding scheduler.</p>
	 */
	public void removeTaskAndOpposite(int index) {
		Task removed = taskList.get(index);
		removeTask(index);
		if ( removed != null ) {
			removed.setScheduler(null);
		}
	}

	/**
	 * <p>Gets device.</p>
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * <p>Sets device.</p>
	 */
	public void setDevice(Device newValue) {
		if (device == null ? newValue != null : (device.equals(newValue) == false)) {
			getChangeRecorder().recordChangeAttribute(this, "device", this.device);
			this.device= newValue;
		}
	}

	/**
	 * <p>Gets deviceOrder.</p>
	 */
	public int getDeviceOrder() {
		return deviceOrder;
	}

	/**
	 * <p>Sets deviceOrder.</p>
	 */
	public void setDeviceOrder(int newValue) {
		if (deviceOrder != newValue) {
			getChangeRecorder().recordChangeAttribute(this, "deviceOrder", this.deviceOrder);
			this.deviceOrder= newValue;
		}
	}

	public void writeToBoost(Boost boost) {
		boost.writeObject(model);
		BoostUtil.writeObjectCollection(boost, taskList);
		boost.writeEnum(device);
		boost.writeInt(deviceOrder);
	}

	/**
	 * Visitor accept method.
	 */
	public void accept(ModelVisitor visitor) {
		visitor.visitScheduler(this);
	}

	public ChangeRecorder getChangeRecorder() {
		if ( getModel() != null ) {
			return getModel().getChangeRecorder();
		}
		return ChangeRecorder.Stub;
	}

}

