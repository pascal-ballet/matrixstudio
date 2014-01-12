package toto;

import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;

public class SchedulerTest {
	
	private cl_context context;
	private cl_command_queue queue;
	
	private int step;
	
	private cl_mem matrix1Mem;
	private cl_mem matrix2Mem;

	private Pointer matrix1Pointer;
	private Pointer matrix2Pointer;
	
	public void initialize(int[] matrix1, long[] matrix2)  {
		initializeCL();
		initializeMemory(matrix1, matrix2);
		initializeScheduler();
	}
	
	private void initializeCL() throws CLException {
		// Enable exceptions and subsequently get error checks
		CL.setExceptionsEnabled(true);

		// gets platform
		cl_platform_id platforms[] = new cl_platform_id[1];
        CL.clGetPlatformIDs(platforms.length, platforms, null);
        
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platforms[0]);

        // obtains the number of devices for the current platform
        int numDevices[] = new int[1];
        CL.clGetDeviceIDs(platforms[0], CL.CL_DEVICE_TYPE_ALL, 0, null, numDevices);

        if ( numDevices[0] == 0 ) {
        	throw new CLException("No OpenCl device detected.");
        }
        
        // obtains informations on devices
        cl_device_id devicesArray[] = new cl_device_id[numDevices[0]];
        CL.clGetDeviceIDs(platforms[0], CL.CL_DEVICE_TYPE_ALL, numDevices[0], devicesArray, null);

        // chooses device
        cl_device_id device = devicesArray[0];
        
        // creates context
        context = CL.clCreateContext(null, 1, new cl_device_id[] { device }, null, null, null);

        // Create a command-queue
    	queue = CL.clCreateCommandQueue(context, device, CL.CL_NONE, null);      

	}
	
	private void initializeMemory(int[] matrix1, long[] matrix2) {
    	// Initialization of pointers
		matrix1Pointer = Pointer.to(matrix1);
		matrix1Mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * + matrix1.length, matrix1Pointer, null);
		
		matrix2Pointer = Pointer.to(matrix2);
		matrix2Mem = CL.clCreateBuffer(context, CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_ulong * + matrix2.length, matrix2Pointer, null);
	}
	
	private void initializeScheduler() {
		step = 0;
		
	}

	public void step() {
		
	}
	
	public static void main(String[] args) {
		SchedulerTest scheduler = new SchedulerTest();
		scheduler.initialize(new int[1024], new long[1024]);
	}
}

