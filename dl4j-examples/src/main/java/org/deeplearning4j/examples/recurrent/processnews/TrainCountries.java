package org.deeplearning4j.examples.recurrent.processnews;

import org.apache.commons.io.IOUtils;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.examples.nlp.word2vec.W2VAffsFull;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.api.Layer;
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
import org.joda.time.DateTime;
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
import java.util.*;

public class TrainCountries {

    private static Logger log = LoggerFactory.getLogger(TrainCountries.class);

    public static String DATA_PATH = "";
    public static String WORD_VECTORS_PATH = "";
    public static WordVectors wordVectors;
    private static TokenizerFactory tokenizerFactory;



    public static void main(String[] args) throws Exception {
        //String workingDir = "Y:\\vmshare\\";
        String projDir = "Y:\\vmshare\\fp2Affs-full\\us-au-gb-ca\\";
        DATA_PATH = projDir;
        WORD_VECTORS_PATH = "Y:\\vmshare\\fp2Affs-full\\w2v-full.model.1";
        //String modelPath = rootDir + "country-tr-2layer.model";
        String modelPath = projDir + "country-acgu.model";

        int batchSize = 128;     //Number of examples in each minibatch
        int nEpochs = 1000;        //Number of epochs (full passes of training data) to train on
        int truncateReviewsToLength = 10;  //Truncate reviews with length (# words) greater than this

        //DataSetIterators for training and testing respectivelyCreating modelove performance vs. waiting for data to load
        log.info("Loading w2v");
        wordVectors = WordVectorSerializer.readWord2VecModel(new File(WORD_VECTORS_PATH));
        log.info("Done loading w2v");

        loadCategoryMap(projDir + "categories.txt");

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

//        CountryIterator iTrain = new CountryIterator.Builder()
//            .dataDirectory(DATA_PATH)
//            .wordVectors(wordVectors)
//            .batchSize(batchSize)
//            .truncateLength(truncateReviewsToLength)
//            .tokenizerFactory(tokenizerFactory)
//            .train(true)
//            .build();
//
//        CountryIterator iTest = new CountryIterator.Builder()
//            .dataDirectory(DATA_PATH)
//            .wordVectors(wordVectors)
//            .batchSize(batchSize)
//            .tokenizerFactory(tokenizerFactory)
//            .truncateLength(truncateReviewsToLength)
//            .train(false)
//            .build();

        int trainSkip = 100;
        Map<Integer, List<String>> mandData = new HashMap<>();
        mandData.put(
            4, Arrays.asList(
                "hospital of chinese pla",
                "chinese pla",
                "new zealand dunedin",
                "dunedin new zealand",
                "cape town",
                "tsukuba tsukuba-city",
                "institute of molecular medicine university of brescia brescia",
                "mie university",
                "szpitala wojewódzkiego im sw łukasza w tarnowie",
                "zakainy samodzielnego publicznego zakladu opieki zdrowotnej w hajnówce",
                "university of turin turin",
                "university of torino torino",
                "'t olderloug te slochteren",
                "'s heeren loo-lozenoord ermelo",
                "'s koonings jaght arnhem",
                "medical microbiology rikshospitalet medical center and university of oslo",
                "university of oslo",
                "bioinformatics line / biology department / universidad de caldas",
                "instytut zdrowia publicznego zakład ekonomiki zdrowia i zabezpieczenia społecznego"
            )
        );
        mandData.put(
            0, Arrays.asList(
                "whitehorse division of general practice victoria"
            )
        );
        mandData.put(
            1, Arrays.asList(
                "of gynaecologic oncology university of western ontario london on",
                "western university libraries london on"
            )
        );
        mandData.put(
            2, Arrays.asList(
                "the lancet london england",
                "of public health st mary's campus london w2 [[daa]]",
                "research & dental public health gkt dental institute london",
                "london school of hygiene and tropical medicine",
                "london school of economics",
                "guy's and st thomas' nhs trust london england",
                "matrix chambers london",
                "royal hospital for neuro-disability london",
                "university college london",
                "[[d4]] university of london neurosurgery [[d4]]",
                "saint jeorge medical school university of london london",
                "t8 university college london hospitals london",
                "bmas london",
                "cell biology university college london london",
                "centre for nanotechnology university college london [[aada]] [[daa]] england",
                "college london hospital nhs foundation trust mortimer market london",
                "department of radiology imaging university college hospital london",
                "health at king's college london school of medicine london",
                "healthcare nhs trust 57 waterloo road london [[aad]] [[daa]]",
                "institute of cardiovascular science university college london london england",
                "north west thames deanery london",
                "london school of economics and political science london",
                "king's college hospital nhs foundation trust hill london",
                "maxillofacial unit/oncology king's college hospital hill london",
                "king's college hospital hill london [[aad]] [[daa]]",
                "king's college hospital hill london",
                "caldecot road hill london england [[aad]] [[daa]]",
                "prism institute of psychiatry hill london england",
                "and gynecology king's college hospital hill london [[aad]] [[daa]]",
                "of neurosciences king's college hospital hill london [[aad]] [[daa]]",
                "department of otolaryngology kings college hospital hill london",
                "king's college london dental institute hill london [[aad]] [[daa]]",
                "traumatic stress service maudsley hospital hill london [[aad]] [[daa]]",
                "king's college school of medicine & dentistry hill london",
                "dental public health king's college hospital hill london",
                "college london dental institute hill london [[aad]] [[daa]] ukb",
                "the king's headache service king's college hospital hill london",
                "institute of liver studies kings college hospital hill london",
                "national addiction centre hill london england",
                "maxillofacial surgery king's college hospital hill london [[aad]] [[daa]]",
                "department of radiology king's college hospital hill london",
                "department of urogynaecology king's college hospital hill london",
                "windsor walk hill london [[aad]] [[daa]]",
                "institute of psychiatry hill london se58af england",
                "college london de crespigny park hill london [[aad]] [[daa]]",
                "k.s jacob institute of psychiatry hill london [[aad]] [[daa]]",
                "northern imperial college and royal brompton hospital london england",
                "imperial college london"
            )
        );
        CountryIteratorSkip iTrain = new CountryIteratorSkip.Builder()
            .dataDirectory(DATA_PATH)
            .wordVectors(wordVectors)
            .batchSize(batchSize)
            .truncateLength(truncateReviewsToLength)
            .tokenizerFactory(tokenizerFactory)
            .train(true)
            .mandatoryData(mandData)
            .useMandatoryDataOnly(false)
            .skip(trainSkip)
            .build();

        int testSkip = 20;
        CountryIteratorSkip iTest = new CountryIteratorSkip.Builder()
            .dataDirectory(DATA_PATH)
            .wordVectors(wordVectors)
            .batchSize(batchSize)
            .tokenizerFactory(tokenizerFactory)
            .truncateLength(truncateReviewsToLength)
            .train(true) // use the same data
            .skip(testSkip)
            .build();

        //DataSetIterator train = new AsyncDataSetIterator(iTrain,1);
        //DataSetIterator test = new AsyncDataSetIterator(iTest,1);

        int inputNeurons = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
        System.out.println("Input (w2v) size: " + inputNeurons);
        int outputs = iTrain.getLabels().size();

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        MultiLayerNetwork net;
        if (new File(modelPath).exists()) {
            System.out.println("+++++++++++++ Restoring model");
            net = ModelSerializer.restoreMultiLayerNetwork(modelPath);
            net.setLearningRate(0.0002);
            //log.info("model restored learning rate: {}", net.getDefaultConfiguration());
//            runTests(
//                net,
//                inputs,
//                expResults
//            );
//            System.out.println("Evaluating existing model ...");
//            evaluateTests(net, iTest);

        }
        else {
            System.out.println("------------- Creating model");
            //Set up network configuration
            int lstmLayerSize = 128;
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .updater(
                    new RmsProp(0.005)
                    //new Nesterovs(0.00001,0.01)
                )
                .l2(1e-5)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
                .list()
                .layer(0, new LSTM.Builder().nIn(inputNeurons).nOut(lstmLayerSize)
                    .activation(Activation.TANH).build())
                .layer(1, new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                    .activation(Activation.TANH).build())
                .layer(2, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                    .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(lstmLayerSize).nOut(outputs).build())
                .pretrain(false)
                .backprop(true)
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTBackwardLength(10)
                .tBPTTForwardLength(10)
                .build();

            net = new MultiLayerNetwork(conf);
            net.init();

            //Print the  number of parameters in the network (and for each layer)
            Layer[] layers = net.getLayers();
            int totalNumParams = 0;
            for( int i=0; i<layers.length; i++ ){
                int nParams = layers[i].numParams();
                System.out.println("Number of parameters in layer " + i + ": " + nParams);
                totalNumParams += nParams;
            }
            System.out.println("Total number of network parameters: " + totalNumParams);
        }

        net.setListeners(new ScoreIterationListener(10));

        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();
        uiServer.attach(statsStorage);
        net.setListeners(new ScoreIterationListener(10), new StatsListener(statsStorage));


        System.out.println("Starting training");

        for (int i = 0; i < nEpochs; i++) {
            iTrain.shuffle();
            net.fit(iTrain);
            iTrain.reset();
            System.out.println("Epoch " + i + " complete. Starting evaluation:");

//            //Run evaluation. This is on 25k reviews, so can take some time
            ModelSerializer.writeModel(net, modelPath, true);

            int runTestPer = 10;
            if (i % runTestPer == runTestPer-1) {
                runTests(
                    net,
                    inputs,
                    expResults
                );
            }
            int evaluationPer = 10;
            if (i % evaluationPer == evaluationPer-1) {
                evaluateTests(net, iTest);
            }

        }

        System.out.println("----- Example complete -----");
    }

    private static void evaluateTests(MultiLayerNetwork net, CountryIteratorSkip iTest) throws Exception {
        long start = DateTime.now().getMillis();
        iTest.reset();
        iTest.shuffle();
        Evaluation evaluation = net.evaluate(iTest);
        System.out.println(evaluation.stats(false, true));
        long duration = DateTime.now().getMillis() - start;
        System.out.println(String.format("Evaluation time: %.2f", duration / 1000.0));
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

    private final static String[] inputs = new String[]{
        "observatory cape town",
        "and dentistry of new jersey new brunswick new jersey [[dgt5]]",
        "professor with the university of british columbia vancouver british columbia",
        "university of turku",
        "independent contractor williamsville ny",
        "spain and novo nordisk [[dk-dgt4]] bagsvaerd",
        "westmead hospital westmead nsw",
        "dentistry and pharmaceutical sciences okayama university",
        "rosedale mansions boulevard hull [[aad]] [[daa]]",
        "montana cancer consortium billings mt",
        "charing cross hospital london",
        "institut pasteur de la guyane cayenne cedex guyane",
        "virginia commonwealth university school of nursing richmond virginia",
        "102nd hospital of chinese pla",
        "gene experiment center institute of applied biochemistry university of tsukuba tsukuba-city 305",
        "direktion pflege und mttb universitätsspital zürich",
        "education centre freeman hospital newcastle upon tyne",
        "best practice advocacy centre new zealand dunedin",
        "**division of gastroenterology and hepatology medical university of vienna vienna",
        "department of psychology princeton university"
    };
    private final static String[] expResults = new String[]{
//        "South Africa",
//        "United States",
//        "Canada",
//        "Finland",
//        "United States",
//        "Denmark",
//        "Australia",
//        "Japan",
//        "United Kingdom",
//        "United States",
//        "United Kingdom",
//        "France",
//        "United States",
//        "China",
//        "Japan",
//        "Switzerland",
//        "United Kingdom",
//        "New Zealand",
//        "Austria",
//        "United States"
        "Other",
        "United States",
        "Canada",
        "Other",
        "United States",
        "Other",
        "Australia",
        "Other",
        "United Kingdom",
        "United States",
        "United Kingdom",
        "Other",
        "United States",
        "Other",
        "Other",
        "Other",
        "United Kingdom",
        "Other",
        "Other",
        "United States"
    };

    private static void runTests(MultiLayerNetwork model, String[] inputs, String[] expCountries) {
        int trueCount = 0;
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

            String trueOrNot;
            if (resCat.equals(expCountry)) {
                trueCount ++;
                trueOrNot = "++";
            }
            else {
                trueOrNot = "--";
            }
            System.out.println(
                String.format("\t%s %s/%.4f(%s):\t%s", trueOrNot, resCat, max, expCountryVal, input)
            );

        }

        System.out.println(
            String.format("Total %%: %.3f", trueCount*100.0/expResults.length)
        );
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
