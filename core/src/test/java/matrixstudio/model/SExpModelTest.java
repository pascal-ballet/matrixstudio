package matrixstudio.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import matrixstudio.kernel.SExpModelLoader;
import matrixstudio.kernel.SExpModelSaver;
import org.junit.Assert;
import org.junit.Test;

public class SExpModelTest {

	protected Model loadAndSave(Model source) throws IOException {
		Path modelPath = Files.createTempDirectory("loadAndSave1");

		SExpModelSaver saver = new SExpModelSaver(source, modelPath);
		saver.saveModel();

		SExpModelLoader loader = new SExpModelLoader(modelPath);
 		return loader.readModel();
	}

	@Test
	public void loadAndSaveEmpty() throws Exception {
		Model model = loadAndSave(new Model());

		Assert.assertEquals(0, model.getParameterCount());
		Assert.assertEquals(0, model.getCodeCount());
		Assert.assertEquals(0, model.getMatrixCount());
		Assert.assertEquals(0, model.getScheduler().getTaskCount());
	}

	@Test
	public void createLoadAndSave1() throws Exception {

		Model source = new Model();
		source.addParameterAndOpposite(createParameter("p1", "42"));
		source.addCodeAndOpposite(createLibrary("Library", "// Code here"));
		source.addCodeAndOpposite(createKernel("Kernel1", "// Kernel 1"));
		source.addCodeAndOpposite(createKernel("Kernel2", "// Kernel 2"));
		createScheduler(source);

		Model loaded = loadAndSave(source);

		Assert.assertEquals(3, loaded.getCodeCount());
		Assert.assertEquals("Library", loaded.getCode(0).getName());
		Assert.assertEquals("Kernel1", loaded.getCode(1).getName());
		Assert.assertEquals("Kernel2", loaded.getCode(2).getName());
		Assert.assertEquals("// Kernel 2", loaded.getCode(2).getContents());

		Assert.assertEquals(4, loaded.getScheduler().getTaskCount());
		Assert.assertEquals(2, loaded.getScheduler().getTask(0).getTaskOutCount());
		Assert.assertEquals(2, loaded.getScheduler().getTask(3).getTaskInCount());
		Assert.assertEquals(1, loaded.getScheduler().getTask(2).getKernelCount());
		Assert.assertEquals(300f, loaded.getScheduler().getTask(1).getPosition()[0], 1e-5);
		Assert.assertEquals(100f, loaded.getScheduler().getTask(1).getPosition()[1], 1e-5);
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
