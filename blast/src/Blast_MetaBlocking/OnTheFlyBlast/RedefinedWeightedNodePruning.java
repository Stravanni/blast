package Blast_MetaBlocking.OnTheFlyBlast;

import Blast_MetaBlocking.ThresholdWeightingScheme;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import MetaBlocking.WeightingScheme;

import java.util.List;

public class RedefinedWeightedNodePruning extends Blast_MetaBlocking.CompleteBlast.RedefinedWeightedNodePruning {

    protected double totalComparisons;
    protected final AbstractDuplicatePropagation duplicatePropagation;

    public RedefinedWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme) {
        this(adp, "Redefined Weighted Node Pruning ("+scheme+")", scheme);
    }

    public RedefinedWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme, ThresholdWeightingScheme threshold_type) {
        this(adp, "Redefined Weighted Node Pruning ("+scheme+")", scheme, threshold_type);
    }

    public RedefinedWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme, boolean t) {
        this(adp, "Redefined Weighted Node Pruning ("+scheme+")", scheme, t);
    }

    protected RedefinedWeightedNodePruning(AbstractDuplicatePropagation adp, String description, WeightingScheme scheme) {
        super(description, scheme);
        duplicatePropagation = adp;
        duplicatePropagation.resetDuplicates();
        totalComparisons = 0;
    }

    protected RedefinedWeightedNodePruning(AbstractDuplicatePropagation adp, String description, WeightingScheme scheme, ThresholdWeightingScheme threshold_type) {
        super(description, scheme, threshold_type);
        duplicatePropagation = adp;
        duplicatePropagation.resetDuplicates();
        totalComparisons = 0;
    }

    protected RedefinedWeightedNodePruning(AbstractDuplicatePropagation adp, String description, WeightingScheme scheme, boolean t) {
        super(description, scheme, t);
        duplicatePropagation = adp;
        duplicatePropagation.resetDuplicates();
        totalComparisons = 0;
    }


    public double[] getPerformance() {
        double[] metrics = new double[3];
        metrics[0] = duplicatePropagation.getNoOfDuplicates() / ((double) duplicatePropagation.getExistingDuplicates()); //PC
        metrics[1] = duplicatePropagation.getNoOfDuplicates() / totalComparisons; //PQ
        metrics[2] = totalComparisons;
        return metrics;
    }

    @Override
    protected void verifyValidEntities(int entityId, List<AbstractBlock> newBlocks) {
        if (!cleanCleanER) {
            for (int neighborId : validEntities) {
                if (isValidComparison(entityId, neighborId)) {
                    totalComparisons++;
                    duplicatePropagation.isSuperfluous(getComparison(entityId, neighborId));
                }
            }
        } else {
            if (entityId < datasetLimit) {
                for (int neighborId : validEntities) {
                    if (isValidComparison(entityId, neighborId)) {
                        totalComparisons++;
                        duplicatePropagation.isSuperfluous(getComparison(entityId, neighborId));
                    }
                }
            } else {
                for (int neighborId : validEntities) {
                    if (isValidComparison(entityId, neighborId)) {
                        totalComparisons++;
                        duplicatePropagation.isSuperfluous(getComparison(entityId, neighborId));
                    }
                }
            }
        }
    }
}
