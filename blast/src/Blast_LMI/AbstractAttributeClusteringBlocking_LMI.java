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
package Blast_LMI;

import BlastUtilities.AbstractModel;
import BlastUtilities.BlastConstants;
import BlastUtilities.BlastRepresentationModel;
import BlockBuilding.AbstractTokenBlocking;
import DataStructures.Attribute;
import DataStructures.EntityProfile;
import Utilities.Constants;
import Utilities.RepresentationModel;
import com.google.common.hash.BloomFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author gap2
 */
public abstract class AbstractAttributeClusteringBlocking_LMI extends AbstractTokenBlocking implements Constants, BlastConstants {

    private final Map<String, Integer>[] attributeClusters;
    private final Map<String, Double>[] entropies;
    private final BlastRepresentationModel model;

    private double[] entropies1;
    private double[] entropies2;

    private double[] entropy_clusters;

    private HashSet<String> wordSet = new HashSet();

    public AbstractAttributeClusteringBlocking_LMI(BlastRepresentationModel md, String[] profilePath, String[] index) {
        super("Disk-based Attribute Clustering Blocking", profilePath, index);

        model = md;
        attributeClusters = new HashMap[2];
        entropies = new HashMap[2];
        AbstractModel[] attributeModels1 = buildAttributeModels(profilePath[0], 0);
        if (profilePath.length == 2) { // Clean-Clean ER
            AbstractModel[] attributeModels2 = buildAttributeModels(profilePath[1], 1);


            SimpleGraph graph = compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }
    }

    // with list generated from SH
    //public AbstractAttributeClusteringBlocking(RepresentationModel md, String[] profilePath, String[] index, HashSet<String> pairs_lsh) {
    public AbstractAttributeClusteringBlocking_LMI(BlastRepresentationModel md, String[] profilePath, String[] index, BloomFilter pairBloom) {
        super("Disk-based Attribute Clustering Blocking", profilePath, index);

        model = md;
        attributeClusters = new HashMap[2];
        entropies = new HashMap[2];
        AbstractModel[] attributeModels1 = buildAttributeModels(profilePath[0], 0);
        if (profilePath.length == 2) { // Clean-Clean ER
            AbstractModel[] attributeModels2 = buildAttributeModels(profilePath[1], 1);

            SimpleGraph graph = compareAttributes(attributeModels1, attributeModels2, pairBloom);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }
    }

    public AbstractAttributeClusteringBlocking_LMI(BlastRepresentationModel md, String[] profilePath, String[] index, String source) {
        super("Disk-based Attribute Clustering Blocking", profilePath, index);

        model = md;
        attributeClusters = new HashMap[2];
        entropies = new HashMap[2];
        AbstractModel[] attributeModels1 = buildAttributeModels(profilePath[0], source + "1");
        if (profilePath.length == 2) { // Clean-Clean ER
            AbstractModel[] attributeModels2 = buildAttributeModels(profilePath[1], source + "2");

            SimpleGraph graph = compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }
    }

    private AbstractModel[] buildAttributeModels(String entitiesPath, int source) {
        List<EntityProfile> profiles = loadEntities(entitiesPath);
        final HashMap<String, List<String>> attributeProfiles = new HashMap<>();
        profiles.stream().forEach((entity) -> {
            entity.getAttributes().stream().forEach((attribute) -> {
                List<String> values = attributeProfiles.get(attribute.getName());
                if (values == null) {
                    values = new ArrayList<>();
                    attributeProfiles.put(attribute.getName(), values);
                }
                values.add(attribute.getValue());
            });
        });
        profiles.clear();

        int index = 0;


        //AbstractModel[] attributeModels = new AbstractModel[attributeProfiles.size()];
        AbstractModel[] attributeModels = new AbstractModel[attributeProfiles.size()];
        if (source == 0) {
            entropies1 = new double[attributeProfiles.size()];
        } else {
            entropies2 = new double[attributeProfiles.size()];
        }

//        for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
//            attributeModels[index] = RepresentationModel.getModel(model, entry.getKey());
//            for (String value : entry.getValue()) {
//                attributeModels[index].updateModel(value);
//            }
//            index++;
//        }

        for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
            attributeModels[index] = BlastRepresentationModel.getModel(model, entry.getKey());
            for (String value : entry.getValue()) {
                //attributeModels[index].updateModel(value, wordSet);
                attributeModels[index].updateModel(value);
            }
            //attributeModels[index].getEntropyToken(false);
            //System.out.println(attributeModels[index].getName());
            //System.out.println(index);
            //System.out.println(attributeModels[index].getEntropyToken(false));
            if (source == 0) {
                //entropies1[index] = attributeModels[index].getEntropyToken(true);
                entropies1[index] = attributeModels[index].getEntropyInstamnce(false);
                //System.out.println(attributeModels[index].getName() + "_1: " + entropies1[index]);
            } else {
                //entropies2[index] = attributeModels[index].getEntropyToken(true);
                entropies2[index] = attributeModels[index].getEntropyInstamnce(false);
                //System.out.println(attributeModels[index].getName() + "_2: " + entropies2[index]);
            }
            index++;
        }

        // iterate and write attributeProfiles

        return attributeModels;
    }

    private AbstractModel[] buildAttributeModels(String entitiesPath, String source) {
        List<EntityProfile> profiles = loadEntities(entitiesPath);
        final HashMap<String, List<String>> attributeProfiles = new HashMap<>();
        profiles.stream().forEach((entity) -> {
            entity.getAttributes().stream().forEach((attribute) -> {
                List<String> values = attributeProfiles.get(attribute.getName());
                if (values == null) {
                    values = new ArrayList<>();
                    attributeProfiles.put(attribute.getName(), values);
                }
                values.add(attribute.getValue());
            });
        });
        profiles.clear();

        int index = 0;
        AbstractModel[] attributeModels = new AbstractModel[attributeProfiles.size()];
        for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
            attributeModels[index] = (AbstractModel) BlastRepresentationModel.getModel(model, entry.getKey());
            for (String value : entry.getValue()) {
                attributeModels[index].updateModel(value);
            }
            index++;
        }

        // iterate and write attributeProfiles

        try {
            PrintWriter writer1 = new PrintWriter("__attributes_values_" + source + ".csv");
            for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
                String attribtue = entry.getKey();
                StringBuilder values = new StringBuilder();
                for (String value : entry.getValue()) {
                    //values.append(value.replaceAll("\\p{Punct}", "").toLowerCase() + " ");
                    values.append(value.toLowerCase() + " ");
                }
                writer1.println(attribtue + "," + values.toString());
            }
            writer1.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return attributeModels;
    }

    private void clusterAttributes(AbstractModel[] attributeModels, SimpleGraph graph) {
        System.out.println("CLUSTER ATTRIBUTE 2");
        int noOfAttributes = attributeModels.length;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;

        attributeClusters[0] = new HashMap<>(2 * noOfAttributes);
        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            int clusterId = counter;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                //if (cluster.size() > 1) {
                counter++;
            }

            for (int attributeId : cluster) {
                //System.out.println("attributeModels[attributeId].getInstanceName() = " + attributeModels[attributeId].getInstanceName());
                attributeClusters[0].put(attributeModels[attributeId].getInstanceName(), clusterId);
            }
        }
        attributeClusters[1] = null;
    }

    private void clusterAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2, SimpleGraph graph) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;

        attributeClusters[0] = new HashMap<>(2 * d1Attributes);
        attributeClusters[1] = new HashMap<>(2 * d2Attributes);

        entropies[0] = new HashMap<>(2 * d1Attributes);
        entropies[1] = new HashMap<>(2 * d2Attributes);

        entropy_clusters = new double[connectedComponents.size() + 2];
        System.out.println(connectedComponents.size() + 1);
        System.out.println("---");

        double entropy_inside_cluster_singleton = Double.MAX_VALUE;
        int count_singleton = 0;

        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            int clusterId = counter;
            double entropy_inside_cluster = 0;
            ArrayList<Double> entropy_inside_cluster_array = new ArrayList<>();
            double max_entropy_inside_cluster = 0;
            double min_entropy_inside_cluster = Double.MAX_VALUE;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                counter++;
            }

            for (int attributeId : cluster) {
                if (attributeId < d1Attributes) {
                    attributeClusters[0].put(attributeModels1[attributeId].getInstanceName(), clusterId);
                    if (clusterId != singletonId) {
                        System.out.println("1_" + attributeModels1[attributeId].getInstanceName() + ": " + entropies1[attributeId]);
                        entropy_inside_cluster += entropies1[attributeId];
                        entropy_inside_cluster_array.add(entropies1[attributeId]);
                        max_entropy_inside_cluster = Math.max(max_entropy_inside_cluster, entropies1[attributeId]);
                        min_entropy_inside_cluster = Math.min(min_entropy_inside_cluster, entropies1[attributeId]);
                        //System.out.println(entropies1[attributeId]);
                    } else {
                        entropy_inside_cluster_singleton = Math.min(entropy_inside_cluster_singleton, entropies1[attributeId]);
                        //entropy_inside_cluster_singleton += entropies1[attributeId];
                        count_singleton++;
                    }
                } else {
                    attributeClusters[1].put(attributeModels2[attributeId - d1Attributes].getInstanceName(), clusterId);
                    if (clusterId != singletonId) {
                        System.out.println("2_" + attributeModels2[attributeId - d1Attributes].getInstanceName() + ": " + entropies2[attributeId - d1Attributes]);
                        entropy_inside_cluster += entropies2[attributeId - d1Attributes];
                        entropy_inside_cluster_array.add(entropies2[attributeId - d1Attributes]);
                        max_entropy_inside_cluster = Math.max(max_entropy_inside_cluster, entropies2[attributeId - d1Attributes]);
                        min_entropy_inside_cluster = Math.min(min_entropy_inside_cluster, entropies2[attributeId - d1Attributes]);
                        entropies[1].put(attributeModels2[attributeId - d1Attributes].getInstanceName(), entropies2[attributeId - d1Attributes]);
                        //max_entropy_inside_cluster = Math.max(max_entropy_inside_cluster, entropies2[attributeId - d1Attributes]);
                        //System.out.println(entropies2[attributeId - d1Attributes]);
                    } else {
                        entropy_inside_cluster_singleton = Math.min(entropy_inside_cluster_singleton, entropies2[attributeId - d1Attributes]);
                        //entropy_inside_cluster_singleton += entropies2[attributeId - d1Attributes];
                        count_singleton++;
                    }
                }
                //System.out.println(attributeId);
                //System.out.println(attributeModels[attributeId].getEntropyToken(false));
            }
            if (clusterId != singletonId) {
                entropy_inside_cluster /= cluster.size();
                entropy_clusters[clusterId] = entropy_inside_cluster;
                System.out.println(clusterId + "::: " + entropy_clusters[clusterId]);
            }
            //System.out.println(clusterId);

            //entropy_clusters[clusterId] = mode(entropy_inside_cluster_array);
            //entropy_clusters[clusterId] = max_entropy_inside_cluster;
            //entropy_clusters[clusterId] = (max_entropy_inside_cluster + min_entropy_inside_cluster)/2.0;

            if ((entropy_inside_cluster == 0) &&
                    (clusterId != singletonId)) {
                System.out.println("problema qua in AbstractAttributeClusteringBlocking");
            }
            //System.out.println(entropy_clusters[clusterId]);
            //System.out.println(clusterId);
        }

        //entropy_clusters[singletonId] = entropy_inside_cluster_singleton / count_singleton;
        entropy_clusters[singletonId] = entropy_inside_cluster_singleton;
        System.out.println(singletonId + "::: " + entropy_clusters[singletonId]);

        System.out.println("attributeClusters_0 = " + attributeClusters[0].toString());
        System.out.println("attributeClusters_1 = " + attributeClusters[1].toString());
    }

    private SimpleGraph compareAttributes(AbstractModel[] attributeModels) {
        int noOfAttributes = attributeModels.length;
        int[] mostSimilarName = new int[noOfAttributes];
        double[] maxSimillarity = new double[noOfAttributes];
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < noOfAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        for (int i = 0; i < noOfAttributes; i++) {
            for (int j = i + 1; j < noOfAttributes; j++) {
                double simValue = attributeModels[i].getSimilarity(attributeModels[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j;
                }

                if (maxSimillarity[j] < simValue) {
                    maxSimillarity[j] = simValue;
                    mostSimilarName[j] = i;
                }
            }
        }

        for (int i = 0; i < noOfAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    public double mode(ArrayList<Double> entropies) {
        double mode = 0;
        //Collections.sort(entropies);
        Map<Double, Integer> seen = new HashMap<Double, Integer>();
        int max = 0;
        List<Double> maxElems = new ArrayList<Double>();
        for (Double value : entropies) {
            if (seen.containsKey(value))
                seen.put(value, seen.get(value) + 1);
            else
                seen.put(value, 1);
            if (seen.get(value) > max) {
                max = seen.get(value);
                maxElems.clear();
                maxElems.add(value);
            } else if (seen.get(value) == max) {
                maxElems.add(value);
            }
        }
        return maxElems.get(0);


        //return mode;
    }

//    private void readFilterList(String file_path) throws IOException {
//        System.out.println("loading pairSet...");
//        File file = new File(file_path);
//        FileReader fileReader = new FileReader(file);
//        BufferedReader bufferedReader = new BufferedReader(fileReader);
//        String line = null;
//        while ((line = bufferedReader.readLine()) != null) {
//            System.out.println("ok");
//            this.pairSet.add(line);
//        }
//        fileReader.close();
//        System.out.println("pairSet loaded");


//        File list_of_pairs = new File(file_path);
//
//        FileInputStream fis = new FileInputStream(list_of_pairs);
//
//        //Construct BufferedReader from InputStreamReader
//        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
//
//        String line = null;
//        while ((line = br.readLine()) != null) {
//            //System.out.println(line);
//            this.pairSet.add(line);
//        }

    //br.close();
    //}

    private SimpleGraph compareAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;
        int totalAttributes = d1Attributes + d2Attributes;
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);

        int[] mostSimilarName = new int[totalAttributes];
        ArrayList<Integer>[] mostSimilarNames = new ArrayList[totalAttributes];
        double[] maxSimillarity = new double[totalAttributes];
        for (int i = 0; i < totalAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            mostSimilarNames[i] = new ArrayList<>();
            namesGraph.addVertex(i);
        }

        for (int i = 0; i < d1Attributes; i++) {
            for (int j = 0; j < d2Attributes; j++) {
                //double simValue = attributeModels1[i].getSimilarity(attributeModels2[j], wordSet);
                double simValue = attributeModels1[i].getSimilarity(attributeModels2[j]);
                if (maxSimillarity[i] < simValue) {
                    maxSimillarity[i] = simValue;
                    mostSimilarName[i] = j + d1Attributes;
                }

                if (maxSimillarity[j + d1Attributes] < simValue) {
                    maxSimillarity[j + d1Attributes] = simValue;
                    mostSimilarName[j + d1Attributes] = i;
                }
            }
        }

        for (int i = 0; i < d1Attributes; i++) {
            for (int j = 0; j < d2Attributes; j++) {
                //double simValue = attributeModels1[i].getSimilarity(attributeModels2[j], wordSet);
                double simValue = attributeModels1[i].getSimilarity(attributeModels2[j]);
                if (simValue >= (0.85d * maxSimillarity[i])) {
                    //if (simValue >= (0.99d * maxSimillarity[i])) {
                    mostSimilarNames[i].add(j + d1Attributes);
                }
                if (simValue >= (0.85d * maxSimillarity[j + d1Attributes])) {
                    //if (simValue >= (0.99d * maxSimillarity[j + d1Attributes])) {
                    mostSimilarNames[j + d1Attributes].add(i);
                }
            }
        }

        //if (d1Attributes <= d2Attributes){
        for (int i = 0; i < d1Attributes; i++) {
            ArrayList<Integer> candidates = mostSimilarNames[i];
            for (int candidate : candidates) {
                if (mostSimilarNames[candidate].contains(i)) {
                    namesGraph.addEdge(i, candidate);
                }
            }
        }
        //}


//        for (int i = 0; i < totalAttributes; i++) {
//            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
//                namesGraph.addEdge(i, mostSimilarName[i]);
//            }
//        }
        return namesGraph;
    }

    // With the filter list generated with LSH
    private SimpleGraph compareAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2, BloomFilter pairBloom) {
        //this.pairSet = pairs_lsh;
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;
        int totalAttributes = d1Attributes + d2Attributes;
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);

        int[] mostSimilarName = new int[totalAttributes];
        double[] maxSimillarity = new double[totalAttributes];
        for (int i = 0; i < totalAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        int counter_pair = 0;

        for (int i = 0; i < d1Attributes; i++) {
            for (int j = 0; j < d2Attributes; j++) {

                String a1_name = attributeModels1[i].getName();
                String a2_name = attributeModels2[j].getName();
                String composed_name = a1_name.concat(a2_name);
                //String composed_name = a2_name.concat(a1_name);

                if (pairBloom.mightContain(composed_name)) {
                    //System.out.println("lsh");
                    counter_pair++;

                    double simValue = attributeModels1[i].getSimilarity(attributeModels2[j]);
                    if (maxSimillarity[i] < simValue) {
                        maxSimillarity[i] = simValue;
                        mostSimilarName[i] = j + d1Attributes;
                    }

                    if (maxSimillarity[j + d1Attributes] < simValue) {
                        maxSimillarity[j + d1Attributes] = simValue;
                        mostSimilarName[j + d1Attributes] = i;
                    }
                } else continue;
            }
        }

        System.out.println("###counter_pairs: " + counter_pair);

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            File file = new File("counter_pairs_aggregate_cardinality_AC_" + timeStamp + ".txt");
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(counter_pair);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < totalAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    private void computeEntropyInstance(AbstractModel[] attributeModels, String source) {

        PrintWriter writer1 = null, writer2 = null, writer3 = null, writer4 = null, writer5 = null, writer6 = null;
        try {
            double alpha = 1.5;
            double b = 0.5;

            writer1 = new PrintWriter("__00entropies_intance_classic" + source + ".csv");
            writer2 = new PrintWriter("__00entropies_intance_classic_normalized" + source + ".csv");

            writer3 = new PrintWriter("__11entropies_intance_reweighted_alpha_" + String.valueOf(alpha) + source + ".csv");
            writer4 = new PrintWriter("__11entropies_intance_reweighted_normalized_alpha_" + String.valueOf(alpha) + "_source_" + source + ".csv");

            writer5 = new PrintWriter("__22entropies_intance_pseudo_beta_" + String.valueOf(b) + source + ".csv");
            writer6 = new PrintWriter("__22entropies_intance_pseudo_normalized_beta_" + String.valueOf(b) + "_source_" + source + ".csv");


            int len = attributeModels.length;

            for (int i = 0; i < len; i++) {
                String attribute_name = attributeModels[i].getName();

                double entropy_classic = attributeModels[i].getEntropyInstamnce(false);
                writer1.println(attribute_name + ";" + entropy_classic);
                double entropy_normalized = attributeModels[i].getEntropyInstamnce(true);
                writer2.println(attribute_name + ";" + entropy_normalized);

                double entropy_reweighted = attributeModels[i].getEntropyRewightedInstamnce(false, alpha);
                writer3.println(attribute_name + ";" + entropy_reweighted);
                double entropy_reweighted_normalized = attributeModels[i].getEntropyRewightedInstamnce(true, alpha);
                writer4.println(attribute_name + ";" + entropy_reweighted_normalized);

                double entropy_pseudo = attributeModels[i].getEntropyPseudoInstamnce(false, b);
                writer5.println(attribute_name + ";" + entropy_pseudo);
                double entropy_pseudo_normalized = attributeModels[i].getEntropyPseudoInstamnce(true, b);
                writer6.println(attribute_name + ";" + entropy_pseudo_normalized);
//                try {
//                    Thread.sleep(5000);                 //1000 milliseconds is one second.
//                } catch(InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }
            }
            writer1.close();
            writer2.close();
            writer3.close();
            writer4.close();
            writer5.close();
            writer6.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

//    private void computeEntropyToken(AbstractModel[] attributeModels, int source) {
//
//        PrintWriter writer1 = null, writer2 = null;
//        try {
//            writer1 = new PrintWriter("entropies_token_classic" + source + ".csv");
//            writer2 = new PrintWriter("entropies_token_normalized" + source + ".csv");
//
//
//            int len = attributeModels.length;
//
//            for (int i = 0; i < len; i++) {
//                String attribute_name = attributeModels[i].getName();
//                double entropy_classic = attributeModels[i].getEntropyToken(false);
//                writer1.println(attribute_name + ";" + entropy_classic);
//                double entropy_normalized = attributeModels[i].getEntropyToken(true);
//                writer2.println(attribute_name + ";" + entropy_normalized);
//            }
//            writer1.close();
//            writer2.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        try {
            int counter = 0;
            Random random = new Random();
            for (EntityProfile profile : entities) {
                //int r = random.nextInt(9 - 0 + 1) + 0;
                //if (r < 0) {
                //    continue;
                //}
                //System.out.println("r: " + r);

                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));


                Set<Attribute> as = profile.getAttributes();
                Attribute[] aa = as.toArray(new Attribute[0]);

                TreeMap<String, Double> aa_map = new TreeMap<>();

                for (Attribute a : aa) {
                    aa_map.put(a.getName(), entropies[sourceId].get(a.getName()));
                }

                for (Entry entry : aa_map.entrySet()) {
                    aa_map.put((String) entry.getKey(), (Double) entry.getValue());
                }

                Set<String> names = new HashSet<>();
                int i = 0;

                for (Entry entry : aa_map.entrySet()) {
                    //if (i < 2) {
                    names.add((String) entry.getKey());
                    //}
                    i++;
                    //System.out.println(entry.getKey() + ": " + entry.getValue());
                }
                //System.out.println("");

                for (Attribute attribute : profile.getAttributes()) {
                    Integer clusterId = attributeClusters[sourceId].get(attribute.getName());
                    //double entropy = entropies[sourceId].get(attribute.getName());

                    if (names.contains(attribute.getName())) {

                        if (clusterId == null) {
                            //System.err.println(attribute.getName() + "\t\t" + attribute.getValue());
                            continue;
                        }
                        String clusterSuffix = ATTRIBUTE_CLUSTER_PREFIX + clusterId + ATTRIBUTE_CLUSTER_SUFFIX + String.valueOf(entropy_clusters[clusterId]);
//                        if (entropy_clusters[clusterId] < 13) {
//                            System.out.println("attribute_clustering_02: entropy " + entropy_clusters[clusterId]);
//                        }
                        //String clusterSuffix = ATTRIBUTE_CLUSTER_PREFIX + clusterId + ATTRIBUTE_CLUSTER_SUFFIX + String.valueOf(entropies[sourceId].get(attribute.getName()));
                        for (String token : getTokens(attribute.getValue())) {
                            if (0 < token.trim().length()) {
                                doc.add(new StringField(VALUE_LABEL, token.trim() + clusterSuffix, Field.Store.YES));
                                //doc.add(new DoubleField(token.trim() + clusterSuffix, entropy_clusters[clusterId], Field.Store.YES));
                            }
                        }
                    }
                }

                index.addDocument(doc);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}