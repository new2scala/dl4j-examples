package org.deeplearning4j.examples.recurrent.processnews;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.javacv.FrameFilter;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;

public class CountryIteratorSkip implements DataSetIterator {

    private static Logger log = LoggerFactory.getLogger(CountryIteratorSkip.class);
    private final WordVectors wordVectors;
    private final int batchSize;
    private final int vectorSize;
    private final int truncateLength;
    private int maxLength;
    private final String dataDirectory;

    private final static Integer CategoryInitSize = 256;
    private List<Pair<Integer, List<String>>> sourceData = new ArrayList<>(CategoryInitSize);
    private Map<Integer, String> categoryNameMap = new HashMap<>(CategoryInitSize);
    private int cursor = 0;
    private final TokenizerFactory tokenizerFactory;
//    private int newsPosition = 0;
    private final List<String> labels = new ArrayList<>(CategoryInitSize);
//    private int currCategory = 0;
    private int skip = 0;

//    private List<Integer> cachedCategory;
//    private List<String> cachedCategoryData;

    private List<DataSet> cachedDataSet;

    /**
     * @param dataDirectory  the directory of the news headlines data set
     * @param wordVectors    WordVectors object
     * @param batchSize      Size of each minibatch for training
     * @param truncateLength If headline length exceed this size, it will be truncated to this size.
     * @param train          If true: return the training data. If false: return the testing data.
     *                       <p>
     *                       - initialize various class variables
     *                       - calls populateData function to load news data in categoryData vector
     *                       - also populates labels (i.e. category related inforamtion) in labels class variable
     */
    private CountryIteratorSkip(String dataDirectory,
        WordVectors wordVectors,
        int batchSize,
        int truncateLength,
        boolean train,
        TokenizerFactory tokenizerFactory,
        int skip
    ) {
        this.dataDirectory = dataDirectory;
        this.batchSize = batchSize;
        this.vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;
        this.wordVectors = wordVectors;
        this.truncateLength = truncateLength;
        this.tokenizerFactory = tokenizerFactory;
        this.skip = skip;

        populateData(train); // source data ready
        //prepareCacheData();

//        for (Integer c : categoryNameMap.keySet()) {
//
//        }
//        this.labels = new ArrayList<>(categoryNameMap.keySet());
    }

    private void prepareCacheData() {
        try {
            Pair<List<Integer>, List<String>> cached = prepareCachedData(sourceData, skip);
            int dataCount = cached.getKey().size();
//        this.cachedCategory = cached.getKey();
//        this.cachedCategoryData = cached.getValue();
            int batches = dataCount / batchSize;
            this.cachedDataSet = new ArrayList<>(batches);
            log.info("Preparing DataSet #: {}", batches);
            int startIndex = 0;
            for (int i = 0; i < batches; i++) {
                this.cachedDataSet.add(nextDataSet(batchSize, startIndex, cached.getKey(), cached.getValue()));
                startIndex += batchSize;
            }
            log.info("... done");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Builder Builder() {
        return new Builder();
    }


    @Override
    public DataSet next(int num) {
        if (cursor >= this.cachedDataSet.size()) throw new NoSuchElementException();
        try {
            DataSet res = cachedDataSet.get(cursor); //nextDataSet(num);
            cursor ++;
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> shuffleList(List<T> in) {
        List<T> res = new ArrayList<>(in.size());
        Random rnd = new Random();
        int dataCount = in.size();
        for (int i = 0; i < dataCount; i++) {
            int nextIdx = rnd.nextInt(in.size());
            res.add(in.get(nextIdx));
            in.remove(nextIdx);
        }
        return res;
    }



    public void shuffle() throws Exception {
        List<Pair<Integer, List<String>>> res = new ArrayList<>(sourceData.size());
        for (Pair<Integer, List<String>> p : sourceData) {
            Pair<Integer, List<String>> p2 = Pair.of(p.getKey(), shuffleList(p.getValue()));
            res.add(p2);
        }
        res = shuffleList(res);
        sourceData = res;
        prepareCacheData();
    }

    private DataSet nextDataSet(int batchSize, int startIndex, List<Integer> categories, List<String> categoryData) throws IOException {
        // Loads news into news list from categoryData List along with category of each news
        List<String> news = new ArrayList<>(batchSize);
        List<Integer> category = new ArrayList<>(batchSize);

        int endIndex = Math.min(startIndex+batchSize, categories.size());

        for (int i = startIndex; i < endIndex; i++) {
            news.add(categoryData.get(i));
            category.add(categories.get(i));
        }

//        int countAtCurrPos = 0;
//        while (news.size() < num && hasNext()) {
//            if (currCategory < categoryData.size()) {
//                Pair<String, List<String>> p = this.categoryData.get(currCategory);
////                if (newsPosition < 0)
////                    System.out.println("ok");
//                if (newsPosition >= p.getValue().size()) {
//                    currCategory ++;
//                }
//                else {
//                    int idx = news.size();
//                    news.add(p.getValue().get(newsPosition));
//                    category[idx] = Integer.parseInt(p.getKey().split(",")[0]);
//                    currCategory++;
//                    countAtCurrPos++;
//                }
//            } else {
//                currCategory = 0;
////                System.out.println(
////                    String.format("%d samples at position %d", countAtCurrPos, newsPosition)
////                );
//                newsPosition += (skip+1);
//                countAtCurrPos = 0;
////                if (countAtCurrPos == 0) {
////                    // no more data
////                    newsPosition = Integer.MAX_VALUE;
////                    System.out.println("No more data");
////
////                    break;
////                }
////                else {
////                    countAtCurrPos = 0;
////                }
//            }
//        }
////        System.out.println(
////            String.format("%d samples at position %d", countAtCurrPos, newsPosition)
////        );
//
//        if (news.size() == num) {
//            // make sure we have more data
//            while (currCategory < categoryData.size()) {
//                Pair<String, List<String>> p = this.categoryData.get(currCategory);
//                if (newsPosition >= p.getValue().size())
//                    currCategory ++;
//                else
//                    break;
//            }
//
//            if (currCategory >= categoryData.size()) {
//                currCategory = 0;
//                newsPosition += (skip+1);
//            }
//        }

        //Second: tokenize news and filter out unknown words
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

        //If longest news exceeds 'truncateLength': only take the first 'truncateLength' words
        //System.out.println("maxLength : " + maxLength);
        if (maxLength > truncateLength) maxLength = truncateLength;

        //Create data for training
        //Here: we have news.size() examples of varying lengths
        if (maxLength <= 0)
            System.out.println("ok");
        INDArray features = Nd4j.create(news.size(), vectorSize, maxLength);
        INDArray labels = Nd4j.create(news.size(), sourceData.size(), maxLength);    //Three labels: Crime, Politics, Bollywood

        //Because we are dealing with news of different lengths and only one output at the final time step: use padding arrays
        //Mask arrays contain 1 if data is present at that time step for that example, or 0 if data is just padding
        INDArray featuresMask = Nd4j.zeros(news.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(news.size(), maxLength);

        int[] temp = new int[2];
        for (int i = 0; i < news.size(); i++) {
            List<String> tokens = allTokens.get(i);
            temp[0] = i;
            //Get word vectors for each word in news, and put them in the training data
            for (int j = 0; j < tokens.size() && j < maxLength; j++) {
                String token = tokens.get(j);
                INDArray vector = wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[]{point(i),
                    all(),
                    point(j)}, vector);

                temp[1] = j;
                featuresMask.putScalar(temp, 1.0);
            }
            int idx = category.get(i);
            int lastIdx = Math.min(tokens.size(), maxLength);
            labels.putScalar(new int[]{i, idx, lastIdx - 1}, 1.0);
            labelsMask.putScalar(new int[]{i, lastIdx - 1}, 1.0);
        }

        DataSet ds = new DataSet(features, labels, featuresMask, labelsMask);
        return ds;
    }

    /**
     * Used post training to load a review from a file to a features INDArray that can be passed to the network output method
     *
     * @param file      File to load the review from
     * @param maxLength Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
     * @return Features array
     * @throws IOException If file cannot be read
     */
    public INDArray loadFeaturesFromFile(File file, int maxLength) throws IOException {
        String news = FileUtils.readFileToString(file);
        return loadFeaturesFromString(news, maxLength);
    }

    /**
     * Used post training to convert a String to a features INDArray that can be passed to the network output method
     *
     * @param reviewContents Contents of the review to vectorize
     * @param maxLength      Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
     * @return Features array for the given input String
     */
    public INDArray loadFeaturesFromString(String reviewContents, int maxLength) {
        List<String> tokens = tokenizerFactory.create(reviewContents).getTokens();
        List<String> tokensFiltered = new ArrayList<>();
        for (String t : tokens) {
            if (wordVectors.hasWord(t)) tokensFiltered.add(t);
        }
        int outputLength = Math.max(maxLength, tokensFiltered.size());

        INDArray features = Nd4j.create(1, vectorSize, outputLength);

        for (int j = 0; j < tokens.size() && j < maxLength; j++) {
            String token = tokens.get(j);
            INDArray vector = wordVectors.getWordVectorMatrix(token);
            features.put(new INDArrayIndex[]{point(0),
                all(),
                point(j)}, vector);
        }

        return features;
    }

    private static Pair<List<Integer>, List<String>> prepareCachedData(
        List<Pair<Integer, List<String>>> categoryData,
        int skip
    ) {
        int totalDataCount = 0;
        int step = skip+1;
        for (Pair<Integer, List<String>> p : categoryData) {
            int categorySize = p.getValue().size();
            int usableDataSize = (categorySize-1) / step + 1;
            totalDataCount += usableDataSize;
        }
        List<Integer> cats = new ArrayList<>(totalDataCount);
        List<String> catData = new ArrayList<>(totalDataCount);
        for (Pair<Integer, List<String>> p : categoryData) {
            int idx = 0;
            Integer cat = p.getKey();
            int catSize = p.getValue().size();
            while (idx < catSize) {
                catData.add(p.getValue().get(idx));
                cats.add(cat);
                idx += step;
            }
        }
        return Pair.of(cats, catData);
    }

    /*
    This function loads news headlines from files stored in resources into categoryData List.
     */
    private void populateData(boolean train) {
        File categories = new File(this.dataDirectory + File.separator + "categories.txt");

        try (BufferedReader brCategories = new BufferedReader(new FileReader(categories))) {

            String temp = "";
            while ((temp = brCategories.readLine()) != null) {
                String[] catParts = temp.split(",");
                Integer cat = Integer.parseInt(catParts[0]);
                String catName = catParts[1];
                categoryNameMap.put(cat, catName);
                labels.add(catName);
                String curFileName =
                    String.format(
                        "%s%s%s%s%d.txt",
                        this.dataDirectory,
                        File.separator,
                        (train ? "train" : "test"),
                        File.separator,
                        cat
                    );

                File currFile = new File(curFileName);
                Pair<Integer, List<String>> tempPair;
                if (currFile.exists()) {
                    BufferedReader currBR = new BufferedReader((new FileReader(currFile)));
                    String tempCurrLine = "";
                    List<String> tempList = new ArrayList<>();
                    while ((tempCurrLine = currBR.readLine()) != null) {
                        tempList.add(tempCurrLine);
                    }
                    currBR.close();
                    tempPair = Pair.of(cat, tempList);
                }
                else {
                    tempPair = Pair.of(cat, new ArrayList<>(0));
                }
                sourceData.add(tempPair);
            }
        } catch (Exception e) {
            System.out.println("Exception in reading file :" + e.getMessage());
        }


    }


    @Override
    public int totalExamples() {
        return this.cachedDataSet.size();
    }

    @Override
    public int inputColumns() {
        return vectorSize;
    }

    @Override
    public int totalOutcomes() {
        return this.categoryNameMap.size();
    }

    @Override
    public void reset() {
        cursor = 0;
//        newsPosition = 0;
//        currCategory = 0;
        //shuffle();
    }

    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public int batch() {
        return batchSize;
    }

    @Override
    public int cursor() {
        return cursor;
    }

    @Override
    public int numExamples() {
        return totalExamples();
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getLabels() {
        return this.labels;
    }

    @Override
    public boolean hasNext() {
        return cursor < cachedDataSet.size();
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }

    @Override
    public void remove() {

    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public static class Builder {
        private String dataDirectory;
        private WordVectors wordVectors;
        private int batchSize;
        private int truncateLength;
        TokenizerFactory tokenizerFactory;
        private boolean train;
        private int skip;

        Builder() {
        }

        public Builder dataDirectory(String dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        public Builder wordVectors(WordVectors wordVectors) {
            this.wordVectors = wordVectors;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder truncateLength(int truncateLength) {
            this.truncateLength = truncateLength;
            return this;
        }

        public Builder train(boolean train) {
            this.train = train;
            return this;
        }

        public Builder skip(int skip) {
            this.skip = skip;
            return this;
        }

        public Builder tokenizerFactory(TokenizerFactory tokenizerFactory) {
            this.tokenizerFactory = tokenizerFactory;
            return this;
        }

        public CountryIteratorSkip build() {
            return new CountryIteratorSkip(dataDirectory,
                wordVectors,
                batchSize,
                truncateLength,
                train,
                tokenizerFactory,
                skip
            );
        }

        public String toString() {
            return "org.deeplearning4j.examples.recurrent.ProcessNews.Builder(dataDirectory=" +
                this.dataDirectory + ", wordVectors=" + this.wordVectors +
                ", batchSize=" + this.batchSize + ", truncateLength="
                + this.truncateLength + ", train=" + this.train + ")";
        }
    }

}
