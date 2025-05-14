package data;

public record ModuleClusterResult(
        int optimalK,
        int[][] assignments,
        double[][] medianProfiles,
        double[][] stdProfiles,
        double[][] intraClusterSimilarity,
        double[][] interClusterSimilarity
) {
}
