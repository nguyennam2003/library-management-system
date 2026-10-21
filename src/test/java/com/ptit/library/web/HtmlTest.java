package com.ptit.library.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HtmlTest {

    @Test
    void escapesSpecialCharacters() {
        assertEquals("&lt;b&gt;&amp;&quot;", Html.esc("<b>&\""));
    }

    @Test
    void nullBecomesEmpty() {
        assertEquals("", Html.esc(null));
    }
}
