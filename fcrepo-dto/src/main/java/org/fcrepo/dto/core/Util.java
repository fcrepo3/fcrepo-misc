package org.fcrepo.dto.core;

import java.io.UnsupportedEncodingException;

/** Package-private utility methods. */
final class Util {

    private Util() { }

    static String normalize(String string) {
        if (string != null) {
            string = string.trim();
            if (string.length() == 0) {
                string = null;
            }
        }
        return string;
    }

    static byte[] getBytes(final String string) {
        if (string == null) return null;
        try {
            return string.getBytes("UTF-8");
        } catch (final UnsupportedEncodingException wontHappen) {
            throw new RuntimeException(wontHappen);
        }
    }

    static String getString(final byte[] bytes) {
        if (bytes == null) return null;
        try {
            return new String(bytes, "UTF-8");
        } catch (final UnsupportedEncodingException wontHappen) {
            throw new RuntimeException(wontHappen);
        }
    }

}
