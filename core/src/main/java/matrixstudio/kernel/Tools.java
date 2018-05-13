package matrixstudio.kernel;

import fr.minibilles.basics.serializer.JBoost;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import matrixstudio.model.Kernel;
import matrixstudio.model.Library;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.Model;
import matrixstudio.model.Task;

public class Tools {

    public static final int BOOST_VERSION = 6;

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
	
		final Kernel kernel1 = new Kernel();
		kernel1.setName("Kernel1");
		kernel1.setContents(INITIAL_KERNEL);
		model.addCodeAndOpposite(kernel1);
		
		
		final Task task1 = new Task();
		task1.addKernel(kernel1);
		task1.setPosition(new float[] { 360f, 120f });
		model.getScheduler().addTaskAndOpposite(task1);
	
		// Add libraries
		Library lib4 = new Library();
		lib4.setName("Library");
		lib4.setContents("// Adds your functions here.\n");
		model.addCodeAndOpposite(lib4);
	
		return model;
	}
	
	private static final String INSIDE_NAME = "contents.umss";
	
	private static JBoost createBoost(boolean safeRead) {
		return new MSBoost("MatrixStudio", BOOST_VERSION, safeRead);
	}
	
	/**
	 * <p>Loads a file '.mss' as a MatrixStudio model.</p>
	 * @param file to file
	 * @param safeRead only tries to read kernels and libraries to read corrupted files (may not work).
	 * @return a {@link Model}
	 * @throws IOException if something goes wrong.
	 */
	public static Model loadMssFile(File file, boolean safeRead) throws IOException {
		final JBoost boost = createBoost(safeRead);
		final ZipInputStream stream = new ZipInputStream(new FileInputStream(file));
		try {
			boost.initializeZippedReading(stream, INSIDE_NAME);
			return boost.readObject(Model.class);
		} finally {
			boost.close();
		}
	}

	/**
	 * <p>Loads a file 'matrixstudio.simulation' as a MatrixStudio model.</p>
	 * @param path from path
	 * @return a {@link Model}
	 * @throws IOException if something goes wrong.
	 */
	public static Model loadMatrixStudioSimulationFile(Path path) throws IOException {
		SExpModelLoader loader = new SExpModelLoader(path);
		return loader.readModel();
	}

	/**
	 * <p>Loads a file as a MatrixStudio model using '.mss' or
	 * 'matrixstudio.simulation' depending on the path extension.</p>
	 * @param path file path to load
	 * @return a model
	 * @throws IOException if something goes wrong
	 */
	public static Model load(Path path) throws IOException {
		String filename = path.getFileName().toString();
		if (filename.endsWith(".mss")) {
			return loadMssFile(path.toFile(), false);
		} else if (filename.equals("matrixstudio.simulation")) {
			return loadMatrixStudioSimulationFile(path);
		} else {
			throw new IOException("Unknown file type '"+ path +"'");
		}
	}

	/**
	 * <p>Saves a MatrixStudio model to a file.</p>
	 * @param model to file
	 * @param file where to file
	 * @throws IOException if something goes wrong.
	 */
	public static void saveMssFile(Model model, File file) throws IOException {
		// saves model to tempory file
		File tmpFile = File.createTempFile("matrixstudio", "mss");
		final JBoost boost = createBoost(false);
		final ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(tmpFile));
		try {
			boost.initializeZippedWriting(stream, INSIDE_NAME);
			boost.writeObject(model);
		} finally {
			boost.close();
		}

		// no error occurred, copy the temp file to given file
		if (file.exists()) {
			file.delete();
		}
		if (!tmpFile.renameTo(file)) {
			throw new IOException("Couldn't write file '"+ file + "'");
		}
	}

	/**
	 * <p>Save model to a file 'matrixstudio.simulation'.</p>
	 * @param path to path
	 * @param model the {@link Model} to save
	 * @throws IOException if something goes wrong.
	 */
	public static void saveMatrixStudioSimulationFile(Model model, Path path) throws IOException {
		SExpModelSaver saver = new SExpModelSaver(model, path);
		saver.saveModel();
	}

	/**
	 * <p>Saves MatrixStudio model to a file using '.mss' or
	 * 'matrixstudio.simulation' depending on the path extension.</p>
	 * @param path file path to save
	 * @param model a model to save
	 * @throws IOException if something goes wrong
	 */
	public static void save(Model model, Path path) throws IOException {
		String filename = path.getFileName().toString();
		if (filename.endsWith(".mss")) {
			saveMssFile(model, path.toFile());
		} else if (filename.equals("matrixstudio.simulation")) {
			saveMatrixStudioSimulationFile(model, path);
		} else {
			throw new IOException("Unknown file type '"+ path +"'");
		}
	}
}
