package sample;

import org.ejml.simple.SimpleMatrix;

class RotationMatrix {
    private Axis axis;
    private double angle;
    private Vec3 pivot;

    RotationMatrix(Axis axis, double angle, Vec3 pivot) {
        this.axis = axis;
        this.angle = angle;
        this.pivot = pivot;
    }

    void setAngle(double angle) {
        this.angle = angle;
    }

    Axis getAxis() {
        return axis;
    }

    double getAngle() {
        return angle;
    }

    void setPivot(Vec3 v) {
        pivot = v;
    }

    SimpleMatrix mult(SimpleMatrix mat) {
        double sinA = Math.sin(angle), cosA = Math.cos(angle);
        double[] mag0 = new double[mat.numCols()];
        double[][] trans = new double[3][mat.numCols()];
        for (int col = 0; col < mat.numCols(); col++) {
            for (int row = 0; row < 3; row++) {
                trans[row][col] = pivot.get(row);
            }
        }
        SimpleMatrix transMatrix = new SimpleMatrix(trans);
        SimpleMatrix delta0 = mat.minus(transMatrix);
        for (int col = 0; col < delta0.numCols(); col++) {
            mag0[col] = Math.sqrt(delta0.get(0, col) * delta0.get(0, col) + delta0.get(1, col) * delta0.get(1, col) + delta0.get(2, col) * delta0.get(2, col));
        }
        SimpleMatrix rot = null;
        switch (axis) {
            case Z:
                rot = new SimpleMatrix(new double[][]{
                        {cosA, -sinA, 0},
                        {sinA, cosA, 0},
                        {0, 0, 1}
                });
                break;
            case Y:
                rot = new SimpleMatrix(new double[][]{
                        {cosA, 0, sinA},
                        {0, 1, 0},
                        {-sinA, 0, cosA}
                });
                break;
            case X:
                rot = new SimpleMatrix(new double[][]{
                        {1, 0, 0},
                        {0, cosA, -sinA},
                        {0, sinA, cosA}
                });
                break;
        }
        SimpleMatrix result = rot.mult(mat.minus(transMatrix)).plus(transMatrix);
        SimpleMatrix deltaf = result.minus(transMatrix);
        double[] magf = new double[deltaf.numCols()];
        for (int col = 0; col < deltaf.numCols(); col++) {
            magf[col] = Math.sqrt(deltaf.get(0, col) * deltaf.get(0, col) + deltaf.get(1, col) * deltaf.get(1, col) + deltaf.get(2, col) * deltaf.get(2, col));
            double diff = Math.abs(magf[col] - mag0[col]);
            if (diff > 0.000001) {
                throw new ArithmeticException("Error rotation changed magnitude by " + diff);
            }
        }
        return result;
    }

    Vec3 mult(Vec3 v) {
        double[][] mat2d = {
                {v.get(0)},
                {v.get(1)},
                {v.get(2)}
        };
        SimpleMatrix matrix = mult(new SimpleMatrix(mat2d));
        return new Vec3(matrix.get(0, 0), matrix.get(1, 0), matrix.get(2, 0));
    }
}