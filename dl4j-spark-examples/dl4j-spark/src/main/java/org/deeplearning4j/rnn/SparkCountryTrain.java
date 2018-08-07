package org.deeplearning4j.rnn;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer;
import org.deeplearning4j.spark.impl.paramavg.ParameterAveragingTrainingMaster;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;

import java.io.File;
import java.util.ArrayList;

public class SparkCountryTrain {

    private static Logger log = LoggerFactory.getLogger(SparkCountryTrain.class);

    public static String DATA_PATH = "";
    public static String WORD_VECTORS_PATH = "";
    public static WordVectors wordVectors;
    private static TokenizerFactory tokenizerFactory;

    public static void main(String[] args) throws Exception {
        String rootDir = "/media/sf_vmshare/aff-w2v-td/";
        DATA_PATH = rootDir;
        WORD_VECTORS_PATH = "/media/sf_vmshare/aff-w2v-full.model";

        int batchSize = 32;     //Number of examples in each minibatch
        int nEpochs = 10;        //Number of epochs (full passes of training data) to train on
        int truncateReviewsToLength = 25;  //Truncate reviews with length (# words) greater than this

        //DataSetIterators for training and testing respectively
        //Using AsyncDataSetIterator to do data loading in a separate thread; this may improve performance vs. waiting for data to load
        log.info("Loading w2v");
        wordVectors = WordVectorSerializer.readWord2VecModel(new File(WORD_VECTORS_PATH));
        log.info("Done loading w2v");

        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        CountryIterator iTrain = new CountryIterator.Builder()
            .dataDirectory(DATA_PATH)
            .wordVectors(wordVectors)
            .batchSize(batchSize)
            .truncateLength(truncateReviewsToLength)
            .tokenizerFactory(tokenizerFactory)
            .train(true)
            .build();

        CountryIterator iTest = new CountryIterator.Builder()
            .dataDirectory(DATA_PATH)
            .wordVectors(wordVectors)
            .batchSize(batchSize)
            .tokenizerFactory(tokenizerFactory)
            .truncateLength(truncateReviewsToLength)
            .train(false)
            .build();

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
            .updater(new RmsProp(0.0005))
            .l2(1e-5)
            .weightInit(WeightInit.XAVIER)
            .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
            .list()
            .layer(0, new LSTM.Builder().nIn(inputNeurons).nOut(lstmLayerSize)
                .activation(Activation.RELU).build())
            .layer(1, new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(lstmLayerSize).nOut(outputs).build())
            .pretrain(false).backprop(true).build();

//        MultiLayerNetwork net = new MultiLayerNetwork(conf);
//        net.init();
//        net.setListeners(new ScoreIterationListener(10));

        SparkConf sparkConf = new SparkConf();
        //if (useSparkLocal) {
        sparkConf.setMaster("local[*]");
        //}
        sparkConf.setAppName("LSTM Character Example");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        //Set up the TrainingMaster. The TrainingMaster controls how learning is actually executed on Spark
        //Here, we are using standard parameter averaging
        //For details on these configuration options, see: https://deeplearning4j.org/spark#configuring
        int examplesPerDataSetObject = 1;
        int averagingFrequency = 3;
        int batchSizePerWorker = 8;
        ParameterAveragingTrainingMaster tm = new ParameterAveragingTrainingMaster.Builder(examplesPerDataSetObject)
            .workerPrefetchNumBatches(2)    //Asynchronously prefetch up to 2 batches
            .averagingFrequency(averagingFrequency)
            .batchSizePerWorker(batchSizePerWorker)
            .build();
        SparkDl4jMultiLayer sparkNetwork = new SparkDl4jMultiLayer(sc, conf, tm);
        sparkNetwork.setListeners(new ScoreIterationListener(10));


        System.out.println("Starting training");
        String modelPath = rootDir + "country.model";
        int dsBatch = 128;
        for (int i = 0; i < nEpochs; i++) {
            while (iTrain.hasNext()) {
                int di = 0;
                ArrayList<DataSet> dsb = new ArrayList<>(dsBatch);
                while (di < dsBatch && iTrain.hasNext()) {
                    dsb.add(iTrain.next());
                    di ++;
                }
                sparkNetwork.fit(sc.parallelize(dsb));
            }


            iTrain.reset();
            System.out.println("Epoch " + i + " complete. Starting evaluation:");

//            //Run evaluation. This is on 25k reviews, so can take some time
//            Evaluation evaluation = net.evaluate(iTest);
//            System.out.println(evaluation.stats());

            ModelSerializer.writeModel(sparkNetwork.getNetwork(), modelPath, true);
        }

        System.out.println("----- Example complete -----");
    }

}
