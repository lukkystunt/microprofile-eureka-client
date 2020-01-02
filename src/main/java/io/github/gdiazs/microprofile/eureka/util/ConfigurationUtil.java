package io.github.gdiazs.microprofile.eureka.util;



import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class ConfigurationUtil {

    public static Properties loadCascadedProperties(String configName) {

        Properties props = new Properties();
        String defaultConfigFileName = configName + ".properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(defaultConfigFileName);
        if (url == null) {
            System.out.println("Cannot locate " + defaultConfigFileName + " as a classpath resource.");
            return props;
        }

        try {
            props.load(url.openStream());
        } catch (IOException e) {
            System.out.println("Error while loading properties from " + defaultConfigFileName + ": " + e.getMessage());
        }

        System.out.println("final properties: \n" + props.values());

        return props;
    }

}
