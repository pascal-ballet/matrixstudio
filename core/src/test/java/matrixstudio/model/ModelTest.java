package matrixstudio.model;

import java.io.File;
import matrixstudio.kernel.Tools;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests loadMssFile and saveMssFile models
 */
public class ModelTest {

    @Test
    public void test1() throws Exception {
        Model model = createModel();

        File file = File.createTempFile("matrixstudio", ".mss");
        Tools.saveMssFile(model,file);

        Model loaded = Tools.loadMssFile(file, false);

        Assert.assertEquals(1, loaded.getMatrixCount());
        Assert.assertEquals(1, loaded.getParameterCount());
        Assert.assertEquals(1, loaded.getCodeCount());
        Assert.assertEquals(1, loaded.getScheduler().getTaskCount());

    }

    private Model createModel() {
        Model model = new Model();

        Parameter p1 = new Parameter();
        p1.setName("p1");
        p1.setFormula("10+10");
        model.addParameterAndOpposite(p1);

        Matrix m1 = new MatrixInteger();
        m1.setName("matrix1");
        model.addMatrixAndOpposite(m1);

        Kernel k1 = new Kernel();
        k1.setName("kernel1");
        model.addCodeAndOpposite(k1);

        Task t1 = new Task();
        t1.addKernel(k1);
        model.getScheduler().addTaskAndOpposite(t1);

        return model;
    }

}
