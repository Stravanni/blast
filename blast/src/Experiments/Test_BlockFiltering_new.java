/**
 * @author @stravanni
 */
package Experiments;

//import BlockBuilding.MemoryBased.TokenBlocking_Gio;

//import BlockBuilding.DiskBased.AttributeClusteringBlocking;

import BlastUtilities.AttributeClusteringBlocking_LMI_inMemory;
import BlastUtilities.BlastRepresentationModel;
import Blast_MetaBlocking.OnTheFlyBlast.BlastWeightedNodePruning;
import Blast_MetaBlocking.ThresholdWeightingScheme;
import BlockBuilding.AbstractBlockingMethod;
import BlockBuilding.MemoryBased.TokenBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.BlockRefinement.SizeBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import BlockProcessing.ComparisonRefinement.BilateralDuplicatePropagation;
import BlockProcessing.ComparisonRefinement.UnilateralDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import MetaBlocking.EnhancedMetaBlocking.FastImplementations.ReciprocalCardinalityNodePruning;
import MetaBlocking.EnhancedMetaBlocking.FastImplementations.RedefinedWeightedNodePruning;
import MetaBlocking.EnhancedMetaBlocking.ReciprocalWeightedNodePruning;
import MetaBlocking.FastImplementations.CardinalityEdgePruning;
import MetaBlocking.FastImplementations.CardinalityNodePruning;
import MetaBlocking.WeightedEdgePruning;
import MetaBlocking.WeightingScheme;
import Utilities.BlockStatistics;
import Utilities.RepresentationModel;
import Utilities.SerializationUtilities;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static Utilities.SerializationUtilities.loadSerializedObject;
import static Utilities.SerializationUtilities.storeSerializedObject;


//import BlockBuilding.DiskBased.TokenBlocking;

/**
 * @author stravanni
 */

public class Test_BlockFiltering_new {

    public static void main(String[] args) throws IOException {


        String file_blocks;
        String basePath = "";
        if (args.length == 0) {
            basePath = "/Users/gio/Desktop/umich/data/data_blockingFramework/";
            file_blocks = "./";
        } else {
            basePath = args[0] + "/";
            //folder_out = args[1] + "/";
            file_blocks = "/data/" + args[2];
        }

        String[] folders = new String[6];
        String[] names = new String[6];
        String dataset = "profiles/";
        String[][] datasets = new String[6][2];

        String dir = basePath;

        names[0] = "articles_1";
        folders[0] = dir + "articles/";
        datasets[0][0] = "dataset1_dblp";
        datasets[0][1] = "dataset2_acm";

        names[1] = "movies";
        folders[1] = dir + "movies/";
        datasets[1][0] = "dataset1_imdb";
        datasets[1][1] = "dataset2_dbpedia";

        names[2] = "articles_2";
        folders[2] = dir + "articles2/";
        datasets[2][0] = "dataset1_dblp";
        datasets[2][1] = "dataset2_scholar";

        names[3] = "products_1";
        folders[3] = dir + "products/";
        datasets[3][0] = "dataset1_amazon";
        datasets[3][1] = "dataset2_gp";

        names[4] = "products_2";
        folders[4] = dir + "products2/";
        datasets[4][0] = "dataset1_abt";
        datasets[4][1] = "dataset2_buy";

        names[5] = "dbpedia";
        folders[5] = dir + "dbpedia/";
        datasets[5][0] = "dataset1";
        datasets[5][1] = "dataset2";


        //for (int i = 0; i < 1; i++) {
        //for (int i = 1; i < 2; i++) {
        for (int i = 2; i < 3; i++) {
            //for (int i = 3; i < 4; i++) { // Da scartare?
            //for (int i = 4; i < 5; i++) {
            //for (int i = 5; i < 6; i++) {
            //for (int i = 0; i < 5; i++) {
            String profilesPath1 = folders[i] + dataset + datasets[i][0];
            String profilesPath2 = folders[i] + dataset + datasets[i][1];

            String[] profilesPath = {profilesPath1, profilesPath2};

            List<AbstractBlock> blocks_original;
            List<AbstractBlock> blocks;

            String blocking_type = "M";
            boolean threshld_new = true;
            WeightingScheme ws = WeightingScheme.JS;

            double FILTERING_RATIO = 0.8;
            double SMOOTHING_FACTOR = 1.005;
            ThresholdWeightingScheme th_schme = ThresholdWeightingScheme.AM2;

            file_blocks = file_blocks + "blocks_" + blocking_type + "_" + names[i];

            Instant start = Instant.now();
            File f = new File(file_blocks);
            if (f.exists() && !f.isDirectory()) {
                blocks = (List<AbstractBlock>) loadSerializedObject(file_blocks);
                System.out.println("loaded pairSet");

            } else {

                List<EntityProfile>[] profiles = new List[2];
                profiles[0] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(profilesPath[0]);
                profiles[1] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(profilesPath[1]);

                AbstractBlockingMethod block;

                if (blocking_type == "T") {
                    block = new TokenBlocking(profiles);
                } else {
                    block = new AttributeClusteringBlocking_LMI_inMemory(BlastRepresentationModel.TOKEN_UNIGRAMS_ENTRO, profilesPath);
                    //block = new AttributeClusteringBlocking_original(RepresentationModel.TOKEN_UNIGRAMS, profilesPath);
                }
                blocks = block.buildBlocks(); //1239940

                Instant end = Instant.now();
                System.out.println(blocking_type + ": " + Duration.between(start, end));

                storeSerializedObject(blocks, file_blocks);
            }

            Instant end = Instant.now();
            System.out.println("AC time: " + Duration.between(start, end));

            System.out.println("Numero di blocchi: " + blocks.size());

            ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTHING_FACTOR);
            cbbp.applyProcessing(blocks);

//            SizeBasedBlockPurging sbbp = new SizeBasedBlockPurging();
//            sbbp.applyProcessing(blocks);

            BlockFiltering bf = new BlockFiltering(FILTERING_RATIO);
            bf.applyProcessing(blocks);

//            BlockStatistics bStats = new BlockStatistics(blocks, new UnilateralDuplicatePropagation(folders[i] + "groundtruth"));
//            bStats.applyProcessing();
//
//            AbstractDuplicatePropagation adp = new UnilateralDuplicatePropagation(folders[i] + "groundtruth");
            BlockStatistics bStats = new BlockStatistics(blocks, new BilateralDuplicatePropagation(folders[i] + "groundtruth"));
            double[] values = bStats.applyProcessing();

            AbstractDuplicatePropagation adp = new BilateralDuplicatePropagation(folders[i] + "groundtruth");

            //RedefinedWeightedNodePruning rwnp = new RedefinedWeightedNodePruning(WeightingScheme.EJS, true);
            //rwnp.applyProcessing(blocks);

            //OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning of_rrwnp = new OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning(adp, ws, threshld_new, threshld_new);
            //OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning of_rrwnp = new OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning(adp, ws);//, false, false);
            //of_rrwnp.applyProcessing(blocks);
            //values = of_rrwnp.getPerformance();

            //BlastWeightedNodePruning of_bwnp = new BlastWeightedNodePruning(adp, ws, ThresholdWeightingScheme.AM2);

            //blocks = Collections.synchronizedList(new ArrayList<AbstractBlock>(blocks));

//            ReciprocalCardinalityNodePruning cnp = new ReciprocalCardinalityNodePruning(ws);
//            cnp.applyProcessing(blocks);
//            bStats = new BlockStatistics(blocks, new BilateralDuplicatePropagation(folders[i] + "groundtruth"));
//
//            values = bStats.applyProcessing();

            //System.out.println("\nF-1: " + (5 * values[0] * values[1]) / ((4 * values[0]) + values[1]));

            System.out.println("block size main: " + blocks.size());

            /*
            blast
            */
//            BlastWeightedNodePruning of_bwnp = new BlastWeightedNodePruning(adp, ws, th_schme, blocks.size());
//            of_bwnp.applyProcessing(blocks);
//            values = of_bwnp.getPerformance();

//            System.out.println("\nmain t: " + of_bwnp.counter_tot);
//            System.out.println("\nmain a: " + of_bwnp.counter_a);
//            //System.out.println("dist a: " + of_bwnp.counter_a_set.size());
//            System.out.println("\nmain b: " + of_bwnp.counter_b);
//            //System.out.println("dist b: " + of_bwnp.counter_b_set.size());
//            System.out.println("non valide: " + (((double) (of_bwnp.counter_a + of_bwnp.counter_b)) / (double) of_bwnp.counter_tot));

            /*
            *
            * WNP - CLASSIC
            *
            * */
//            OnTheFlyMethods.FastImplementations.WeightedNodePruning wnp = new OnTheFlyMethods.FastImplementations.WeightedNodePruning(adp, ws);
//            wnp.applyProcessing(blocks);
//            double[] values = wnp.getPerformance();

            /*
            *
            * RECIPROCAL_WNP - AND
            *
            * */
//            Instant wnp_start = Instant.now();
//            OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning rr_wnp = new OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning(adp, ws);
//            rr_wnp.applyProcessing(blocks);
//            double[] values = rr_wnp.getPerformance();
//            Instant wnp_end = Instant.now();
//            System.out.println("\nwnp time: " + Duration.between(wnp_start, wnp_end) + "\n");


            /*
            *
            * REDEFINED_WNP - OR
            *
            * */
//            OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning r_wnp = new OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning(adp, ws);
//            r_wnp.applyProcessing(blocks);
//            double[] values = r_wnp.getPerformance();


            /*
            *
            * blast_wnp - (e1 + e2) / 4
            *
            * */
            Instant start_blast = Instant.now();
            //BlastWeightedNodePruning b_wnp = new BlastWeightedNodePruning(adp, ws, th_schme, blocks.size());

            Blast_MetaBlocking.CompleteBlast.BlastWeightedNodePruning b_wnp = new Blast_MetaBlocking.CompleteBlast.BlastWeightedNodePruning(ws, th_schme);
            //OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning b_wnp = new OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning(adp, ws);
            //BlastWeightedNodePruning bwnp = new BlastWeightedNodePruning(adp, ws, th_schemes[0], blocks.size());
            b_wnp.applyProcessing(blocks);
            end = Instant.now();
            System.out.println("Total time: " + Duration.between(start_blast, end));

            //values = b_wnp.getPerformance();

            bStats = new BlockStatistics(blocks, new BilateralDuplicatePropagation(folders[i] + "groundtruth"));
            values = bStats.applyProcessing();

            System.out.println("pc= " + values[0]);
            System.out.println("pq= " + values[1]);
            System.out.println("f1= " + (2 * values[0] * values[1]) / (values[0] + values[1]));


//            ReciprocalWeightedNodePruning rrwnp = new ReciprocalWeightedNodePruning(ws);
//            rrwnp.applyProcessing(blocks);
//
//            BlockStatistics bStats1 = new BlockStatistics(blocks, new UnilateralDuplicatePropagation(folders[i] + "groundtruth"));
//            double[] values = bStats1.applyProcessing();


            end = Instant.now();
            System.out.println("Total time: " + Duration.between(start, end));

        }
    }
}