package com.gregory.kafka;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class KafkaWebClient {

    private static final int PORT = 7080;

    private KafkaWebClient() {
    }

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setConnector(httpConnector());
        tomcat.getService().addConnector(tomcat.getConnector());

        tomcat.addWebapp("", webappPath());

        tomcat.start();
        tomcat.getServer().await();
    }

    private static Connector httpConnector() throws Exception {
        Connector connector = new Connector("HTTP/1.1");
        connector.setPort(PORT);
        connector.setSecure(false);
        connector.setScheme("http");
        return connector;
    }

    private static String webappPath() throws IOException {
        String webappPath = Files.createTempDirectory("KafkaWebsocket").toFile().getAbsolutePath();
        copyWebappResources(
                webappPath,
                "bootstrap.min.css",
                "bootstrap.min.js",
                "bootstrap-theme.min.css",
                "index.html",
                "jquery.min.js",
                "knockout-min.js",
                "styles.css",
                "web.xml");
        return webappPath;
    }

    private static void copyWebappResources(String webapp, String... resources) throws IOException {
        for (String resource : resources) {
            copyWebappResource(webapp, resource);
        }
    }

    private static void copyWebappResource(String webapp, String resource) throws IOException {
        IOUtils.copy(
                resourceStream(resource),
                new FileOutputStream(Paths.get(webapp, resource).toFile()));
    }

    private static InputStream resourceStream(String resource) {
        String path = "/webapp/" + resource;
        return KafkaWebClient.class.getResourceAsStream(path);
    }

}
