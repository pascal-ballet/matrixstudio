package matrixstudio.model;

import java.nio.file.Paths;
import matrixstudio.kernel.SExpModelLoader;
import org.junit.Assert;
import org.junit.Test;

public class SExpModelTest {

	@Test
	public void loadModel1() throws Exception {
		SExpModelLoader loader = new SExpModelLoader(Paths.get("tests/model1/"));
		Model model = loader.readModel();

		Assert.assertEquals(3, model.getCodeCount());
		Assert.assertEquals("Library", model.getCode(0).getName());
		Assert.assertEquals("Kernel1", model.getCode(1).getName());
		Assert.assertEquals("Kernel2", model.getCode(2).getName());

		Assert.assertEquals(4, model.getScheduler().getTaskCount());
		Assert.assertEquals(2, model.getScheduler().getTask(0).getTaskOutCount());
		Assert.assertEquals(2, model.getScheduler().getTask(3).getTaskInCount());
	}
}
