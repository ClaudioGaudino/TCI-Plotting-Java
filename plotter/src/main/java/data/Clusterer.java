package data;

import java.util.Map;

public interface Clusterer {
    Map<String, Object> runModuleClustering(double[][][] Ws, int maxK, int clusteringRepeats);
}
