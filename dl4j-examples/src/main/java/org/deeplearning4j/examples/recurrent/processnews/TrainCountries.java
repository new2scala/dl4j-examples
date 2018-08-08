package org.deeplearning4j.examples.recurrent.processnews;

import org.apache.commons.io.IOUtils;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.examples.nlp.word2vec.W2VAffsFull;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainCountries {

    private static Logger log = LoggerFactory.getLogger(TrainCountries.class);

    public static String DATA_PATH = "";
    public static String WORD_VECTORS_PATH = "";
    public static WordVectors wordVectors;
    private static TokenizerFactory tokenizerFactory;

    public static void main(String[] args) throws Exception {
        String rootDir = "/media/sf_vmshare/aff-w2v-td/";
        DATA_PATH = rootDir;
        WORD_VECTORS_PATH = "/media/sf_vmshare/aff-w2v-full.model";

        int batchSize = 128;     //Number of examples in each minibatch
        int nEpochs = 10;        //Number of epochs (full passes of training data) to train on
        int truncateReviewsToLength = 20;  //Truncate reviews with length (# words) greater than this

        //DataSetIterators for training and testing respectively
        //Using AsyncDataSetIterator to do data loading in a separate thread; this may improve performance vs. waiting for data to load
        log.info("Loading w2v");
        wordVectors = WordVectorSerializer.readWord2VecModel(new File(WORD_VECTORS_PATH));
        log.info("Done loading w2v");

        loadCategoryMap("/media/sf_vmshare/aff-w2v-td/categories.txt");

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        CountryIterator iTrain = new CountryIterator.Builder()
            .dataDirectory(DATA_PATH)
            .wordVectors(wordVectors)
            .batchSize(batchSize)
            .truncateLength(truncateReviewsToLength)
            .tokenizerFactory(tokenizerFactory)
            .train(true)
            .build();

//        CountryIterator iTest = new CountryIterator.Builder()
//            .dataDirectory(DATA_PATH)
//            .wordVectors(wordVectors)
//            .batchSize(batchSize)
//            .tokenizerFactory(tokenizerFactory)
//            .truncateLength(truncateReviewsToLength)
//            .train(false)
//            .build();

        //DataSetIterator train = new AsyncDataSetIterator(iTrain,1);
        //DataSetIterator test = new AsyncDataSetIterator(iTest,1);

        int inputNeurons = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length; // 100 in our case
        System.out.println("Input (w2v) size: " + inputNeurons);
        int outputs = iTrain.getLabels().size();

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        //Set up network configuration
        int lstmLayerSize = 512;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .updater(new RmsProp(0.0001))
            .l2(1e-5)
            .weightInit(WeightInit.XAVIER)
            .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
            .list()
            .layer(0, new LSTM.Builder().nIn(inputNeurons).nOut(lstmLayerSize)
                .activation(Activation.SOFTSIGN).build())
//            .layer(1, new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
//                .activation(Activation.SOFTSIGN).build())
            .layer(1, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(lstmLayerSize).nOut(outputs).build())
            .pretrain(false)
            .backprop(true)
            .backpropType(BackpropType.TruncatedBPTT)
            .tBPTTBackwardLength(6)
            .tBPTTForwardLength(6)
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(10));

        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();
        uiServer.attach(statsStorage);
        net.setListeners(new ScoreIterationListener(10), new StatsListener(statsStorage));


        System.out.println("Starting training");
        String modelPath = rootDir + "country_wui.model";
        for (int i = 0; i < nEpochs; i++) {
            net.fit(iTrain);
            iTrain.reset();
            System.out.println("Epoch " + i + " complete. Starting evaluation:");

//            //Run evaluation. This is on 25k reviews, so can take some time
//            Evaluation evaluation = net.evaluate(iTest);
//            System.out.println(evaluation.stats());

            ModelSerializer.writeModel(net, modelPath, true);

            runTests(
                net,
                new String[]{
                    "1 virginia commonwealth university school of nursing richmond virginia",
                    "102nd hospital of chinese pla",
                    "1 gene experiment center institute of applied biochemistry university of tsukuba tsukuba-city 305",
                    "1 direktion pflege und mttb universitätsspital zürich",
                    "11 education centre freeman hospital newcastle upon tyne",
                    "1 best practice advocacy centre new zealand dunedin",
                    "**division of gastroenterology and hepatology medical university of vienna vienna",
                    "1 department of psychology princeton university"
                },
                new String[]{
                    "United States",
                    "China",
                    "Japan",
                    "Switzerland",
                    "United Kingdom",
                    "New Zealand",
                    "Austria",
                    "United States"
                }
            );
        }

        System.out.println("----- Example complete -----");
    }

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

    private static void runTests(MultiLayerNetwork model, String[] inputs, String[] expCountries) {
        for (int i = 0; i < inputs.length; i ++) {
            String input = inputs[i];
            String expCountry = expCountries[i];
            int expCat = revCategoryMap.get(expCountry);
            DataSet ds = prepareTestData(input);
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