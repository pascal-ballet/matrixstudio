package matrixstudio.kernel;

import fr.minibilles.basics.sexp.S;
import fr.minibilles.basics.sexp.SExp;
import fr.minibilles.basics.sexp.SList;
import fr.minibilles.basics.sexp.model.ModelToSExp;
import fr.minibilles.basics.sexp.model.Referencer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import matrixstudio.model.Code;
import matrixstudio.model.Device;
import matrixstudio.model.Kernel;
import matrixstudio.model.Matrix;
import matrixstudio.model.MatrixFloat;
import matrixstudio.model.MatrixInteger;
import matrixstudio.model.MatrixULong;
import matrixstudio.model.Model;
import matrixstudio.model.Parameter;
import matrixstudio.model.Scheduler;
import matrixstudio.model.Task;


import static matrixstudio.kernel.SExpModel.MATRIXSTUDIO_SIMULATION;

/**
 * {@link SExpModelSaver} is able to save a model to a given directory.
 * <p>
 * The save will write th file <code>matrixstudio.simulation</code> which
 * contains all the simulation description and the path to all the external
 * files it needs.
 */
public class SExpModelSaver {

	protected final Referencer referencer = new ModelReferencer();

	protected final ModelToSExp context = new ModelToSExp(referencer);

	/** Set of resolved path during loading, used to find new files to insert */
	protected final Set<Path> resolvedPath = new HashSet<>();

	protected final Path root;
	protected final Model model;

	public SExpModelSaver(Model model, Path root) {
		this.root = Files.isDirectory(root) ?
			root.resolve(MATRIXSTUDIO_SIMULATION): root;
		this.model = model;
	}

	protected Path resolve(String path) throws IOException {
		Path child = root.getParent().resolve(path);
		resolvedPath.add(child);
		if (!Files.exists(child)) {
			Files.createDirectories(child.getParent());
			Files.createFile(child);
		}
		return child;
	}

	public void saveModel() throws IOException {
		if (!MATRIXSTUDIO_SIMULATION.equals(root.getFileName().toString())) {
			throw new IOException("File '" +root+ "' isn't a '" + MATRIXSTUDIO_SIMULATION + "' file.");
		}

		SExp result = simulationToSExp(model);
		byte[] bytes = result.toStringMultiline(0).getBytes(StandardCharsets.UTF_8);
		Files.write(root, bytes);
	}

	protected SExp simulationToSExp(Model model) throws IOException {
		context.push(model);

		SList result = new SList();
		result.addChild(S.satom("simulation"));

		if (model.getParameterCount() > 0) {
			SExp parameters = S.slist(S.satom("parameter"));
			for (Parameter parameter : model.getParameterList()) {
				parameters.addChild(parameterToSExp(parameter));
			}
			result.addChild(parameters);
		}

		if (model.getMatrixCount() > 0) {
			SExp matrices = S.slist(S.satom("matrix"));
			for (Matrix matrix : model.getMatrixList()) {
				matrices.addChild(matrixToSExp(matrix));
			}
			result.addChild(matrices);
		}

		if (model.getCodeCount() > 0) {
			SExp codes = S.slist(S.satom("code"));
			for (Code code: model.getCodeList()) {
				codes.addChild(codeToSExp(code));
			}
			result.addChild(codes);
		}

		result.addChild(schedulerToSExp(model.getScheduler()));

		context.pop(model);
		return result;
	}

	protected SExp parameterToSExp(Parameter parameter) {
		context.push(parameter);
		SList result = new SList();
		result.addChild(S.satom("parameter"));
		S.addChildIfNotNull(result, S.stringToSExp("name", parameter.getName()));
		S.addChildIfNotNull(result, S.stringToSExp("formula", parameter.getFormula()));
		context.pop(parameter);
		return result;
	}

	protected SExp matrixToSExp(Matrix matrix) throws IOException {
		context.push(matrix);

		SList result = new SList();
		String type = matrix.getClass().getSimpleName().toLowerCase();
		result.addChild(S.satom(type));
		S.addChildIfNotNull(result, S.stringToSExp("name", matrix.getName()));
		S.addChildIfNotNull(result, S.stringToSExp("x", matrix.getSizeX()));
		S.addChildIfNotNull(result, S.stringToSExp("y", matrix.getSizeY()));
		S.addChildIfNotNull(result, S.stringToSExp("z", matrix.getSizeZ()));

		if (matrix.isRandom()) {
			S.addChildIfNotNull(result, S.booleanToSExp("random", matrix.isRandom()));
		}

		if (matrix.isRainbow()) {
			S.addChildIfNotNull(result, S.booleanToSExp("rainbow", matrix.isRainbow()));
		}

		if (matrix.isNdRange()) {
			S.addChildIfNotNull(result, S.booleanToSExp("range", matrix.isNdRange()));
		}

		if (!matrix.isRandom()) {
			String source = "matrix/" + matrix.getName() + ".png";
			S.addChildIfNotNull(result, S.stringToSExp("source", source));

			BufferedImage image = new BufferedImage(matrix.safeGetSizeXValue(), matrix.safeGetSizeYValue(), BufferedImage.TYPE_INT_RGB);
			int x = image.getWidth();
			int y = image.getHeight();
			if (matrix instanceof MatrixInteger) {
				int[] values = ((MatrixInteger) matrix).getMatrixInit();
				for (int i = 0; i < x; i++) {
					for (int j = 0; j < y; j++) {
						int value = values[i+x*j];
						/*
						int R = value & 0x0000FF;
						int G = (value & 0x00FF00) >> 8;
						int B = (value & 0xFF0000) >> 16;
						int val = (R << 16) + (G << 8) + B;
						*/
						image.setRGB(i, j, value);

					}
				}
			} else if (matrix instanceof MatrixFloat) {
				float[] values = ((MatrixFloat) matrix).getMatrixInit();
				for(int i=0; i<x; i++) {
					for (int j = 0; j < y; j++) {
						float value = values[i+x*j];
						image.setRGB(i, j, (int) (value*256));
					}
				}

			} else if (matrix instanceof MatrixULong) {

			} else {
				throw new IOException("Unknown matrix type '" + type + "'");
			}

			ImageIO.write(image, "png", resolve(source).toFile());
		}

		context.pop(matrix);
		return result;
	}

	protected SExp codeToSExp(Code code) throws IOException {
		context.push(code);

		SList result = new SList();
		String type = code.getClass().getSimpleName().toLowerCase();
		result.addChild(S.satom(type));
		S.addChildIfNotNull(result, S.stringToSExp("name", code.getName()));

		String source = type + "/" + code.getName() + ".cl";
		S.addChildIfNotNull(result, S.stringToSExp("source", source));

		Path sourcePath = resolve(source);
		Files.write(sourcePath, code.getContents().getBytes(StandardCharsets.UTF_8));

		context.pop(code);
		return result;
	}

	protected SExp schedulerToSExp(Scheduler scheduler) {
		context.push(scheduler);

		SList result = new SList();
		result.addChild(S.satom("scheduler"));

		if (scheduler.getDevice() != Device.ANY) {
			S.addChildIfNotNull(result, S.enumToSExp("device", scheduler.getDevice()));
		}

		if (scheduler.getDeviceOrder() != 1) {
			S.addChildIfNotNull(result, S.intToSExp("order", scheduler.getDeviceOrder()));
		}

		if (scheduler.getTaskCount() > 0) {
			SExp tasks = S.slist(S.satom("task"));
			for (Task task : scheduler.getTaskList()) {
				tasks.addChild(taskToSExp(task));
			}
			result.addChild(tasks);
		}

		context.pop(scheduler);
		return result;
	}

	protected SExp taskToSExp(Task task) {
		context.push(task);

		SList result = new SList();
		result.addChild(S.satom("task"));

		result.addChild(S.floatArrayToSExp("position", task.getPosition()));

		if (!task.getGlobalWorkSizeX().equals("521")) {
			S.addChildIfNotNull(result, S.stringToSExp("globalworksizex", task.getGlobalWorkSizeX()));
		}

		if (!task.getGlobalWorkSizeY().equals("256")) {
			S.addChildIfNotNull(result, S.stringToSExp("globalworksizey", task.getGlobalWorkSizeY()));
		}

		if (!task.getGlobalWorkSizeZ().equals("1")) {
			S.addChildIfNotNull(result, S.stringToSExp("globalworksizez", task.getGlobalWorkSizeZ()));
		}

		if (!task.getRepetition().equals("1")) {
			S.addChildIfNotNull(result, S.stringToSExp("repetition", task.getRepetition()));
		}

		if (task.isRandom()) {
			S.addChildIfNotNull(result, S.booleanToSExp("random", task.isRandom()));
		}

		if (task.getTaskInCount() > 0) {
			SList ins = S.slist(S.satom("in"));
			for (Task in : task.getTaskInList()) {
				ins.addChild(context.createReference(in));
			}
			result.addChild(ins);
		}

		if (task.getTaskOutCount() > 0) {
			SList outs = S.slist(S.satom("out"));
			for (Task out : task.getTaskOutList()) {
				outs.addChild(context.createReference(out));
			}
			result.addChild(outs);
		}

		if (task.getKernelCount() > 0) {
			SList kernels = S.slist(S.satom("kernel"));
			for (Kernel kernel : task.getKernelList()) {
				kernels.addChild(context.createReference(kernel));
			}
			result.addChild(kernels);
		}

		context.pop(task);
		return result;
	}
}
