package model.supervised.neuralnetwork;

import algorithms.gradient.Decent;
import algorithms.gradient.GradientDecent;
import com.google.common.util.concurrent.AtomicDouble;
import data.DataSet;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import model.Predictable;
import model.Trainable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.NumericalComputation;
import utils.array.ArraySumUtil;
import utils.random.RandomUtils;
import utils.sort.SortIntDoubleUtils;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by hanxuan on 10/1/15 for machine_learning.
 */
public class NeuralNetwork implements Trainable, Predictable, GradientDecent, Decent {

    private static final Logger log = LogManager.getLogger(NeuralNetwork.class);

    public static double COST_DECENT_THRESHOLD = 0.00000001;

    public static double COST_COEF = 1;

    public static int MAX_THREADS = 4;

    public static int THREAD_WORK_LOAD = 200; // every thread should work at least 1 second

    public static int BATCH_WORK_LOAD = 500;

    public static int MAX_ROUND = 5000;

    public static int PRINT_GAP = 100;

    public static boolean PRINT_HIDDEN = false;

    public static double EPSILON = 1;

    public static double ALPHA = 0.01;   // learning rate

    public static double LAMBDA = 0.0;  // punish rate

    public static int BUCKET_COUNT = 1;   // mini batch

    private double[][][] theta = null;

    private int[] structure = null;

    private int layerCount = 0;

    private boolean biased = true;

    protected DataSet data = null;

    private TIntArrayList indices = null;

    private ExecutorService service = null;

    private CountDownLatch countDownLatch = null;

    public NeuralNetwork(int[] structure, boolean bias) {
        this.structure = structure;
        this.biased = bias;
    }

    @Override
    public void initialize(DataSet d) {

        this.data = d;

        indices = new TIntArrayList(RandomUtils.getIndexes(d.getInstanceLength()));

        layerCount = structure.length;
        theta = new double[layerCount - 1][][];
        for (int i = 1; i < layerCount; i++) {

            int layerIn = structure[i - 1];
            int layerOut = structure[i];

            if (i < layerCount - 1) {
                layerOut -= (biased ? 1 : 0);    //  output layer no bias other layer consider bias.
            }

            double[][] w = new double[layerOut][];
            double epsilonInit = EPSILON * Math.sqrt(6 / (double)(layerIn + layerOut));
            int randMin = Math.min(layerIn, layerOut);
            int randMax = Math.max(layerIn, layerOut) + 1;
            for (int j = 0; j < layerOut; j++) {
                w[j] = Arrays.stream(RandomUtils.randomIntRangeArray(randMin, randMax, layerIn)).
                        mapToDouble(x ->  epsilonInit * (x * 2.0  - 1)).toArray();
            }
            theta[i - 1] = w;
        }

        log.debug("Initial theta: {}", Arrays.deepToString(theta));

        log.info("Neural Network initialized, structure: {}, bias = {}", structure, biased);
    }

    @Override
    public double predict(double[] feature) {
        double[] labels = Arrays.stream(feedForward(feature, theta)).map(x -> x * 1000000).toArray();
        int[] index = RandomUtils.getIndexes(labels.length);
        SortIntDoubleUtils.sort(index, labels);
        return index[index.length - 1];
    }

    @Override
    public double score(double[] feature) {
        double[] labels = Arrays.stream(feedForward(feature, theta)).map(x -> x * 1000000).toArray();
        int[] index = RandomUtils.getIndexes(labels.length);
        SortIntDoubleUtils.sort(index, labels);
        return index[index.length - 1] == 1 ? labels[index.length - 1] / (double) 1000000 : 1 - labels[index.length - 1] / (double) 1000000;
    }

    @Override
    public double[] probs(double[] feature) {
        return ArraySumUtil.normalize(feedForward(feature, theta));
    }

    @Override
    public void train() {

        log.info("Training started ..");
        loop(data.getInstanceLength(), BUCKET_COUNT, theta, COST_DECENT_THRESHOLD, MAX_ROUND, PRINT_GAP);
        log.info("Training finished ...");
    }



    @Override
    public <T> double cost(T params) {

        double[][][] theta = (double[][][]) params;

        AtomicDouble cost = new AtomicDouble(0);

        int costCalcLength = (int) (data.getInstanceLength() * COST_COEF);

        service = Executors.newFixedThreadPool(MAX_THREADS);
        int packageCount = (int) Math.ceil(costCalcLength / (double) THREAD_WORK_LOAD);
        countDownLatch = new CountDownLatch(packageCount);

        indices.shuffle(new Random());

        TIntHashSet tasks = new TIntHashSet();
        IntStream.range(0, costCalcLength).forEach(i -> {

                    tasks.add(indices.get(i));

                    if (tasks.size() == THREAD_WORK_LOAD || i == costCalcLength - 1) {
                        TIntHashSet tasks2 = new TIntHashSet(tasks);
                        service.submit(() ->
                        {
                            try {

                                for (int taskId : tasks2.toArray()) {

                                    double[] X = data.getInstance(taskId);
                                    double y = data.getLabel(taskId);
                                    double[] labels = probs(X);
                                    cost.getAndAdd(- Math.log(labels[(int) y]));
                                }
                            } catch (Throwable t) {
                                log.error(t.getMessage(), t);
                            }
                            countDownLatch.countDown();

                        });

                        tasks.clear();
                    }
                }
        );

        try {
            TimeUnit.MILLISECONDS.sleep(10);
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        service.shutdown();

        double punish = 0;
        for (int i = 0; i < theta.length; i++)
            for (int j = 0; j < theta[i].length; j++)
                for (int k = 1; k < theta[i][j].length; k++)
                    punish += Math.pow(theta[i][j][k], 2);

        return (cost.get() + punish * LAMBDA) / costCalcLength;
    }

    @Override
    public <T> void gGradient(int start, int end, T params) {

        double[][][] theta = (double[][][]) params;

        service = Executors.newFixedThreadPool(MAX_THREADS);
        int packageCount = (int) Math.ceil((end - start) / (double) THREAD_WORK_LOAD);
        countDownLatch = new CountDownLatch(packageCount);

        TIntArrayList tempIndices = new TIntArrayList(IntStream.range(start, end).toArray());
        tempIndices.shuffle(new Random());

        TIntHashSet tasks = new TIntHashSet();
        IntStream.range(0, tempIndices.size()).forEach(i ->{

                    tasks.add(tempIndices.get(i));

                    if (tasks.size() == THREAD_WORK_LOAD || i == tempIndices.size() - 1) {
                        TIntArrayList tasks2 = new TIntArrayList(tasks);
                        service.submit(() ->
                        {
                            try{
                                TIntHashSet batchTasks = new TIntHashSet(BATCH_WORK_LOAD);
                                for (int batchTaskIdIdx = 0; batchTaskIdIdx < tasks2.size(); batchTaskIdIdx++) {

                                    batchTasks.add(tasks2.get(batchTaskIdIdx));

                                    if(batchTasks.size() == BATCH_WORK_LOAD || batchTaskIdIdx == tasks2.size() - 1) {

                                        double[][][] gradient = new double[theta.length][][];
                                        for (int j = 0; j < theta.length; j++) {
                                            gradient[j] = new double[theta[j].length][theta[j][0].length];
                                        }

                                        Arrays.stream(batchTasks.toArray()).forEach(taskId -> backPropagation(taskId, gradient));


                                        for (int j = 0; j < gradient.length; j++)
                                            for (int k = 0; k < gradient[j].length; k++)
                                                for (int l = 0; l < gradient[j][k].length; l++)
                                                    gradient[j][k][l] = ALPHA * gradient[j][k][l] / (double) batchTasks.size()
                                                            + (l > 0 ? LAMBDA * theta[j][k][l] : 0);

                                        synchronized (theta) {
                                            for (int j = 0; j < gradient.length; j++)
                                                for (int k = 0; k < gradient[j].length; k++)
                                                    for (int l = 0; l < gradient[j][k].length; l++)
                                                        theta[j][k][l] -= gradient[j][k][l];
                                        }

                                        batchTasks.clear();
                                    }
                                }
                            }catch (Throwable t){
                                log.error(t.getMessage(), t);
                            }
                            countDownLatch.countDown();
                        });

                        tasks.clear();
                    }
                }
        );
        try {
            TimeUnit.MILLISECONDS.sleep(10);
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        service.shutdown();
    }

    private static final double sigmG1 = NumericalComputation.sigmoidGradient(1.0);
    private void backPropagation(int i, double[][][] tempGradient) {

        double[] X = data.getInstance(i);
        double[] yVector = yVector(i);

        double[][] Z = new double[layerCount][];
        double[][] A = new double[layerCount][];
        A[0] = X;

        for (int j = 1; j < layerCount; j++) {
            double[][] currentLayerTheta = theta[j - 1];

            Z[j] = new double[currentLayerTheta.length];
            for (int k = 0; k < currentLayerTheta.length; k++) {
                double[] w = currentLayerTheta[k];
                Z[j][k] = z(A[j - 1], w);
            }

            double[] AZ = a(Z[j]);
            if (j < layerCount - 1 && biased) {
                int activeNodeLength = Z[j].length + 1;
                A[j] = new double[activeNodeLength];
                A[j][0] = 1;
                System.arraycopy(AZ, 0, A[j], 1, AZ.length);
            } else {
                A[j] = AZ;
            }
        }

        double[][] DELTA = new double[layerCount][];
        DELTA[layerCount - 1] = IntStream.range(0, yVector.length).mapToDouble(idx -> A[layerCount - 1][idx] - yVector[idx]).toArray();
        for (int j = layerCount - 2; j >= 1; --j) {

            double[][] currentLayerTheta = theta[j];
            double[] currentZ = Z[j];
            double[] sigmG;
            if (biased) {
                sigmG = new double[currentZ.length + 1];
                IntStream.range(1, sigmG.length).forEach(k -> sigmG[k] = NumericalComputation.sigmoidGradient(currentZ[k - 1]));
                sigmG[0] = sigmG1;
                DELTA[j] = new double[currentLayerTheta[0].length - 1];
                for (int k = 0; k < DELTA[j].length; k++) {
                    DELTA[j][k] = z(DELTA[j + 1], currentLayerTheta, k + 1) * sigmG[k + 1];
                }

            }else {
                sigmG = new double[currentZ.length];
                IntStream.range(0, currentZ.length).forEach(k -> sigmG[k] = NumericalComputation.sigmoidGradient(currentZ[k]));
                DELTA[j] = new double[currentLayerTheta[0].length];
                for (int k = 0; k < DELTA[j].length; k++) {
                    DELTA[j][k] = z(DELTA[j + 1], currentLayerTheta, k) * sigmG[k];
                }
            }
        }

        for (int j = 0; j < tempGradient.length; j++)
            for (int k = 0; k < tempGradient[j].length; k++)
                for (int l = 0; l < tempGradient[j][k].length; l++)
                    tempGradient[j][k][l] += DELTA[j + 1][k] * A[j][l];
    }

    public double[] feedForward(double[] feature, double[][][] theta) {

        double[] X = feature;

        double[][] Z = new double[layerCount][];
        double[][] A = new double[layerCount][];
        A[0] = X;

        for (int j = 1; j < layerCount; j++) {
            double[][] currentLayerTheta = theta[j - 1];

            Z[j] = new double[currentLayerTheta.length];
            for (int k = 0; k < currentLayerTheta.length; k++) {
                double[] w = currentLayerTheta[k];
                Z[j][k] = z(A[j - 1], w);
            }

            double[] AZ = a(Z[j]);

            log.debug("HIDDEN Z: {}-{}", j, Z[j]);
            log.debug("HIDDEN A: {}-{}", j, AZ);

            if (j < layerCount - 1 && biased) {
                int activeNodeLength = Z[j].length + 1;
                A[j] = new double[activeNodeLength];
                A[j][0] = 1;
                System.arraycopy(AZ, 0, A[j], 1, AZ.length);
            } else {
                A[j] = AZ;
            }
        }

        if (PRINT_HIDDEN) {
            for (int i = 1; i < A.length - 1; i++) {
                System.out.println("HIDDEN " + i + " : " + Arrays.toString(A[i]));
            }
        }

        return A[A.length - 1];
    }

    private double[] a(double[] Z) {
        return Arrays.stream(Z).map(x -> NumericalComputation.sigmoid(x)).toArray();
    }

    private double z(double[] A, double[] theta){
        return IntStream.range(0, A.length).mapToDouble(i -> A[i] * theta[i]).sum();
    }

    private double z(double[] A, double[][] theta, int col){
        return IntStream.range(0, A.length).mapToDouble(i -> A[i] * theta[i][col]).sum();
    }

    @Override
    public <T> void parameterGradient(int start, int end, T params) {
        gGradient(start, end, params);
    }

    public double[] yVector(int idx) {
        double y = data.getLabel(idx);
        double[] yVector = new double[structure[structure.length - 1]];
        yVector[(int) y] = 1;
        return yVector;
    }

    public static void main(String[] args) {
        int[] struct = new int[]{6, 4, 2, 1};
        NeuralNetwork mp = new NeuralNetwork(struct, false);
        mp.initialize(null);
        log.info(Arrays.deepToString(mp.theta));
    }
}
