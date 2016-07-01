package RepresentationModels;

import Utilities.RepresentationModel;
import info.debatty.java.lsh.MinHash;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

/**
 * @author @stravanni
 */

public class TokenShingling extends AbstractModel {

    private double noOfTotalTerms;
    private final HashMap<String, Integer> itemsFrequency;
    private boolean[] shingles;
    private HashSet<Integer> shingles_set;
    private int shingles_size;
    private final LinkedHashSet<String> tokens;
    private final HashMap<String, Integer> attributeInstanceFrequency;

    private int full;

    public TokenShingling(int n, RepresentationModel model, String iName) {
        super(n, model, iName);

        full = 0;
        shingles = null;
        shingles_set = new HashSet<>();
        //shingles_attribute_size = 0;

        attributeInstanceFrequency = new HashMap<>();

        noOfTotalTerms = 0;
        itemsFrequency = new HashMap<String, Integer>();
        tokens = new LinkedHashSet<>();

        shingles = null;
    }


    @Override
    public LinkedHashSet<String> getFrequencies() {
        return tokens;
    }

    public boolean[] getShingles() {
        boolean[] shingles_list = new boolean[shingles_size];
        for (int i : shingles_set) {
            shingles_list[i] = true;
        }
        return shingles_list;
    }

    public HashSet<Integer> getShinglesSet() {
        return shingles_set;
    }

    public double getNoOfTotalTerms() {
        return noOfTotalTerms;
    }

    @Override
    public void updateModel(String text) {
        noOfDocuments++;

        String[] tokens = gr.demokritos.iit.jinsect.utils.splitToWords(text);

//        Integer freq_instance = attributeInstanceFrequency.getOrDefault(attribute_instance, 0);
//        attributeInstanceFrequency.put(attribute_instance, ++freq_instance);

        int noOfTokens = tokens.length;
        noOfTotalTerms += noOfTokens;
        final HashSet<String> features = new HashSet<String>();
//        for (int j = 0; j < noOfTokens - nSize; j++) {
//            final StringBuilder sb = new StringBuilder();
//            for (int k = 0; k < nSize; k++) {
//                sb.append(tokens[j + k]).append(" ");
//            }
//            String feature = sb.toString().trim();
//            features.add(feature);
//            itemsFrequency.put(feature, true);
//            this.tokens.add(feature);
//        }
        for (int j = 0; j < noOfTokens; j++) {
            String feature = tokens[j].trim();
            features.add(feature);
            itemsFrequency.put(feature, (itemsFrequency.getOrDefault(feature, 0) + 1));
            this.tokens.add(feature);
        }
    }

    @Override
    public int updateModel(String text, HashMap<String, Integer> all_tokens) {
        noOfDocuments++;

        String[] tokens = gr.demokritos.iit.jinsect.utils.splitToWords(text);

        int noOfTokens = tokens.length;
        noOfTotalTerms += noOfTokens;

        for (int j = 0; j < noOfTokens; j++) {
            String feature = tokens[j].trim();
            itemsFrequency.put(feature, (itemsFrequency.getOrDefault(feature, 0) + 1));
            this.tokens.add(feature);
            shingles_set.add(all_tokens.get(feature));
        }

        return 1;
    }

    public int updateModelApproximation(String text, HashMap<String, Integer> all_tokens) {
        noOfDocuments++;

        String[] tokens = gr.demokritos.iit.jinsect.utils.splitToWords(text);

        int noOfTokens = tokens.length;
        noOfTotalTerms += noOfTokens;

        for (int j = 0; j < noOfTokens; j++) {
            String feature = tokens[j].trim();
            itemsFrequency.put(feature, (itemsFrequency.getOrDefault(feature, 0) + 1));
            shingles_set.add(all_tokens.get(feature));
        }

        return 1;
    }

    @Override
    public double getEntropyToken(boolean normalized) {
        double entropy = 0.0;
        double len = noOfTotalTerms;
        for (Entry<String, Integer> entry : itemsFrequency.entrySet()) {
            double p_i = (entry.getValue() / len);
            entropy -= (p_i * (Math.log10(p_i) / Math.log10(2.0d)));
        }
        if (normalized) {
            return entropy / getMaxEntropy(len);
        } else {
            return entropy;
        }
    }

    public int[] getMinHash(MinHash minhash) {
        int[] minhashes = minhash.signature(shingles);
        return minhashes;
    }

    //@Override
    public double getSimilarity_real(AbstractModel oModel) {
        final TokenShingling otherModel = (TokenShingling) oModel;
        final LinkedHashSet<String> oItemVector = otherModel.getFrequencies();

        LinkedHashSet<String> intersection = new LinkedHashSet<>(this.tokens);
        intersection.retainAll(oItemVector);
        LinkedHashSet<String> union = new LinkedHashSet<>(this.tokens);
        union.addAll(oItemVector);

        return (double) intersection.size() / (double) union.size();
    }

    private double getIntersection(AbstractModel oModel) {
        final TokenShingling otherModel = (TokenShingling) oModel;
        final LinkedHashSet<String> oItemVector = otherModel.getFrequencies();

        LinkedHashSet<String> intersection = new LinkedHashSet<>(this.tokens);
        intersection.retainAll(oItemVector);
        LinkedHashSet<String> union = new LinkedHashSet<>(this.tokens);
        union.addAll(oItemVector);

        return (double) intersection.size();
    }

    @Override
    public double getSimilarity(AbstractModel oModel) {
        boolean[] shingles_list = this.getShingles();
        TokenShingling otherModel = (TokenShingling) oModel;
        boolean[] oItemVector = otherModel.getShingles();
        int intersection = 0;

        for (int i = 0; i < shingles_list.length; i++) {
            if (shingles_list[i] && oItemVector[i]) {
                intersection++;
            }
        }
        double sim = (double) intersection / (double) (tokens.size() + ((TokenShingling) oModel).getFrequencies().size() - intersection);
        return sim;
    }

    private double getMaxEntropy(double N) {
        double entropy = Math.log10(N) / Math.log10(2.0d);
        return entropy;
    }
}