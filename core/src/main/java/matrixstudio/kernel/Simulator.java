package matrixstudio.kernel;

import fr.minibilles.basics.error.DiagnosticUtil;
import fr.minibilles.basics.progress.ActionMonitor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import matrixstudio.formula.EvaluationException;
import matrixstudio.formula.FormulaCache;
import matrixstudio.model.Code;
import matrixstudio.model.Kernel;
import matrixstudio.model.Library;
import matrixstudio.model.Matrix;
import matrixstudio.model.MatrixFloat;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.MatrixULong;
import matrixstudio.model.Model;
import matrixstudio.model.Parameter;
import matrixstudio.model.Scheduler;
import matrixstudio.model.Task;
import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_event;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;


import static org.jocl.CL.*;


public class Simulator implements Runnable {

    public boolean Embedded = false;

	public boolean recordingMPEG = false; // Must be changed (?) to be displayed in MatrixStudio 
	public boolean recordingPNG = false; // Must be changed (?) to be displayed in MatrixStudio 
	
	public interface UserInputProvider {
		int getButton();
		int getMouseX();
		int getMouseY();
		int getMouseZ();
                int getKey();
	}
	
	private final UserInputProvider emptyProvider = new UserInputProvider() {
                @Override
		public int getButton() { return 0; }
                @Override
		public int getMouseX() { return -1; }
                @Override
		public int getMouseY() { return -1; }
                @Override
		public int getMouseZ() { return 0; }
                @Override
		public int getKey() { return 0; }
	};
	
	/** Logger */
	private final SimulatorContext log;

    private int refreshStep = 1;
    
    private int nbSteps=0;
    private long initialSimulationTime=-1;
    private int initialStep = 0;

	private boolean running = false;
    private Thread internalThread = null;

    private ActionMonitor monitor;
    private UserInputProvider inputProvider = emptyProvider;
    
    private cl_device_id device = null;
    private cl_context context = null;
    private cl_command_queue commandQueue   = null;
    private cl_mem memObjects[]             = null;
    private Pointer[] matricesPointer       = null;
    private cl_program program              = null;
    private cl_kernel[] kernels             = null;
    
    private HashMap<String, cl_kernel> clKernelsByName = null;
    
    private HashMap<Task, long[]> globalSizeByTask = null;
    
    private HashMap<Task,cl_event> eventsByTask = null;
    private cl_event[] allEvents = null;
    private HashMap<Task, cl_event[]> dependenciesByTask = null;
    
    private List<Task> orderedTasks = null;
    
    public Simulator(SimulatorContext log) {
    	this.log = log;
    }
    
    public Model getModel() {
    	return log.getModel();
    }
    
    public UserInputProvider getInputProvider() {
		return inputProvider;
	}
    
    public void setInputProvider(UserInputProvider inputProvider) {
    	if ( inputProvider != null ) {
    		this.inputProvider = inputProvider;
    	} else {
    		this.inputProvider = emptyProvider;
    	}
	}
    
    public boolean compileKernelCode() {
        try {
            releaseCL();

            // order matters a lot !
            initMatrices();
            initScheduler();
            initCL();
            initCode();
            initKernel();
            initTasks();

            log.log("Compilation finished at " + Tools.getDateTime());
            log.log("Compilation successful.");
            initialSimulationTime = System.currentTimeMillis();

        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;        
    }

    private int evaluateFormula(String formula) throws EvaluationException, ParseException {
        return FormulaCache.SHARED.computeValue(formula, getModel());
    }

    int toto=0;

	private void initCode() throws EvaluationException, ParseException {
		
		// creation of openCL code to compile
    	final Model model = getModel();
        final StringBuilder prg = new StringBuilder();

        // first appends parameters
        for (Parameter parameter : getModel().getParameterList()) {
            int value = evaluateFormula(parameter.getFormula());
            prg.append("__constant const int " + parameter.getName() + " = " + value + ";\n");
        }
        prg.append("\n");

        // then appends libraries
        for (Code code : model.getCodeList() ) {
        	if ( code instanceof Library ) {
        		final String contents = code.getWholeContents();
        		if ( contents != null ) {
        			prg.append(contents);
        			if ( (contents.endsWith("\n") || contents.endsWith("\n\r")) == false ) {
        				prg.append("\n");
        			}
        		}
        	}
        }
        
        // then appends kernels
        for (Code code : model.getCodeList() ) {
        	if ( code instanceof Kernel ) {
        		final String contents = code.getWholeContents();
        		if ( contents != null ) prg.append(contents);
        	}
        }
        
        // Creation of openCL program
        program = clCreateProgramWithSource(context, 1, new String[]{ prg.toString() }, null, null);
        if(program == null) {
            throw new CLException("Impossible to create the program (clCreateProgramWithSource).");
        }
        // Build the program
        int err = CL_SUCCESS;
        String opt = null; //new String("-g"); //"-cl-opt-disable"); // Try new String("-g"); to allow the debugging
        err = clBuildProgram(program, 0, null, opt, null, null);

        // Retrieves build log
        long logSize[] = new long[1];
        CL.clGetProgramBuildInfo(program, device, CL.CL_PROGRAM_BUILD_LOG, 0, null, logSize);
        byte logData[] = new byte[(int)logSize[0]];
        CL.clGetProgramBuildInfo(program, device, CL.CL_PROGRAM_BUILD_LOG, logSize[0], Pointer.to(logData), null);
        log.warning(new String(logData, 0, logData.length-1));

        switch (err) {
            case CL_SUCCESS:
                break;

            case CL_INVALID_PROGRAM:
                throw new CLException("Invalid program object");

            case CL_INVALID_VALUE:
                throw new CLException("Device_list is NULL and num_devices is greater than zero, or if device_list is not NULL and num_devices is zero.");

            case CL_INVALID_DEVICE:
                throw new CLException("OpenCL devices listed in device_list are not in the list of devices associated with program.");

            case CL_INVALID_BINARY:
                throw new CLException("Program is created with clCreateWithProgramWithBinary and devices listed in device_list do not have a valid program binary loaded.");

            case CL_INVALID_BUILD_OPTIONS:
                throw new CLException("The build options specified by options are invalid.");

            case CL_INVALID_OPERATION:
                throw new CLException("The build of a program executable for any of the devices listed in device_list by a previous call to clBuildProgram for program has not completed.");

            case CL_COMPILER_NOT_AVAILABLE:
                throw new CLException("Program is created with clCreateProgramWithSource and a compiler is not available i.e. CL_DEVICE_COMPILER_AVAILABLE specified in the table of OpenCL Device Queries for clGetDeviceInfo is set to CL_FALSE.");

            case CL_BUILD_PROGRAM_FAILURE:
                throw new CLException("There is a failure to build the program executable. This error will be returned if clBuildProgram does not return until the build has completed.");

            case CL_OUT_OF_HOST_MEMORY:
                throw new CLException("There is a failure to allocate resources required by the OpenCL implementation on the host.");
        }
	}
    
    private void initMatrices() {
	    // Init of matrices with matrixInit values
		for(final Matrix matrix : getModel().getMatrixList() ) {
			matrix.setToInitialValues();
		}
	
	    // Creating java-side pointers to get data from openCL matrices 
	    final int matrixCount = getModel().getMatrixCount();
		matricesPointer = new Pointer[matrixCount];
		for ( int i=0; i<matrixCount; i++ ) {
			final Matrix matrix = getModel().getMatrix(i);
			if ( matrix instanceof MatrixInteger ) {
				matricesPointer[i] = Pointer.to(  ((MatrixInteger)matrix).getMatrix());
			} else if ( matrix instanceof MatrixULong ) {
				matricesPointer[i] = Pointer.to(  ((MatrixULong)matrix).getMatrix());
			} else if ( matrix instanceof MatrixFloat ) {
				matricesPointer[i] = Pointer.to(  ((MatrixFloat)matrix).getMatrix());
			}
		}
	}

	private void initCL() throws EvaluationException, ParseException {
         long numBytes[] = new long[1];

        // Creation of an OpenCL context on GPU
        log.log("Obtaining platform...");
        cl_platform_id platforms[] = new cl_platform_id[1];
        clGetPlatformIDs(platforms.length, platforms, null);
        if(platforms[0]==null) {
        	throw new CLException("No OpenCL plateform found. Impossible to compile the code.");
    	} else {
    		String platformName = CLUtil.getString(platforms[0], CL_PLATFORM_NAME);
    		log.log("Set plateform to " + platformName);
    	}
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platforms[0]);

        // Enable exceptions and subsequently get error checks
        CL.setExceptionsEnabled(true);
        
        final Model model = getModel();
		final Scheduler scheduler = model.getScheduler();

        // checks if device exist
        final List<cl_device_id> hardwareList = CLUtil.selectHardware(scheduler.getDevice());
        if ( hardwareList.isEmpty() ) {
        	throw new CLException("No suitable OpenCL device found. Impossible to compile the code.");
        }
        int order = scheduler.getDeviceOrder();
        if ( order < 0 || order >= hardwareList.size() ) {
        	order = 0;
        }
		device = hardwareList.get(order);
        context = clCreateContext(null, 1, new cl_device_id[] { device }, null, null, null);

        if (context == null) {
            throw new CLException("Can't create an openCL context.");
        }
        
        // Get the list of devices associated with the context
        clGetContextInfo(context, CL_CONTEXT_DEVICES, 0, null, numBytes);
        // Obtain the cl_device_id for the first device
        int numDevices = (int) numBytes[0] / Sizeof.cl_device_id;
        cl_device_id devices[] = new cl_device_id[numDevices];
        
        clGetContextInfo(context, CL_CONTEXT_DEVICES, numBytes[0], Pointer.to(devices), null);
        
        // *******************************
        log.log("Device used:" + CLUtil.getString(device, CL_DEVICE_NAME));

        // *******************************

        // Create a command-queue
        commandQueue = clCreateCommandQueue(context, device, CL.CL_NONE, null);

        // Initialization of pointers
        
        // Allocate the memory objects for the input- and output data
        final int matrixCount = model.getMatrixCount();
		log.log("Number of matrices: "+matrixCount);
        memObjects = new cl_mem[matrixCount];
        for(int t=0; t<matrixCount; t++) {
            Matrix mat = model.getMatrixList().get(t);
            int n = mat.getSizeXValue()*mat.getSizeYValue()*mat.getSizeZValue();
            if(mat instanceof MatrixInteger)
                memObjects[t] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int   * n, matricesPointer[t], null);
            if(mat instanceof MatrixULong)
            	memObjects[t] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_ulong   * n, matricesPointer[t], null);
            if(mat instanceof MatrixFloat)
                memObjects[t] = clCreateBuffer(context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * n, matricesPointer[t], null);
        }
    }
    
    private void initKernel() throws EvaluationException, ParseException {
    	 // Create the kernels
        clKernelsByName = new HashMap<String, cl_kernel>();
        
        final Model model = getModel();
        final int matrixCount = model.getMatrixCount();
		
        final List<Kernel> kernelList = model.getKernelList();
        kernels = new cl_kernel[kernelList.size()];
        for(int k=0; k<kernelList.size(); k++) {
        	final String name = kernelList.get(k).getName();
			
        	log.log("Creating kernel: " + name + ".");
            kernels[k] = clCreateKernel(program, name, null);
            if(nbSteps == initialStep) {
                clKernelsByName.put(name, kernels[k]);
            }

	        int numArg = 0;
	        clSetKernelArg(kernels[k], numArg++, Sizeof.cl_int, Pointer.to(new int[]{0})); // rand
	        clSetKernelArg(kernels[k], numArg++, Sizeof.cl_uint, Pointer.to(new int[]{0})); // t
	        clSetKernelArg(kernels[k], numArg++, Sizeof.cl_int, Pointer.to(new int[]{0})); // mouseX
	        clSetKernelArg(kernels[k], numArg++, Sizeof.cl_int, Pointer.to(new int[]{0})); // mouseY
	        clSetKernelArg(kernels[k], numArg++, Sizeof.cl_int, Pointer.to(new int[]{0})); // mouseZ
	        clSetKernelArg(kernels[k], numArg++, Sizeof.cl_int, Pointer.to(new int[]{0})); // mouseBtn
	        clSetKernelArg(kernels[k], numArg++, Sizeof.cl_int, Pointer.to(new int[]{0})); // key

			clSetKernelArg(kernels[k], numArg++, Sizeof.cl_int, Pointer.to(new int[]{0})); // workSizeX
			clSetKernelArg(kernels[k], numArg++, Sizeof.cl_int, Pointer.to(new int[]{0})); // workSizeY
			clSetKernelArg(kernels[k], numArg++, Sizeof.cl_int, Pointer.to(new int[]{0})); // workSizeZ

			if(matrixCount>0) {
	            for(int c=0; c<matrixCount; c++) {
	            	final Matrix mat = model.getMatrix(c);
	                clSetKernelArg(kernels[k], numArg++, Sizeof.cl_mem, Pointer.to(memObjects[c])); // Matrix
	                clSetKernelArg(kernels[k], numArg++, Sizeof.cl_uint, Pointer.to(new int[]{mat.getSizeXValue()})); // SX
	                clSetKernelArg(kernels[k], numArg++, Sizeof.cl_uint, Pointer.to(new int[]{mat.getSizeYValue()})); // SY
	                clSetKernelArg(kernels[k], numArg++, Sizeof.cl_uint, Pointer.to(new int[]{mat.getSizeZValue()})); // SZ
	            }
	        }
        }
    }

    private void initScheduler() {
		final Scheduler scheduler = getModel().getScheduler();
		
		if(scheduler.getTaskCount() <= 0) {
			throw new CLException("No task to execute.");
		}
		
		orderedTasks = new LinkedList<Task>();
		
		// constructs task list in order to be enqueued.
		final HashMap<Task, Integer> postoned = new HashMap<Task, Integer>();
		final LinkedList<Task> toOrder = new LinkedList<Task>(scheduler.getTaskList());
		int taskCount = scheduler.getTaskCount();
		
		// while there are tasks to order
		// bahh, using a label, I'm not happy with this solution
		global: 
		while (toOrder.isEmpty() == false ) {
			final Task task = toOrder.removeFirst();
			
			// checks if all deps are valid
			for ( Task in : task.getTaskInList() ) {
				int index = orderedTasks.indexOf(in);
				if (index < 0 ) {
					int postponedCount = postoned.containsKey(task) ? postoned.get(task) : 0;
					if ( postponedCount >= taskCount ) {
						// task has been postponed too much times, this is a cycle in scheduler
                        throw new CLException("SchedulerTest contains cycles.");
					} else {
						// previous task isn't ordered yet, postpone task
						postoned.put(task, postponedCount +1);
						toOrder.addLast(task);
						continue global;
					}
				}				
			}
			
			// task can be scheduled
			orderedTasks.add(task);
		}
	}

    private void initTasks() throws EvaluationException, ParseException {
    	globalSizeByTask = new HashMap<>();
    	eventsByTask = new HashMap<>();
    	dependenciesByTask = new HashMap<>();

    	final List<Task> taskList = orderedTasks;
    	
    	allEvents = new cl_event[taskList.size()];

        for (int n = 0; n < taskList.size(); n++) {
            final Task ta = taskList.get(n);

            // create global work size array
            final int gX = evaluateFormula(ta.getGlobalWorkSizeX());
            final int gY = evaluateFormula(ta.getGlobalWorkSizeY());
            final int gZ = evaluateFormula(ta.getGlobalWorkSizeZ());

            if (gZ > 1) {
                globalSizeByTask.put(ta, new long[]{gX, gY, gZ});
            } else if (gY > 1) {
                globalSizeByTask.put(ta, new long[]{gX, gY});
            } else {
                globalSizeByTask.put(ta, new long[]{gX});
            }

            // Creates events for each task
            final cl_event event = new cl_event();
            allEvents[n] = event;
            eventsByTask.put(ta, event);
        }

		// creates dependencies
		for ( Task ta : taskList ) {
			int size = ta.getTaskInCount();
			// keeps a null array when there is no dependencies.
			if ( size > 0 ) {
				cl_event[] deps = new cl_event[size];
				for ( int i=0; i<size; i++ ) {
					deps[i] = eventsByTask.get(ta.getTaskIn(i));
				}
				dependenciesByTask.put(ta, deps);
			}
		}
    }
    
    private void releaseCL() {
		// Release kernel, program, and memory objects
        System.out.println("[INFO] clReleaseMemObject:");
        if(memObjects != null) {
            for(int c=0; c<memObjects.length; c++) {
                CL.clReleaseMemObject(memObjects[c]);
            }
            memObjects = null;
        }
        System.out.println("[INFO] clReleaseKernel:");
        if(kernels != null) {
        	for(int k=0;k<kernels.length;k++) 
        		CL.clReleaseKernel(kernels[k]);
        }
        kernels = null;
        System.out.println("[INFO] clReleaseProgram:");
        if(program != null) {
        	CL.clReleaseProgram(program);
        	program = null;
        }
        System.out.println("[INFO] clReleaseCommandQueue:");
        if(commandQueue != null) {
        	CL.clReleaseCommandQueue(commandQueue); 
        	commandQueue = null;
        }
        System.out.println("[INFO] clReleaseContext:");
        if(context != null) {
        	CL.clReleaseContext(context);           
        	context = null;
        }
        System.out.println("[INFO] clRelease OK");

    }
    
    public void reset() {
    	
    	// Re-init the current matrix to the initial values
		for(final Matrix matrix : getModel().getMatrixList() ) {
			matrix.setToInitialValues();
		}
		
		clKernelsByName = null;
		    
		globalSizeByTask = null;
		    
		eventsByTask = null;
		allEvents = null;
		dependenciesByTask = null;
		    
		orderedTasks = null;

		releaseCL();
		
		nbSteps = initialStep;
		initialSimulationTime = -1;
		internalThread = null;
    }
    
    public int getNbSteps() {
    	return nbSteps;
    }
    
    public void setNbSteps(int t) {
    	nbSteps = t;
    }

    public int getInitialStep() {
    	return initialStep;
    }
    
    public void setInitialStep(int t) {
    	initialStep = t;
    }
    
    public long getInitialSimulationTime() {
    	return initialSimulationTime;
    }
    
    public void setInitialSimulationTime(long t) {
    	initialSimulationTime = t;
    }
    
    public int getRefreshStep() {
		return refreshStep;
	}
    
    public void setRefreshStep(int refreshStep) {
		this.refreshStep = refreshStep;
	}

	// integer parameters of the kernels
	boolean pt_init = false;
	int[] pt_rand;
	int[] pt_steps;
	int[] pt_mouseX;
	int[] pt_mouseY;
	int[] pt_mouseZ;
	int[] pt_mouseBtn;
	int[] pt_key;
	int[] pt_WSX;
	int[] pt_WSY;
	int[] pt_WSZ;

	// Execution mode for 1 step
    private int _currentTaskToExecute = 0;
    private int _executionMode = 0; // 0 = ALL TASKS, 1 = TASK by TASK

    public boolean executeStep(int mouseX, int mouseY, int mouseZ, int mouseBtn, int key) {
		int rand = 0xFF;
		if(pt_init == false) {
			pt_init = true;
			pt_rand = new int[1];
			pt_steps = new int[1];
			pt_mouseX = new int[1];
			pt_mouseY = new int[1];
			pt_mouseZ = new int[1];
			pt_mouseBtn = new int[1];
			pt_key = new int[1];
			pt_WSX = new int[1];
			pt_WSY = new int[1];
			pt_WSZ = new int[1];
		}
        if(_currentTaskToExecute == 0) {
            CL.clFlush(commandQueue);
        }
        Scheduler schedule = getModel().getScheduler();

        try {

            // First, we build the kernels parameters
            for (int tt = 0; tt < schedule.getTaskCount(); tt++) {
                Task task = schedule.getTask(tt);
                int repetition = evaluateFormula(task.getRepetition());
                if (repetition < 0) repetition = 0; // Protection


                for (int i = 0; i < repetition; i++) {
                    // Execute the kernel(s)
                    for (int ki = 0; ki < task.getKernelCount(); ki++) {
                        rand = Tools.rnd.nextInt(1073741824);
                        // Update parameters
                        pt_rand[0]      = rand;
                        pt_steps[0]     = nbSteps;
                        pt_mouseX[0]    = mouseX;
                        pt_mouseY[0]    = mouseY;
                        pt_mouseZ[0]    = mouseZ;
                        pt_mouseBtn[0]  = mouseBtn;
                        pt_key[0]       = key;
                        pt_WSX[0] = evaluateFormula(task.getGlobalWorkSizeX());
                        pt_WSY[0] = evaluateFormula(task.getGlobalWorkSizeY());
                        pt_WSZ[0] = evaluateFormula(task.getGlobalWorkSizeZ());
                        // Pass modified arguments that are not pointers (pointers do not need update)
                        int argNum = 0;
                        cl_kernel kernel = clKernelsByName.get(task.getKernel(ki).getName());
                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_rand));

                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_steps));
                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_mouseX));
                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_mouseY));
                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_mouseZ));
                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_mouseBtn));
                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_key));

                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_WSX));
                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_WSY));
                        clSetKernelArg(kernel, argNum++, Sizeof.cl_uint, Pointer.to(pt_WSZ));
                    }
                }
            }
            // Enqueue of all the tasks (schedule must be respected)
            if(_executionMode == 0) {
                executeAllTasks();
            } else {
                executeOneTask();
            }


        } catch (ParseException | EvaluationException e) {
            log.error(DiagnosticUtil.createMessage(e));
            return false;
        }
        return true;
    }
    
    
    private void executeAllTasks() throws EvaluationException, ParseException {
    	for ( Task task : orderedTasks ) {
    		enqueueTask(task);
    	}
        nbSteps++;
    }

    private void executeOneTask() throws EvaluationException, ParseException {
        Task task = orderedTasks.get(_currentTaskToExecute);
        enqueueTask(task);
        _currentTaskToExecute++;
        if(_currentTaskToExecute == orderedTasks.size()) {
            _currentTaskToExecute = 0;
            nbSteps++;
        }
    }

    private void enqueueTask(final Task task) throws EvaluationException, ParseException, CLException {
        int repetition = evaluateFormula(task.getRepetition());
        if (repetition <= 0) repetition = 1;

        for (int i = 0; i < repetition; i++) {
            // Generate a list of successive integers (indexes) if random execution is selected
            ArrayList<Integer> listIndexes = null;
            if(task.isRandom() == true) {
                listIndexes = new ArrayList<Integer>(task.getKernelCount());
                for (int ii = 0; ii < task.getKernelCount(); ii++) {
                    listIndexes.add(ii);
                }
            }
            // Execute the kernel(s)
            // Kernels time duration
            for (int ki = 0; ki < task.getKernelCount(); ki++) {
                // If required, select at random an index of kernel to execute
                int kerIndex = ki;
                if(task.isRandom() == true) {
                    int p = Tools.rnd.nextInt(listIndexes.size());
                    kerIndex = listIndexes.get(p).intValue();
                    listIndexes.remove(p);
                }
                Kernel ker = task.getKernel(kerIndex);
                final cl_kernel kernel = clKernelsByName.get(ker.getName());

                final long[] global_work_size = globalSizeByTask.get(task);
                final int dimension = global_work_size.length;

                final cl_event event = eventsByTask.get(task);
                final cl_event[] dependencies = dependenciesByTask.get(task);
                final int num_events_in_wait_list = dependencies == null ? 0 : dependencies.length;

                long time_before_exe = System.currentTimeMillis();
                
                    final int error = clEnqueueNDRangeKernel(
                            commandQueue, kernel,
                            dimension, null, global_work_size, null,
                            //dimension, null, global_work_size, local_work_size,
                            0, null, null
                            //num_events_in_wait_list, dependencies, event
                    );
                    CL.clFinish(commandQueue);
                    
                long time_after_exe = System.currentTimeMillis();
                ker.setDuration(time_after_exe - time_before_exe);
                    if (error != 0) {
                        throw new CLException("Error in launchTask:" + error + ".", error);
                    }
            }
        }
        // Put the durations of all executed tasks
    }
      
    public String GetResultCL(List<Matrix> lst_mat) {
        // Read the output data
        if(commandQueue != null && memObjects != null && matricesPointer != null)
        try {
            for(int i=0; i<lst_mat.size(); i++) {
            	Matrix mat = lst_mat.get(i);
                int size = mat.getSizeXValue() * mat.getSizeYValue() * mat.getSizeZValue();
            	if(mat instanceof MatrixInteger) {
                    clEnqueueReadBuffer(commandQueue, memObjects[i], CL.CL_TRUE, 0, size * Sizeof.cl_int, matricesPointer[i], 0, null, null);
					//clFinish(commandQueue);
            	}
            	if(mat instanceof MatrixULong) {
            		clEnqueueReadBuffer(commandQueue, memObjects[i], CL.CL_TRUE, 0, size * Sizeof.cl_ulong, matricesPointer[i], 0, null, null);
					//clFinish(commandQueue);
            	}
            	if(mat instanceof MatrixFloat) {
            		clEnqueueReadBuffer(commandQueue, memObjects[i], CL.CL_TRUE, 0, size * Sizeof.cl_float, matricesPointer[i], 0, null, null);
					//clFinish(commandQueue);
            	}
            }
        } catch (Exception ex) {
            return ex.toString();
        }
        return null;
    }
    
    public boolean canCompile() {
    	return CLUtil.isClPresent() && isStarted() == false;
    }
    
    public boolean canRun() {
    	return CLUtil.isClPresent() && isStarted() == false;
    }
    
    public synchronized boolean isStarted() {
		return internalThread != null;
	}
    
    public boolean isRunning() {
		return running;
	}
    
    public void setRunning(boolean running) {
		this.running = running;
	}
    
    
    public synchronized void start(ActionMonitor monitor) {
    	this.monitor = monitor == null ? ActionMonitor.empty : monitor;
    	
    	setRunning(true);
    	internalThread = new Thread(this);
    	internalThread.start();
    }
    
    public synchronized void stop() {
    	if(internalThread != null)
    		internalThread.interrupt();
    	internalThread = null;
    }

    public void run() {
    	try {
    		log.log("Simulation started.");
	    	monitor.setTaskName("Simulating");
	    	while( isStarted() ) {
	    		if ( isRunning() ) {
	    			
	    			executeStep(inputProvider.getMouseX(), inputProvider.getMouseY(), inputProvider.getMouseZ(), inputProvider.getButton(), inputProvider.getKey());
	    			// if step failed, break infinite loop.

	    			if( nbSteps % refreshStep == 0 ) {
	    				GetResultCL(getModel().getMatrixList());
						if(recordingMPEG == true)
							log.recordMPEG();
	
					}
					if(recordingPNG == true)
						log.recordPNG();
	    		} else {
	    			try { 
	    				Thread.sleep(250); 
	    			} catch (InterruptedException e) {
	    				log.log("Simulation stopped.");
	    			}
	    		}
	    	}
	    	
    	} catch (Throwable e) {
    		// catch any exception.
    		log.error(DiagnosticUtil.createMessage(e));
    	} finally {
    		// what ever happen, reset every thing
    		reset();
       		log.log("Simulation ended.");
       		monitor.done();
    	}
    }
}
