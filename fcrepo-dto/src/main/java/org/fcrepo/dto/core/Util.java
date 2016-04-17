
package org.fcrepo.dto.core;

/** Package-private utility methods. */
final class Util {

    private Util() {}

    static String normalize(final String string) {
        if (string == null) return null;
        final String trimmedString = string.trim();
        return trimmedString.isEmpty()
                        ? null : trimmedString;
    }

}
