package Blast_MetaBlocking.CompleteBlast;

import Blast_MetaBlocking.ThresholdWeightingScheme;
import DataStructures.AbstractBlock;
import MetaBlocking.FastImplementations.WeightedEdgePruning;
import MetaBlocking.WeightingScheme;

import java.util.List;

/**
 * @author stravanni
 */
public class WeightedNodePruning extends WeightedEdgePruning {

    protected int firstId;
    protected int lastId;
    protected double threshold_max;
    private boolean threshold_new;

    protected ThresholdWeightingScheme threshold_type;
    public double totalBlocks;

    public WeightedNodePruning(WeightingScheme scheme) {
        this("Fast Weighted Node Pruning (" + scheme + ")", scheme, ThresholdWeightingScheme.AVG);
    }

    public WeightedNodePruning(WeightingScheme scheme, ThresholdWeightingScheme threshold_type) {
        this("Fast Weighted Node Pruning (" + scheme + ")", scheme, threshold_type);
    }
    public WeightedNodePruning(WeightingScheme scheme, boolean threshold_new) {
        this("Fast Weighted Node Pruning (" + scheme + ")", scheme, threshold_new);
        if (threshold_new) {
            this.threshold_type = ThresholdWeightingScheme.AM2;
        } else {
            this.threshold_type = ThresholdWeightingScheme.AVG;
        }
    }

    protected WeightedNodePruning(String description, WeightingScheme scheme) {
        super(description, scheme);
        nodeCentric = true;
        this.threshold_type = ThresholdWeightingScheme.AVG;
    }

    protected WeightedNodePruning(String description, WeightingScheme scheme, ThresholdWeightingScheme threshold_type) {
        super(description, scheme);
        nodeCentric = true;
        this.threshold_type = threshold_type;
    }

    protected WeightedNodePruning(String description, WeightingScheme scheme, boolean threshold_new) {
        super(description, scheme);
        nodeCentric = true;
        this.threshold_new = threshold_new;
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
//        else if (weightingScheme.equals(WeightingScheme.ARCS_ENTRO)) {
//            for (int i = firstId; i < lastId; i++) {
//                processArcs_entro_Entity(i);
//                setThreshold(i);
//                verifyValidEntities(i, newBlocks);
//            }
        //    }
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
        threshold_max = 0;
        double min = Double.MAX_VALUE;
        double w = 0;
        threshold = 0;

        //boolean new_threshold = false;


        switch (threshold_type) {
            case AVG:
                for (int neighborId : validEntities) {
                    threshold += getWeight(entityId, neighborId);
                }
                threshold /= validEntities.size();
                break;

            case AM2:
                for (int neighborId : validEntities) {
                    w = getWeight(entityId, neighborId);
                    threshold_max = Math.max(threshold_max, w);
                    //min = Math.min(min, w);
                }
                threshold = threshold_max / 2;
                //threshold = threshold_max - Math.pow(Math.pow(threshold_max, .5) - Math.pow(min, .5), 2);
                //threshold = (threshold_max + min) / 2;
                break;
            case AM3:
                for (int neighborId : validEntities) {
                    w = getWeight(entityId, neighborId);
                    threshold_max = Math.max(threshold_max, w);
                    min = Math.min(min, w);
                }
                threshold = threshold_max / 2;
                //threshold = threshold_max - Math.pow(Math.pow(threshold_max, .5) - Math.pow(min, .5), 2);
                break;
        }
//        for (int neighborId : validEntities) {
//
//            if (threshold_new) {
//                w = getWeight(entityId, neighborId);
//                threshold_max = Math.max(threshold_max, w);
//                min = Math.min(min, w);
//            } else {
//                threshold += getWeight(entityId, neighborId);
//            }
//        }
//        //threshold = (threshold_max - min)/2;
//        if (threshold_new) {
//            threshold = threshold_max / 2;
//            //threshold = (threshold_max + min)/2;
//        } else {
//            threshold /= validEntities.size();
//        }
    }
}