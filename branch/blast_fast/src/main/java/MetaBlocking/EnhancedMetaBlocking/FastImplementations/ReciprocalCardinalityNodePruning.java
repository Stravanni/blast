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

package MetaBlocking.EnhancedMetaBlocking.FastImplementations;

import DataStructures.Comparison;
import MetaBlocking.WeightingScheme;

/**
 * @author G.A.P. II
 */

public class ReciprocalCardinalityNodePruning extends RedefinedCardinalityNodePruning {

    public ReciprocalCardinalityNodePruning(WeightingScheme scheme) {
        super("Fast Reciprocal Cardinality Node Pruning (" + scheme + ")", scheme);
    }

    public ReciprocalCardinalityNodePruning(WeightingScheme scheme, double totalBlocks) {
        super("Fast Reciprocal Cardinality Node Pruning (" + scheme + ")", scheme);
        this.totalBlocks = totalBlocks;
    }

    @Override
    protected boolean isValidComparison(int entityId, Comparison comparison) {
        int neighborId = comparison.getEntityId1() == entityId ? comparison.getEntityId2() : comparison.getEntityId1();
        if (cleanCleanER && entityId < datasetLimit) {
            neighborId += datasetLimit;
        }

        if (nearestEntities[neighborId] == null) {
            return false;
        }

        if (nearestEntities[neighborId].contains(comparison)) {
            return entityId < neighborId;
        }

        return false;
    }
}
