package splitting;

import main.AppStart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public class Line {

    static Random rnd = new Random();

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

    public void setParams(ArrayList<int[]> indices, float[][][] image){
        allIndices = indices;
        color = allIndices.stream().map(ints -> image[ints[0]][ints[1]]).reduce(new float[3], (f1, f2)-> {
            f1[0] += f2[0];
            f1[1] += f2[1];
            f1[2] += f2[2];
            return f1;
        });
        color[0] /= allIndices.size();
        color[1] /= allIndices.size();
        color[2] /= allIndices.size();
    }

    public void devide(float[][][] image, float minDist){
        lod = fitLine(cL, cH, allIndices, image);
        closeEnough = (lod < minDist);
        sectionOneIndices = allIndices.parallelStream().filter(ints -> spaceDistance(ints, cL) < spaceDistance(ints, cH)).collect(Collectors.toCollection(ArrayList::new));
        sectionTwoIndices = allIndices.parallelStream().filter(ints -> spaceDistance(ints, cL) >= spaceDistance(ints, cH)).collect(Collectors.toCollection(ArrayList::new));
    }

    public void children(int genLeft, float[][][] image, float minDist){
        if (genLeft > 0 && !closeEnough) {
            sectionOne = new Line();
            sectionTwo = new Line();
            sectionOne.setParams(sectionOneIndices, image);
            sectionTwo.setParams(sectionTwoIndices, image);
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
        float[] cluster1 = {rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat()};
        float[] cluster2 = {rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat()};


        for (int i = 0; i < 5; i++){
            int cl1Count = 0;
            int cl2Count = 0;
            float[] cl1Tmp = new float[3];
            float[] cl2Tmp = new float[3];
            for (int[] index: indices){
                if (unsignedDistanceColor(image[index[0]][index[1]], cluster1) < unsignedDistanceColor(image[index[0]][index[1]], cluster2)){
                    cl1Tmp[0] += image[index[0]][index[1]][0];
                    cl1Tmp[1] += image[index[0]][index[1]][1];
                    cl1Tmp[2] += image[index[0]][index[1]][2];
                    cl1Count++;
                } else {
                    cl2Tmp[0] += image[index[0]][index[1]][0];
                    cl2Tmp[1] += image[index[0]][index[1]][1];
                    cl2Tmp[2] += image[index[0]][index[1]][2];
                    cl2Count++;
                }
            }

            if (cl1Count > 0) {
                cluster1[0] = cl1Tmp[0] / cl1Count;
                cluster1[1] = cl1Tmp[1] / cl1Count;
                cluster1[2] = cl1Tmp[2] / cl1Count;
            }

            if (cl2Count > 0) {
                cluster2[0] = cl2Tmp[0] / cl2Count;
                cluster2[1] = cl2Tmp[1] / cl2Count;
                cluster2[2] = cl2Tmp[2] / cl2Count;
            }

        }


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

        //ArrayList<int[]> sortedI = indices.stream().sorted(Comparator.comparingDouble(i -> distanceColor(i, image, mid))).collect(Collectors.toCollection(ArrayList::new));

        double averageDist = indices.parallelStream().map(ints -> Math.pow((distanceColor(ints, image, mid)*4), 4)).reduce(0.0, Double::sum);
        averageDist /= Math.sqrt(indices.size())*Math.min(indices.size(), 50);

        float[] centerlow = new float[2];
        float[] centerHigh = new float[2];

        /*for (int i = 0; i < sortedI.size()/2; i++){
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
        centerHigh[1] /= sortedI.size()/2.0;*/

        int clCount = 0;
        int chCount = 0;

        for (int[] index: indices){
            if (unsignedDistanceColor(image[index[0]][index[1]], cluster1) < unsignedDistanceColor(image[index[0]][index[1]], cluster2)){
                centerlow[0] += index[0];
                centerlow[1] += index[1];
                clCount++;
            } else {
                centerHigh[0] += index[0];
                centerHigh[1] += index[1];
                chCount++;
            }
        }


        if (clCount > 0) {
            cL[0] = centerlow[0] / clCount;
            cL[1] = centerlow[1] / clCount;
        }

        if (chCount > 0) {
            cH[0] = centerHigh[0] / chCount;
            cH[1] = centerHigh[1] / chCount;
        }

        return averageDist;
    }

    public static double distanceColor(int[] i, float[][][] image, double mid){
        return Math.sqrt(Math.pow(image[i[0]][i[1]][0], 2) + Math.pow(image[i[0]][i[1]][1], 2) + Math.pow(image[i[0]][i[1]][2], 2))-mid;
    }

    public static double unsignedDistanceColor(float[] color1, float[] color2){
        return Math.sqrt(Math.pow(color1[0]-color2[0], 2) + Math.pow(color1[1]-color2[1], 2) + Math.pow(color1[2]-color2[2], 2));
    }

    public static double spaceDistance(int[] i, float[] center){
        return Math.sqrt(Math.pow(i[0]-center[0], 2) + Math.pow(i[1]-center[1], 2));
    }
}
