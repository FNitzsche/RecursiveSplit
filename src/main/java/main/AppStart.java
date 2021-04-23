package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import splitting.Line;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppStart extends Application {

    public static final float minDist = 0.05f;

    public static final int targetrX = 1280;
    public static final int targetrY = 720;

    public static int rX = targetrX;
    public static int rY = targetrY;

    public ExecutorService exe = Executors.newSingleThreadExecutor();

    Image img;

    FXMLLoad mainScreen = new FXMLLoad("/main/MainScreen.fxml", new MainScreenCon());


    float[][][] imgArray;
    float[][][] lineImage;
    ArrayList<int[]> baseIndices = new ArrayList<>();

    Line baseLine;

    @Override
    public void start(Stage stage) throws Exception {
        mainScreen.getController(MainScreenCon.class).appStart = this;
        stage.setScene(mainScreen.getScene());
        stage.show();
    }

    public void calculateLines(float maxLod, int iter){
        if (img == null){
            return;
        }
        imgArray = new float[(int)img.getWidth()][(int)img.getHeight()][3];
        lineImage = new float[(int)img.getWidth()][(int)img.getHeight()][3];
        for (int i = 0; i < img.getWidth(); i++){
            for (int j = 0; j < img.getHeight(); j++){
                imgArray[i][j][0] = (float) img.getPixelReader().getColor(i, j).getRed();
                imgArray[i][j][1] = (float) img.getPixelReader().getColor(i, j).getGreen();
                imgArray[i][j][2] = (float) img.getPixelReader().getColor(i, j).getBlue();
                baseIndices.add(new int[]{i, j});
            }
        }
        long s = System.currentTimeMillis();

        baseLine = new Line();
        baseLine.setParams(baseIndices);
        baseLine.devide(imgArray, maxLod);
        baseLine.children(iter, imgArray, maxLod);

        System.out.println("Time: " + (System.currentTimeMillis()-s));
    }

    public Image rePaint(float minDist){
        if (img == null){
            return null;
        }
        baseLine.paintImage(lineImage, minDist);

        WritableImage wimg = new WritableImage((int)img.getWidth(), (int)img.getHeight());

        for (int i = 0; i < img.getWidth(); i++){
            for (int j = 0; j < img.getHeight(); j++){
                wimg.getPixelWriter().setColor(i, j, Color.color(lineImage[i][j][0], lineImage[i][j][1], lineImage[i][j][2]));
            }
        }
        return wimg;
    }

    public void loadImage(String path){
        img = new Image("file:\\" + path, targetrX, targetrY, true, true);
    }

}
