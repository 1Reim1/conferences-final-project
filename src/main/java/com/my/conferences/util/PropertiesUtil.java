package com.my.conferences.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertiesUtil {

    private PropertiesUtil() {}

    public static Properties loadFromResources(String fileName) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = loader.getResourceAsStream(fileName);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        }
    }
}
