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
                System.out.println(entropies1[index]);
            } else {
                entropies2[index] = attributeModels[index].getEntropyToken(false);
            }

            index++;
        }
        return attributeModels;
    }

}