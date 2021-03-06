package model.supervised.boosting.adaboot;

import data.DataSet;
import gnu.trove.list.array.TIntArrayList;
import model.Predictable;
import model.Trainable;
import model.supervised.boosting.Boost;
import model.supervised.boosting.adaboot.adaboostclassifier.AdaBoostClassifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import performance.ClassificationEvaluator;
import performance.Evaluator;
import utils.array.ArraySumUtil;
import utils.array.ArrayUtil;
import utils.random.RandomUtils;
import utils.sort.SortIntDoubleUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by hanxuan on 10/30/15 for machine_learning.
 */
public class SAMME implements Trainable, Predictable, Boost{

    public static boolean NEED_ROUND_REPORT = false;

    private static Logger log = LogManager.getLogger(SAMME.class);

    protected DataSet trainingData = null;

    protected DataSet testingData = null;

    protected double[] alpha = null;

    protected double[] weights = null;

    protected int classCount = Integer.MIN_VALUE;

    protected AdaBoostClassifier[] adaBoostClassifiers = null;

    protected ClassificationEvaluator roundEvaluator = null;

    protected double[] roundTrainingError = null;

    protected double[] roundTestingError = null;

    protected double[] roundError = null;

    protected double[] roundTestingAUC = null;

    protected int[] topFeatures = null;

    public SAMME(){}

    @Override
    public double predict(double[] feature) {
        double[] classScores = probs(feature);
        int[] indexes = RandomUtils.getIndexes(classCount);
        SortIntDoubleUtils.sort(indexes, classScores);
        return indexes[classCount - 1];
    }

    @Override
    public double[] probs(double[] feature) {
        double[] classScores = new double[classCount];
        for (int i = 0; i < alpha.length; i++) {
            if (alpha[i] == 0) continue;
            int predictClass = (int) adaBoostClassifiers[i].boostPredict(feature);
            classScores[predictClass] += alpha[i];
        }
        return ArraySumUtil.normalize(classScores);
    }

    @Override
    public double score(double[] feature){
        double[] classScores = new double[classCount];
        for (int i = 0; i < alpha.length; i++) {
            if (alpha[i] == 0) continue;
            int predictClass = (int) adaBoostClassifiers[i].boostPredict(feature);
            classScores[predictClass] += alpha[i];
        }
        return classScores[1] - classScores[0];
    }

    @Override
    public void train() {

        for (int i = 0; i < adaBoostClassifiers.length; i++) {
            AdaBoostClassifier classifier = adaBoostClassifiers[i];
            classifier.boostInitialize(trainingData, weights);
            classifier.boost();

            double error = getWeightedError(classifier);

            alpha[i] = (Math.log((1 - error) / error) + Math.log(classCount - 1)) / (double) 2;

            modifyWeights(classifier, alpha[i]);
            ArraySumUtil.normalize(weights);

            if (NEED_ROUND_REPORT) {
                statisticReport(i, error);
            }

            log.info("{} round boosting finished ...", i);
        }

        log.info("SAMME Training finished ...");

        if (NEED_ROUND_REPORT) {
            printRoundReport();
        }

        try{
            TimeUnit.SECONDS.sleep(1);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void printRoundReport() {

        log.info("======================= Round Report =======================");
        log.info("alpha: {}", alpha);
        log.info("roundTestingError: {}", roundTestingError);
        log.info("roundTestingAUC: {}", roundTestingAUC);
        log.info("roundTrainingError: {}", roundTrainingError);
        log.info("roundError: {}", roundError);
        log.info("======================= ============ =======================");
    }

    protected void statisticReport(int round, double error){

        roundError[round] = error;

        roundEvaluator.initialize(testingData, this);
        roundEvaluator.getPredictLabel();
        roundTestingError[round] = 1 - roundEvaluator.evaluate();
        roundTestingAUC[round] = roundEvaluator.getArea();

        roundEvaluator.initialize(trainingData, this);
        roundEvaluator.getPredictLabel();
        roundTrainingError[round] = 1 - roundEvaluator.evaluate();

        log.info("round({}) report: roundError({}) testError({}), trainError({})", round, error,
                roundTestingError[round], roundTrainingError[round]);
    }

    protected void modifyWeights(AdaBoostClassifier classifier, double alpha){

        for (int i = 0; i < weights.length; i++) {
            double[] feature = trainingData.getInstance(i);
            if (classifier.boostPredict(feature) != trainingData.getLabel(i)){
                weights[i] *= Math.exp(alpha);
            }else {
                weights[i] *= Math.exp(-alpha);
            }
        }
    }

    protected double getWeightedError(AdaBoostClassifier classifier) {

        double weightedError = 0;
        int instanceLength = trainingData.getInstanceLength();
        for (int i = 0; i < instanceLength; i++) {
            double[] feature = trainingData.getInstance(i);
            if (classifier.boostPredict(feature) != trainingData.getLabel(i)){
                weightedError += weights[i];
            }
        }
        return weightedError;
    }

    @Override
    public void initialize(DataSet d) {

        trainingData = d;
        classCount = trainingData.getLabels().getIndexClassMap().size();
        topFeatures = RandomUtils.getIndexes(trainingData.getFeatureLength());

        int instanceLength = trainingData.getInstanceLength();
        weights = new double[instanceLength];
        Arrays.fill(weights, 1 / (double) instanceLength);
    }

    @Override
    public void boostConfig(int iteration, String classifierClassName, Evaluator evaluator, DataSet testData)
    throws Exception{

        AdaBoostClassifier[] classifiers = new AdaBoostClassifier[iteration];
        for (int i = 0; i < iteration; i++) {
            classifiers[i] = (AdaBoostClassifier) Class.forName(classifierClassName).getConstructor().newInstance();
        }

        roundTestingError = new double[classifiers.length];
        roundTrainingError = new double[classifiers.length];
        roundError = new double[classifiers.length];
        roundTestingAUC = new double [classifiers.length];

        alpha = new double[classifiers.length];
        adaBoostClassifiers = classifiers;
        roundEvaluator = (ClassificationEvaluator) evaluator;
        testingData = testData;

        log.info("SAMME configTrainable: ");
        log.info("classifiers count: {}", classifiers.length);
        log.info("classifiers CLASS: {}", classifierClassName);
    }

    public void topFeatureCalc() {

        double[] featureScore = new double[trainingData.getFeatureLength()];
        for (int i = 0; i < trainingData.getInstanceLength(); i++) {
            double[] X = trainingData.getInstance(i);
            for (int j = 0; j < adaBoostClassifiers.length; j++) {
                int bestFeatureId = adaBoostClassifiers[j].bestFeatureId();
                if (adaBoostClassifiers[j].boostPredict(X) ==  trainingData.getLabel(j)) {
                    featureScore[bestFeatureId] += alpha[j];
                }else {
                    featureScore[bestFeatureId] -= alpha[j];
                }
            }
        }

        ArraySumUtil.normalize(featureScore);
        SortIntDoubleUtils.sort(topFeatures, featureScore);
        ArrayUtil.reverse(topFeatures);
    }

    public int[] topNFeatures(int n){

        TIntArrayList topN = new TIntArrayList(n);
        IntStream.range(0, Math.min(n, topFeatures.length)).forEach(i -> topN.add(topFeatures[i]));
        return topN.toArray();
    }
}
