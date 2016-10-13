package matrixstudio.kernel;

import matrixstudio.model.Kernel;
import matrixstudio.model.Library;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.Model;
import matrixstudio.model.Scheduler;
import matrixstudio.model.Task;
import org.xid.basics.serializer.JBoost;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Tools {

    public static final int BOOST_VERSION = 3;

	public static final Random rnd = new Random();
	public static final String INITIAL_KERNEL =
					"\tuint x = get_global_id(0);\n" +
					"\tuint y = get_global_id(1);\n" +
					"\tuint z = get_global_id(2);\n\n" +
					"\t// Position in matrix\n" +
					"\tuint p = x + y*workSizeX + z*workSizeX*workSizeY;\n\n" +
					"\t// Work here"
					;

	public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	public static final Pattern functionPrototypePattern = Pattern.compile(
			"^\\s*(u?int|float|bool|void)\\s+([a-zA-Z0-9�]+)\\([^\\)]*\\)\\s*\\{?$"
	);
	
	/**
	 * <p>Creates an new model with some basics contents.</p> 
	 */
	public static Model createEmptyModel()  {
		Model model = new Model();
		
		MatrixInteger matrix1 = new MatrixInteger();
		matrix1.setName("Matrix1");
		//matrix1.setRandom(true);
        matrix1.initBlank(false);
		model.addMatrixAndOpposite(matrix1);
	
		// Creation of a basic scheduler
		final Scheduler scheduler = new Scheduler();
		model.setSchedulerAndOpposite(scheduler);
			
		final Kernel kernel1 = new Kernel();
		kernel1.setName("Kernel1");
		kernel1.setContents(INITIAL_KERNEL);
		model.addCodeAndOpposite(kernel1);
		
		
		final Task task1 = new Task();
		task1.addKernel(kernel1);
		task1.setPosition(new float[] { 360f, 120f });
		scheduler.addTaskAndOpposite(task1);    	
	
		// Add libraries
		Library lib4 = new Library();
		lib4.setName("Library");
		lib4.setContents("// Adds your functions here.\n");
		model.addCodeAndOpposite(lib4);
	
		return model;
	}
	
	private static final String INSIDE_NAME = "contents.umss";
	
	private static JBoost createBoost() {
		return new MSBoost("MatrixStudio", BOOST_VERSION);
	}
	
	/**
	 * <p>Loads a file as a MatrixStudio model.</p>
	 * @param file to load
	 * @return a {@link Model}
	 * @throws IOException if something goes wrong.
	 */
	public static Model load(File file) throws IOException {
		final JBoost boost = createBoost();
		final ZipInputStream stream = new ZipInputStream(new FileInputStream(file));
		try {
			boost.initializeZippedReading(stream, INSIDE_NAME);
			return boost.readObject(Model.class);
		} finally {
			boost.close();
		}
	}
	
	/**
	 * <p>Saves a MatrixStudio model to a file.</p>
	 * @param model to save
	 * @param file where to save
	 * @throws IOException if something goes wrong.
	 */
	public static void save(Model model, File file) throws IOException {
		final JBoost boost = createBoost();
		final ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(file));
		try {
			boost.initializeZippedWriting(stream, INSIDE_NAME);
			boost.writeObject(model);
		} finally {
			boost.close();
		}
	}
}
