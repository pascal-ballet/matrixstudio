package matrixstudio.kernel;

import fr.minibilles.basics.sexp.S;
import fr.minibilles.basics.sexp.SExp;
import fr.minibilles.basics.sexp.SExpParser;
import fr.minibilles.basics.sexp.SVariable;
import fr.minibilles.basics.sexp.VariableResolver;
import fr.minibilles.basics.sexp.model.Referencer;
import fr.minibilles.basics.sexp.model.SExpToModel;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.imageio.ImageIO;
import matrixstudio.model.Code;
import matrixstudio.model.Device;
import matrixstudio.model.Kernel;
import matrixstudio.model.Library;
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
 * {@link SExpModelLoader} is able to load a model from a given directory.
 * <p>
 * The loader will search for a file <code>matrixstudio.simulation</code> which
 * contains all the simulation description and the path to all the external
 * files it needs.
 * <p>
 * All referenced path in the description must be relative and inside the root
 * directory.
 */
public class SExpModelLoader {

	protected final VariableResolver resolver = new VariableResolver.Mapped();

	protected final Referencer referencer = new ModelReferencer();

	protected final SExpToModel context = new SExpToModel(referencer, resolver);

	/** Set of resolved path during loading, used to find new files to insert */
	protected final Set<Path> resolvedPath = new HashSet<>();

	/** Runnables ran after model load (used for matrix load for instance) */
	protected final List<Runnable> postLoadingRunnables = new ArrayList<>();

	protected final Path root;

	public SExpModelLoader(Path root) {
		this.root = !Files.isRegularFile(root) ?
			root.resolve(MATRIXSTUDIO_SIMULATION): root;
	}

	protected Path resolve(String path) throws IOException {
		Path child = root.getParent().resolve(path);
		resolvedPath.add(child);
		if (!Files.exists(child)) {
			throw new IOException("Referenced file '"+ child +"' doesn't exist");
		}
		return child;
	}

	public Model readModel() throws IOException {
		if (!Files.exists(root)) {
			throw new IOException("File '" +root+ "' doesn't exist");
		}

		if (!MATRIXSTUDIO_SIMULATION.equals(root.getFileName().toString())) {
			throw new IOException("File '" +root+ "' isn't a '" + MATRIXSTUDIO_SIMULATION + "' file.");
		}

		SExpParser parser = new SExpParser("utf-8");
		try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(root.toFile()))) {
			SExp parsed = parser.parse(stream);
			Model model = create(Model.class, parsed);
			if (!context.unresolvedReferences().isEmpty()) {
				throw new IOException("Unresolved references " + context.unresolvedReferences());
			}

			for (Runnable loader : postLoadingRunnables) {
				loader.run();
			}

			return model;
		}
	}

	/**
	 * Creates an instance of T from given SExp. It create an instance.
	 * and tries to resolve references. If a reference isn't resolved, it
	 * keeps them stored for later calls to create.
	 */
	public <T> T create(Class<T> klass, SExp sexp) throws IOException {
		if (sexp.isVariable() ) {
			// checks variable case
			return resolver.resolve(((SVariable) sexp).getName(), klass);
		}

		// not a variable
		String type = sexp.getConstructor();
		Object result;
		if ( "simulation".equals(type) ) {
			result = createSimulation(sexp);
		} else
		if ( "parameter".equals(type) ) {
			result = createParameter(sexp);
		} else
		if ( "matrixinteger".equals(type) ) {
			result = createMatrixInteger(sexp);
		} else
		if ( "matrixfloat".equals(type) ) {
			result = createMatrixFloat(sexp);
		} else
		if ( "matrixlong".equals(type) ) {
			result = createMatrixLong(sexp);
		} else
		if ( "kernel".equals(type) ) {
			result = createKernel(sexp);
		} else
		if ( "library".equals(type) ) {
			result = createLibrary(sexp);
		} else
		if ( "task".equals(type) ) {
			result = createTask(sexp);
		} else
		if ( "scheduler".equals(type) ) {
			result = createScheduler(sexp);
		} else
		{
			StringBuilder message = new StringBuilder();
			if ( type == null ) {
				message.append("Can't create a '");
				message.append(klass.getSimpleName());
				message.append("' instance from '");
				message.append(sexp);
				message.append("'.");
			} else {
				message.append("Unknown type '");
				message.append(type);
				message.append("'.");
			}
			throw new IOException(message.toString());
		}

		//return casted result.
		//If result isn't of klass, it throws a ClassCastException.
		return result == null ? null : klass.cast(result);
	}

	protected Model createSimulation(SExp sexp) throws IOException {
		Model result = new Model();

		int current = 1;
		int count = sexp.getChildCount();

		context.push(result);
		while ( current < count ) {
			final SExp currentSexp = sexp.getChild(current);
			final String type = currentSexp.getConstructor();

			if ( "parameter".equals(type) ) {
				for (int i=1; i<currentSexp.getChildCount(); i++ ) {
					Parameter child = create(Parameter.class, currentSexp.getChild(i));
					result.addParameterAndOpposite(child);
				}
			} else
			if ( "matrix".equals(type) ) {
				for (int i=1; i<currentSexp.getChildCount(); i++ ) {
					Matrix child = create(Matrix.class, currentSexp.getChild(i));
					result.addMatrixAndOpposite(child);
				}
			} else
			if ( "code".equals(type) ) {
				for (int i=1; i<currentSexp.getChildCount(); i++ ) {
					Code child = create(Code.class, currentSexp.getChild(i));
					result.addCodeAndOpposite(child);
				}
			} else
			if ( "scheduler".equals(type) ) {
				Scheduler child = create(Scheduler.class, currentSexp);
				result.setSchedulerAndOpposite(child);
			} else
			{
				//unknown, this is an error.
				StringBuilder message = new StringBuilder();
				if ( type == null ) {
					message.append("Unknown attribute format '");
					message.append(currentSexp);
					message.append("'.");
				} else {
					message.append("Unknown attribute '");
					message.append(type);
					message.append("'.");
				}
				throw new IOException(message.toString());
			}
			current += 1;
		}

		context.pop(result);
		return result;
	}

	protected Parameter createParameter(SExp sexp) throws IOException {
		Parameter result = new Parameter();

		int current = 1;
		int count = sexp.getChildCount();
		while ( current < count ) {
			final SExp currentSexp = sexp.getChild(current);
			final String type = currentSexp.getConstructor();

			if ( "name".equals(type) ) {
				result.setName(S.sexpToString(currentSexp) );
			} else
			if ( "formula".equals(type) ) {
				result.setFormula(S.sexpToString(currentSexp));
			} else {
				//unknown, this is an error.
				String message = "Unknown attribute format '" + currentSexp + "'.";
				throw new IOException(message);
			}
			current += 1;
		}
		context.push(result);
		context.pop(result);
		return result;
	}

	protected <T extends Matrix> T readMatrix(SExp sexp, T matrix, BiConsumer<T, BufferedImage> initializer) throws IOException {
		int current = 1;
		String source = null;
		int count = sexp.getChildCount();
		while ( current < count ) {
			final SExp currentSexp = sexp.getChild(current);
			final String type = currentSexp.getConstructor();

			if ( "name".equals(type) ) {
				matrix.setName(S.sexpToString(currentSexp) );
			} else
			if ( "x".equals(type) ) {
				matrix.setSizeX(S.sexpToString(currentSexp));
			} else
			if ( "y".equals(type) ) {
				matrix.setSizeY(S.sexpToString(currentSexp));
			} else
			if ( "z".equals(type) ) {
				matrix.setSizeZ(S.sexpToString(currentSexp));
			} else
			if ( "random".equals(type) ) {
				matrix.setRandom(S.sexpToBoolean(currentSexp));
			} else
			if ( "rainbow".equals(type) ) {
				matrix.setRainbow(S.sexpToBoolean(currentSexp));
			} else
			if ( "range".equals(type) ) {
				matrix.setNdRange(S.sexpToBoolean(currentSexp));
			} else
			if ( "source".equals(type) ) {
				source = S.sexpToString(currentSexp);
			} else {
				//unknown, this is an error.
				String message = "Unknown attribute format '" + currentSexp + "'.";
				throw new IOException(message);
			}
			current += 1;
		}

		final String toLoad = source;
		postLoadingRunnables.add(() -> {
			if (toLoad != null) {
				matrix.initBlank(true);
				try {
					BufferedImage image = ImageIO.read(resolve(toLoad).toFile());
					initializer.accept(matrix, image);
				} catch (IOException e) {
					// TODO present error
				}
			}
		});


		context.push(matrix);
		context.pop(matrix);

		return matrix;
	}

	protected MatrixInteger createMatrixInteger(SExp sexp) throws IOException {
		return readMatrix(sexp, new MatrixInteger(), (matrix, image) -> {
			int x = image.getWidth();
			int y = image.getHeight();
			for (int i = 0; i < x; i++) {
				for (int j = 0; j < y; j++) {
					int value = image.getRGB(i, j);
					int R = value & 0x0000FF;
					int G = (value & 0x00FF00) >> 8;
					int B = (value & 0xFF0000) >> 16;
					int val = (R << 16) + (G << 8) + B;
					matrix.setValueAt(i, j, 0, val);
					matrix.setInitValueAt(i, j, 0, val);
				}
			}
		});
	}

	protected MatrixFloat createMatrixFloat(SExp sexp) throws IOException {
		return readMatrix(sexp, new MatrixFloat(), (matrix, image) -> {
			int x = image.getWidth();
			int y = image.getHeight();
			for(int i=0; i<x; i++) {
				for (int j = 0; j < y; j++) {
					int value = image.getRGB(i, j);
					matrix.setValueAt(i, j, 0, value/256f);
					matrix.setInitValueAt(i, j, 0, value/256f);
				}
			}
		});
	}

	protected MatrixULong createMatrixLong(SExp sexp) throws IOException {
		return readMatrix(sexp, new MatrixULong(), (matrix, image) -> {
			int x = image.getWidth();
			int y = image.getHeight();
			for(int i=0; i<x; i++) {
				for (int j = 0; j < y; j++) {
					int value = image.getRGB(i, j);
					matrix.setValueAt(i, j, 0, value/256f);
					matrix.setInitValueAt(i, j, 0, value/256f);
				}
			}
		});
	}

	protected String readCode(SExp sexp, Code code) throws IOException {
		int current = 1;
		String source = null;
		int count = sexp.getChildCount();
		while ( current < count ) {
			final SExp currentSexp = sexp.getChild(current);
			final String type = currentSexp.getConstructor();

			if ( "name".equals(type) ) {
				code.setName(S.sexpToString(currentSexp) );
			} else
			if ( "source".equals(type) ) {
				source = S.sexpToString(currentSexp);
			} else {
				//unknown, this is an error.
				String message = "Unknown attribute format '" + currentSexp + "'.";
				throw new IOException(message);
			}
			current += 1;
		}
		return source;
	}

	protected Kernel createKernel(SExp sexp) throws IOException {
		Kernel result = new Kernel();
		String source = readCode(sexp, result);
		if (source != null) {
			Path path = resolve(source);
			result.setContents(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
		}
		context.push(result);
		context.pop(result);
		return result;
	}

	protected Library createLibrary(SExp sexp) throws IOException {
		Library result = new Library();
		String source = readCode(sexp, result);
		if (source != null) {
			Path path = resolve(source);
			result.setContents(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
		}
		context.push(result);
		context.pop(result);
		return result;
	}

	protected Task createTask(SExp sexp) throws IOException {
		Task result = new Task();

		int current = 1;
		int count = sexp.getChildCount();
		while ( current < count ) {
			final SExp currentSexp = sexp.getChild(current);
			final String type = currentSexp.getConstructor();

			if ( "globalworksizex".equals(type) ) {
				result.setGlobalWorkSizeX(S.sexpToString(currentSexp));
			} else
			if ( "globalworksizey".equals(type) ) {
				result.setGlobalWorkSizeY(S.sexpToString(currentSexp));
			} else
			if ( "globalworksizez".equals(type) ) {
				result.setGlobalWorkSizeZ(S.sexpToString(currentSexp));
			} else
			if ( "repetition".equals(type) ) {
				result.setRepetition(S.sexpToString(currentSexp));
			} else
			if ( "random".equals(type) ) {
				result.setRandom(S.sexpToBoolean(currentSexp));
			} else
			if ( "position".equals(type) ) {
				result.setPosition(S.sexpToFloatArray(currentSexp));
			} else
			if ( "in".equals(type) ) {
				for (int i=1; i<currentSexp.getChildCount(); i++ ) {
					SExp reference = currentSexp.getChild(i);
					context.registerReference(result, "taskIn", true, reference);
				}
			} else
			if ( "out".equals(type) ) {
				for (int i=1; i<currentSexp.getChildCount(); i++ ) {
					SExp reference = currentSexp.getChild(i);
					context.registerReference(result, "taskOut", true, reference);
				}
			} else
			if ( "kernel".equals(type) ) {
				for (int i=1; i<currentSexp.getChildCount(); i++ ) {
					SExp reference = currentSexp.getChild(i);
					context.registerReference(result, "kernel", true, reference);
				}
			} else{
				//unknown, break.
				break;
			}
			current += 1;
		}
		context.push(result);
		context.pop(result);
		return result;
	}

	protected Scheduler createScheduler(SExp sexp) throws IOException {
		Scheduler result = new Scheduler();

		int current = 1;
		int count = sexp.getChildCount();
		while ( current < count ) {
			final SExp currentSexp = sexp.getChild(current);
			final String type = currentSexp.getConstructor();

			if ( "device".equals(type) ) {
				result.setDevice(S.sexpToEnum(Device.class, currentSexp));
			} else
			if ( "order".equals(type) ) {
				result.setDeviceOrder(S.sexpToInt(currentSexp));
			} else
			{
				//unknown, break.
				break;
			}
			current += 1;
		}

		context.push(result);
		while ( current < count ) {
			final SExp currentSexp = sexp.getChild(current);
			final String type = currentSexp.getConstructor();

			if ( "task".equals(type) ) {
				for (int i=1; i<currentSexp.getChildCount(); i++ ) {
					Task child = create(Task.class, currentSexp.getChild(i));
					result.addTaskAndOpposite(child);
				}
			} else
			{
				//unknown, this is an error.
				StringBuilder message = new StringBuilder();
				if ( type == null ) {
					message.append("Unknown attribute format '");
					message.append(currentSexp);
					message.append("' for type 'EAttribute'.");
				} else {
					message.append("Unknown attribute '");
					message.append(type);
					message.append("' for type 'EAttribute'.");
				}
				throw new IOException(message.toString());
			}
			current += 1;
		}

		context.pop(result);
		return result;
	}
}
