package com.gregory.kafka;

import java.io.IOException;
import java.util.Properties;

final class Utils {

    private Utils() {
    }

    static Properties loadProperties(String file) throws IOException {
        Properties props = new Properties();
        props.load(Utils.class.getResourceAsStream("/"+file));
        return props;
    }
}
