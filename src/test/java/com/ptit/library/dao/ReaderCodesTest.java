package com.ptit.library.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReaderCodesTest {

    @Test
    void formatsSixDigitCode() {
        assertEquals("DG000001", ReaderCodes.formatReaderCode(1));
        assertEquals("DG000003", ReaderCodes.formatReaderCode(3));
        assertEquals("DG123456", ReaderCodes.formatReaderCode(123456));
    }

    @Test
    void rejectsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> ReaderCodes.formatReaderCode(0));
    }
}
