package com.jrodolfo.elasticsearch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class PropertyUtil {

    private static Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static {
        properties = new Properties();
        InputStream inputStream = null;
        try {
            final String propFileName = "app.properties";
            inputStream = MethodHandles.lookup().lookupClass().getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new FileNotFoundException("Property file '" + propFileName + "' was not found in the classpath.");
            }
        } catch (Exception e) {
            logger.error("Exception: " + e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}
