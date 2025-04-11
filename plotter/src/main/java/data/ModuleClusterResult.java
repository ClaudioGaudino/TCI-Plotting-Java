package data;

public record ModuleClusterResult(
        int optimalK,
        int[][] assignments, // will always be [x][3]
        double[][] medianProfiles,
        double[][] stdProfiles,
        double[][] intraClusterSimilarity, //will always be [y][2]
        double[][] interClusterSimilarity
) {
}
