package BlockBuilding;

import DataStructures.Attribute;
import DataStructures.EntityProfile;
import RepresentationModels.AbstractModel;
import RepresentationModels.TokenShingling;
import Utilities.Constants;
import Utilities.RepresentationModel;
import info.debatty.java.lsh.MinHash;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author stravanni
 */


public abstract class AbstractAttributeClusteringBlockingEntropy extends AbstractAttributeClusteringBlocking {

    private final Map<String, Double>[] entropies;
    private double[] entropies1;
    private double[] entropies2;

    private double[] entropy_clusters;

    public AbstractAttributeClusteringBlockingEntropy(RepresentationModel md, List<EntityProfile>[] profiles) {
        super(md, profiles);

        entropies = new HashMap[2];
    }

    public AbstractAttributeClusteringBlockingEntropy(RepresentationModel md, List<EntityProfile>[] profiles, int minhash_size, int rows_per_band, boolean approx) {
        super(md, profiles, minhash_size, rows_per_band, approx);

        entropies = new HashMap[2];
    }

    public AbstractAttributeClusteringBlockingEntropy(RepresentationModel md, String[] entities, String[] index) {
        super(md, entities, index);

        entropies = new HashMap[2];
    }

    @Override
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

        /*
         * for the entorpy computation
         */
        if (sourceId == 0) {
            entropies1 = new double[attributeProfiles.size()];
        } else {
            entropies2 = new double[attributeProfiles.size()];
        }

        /*
         * for the entorpy computation
         */
        if (sourceId == 0) {
            entropies1 = new double[attributeProfiles.size()];
        } else {
            entropies2 = new double[attributeProfiles.size()];
        }

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

            // Here I'm using EntropyToken, in the paper I used entropyInstance
            // entorpyToken is computed considering all the tokens of an attribute
            // entropyInstance considers each instance of attribute as a value (i.e. bag of tokens)
            if (sourceId == 0) {
                entropies1[index] = attributeModels[index].getEntropyToken(false);
                System.out.println("i: " + index + " " + entry.getKey() + " h: " + entropies1[index]);
            } else {
                entropies2[index] = attributeModels[index].getEntropyToken(false);
                System.out.println("i: " + index + " " + entry.getKey() + " h: " + entropies2[index]);
            }

            index++;
        }
        System.out.println("\n");
        return attributeModels;
    }


    @Override
    protected void clusterAttributes(AbstractModel[] attributeModels, SimpleGraph graph) {
        int noOfAttributes = attributeModels.length;

        double entropy_inside_cluster = 0;
        double entropy_inside_cluster_singleton = 0;
        int singletonSize = 0;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;

        entropy_clusters = new double[connectedComponents.size() + 2];

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
                if (clusterId == singletonId) {
                    entropy_inside_cluster_singleton = entropies1[attributeId];
                    singletonSize++;
                } else {
                    entropy_inside_cluster = entropies1[attributeId];
                }
            }
            entropy_inside_cluster /= cluster.size();
            entropy_clusters[clusterId] = entropy_inside_cluster;
        }
        attributeClusters[1] = null;
    }

    @Override
    protected void clusterAttributes(AbstractModel[] attributeModels1, AbstractModel[] attributeModels2, SimpleGraph graph) {

        System.out.println("cluster attribtutes with entropies");

        int d1Attributes = attributeModels1.length;
        int d2Attributes = attributeModels2.length;

        double entropy_inside_cluster = 0;
        double entropy_inside_cluster_singleton = 0;
        int singletonSize = 0;

        ConnectivityInspector ci = new ConnectivityInspector(graph);
        List<Set<Integer>> connectedComponents = ci.connectedSets();
        int singletonId = connectedComponents.size() + 1;

        entropy_clusters = new double[connectedComponents.size() + 2];

        attributeClusters[0] = new HashMap<>(2 * d1Attributes);
        attributeClusters[1] = new HashMap<>(2 * d2Attributes);
        int counter = 0;
        for (Set<Integer> cluster : connectedComponents) {
            entropy_inside_cluster = 0;
            System.out.println("cluster: " + cluster.toString());
            int clusterId = counter;
            if (cluster.size() == 1) {
                clusterId = singletonId;
            } else {
                counter++;
            }

            /**
             * todo in the paper it was not "+=", but only "="
             */
            for (int attributeId : cluster) {
                if (attributeId < d1Attributes) {
                    attributeClusters[0].put(attributeModels1[attributeId].getInstanceName(), clusterId);
                    if (clusterId == singletonId) {
                        entropy_inside_cluster_singleton += entropies1[attributeId];
                        singletonSize++;
                    } else {
                        entropy_inside_cluster += entropies1[attributeId];
                    }
                } else {
                    attributeClusters[1].put(attributeModels2[attributeId - d1Attributes].getInstanceName(), clusterId);
                    if (clusterId == singletonId) {
                        entropy_inside_cluster_singleton += entropies2[attributeId - d1Attributes];
                        singletonSize++;
                    } else {
                        entropy_inside_cluster += entropies2[attributeId - d1Attributes];
                    }
                }
            }
            entropy_inside_cluster /= cluster.size();
            entropy_clusters[clusterId] = entropy_inside_cluster;
        }
        entropy_inside_cluster_singleton /= singletonSize;
        entropy_clusters[singletonId] = entropy_inside_cluster_singleton;
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
                    String clusterSuffix = CLUSTER_PREFIX + clusterId + CLUSTER_SUFFIX + String.valueOf(entropy_clusters[clusterId]);
                    //System.out.println(clusterSuffix);
                    for (String token : getTokens(attribute.getValue())) {
                        if (0 < token.trim().length()) {
                            doc.add(new StringField(VALUE_LABEL, token.trim() + clusterSuffix, Field.Store.YES));
                        }
                    }
                }

                index.addDocument(doc);
            }
            for (String key : clusters.keySet()) {
                System.out.println(key + ":\t" + clusters.get(key) + "\th: " + entropy_clusters[clusters.get(key)]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}