package matrixstudio.test;

import java.io.File;
import java.io.IOException;

import matrixstudio.export.JavaExporter;
import matrixstudio.kernel.Tools;
import matrixstudio.model.Model;

import org.junit.Test;

public class JavaExporterTest {

	@Test
	public void test() throws IOException {
		// Exports Toto.mss 
		Model model = Tools.load(new File("file-test/Toto.mss"));
		JavaExporter exporter = new JavaExporter(model, "toto", new File("src-gen"));
		exporter.export();
	}

}
