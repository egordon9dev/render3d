package sample;

enum Axis {
    X, Y, Z;

    Vec3 toVec3() {
        switch (this) {
            case X:
                return Vec3.I;
            case Y:
                return Vec3.J;
            case Z:
                return Vec3.K;
        }
        return null;
    }
}