package splitting;

import main.AppStart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public class Line {

    Random rnd = new Random();

    public float[] cL = new float[2];
    public float[] cH = new float[2];

    public float[] color = {rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat()};

    public ArrayList<int[]> allIndices = new ArrayList<>();
    public ArrayList<int[]> sectionOneIndices = new ArrayList<>();
    public ArrayList<int[]> sectionTwoIndices = new ArrayList<>();

    public Line sectionOne = null;
    public Line sectionTwo = null;

    public boolean closeEnough = false;
    public double lod = 1;

    public void setParams(ArrayList<int[]> indices){
        allIndices = indices;
    }

    public void devide(float[][][] image, float minDist){
        lod = fitLine(cL, cH, allIndices, image);
        closeEnough = (lod < minDist);
        sectionOneIndices = allIndices.parallelStream().filter(ints -> spaceDistance(ints, cL) < spaceDistance(ints, cH)).collect(Collectors.toCollection(ArrayList::new));
        sectionTwoIndices = allIndices.parallelStream().filter(ints -> spaceDistance(ints, cL) >= spaceDistance(ints, cH)).collect(Collectors.toCollection(ArrayList::new));
    }

    public void children(int genLeft, float[][][] image, float minDist){
        color = allIndices.stream().map(ints -> image[ints[0]][ints[1]]).reduce(new float[3], (f1, f2)-> {
            f1[0] += f2[0];
            f1[1] += f2[1];
            f1[2] += f2[2];
            return f1;
        });
        color[0] /= allIndices.size();
        color[1] /= allIndices.size();
        color[2] /= allIndices.size();
        if (genLeft > 0 && !closeEnough) {
            sectionOne = new Line();
            sectionTwo = new Line();
            sectionOne.setParams(sectionOneIndices);
            sectionTwo.setParams(sectionTwoIndices);
            sectionOne.devide(image, minDist);
            sectionTwo.devide(image, minDist);
            Line[] tmp = {sectionOne, sectionTwo};
            Arrays.stream(tmp).parallel().forEach(line -> line.children(genLeft-1, image, minDist));
        }
    }

    public void paintImage(float[][][] image, float minDist){
        if (sectionOne == null || sectionTwo == null || (lod < minDist)){
            allIndices.stream().parallel().forEach(ints -> {
                image[ints[0]][ints[1]][0] = color[0];
                image[ints[0]][ints[1]][1] = color[1];
                image[ints[0]][ints[1]][2] = color[2];
            });
        } else {
            sectionOne.paintImage(image, minDist);
            sectionTwo.paintImage(image, minDist);
        }
    }

    public static double fitLine(float[] cL, float[] cH, ArrayList<int[]> indices, float[][][] image){
        float[] midPoint = indices.stream().map(i -> image[i[0]][i[1]]).reduce(new float[3], (f1, f2) -> {
            f1[0] += f2[0];
            f1[1] += f2[1];
            f1[2] += f2[2];
            return f1;
        });
        midPoint[0] /= indices.size();
        midPoint[1] /= indices.size();
        midPoint[2] /= indices.size();

        double mid = Math.sqrt(Math.pow(midPoint[0], 2) + Math.pow(midPoint[1], 2) + Math.pow(midPoint[2], 2));

        ArrayList<int[]> sortedI = indices.stream().sorted(Comparator.comparingDouble(i -> distanceColor(i, image, mid))).collect(Collectors.toCollection(ArrayList::new));

        double averageDist = indices.parallelStream().map(ints -> Math.pow(distanceColor(ints, image, mid)*2, 2)).reduce(0.0, Double::sum);
        averageDist /= indices.size();

        float[] centerlow = new float[2];
        float[] centerHigh = new float[2];

        for (int i = 0; i < sortedI.size()/2; i++){
            centerlow[0] += sortedI.get(i)[0];
            centerlow[1] += sortedI.get(i)[1];
        }
        centerlow[0] /= sortedI.size()/2.0;
        centerlow[1] /= sortedI.size()/2.0;

        for (int i = sortedI.size()/2; i < sortedI.size(); i++){
            centerHigh[0] += sortedI.get(i)[0];
            centerHigh[1] += sortedI.get(i)[1];
        }
        centerHigh[0] /= sortedI.size()/2.0;
        centerHigh[1] /= sortedI.size()/2.0;

        cL[0] = centerlow[0];
        cL[1] = centerlow[1];

        cH[0] = centerHigh[0];
        cH[1] = centerHigh[1];

        return averageDist;
    }

    public static double distanceColor(int[] i, float[][][] image, double mid){
        return Math.sqrt(Math.pow(image[i[0]][i[1]][0], 2) + Math.pow(image[i[0]][i[1]][1], 2) + Math.pow(image[i[0]][i[1]][2], 2))-mid;
    }

    public static double spaceDistance(int[] i, float[] center){
        return Math.sqrt(Math.pow(i[0]-center[0], 2) + Math.pow(i[1]-center[1], 2));
    }
}
