package bcx.playwright.properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class EnvProp {
    private static String environnement;
    @Getter
    private static String defaultSchema;
    @Getter
    private static String allBddSchemas; //séparé par des ","



    public static void loadProperties(String env) {
        environnement = env;
        if (env != null) {
            final java.util.Properties prop = new java.util.Properties();
            try (InputStream input = new FileInputStream("target/test-classes/test_" + env + ".properties")) {
                prop.load(input);
                allBddSchemas = prop.getProperty("allBddSchemas");
                defaultSchema = prop.getProperty("defaultSchema");
            } catch (final IOException ex) {
                log.error("exception lors du chargement des variables globles", ex);
            }
        }

    }

    public static String getEnvironnement() {
        return String.valueOf(environnement);
    }

    /**
     * renvoie la valeur d'une propertie key du fichier test_environnement.properties
     * @param key extension du properties : test_extension.properties (correspond au propfile)
     * @return la valeur d'une propertie key du fichier test_environnement.properties
     */
    public static String get(String key)  {
        return get(environnement, key);
    }

    /**
     * renvoie la valeur d'une propertie key du fichier test_environnement.properties
     * @param key extension du properties : test_extension.properties (correspond au propfile)
     * @return la valeur d'une propertie key du fichier test_environnement.properties
     */
    public static String get(String env, String key)  {
        final java.util.Properties prop = new java.util.Properties();
        String value=null;
        try (InputStream input = new FileInputStream("target/test-classes/test_" + env + ".properties")) {
            prop.load(input);
            value = prop.getProperty(key);
        } catch (Exception e) {
            log.error("exception lors du chargement valeur de la variable globale " + key, e) ;
        }
        return value;
    }


}