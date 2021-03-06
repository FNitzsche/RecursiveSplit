package main;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;

public class MainScreenCon {

    @FXML
    Canvas canvas;
    @FXML
    Slider lod;
    @FXML
    Slider maxLod;
    @FXML
    Slider iter;
    @FXML
    Button recalc;
    @FXML
    Button load;
    @FXML
    Button save;

    public AppStart appStart;

    String imgPath;

    Image shownImage;

    FileChooser fileChooser = new FileChooser();

    public void initialize(){
        lod.setOnMouseReleased(e -> lodChange());
        recalc.setOnAction(e -> recalcLines());
        load.setOnAction(e -> loadImage());
        save.setOnAction(e -> saveImage());
    }

    public void drawImage(Image image){
        Platform.runLater(() -> canvas.getGraphicsContext2D().drawImage(image, 0, 0, 1280, 720));
    }

    public void recalcLines(){
        Runnable run = new Runnable() {
            @Override
            public void run() {
                appStart.calculateLines(((float)Math.pow(10, -maxLod.getValue())), ((int)iter.getValue()));
                Image img = appStart.rePaint(((float)Math.pow(10, -lod.getValue())));
                if (img != null) {
                    Platform.runLater(() -> {
                        shownImage = img;
                        canvas.getGraphicsContext2D().drawImage(img, 0, 0, 1280, 720);
                    });
                }
            }
        };
       appStart.exe.execute(run);
    }

    public void lodChange(){
        Runnable run = new Runnable() {
            @Override
            public void run() {
                Image img = appStart.rePaint(((float)Math.pow(10, -lod.getValue())));
                if (img != null) {
                    Platform.runLater(() -> {
                        shownImage = img;
                        canvas.getGraphicsContext2D().drawImage(img, 0, 0, 1280, 720);
                    });
                }
            }
        };
        appStart.exe.execute(run);

    }

    public void loadImage(){
        File file = fileChooser.showOpenDialog(appStart.mainStage);
        imgPath = file.getAbsolutePath();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                appStart.loadImage(file.getAbsolutePath());
                    Platform.runLater(() -> {
                        drawImage(appStart.img);
                    });
            }
        };
        appStart.exe.execute(run);

    }

    public void saveImage(){
        File file = fileChooser.showSaveDialog(appStart.mainStage);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                Image img = appStart.rePaint(((float)Math.pow(10, -lod.getValue())));
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
                } catch (Exception s) {
                    System.out.println(s);
                }
            }
        };
        appStart.exe.execute(run);
    }


}
