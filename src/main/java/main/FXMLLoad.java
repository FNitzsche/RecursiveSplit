package main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class FXMLLoad {

    private static ArrayList<FXMLLoad> fxmlLoads = new ArrayList<>();
    private final String defaultprop = "en-US.properties";
    private Scene scene = null;
    private FXMLLoader fxmlLoader = null;
    private Parent parent;
    private String path;
    private ResourceBundle resourceBundle;


    public FXMLLoad(String fxmlpath, Object controller, boolean list) {
        loadFXML(fxmlpath, defaultprop, controller);
        if (list) {
            fxmlLoads.add(this);
        }
    }


    /**
     * Constructs a Instance with a fxml and adds the default properties file
     *
     * @param fxmlpath
     */
    public FXMLLoad(String fxmlpath) {
        loadFXML(fxmlpath, defaultprop, null);
        if (getController() != null) {
            fxmlLoads.add(this);
        }
    }

    /**
     * Constructs a Instance with a fxml and adds a custom properties file
     *
     * @param fxmlpath
     * @param propertiespath
     */
    public FXMLLoad(String fxmlpath, String propertiespath) {
        loadFXML(fxmlpath, propertiespath, null);
        if (getController() != null) {
            fxmlLoads.add(this);
        }
    }

    /**
     * Constructs a Instance with a fxml and adds a custom properties file and sets the given controller
     *
     * @param fxmlpath
     * @param propertiespath
     * @param controller
     */
    public FXMLLoad(String fxmlpath, String propertiespath, Object controller) {
        loadFXML(fxmlpath, propertiespath, controller);
        fxmlLoads.add(this);
    }

    /**
     * Constructs a Instance with a fxml and add the default properties file and sets the given controller
     *
     * @param fxmlpath
     * @param controller
     */
    public FXMLLoad(String fxmlpath, Object controller) {
        loadFXML(fxmlpath, defaultprop, controller);
        fxmlLoads.add(this);
    }

    /**
     * get a list of the FXMLLoads using the given controller
     *
     * @param con
     * @return a list of FXMLLoads
     */
    static public ArrayList<FXMLLoad> getFXMLFor(Object con) {
        ArrayList<FXMLLoad> loaders = new ArrayList<>();
        for (FXMLLoad fxmlLoad : fxmlLoads) {
            if (fxmlLoad.fxmlLoader.getController() == con) {
                loaders.add(fxmlLoad);
            }
        }
        return loaders;
    }

    /**
     * Load the fxml and properties from the given paths
     *
     * @param fxmlpath
     * @param propertiespath
     * @return if the loading was successful
     */
    private boolean loadFXML(String fxmlpath, String propertiespath, Object controller) {
        System.out.println("Path to try: " + fxmlpath);
        System.out.println("Path to properties: " + propertiespath);
        boolean success = true;

        if ((fxmlpath == null) || (propertiespath == null)) {
            return false;
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = null;
        ResourceBundle bundle = null;
        URL loc = null;
        try {
            System.out.println(classLoader.getResource(propertiespath).toString());
            inputStream = classLoader.
                    getResource(propertiespath).
                    openStream();
            bundle = new PropertyResourceBundle(inputStream);
            resourceBundle = bundle;
            loc = getClass().getResource(fxmlpath);
        } catch (IOException e) {
            System.out.println("Path tried: " + fxmlpath);
            e.printStackTrace();
            success = false;
        }

        if ((loc == null) || (bundle == null)) {
            return false;
        }

        fxmlLoader = new FXMLLoader(loc, bundle);
        if ((controller != null)) {
            fxmlLoader.setController(controller);
        }

        parent = null;
        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }
        scene = new Scene(parent);
        path = fxmlpath;
        return success;
    }

    /**
     * @param tClass the class of the controller
     * @return the controller
     */
    public <T> T getController(Class<T> tClass) {
        return fxmlLoader.getController();
    }

    /**
     * @return the controller
     */
    public Object getController() {
        return fxmlLoader.getController();
    }

    /**
     * @return the Scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * delets the FXMLLoad from the List
     */
    public void delete() {
        int ind = fxmlLoads.indexOf(this);
        fxmlLoads.remove(ind);
    }

    public Parent getParent() {
        return parent;
    }

    /**
     * @return the fxmlpath
     */
    public String getPath() {
        return path;
    }


    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}
