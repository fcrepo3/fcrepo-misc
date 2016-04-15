package org.fcrepo.dto.core.io;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Date-related utility methods.
 */
public final class DateUtil {

    private DateUtil() { }

    /**
     * The preferred date format, output by {@link #toString(java.time.LocalDateTime)}
     * and checked first for parsing via {@link #toDate(String)}.
     */
    public static final String PREFERRED_DATE_FORMAT =
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final DateTimeFormatter PREFERRED_DATE_FORMATTER = DateTimeFormatter.ofPattern(PREFERRED_DATE_FORMAT);

    /**
     * Acceptable date formats for parsing via {@link #toDate(String)}.
     */
    public static final String[] ALLOWED_DATE_FORMATS = new String[] {
            PREFERRED_DATE_FORMAT,
            "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.S'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SS",
            "yyyy-MM-dd'T'HH:mm:ss.S",
            "yyyy-MM-dd'T'HH:mm:ss"
    };

    /**
     * Gets a UTC <code>LocalDate</code> from a UTC ISO-8601 <code>String</code>.
     * <p>
     * The string should be in one of the {@link #ALLOWED_DATE_FORMATS}, ideally {@link #PREFERRED_DATE_FORMAT} (e.g.
     * <code>2011-01-16T08:27:01.002Z</code>).
     *
     * @param dateString the string to parse.
     * @return the date if parsing was successful, or <code>null</code> if if dateString was given as null or parsing
     *         was unsuccessful for any other reason.
     */
    public static LocalDateTime toDate(final String dateString) {
        if (dateString == null) return null;
        for (final String format: ALLOWED_DATE_FORMATS) {
            try {
                return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(format));
            } catch (final DateTimeParseException e) { /* try next format */ }
        }
        return null;
    }

    /**
     * Gets a UTC ISO-8601 <code>String</code> from a given <code>Date</code>.
     * <p>
     * The string will be in the {@link #PREFERRED_DATE_FORMAT}
     * (e.g. <code>2011-01-16T08:27:01.002Z</code>).
     *
     * @param date the date.
     * @return the string, or <code>null</code> if the date was given as null.
     */
    public static String toString(final LocalDateTime date) {
        if (date == null) return null;
        return PREFERRED_DATE_FORMATTER.format(date);
    }

}
