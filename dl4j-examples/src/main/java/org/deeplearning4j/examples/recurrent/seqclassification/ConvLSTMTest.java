package org.deeplearning4j.examples.recurrent.seqclassification;

import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.AdaGrad;

public class ConvLSTMTest {

    public static void main(String[] args) throws Exception {
//        int filters = 10;
//        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
//            .seed(12345)
//            .l2(0.001)
//            .updater(new AdaGrad(0.04))
//            .list()
//            .layer(0, new ConvolutionLayer.Builder(3, 3)
//                .nIn(1)
//                .nOut(filters)
//                .stride(1, 1)
//                .activation(Activation.RELU)
//                .weightInit(WeightInit.RELU)
//                .build()
//            )   // 10 x 6*6
//            .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
//                .kernelSize(2, 2)
//                .stride(2, 2)
//                .build()
//            )   // 10 x 3*3
//            .layer(2, new DenseLayer.Builder()
//                .activation(Activation.RELU)
//                .nIn(90)
//                .nOut(16)
//                .weightInit(WeightInit.RELU)
//                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
//                .gradientNormalizationThreshold(10)
//                .updater(new AdaGrad(0.01))
//                .build()
//            )
    }
}
