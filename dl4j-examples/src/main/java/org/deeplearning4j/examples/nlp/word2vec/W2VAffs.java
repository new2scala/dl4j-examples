package org.deeplearning4j.examples.nlp.word2vec;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
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

public class W2VAffs {
    private static Logger log = LoggerFactory.getLogger(Word2VecRawTextExample.class);

    public static void main(String[] args) throws Exception {

        String modelFile = "/media/sf_vmshare/aff-w2v.model";
        File mf = new File(modelFile);

        Word2Vec vec;
        if (!mf.exists()) {
            // Gets Path to Text file
            File dataPath = new File("/media/sf_vmshare/aff-w2v");

            log.info("Load & Vectorize Sentences....");
            // Strip white space before and after for each line
            SentenceIterator iter = new FileSentenceIterator(dataPath);
            // Split on white spaces in the line to get words
            TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
            t.setTokenPreProcessor(new CommonPreprocessor());

            log.info("Building model....");
            vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(50)
                .layerSize(64)
                .seed(1234)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

            log.info("Fitting Word2Vec model....");
            vec.fit();

            log.info("Writing word vectors to text file....");
            WordVectorSerializer.writeWord2VecModel(vec, modelFile);
        }
        else {
            vec = WordVectorSerializer.readWord2VecModel(modelFile);
        }


        log.info("Closest Words:");
        List<String> words = Arrays.asList("Department", "Institute", "Laboratory", "Germany", "USA", "TN");


        for (String w : words) {
            Collection<String> lst = vec.wordsNearestSum(w.toLowerCase(), 10);
            log.info("10 Words closest to '{}': {}", w, lst);
        }

        // TODO resolve missing UiServer
//        UiServer server = UiServer.getInstance();
//        System.out.println("Started on port " + server.getPort());
    }

}
