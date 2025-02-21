public record Config (
        boolean multifile,
        String filePath,
        String accelerationFilePath,
        String anglesFilePath,
        String angularVelocityFilePath,
        String accColX, String accColY, String accColZ,
        String angColX, String angColY, String angColZ,
        String angVelColX, String angVelColY, String angVelColZ,
        String indexCol,
        String accIndexCol, String angIndexCol, String angVelIndexCol,
        boolean useAccMagnitude, boolean useAngVelMagnitude,
        boolean free,
        boolean plotX, boolean plotY, boolean plotZ
) { }
