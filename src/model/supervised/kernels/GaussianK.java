package model.supervised.kernels;

import utils.array.ArrayUtil;

/**
 * Created by hanxuan on 11/25/15 for machine_learning.
 */
public class GaussianK implements Kernel {

    public static double GAMMA = 1;

    @Override
    public double similarity(double[] x1, double[] x2) {
        double dis = ArrayUtil.euclidean(x1, x2);
        return Math.exp( - GAMMA * dis);
    }

    public double similarity(double x1x1, double x2x2, double[] x1, double[] x2) {
        double dis = x1x1 - 2 * ArrayUtil.innerProduct(x1, x2) + x2x2;
        return Math.exp( - GAMMA * dis);
    }
}
