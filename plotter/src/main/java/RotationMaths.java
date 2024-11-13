public class RotationMaths {
    public static double[] rotate(double angleX, double angleY, double angleZ, double x, double y, double z) {
        double[][] rotationMatrix = createRotationMatrix(angleX, angleY, angleZ);

        return applyRotation(x, y, z, rotationMatrix);
    }

    private static double[][] createRotationMatrix(double x, double y, double z) {
        double pitch = Math.toRadians(y);
        double roll = Math.toRadians(x);
        double yaw = Math.toRadians(z);

        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double cosRoll = Math.cos(roll);
        double sinRoll = Math.sin(roll);
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);

        return new double[][]{
                {cosYaw * cosPitch, cosYaw * sinPitch * sinRoll - sinYaw * cosRoll, cosYaw * sinPitch * cosRoll + sinYaw * sinRoll},
                {sinYaw * cosPitch, sinYaw * sinPitch * sinRoll - cosYaw * cosRoll, sinYaw * sinPitch * cosRoll - cosYaw * sinRoll},
                {-1 * sinPitch, cosPitch * sinRoll, cosPitch * cosRoll}
        };
    }

    private static double[] applyRotation(double x, double y, double z, double[][] rotationMatrix) {
        return new double[]{
                x * rotationMatrix[0][0] + y * rotationMatrix[0][1] + z * rotationMatrix[0][2],
                x * rotationMatrix[1][0] + y * rotationMatrix[1][1] + z * rotationMatrix[1][2],
                x * rotationMatrix[2][0] + y * rotationMatrix[2][1] + z * rotationMatrix[2][2]
        };
    }
}
