/*
 * This file is part of MatrixStudio.
 *
 *     MatrixStudio is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     MatrixStudio is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with MatrixStudio.  If not, see <http://www.gnu.org/licenses/>.
 */

package matrixstudio.ui;

import matrixstudio.kernel.Simulator;
import matrixstudio.kernel.SimulatorContext;
import org.eclipse.swt.widgets.Shell;

/**
 * Created by charlie on 12/01/2014.
 */
public interface StudioContext extends SimulatorContext, RendererContext {

    Simulator getSimulator();

    boolean isCompiled();

    void setCompiled(boolean compiled);

    Object getSelection();

    void setSelection(Object selection);

    boolean saveModel(boolean forceDialog);

    Shell getShell();

    MSResources getResources();

    void asynchronousRun(int milliseconds, Runnable runnable);

    void simulationRefresh();

}
