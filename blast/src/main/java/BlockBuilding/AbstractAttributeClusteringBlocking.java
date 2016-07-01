package BlockBuilding;

import DataStructures.Attribute;
import DataStructures.EntityProfile;
import RepresentationModels.AbstractModel;
import RepresentationModels.TokenShingling;
import Utilities.Constants;
import Utilities.RepresentationModel;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import info.debatty.java.lsh.MinHash;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * @author stravanni
 */


public abstract class AbstractAttributeClusteringBlocking extends AbstractTokenBlocking implements Constants {

    protected int latestEntities;
    protected final Map<String, Integer>[] attributeClusters;
    protected final RepresentationModel model;


    protected int minhash_size = 120;
    protected int rows = 1;
    protected boolean approx = true;

    protected HashMap<String, Integer> all_tokens;
    protected int all_tokens_size;
    protected int all_shingle_counter;

    public AbstractAttributeClusteringBlocking(RepresentationModel md, List<EntityProfile>[] profiles) {
        super("Memory-based Attribute Clustering Blocking", profiles);

        model = md;

        if (md == RepresentationModel.TOKEN_SHINGLING) {
            all_shingle_counter = 0;
            all_tokens = new HashMap<>();
            sourceId = 0;
            buildShingles();
            if (cleanCleanER) {
                sourceId = 1;
                buildShingles();
            }
        }

        attributeClusters = new HashMap[2];
        sourceId = 0;
        AbstractModel[] attributeModels1 = (md == RepresentationModel.TOKEN_SHINGLING) ? buildAttributeModels_lsh() : buildAttributeModels();
        noOfEntities[0] = latestEntities;
        if (cleanCleanER) {
            sourceId = 1;
            AbstractModel[] attributeModels2 = (md == RepresentationModel.TOKEN_SHINGLING) ? buildAttributeModels_lsh() : buildAttributeModels();
            noOfEntities[1] = latestEntities;
            SimpleGraph graph = (md == RepresentationModel.TOKEN_SHINGLING) ? compareAttributesLSH(attributeModels1, attributeModels2, minhash_size, rows, approx) : compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = (md == RepresentationModel.TOKEN_SHINGLING) ? compareAttributesLSH(attributeModels1, minhash_size, rows, approx) : compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }
    }

    /**
     * Employ AttributeClustering with LSH pre-processing step.
     *
     * @param md            Representation Model (compatible with LSH)
     * @param profiles      List of EntityProfiles
     * @param minhash_size  The size of the minhash signature
     * @param rows_per_band The size of each band
     * @param approx        If ture the similarity among attributes is estimate thorugh the minhash signatures
     */
    public AbstractAttributeClusteringBlocking(RepresentationModel md, List<EntityProfile>[] profiles, int minhash_size, int rows_per_band, boolean approx) {
        super("Memory-based Attribute Clustering Blocking", profiles);

        model = md;

        this.minhash_size = minhash_size;
        this.rows = rows_per_band;
        this.approx = approx;

        if (md == RepresentationModel.TOKEN_SHINGLING) {
            all_shingle_counter = 0;
            all_tokens = new HashMap<>();
            sourceId = 0;
            buildShingles();
            if (cleanCleanER) {
                sourceId = 1;
                buildShingles();
            }
        }

        attributeClusters = new HashMap[2];
        sourceId = 0;
        AbstractModel[] attributeModels1 = (md == RepresentationModel.TOKEN_SHINGLING) ? buildAttributeModels_lsh() : buildAttributeModels();
        noOfEntities[0] = latestEntities;
        if (cleanCleanER) {
            sourceId = 1;
            AbstractModel[] attributeModels2 = (md == RepresentationModel.TOKEN_SHINGLING) ? buildAttributeModels_lsh() : buildAttributeModels();
            noOfEntities[1] = latestEntities;
            SimpleGraph graph = (md == RepresentationModel.TOKEN_SHINGLING) ? compareAttributesLSH(attributeModels1, attributeModels2, minhash_size, rows, approx) : compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = (md == RepresentationModel.TOKEN_SHINGLING) ? compareAttributesLSH(attributeModels1, minhash_size, rows, approx) : compareAttributes(attributeModels1);
            clusterAttributes(attributeModels1, graph);
        }
    }

    public AbstractAttributeClusteringBlocking(RepresentationModel md, String[] entities, String[] index) {
        super("Disk-based Attribute Clustering Blocking", entities, index);

        model = md;

        attributeClusters = new HashMap[2];
        sourceId = 0;
        AbstractModel[] attributeModels1 = buildAttributeModels();
        noOfEntities[0] = latestEntities;
        if (cleanCleanER) {
            sourceId = 1;
            AbstractModel[] attributeModels2 = buildAttributeModels();
            noOfEntities[1] = latestEntities;
            SimpleGraph graph = compareAttributes(attributeModels1, attributeModels2);
            clusterAttributes(attributeModels1, attributeModels2, graph);
        } else {
            SimpleGraph graph = compareAttributesLSH(attributeModels1, minhash_size, rows, approx);
            clusterAttributes(attributeModels1, graph);
        }
    }

    protected AbstractModel[] buildAttributeModels() {
        List<EntityProfile> profiles = getProfiles();
        latestEntities = profiles.size();

        final HashMap<String, List<String>> attributeProfiles = new HashMap<>();
        for (EntityProfile entity : profiles) {
            for (Attribute attribute : entity.getAttributes()) {
                List<String> values = attributeProfiles.get(attribute.getName());
                if (values == null) {
                    values = new ArrayList<>();
                    attributeProfiles.put(attribute.getName(), values);
                }
                values.add(attribute.getValue());
            }
        }

        if (entitiesPath != null) {
            profiles.clear();
        }

        int index = 0;
        AbstractModel[] attributeModels = new AbstractModel[attributeProfiles.size()];


        for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
            attributeModels[index] = RepresentationModel.getModel(model, entry.getKey());
            for (String value : entry.getValue()) {
                attributeModels[index].updateModel(value);
            }

            index++;
        }
        return attributeModels;
    }

    protected void buildShingles() {
        List<EntityProfile> profiles = getProfiles();
        //latestEntities = profiles.size();

        for (EntityProfile entity : profiles) {
            for (Attribute attribute : entity.getAttributes()) {
                String values = attribute.getValue();

                String[] tokens = gr.demokritos.iit.jinsect.utils.splitToWords(values);

                int noOfTokens = tokens.length;
                //if (attribute.getName().toLowerCase().trim().equals("year")) {System.out.println("tl: " + noOfTokens);}
                for (int j = 0; j < noOfTokens; j++) {
                    String feature = tokens[j].trim();

                    if (!(all_tokens.containsKey(feature))) {
                        all_tokens.put(feature, all_shingle_counter++);
                        //if (attribute.getName().toLowerCase().trim().equals("year")) {System.out.println("f: " + feature);}
                    }
                }
            }
        }
        all_tokens_size = all_tokens.size();
    }

    // build_lsh
    protected TokenShingling[] buildAttributeModels_lsh() {
        List<EntityProfile> profiles = getProfiles();
        latestEntities = profiles.size();

        final HashMap<String, List<String>> attributeProfiles = new HashMap<>();
        for (EntityProfile entity : profiles) {
            for (Attribute attribute : entity.getAttributes()) {
                List<String> values = attributeProfiles.get(attribute.getName());
                if (values == null) {
                    values = new ArrayList<>();
                    attributeProfiles.put(attribute.getName(), values);
                }
                values.add(attribute.getValue());
            }
        }

        if (entitiesPath != null) {
            profiles.clear();
        }

        int index = 0;
        TokenShingling[] attributeModels = new TokenShingling[attributeProfiles.size()];

        for (Entry<String, List<String>> entry : attributeProfiles.entrySet()) {
            attributeModels[index] = (TokenShingling) RepresentationModel.getModel(model, entry.getKey());
            for (String value : entry.getValue()) {
                if (approx) {
                    ((TokenShingling) attributeModels[index]).updateModelApproximation(value, all_tokens);
                } else {
                    attributeModels[index].updateModel(value, all_tokens);
                }
                //attributeModels[index].updateModel(value);
            }

            index++;
        }
        return attributeModels;
    }

    protected void clusterAttributes(AbstractModel[] attributeModels, SimpleGraph graph) {
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
                counter++;
            }

            for (int attributeId : cluster) {
                attributeClusters[0].put(attributeModels[attributeId].getInstanceName(), clusterId);
            }
        }
        attributeClusters[1] = null;
    }

    protected void clusterAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2, SimpleGraph graph) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;

        attributeClusters[0] = new HashMap<>(2 * d1Attributes);
        attributeClusters[1] = new HashMap<>(2 * d2Attributes);
        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            int clusterId = counter;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                counter++;
            }

            for (int attributeId : cluster) {
                if (attributeId < d1Attributes) {
                    attributeClusters[0].put(attributeModels1[attributeId].getInstanceName(), clusterId);
                } else {
                    attributeClusters[1].put(attributeModels2[attributeId - d1Attributes].getInstanceName(), clusterId);
                }
            }
        }
    }

    protected SimpleGraph compareAttributes(AbstractModel[] attributeModels) {
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

    protected SimpleGraph compareAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2) {
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

        for (int i = 0; i < d1Attributes; i++) {
            for (int j = 0; j < d2Attributes; j++) {
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

        for (int i = 0; i < totalAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    protected SimpleGraph compareAttributesLSH(AbstractModel[] attributeModels, int signature_size, int rows_band, boolean approx) {
        int noOfAttributes = attributeModels.length;
        int[] mostSimilarName = new int[noOfAttributes];
        double[] maxSimillarity = new double[noOfAttributes];
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < noOfAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        MinHash minhash = new MinHash(signature_size, all_tokens_size);

        int[][] signatures = new int[noOfAttributes][signature_size];
        for (int i = 0; i < noOfAttributes; i++) {
            HashSet<Integer> singature = ((TokenShingling) attributeModels[i]).getShinglesSet();
            signatures[i] = minhash.signature(singature);
        }

        HashMap<String, HashSet<Integer>> candidates = new HashMap<>();

        for (int i = 0; i < noOfAttributes; i++) {
            for (int j = 0; j < signature_size - rows_band; j += rows_band) {
                String band = "";
                for (int k = 0; k < rows_band; k++) {
                    band += "-" + Integer.toString(signatures[i][j + k]);
                }
                HashSet<Integer> candidate_set = candidates.getOrDefault(band, new HashSet<>());
                candidate_set.add(i);
                candidates.put(band, candidate_set);
            }
        }

        int counter_comparisons = 0;
        for (String key : candidates.keySet()) {
            if (candidates.get(key).size() > 1 && candidates.get(key).size() < 100) {
                for (Integer c1 : candidates.get(key)) {
                    for (Integer c2 : candidates.get(key)) {
                        if (c1 < c2) {
                            double simValue = (approx) ? minhash.similarity(signatures[c1], signatures[c2]) : attributeModels[c1].getSimilarity(attributeModels[c2]);

                            if (maxSimillarity[c1] < simValue) {
                                maxSimillarity[c1] = simValue;
                                mostSimilarName[c1] = c2;
                            }

                            if (maxSimillarity[c2] < simValue) {
                                maxSimillarity[c2] = simValue;
                                mostSimilarName[c2] = c1;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("attribute_profiles comparisons: " + counter_comparisons);

        for (int i = 0; i < noOfAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    protected SimpleGraph compareAttributesLSH(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2, int signature_size, int rows_band, boolean approx) {
        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;
        int totalAttributes = d1Attributes + d2Attributes;

        int[] mostSimilarName = new int[totalAttributes];
        double[] maxSimillarity = new double[totalAttributes];
        final SimpleGraph namesGraph = new SimpleGraph(DefaultEdge.class);
        for (int i = 0; i < totalAttributes; i++) {
            maxSimillarity[i] = -1;
            mostSimilarName[i] = -1;
            namesGraph.addVertex(i);
        }

        MinHash minhash = new MinHash(signature_size, all_tokens_size);

        int[][] signatures = new int[totalAttributes][signature_size];
        for (int i = 0; i < totalAttributes; i++) {
            //boolean[] singature = (i < d1Attributes) ? ((TokenShingling) attributeModels1[i]).getShingles() : ((TokenShingling) attributeModels2[i - d1Attributes]).getShingles();
            HashSet<Integer> singature = (i < d1Attributes) ? ((TokenShingling) attributeModels1[i]).getShinglesSet() : ((TokenShingling) attributeModels2[i - d1Attributes]).getShinglesSet();

            //System.out.println("bool: " + singature.length);
            signatures[i] = minhash.signature(singature);

//            for (int sig : signatures[i]) {
//                System.out.print(sig + "-");
//            }
//            System.out.println("");
        }

        HashMap<String, HashSet<Integer>> candidates = new HashMap<>();

        for (int i = 0; i < totalAttributes; i++) {
            for (int j = 0; j < signature_size - rows_band; j += rows_band) {
                String band = Integer.toString(j) + "-";
                for (int k = 0; k < rows_band; k++) {
                    band += "-" + Integer.toString(signatures[i][j + k]);
                }
                HashSet<Integer> candidate_set = candidates.getOrDefault(band, new HashSet<>());
                candidate_set.add(i);
                candidates.put(band, candidate_set);
            }
        }

        int counter_comparisons = 0;

        for (String key : candidates.keySet()) {
            if (candidates.get(key).size() > 1 && candidates.get(key).size() < 100) {
                for (Integer c1 : candidates.get(key)) {
                    for (Integer c2 : candidates.get(key)) {
                        if (c1 < c2) {
                            double simValue = (approx) ? minhash.similarity(signatures[c1], signatures[c2]) : ((TokenShingling) attributeModels1[c1]).getSimilarity_real(attributeModels2[c2 - d1Attributes]);

                            if (maxSimillarity[c1] < simValue) {
                                maxSimillarity[c1] = simValue;
                                mostSimilarName[c1] = c2;
                            }

                            if (maxSimillarity[c2] < simValue) {
                                maxSimillarity[c2] = simValue;
                                mostSimilarName[c2] = c1;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("attribute_profiles comparisons: " + counter_comparisons);

        for (int i = 0; i < totalAttributes; i++) {
            if (MINIMUM_ATTRIBUTE_SIMILARITY_THRESHOLD < maxSimillarity[i]) {
                namesGraph.addEdge(i, mostSimilarName[i]);
            }
        }
        return namesGraph;
    }

    @Override
    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        try {
            int counter = 0;
            HashMap<String, Integer> clusters = new HashMap<>();
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));
                for (Attribute attribute : profile.getAttributes()) {
                    Integer clusterId = attributeClusters[sourceId].get(attribute.getName());
                    //System.out.println(attribute.getName() + ": " + clusterId);
                    clusters.put(attribute.getName(), clusterId);
                    if (clusterId == null) {
                        System.err.println(attribute.getName() + "\t\t" + attribute.getValue());
                        continue;
                    }
                    String clusterSuffix = CLUSTER_PREFIX + clusterId + CLUSTER_SUFFIX;
                    for (String token : getTokens(attribute.getValue())) {
                        if (0 < token.trim().length()) {
                            doc.add(new StringField(VALUE_LABEL, token.trim() + clusterSuffix, Field.Store.YES));
                        }
                    }
                }

                index.addDocument(doc);
            }
            for (String key : clusters.keySet()) {
                System.out.println(key + ": " + clusters.get(key));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}