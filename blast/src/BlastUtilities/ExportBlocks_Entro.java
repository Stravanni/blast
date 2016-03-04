package BlastUtilities;

import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.UnilateralBlock;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import BlastUtilities.BlastConstants;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author stravanni
 */
public class ExportBlocks_Entro extends Utilities.ExportBlocks implements BlastConstants {


    private IndexReader[] iReaders;

    public ExportBlocks_Entro(String[] paths) {
        this(getDirectories(paths));
    }

    public ExportBlocks_Entro(Directory[] dirs) {
        super(dirs);
    }


    protected Map<String, int[]> parseD1Index(IndexReader d1Index, IndexReader d2Index) {
        try {
            int[] documentIds = BlockBuilding.Utilities.getDocumentIds(d1Index);
            final Map<String, int[]> hashedBlocks = new HashMap<>();
            Fields fields = MultiFields.getFields(d1Index);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator(null);
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    // check whether it is a common term
                    int d2DocFrequency = d2Index.docFreq(new Term(field, text));
                    if (d2DocFrequency == 0) {
                        continue;
                    }

                    final List<Integer> entityIds = new ArrayList<>();
                    DocsEnum de = MultiFields.getTermDocsEnum(d1Index, MultiFields.getLiveDocs(d1Index), field, text);
                    int doc;
                    while ((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
                        entityIds.add(documentIds[doc]);
                    }

                    int[] idsArray = Utilities.Converter.convertCollectionToArray(entityIds);
                    hashedBlocks.put(text.utf8ToString(), idsArray);
                }
            }
            return hashedBlocks;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected void parseD2Index(IndexReader d2Index, Map<String, int[]> hashedBlocks) {
        try {
            int[] documentIds = BlockBuilding.Utilities.getDocumentIds(d2Index);
            Fields fields = MultiFields.getFields(d2Index);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator(null);
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    if (!hashedBlocks.containsKey(text.utf8ToString())) {
                        continue;
                    }

                    final List<Integer> entityIds = new ArrayList<>();
                    DocsEnum de = MultiFields.getTermDocsEnum(d2Index, MultiFields.getLiveDocs(d2Index), field, text);
                    int doc;
                    while ((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
                        entityIds.add(documentIds[doc]);
                    }

                    int[] idsArray = Utilities.Converter.convertCollectionToArray(entityIds);
                    int[] d1Entities = hashedBlocks.get(text.utf8ToString());
                    //blocks.add(new BilateralBlock(d1Entities, idsArray));
                    double entropy = 0;
                    String[] entropy_string = text.utf8ToString().split(ATTRIBUTE_CLUSTER_SUFFIX);
                    if (entropy_string.length == 2) {
                        entropy = Double.parseDouble(entropy_string[1]);
//                        if (entropy < 13) {
//                            System.out.println("exportblock: entropy " + entropy);
//                        }
                    }
                    //double entropy = 0;
                    //System.out.println(d2Index.document(doc).get(text.utf8ToString()).toString());
                    //System.out.println(entropy);
                    //System.out.println("entropy letta dall'indice dei blocchi: " + entropy);
                    this.getBlocks().add(new BilateralBlock_Entro(d1Entities, idsArray, entropy)); //BilateralBlock(int[] entities1, int[] entities2)
                    //blocks.add(new BilateralBlock(d1Entities, idsArray));
                }
            }
            System.out.println("fine di parsed2index");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

//    protected void parseIndex(IndexReader d1Index) {
//        try {
//            System.out.println("parse 1 index");
//            int[] documentIds = BlockBuilding.Utilities.getDocumentIds(d1Index);
//            Fields fields = MultiFields.getFields(d1Index);
//            for (String field : fields) {
//                Terms terms = fields.terms(field);
//                TermsEnum termsEnum = terms.iterator(null);
//                BytesRef text;
//                while ((text = termsEnum.next()) != null) {
//                    if (termsEnum.docFreq() < 2) {
//                        continue;
//                    }
//
//                    final List<Integer> entityIds = new ArrayList<>();
//                    DocsEnum de = MultiFields.getTermDocsEnum(d1Index, MultiFields.getLiveDocs(d1Index), field, text);
//                    int doc;
//                    while ((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
//                        entityIds.add(documentIds[doc]);
//                    }
//
//                    int[] idsArray = Utilities.Converter.convertCollectionToArray(entityIds);
//                    UnilateralBlock block = new UnilateralBlock(idsArray);
//                    this.getBlocks().add(block);
//                }
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }

//    public void storeBlocks(String outputPath) {
//        System.out.println("\n\nStoring blocks...");
//        Utilities.SerializationUtilities.storeSerializedObject(this.getBlocks(), outputPath);
//        System.out.println("Blocks were stored!");
//    }
//
//    public static void main(String[] args) throws IOException {
//        String mainDirectory = "/opt/data/frameworkData/";
//        String blocksPath = mainDirectory + "blocks/movies/tokenUnigramsBlocking";
//        String[] indexDirs = {mainDirectory + "indices/movies/tokenUnigramsDBP", mainDirectory + "indices/movies/tokenUnigramsIMDB"};
//        Utilities.ExportBlocks expbl = new Utilities.ExportBlocks(indexDirs);
//        expbl.storeBlocks(blocksPath);
//    }
}
