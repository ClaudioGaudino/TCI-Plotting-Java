package data;

import java.util.List;

public interface Clusterer {
    void runModuleClustering(double[][][] Ws, int maxK, int clusteringRepeats);

    int getKOptimal();

    List<List<Integer>> getAssignments();

    List<List<Double>> getMedianProfiles();

    List<List<Double>> getStdProfiles();

    List<List<Double>> getIntraSimilarity();

    List<List<Double>> getInterSimilarity();
}
