package sample;

class Vec3 {
    public static final Vec3 I = new Vec3(1, 0, 0);
    public static final Vec3 J = new Vec3(0, 1, 0);
    public static final Vec3 K = new Vec3(0, 0, 1);
    double[] v;

    Vec3(double... v) {
        if (v.length == 0) {
            v = new double[]{0, 0, 0};
        } else if (v.length != 3) {
            throw new IllegalArgumentException("Error creating a new Vec3: length of Vec3 must be 3");
        }
        this.v = new double[3];
        for (int i = 0; i < 3; i++) {
            this.v[i] = v[i];
        }
    }

    double get(int i) {
        return v[i];
    }

    void set(int i, double d) {
        v[i] = d;
    }

    Vec3 cross(Vec3 v2) {
        double _0 = v[1] * v2.get(2) - v[2] * v2.get(1);
        double _1 = -(v[0] * v2.get(2) - v[2] * v2.get(0));
        double _2 = v[0] * v2.get(1) - v[1] * v2.get(0);
        return new Vec3(_0, _1, _2);
    }

    double dot(Vec3 v2) {
        return v[0] * v2.get(0) + v[1] * v2.get(1) + v[2] * v2.get(2);
    }

    Vec3 scalarMult(double d) {
        return new Vec3(d * v[0], d * v[1], d * v[2]);
    }

    Vec3 plus(Vec3 v2) {
        return new Vec3(v[0] + v2.get(0), v[1] + v2.get(1), v[2] + v2.get(2));
    }

    Vec3 minus(Vec3 v2) {
        return new Vec3(v[0] - v2.get(0), v[1] - v2.get(1), v[2] - v2.get(2));
    }

    double angleBetween(Vec3 v2) {
        double denominator = v2.dot(v2);
        if (denominator < 0.000001) {
            throw new ArithmeticException("Error: cannot find angle; zero vector has no direction");
        }
        return Math.acos(this.dot(v2) / denominator);
    }

    Vec3 unit() {
        double mag = Math.sqrt(this.dot(this));
        if (mag < 0.000001) {
            throw new ArithmeticException("Error: cannot normalize the zero vector");
        }
        return this.scalarMult(1 / mag);
    }

    double mag() {
        return Math.sqrt(this.dot(this));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Vec3)) {
            return false;
        }
        Vec3 v2 = ((Vec3) other);
        if (v[0] == v2.get(0) && v[1] == v2.get(1) && v[2] == v2.get(2)) {
            return true;
        }
        return false;
    }

    double[] toArray() {
        return new double[]{v[0], v[1], v[2]};
    }
}