package sample;

import org.ejml.simple.SimpleMatrix;

class RotationMatrix {
    private Vec3 axis;
    private double angle;
    private Vec3 pivot;

    RotationMatrix(Axis axis, double angle, Vec3 pivot) {
        this(axis.toVec3(), angle, pivot);
    }

    RotationMatrix(Vec3 axis, double angle, Vec3 pivot) {
        if (axis.mag() < 0.000001) {
            throw new IllegalArgumentException("Error creating rotation matrix: axis cannot be the zero vector");
        }
        this.axis = axis.unit();
        this.angle = angle;
        this.pivot = pivot.clone();
    }

    Vec3 getAxis() {
        return axis;
    }

    double getAngle() {
        return angle;
    }

    void setAngle(double angle) {
        this.angle = angle;
    }

    void setAxis(Vec3 v) {
        setAxis(v.get(0), v.get(1), v.get(2));
    }

    void setAxis(double d1, double d2, double d3) {
        if (Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3) < 0.000001) {
            throw new IllegalArgumentException("axis cannot be the zero vector");
        }
        axis.set(0, d1);
        axis.set(1, d2);
        axis.set(2, d3);
        axis = axis.unit();
    }

    void setPivot(Vec3 v) {
        pivot = v.clone();
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
        ;
//        switch (axis) {
//            case Z:
//                rot = new SimpleMatrix(new double[][]{
//                        {cosA, -sinA, 0},
//                        {sinA, cosA, 0},
//                        {0, 0, 1}
//                });
//                break;
//            case Y:
//                rot = new SimpleMatrix(new double[][]{
//                        {cosA, 0, sinA},
//                        {0, 1, 0},
//                        {-sinA, 0, cosA}
//                });
//                break;
//            case X:
        SimpleMatrix rot = new SimpleMatrix(new double[][]{
                {1, 0, 0},
                {0, cosA, -sinA},
                {0, sinA, cosA}
        });
        Vec3 v1 = axis, v2, v3;
        if (axis.get(2) == 0) {
            v3 = new Vec3(0, 0, 1);
        } else {
            v3 = new Vec3(0, 1, -axis.get(1) / axis.get(2)).unit();
        }
        v2 = v3.cross(v1).unit();
        if (Math.abs(v1.dot(v2)) > 0.000001 || Math.abs(v1.dot(v3)) > 0.000001 || Math.abs(v2.dot(v3)) > 0.000001
                || Math.abs(v1.mag() - 1) > 0.000001 || Math.abs(v2.mag() - 1) > 0.000001 || Math.abs(v3.mag() - 1) > 0.000001) {
            throw new ArithmeticException("Error multiplying by RotationMatrix: cannot create orthonormal basis");
        }
        SimpleMatrix basis = new SimpleMatrix(new double[][]{
                {v1.get(0), v2.get(0), v3.get(0)},
                {v1.get(1), v2.get(1), v3.get(1)},
                {v1.get(2), v2.get(2), v3.get(2)}
        });
        SimpleMatrix verticesInBasis = basis.invert().mult(mat.minus(transMatrix));
        SimpleMatrix result = basis.mult(rot).mult(verticesInBasis).plus(transMatrix);
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