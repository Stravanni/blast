package RepresentationModels;

import Utilities.RepresentationModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;


public abstract class AbstractModel implements Serializable {

    private static final long serialVersionUID = 328759404L;

    protected final int nSize;
    protected double noOfDocuments;

    protected final RepresentationModel modelType;
    protected final String instanceName;

    public AbstractModel(int n, RepresentationModel md, String iName) {
        instanceName = iName;
        modelType = md;
        nSize = n;
        noOfDocuments = 0;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public LinkedHashSet<String> getFrequencies() {
        return null;
    }

    public RepresentationModel getModelType() {
        return modelType;
    }

    public double getNoOfDocuments() {
        return noOfDocuments;
    }

    public int getNSize() {
        return nSize;
    }

    public abstract double getSimilarity(AbstractModel oModel);

    public abstract void updateModel(String text);

    public int updateModel(String text, HashMap<String, Integer> all_tokens) {
        return Integer.parseInt(null);
    }

    public double getEntropyToken(boolean normalized) {
        return 0.0;
    }
}