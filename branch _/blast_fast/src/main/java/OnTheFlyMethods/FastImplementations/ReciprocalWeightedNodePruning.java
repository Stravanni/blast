/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    Copyright (C) 2015 George Antony Papadakis (gpapadis@yahoo.gr)
 */
package OnTheFlyMethods.FastImplementations;

import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import MetaBlocking.ThresholdWeightingScheme;
import MetaBlocking.WeightingScheme;

public class ReciprocalWeightedNodePruning extends RedefinedWeightedNodePruning {

    private boolean threshold_reiprocal;

    public ReciprocalWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme) {
        super(adp, "Reciprocal Weighted Node Pruning (" + scheme + ")", scheme);
        this.threshold_reiprocal = false;
    }

    public ReciprocalWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme, boolean t) {
        super(adp, "Reciprocal Weighted Node Pruning (" + scheme + ")", scheme, t);
        this.threshold_reiprocal = false;
    }

    public ReciprocalWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme, ThresholdWeightingScheme threshold_type) {
        super(adp, "Reciprocal Weighted Node Pruning (" + scheme + ")", scheme, threshold_type);
        if (!ThresholdWeightingScheme.AVG.equals(threshold_type)) {
            this.threshold_reiprocal = true;
        }
        this.threshold_reiprocal = false;
    }

    public ReciprocalWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme, ThresholdWeightingScheme threshold_type, double totalBlocks) {
        super(adp, "Reciprocal Weighted Node Pruning (" + scheme + ")", scheme, threshold_type);
        if (!ThresholdWeightingScheme.AVG.equals(threshold_type)) {
            this.threshold_reiprocal = true;
        }
        this.threshold_reiprocal = false;
        this.totalBlocks = totalBlocks;
    }

    public ReciprocalWeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme, boolean t, boolean threshold_reiprocal) {
        super(adp, "Reciprocal Weighted Node Pruning (" + scheme + ")", scheme, t);
        this.threshold_reiprocal = threshold_reiprocal;
    }

    @Override
    protected boolean isValidComparison(int entityId, int neighborId) {
        double weight = getWeight(entityId, neighborId);

        if (threshold_reiprocal) {
            //if (inNeighborhood1 || inNeighborhood2) {
            if ((averageWeight[entityId] + averageWeight[neighborId]) / 2 <= weight) {
                //if (Math.max(averageWeight[entityId],averageWeight[neighborId]) <= weight) {
                return entityId < neighborId;
            }
        } else if (!threshold_reiprocal) {
            boolean inNeighborhood1 = averageWeight[entityId] <= weight;
            boolean inNeighborhood2 = averageWeight[neighborId] <= weight;

            if (inNeighborhood1 && inNeighborhood2) {
                return entityId < neighborId;
            }
        }
//        boolean inNeighborhood1 = (averageWeight[entityId] + averageWeight[neighborId]) / 2 <= weight;
//        if (inNeighborhood1) {
//            return entityId < neighborId;
//        }

        return false;
    }
}
