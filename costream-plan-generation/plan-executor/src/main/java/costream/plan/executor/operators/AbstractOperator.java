package costream.plan.executor.operators;

import costream.plan.executor.main.Constants;
import org.apache.storm.topology.base.BaseRichSpout;

import java.util.HashMap;
import java.util.Random;

/**
 * This is the abstract operator holding several operator properties.
 * These are among others the operator type itself, a class that the operator applies to, the operator function that is
 * applied later and so on.
 *
 * @param <T>
 */
public abstract class AbstractOperator<T> {
    protected final String type;        // WindowOperator, MapOperator ...
    protected Class<?> klass;     // Class that this operator applies to, like filter for Strings (not valid for all types)
    protected T function;         // The operator function itself
    protected String id;        // Unique identifier
    protected int parallelism;


    public AbstractOperator() {
        this.type = this.getClass().getSimpleName();
        //        Random random = new Random();
        //        int availableCores = Runtime.getRuntime().availableProcessors();
        // hardcoded for testing        
        this.parallelism = 3 ;//random.nextInt(availableCores) + 1;
    }

    /**
     * This is called for each operator when building the final graph object. Note that this is often overwritten in the
     * single operators.
     *
     * @return A hash map that contains descriptions (=features) for the operator
     */
    public HashMap<String, Object> getDescription() {
        HashMap<String, Object> description = new HashMap<>();
        description.put(Constants.Features.id.name(), id);
        description.put(Constants.Features.operatorType.name(), type);
        description.put(Constants.QueryProperties.PARALLELISM, parallelism);
        //if (klass != null) {
        //    description.put(Constants.Features.dataType.name(), klass.getSimpleName());
        //}
        return description;
    }

    public T getFunction() {
        return function;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BaseRichSpout getSpoutOperator(String queryId)
    {return null;}

    public int getParallelism() {
        return this.parallelism;
    }

    public Class<?> getKlass() {
        return klass;
    }
}
