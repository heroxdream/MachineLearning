package algorithms.gradient;

import data.DataSet;

/**
 * Created by hanxuan on 9/30/15 for machine_learning.
 */
public interface NewtonDecent {
    double[] nGradient(DataSet data, int start, int end, double[] theta);
}
