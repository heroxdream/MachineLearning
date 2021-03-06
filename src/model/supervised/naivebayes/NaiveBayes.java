package model.supervised.naivebayes;

import data.DataSet;
import model.Predictable;
import model.Trainable;
import org.apache.commons.math3.util.FastMath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.random.RandomUtils;
import utils.sort.SortIntDoubleUtils;

import java.util.HashMap;

/**
 * Created by hanxuan on 10/15/15 for machine_learning.
 */
public abstract class NaiveBayes implements Predictable, Trainable{

    private static final Logger log = LogManager.getLogger(NaiveBayes.class);

    protected DataSet data = null;

    protected int featureLength = Integer.MIN_VALUE;

    protected HashMap<Integer, Object> indexClassMap = null;

    protected int classCount = Integer.MIN_VALUE;

    private double[] priors = null;


    //******************************//

    protected abstract double[] predictClassProbability(double[] features);

    protected abstract void naiveBayesTrain();

    protected abstract void naiveBayesInit();

    //******************************//


    @Override
    public double predict(double[] feature) {
        double[] probabilities = probs(feature);
        log.debug("probabilities {}", probabilities);
        int[] indexes = RandomUtils.getIndexes(classCount);
        SortIntDoubleUtils.sort(indexes, probabilities);
        return indexes[indexes.length - 1];
    }

    @Override
    public double[] probs(double[] feature) {
        double[] probabilities = predictClassProbability(feature);
        for (int i = 0; i < classCount; i++) {
            probabilities[i] += FastMath.log(priors[i]);
        }
        return probabilities;
    }

    @Override
    public double score(double[] feature) {
        double[] probabilities = probs(feature);
        double score = probabilities[1] - probabilities[0];
        return score;
    }

    @Override
    public void train() {

        for (int category : indexClassMap.keySet()) {
            priors[category] = data.getCategoryProportion(category);
        }

        naiveBayesTrain();
    }

    @Override
    public void initialize(DataSet d) {

        data = d;
        featureLength = d.getFeatureLength();
        indexClassMap = d.getLabels().getIndexClassMap();
        classCount = indexClassMap.size();
        priors = new double[classCount];
        naiveBayesInit();

        log.info("Naive Bayes Model initializing, classCount: {}", classCount);
    }
}
