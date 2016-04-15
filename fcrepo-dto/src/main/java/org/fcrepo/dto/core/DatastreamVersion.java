package org.fcrepo.dto.core;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fcrepo.dto.core.io.DateUtil;

/**
 * A particular revision of a {@link Datastream}.
 *
 * @see <a href="package-summary.html#working">Working With DTO Classes</a>
 */
public class DatastreamVersion extends FedoraDTO {

    private final SortedSet<URI> altIds = new TreeSet<>();

    private final String id;
    private final LocalDateTime createdDate;

    private String label;
    private String mimeType;
    private URI formatURI;
    private Long size;
    private ContentDigest contentDigest;
    private InlineXML inlineXML;
    private URI contentLocation;

    /**
     * Creates an instance.
     *
     * @param id the id of the version (not null, immutable).
     * @param createdDate the date the version was created
     *        (possibly null, immutable).
     * @throws NullPointerException if id is given as <code>null</code>.
     */
    public DatastreamVersion(final String id, final LocalDateTime createdDate) {
        this.id = Util.normalize(id);
        if (this.id == null) {
            throw new NullPointerException();
        }
        this.createdDate = createdDate;
    }

    /**
     * Creates an instance based on the current state of this one.
     *
     * @return a deep copy.
     */
    public DatastreamVersion copy() {
        final DatastreamVersion copy = new DatastreamVersion(id, createdDate)
                .label(label)
                .mimeType(mimeType)
                .formatURI(formatURI)
                .size(size)
                .contentDigest(contentDigest == null ? null : contentDigest.copy())
                .inlineXML(inlineXML)
                .contentLocation(contentLocation);
        copy.altIds().addAll(altIds);
        return copy;
    }

    /**
     * Creates an instance based on the current state of this one, but with a
     * different id and createdDate.
     *
     * @param id the id of the version (not null, immutable).
     * @param createdDate the date the version was created
     *        (possibly null, immutable).
     * @return a deep copy.
     * @throws NullPointerException if id is given as <code>null</code>.
     */
    public DatastreamVersion copy(final String id, final LocalDateTime createdDate) {
        final DatastreamVersion copy = new DatastreamVersion(id, createdDate)
                .label(label)
                .mimeType(mimeType)
                .formatURI(formatURI)
                .size(size)
                .contentDigest(contentDigest == null ? null : contentDigest.copy())
                .inlineXML(inlineXML)
                .contentLocation(contentLocation);
        copy.altIds().addAll(altIds);
        return copy;
    }

    /**
     * Gets the id.
     *
     * @return the value, never <code>null</code>.
     */
    public String id() {
        return id;
    }

    /**
     * Gets the label.
     *
     * @return the value, possibly <code>null</code>.
     */
    public String label() {
        return label;
    }

    /**
     * Sets the label.
     *
     * @param label the new value, which will be string-normalized.
     * @return this instance.
     */
    public DatastreamVersion label(final String label) {
        this.label = Util.normalize(label);
        return this;
    }

    /**
     * Gets the created date.
     *
     * @return the value, possibly <code>null</code>.
     */
    public LocalDateTime createdDate() {
        return createdDate;
    }

    /**
     * Gets the mime type of the content.
     *
     * @return the value, possibly <code>null</code>.
     */
    public String mimeType() {
        return mimeType;
    }

    /**
     * Sets the mime type of the content.
     *
     * @param mimeType the new value, which will be string-normalized.
     * @return this instance.
     */
    public DatastreamVersion mimeType(final String mimeType) {
        this.mimeType = Util.normalize(mimeType);
        return this;
    }

    /**
     * Gets the (mutable) set of alternate ids. Iterators over the elements
     * of the set will provide the values in ascending alphabetical order.
     *
     * @return the set, possibly empty, never <code>null</code>.
     */
    public SortedSet<URI> altIds() {
        return altIds;
    }

    /**
     * Gets the format URI.
     *
     * @return the value, possibly <code>null</code>.
     */
    public URI formatURI() {
        return formatURI;
    }

    /**
     * Sets the format URI.
     *
     * @param formatURI the new value, possibly <code>null</code>.
     * @return this instance.
     */
    public DatastreamVersion formatURI(final URI formatURI) {
        this.formatURI = formatURI;
        return this;
    }

    /**
     * Gets the content digest.
     *
     * @return the value, possibly <code>null</code>.
     */
    public ContentDigest contentDigest() {
        return contentDigest;
    }

    /**
     * Sets the content digest.
     *
     * @param contentDigest the new value, possibly <code>null</code>.
     * @return this instance.
     */
    public DatastreamVersion contentDigest(final ContentDigest contentDigest) {
        this.contentDigest = contentDigest;
        return this;
    }

    /**
     * Gets the size of the content, in bytes.
     *
     * @return the value, possibly <code>null</code>.
     */
    public Long size() {
        return size;
    }

    /**
     * Sets the size of the content, in bytes.
     *
     * @param size the new value, 0, positive, negative, or <code>null</code>.
     * @return this instance.
     */
    public DatastreamVersion size(final Long size) {
        this.size = size;
        return this;
    }

    /**
     * Gets the inline XML content.
     *
     * @return the value, possibly <code>null</code>.
     */
    public InlineXML inlineXML() {
        return inlineXML;
    }

    /**
     * Sets the inline XML content.
     *
     * @param inlineXML the new value, possibly <code>null</code>.
     * @return this instance.
     */
    public DatastreamVersion inlineXML(final InlineXML inlineXML) {
        this.inlineXML = inlineXML;
        return this;
    }

    /**
     * Gets the content location.
     *
     * @return the value, possibly <code>null</code>.
     */
    public URI contentLocation() {
        return contentLocation;
    }

    /**
     * Sets the content location.
     *
     * @param contentLocation the new value, possibly <code>null</code>.
     * @return this instance.
     */
    public DatastreamVersion contentLocation(final URI contentLocation) {
        this.contentLocation = contentLocation;
        return this;
    }

    @Override
    Object[] getEqArray() {
        return new Object[] {
                id,
                label,
                DateUtil.toString(createdDate),
                mimeType,
                formatURI,
                contentDigest,
                size,
                inlineXML,
                contentLocation,
                altIds };
    }

}
