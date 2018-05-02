package org.deeplearning4j.examples.modelimport.keras;

import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModel;
import org.deeplearning4j.nn.modelimport.keras.utils.KerasModelBuilder;

/**
 * Simple example showing how to load resnet50 from keras into dl4j
 */
public class LoadResNet50 {

    public final static String MODEL_PATH = "modelimport/keras/resnet50_weights_tf_dim_ordering_tf_kernels.h5";

    public static void main(String[] args) throws Exception {

        //ComputationGraph model = KerasModelImport.importKerasModelAndWeights(new ClassPathResource(MODEL_PATH).getFile().getPath());

        KerasModelBuilder builder = new KerasModel().modelBuilder();
        builder.modelHdf5Filename(new ClassPathResource(MODEL_PATH).getFile().getPath());
        builder.enforceTrainingConfig(false);

        ComputationGraph model = builder.buildModel().getComputationGraph();


        System.out.println(model.summary());

    }
}
