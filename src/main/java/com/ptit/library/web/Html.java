package com.ptit.library.web;

// escape ký tự đặc biệt khi in ra JSP để tránh lỗi XSS
public final class Html {

    private Html() {
    }

    public static String esc(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
