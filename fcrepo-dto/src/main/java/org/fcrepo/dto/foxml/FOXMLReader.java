
package org.fcrepo.dto.foxml;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.fcrepo.dto.core.ContentDigest;
import org.fcrepo.dto.core.ControlGroup;
import org.fcrepo.dto.core.Datastream;
import org.fcrepo.dto.core.DatastreamVersion;
import org.fcrepo.dto.core.FedoraObject;
import org.fcrepo.dto.core.InlineXML;
import org.fcrepo.dto.core.State;
import org.fcrepo.dto.core.io.ContentHandlingDTOReader;
import org.fcrepo.dto.core.io.DTOReader;
import org.fcrepo.dto.core.io.DateUtil;
import org.fcrepo.dto.core.io.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.fcrepo.dto.foxml.Constants.CREATED;
import static org.fcrepo.dto.foxml.Constants.INTERNALREF_SCHEME;
import static org.fcrepo.dto.foxml.Constants.INTERNALREF_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link DTOReader} that reads Fedora Object XML.
 * <p>
 * <b>NOTE:</b> Only FOXML version 1.1 is supported.
 *
 * @see <a href="https://wiki.duraspace.org/x/fABI">Introduction to FOXML</a>
 * @see <a href="http://fedora-commons.org/definitions/1/0/foxml1-1.xsd">FOXML 1.1 XML Schema</a>
 */
public class FOXMLReader extends ContentHandlingDTOReader {

    private static final Logger logger = LoggerFactory.getLogger(FOXMLReader.class);

    private FedoraObject obj;

    private XMLStreamReader r;

    /**
     * Creates an instance.
     */
    public FOXMLReader() {}

    @Override
    public DTOReader getInstance() {
        final FOXMLReader reader = new FOXMLReader();
        if (contentHandler != defaultContentHandler) {
            reader.setContentHandler(contentHandler);
        }
        return reader;
    }

    @Override
    public FedoraObject readObject(final InputStream source) throws IOException {
        obj = new FedoraObject();
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            r = factory.createXMLStreamReader(source, Constants.CHAR_ENCODING);
            readObject();
            return obj;
        } catch (final XMLStreamException e) {
            throw new IOException(e);
        } finally {
            XMLUtil.closeQuietly(r);
            source.close();
        }
    }

    private void readObject() throws IOException, XMLStreamException {
        if (moveToStart(Constants.digitalObject, null)) {
            obj.pid(readAttribute(Constants.PID));
            readObjectProperties();
            while (r.getEventType() == XMLStreamConstants.START_ELEMENT &&
                            r.getLocalName().equals(Constants.datastream)) {
                readDatastream();
            }
        }
    }

    private void readObjectProperties() throws XMLStreamException {
        while (moveToStart(Constants.property, Constants.datastream)) {
            final String name = readAttribute(Constants.NAME);
            final String value = readAttribute(Constants.VALUE);
            if (name != null) {
                if (name.equals(Constants.STATE_URI)) {
                    obj.state(parseState(value, "object"));
                } else if (name.equals(Constants.LABEL_URI)) {
                    obj.label(value);
                } else if (name.equals(Constants.OWNERID_URI)) {
                    obj.ownerId(value);
                } else if (name.equals(Constants.CREATEDDATE_URI)) {
                    obj.createdDate(parseDate(value, "object created"));

                } else if (name.equals(Constants.LASTMODIFIEDDATE_URI)) {
                    obj.lastModifiedDate(parseDate(value, "object last modified"));
                } else {
                    logger.warn("Ignoring unrecognized object property name: " + name);
                }
            }
        }
    }

    private void readDatastream() throws IOException, XMLStreamException {
        final String id = readAttribute(Constants.ID);
        if (id != null) {
            final Datastream ds = new Datastream(obj.pid(), id);
            obj.putDatastream(ds);
            ds.state(parseState(readAttribute(Constants.STATE), "datastream"));
            ds.controlGroup(parseControlGroup(readAttribute(Constants.CONTROL_GROUP)));
            ds.versionable(parseVersionable(readAttribute(Constants.VERSIONABLE)));
            while (moveToStart(Constants.datastreamVersion, Constants.datastream)) {
                readDatastreamVersion(ds);
            }
        } else {
            logger.warn("Ignoring datastream; no id specified");
        }
    }

    private void readDatastreamVersion(final Datastream ds) throws IOException, XMLStreamException {
        final String id = readAttribute(Constants.ID);
        if (id != null) {
            final LocalDateTime created = parseDate(readAttribute(CREATED), "datastream created");
            final DatastreamVersion dsv = new DatastreamVersion(id, created);
            ds.versions().add(dsv);
            dsv.altIds().addAll(parseAltIds(readAttribute(Constants.ALT_IDS)));
            dsv.label(readAttribute(Constants.LABEL));
            dsv.mimeType(readAttribute(Constants.MIMETYPE));
            dsv.formatURI(parseURI(readAttribute(Constants.FORMAT_URI)));
            dsv.size(parseLong(readAttribute(Constants.SIZE), "datastream size"));
            if (r.nextTag() == XMLStreamConstants.START_ELEMENT) {
                if (r.getLocalName().equals(Constants.contentDigest)) {
                    readContentDigest(dsv);
                    if (r.nextTag() == XMLStreamConstants.END_ELEMENT) { return; // end of datastreamVersion
                    }
                }
                if (r.getLocalName().equals(Constants.xmlContent)) {
                    readXMLContent(dsv);
                } else if (r.getLocalName().equals(Constants.binaryContent)) {
                    readBinaryContent(ds, dsv);
                } else if (r.getLocalName().equals(Constants.contentLocation)) {
                    readContentLocation(dsv);
                }
            }
        } else {
            logger.warn("Ignoring datastream version; no id specified");
        }
    }

    private void readContentLocation(final DatastreamVersion dsv) {
        final String type = readAttribute(Constants.TYPE);
        final URI ref = parseURI(readAttribute(Constants.REF));
        if (ref != null) {
            if (INTERNALREF_TYPE.equals(type)) dsv.contentLocation(URI.create(INTERNALREF_SCHEME + ":" + ref));
            else dsv.contentLocation(ref);
        }
    }

    private void readBinaryContent(final Datastream ds, final DatastreamVersion dsv)
                    throws IOException, XMLStreamException {
        try (final OutputStream sink = contentHandler.handleContent(obj, ds, dsv);
                        final OutputStream out = new Base64OutputStream(sink, false)) {
            if (sink == null) return; // handler opted out
            while (r.next() != XMLStreamConstants.END_ELEMENT) {
                if (r.isCharacters()) {
                    out.write(r.getText().getBytes(Constants.CHAR_ENCODING));
                }
            }
            out.flush();
        }
    }

    private void readXMLContent(final DatastreamVersion dsv) throws IOException, XMLStreamException {
        while (r.next() != XMLStreamConstants.START_ELEMENT)
            if (r.getEventType() == XMLStreamConstants.END_ELEMENT)  return; // xmlContent element is empty
        final ByteArrayOutputStream sink = new ByteArrayOutputStream();
        try {
            XMLUtil.copy(r, sink);
            dsv.inlineXML(new InlineXML(sink.toByteArray()));
        } catch (final Exception e) {
            throw new IOException("Error parsing foxml:xmlContent", e);
        }
    }

    private void readContentDigest(final DatastreamVersion dsv) throws XMLStreamException {
        final String type = readAttribute(Constants.TYPE);
        final String digest = readAttribute(Constants.DIGEST);
        if (type != null || digest != null) {
            dsv.contentDigest(new ContentDigest().type(type).hexValue(digest));
        }
        r.nextTag(); // consume closing contentDigest tag
    }

    private boolean moveToStart(final String localName, final String stopAtLocalName) throws XMLStreamException {
        while (r.hasNext()) {
            if (r.next() == XMLStreamConstants.START_ELEMENT) {
                if (r.getLocalName().equals(localName)) {
                    return true;
                } else if (r.getLocalName().equals(stopAtLocalName)) { return false; }
            }
        }
        return false;
    }

    private String readAttribute(final String localName) {
        final String value = r.getAttributeValue(null, localName);
        if (value != null && value.trim().length() > 0) { return value.trim(); }
        return null;
    }

    private static LocalDateTime parseDate(final String value, final String kind) {
        if (value == null) return null;
        final LocalDateTime date = DateUtil.toDate(value);
        if (date == null) {
            logger.warn("Ignoring malformed " + kind + " date value: " + value);
        }
        return date;
    }

    private static State parseState(final String value, final String kind) {
        if (value == null) return null;
        try {
            return State.forShortName(value);
        } catch (@SuppressWarnings("unused") final IllegalArgumentException e) {
            try {
                return State.forLongName(value);
            } catch (@SuppressWarnings("unused") final IllegalArgumentException e2) {
                logger.warn("Ignoring unrecognized " + kind + " state value: " + value);
                return null;
            }
        }
    }

    private static ControlGroup parseControlGroup(final String value) {
        if (value == null) return null;
        try {
            return ControlGroup.forShortName(value);
        } catch (@SuppressWarnings("unused") final IllegalArgumentException e) {
            logger.warn("Ignoring unrecognized datastream control group value: " + value);
            return null;
        }
    }

    private static Boolean parseVersionable(final String value) {
        if (value == null) return null;
        if (value.equalsIgnoreCase("true") || value.equals("1")) {
            return true;

        } else if (value.equalsIgnoreCase("false") || value.equals("0")) {
            return false;
        } else {
            logger.warn("Ignoring unrecognized datastream versionable value: " + value);
            return null;
        }
    }

    private static Long parseLong(final String value, final String kind) {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (@SuppressWarnings("unused") final NumberFormatException e) {
            logger.warn("Ignoring invalid " + kind + " value: " + value);
            return null;
        }
    }

    private static URI parseURI(final String value) {
        if (value == null) return null;
        return URI.create(value);
    }

    private static Set<URI> parseAltIds(final String value) {
        final Set<URI> set = new HashSet<>();
        if (value != null) {
            for (final String uriString : value.split("\\s+")) {
                final URI uri = parseURI(uriString);
                if (uri != null) set.add(uri);
            }
        }
        return set;
    }

}
