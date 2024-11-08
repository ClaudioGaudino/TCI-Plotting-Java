public class RotationMaths {

    public static double[] rotate(double angleX, double angleY, double angleZ, double x, double y, double z) {
        double[][] rx = createRotationMatrix(angleX);
        double[][] ry = createRotationMatrix(angleY);
        double[][] rz = createRotationMatrix(angleZ);

        double[] rot = applyRotation(rx, new double[]{x, y, z});
        rot = applyRotation(ry, rot);
        rot = applyRotation(rz, rot);

        return rot;
    }

    private static double[][] createRotationMatrix(double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);

        return new double[][]{
                {c,-s, 0},
                {s, c, 0},
                {0, 0, 1}
        };
    }

    private static double[] applyRotation(double[][] matrix, double[] coords) {
        return new double[] {
                matrix[0][0] * coords[0] + matrix[0][1] * coords[1] + matrix[0][2] * coords[2],
                matrix[1][0] * coords[0] + matrix[1][1] * coords[1] + matrix[1][2] * coords[2],
                matrix[2][0] * coords[0] + matrix[2][1] * coords[1] + matrix[2][2] * coords[2]
        };
    }
}
