package matrixstudio.model;

import matrixstudio.kernel.Tools;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Tests load and save models
 */
public class ModelTest {

    @Test
    public void test1() throws Exception {
        Model model = createModel();

        File file = File.createTempFile("matrixstudio", ".mss");
        Tools.save(model,file);

        Model loaded = Tools.load(file);

        Assert.assertEquals(1, loaded.getMatrixCount());
        Assert.assertEquals(1, loaded.getCodeCount());
        Assert.assertEquals(1, loaded.getScheduler().getTaskCount());

    }

    private Model createModel() {
        Model model = new Model();

        Matrix m1 = new MatrixInteger();
        m1.setName("matrix1");
        model.addMatrix(m1);

        Scheduler scheduler = new Scheduler();
        model.setScheduler(scheduler);

        Kernel k1 = new Kernel();
        k1.setName("kernel1");
        model.addCode(k1);

        Task t1 = new Task();
        t1.addKernel(k1);
        scheduler.addTask(t1);

        return model;
    }

}
