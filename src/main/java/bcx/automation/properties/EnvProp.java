package bcx.automation.properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classe utilitaire pour charger et gérer les propriétés d'environnement.
 */
@Slf4j
public class EnvProp {
    private static String environnement;
    @Getter
    private static String defaultSchema;
    @Getter
    private static String allBddSchemas; // Séparé par des virgules

    /**
     * Charge les propriétés à partir d'un fichier de configuration spécifique à l'environnement.
     *
     * @param env L'environnement pour lequel charger les propriétés.
     */
    public static void loadProperties(String env) {
        environnement = env;
        if (env != null) {
            final java.util.Properties prop = new java.util.Properties();
            try (InputStream input = new FileInputStream("target/test-classes/test_" + env + ".properties")) {
                prop.load(input);
                allBddSchemas = prop.getProperty("allBddSchemas");
                defaultSchema = prop.getProperty("defaultSchema");
            } catch (final IOException ex) {
                log.error("Exception lors du chargement des variables globales", ex);
            }
        }
    }

    /**
     * Renvoie l'environnement actuel.
     *
     * @return L'environnement actuel.
     */
    public static String getEnvironnement() {
        return String.valueOf(environnement);
    }

    /**
     * Renvoie la valeur d'une propriété spécifique à partir du fichier de configuration de l'environnement actuel.
     *
     * @param key La clé de la propriété à récupérer.
     * @return La valeur de la propriété ou null si non trouvée.
     */
    public static String get(String key) {
        return get(environnement, key);
    }

    /**
     * Renvoie la valeur d'une propriété spécifique à partir du fichier de configuration d'un environnement donné.
     *
     * @param env L'environnement pour lequel récupérer la propriété.
     * @param key La clé de la propriété à récupérer.
     * @return La valeur de la propriété ou null si non trouvée.
     */
    public static String get(String env, String key) {
        final java.util.Properties prop = new java.util.Properties();
        String value = null;
        try (InputStream input = new FileInputStream("target/test-classes/test_" + env + ".properties")) {
            prop.load(input);
            value = prop.getProperty(key);
        } catch (Exception e) {
            log.error("Exception lors du chargement de la valeur de la variable globale " + key, e);
        }
        return value;
    }
}
