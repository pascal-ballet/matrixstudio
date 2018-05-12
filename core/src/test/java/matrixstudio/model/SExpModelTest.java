package matrixstudio.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.function.BiFunction;
import matrixstudio.formula.EvaluationException;
import matrixstudio.kernel.SExpModelLoader;
import matrixstudio.kernel.SExpModelSaver;
import org.junit.Assert;
import org.junit.Test;

public class SExpModelTest {

	protected Model loadAndSave(Model source) throws IOException, EvaluationException, ParseException {
		Path modelPath = Files.createTempDirectory("loadAndSave" + source.hashCode());

		SExpModelSaver saver = new SExpModelSaver(source, modelPath);
		saver.saveModel();

		SExpModelLoader loader = new SExpModelLoader(modelPath);
		Model loaded = loader.readModel();

		Assert.assertEquals(source.getParameterCount(), loaded.getParameterCount());
		for (int i = 0; i < loaded.getParameterCount(); i++) {
			Parameter loadedParameter = loaded.getParameter(i);
			Parameter sourceParameter = source.getParameter(i);
			Assert.assertEquals(sourceParameter.getName(), loadedParameter.getName());
			Assert.assertEquals(sourceParameter.getFormula(), loadedParameter.getFormula());
		}

		Assert.assertEquals(source.getCodeCount(), loaded.getCodeCount());
		for (int i = 0; i < loaded.getCodeCount(); i++) {
			Code loadedCode = loaded.getCode(i);
			Code sourceCode = source.getCode(i);
			Assert.assertEquals(sourceCode.getName(), loadedCode.getName());
			Assert.assertEquals(sourceCode.getContents(), loadedCode.getContents());
		}

		Assert.assertEquals(source.getMatrixCount(), loaded.getMatrixCount());
		for (int i = 0; i < loaded.getMatrixCount(); i++) {
			Matrix loadedMatrix = loaded.getMatrix(i);
			Matrix sourceMatrix = source.getMatrix(i);
			Assert.assertEquals(sourceMatrix.getClass(), loadedMatrix.getClass());
			Assert.assertEquals(sourceMatrix.getName(), loadedMatrix.getName());
			Assert.assertEquals(sourceMatrix.getSizeX(), loadedMatrix.getSizeX());
			Assert.assertEquals(sourceMatrix.getSizeY(), loadedMatrix.getSizeY());
			Assert.assertEquals(sourceMatrix.getSizeZ(), loadedMatrix.getSizeZ());

			if (loadedMatrix instanceof MatrixInteger) {
				Assert.assertArrayEquals(((MatrixInteger) sourceMatrix).getMatrixInit(), ((MatrixInteger) loadedMatrix).getMatrixInit());
				//Assert.assertArrayEquals(((MatrixInteger) sourceMatrix).getMatrix(), ((MatrixInteger) loadedMatrix).getMatrix());

			} else if (loadedMatrix instanceof MatrixFloat) {
				Assert.assertArrayEquals(((MatrixFloat) sourceMatrix).getMatrixInit(), ((MatrixFloat) loadedMatrix).getMatrixInit(), 1e-5f);
				//Assert.assertArrayEquals(((MatrixFloat) sourceMatrix).getMatrix(), ((MatrixFloat) loadedMatrix).getMatrix(), 1e-5f);

			} else if (loadedMatrix instanceof MatrixULong) {
				Assert.assertArrayEquals(((MatrixULong) sourceMatrix).getMatrixInit(), ((MatrixULong) loadedMatrix).getMatrixInit());
				//Assert.assertArrayEquals(((MatrixULong) sourceMatrix).getMatrix(), ((MatrixULong) loadedMatrix).getMatrix());
			}
		}

		Scheduler sourceScheduler = source.getScheduler();
		Scheduler loadedScheduler = loaded.getScheduler();

		Assert.assertEquals(sourceScheduler.getTaskCount(), loadedScheduler.getTaskCount());
		for (int i = 0; i < loadedScheduler.getTaskCount(); i++) {
			Task loadedTask = loadedScheduler.getTask(i);
			Task sourceTask = sourceScheduler.getTask(i);

			Assert.assertEquals(sourceTask.getKernelCount(), loadedTask.getKernelCount());
			Assert.assertEquals(sourceTask.getTaskInCount(), loadedTask.getTaskInCount());
			Assert.assertEquals(sourceTask.getTaskOutCount(), loadedTask.getTaskOutCount());
			Assert.assertArrayEquals(sourceTask.getPosition(), loadedTask.getPosition(), 1e-5f);

		}
		return loaded;
	}

	@Test
	public void loadAndSaveEmpty() throws Exception {
		loadAndSave(new Model());
	}

	@Test
	public void createLoadAndSaveParameter() throws Exception {
		Model source = new Model();
		source.addParameterAndOpposite(createParameter("p1", "42"));
		source.addParameterAndOpposite(createParameter("p2", "42+42"));

		loadAndSave(source);
	}

	@Test
	public void createLoadAndSaveSchedulerAndCode() throws Exception {
		Model source = new Model();
		source.addCodeAndOpposite(createLibrary("Library", "// Code here"));
		source.addCodeAndOpposite(createKernel("Kernel1", "// Kernel 1"));
		source.addCodeAndOpposite(createKernel("Kernel2", "// Kernel 2"));
		createScheduler(source);

		loadAndSave(source);
	}

	@Test
	public void createLoadAndSaveMatrices() throws Exception {
		Model source = new Model();

		source.addMatrixAndOpposite(createMatrixInteger("Matrix1", 50, 50, (i,j) -> i%2==0 ? i+j : j%2== 0 ? Integer.MAX_VALUE-1 : Integer.MIN_VALUE+1));
		source.addMatrixAndOpposite(createMatrixFloat("Matrix2", 50, 50, (i,j) -> (float) Math.sqrt(i+j)));
		source.addMatrixAndOpposite(createMatrixULong("Matrix3", 50, 50, (i,j) -> i%2==0 ? (long) i+j : j%2== 0 ? Long.MAX_VALUE-1 : Long.MIN_VALUE+1));

		loadAndSave(source);
	}

	@Test
	public void createLoadAndSaveAll() throws Exception {
		Model source = new Model();
		source.addParameterAndOpposite(createParameter("p1", "42"));
		source.addParameterAndOpposite(createParameter("p2", "42+42"));

		source.addCodeAndOpposite(createLibrary("Library", "// Code here"));
		source.addCodeAndOpposite(createKernel("Kernel1", "// Kernel 1"));
		source.addCodeAndOpposite(createKernel("Kernel2", "// Kernel 2"));
		createScheduler(source);

		source.addMatrixAndOpposite(createMatrixInteger("Matrix1", 50, 50, (i,j) -> i%2==0 ? i+j : j%2== 0 ? Integer.MAX_VALUE-1 : Integer.MIN_VALUE+1));
		source.addMatrixAndOpposite(createMatrixFloat("Matrix2", 50, 50, (i,j) -> (float) Math.sqrt(i+j)));
		source.addMatrixAndOpposite(createMatrixULong("Matrix3", 50, 50, (i,j) -> i%2==0 ? (long) i+j : j%2== 0 ? Long.MAX_VALUE-1 : Long.MIN_VALUE+1));

		loadAndSave(source);
	}

	private Parameter createParameter(String name, String formula) {
		Parameter parameter = new Parameter();
		parameter.setName(name);
		parameter.setFormula(formula);
		return parameter;
	}

	private Kernel createKernel(String name, String contents) {
		Kernel kernel = new Kernel();
		kernel.setName(name);
		kernel.setContents(contents);
		return kernel;
	}

	private Library createLibrary(String name, String contents) {
		Library library = new Library();
		library.setName(name);
		library.setContents(contents);
		return library;
	}

	protected MatrixInteger createMatrixInteger(String name, int x, int y, BiFunction<Integer, Integer, Integer> filler) {
		MatrixInteger matrix = new MatrixInteger();
		matrix.setName(name);
		matrix.setSizeX(Integer.toString(x));
		matrix.setSizeY(Integer.toString(y));
		matrix.setSizeZ(Integer.toString(1));
		matrix.initBlank(true);
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				matrix.setInitValueAt(i, j, 0, filler.apply(i, j));
			}
		}
		return matrix;
	}

	protected MatrixFloat createMatrixFloat(String name, int x, int y, BiFunction<Integer, Integer, Float> filler) {
		MatrixFloat matrix = new MatrixFloat();
		matrix.setName(name);
		matrix.setSizeX(Integer.toString(x));
		matrix.setSizeY(Integer.toString(y));
		matrix.setSizeZ(Integer.toString(1));
		matrix.initBlank(true);
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				matrix.setInitValueAt(i, j, 0, filler.apply(i, j));
			}
		}
		return matrix;
	}

	protected MatrixULong createMatrixULong(String name, int x, int y, BiFunction<Integer, Integer, Long> filler) {
		MatrixULong matrix = new MatrixULong();
		matrix.setName(name);
		matrix.setSizeX(Integer.toString(x));
		matrix.setSizeY(Integer.toString(y));
		matrix.setSizeZ(Integer.toString(1));
		matrix.initBlank(true);
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				matrix.setInitValueAt(i, j, 0, filler.apply(i, j));
			}
		}
		return matrix;
	}

	private void createScheduler(Model model) {
		Task task1 = new Task();
		task1.setPosition(new float[] {100f,200f});
		Task task2 = new Task();
		task2.setPosition(new float[] {300f,100f});
		Task task3 = new Task();
		task3.setPosition(new float[] {300f,300f});
		Task task4 = new Task();
		task4.setPosition(new float[] {500f,200f});

		task1.addTaskOut(task2);
		task1.addTaskOut(task3);

		task2.addTaskIn(task1);
		task2.addTaskOut(task4);

		task3.addTaskIn(task1);
		task3.addTaskOut(task4);

		task4.addTaskIn(task1);
		task4.addTaskIn(task2);

		Scheduler scheduler = model.getScheduler();
		scheduler.addTaskAndOpposite(task1);
		scheduler.addTaskAndOpposite(task2);
		scheduler.addTaskAndOpposite(task3);
		scheduler.addTaskAndOpposite(task4);

		int current = 0;
		List<Kernel> kernelList = model.getKernelList();
		for (Task task : scheduler.getTaskList()) {
			if (current < kernelList.size()) {
				task.addKernel(kernelList.get(current));
				current += 1;
			}
			if (current >= kernelList.size()) {
				current = 0;
			}
		}

	}
}
