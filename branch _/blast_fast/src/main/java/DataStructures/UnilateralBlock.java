package DataStructures;

import java.io.Serializable;
import java.util.Arrays;

public class UnilateralBlock extends AbstractBlock implements Serializable {

    private static final long serialVersionUID = 43532585408538695L;

    protected final int[] entities;

    public UnilateralBlock(int[] entities) {
        super();
        this.entities = entities;
        comparisons = ((double) entities.length) * (entities.length - 1) / 2;
    }

    public UnilateralBlock(int[] entities, double entropy) {
        super(entropy);
        this.entities = entities;
        comparisons = ((double) entities.length) * (entities.length - 1) / 2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnilateralBlock other = (UnilateralBlock) obj;
        if (!Arrays.equals(this.entities, other.entities)) {
            return false;
        }
        return true;
    }

    public int[] getEntities() {
        return entities;
    }

    @Override
    public double getTotalBlockAssignments() {
        return entities.length;
    }

    @Override
    public double getAggregateCardinality() {
        return entities.length * (entities.length - 1);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Arrays.hashCode(this.entities);
        return hash;
    }

    @Override
    public void setUtilityMeasure() {
        utilityMeasure = 1.0 / entities.length;
    }
}