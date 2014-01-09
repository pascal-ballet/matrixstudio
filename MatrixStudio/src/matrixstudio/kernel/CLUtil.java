package matrixstudio.kernel;

import static org.jocl.CL.CL_DEVICE_TYPE_ALL;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetDeviceInfo;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clGetPlatformInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import matrixstudio.model.Device;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

/** Sets of utility method for OpenCL. */
public abstract class CLUtil {
	
	private static boolean clPresent = false;

	static {
		// Tests if CL is present
		try {
			List<cl_device_id> devicesList = getHardwareListInternal();
			clPresent = (devicesList.isEmpty() == false);
			
		} catch (Throwable error) {
			error.printStackTrace();
			clPresent = false;
		}
	}

	public static boolean isClPresent() {
		return clPresent;
	}
	
    private static List<cl_device_id> getHardwareListInternal() {
        // Obtain the number of platforms
        int numPlatforms[] = new int[1];
        clGetPlatformIDs(0, null, numPlatforms);
        
        // Obtain the platform IDs
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms[0]];
        clGetPlatformIDs(platforms.length, platforms, null);

        // Collect all devices of all platforms
        List<cl_device_id> devices = new ArrayList<cl_device_id>();
        for (int i=0; i<platforms.length; i++)
        {
            // Obtain the number of devices for the current platform
            int numDevices[] = new int[1];
            clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_ALL, 0, null, numDevices);

            cl_device_id devicesArray[] = new cl_device_id[numDevices[0]];
            clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_ALL, numDevices[0], devicesArray, null);

            devices.addAll(Arrays.asList(devicesArray));
        }
        return devices;
    }
    
    public static List<cl_device_id> getHardwareList() {
    	if ( isClPresent() ) {
    		return getHardwareListInternal();
    	} else {
    		return Collections.emptyList();
    	}
    }
    
    public static List<cl_device_id> selectHardware(Device device) {
    	final List<cl_device_id> hardwareList = getHardwareList();

    	// for AUTO returns all.
		if ( device == null || device == Device.ANY ) return hardwareList;
		
		final List<cl_device_id> deviceList = new ArrayList<cl_device_id>();
    	for ( cl_device_id id : hardwareList ) {
    		final Device found = getDevice(id);
    		if ( found == Device.ANY || found == device ) {
    			deviceList.add(id);
    		}
    	}
    	return deviceList;
    }
    
    /**
     * Returns the value of the platform info parameter with the given name
     *
     * @param platform The platform
     * @param paramName The parameter name
     * @return The value
     */
    public static String getString(cl_platform_id platform, int paramName)
    {
    	if ( !isClPresent() ) return "Cl not present";
    	
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(platform, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }
    

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    public static String getString(cl_device_id device, int paramName)
    {
    	if ( !isClPresent() ) return "Cl not present";

    	// Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }    
    
    public static long getLong(cl_device_id device, int paramName)
    {
    	long[] value = new long[1];
    	clGetDeviceInfo(device, paramName, 8, Pointer.to(value), null);
    	return value[0];
    }    
    
    public static Device getDevice(cl_device_id device) {
    	// Obtain the length of the string that will be queried
    	long[] size = new long[1];
    	clGetDeviceInfo(device, CL.CL_DEVICE_TYPE, 0, null, size);
    	
    	// Create a buffer of the appropriate size and fill it with the info
    	byte[] buffer = new byte[(int)size[0]];
    	clGetDeviceInfo(device, CL.CL_DEVICE_TYPE, buffer.length, Pointer.to(buffer), null);
    	final long type = buffer[0];
    	if ( (type & CL.CL_DEVICE_TYPE_GPU ) > 0) {
    		return Device.GPU;
    	} else if ( (type & CL.CL_DEVICE_TYPE_CPU ) > 0) {
    		return Device.CPU;
    	} else {
    		return Device.ANY;
    	}
    }
    
    public static void main(String[] args) {
		cl_device_id id = selectHardware(Device.CPU).get(0);
		System.out.println(getLong(id, CL.CL_DEVICE_MAX_WORK_GROUP_SIZE));
	}
}
