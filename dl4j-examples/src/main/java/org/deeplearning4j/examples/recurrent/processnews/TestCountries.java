package org.deeplearning4j.examples.recurrent.processnews;

import org.apache.commons.io.IOUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TestCountries {
    private static MultiLayerNetwork model;
    private static WordVectors wordVectors;
    private static TokenizerFactory tokenizerFactory;
    private static Map<Integer, String> categoryMap;
    private static Map<String, Integer> revCategoryMap;

    private static void loadCategoryMap(String categoryPath) throws Exception {
        categoryMap = new HashMap<>();
        revCategoryMap = new HashMap<>();

        List<String> lines = IOUtils.readLines(new FileInputStream(categoryPath), StandardCharsets.UTF_8);
        for (String l : lines) {
            String[] parts = l.split(",");
            int cat = Integer.parseInt(parts[0]);
            String countryName = parts[1];
            categoryMap.put(cat, countryName);
            revCategoryMap.put(countryName, cat);
        }
    }



    public static void main(String[] args) throws Exception {
        String workingDir = "Y:\\vmshare\\fp2Affs-w2v\\";
        String WORD_VECTORS_PATH = workingDir + "aff-full.model";
        String projDir = workingDir + "us233\\";

        loadCategoryMap(projDir + "categories.txt");
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        wordVectors = WordVectorSerializer.readWord2VecModel(new File(WORD_VECTORS_PATH));

        System.out.println("word2vec loaded");
        model = ModelSerializer.restoreMultiLayerNetwork(projDir + "country-cuda.model");

        System.out.println("model loaded");
        runTests(workingDir + "-1.txt", projDir + "-1-res.txt");

//        runTests(
//            new String[]{
//                "1 virginia commonwealth university school of nursing richmond virginia",
//                "102nd hospital of chinese pla",
//                "1 gene experiment center institute of applied biochemistry university of tsukuba tsukuba-city 305",
//                "1 direktion pflege und mttb universitätsspital zürich",
//                "11 education centre freeman hospital newcastle upon tyne",
//                "1 best practice advocacy centre new zealand dunedin",
//                "**division of gastroenterology and hepatology medical university of vienna vienna",
//                "1 department of psychology princeton university"
//            },
//            new String[]{
//                "United States",
//                "China",
//                "Japan",
//                "Switzerland",
//                "United Kingdom",
//                "New Zealand",
//                "Austria",
//                "United States"
//            }
//        );

    }

    private static void runTests(String fileName, String resultFile) throws Exception {

        List<String> lines = IOUtils.readLines(new FileInputStream(fileName), StandardCharsets.UTF_8);
        System.out.println(lines.size() + " lines loaded");
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            DataSet ds = prepareTestData(line);
            if (ds == null)
                continue;
            INDArray feats = ds.getFeatureMatrix();

            INDArray res = model.output(feats, false);
            int[] arrSize = res.shape();

            double max = Double.MIN_VALUE;
            int cat = -1;
            String expCountryVal = "N/A";
            for (int j = 0; j < arrSize[1]; j ++) {
                Double v = (Double)res.getColumn(j).sumNumber();
                if (max < v) {
                    max = v;
                    cat = j;
                }

            }
            String resCat = categoryMap.get(cat);
            String resTr = String.format("%s/%.4f(%s):\t%s", resCat, max, expCountryVal, line);
            System.out.println(resTr);
            result.add(resTr);
        }

        IOUtils.writeLines(result, "\n", new FileOutputStream(resultFile), StandardCharsets.UTF_8);
    }

    private static void runTests(String[] inputs, String[] expCountries) {
        for (int i = 0; i < inputs.length; i ++) {
            String input = inputs[i];
            String expCountry = expCountries[i];
            int expCat = revCategoryMap.get(expCountry);
            DataSet ds = prepareTestData(input);
            if (ds == null)
                continue;
            INDArray feats = ds.getFeatureMatrix();

            INDArray res = model.output(feats, false);
            int[] arrSize = res.shape();

            double max = Double.MIN_VALUE;
            int cat = -1;
            String expCountryVal = "N/A";
            for (int j = 0; j < arrSize[1]; j ++) {
                Double v = (Double)res.getColumn(j).sumNumber();
                if (max < v) {
                    max = v;
                    cat = j;
                }

                if (j == expCat)
                    expCountryVal = String.format("expected [%s/%.4f]", expCountry, v);
            }
            String resCat = categoryMap.get(cat);
            System.out.println(
                String.format("%s/%.4f(%s):\t%s", resCat, max, expCountryVal, input)
            );
        }
    }

    private static DataSet prepareTestData(String input) {
        List<String> news = new ArrayList<>(1);
        int[] category = new int[1];
        int currCategory = 0;
        news.add(input);

        List<List<String>> allTokens = new ArrayList<>(news.size());
        int maxLength = 0;
        for (String s : news) {
            List<String> tokens = tokenizerFactory.create(s).getTokens();
            List<String> tokensFiltered = new ArrayList<>();
            for (String t : tokens) {
                if (wordVectors.hasWord(t)) tokensFiltered.add(t);
            }
            allTokens.add(tokensFiltered);
            maxLength = Math.max(maxLength, tokensFiltered.size());
        }

        if (maxLength <= 0) {
            return null;
        }

        INDArray features = Nd4j.create(news.size(), wordVectors.lookupTable().layerSize(), maxLength);
        INDArray labels = Nd4j.create(news.size(), 4, maxLength);    //labels: Crime, Politics, Bollywood, Business&Development
        INDArray featuresMask = Nd4j.zeros(news.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(news.size(), maxLength);

        int[] temp = new int[2];
        for (int i = 0; i < news.size(); i++) {
            List<String> tokens = allTokens.get(i);
            temp[0] = i;
            for (int j = 0; j < tokens.size() && j < maxLength; j++) {
                String token = tokens.get(j);
                INDArray vector = wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[]{NDArrayIndex.point(i),
                        NDArrayIndex.all(),
                        NDArrayIndex.point(j)},
                    vector);

                temp[1] = j;
                featuresMask.putScalar(temp, 1.0);
            }
            int idx = category[i];
            int lastIdx = Math.min(tokens.size(), maxLength);
            labels.putScalar(new int[]{i, idx, lastIdx - 1}, 1.0);
            labelsMask.putScalar(new int[]{i, lastIdx - 1}, 1.0);
        }

        DataSet ds = new DataSet(features, labels, featuresMask, labelsMask);
        return ds;
    }

}
