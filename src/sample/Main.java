package sample;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.ejml.data.MatrixType;
import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.Observable;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main extends Application {
    private double ax, ay, az;
    private long frameCtr, t0;
    private double userScale;
    private Color color;
    private Pane pane;
    private Vec3 cameraPos, cameraGaze, cameraUp, cameraRotVel;
    private SimpleMatrix cameraVel;

    public Main() {
        frameCtr = 0;
        t0 = System.currentTimeMillis();
        userScale = 1;
        ax = ay = az = 0;
        color = Color.BLACK;
        cameraPos = new Vec3(0, 0, 4);
        cameraGaze = new Vec3(0, 0, -1);
        cameraUp = new Vec3(0, 1, 0);
        cameraVel = new SimpleMatrix(new double[][]{{0}, {0}, {0}});
        cameraRotVel = new Vec3();
    }

    private List<Line> getEdges(SimpleMatrix vertices) {
        int cols = vertices.numCols();
        if (cols != 8) {
            throw new IllegalArgumentException("Error wrong number of vertices cols: " + cols);
        }
        double transX = pane.getWidth() / 2, transY = pane.getHeight() / 2, scale = 100;
        List<Line> lines = new ArrayList<>();

        Vec3 sum = new Vec3();
        for (int i = 0; i < vertices.numCols(); i++) {
            double z = Math.abs(vertices.get(2, i));
            vertices.set(0, i, vertices.get(0, i) / (z * 0.5));
            vertices.set(1, i, vertices.get(1, i) / (z * 0.5));
            Vec3 toI = new Vec3(vertices.get(0, i), vertices.get(1, i), vertices.get(2, i));
            if (vertices.get(2, i) > 0) {
                return null;
            }
            sum.plus(new Vec3(vertices.get(0, i), vertices.get(1, i), vertices.get(2, i)));
        }
        Vec3 center = new Vec3(sum.get(0) / vertices.numCols(), sum.get(1) / vertices.numCols(), sum.get(2) / vertices.numCols());
        for (int i = 0; i < 4; i++) {
            Line line1 = new Line(transX + scale * (vertices.get(0, i)), transY + scale * (vertices.get(1, i)),
                    transX + scale * (vertices.get(0, i + 4)), transY + scale * (vertices.get(1, i + 4)));
            Line line2 = new Line(transX + scale * (vertices.get(0, i)), transY + scale * (vertices.get(1, i)),
                    transX + scale * (vertices.get(0, (i + 1) % 4)), transY + scale * (vertices.get(1, (i + 1) % 4)));
            Line line3 = new Line(transX + scale * (vertices.get(0, i + 4)), transY + scale * (vertices.get(1, i + 4)),
                    transX + scale * (vertices.get(0, (i + 1) % 4 + 4)), transY + scale * (vertices.get(1, (i + 1) % 4 + 4)));
            //flip y coords because javafx draws things upside down :(
            line1.setStartY(pane.getHeight() - line1.getStartY());
            line1.setEndY(pane.getHeight() - line1.getEndY());
            line2.setStartY(pane.getHeight() - line2.getStartY());
            line2.setEndY(pane.getHeight() - line2.getEndY());
            line3.setStartY(pane.getHeight() - line3.getStartY());
            line3.setEndY(pane.getHeight() - line3.getEndY());
            line1.setStroke(color);
            line2.setStroke(color);
            line3.setStroke(color);
            lines.addAll(Arrays.asList(line1, line2, line3));
        }
        return lines;
    }

    private SimpleMatrix getCameraBasis() {
        Vec3 v3 = cameraGaze.scalarMult(-1);
        Vec3 v2 = cameraUp;
        Vec3 v1 = v2.cross(v3).unit();
        SimpleMatrix basis = new SimpleMatrix(new double[][]{
                {v1.get(0), v2.get(0), v3.get(0)},
                {v1.get(1), v2.get(1), v3.get(1)},
                {v1.get(2), v2.get(2), v3.get(2)},
        });
        return basis;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        pane = new Pane();
        double[] origin = {0, 0, 0};

        double[][] vertices = {
                {-1, -1, -1},
                {-1, 1, -1},
                {1, 1, -1},
                {1, -1, -1},
                {-1, -1, 1},
                {-1, 1, 1},
                {1, 1, 1},
                {1, -1, 1}
        };
        RotationMatrix rotVertical = new RotationMatrix(Axis.X, 0, new Vec3(0, 0, 0));
        RotationMatrix rotHorizontal = new RotationMatrix(Axis.Y, 0, new Vec3(0, 0, 0));
        RotationMatrix rotToZAxis = new RotationMatrix(Axis.Y, 0, new Vec3(0, 0, 0));
        RotationMatrix rotAroundZAxis = new RotationMatrix(Axis.Z, 0, new Vec3(0, 0, 0));
        Text cameraPosText = new Text(200, 200, "FILEER"), cameraGazeText = new Text(300, 350, "TESTINGTESTIMG");
        cameraPosText.setFont(new Font(20));
        cameraGazeText.setFont(new Font(20));
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(10), e -> {
            rotVertical.setAxis(cameraGaze.cross(cameraUp));
            rotVertical.setAngle(cameraRotVel.get(0));
            rotHorizontal.setAxis(new Vec3(0, 1, 0));
            rotHorizontal.setAngle(cameraRotVel.get(1));

            cameraGaze = rotHorizontal.mult(cameraGaze);
            cameraUp = rotHorizontal.mult(cameraUp);

            cameraGaze = rotVertical.mult(cameraGaze);
            cameraUp = rotVertical.mult(cameraUp);
            SimpleMatrix cameraBasis = getCameraBasis();
            //----- update camera position and gaze direction -----
            cameraPos = cameraPos.plus(cameraBasis.mult(cameraVel));
            double magCameraGaze = Math.sqrt(cameraGaze.dot(cameraGaze));
            if (magCameraGaze - 1 > 0.000001) {
                throw new ArithmeticException("Error cameraGaze is not a unit vector mag: " + magCameraGaze);
            }

            List<Line> lines;
            SimpleMatrix verticesMatrix = new SimpleMatrix(vertices).transpose();
            double[][] cameraPositions = new double[3][verticesMatrix.numCols()];
            for (int col = 0; col < verticesMatrix.numCols(); col++) {
                for (int row = 0; row < 3; row++) {
                    cameraPositions[row][col] = cameraPos.get(row);
                }
            }
            SimpleMatrix cameraPosMatrix = new SimpleMatrix(cameraPositions);
            //calculate coordinates within the camera's coordinate axes
//            SimpleMatrix verticesInBasis = cameraBasis.invert().mult(verticesMatrix.minus(cameraPosMatrix));
            //----- prepare for projection: rotate gaze direction to z-axis
            double rotationAngle = cameraGaze.angleBetween(new Vec3(0, 0, -1));
            rotToZAxis.setAngle(rotationAngle);
            rotToZAxis.setPivot(new Vec3());
            Vec3 rotationAxis = cameraGaze.cross(new Vec3(0, 0, -1));
            if (rotationAxis.mag() > 0.000001) {
                rotToZAxis.setAxis(rotationAxis);
                verticesMatrix = rotToZAxis.mult(verticesMatrix.minus(cameraPosMatrix));
                rotToZAxis.setPivot(0, 0, 0);
                Vec3 rotatedCameraUp = rotToZAxis.mult(cameraUp);
                double spin = rotatedCameraUp.angleBetween(Vec3.J);
                if (spin > 0.000001) {
                    rotAroundZAxis.setAngle(spin);
                    Vec3 axis = rotatedCameraUp.cross(Vec3.J);
                    //pivot is (0,0,0)
                    rotAroundZAxis.setAxis(axis);
                    verticesMatrix = rotAroundZAxis.mult(verticesMatrix);
//                    throw new IndexOutOfBoundsException("FUCK spin is " + spin + "cameraUp:" + cameraUp + "rotatedCameraUp: " + rotatedCameraUp);

                }
                verticesMatrix = verticesMatrix.plus(cameraPosMatrix);
            }
            int cols = verticesMatrix.numCols();
            if (cols != 8) {
                throw new ArithmeticException("Oops we lost sum columns. cols: " + cols);
            }

            lines = getEdges(verticesMatrix.minus(cameraPosMatrix));
            pane.getChildren().clear();
            cameraGazeText.setText(cameraGaze.toString());
            cameraPosText.setText(cameraPos.toString());
            if (lines != null) {
                pane.getChildren().addAll(lines);
            }
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();

        BorderPane borderPane = new BorderPane();
        final double MOVEMENT_STEP = 0.15;
        final double ROTATION_STEP = 0.01;
        borderPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent e) {
                switch (e.getCode()) {
                    case W:
                        cameraVel.set(2, -MOVEMENT_STEP);
                        break;
                    case S:
                        cameraVel.set(2, MOVEMENT_STEP);
                        break;
                    case A:
                        cameraVel.set(0, -MOVEMENT_STEP);
                        break;
                    case D:
                        cameraVel.set(0, MOVEMENT_STEP);
                        break;
                    case UP:
                        cameraRotVel.set(0, ROTATION_STEP);
                        break;
                    case DOWN:
                        cameraRotVel.set(0, -ROTATION_STEP);
                        break;
                    case LEFT:
                        cameraRotVel.set(1, ROTATION_STEP);
                        break;
                    case RIGHT:
                        cameraRotVel.set(1, -ROTATION_STEP);
                        break;
                    case SPACE:
                        if (e.isShiftDown()) {
                            cameraVel.set(1, -MOVEMENT_STEP);
                        } else {
                            cameraVel.set(1, MOVEMENT_STEP);
                        }
                        break;
                }
            }
        });
        borderPane.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W:
                case S:
                    cameraVel.set(2, 0);
                    break;
                case A:
                case D:
                    cameraVel.set(0, 0);
                    break;
                case UP:
                case DOWN:
                    cameraRotVel.set(0, 0);
                    break;
                case LEFT:
                case RIGHT:
                    cameraRotVel.set(1, 0);
                    break;
                case SPACE:
                    cameraVel.set(1, 0);
                    break;
            }
        });
        HBox footer = new HBox();
        Button smallerButton = new Button("Smaller");
        Button biggerButton = new Button("Bigger");
        TextField field = new TextField();
        borderPane.setOnKeyTyped(e -> {
            if (e.getCharacter().charAt(0) == ' ') {
                color = Color.color(Math.random(), Math.random(), Math.random() / 3 + 0.6);
            }
        });
        smallerButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                userScale -= 0.1;
                borderPane.requestFocus();

            }
        });
        biggerButton.setOnAction(e -> {
            userScale += 0.1;
            borderPane.requestFocus();
        });
        footer.getChildren().addAll(smallerButton, biggerButton, field);
        footer.getChildren().addAll(cameraPosText, cameraGazeText);
        footer.setSpacing(10);
        borderPane.setCenter(pane);
        borderPane.setBottom(footer);

        primaryStage.setTitle("Render 3D!");
        primaryStage.setScene(new Scene(borderPane, 800, 600));
        primaryStage.show();
        borderPane.requestFocus();
    }
}