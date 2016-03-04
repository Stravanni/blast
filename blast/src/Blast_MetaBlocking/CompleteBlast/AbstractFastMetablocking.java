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

package Blast_MetaBlocking.CompleteBlast;


import BlastUtilities.AbstractFastEfficiencyMethod;
import BlastUtilities.BilateralBlock_Entro;
import Blast_MetaBlocking.WeightingScheme;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import DataStructures.UnilateralBlock;
import jsc.contingencytables.ContingencyTable2x2;
import jsc.contingencytables.FishersExactTest;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author simonini
 */
public abstract class AbstractFastMetablocking extends AbstractFastEfficiencyMethod {

    protected boolean nodeCentric;

    protected int[] flags;

    protected double threshold;
    protected double blockAssingments;
    protected double distinctComparisons;
    protected double[] comparisonsPerEntity;
    protected double[] counters;
    protected double[] counters_entro;

    protected double max_weight = 0.0d;

    public double totalBlocks;

    public int counter_a = 0;
    public HashSet<Integer> counter_a_set = new HashSet<>();
    public int counter_b = 0;
    public HashSet<Integer> counter_b_set = new HashSet<>();
    public int counter_tot = 0;

    protected final List<Integer> neighbors;
    protected final List<Integer> retainedNeighbors;
    protected WeightingScheme weightingScheme;

    public AbstractFastMetablocking(String nm, WeightingScheme wScheme) {
        super(nm);
        neighbors = new ArrayList<>();
        retainedNeighbors = new ArrayList<>();
        weightingScheme = wScheme;
    }

    protected abstract void pruneEdges(List<AbstractBlock> blocks);

    protected abstract void setThreshold();

    @Override
    protected void applyMainProcessing(List<AbstractBlock> blocks) {
        //totalBlocks = blocks.size();
        //System.out.println("total n blocks: " + totalBlocks);
        counters = new double[noOfEntities];
        counters_entro = new double[noOfEntities];
        flags = new int[noOfEntities];
        for (int i = 0; i < noOfEntities; i++) {
            flags[i] = -1;
        }

        blockAssingments = 0;
        if (cleanCleanER) {
            for (BilateralBlock_Entro bBlock : bBlocks) {
                blockAssingments += bBlock.getTotalBlockAssignments();
            }
        } else {
            for (UnilateralBlock uBlock : uBlocks) {
                blockAssingments += uBlock.getTotalBlockAssignments();
            }
        }

        if (weightingScheme.equals(WeightingScheme.EJS)) {
            setStatistics();
        }
        if (weightingScheme.equals(WeightingScheme.EJS_ENTRO)) {
            setStatistics();
        }

        setThreshold();
        pruneEdges(blocks);
    }

    protected void freeMemory() {
        bBlocks = null;
        flags = null;
        counters = null;
        uBlocks = null;
    }

    protected Comparison getComparison(int entityId, int neighborId) {
        if (!cleanCleanER) {
            if (entityId < neighborId) {
                return new Comparison(cleanCleanER, entityId, neighborId);
            } else {
                return new Comparison(cleanCleanER, neighborId, entityId);
            }
        } else {
            if (entityId < datasetLimit) {
                return new Comparison(cleanCleanER, entityId, neighborId - datasetLimit);
            } else {
                return new Comparison(cleanCleanER, neighborId, entityId - datasetLimit);
            }
        }
    }

    protected int[] getNeighborEntities(int blockIndex, int entityId) {
        if (cleanCleanER) {
            if (entityId < datasetLimit) {
                return bBlocks[blockIndex].getIndex2Entities();
            } else {
                return bBlocks[blockIndex].getIndex1Entities();
            }
        }
        return uBlocks[blockIndex].getEntities();
    }

//    protected void makeNormalized() {
//        switch (weightingScheme) {
//            case CHI_ENTRO:
//                double[] vE = new double[2];
//                double[] v_E = new double[2];
//                vE[0] = totalBlocks - 2;
//                vE[1] = 1;
//                v_E[0] = 1;
//                v_E[1] = (int) (totalBlocks - 2);
//
//
//                double[][] cME = {vE, v_E};
//
//                ChisqTest inferenceChiE = new ChisqTest(cME);
//                max_weight = inferenceChiE.testStatistic * entityIndex.get_maxEntropy();
//                break;
//            case ARCS_ENTRO:
//                max_weight = totalBlocks * entityIndex.get_maxEntropy();
//                break;
//        }
//    }

    protected double getWeight(int entityId, int neighborId) {
        switch (weightingScheme) {
            case ARCS:
                return counters[neighborId];
            case ARCS_ENTRO:
                return counters[neighborId] * counters_entro[neighborId];
            case CBS:
                return counters[neighborId];
            case CBS_ENTRO:
                return counters_entro[neighborId];
            case ECBS:
                return counters[neighborId] * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(entityId, 0)) * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(neighborId, 0));
            case ECBS_ENTRO:
                return counters_entro[neighborId] * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(entityId, 0)) * Math.log10(noOfBlocks / entityIndex.getNoOfEntityBlocks(neighborId, 0));
            case JS:
                return counters[neighborId] / (entityIndex.getNoOfEntityBlocks(entityId, 0) + entityIndex.getNoOfEntityBlocks(neighborId, 0) - counters[neighborId]);
            case JS_ENTRO:
                // TODO counters_entro cossi' non va bene
                return counters_entro[neighborId] / (entityIndex.getNoOfEntityBlocks_entopy(entityId, 0) + entityIndex.getNoOfEntityBlocks_entopy(neighborId, 0) - counters_entro[neighborId]);
            case EJS:
                double probability = counters[neighborId] / (entityIndex.getNoOfEntityBlocks(entityId, 0) + entityIndex.getNoOfEntityBlocks(neighborId, 0) - counters[neighborId]);
                double l_e1 = Math.log10(distinctComparisons / comparisonsPerEntity[entityId]);
                double l_e2 = Math.log10(distinctComparisons / comparisonsPerEntity[neighborId]);
                return probability * l_e1 * l_e2;
            case EJS_ENTRO:
                double probability_ = counters_entro[neighborId] / (entityIndex.getNoOfEntityBlocks_entopy(entityId, 0) + entityIndex.getNoOfEntityBlocks_entopy(neighborId, 0) - counters_entro[neighborId]);
                double a_ = Math.log10(distinctComparisons / comparisonsPerEntity[entityId]);
                double b_ = Math.log10(distinctComparisons / comparisonsPerEntity[neighborId]);
                return probability_ * a_ * b_;

            case MI_ENTRO:
                //double ab = entityIndex.getTotalNoOfCommonBlocks(comparison) / totalBlocks;
                //double a = entityIndex.getNoOfEntityBlocks(comparison.getEntityId1(), 0) / totalBlocks;
                //double b = entityIndex.getNoOfEntityBlocks(comparison.getEntityId2(), comparison.isCleanCleanER() ? 1 : 0) / totalBlocks;
                double abE_mi = counters_entro[neighborId] / totalBlocks;
                double aE_mi = entityIndex.getNoOfEntityBlocks(entityId, 0) / totalBlocks;
                double bE_mi = entityIndex.getNoOfEntityBlocks(neighborId, 0) / totalBlocks;
                //double mi = ab * (Math.log10(ab / (a * b))/Math.log10(2));
                double miE_mi = abE_mi * Math.log(abE_mi / (aE_mi * bE_mi));
                return miE_mi;

            case CHI:
                long[] v = new long[2];
                long[] v_ = new long[2];
                //@fof
                long ab = (long)counters[neighborId];
                long a  = entityIndex.getNoOfEntityBlocks(entityId, 0) - ab;
                long b  = entityIndex.getNoOfEntityBlocks(neighborId, 0) - ab;
                //@fon
                counter_tot++;
                if (ab < 1) {
                    System.out.println("ab < 1");
                }
                if (a < 0) {
                    counter_a++;
                }
                if (b < 0) {
                    counter_b++;
                }
                v[0] = (long) counters[neighborId];
                v[1] = entityIndex.getNoOfEntityBlocks(entityId, 0) - v[0];
                v_[0] = entityIndex.getNoOfEntityBlocks(neighborId, 0) - v[0];
                v_[1] = (int) (totalBlocks - (v[0] + v[1] + v_[0]));

                if (v[0] < 1) {
                    v[0] = 0;
                }
                if (v_[0] < 1) {
                    v_[0] = 0;
                }
                if (v[1] < 1) {
                    v[1] = 0;
                }
                if (v_[1] < 1) {
                    v_[1] = 0;
                }
                long[][] cM = {v, v_};
                //ChisqTest inferenceChiE = new ChisqTest(cME);

                ChiSquareTest chi_squared_test = new ChiSquareTest();
                //GTest g_test = new GTest();
                //chi_squared_test.chiSquareTest(cME);
                if (max_weight == 0.0d) {
                    //System.out.println("max weight chi = 0");
                    //return (inferenceChiE.testStatistic * counters_entro[neighborId]);

                    return chi_squared_test.chiSquare(cM);

                    //double[] expected = new double[]{(double) vE[0], (double) vE[1]};
                    //return g_test.g(expected, v_E) * counters_entro[neighborId];
                } else {
                    //return (inferenceChiE.testStatistic * entityIndex.get_common_entropy(comparison, true)) / max_weight;
                    System.out.println("Not implemented");
                }

            case CHI_ENTRO:
                long[] vE = new long[2];
                long[] v_E = new long[2];
                //@fof
                long abE = (long)counters[neighborId];
                long aE  = entityIndex.getNoOfEntityBlocks(entityId, 0) - abE;
                long bE  = entityIndex.getNoOfEntityBlocks(neighborId, 0) - abE;
                //@fon
                counter_tot++;
//                if (abE < 1) {
//                    System.out.println("ab < 1");
//                }
//                if (aE < 0) {
//                    counter_a++;
//                }
//                if (bE < 0) {
//                    counter_b++;
//                }
                vE[0] = (long) counters[neighborId];
                vE[1] = entityIndex.getNoOfEntityBlocks(entityId, 0) - vE[0];
                v_E[0] = entityIndex.getNoOfEntityBlocks(neighborId, 0) - vE[0];
                v_E[1] = (int) (totalBlocks - (vE[0] + vE[1] + v_E[0]));

                if (vE[0] < 1) {
                    vE[0] = 0;
                }
                if (v_E[0] < 1) {
                    v_E[0] = 0;
                }
                if (vE[1] < 1) {
                    vE[1] = 0;
                }
                if (v_E[1] < 1) {
                    v_E[1] = 0;
                }
                long[][] cME = {vE, v_E};
                //ChisqTest inferenceChiE = new ChisqTest(cME);

                ChiSquareTest chi_squared_test_E = new ChiSquareTest();
                //GTest g_test = new GTest();
                //chi_squared_test.chiSquareTest(cME);
                if (max_weight == 0.0d) {
                    //System.out.println("max weight chi = 0");
                    //return (inferenceChiE.testStatistic * counters_entro[neighborId]);

                    return chi_squared_test_E.chiSquare(cME) * counters_entro[neighborId];

                    //double[] expected = new double[]{(double) vE[0], (double) vE[1]};
                    //return g_test.g(expected, v_E) * counters_entro[neighborId];
                } else {
                    //return (inferenceChiE.testStatistic * entityIndex.get_common_entropy(comparison, true)) / max_weight;
                    System.out.println("Not implemented");
                }
            case FISHER_ENTRO:
                int[] vE_f = new int[2];
                int[] v_E_f = new int[2];
                int ab_f = (int) counters[neighborId];
                int a_f = ((int) entityIndex.getNoOfEntityBlocks(entityId, 0)) - ab_f;
                int b_f = ((int) entityIndex.getNoOfEntityBlocks(neighborId, 0)) - ab_f;
                counter_tot++;
                if (ab_f < 1) {
                    System.out.println("ab < 1");
                }
                if (a_f < 0) {
                    counter_a++;
                }
                if (b_f < 0) {
                    counter_b++;
                }
                vE_f[0] = (int) counters[neighborId];
                vE_f[1] = (int) entityIndex.getNoOfEntityBlocks(entityId, 0) - vE_f[0];
                v_E_f[0] = (int) entityIndex.getNoOfEntityBlocks(neighborId, 0) - vE_f[0];
                v_E_f[1] = (int) (totalBlocks - (vE_f[0] + vE_f[1] + v_E_f[0]));
//                System.out.println("\nvE[0]: " + vE[0]);
//                System.out.println("vE[1]: " + entityIndex.getNoOfEntityBlocks(entityId, 0) + " - " + vE[0] + " = " + vE[1]);
//                System.out.println("v_E[0]: " + entityIndex.getNoOfEntityBlocks(neighborId, 0) + " - " + vE[0] + " = " + v_E[0]);
//                System.out.println("total blocks: " + totalBlocks);
//                System.out.println("v_E[1]: " + totalBlocks + " - (" + vE[0] + " + " + vE[1] + " + " + v_E[0] + ") =  " + v_E[1]);
                if (vE_f[0] < 1) {
                    vE_f[0] = 1;
                }
                if (v_E_f[0] < 1) {
                    v_E_f[0] = 1;
                }
                if (vE_f[1] < 1) {
                    vE_f[1] = 1;
                }
                if (v_E_f[1] < 1) {
                    v_E_f[1] = 1;
                }
                int[][] cME_f = {vE_f, v_E_f};
                FishersExactTest fisher_test = new FishersExactTest(new ContingencyTable2x2(cME_f));
                if (max_weight == 0.0d) {
                    return fisher_test.getChiSquared();
                } else {
                    System.out.println("Not implemented");
                }
        }
        return -1;
    }

    protected void setNormalizedNeighborEntities(int blockIndex, int entityId) {
        neighbors.clear();
        if (cleanCleanER) {
            if (entityId < datasetLimit) {
                for (int originalId : bBlocks[blockIndex].getIndex2Entities()) {
                    neighbors.add(originalId + datasetLimit);
                    //neighbors_entro.add();
                }
            } else {
                for (int originalId : bBlocks[blockIndex].getIndex1Entities()) {
                    neighbors.add(originalId);
                }
            }
        } else {
            if (!nodeCentric) {
                for (int neighborId : uBlocks[blockIndex].getEntities()) {
                    if (neighborId < entityId) {
                        neighbors.add(neighborId);
                    }
                }
            } else {
                for (int neighborId : uBlocks[blockIndex].getEntities()) {
                    if (neighborId != entityId) {
                        neighbors.add(neighborId);
                    }
                }
            }
        }
    }

    protected void setStatistics() {
        distinctComparisons = 0;
        comparisonsPerEntity = new double[noOfEntities];
        final Set<Integer> distinctNeighbors = new HashSet<Integer>();
        for (int i = 0; i < noOfEntities; i++) {
            final int[] associatedBlocks = entityIndex.getEntityBlocks(i, 0);
            if (associatedBlocks.length != 0) {
                distinctNeighbors.clear();
                for (int blockIndex : associatedBlocks) {
                    for (int neighborId : getNeighborEntities(blockIndex, i)) {
                        distinctNeighbors.add(neighborId);
                    }
                }
                comparisonsPerEntity[i] = distinctNeighbors.size();
                if (!cleanCleanER) {
                    comparisonsPerEntity[i]--;
                }
                distinctComparisons += comparisonsPerEntity[i];
            }
        }
        distinctComparisons /= 2;
    }
}
