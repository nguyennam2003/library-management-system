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
        assertTrue(xml.contains("com.ptit.library.web.StaffCatalogServlet"));
        assertTrue(xml.contains("<url-pattern>/staff/catalog</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.StaffBookServlet"));
        assertTrue(xml.contains("<url-pattern>/staff/books</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.StaffBookCopyServlet"));
        assertTrue(xml.contains("<url-pattern>/staff/books/copies</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.ReaderBookServlet"));
        assertTrue(xml.contains("<url-pattern>/reader/books</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.ReaderLoanServlet"));
        assertTrue(xml.contains("<url-pattern>/reader/my-loans</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.ChangePasswordServlet"));
        assertTrue(xml.contains("<url-pattern>/account/password</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.StaffReaderApprovalServlet"));
        assertTrue(xml.contains("<url-pattern>/staff/readers/pending</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.StaffReaderServlet"));
        assertTrue(xml.contains("<url-pattern>/staff/readers</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.StaffLoanCreateServlet"));
        assertTrue(xml.contains("<url-pattern>/staff/loans/new</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.StaffLoanReturnServlet"));
        assertTrue(xml.contains("<url-pattern>/staff/loans</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.StaffOverdueServlet"));
        assertTrue(xml.contains("<url-pattern>/staff/overdue</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.StaffDashboardServlet"));
        assertTrue(xml.contains("<url-pattern>/staff/dashboard</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.AdminStaffUserServlet"));
        assertTrue(xml.contains("<url-pattern>/admin/staff-users</url-pattern>"));
        assertTrue(xml.contains("com.ptit.library.web.AdminSettingsServlet"));
        assertTrue(xml.contains("<url-pattern>/admin/settings</url-pattern>"));
    }
}
