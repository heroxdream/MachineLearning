package model.supervised.svm.kernels;

import utils.array.ArrayUtil;

/**
 * Created by hanxuan on 11/25/15 for machine_learning.
 */
public class GaussianK implements Kernel {

    public static double sigma = 1;

    @Override
    public double similarity(double[] x1, double[] x2) {
        double dis = ArrayUtil.euclidean(x1, x2);
        return Math.exp(- dis / sigma / 2);
    }
}
