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
import DataStructures.AbstractBlock;
import MetaBlocking.WeightingScheme;

import java.util.List;

/**
 * @author gap2
 */
public class WeightedNodePruning extends WeightedEdgePruning {

    protected int firstId;
    protected int lastId;
    protected boolean threshold_new;

    public WeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme) {
        this(adp, "Fast Weighted Node Pruning (" + scheme + ")", scheme);
    }

    public WeightedNodePruning(AbstractDuplicatePropagation adp, WeightingScheme scheme, boolean threshold) {
        this(adp, "Fast Weighted Node Pruning (" + scheme + ")", scheme, threshold);
    }

    protected WeightedNodePruning(AbstractDuplicatePropagation adp, String description, WeightingScheme scheme) {
        super(adp, description, scheme);
        nodeCentric = true;
    }

    protected WeightedNodePruning(AbstractDuplicatePropagation adp, String description, WeightingScheme scheme, boolean threshold) {
        super(adp, description, scheme);
        nodeCentric = true;
        threshold_new = threshold;
    }

    @Override
    protected void pruneEdges(List<AbstractBlock> newBlocks) {
        setLimits();
        if (weightingScheme.equals(WeightingScheme.ARCS)) {
            for (int i = firstId; i < lastId; i++) {
                processArcsEntity(i);
                setThreshold(i);
                verifyValidEntities(i, newBlocks);
            }
        }
//        else if (weightingScheme.equals(WeightingScheme.CHI_ENTRO)) {
//            System.out.println("\n\n\n\nchi entro in weighted node pruning\n\n\n\n\n");
//            for (int i = firstId; i < lastId; i++) {
//                processCHI_entro_Entity(i);
//                setThreshold(i);
//                verifyValidEntities(i, newBlocks);
//            }
//        }
        else {
            for (int i = firstId; i < lastId; i++) {
                processEntity(i);
                setThreshold(i);
                verifyValidEntities(i, newBlocks);
            }
        }
    }

    protected void setLimits() {
        firstId = 0;
        lastId = noOfEntities;
    }

    @Override
    protected void setThreshold() {
    }

    protected void setThreshold(int entityId) {
        threshold = 0;
        if (threshold_new) {
            for (int neighborId : validEntities) {
                threshold = Math.max(threshold, getWeight(entityId, neighborId));
            }
            threshold /= 2;
        } else {
            for (int neighborId : validEntities) {
                threshold += getWeight(entityId, neighborId);
            }
            threshold /= validEntities.size();
            //System.out.println("on the fly valid entity: " + validEntities.size());
        }
    }
}
