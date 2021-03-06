package org.deeplearning4j.examples.nlp.word2vec;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by dev on 2018-08-08.
 */
public class W2vAffsFullTrunc {
    private static Logger log = LoggerFactory.getLogger(W2VAffsFull.class);

    private static void trainOnce(Word2Vec w2v, File trainingDataPath) throws Exception {
        // Strip white space before and after for each line
        SentenceIterator iter = new FileSentenceIterator(trainingDataPath);
        //SentenceIterator iter = new BasicLineIterator(trainingDataPath);

        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
        t.setTokenPreProcessor(new CommonPreprocessor());

        w2v.setTokenizerFactory(t);
        w2v.setSentenceIterator(iter);
        //w2v.getConfiguration().setEpochs(epochs);
        //w2v.getConfiguration().setIterations(iterations);

        log.info("Fitting Word2Vec model....");
        w2v.fit();
//
//        log.info("Writing word vectors to text file....");
//        WordVectorSerializer.writeWord2VecModel(w2v, modelFile);

    }

    private static void evaluateSamples(Word2Vec w2v) {
        List<String> words = Arrays.asList("Department", "Institute", "Laboratory", "Germany", "USA", "TN");

        for (String w : words) {
            Collection<String> lst = w2v.wordsNearestSum(w.toLowerCase(), 10);
            log.info("10 Words closest to '{}': {}", w, lst);
        }
    }

    public static void main(String[] args) throws Exception {

        String workingDir = "Y:\\vmshare\\fp2Affs-full\\";
        //String modelFile = workingDir + "aff-w2v.model";
        //File trainingDataPath = new File("Y:\\vmshare\\aff-w2v-trunc");
        File modelFile = new File(workingDir + "w2v-full-40-0001.model");
        File trainingDataPath = new File("Y:\\vmshare\\fp2Affs_uniq_trunc");
        //File trainingDataPath = new File("Y:\\vmshare\\aff-w2v-trunc");

        //String vocabDir = "Y:\\vmshare\\fp2Affs_uniq_trunc_freq";
        //String useVocabFrom = "Y:\\vmshare\\aff-w2v-tr\\aff-full.model";

        Word2Vec vec;

        int epochs = 40;
        int vecSize = 128;

        VocabCache<VocabWord> cache;
        WeightLookupTable<VocabWord> table;
        if (!modelFile.exists()) {
            cache = new AbstractCache<>();
            table = new InMemoryLookupTable.Builder<VocabWord>()
                .vectorLength(vecSize)
                .useAdaGrad(false)
                .cache(cache).build();
        }
        else {
            throw new RuntimeException("model file exists: " + modelFile);
        }

//
//        log.info("Loading model....");
//        vec = WordVectorSerializer.readWord2VecModel(modelFile);
//        evaluateSamples(vec);
//        //vec.getConfiguration().setEpochs(epochs);

        log.info("Creating model....");
        vec = new Word2Vec.Builder()
            .minWordFrequency(10)
            .iterations(1)
            .epochs(epochs)
            .layerSize(vecSize)
            .seed(1234)
            .windowSize(9)
            .lookupTable(table)
            .vocabCache(cache)
            .learningRate(0.001)
            .minLearningRate(1e-5)
            .build();
        vec.setLookupTable(table);
        vec.setVocab(table.getVocabCache());
//        WordVectorSerializer.writeWord2VecModel(vec, modelFile);
//        vec = WordVectorSerializer.readWord2VecModel(modelFile);


        int round = 0;
//        while (true) {

        //log.info("------------ Uptraining round {}", round++);
//        log.info("Building vocab using [{}]", vocabDir);
//            trainOnce(vec, new File(vocabDir));
//        WordVectorSerializer.writeWord2VecModel(vec, modelFile);
//
//        vec = WordVectorSerializer.readWord2VecModel(modelFile);
//        vec.getConfiguration().setLearningRate(0.05);
//        vec.getConfiguration().setMinLearningRate(1e-5);

        log.info("Start training using [{}]", trainingDataPath);
        trainOnce(vec, trainingDataPath);

            evaluateSamples(vec);
            log.info("Writing word vectors to model file {} ....", modelFile);
            WordVectorSerializer.writeWord2VecModel(vec, modelFile);

            //vec = WordVectorSerializer.readWord2VecModel(modelFile);
//        }


        // TODO resolve missing UiServer
//        UiServer server = UiServer.getInstance();
//        System.out.println("Started on port " + server.getPort());
    }
}
