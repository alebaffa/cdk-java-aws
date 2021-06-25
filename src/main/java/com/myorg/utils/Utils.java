package com.myorg.utils;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Utils {

    public static Properties getProperties() {
        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("aws.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
