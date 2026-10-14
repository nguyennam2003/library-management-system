package com.ptit.library.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebXmlTest {

    private static String xml;

    @BeforeAll
    static void load() throws IOException {
        xml = Files.readString(Path.of("src/main/webapp/WEB-INF/web.xml"), StandardCharsets.UTF_8);
    }

    @Test
    void declaresServlet40SessionHealthAndEncoding() {
        assertTrue(xml.contains("version=\"4.0\""));
        assertTrue(xml.contains("<session-timeout>30</session-timeout>"));
        assertTrue(xml.contains("com.ptit.library.web.HealthServlet"));
        assertTrue(xml.contains("<url-pattern>/health</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.CharacterEncodingFilter"));
        assertTrue(xml.contains("com.ptit.library.web.AppContextListener"));
        assertTrue(xml.contains("<url-pattern>/*</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.AuthFilter"));
        assertTrue(xml.contains("com.ptit.library.web.LoginServlet"));
        assertTrue(xml.contains("<url-pattern>/login</url-pattern>"));
        assertTrue(xml.contains("<url-pattern>/logout</url-pattern>"));
        assertTrue(xml.contains("<url-pattern>/register</url-pattern>"));
        assertTrue(xml.contains("<url-pattern>/home</url-pattern>"));
        assertTrue(xml.contains("<url-pattern>/staff/home</url-pattern>"));
        assertTrue(xml.contains("<url-pattern>/reader/home</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.StaffHomeServlet"));
        assertTrue(xml.contains("com.ptit.library.web.ReaderHomeServlet"));
    }
}
