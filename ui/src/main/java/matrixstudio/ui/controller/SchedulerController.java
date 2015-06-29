package matrixstudio.ui.controller;

import matrixstudio.kernel.CLUtil;
import matrixstudio.model.Device;
import matrixstudio.model.Scheduler;
import org.jocl.CL;
import org.jocl.cl_device_id;
import org.xid.basics.ui.BasicsUI;
import org.xid.basics.ui.controller.Controller;
import org.xid.basics.ui.field.ChoiceField;
import org.xid.basics.ui.field.CompositeField;
import org.xid.basics.ui.field.Field;

import java.util.Arrays;
import java.util.List;


public class SchedulerController extends Controller<Scheduler> {

	private ChoiceField<Device> deviceField;
	private ChoiceField<cl_device_id> hardwareField;
	
	private CompositeField compositeField;
	
	@Override
	public CompositeField createFields() {
		deviceField = new ChoiceField<Device>("Device", BasicsUI.NO_INFO);
		deviceField.setRange(Arrays.asList(Device.ANY, Device.CPU, Device.GPU));
		
		hardwareField = new ChoiceField<cl_device_id>("Hardware", BasicsUI.NO_INFO) {
			public String getText(cl_device_id element) {
				return CLUtil.getString(element, CL.CL_DEVICE_NAME);
			}
		};
		
		compositeField = new CompositeField("SchedulerTest", BasicsUI.GROUP, deviceField, hardwareField);
		return compositeField;
	}

	
	@Override
	public void refreshFields() {
		if ( getSubject() == null ) {
			compositeField.setEnable(false);
		} else {
			compositeField.setEnable(true);
			final Device device = getSubject().getDevice();
			final List<cl_device_id> hardwareList = CLUtil.selectHardware(device);
			int deviceOrder = getSubject().getDeviceOrder();
			if ( deviceOrder < 0 || deviceOrder >= hardwareList.size()) {
				deviceOrder = 0;
			}
			
			deviceField.setValue(device);
			if ( hardwareList.size() > 0 ) {
				hardwareField.setRange(hardwareList);
				hardwareField.setValue(hardwareList.get(deviceOrder));
			}
		}
	}
	
	@Override
	public boolean updateSubject(Field field) {
		if ( field == deviceField ) {
			getSubject().setDevice(deviceField.getValue());
			return true;
		}
		if ( field == hardwareField ) {
			final int index = hardwareField.getRange().indexOf(hardwareField.getValue());
			getSubject().setDeviceOrder(index);
			return true;
		}
		
		return false;
	}
}
