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

import MetaBlocking.WeightingScheme;

/**
 * @author G.A.P. II
 */
public class ReciprocalWeightedNodePruning extends RedefinedWeightedNodePruning {

    private boolean threshold_new;

    public ReciprocalWeightedNodePruning(WeightingScheme scheme) {
        super("Reciprocal Weighted Node Pruning (" + scheme + ")", scheme);
        threshold_new = false;
    }

    public ReciprocalWeightedNodePruning(WeightingScheme scheme, boolean t) {
        super("Reciprocal Weighted Node Pruning (" + scheme + ")", scheme, t);
        threshold_new = false;
    }

    public ReciprocalWeightedNodePruning(WeightingScheme scheme, boolean t, boolean tt) {
        super("Reciprocal Weighted Node Pruning (" + scheme + ")", scheme, t);
        threshold_new = tt;
    }

    @Override
    protected boolean isValidComparison(int entityId, int neighborId) {
        double weight = getWeight(entityId, neighborId);
        boolean inNeighborhood1 = averageWeight[entityId] <= weight;
        boolean inNeighborhood2 = averageWeight[neighborId] <= weight;

        if (threshold_new) {
            //if (inNeighborhood1 || inNeighborhood2) {
            if ((averageWeight[entityId] + averageWeight[neighborId]) / 2 <= weight) {
            //if (Math.max(averageWeight[entityId],averageWeight[neighborId]) <= weight) {
                return entityId < neighborId;
            }
        } else if (!threshold_new) {
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
