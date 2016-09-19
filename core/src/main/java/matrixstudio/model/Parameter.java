package matrixstudio.model;

import org.xid.basics.model.ChangeRecorder;
import org.xid.basics.serializer.Boost;

/**
 * Created by j5r on 19/09/2016.
 */
public class Parameter implements Named {

    private Model model;

    private String name;

    private String formula = "";

    public Parameter() {
    }

    protected Parameter(Boost boost) {
        boost.register(this);
        model = boost.readObject(Model.class);
        name = boost.readString();
        formula = boost.readString();
    }

    /**
     * <p>Gets model.</p>
     */
    public Model getModel() {
        return model;
    }

    /**
     * <p>Sets model.</p>
     */
    public void setModel(Model newValue) {
        if (model == null ? newValue != null : (model.equals(newValue) == false)) {
            getChangeRecorder().recordChangeAttribute(this, "model", this.model);
            this.model= newValue;
        }
    }

    /**
     * <p>Gets formula.</p>
     */
    public String getFormula() {
        return formula;
    }

    /**
     * <p>Sets formula.</p>
     */
    public void setFormula(String newValue) {
        if (formula == null ? newValue != null : (formula.equals(newValue) == false)) {
            getChangeRecorder().recordChangeAttribute(this, "formula", this.formula);
            this.formula = newValue;
        }
    }

    /**
     * <p>Gets name.</p>
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Sets name.</p>
     */
    public void setName(String newValue) {
        if (name == null ? newValue != null : (name.equals(newValue) == false)) {
            getChangeRecorder().recordChangeAttribute(this, "name", this.name);
            this.name= newValue;
        }
    }

    public void writeToBoost(Boost boost) {
        boost.writeObject(model);
        boost.writeString(formula);
        boost.writeString(name);
    }

    /**
     * Visitor accept method.
     */
    public void accept(ModelVisitor visitor) {
        visitor.visitParameter(this);
    }


    public ChangeRecorder getChangeRecorder() {
        if ( getModel() != null ) {
            return getModel().getChangeRecorder();
        }
        return ChangeRecorder.Stub;
    }

}
