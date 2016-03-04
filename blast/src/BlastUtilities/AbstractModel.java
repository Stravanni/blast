package BlastUtilities;


import Utilities.RepresentationModel;

import java.io.Serializable;

/**
 * @author stravanni
 */

public abstract class AbstractModel implements Serializable {


    private static final long serialVersionUID = 328759404L;
    protected final int nSize;
    protected double noOfDocuments;
    protected final BlastRepresentationModel modelType;
    protected final String instanceName;

    public AbstractModel(int n, BlastRepresentationModel md, String iName) {
        this.instanceName = iName;
        this.modelType = md;
        this.nSize = n;
        this.noOfDocuments = 0.0D;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public BlastRepresentationModel getModelType() {
        return this.modelType;
    }

    public double getNoOfDocuments() {
        return this.noOfDocuments;
    }

    public int getNSize() {
        return this.nSize;
    }

    public abstract double getSimilarity(AbstractModel var1);

    public abstract void updateModel(String var1);

    public double getEntropyInstamnce(boolean normalized) {
        return 0.0;
    }

    public double getEntropyRewightedInstamnce(boolean normalized, double alpha) {
        return 0.0;
    }

    public double getEntropyPseudoInstamnce(boolean normalized, double b) {
        return 0.0;
    }

    public double getEntropyToken(boolean normalized) {
        return 0.0;
    }

    public String getName() {
        return instanceName;
    }

    //public abstract double getSimilarity(AbstractModel oModel);

}