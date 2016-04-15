package org.fcrepo.dto.core;

import static java.nio.charset.StandardCharsets.UTF_8;

/** Package-private utility methods. */
final class Util {

    private Util() { }

    static String normalize(String string) {
        if (string != null) {
            string = string.trim();
            if (string.isEmpty()) return null;
        }
        return string;
    }

    static byte[] getBytes(final String string) {
        return string == null ? null : string.getBytes(UTF_8);
    }

    static String getString(final byte[] bytes) {
        return bytes == null ? null : new String(bytes, UTF_8);
    }

}
