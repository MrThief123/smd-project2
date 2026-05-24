package thrones.game;

import ch.aplu.jgamegrid.Actor;
import thrones.game.utility.PropertiesLoader;

import java.awt.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public class Driver {
    public static final String DEFAULT_PROPERTIES_PATH = "properties/game3.properties";

    /**
     * Starting point
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        String propertiesPath = DEFAULT_PROPERTIES_PATH;
        if (args.length > 0) {
            propertiesPath = args[0];
        }
        final Properties properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
        GameOfThrones gameOfThrones = new GameOfThrones(properties);
        String logResult = gameOfThrones.runApp();
        System.out.println("logResult = " + logResult);
    }
}