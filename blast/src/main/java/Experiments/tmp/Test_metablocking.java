/**
 * @author @stravanni
 */
package Experiments.tmp;

import BlockBuilding.AbstractBlockingMethod;
import BlockBuilding.MemoryBased.TokenBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import Experiments.Exp_Util;
import MetaBlocking.ThresholdWeightingScheme;
import MetaBlocking.WeightingScheme;
import OnTheFlyMethods.FastImplementations.BlastWeightedNodePruning;
import Utilities.BlockStatistics;
import Utilities.RepresentationModel;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author stravanni
 */

public class Test_metablocking {

    private static boolean CLEAN = true;
    private static String BASEPATH_CER = "/Users/gio/Desktop/umich/data/data_blockingFramework/";
    private static String BASEPATH_DER = "/Users/gio/Desktop/umich/data/data_blockingFramework/";

    public static void main(String[] args) throws IOException {

        int dataset = 0;
        String blocking_type = "M"; /*M or T*/
        WeightingScheme ws = WeightingScheme.CHI_ENTRO;
        ThresholdWeightingScheme th_schme = ThresholdWeightingScheme.AM3;

        List<EntityProfile>[] profiles;
        if (args.length > 0) {
            BASEPATH_CER = args[0] + "/";
            BASEPATH_DER = args[0] + "/";
            profiles = Exp_Util.getEntities(BASEPATH_CER + "profiles/", dataset, CLEAN);
        } else {
            profiles = Exp_Util.getEntities(CLEAN ? BASEPATH_CER : BASEPATH_DER + "profiles/", dataset, CLEAN);
        }

        AbstractBlockingMethod blocking;

        Instant start = Instant.now();

        if (profiles.length > 1) {
            blocking = (blocking_type == "T") ? new TokenBlocking(new List[]{profiles[0], profiles[1]}) :
                    new BlockBuilding.MemoryBased.AttributeClusteringBlockingEntropy(RepresentationModel.TOKEN_SHINGLING, profiles, 120, 3, true);
        } else {
            System.out.println("\nok\n");
            blocking = new BlockBuilding.MemoryBased.AttributeClusteringBlockingEntropy(RepresentationModel.TOKEN_SHINGLING, profiles, 120, 3, true);
            /*blocking = new BlockBuilding.MemoryBased.AttributeClusteringBlockingEntropy(RepresentationModel.TOKEN_SHINGLING, profiles, 120, 3, false);*/
        }
        List<AbstractBlock> blocks = blocking.buildBlocks();


        double SMOOTHING_FACTOR = 1.005; // CLEAN
        //double SMOOTHING_FACTOR = 1.0; // CLEAN Dbpedia
        //double SMOOTHING_FACTOR = 1.015; // DIRTY
        double FILTERING_RATIO = 0.8;

        // TODO NOTICE DOWN:
        //FOR CENSUS
//        double SMOOTHING_FACTOR = 1.05; // DIRTY
//        double FILTERING_RATIO = 1; //

        Instant start_purging = Instant.now();
        /*System.out.println("blocking time: " + Duration.between(start, start_purging));*/
        System.out.println("#B_AC: " + blocks.size());

        ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTHING_FACTOR);
        cbbp.applyProcessing(blocks);
        System.out.println("#B_BP: " + blocks.size());

        /*System.out.println("\n01: " + blocks.get(0).getEntropy() + "\n\n");*/

        BlockFiltering bf = new BlockFiltering(FILTERING_RATIO);
        bf.applyProcessing(blocks);
        System.out.println("#B_BF: " + blocks.size());

        /*System.out.println("\n02: " + blocks.get(0).getEntropy() + "\n\n");
        System.out.println("n. of blocks: " + blocks.size());*/

        AbstractDuplicatePropagation adp = Exp_Util.getGroundTruth(CLEAN ? BASEPATH_CER : BASEPATH_DER + "groundTruth/", dataset, CLEAN);


        Instant start_blast = Instant.now();
        System.out.println("block purging_filtering time: " + Duration.between(start_purging, start_blast));
        System.out.println("\nmain: " + blocks.get(0).getEntropy() + "\n\n");



        /*OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning b_wnp = new OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning(adp, ws, th_schme, blocks.size());*/
        /*OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning b_wnp = new OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning(adp, ws, th_schme, blocks.size());*/
        BlastWeightedNodePruning b_wnp = new BlastWeightedNodePruning(adp, ws, th_schme, blocks.size());
        /*MetaBlocking.EnhancedMetaBlocking.FastImplementations.RedefinedCardinalityNodePruning b_wnp= new MetaBlocking.EnhancedMetaBlocking.FastImplementations.RedefinedCardinalityNodePruning(WeightingScheme.CHI_ENTRO);*/
        b_wnp.applyProcessing(blocks);

        double[] values = b_wnp.getPerformance();

        System.out.println("pc: " + values[0]);
        System.out.println("pq: " + values[1]);
        System.out.println("f1: " + (2 * values[0] * values[1]) / (values[0] + values[1]));

        /*BlockStatistics bStats1 = new BlockStatistics(blocks, adp);
        double[] values = bStats1.applyProcessing();
        System.out.println("final  1 " + values[0] +" values 2 " + values[1] +" values 3" + values[2]);*/

        Instant end_blast = Instant.now();

        System.out.println("blast time: " + Duration.between(start_blast, end_blast));
        System.out.println("Total time: " + Duration.between(start, end_blast));
    }
}