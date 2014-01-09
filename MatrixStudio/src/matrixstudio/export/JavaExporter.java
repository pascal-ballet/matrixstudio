package matrixstudio.export;

import java.io.File;
import java.io.IOException;

import matrixstudio.model.Kernel;
import matrixstudio.model.Library;
import matrixstudio.model.Matrix;
import matrixstudio.model.Model;

import org.xid.basics.generation.java.DependencyManager;
import org.xid.basics.generation.java.Java;
import org.xid.basics.generation.java.JavaContentFormatter;
import org.xid.basics.generation.java.JavaContentHandler;
import org.xid.basics.generation.java.JavaContentWriter;

/**
 * <p>Generates Java code that embeds the given {@link Model} into Java code.
 * The generated Java codes uses JOCL as OpenCL wrapper.</p>
 * 
 * TODO no output handled for now.
 * 
 * @author charlie
 *
 */
public class JavaExporter {

	private final Model model;
	
	private final String basePackage;
	
	private final JavaContentHandler content;
	
	private final DependencyManager dependencyManager;
	
	public JavaExporter(Model model, String basePackage, File outputFolder) {
		this.model = model;
		this.basePackage = basePackage;

		final JavaContentWriter writer = new JavaContentWriter(outputFolder);
		content = new JavaContentFormatter(writer);
		
		dependencyManager = new DependencyManager();
	}
	
	public void export() throws IOException {
		content.beginPackage(basePackage);
		
		// exports code as resources
		for ( Kernel kernel : model.getKernelList() ) {
			// binary file isn't adapted for dynamic content.
			// TODO uses binaryFile when basics is updated.
			final String fileName = kernel.getName() + ".kernel";
			content.beginFile(fileName);
			content.code(kernel.getWholeContents());
			content.endFile(fileName);
		}
		
		for ( Library kernel : model.getLibraryList() ) {
			// binary file isn't adapted for dynamic content.
			// TODO uses binaryFile when basics is updated.
			final String fileName = kernel.getName() + ".library";
			content.beginFile(fileName);
			content.code(kernel.getWholeContents());
			content.endFile(fileName);
		}

		// creates class for SchedulerTest it contains matrices handling.
		genarateScheduler();

		content.endPackage(basePackage);
	}
	
	private void genarateScheduler() {
		final String name = "SchedulerTest";
		
		// creates SchedulerTest file and class
		content.beginFile(name + ".java");
		content.markImports();
		
		content.beginClass(Java.PUBLIC, name, null,  null);

		final String cl = dependencyManager.getShortName("org.jocl.CL");
		final String sizeOf = dependencyManager.getShortName("org.jocl.Sizeof");

		final String clMen = dependencyManager.getShortName("org.jocl.cl_mem");
		final String clContext = dependencyManager.getShortName("org.jocl.cl_context");

		// adds context fieds
		content.beginAttribute(Java.PRIVATE, clContext, "context");
		content.endAttribute("context");
		
		// adds matrices fields
		for ( Matrix matrix : model.getMatrixList() ) {
			// FIXME handle safe names
			final String matrixName = lowerCaseName(matrix.getName());
			content.beginAttribute(Java.PRIVATE, clMen, matrixName);
			content.endAttribute(matrixName);
		}
		

		// TODO creates init CL method
		// TODO creates init pointer method
		
		// TODO creates init memory method
		content.beginMethod(Java.PRIVATE, "void", "initializeMemory", null);
		for ( Matrix matrix : model.getMatrixList() ) {
			final String matrixName = lowerCaseName(matrix.getName());
			content.codeln(0, matrixName + " = " + cl + "." + "clCreateBuffer(context, CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, "+ sizeOf +".cl_"+ matrix.getCType()  +" + " + matrix.getSize() +", matricesPointer[t], null);");
		}
		content.endMethod("initializeMemory");
		
		
		// ends SchedulerTest file and class
		content.endClass(name);
		
		handleImports();
		content.endFile(name + ".java");
	}
	
	private void handleImports() {
		for ( String import_ : dependencyManager.getJavaImports() ) {
			content.import_(Java.NONE, import_);
		}
		dependencyManager.clear();
	}
	
	private String lowerCaseName(String name) {
		if (name==null || name.length() == 0 ) return name;
		name = name.substring(0, 1).toLowerCase() + name.substring(1);
		return name;
	}
}
