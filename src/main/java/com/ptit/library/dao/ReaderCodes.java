package com.ptit.library.dao;

public final class ReaderCodes {

    private ReaderCodes() {
    }

    public static String formatReaderCode(long sequence) {
        if (sequence < 1 || sequence > 999999) {
            throw new IllegalArgumentException("reader sequence out of range: " + sequence);
        }
        return String.format("DG%06d", sequence);
    }
}
