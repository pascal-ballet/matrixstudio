package matrixstudio.model;

/**
 * <p>Visitor interface for package 'model'.</p>
 */
public interface ModelVisitor {

	/**
	 * <p>Empty visitor implementation for package 'model'.</p>
	 */
	class Stub implements ModelVisitor {

		/**
		 * Empty visit method for Model.
		 */
		public void visitModel(Model toVisit) {
			//do nothing
		}

		/**
		 * Empty visit method for Scheduler.
		 */
		public void visitScheduler(Scheduler toVisit) {
			//do nothing
		}

		/**
		 * Empty visit method for Task.
		 */
		public void visitTask(Task toVisit) {
			//do nothing
		}

		/**
		 * Empty visit method for Kernel.
		 */
		public void visitKernel(Kernel toVisit) {
			//do nothing
		}

		/**
		 * Empty visit method for Library.
		 */
		public void visitLibrary(Library toVisit) {
			//do nothing
		}

		/**
		 * Empty visit method for MatrixFloat.
		 */
		public void visitMatrixFloat(MatrixFloat toVisit) {
			//do nothing
		}

		/**
		 * Empty visit method for MatrixInteger.
		 */
		public void visitMatrixInteger(MatrixInteger toVisit) {
			//do nothing
		}

		/**
		 * Empty visit method for MatrixULong.
		 */
		public void visitMatrixULong(MatrixULong toVisit) {
			//do nothing
		}

		/**
		 * Empty visit method for Parameter.
		 */
        @Override
        public void visitParameter(Parameter toVisit) {
            // do nothing
        }
    }

	/**
	 * Visit method for Model.
	 */
	void visitModel(Model toVisit);
	

	/**
	 * Visit method for Scheduler.
	 */
	void visitScheduler(Scheduler toVisit);
	

	/**
	 * Visit method for Task.
	 */
	void visitTask(Task toVisit);
	

	/**
	 * Visit method for Kernel.
	 */
	void visitKernel(Kernel toVisit);
	

	/**
	 * Visit method for Library.
	 */
	void visitLibrary(Library toVisit);
	

	/**
	 * Visit method for MatrixFloat.
	 */
	void visitMatrixFloat(MatrixFloat toVisit);
	

	/**
	 * Visit method for MatrixInteger.
	 */
	void visitMatrixInteger(MatrixInteger toVisit);
	

	/**
	 * Visit method for MatrixULong.
	 */
	void visitMatrixULong(MatrixULong toVisit);


	void visitParameter(Parameter toVisit);

}

