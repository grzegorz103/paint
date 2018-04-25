package main;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;


public class Controller {

    @FXML
    private TextField size;

    @FXML
    private ColorPicker color;

    @FXML
    private Canvas cv;

    @FXML
    private ToggleGroup tg;

    @FXML
    private ToggleButton rect, line, elipse, eraser, fill;

    private double x;
    private double y;
    private GraphicsContext gc;

    private final Alert.AlertType TYPE1 = Alert.AlertType.CONFIRMATION;
    private final Alert.AlertType TYPE2 = Alert.AlertType.INFORMATION;

    @FXML
    public void initialize() {
        gc = cv.getGraphicsContext2D();
        gc.setFill(Color.color(1, 1, 1));
        gc.fillRect(0, 0, this.cv.getWidth(), this.cv.getHeight());

        cv.setOnMouseDragged(this::drawDragged);
        cv.setOnMousePressed(this::mousePressed);
        cv.setOnMouseReleased(this::mouseReleased);

    }

    private void mouseReleased(MouseEvent event) {
        gc.setFill(color.getValue());
        Toggle current = tg.getSelectedToggle();
        if (current == rect) {
            Point2D p2d = new Point2D(Math.min(event.getSceneX(), x), Math.min(event.getSceneY(), y));
            Point2D p2dd = new Point2D(Math.abs(event.getSceneX() - x), Math.abs(event.getSceneY() - y));
            gc.fillRect(p2d.getX(), p2d.getY(), p2dd.getX(), p2dd.getY());
        } else if (current == line) {
            gc.setLineWidth(Double.parseDouble(this.size.getText()));
            gc.setStroke(color.getValue());
            gc.strokeLine(x, y, event.getSceneX(), event.getSceneY());
        } else if (current == elipse) {
            Point2D p2d = new Point2D(Math.min(event.getSceneX(), x), Math.min(event.getSceneY(), y));
            Point2D p2dd = new Point2D(Math.abs(event.getSceneX() - x), Math.abs(event.getSceneY() - y));
            gc.fillOval(p2d.getX(), p2d.getY(), p2dd.getX(), p2dd.getY());
        } else if (current == eraser) {
            gc.setFill(Color.color(1, 1, 1));
            gc.fillRect(event.getSceneX(), event.getSceneY(), 15, 15);
        } else if (current == fill) {
            gc.setFill(color.getValue());
            flood_fill((int) event.getSceneX(), (int) event.getSceneY());
        }
    }


    private void flood_fill(int x, int y) {
        WritableImage wi = new WritableImage((int) this.cv.getWidth(), (int) this.cv.getHeight());
        boolean[][] visited = new boolean[(int) this.cv.getWidth()][(int) this.cv.getHeight()];
        cv.snapshot(null, wi);
        PixelReader pr = wi.getPixelReader();
        Stack<Point2D> stack = new Stack();
        Color source = pr.getColor(x, y);
        stack.add(new Point2D(x, y));

        while (!stack.isEmpty()) {
            Point2D newPosition = stack.pop();
            if (newPosition.getX() < wi.getWidth() && newPosition.getX() > 0 && newPosition.getY() < wi.getHeight() && newPosition.getY() > 0) {
                if (pr.getColor((int) newPosition.getX(), (int) newPosition.getY()).equals(source)) {
                    if (!visited[(int) newPosition.getX()][(int) newPosition.getY()]) {
                        gc.fillRect(newPosition.getX(), newPosition.getY(), 1, 1);
                        visited[(int) newPosition.getX()][(int) newPosition.getY()] = true;
                        stack.add(new Point2D(newPosition.getX() - 1, newPosition.getY()));
                        stack.add(new Point2D(newPosition.getX() + 1, newPosition.getY()));
                        stack.add(new Point2D(newPosition.getX(), newPosition.getY() - 1));
                        stack.add(new Point2D(newPosition.getX(), newPosition.getY() + 1));
                    }
                }
            }
        }
    }


    private void mousePressed(MouseEvent event) {
        gc.setFill(color.getValue());
        Toggle current = tg.getSelectedToggle();
        if (current == null) {
            gc.fillRect(event.getSceneX(), event.getSceneY(), Double.parseDouble(size.getText()), Double.parseDouble(size.getText()));
        } else if (current == rect || current == line || current == elipse) {
            x = event.getSceneX();
            y = event.getSceneY();
        } else if (current == eraser) {
            gc.setFill(Color.color(1, 1, 1));
            gc.fillRect(event.getSceneX(), event.getSceneY(), 15, 15);
        }
    }

    private void drawDragged(MouseEvent event) {
        gc.setFill(color.getValue());
        Toggle current = tg.getSelectedToggle();
        if (current == null) {
            gc.fillRect(event.getSceneX(), event.getSceneY(), Double.parseDouble(size.getText()), Double.parseDouble(size.getText()));
        } else if (current == eraser) {
            gc.setFill(Color.color(1, 1, 1));
            gc.fillRect(event.getSceneX(), event.getSceneY(), 15, 15);
        }
    }


    public void close(ActionEvent actionEvent) {
        setAlert(TYPE1, "Are you sure you want to exit?", 1);
    }

    public void save(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter ef = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        fc.getExtensionFilters().add(ef);

        Stage stage = (Stage) this.cv.getScene().getWindow();
        File file = fc.showSaveDialog(stage);

        if (file != null) {
            WritableImage wi = new WritableImage((int) this.cv.getWidth(), (int) this.cv.getHeight());
            cv.snapshot(null, wi);
            RenderedImage ri = SwingFXUtils.fromFXImage(wi, null);
            try {
                ImageIO.write(ri, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void about(ActionEvent actionEvent) {
        setAlert(TYPE2, "Simple paint application which allows you to draw various shapes.\n\nAuthor: github.com/grzegorz103", 0);
    }

    public void newCanvas(ActionEvent actionEvent) {
        setAlert(TYPE1, "Are you sure?", 0);
    }

    private void setAlert(Alert.AlertType type, String text, int close) {
        Alert alert = new Alert(type);
        alert.setTitle("Paint");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();

        if (type == TYPE1) {
            if (alert.getResult() == ButtonType.OK) {
                if (close == 0) {
                    gc.setFill(Color.color(1, 1, 1));
                    gc.fillRect(0, 0, this.cv.getWidth(), this.cv.getHeight());
                } else {
                    System.exit(0);
                }
            }
        }
    }
}
