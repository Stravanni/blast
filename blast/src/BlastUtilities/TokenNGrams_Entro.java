package BlastUtilities;


import Utilities.RepresentationModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * @author stravanni
 */

public class TokenNGrams_Entro extends AbstractModel {

    private double noOfTotalTerms;
    private final HashMap<String, Integer> documentFrequency;
    private final HashMap<String, Integer> itemsFrequency;
    private final HashMap<String, Integer> attributeInstanceFrequency;

    public TokenNGrams_Entro(int n, BlastRepresentationModel model, String iName) {
        super(n, model, iName);

        noOfTotalTerms = 0;
        documentFrequency = new HashMap<String, Integer>();
        itemsFrequency = new HashMap<String, Integer>();

        attributeInstanceFrequency = new HashMap<>();
    }


    public HashMap<String, Integer> getDocumentFrequency() {
        return documentFrequency;
    }

    public HashMap<String, Integer> getItemsFrequency() {
        return itemsFrequency;
    }

    public double getNoOfTotalTerms() {
        return noOfTotalTerms;
    }

    @Override
    public void updateModel(String text) {
        noOfDocuments++;

        String attribute_instance = text.toLowerCase().trim();
        Integer freq_instance = attributeInstanceFrequency.getOrDefault(attribute_instance, 0);
        attributeInstanceFrequency.put(attribute_instance, ++freq_instance);

        String[] tokens = gr.demokritos.iit.jinsect.utils.splitToWords(text);

        int noOfTokens = tokens.length;
        noOfTotalTerms += noOfTokens;
        final HashSet<String> features = new HashSet<String>();
        for (int j = 0; j < noOfTokens - nSize; j++) {
            final StringBuilder sb = new StringBuilder();
            for (int k = 0; k < nSize; k++) {
                sb.append(tokens[j + k]).append(" ");
            }
            String feature = sb.toString().trim();
            features.add(feature);

            Integer frequency = itemsFrequency.get(feature);
            if (frequency == null) {
                frequency = new Integer(0);
            }
            frequency++;
            itemsFrequency.put(feature, frequency);
        }

        for (String item : features) {
            Integer frequency = documentFrequency.get(item);
            if (frequency == null) {
                frequency = new Integer(0);
            }
            frequency++;
            documentFrequency.put(item, frequency);
        }
    }

//    @Override
//    public double getSimilarity(AbstractModel oModel) {
//        final TokenNGrams_Entro otherModel = (TokenNGrams_Entro) oModel;
//        final HashMap<String, Integer> oItemVector = otherModel.getItemsFrequency();
//
//        double numerator = 0.0;
//        for (Entry<String, Integer> entry : itemsFrequency.entrySet()) {
//            Integer frequency2 = oItemVector.get(entry.getKey());
//            if (frequency2 != null) {
//                double inverseDocFreq1 = Math.log(this.getNoOfDocuments() / (1.0 + this.getDocumentFrequency().get(entry.getKey())));
//                double inverseDocFreq2 = Math.log(otherModel.getNoOfDocuments() / (1.0 + otherModel.getDocumentFrequency().get(entry.getKey())));
//                numerator += ((double) entry.getValue() / this.getNoOfTotalTerms()) * ((double) frequency2 / otherModel.getNoOfTotalTerms()) * inverseDocFreq1 * inverseDocFreq2;
//            }
//        }
//
//        double denominator = getVectorMagnitude(this) * getVectorMagnitude(otherModel);
//        return numerator / denominator;
//    }


    public double getSimilarity(AbstractModel oModel) {
        final TokenNGrams_Entro otherModel = (TokenNGrams_Entro) oModel;
        final HashMap<String, Integer> oItemVector = otherModel.getItemsFrequency();

        double numerator = 0.0;
        for (Entry<String, Integer> entry : itemsFrequency.entrySet()) {
            Integer frequency2 = oItemVector.get(entry.getKey());
            if (frequency2 != null) {
                double inverseDocFreq1 = Math.log(this.getNoOfDocuments() / (1.0 + this.getDocumentFrequency().get(entry.getKey())));
                double inverseDocFreq2 = Math.log(otherModel.getNoOfDocuments() / (1.0 + otherModel.getDocumentFrequency().get(entry.getKey())));
                numerator += ((double) entry.getValue() / this.getNoOfTotalTerms()) * ((double) frequency2 / otherModel.getNoOfTotalTerms()) * inverseDocFreq1 * inverseDocFreq2;
            }
        }

        double denominator = getVectorMagnitude(this) * getVectorMagnitude(otherModel);
        return numerator / denominator;
    }

    @Override
    public double getEntropyInstamnce(boolean normalized) {
        double entropy = 0.0;
        double len = noOfDocuments;
        for (Entry<String, Integer> entry : attributeInstanceFrequency.entrySet()) {
            double p_i = (entry.getValue() / len);
            entropy -= (p_i * (Math.log10(p_i) / Math.log10(2.0d)));
        }
        if (normalized) {
            return entropy / getMaxEntropy(len);
        } else {
            return entropy;
        }
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

    private double getVectorMagnitude(TokenNGrams_Entro model) {
        double magnitude = 0.0;
        for (Entry<String, Integer> entry : model.getItemsFrequency().entrySet()) {
            double inverseDocFreq = Math.log(model.getNoOfDocuments() / (1.0 + model.getDocumentFrequency().get(entry.getKey())));
            double weight = ((double) entry.getValue()) / model.getNoOfTotalTerms() * inverseDocFreq;
            magnitude += Math.pow(weight, 2.0);
        }

        return Math.sqrt(magnitude);
    }

    private double getMaxEntropy(double N) {
        double entropy = Math.log10(N) / Math.log10(2.0d);
        return entropy;
    }
}