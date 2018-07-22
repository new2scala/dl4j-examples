package org.deeplearning4j.examples.recurrent.seqclassification;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Ex2 {
    private static final Logger log = LoggerFactory.getLogger(UCISequenceClassificationExample.class);

    //'baseDir': Base directory for the data. Change this if you want to save the data somewhere else
    private static File baseDir = new File("/home/dev/tmp/lstm1/");
    private static File baseTrainDir = new File(baseDir, "train");
    private static File featuresDirTrain = new File(baseTrainDir, "mm");
    private static File labelsDirTrain = new File(baseTrainDir, "lb");
    private static File baseTestDir = new File(baseDir, "test");
    private static File featuresDirTest = new File(baseTestDir, "mm");
    private static File labelsDirTest = new File(baseTestDir, "lb");
    private static File baseT2Dir = new File(baseDir, "t2");
    private static File featuresDirT2 = new File(baseT2Dir, "mm");
    private static File labelsDirT2 = new File(baseT2Dir, "lb");

    public static void main(String[] args) throws Exception {
        String modelSavePath = "/home/dev/tmp/lstm1/model";
        //trainAndSaveModel(modelSavePath);
        loadModelAndTest(modelSavePath);
    }

    private static void loadModelAndTest(String modelSavePath) throws Exception {
        MultiLayerNetwork net = MultiLayerNetwork.load(new File(modelSavePath), false);

        int maxFileIndex = 19;
        SequenceRecordReader t2Features = new CSVSequenceRecordReader();
        t2Features.initialize(new NumberedFileInputSplit(
            featuresDirT2.getAbsolutePath() + "/" + csvFileNameFmt,
            0, maxFileIndex
        ));
        SequenceRecordReader t2Labels = new CSVSequenceRecordReader();
        t2Labels.initialize(new NumberedFileInputSplit(
            labelsDirT2.getAbsolutePath() + "/" + csvFileNameFmt,
            0, maxFileIndex
        ));

        DataSetIterator t2Data = new SequenceRecordReaderDataSetIterator(t2Features, t2Labels, miniBatchSize, numLabelClasses,
            false, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

        while (t2Data.hasNext()) {
            DataSet ds = t2Data.next();
            INDArray res = net.rnnTimeStep(ds.getFeatures());
            INDArray roundRes = Transforms.round(res);
            System.out.println(roundRes.shape());
        }
        //
    }
    private static final String csvFileNameFmt = "%04d.csv";
    private static final int miniBatchSize = 10;
    private static final int numLabelClasses = 9;


    private static void trainAndSaveModel(String modelSavePath) throws Exception {

        // ----- Load the training data -----
        //Note that we have 450 training files for features: train/features/0.csv through train/features/449.csv
        int maxFileIndex = 4499;
        SequenceRecordReader trainFeatures = new CSVSequenceRecordReader();
        trainFeatures.initialize(new NumberedFileInputSplit(
            featuresDirTrain.getAbsolutePath() + "/" + csvFileNameFmt,
            0, maxFileIndex
        ));
        SequenceRecordReader trainLabels = new CSVSequenceRecordReader();
        trainLabels.initialize(new NumberedFileInputSplit(
            labelsDirTrain.getAbsolutePath() + "/" + csvFileNameFmt,
            0, maxFileIndex
        ));


        DataSetIterator trainData = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels, miniBatchSize, numLabelClasses,
            false, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

//        DataSet dbg = ((SequenceRecordReaderDataSetIterator) trainData).next();
//        System.out.println(dbg);

        //Normalize the training data
        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(trainData);              //Collect training data statistics
        trainData.reset();

        //Use previously collected statistics to normalize on-the-fly. Each DataSet returned by 'trainData' iterator will be normalized
        trainData.setPreProcessor(normalizer);


        // ----- Load the test data -----
        //Same process as for the training data.
        int testFileMaxIndex = 1499;
        SequenceRecordReader testFeatures = new CSVSequenceRecordReader();
        testFeatures.initialize(new NumberedFileInputSplit(featuresDirTest.getAbsolutePath() + "/" + csvFileNameFmt,
            0, testFileMaxIndex
        ));
        SequenceRecordReader testLabels = new CSVSequenceRecordReader();
        testLabels.initialize(new NumberedFileInputSplit(labelsDirTest.getAbsolutePath() + "/" + csvFileNameFmt,
            0, testFileMaxIndex
        ));

        DataSetIterator testData = new SequenceRecordReaderDataSetIterator(testFeatures, testLabels, miniBatchSize, numLabelClasses,
            false, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

        testData.setPreProcessor(normalizer);   //Note that we are using the exact same normalization process as the training data


        int lstmLayerNodes = 50;
        // ----- Configure the network -----
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(123)    //Random number generator seed for improved repeatability. Optional.
            .weightInit(WeightInit.XAVIER)
            .updater(new Nesterovs(0.005))
//            .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)  //Not always required, but helps with this data set
//            .gradientNormalizationThreshold(0.5)
            .list()
            .layer(0, new LSTM.Builder().activation(Activation.TANH).nIn(4).nOut(lstmLayerNodes).build())
            .layer(1, new LSTM.Builder().activation(Activation.TANH).nIn(lstmLayerNodes).nOut(lstmLayerNodes).build())
            .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                .activation(Activation.SOFTMAX).nIn(lstmLayerNodes).nOut(numLabelClasses).build())
            .pretrain(false).backprop(true).build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        net.setListeners(new ScoreIterationListener(20));   //Print the score (loss function value) every 20 iterations


        // ----- Train the network, evaluating the test set performance at each epoch -----
        int nEpochs = 50;
        String str = "Test set evaluation at epoch %d: Accuracy = %.3f, F1 = %.3f";
        for (int i = 0; i < nEpochs; i++) {
            net.fit(trainData);

            //Evaluate on the test set:
            Evaluation evaluation = net.evaluate(testData);
            log.info(String.format(str, i, evaluation.accuracy(), evaluation.f1()));

            testData.reset();
            trainData.reset();
        }


        net.save(new File(modelSavePath));

        log.info("----- Example Complete -----");
    }


}
