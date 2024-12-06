public class Config {
    private final boolean multifile;

    private final String filePath;

    private final String accelerationFilePath;
    private final String anglesFilePath;
    private final String angularVelocityFilePath;

    private final String accColX;
    private final String accColY;
    private final String accColZ;

    private final String angColX;
    private final String angColY;
    private final String angColZ;

    private final String angVelColX;
    private final String angVelColY;
    private final String angVelColZ;

    private final String indexCol;

    private final String accIndexCol;
    private final String angIndexCol;
    private final String angVelIndexCol;

    private final boolean useAccMagnitude;
    private final boolean useAngVelMagnitude;

    private final boolean free;

    private final boolean plotX;
    private final boolean plotY;
    private final boolean plotZ;

    public Config(boolean multifile, String filePath, String accelerationFilePath, String anglesFilePath, String angularVelocityFilePath, String accColX, String accColY, String accColZ, String angColX, String angColY, String angColZ, String angVelColX, String angVelColY, String angVelColZ, String indexCol, String accIndexCol, String angIndexCol, String angVelIndexCol, boolean useAccMagnitude, boolean useAngVelMagnitude, boolean free, boolean plotX, boolean plotY, boolean plotZ) {
        this.multifile = multifile;
        this.filePath = filePath;
        this.accelerationFilePath = accelerationFilePath;
        this.anglesFilePath = anglesFilePath;
        this.angularVelocityFilePath = angularVelocityFilePath;
        this.accColX = accColX;
        this.accColY = accColY;
        this.accColZ = accColZ;
        this.angColX = angColX;
        this.angColY = angColY;
        this.angColZ = angColZ;
        this.angVelColX = angVelColX;
        this.angVelColY = angVelColY;
        this.angVelColZ = angVelColZ;
        this.indexCol = indexCol;
        this.accIndexCol = accIndexCol;
        this.angIndexCol = angIndexCol;
        this.angVelIndexCol = angVelIndexCol;
        this.useAccMagnitude = useAccMagnitude;
        this.useAngVelMagnitude = useAngVelMagnitude;
        this.free = free;
        this.plotX = plotX;
        this.plotY = plotY;
        this.plotZ = plotZ;
    }

    public boolean isMultifile() {
        return multifile;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getAccelerationFilePath() {
        return accelerationFilePath;
    }

    public String getAnglesFilePath() {
        return anglesFilePath;
    }

    public String getAngularVelocityFilePath() {
        return angularVelocityFilePath;
    }

    public String getAccColX() {
        return accColX;
    }

    public String getAccColY() {
        return accColY;
    }

    public String getAccColZ() {
        return accColZ;
    }

    public String getAngColX() {
        return angColX;
    }

    public String getAngColY() {
        return angColY;
    }

    public String getAngColZ() {
        return angColZ;
    }

    public String getAngVelColX() {
        return angVelColX;
    }

    public String getAngVelColY() {
        return angVelColY;
    }

    public String getAngVelColZ() {
        return angVelColZ;
    }

    public String getIndexCol() {
        return indexCol;
    }

    public String getAccIndexCol() {
        return accIndexCol;
    }

    public String getAngIndexCol() {
        return angIndexCol;
    }

    public String getAngVelIndexCol() {
        return angVelIndexCol;
    }

    public boolean isUseAccMagnitude() {
        return useAccMagnitude;
    }
    public boolean isUseAngVelMagnitude() {
        return useAngVelMagnitude;
    }

    public boolean isFree() {
        return free;
    }

    public boolean isPlotX() {
        return plotX;
    }

    public boolean isPlotY() {
        return plotY;
    }

    public boolean isPlotZ() {
        return plotZ;
    }
}
