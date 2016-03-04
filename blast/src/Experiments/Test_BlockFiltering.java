

/**
 * @author @stravanni
 */
package Experiments;

import BlockBuilding.MemoryBased.TokenBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import BlockProcessing.ComparisonRefinement.BilateralDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.EntityProfile;
import MetaBlocking.EnhancedMetaBlocking.FastImplementations.ReciprocalCardinalityNodePruning;
import MetaBlocking.WeightingScheme;
import Utilities.BlockStatistics;
import Utilities.SerializationUtilities;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


//import BlockBuilding.DiskBased.TokenBlocking;

/**
 * @author stravanni
 */

public class Test_BlockFiltering {

    public static void main(String[] args) throws IOException {
        Instant start = Instant.now();

        String basePath = "";
        if (args.length == 0) {
            basePath = "/Users/gio/Desktop/umich/data/data_blockingFramework/";
        } else {
            basePath = args[0] + "/";
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


        for (int i = 0; i < 1; i++) {
            //for (int i = 1; i < 2; i++) {
            //for (int i = 2; i < 3; i++) {
            //for (int i = 3; i < 4; i++) {
            //for (int i = 4; i < 5; i++) {
            //for (int i = 3; i < 4; i++) {
            //for (int i = 5; i < 6; i++) {
            //for (int i = 0; i < 5; i++) {
            String profilesPath1 = folders[i] + dataset + datasets[i][0];
            String profilesPath2 = folders[i] + dataset + datasets[i][1];

            String[] profilesPath = {profilesPath1, profilesPath2};

            List<AbstractBlock> blocks_original;
            List<AbstractBlock> blocks;


            List<EntityProfile>[] profiles = new List[2];
            profiles[0] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(profilesPath[0]);
            profiles[1] = (List<EntityProfile>) SerializationUtilities.loadSerializedObject(profilesPath[1]);
            TokenBlocking block = new TokenBlocking(profiles);
            //AttributeClusteringBlocking block = new AttributeClusteringBlocking(RepresentationModel.TOKEN_UNIGRAMS, profiles);
            //AttributeClusteringBlocking_candidate_02 block = new AttributeClusteringBlocking_candidate_02(RepresentationModel.TOKEN_UNIGRAMS, profilesPath);
            blocks = block.buildBlocks();

            //blocks = Collections.synchronizedList((List<AbstractBlock>) blocks);

            System.out.println("Numero di blocchi: " + blocks.size());

            start = Instant.now();

            //blocks = new ArrayList<>(blocks_original);

            //TokenBlocking tb = new TokenBlocking(profiles);
            //List<AbstractBlock> blocks = tb.buildBlocks();

            double FILTERING_RATIO = 0.8;
            double SMOOTHING_FACTOR = 1.005;
            WeightingScheme ws = WeightingScheme.JS;

            ComparisonsBasedBlockPurging cbbp = new ComparisonsBasedBlockPurging(SMOOTHING_FACTOR);
            cbbp.applyProcessing(blocks);

            BlockFiltering bf = new BlockFiltering(FILTERING_RATIO);
            bf.applyProcessing(blocks);

            //FastComparisonPropagation fcp = new FastComparisonPropagation();
            //fcp.applyProcessing(blocks);

            AbstractDuplicatePropagation adp = new BilateralDuplicatePropagation(folders[i] + "groundtruth");

//            OnTheFlyMethods.FastImplementations.WeightedNodePruning wnp = new OnTheFlyMethods.FastImplementations.WeightedNodePruning(adp, WeightingScheme.ARCS);
//            wnp.applyProcessing(blocks);
//            double[] values = wnp.getPerformance();

            double[] values;

            List<AbstractBlock> blocks_ = new ArrayList<>(blocks);

            //OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning rr_wnp = new OnTheFlyMethods.FastImplementations.ReciprocalWeightedNodePruning(adp, ws);
            //OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning rr_wnp = new OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning(adp, ws);
            //rr_wnp.applyProcessing(blocks_);
            //values = rr_wnp.getPerformance();

            ReciprocalCardinalityNodePruning rr_cnp = new ReciprocalCardinalityNodePruning(ws);
            //RedefinedCardinalityNodePruning rr_cnp = new RedefinedCardinalityNodePruning(ws);
            rr_cnp.applyProcessing(blocks_);

            BlockStatistics bStats = new BlockStatistics(blocks_, new BilateralDuplicatePropagation(folders[i] + "groundtruth"));
            values = bStats.applyProcessing();


            System.out.println("rec:");
            System.out.println("pc: " + values[0]);
            System.out.println("pq: " + values[1]);

//            blocks_ = new ArrayList<>(blocks);
//
//            OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning r_wnp = new OnTheFlyMethods.FastImplementations.RedefinedWeightedNodePruning(adp, ws);
//            r_wnp.applyProcessing(blocks_);
//            values = r_wnp.getPerformance();
//
//            System.out.println("red:");
//            System.out.println("pc: " + values[0]);
//            System.out.println("pq: " + values[1]);


            //BlockStatistics bStats = new BlockStatistics(blocks, new BilateralDuplicatePropagation(folders[i] + "groundtruth"));
            //bStats.applyProcessing();


//            double SMOOTHING_FACTOR = 1.015;
//            ComparisonsBasedBlockPurging cp = new ComparisonsBasedBlockPurging(SMOOTHING_FACTOR);
//            cp.applyProcessing(blocks);
//
//            BlockFiltering blockFiltering = new BlockFiltering(0.25);
//            blockFiltering.applyProcessing(blocks);
//
//
//            AbstractDuplicatePropagation adp = new BilateralDuplicatePropagation(folders[i] + "groundtruth");
//
//            BlockStatistics bStats0 = new BlockStatistics(blocks, new BilateralDuplicatePropagation(folders[i] + "groundtruth"));
//            bStats0.applyProcessing();
//            System.out.println("\n\n###########################\n\n");

            //RedefinedWeightedNodePruning of_red = new RedefinedWeightedNodePruning(adp, WeightingScheme.CBS);
            //of_red.applyProcessing();

//            BlockStatistics bStats2 = new BlockStatistics(blocks, new BilateralDuplicatePropagation(folders[i] + "groundtruth"));
//            double[] values = bStats2.applyProcessing();
//
//            System.out.println(values[0]);
//            System.out.println(values[1]);


            Instant end = Instant.now();
            String duration = Duration.between(start, end).toString();
            System.out.println("Total time: " + Duration.between(start, end));

        }
    }
//    public static double round(double value, int places) {
//        if (places < 0) throw new IllegalArgumentException();
//
//        BigDecimal bd = new BigDecimal(value);
//        bd = bd.setScale(places, RoundingMode.HALF_UP);
//        return bd.doubleValue();
//    }
}