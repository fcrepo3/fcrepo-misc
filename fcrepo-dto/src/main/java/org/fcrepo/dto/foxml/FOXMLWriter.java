package org.fcrepo.dto.foxml;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.fcrepo.dto.foxml.Constants.BASE64_LINE_LENGTH;
import static org.fcrepo.dto.foxml.Constants.LINE_FEED;
import static org.fcrepo.dto.foxml.Constants.binaryContent;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.fcrepo.dto.core.ContentDigest;
import org.fcrepo.dto.core.ControlGroup;
import org.fcrepo.dto.core.Datastream;
import org.fcrepo.dto.core.DatastreamVersion;
import org.fcrepo.dto.core.FedoraObject;
import org.fcrepo.dto.core.InlineXML;
import org.fcrepo.dto.core.State;
import org.fcrepo.dto.core.io.ContentResolvingDTOWriter;
import org.fcrepo.dto.core.io.DTOWriter;
import org.fcrepo.dto.core.io.DateUtil;
import org.fcrepo.dto.core.io.XMLUtil;

/**
 * A {@link DTOWriter} that writes Fedora Object XML.
 * <p>
 * <b>NOTE:</b> Only FOXML version 1.1 is supported.
 *
 * @see <a href="https://wiki.duraspace.org/x/fABI">Introduction to FOXML</a>
 * @see <a href="http://fedora-commons.org/definitions/1/0/foxml1-1.xsd">FOXML 1.1 XML Schema</a>
 */
public class FOXMLWriter extends ContentResolvingDTOWriter {

    private Set<String> managedDatastreamsToEmbed = new HashSet<>();

    private FedoraObject obj;
    private OutputStream sink;
    private XMLStreamWriter w;

    public FOXMLWriter() {
    }

    public void setManagedDatastreamsToEmbed(
            final Set<String> managedDatastreamsToEmbed) {
        this.managedDatastreamsToEmbed = managedDatastreamsToEmbed;
    }

    @Override
    public DTOWriter getInstance() {
        final FOXMLWriter writer = new FOXMLWriter();
        if (contentResolver != defaultContentResolver) {
            writer.setContentResolver(contentResolver);
        }
        writer.setManagedDatastreamsToEmbed(
                new HashSet<>(managedDatastreamsToEmbed));
        return writer;
    }

    @Override
    public void writeObject(final FedoraObject obj, final OutputStream sink)
            throws IOException {
        this.obj = obj;
        this.sink = sink;
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            w = factory.createXMLStreamWriter(sink, Constants.CHAR_ENCODING);
            writeObject();
        } catch (final XMLStreamException e) {
            throw new IOException(e);
        } finally {
            XMLUtil.closeQuietly(w);
        }
    }

    private void writeObject()
            throws IOException, XMLStreamException {
        w.writeStartDocument(Constants.CHAR_ENCODING, Constants.XML_VERSION);

        w.writeStartElement(Constants.digitalObject);
        w.writeDefaultNamespace(Constants.xmlns);

        w.writeAttribute(Constants.VERSION, Constants.FOXML_VERSION);
        writeAttribute(Constants.PID, obj.pid());

        writeObjectProperties(obj);
        for (final String id: obj.datastreams().keySet()) {
            writeDatastream(obj.datastreams().get(id));
        }
        w.writeEndDocument();
    }

    private void writeObjectProperties(final FedoraObject obj)
            throws XMLStreamException {
        if (obj.state() != null || obj.label() != null
                || obj.ownerId() != null || obj.createdDate() != null
                || obj.lastModifiedDate() != null) {
            w.writeStartElement(Constants.objectProperties);
            writeProperty(Constants.STATE_URI, obj.state());
            writeProperty(Constants.LABEL_URI, obj.label());
            writeProperty(Constants.OWNERID_URI, obj.ownerId());
            writeProperty(Constants.CREATEDDATE_URI, obj.createdDate());
            writeProperty(Constants.LASTMODIFIEDDATE_URI, obj.lastModifiedDate());
            w.writeEndElement();
        }
    }

    private void writeDatastream(final Datastream ds)
            throws IOException, XMLStreamException {
        w.writeStartElement(Constants.datastream);
        writeAttribute(Constants.ID, ds.id());
        writeAttribute(Constants.STATE, ds.state());
        writeAttribute(Constants.CONTROL_GROUP, ds.controlGroup());
        writeAttribute(Constants.VERSIONABLE, ds.versionable());
        for (final DatastreamVersion dsv: ds.versions()) {
            writeDatastreamVersion(ds, dsv);
        }
        w.writeEndElement();
    }

    private void writeDatastreamVersion(final Datastream ds, final DatastreamVersion dsv)
            throws IOException, XMLStreamException {
        w.writeStartElement(Constants.datastreamVersion);
        writeAttribute(Constants.ID, dsv.id());
        writeAttribute(Constants.ALT_IDS, dsv.altIds().toArray());
        writeAttribute(Constants.LABEL, dsv.label());
        writeAttribute(Constants.CREATED, dsv.createdDate());
        writeAttribute(Constants.MIMETYPE, dsv.mimeType());
        writeAttribute(Constants.FORMAT_URI, dsv.formatURI());
        writeAttribute(Constants.SIZE, dsv.size());
        writeContentDigest(dsv.contentDigest());
        if (ds.controlGroup() == ControlGroup.INLINE_XML) {
            writeXMLContent(dsv);
        } else if (ds.controlGroup() == ControlGroup.MANAGED
                && managedDatastreamsToEmbed.contains(ds.id())) {
            writeBinaryContent(dsv.contentLocation());
        } else {
            writeContentLocation(dsv.contentLocation());
        }
        w.writeEndElement();
    }

    private void writeContentLocation(final URI ref) throws XMLStreamException {
        if (ref != null) {
            w.writeStartElement(Constants.contentLocation);
            if (ref.getScheme().equals(Constants.INTERNALREF_SCHEME)) {
                w.writeAttribute(Constants.TYPE, Constants.INTERNALREF_TYPE);
                w.writeAttribute(Constants.REF, ref.getRawSchemeSpecificPart());
            } else {
                w.writeAttribute(Constants.TYPE, Constants.URL_TYPE);
                w.writeAttribute(Constants.REF, ref.toString());
            }
            w.writeEndElement();
        }
    }

    private void writeBinaryContent(final URI ref) throws IOException, XMLStreamException {
        if (ref != null) try (Base64OutputStream out =
                        new Base64OutputStream(sink, true, BASE64_LINE_LENGTH, LINE_FEED.getBytes(UTF_8))) {
            w.writeStartElement(binaryContent);
            w.writeCharacters(LINE_FEED);
            w.flush();
            contentResolver.resolveContent(baseURI, ref, out);
            out.flush();
            w.writeEndElement();
        }
    }

    private void writeXMLContent(final DatastreamVersion dsv)
            throws IOException, XMLStreamException {
        final InlineXML inlineXML = dsv.inlineXML();
        if (inlineXML != null) {
            w.writeStartElement(Constants.xmlContent);
            w.writeCharacters(Constants.LINE_FEED);
            w.flush();
            sink.write(inlineXML.bytes());
            w.writeEndElement();
        }
    }

    private void writeContentDigest(final ContentDigest contentDigest)
            throws XMLStreamException {
        if (contentDigest != null) {
            w.writeStartElement(Constants.contentDigest);
            writeAttribute(Constants.TYPE, contentDigest.type());
            writeAttribute(Constants.DIGEST, contentDigest.hexValue());
            w.writeEndElement();
        }
    }

    private void writeAttribute(final String name, final Object[] values)
            throws XMLStreamException {
        if (values != null && values.length > 0) {
            final StringBuilder b = new StringBuilder();
            for (final Object value: values) {
                if (b.length() > 0) {
                    b.append(" ");
                }
                b.append(value);
            }
            w.writeAttribute(name, b.toString());
        }
    }

    private void writeAttribute(final String name, final LocalDateTime value)
            throws XMLStreamException {
        if (value != null) {
            writeAttribute(name, DateUtil.toString(value));
        }
    }

    private void writeAttribute(final String name, final State value)
            throws XMLStreamException {
        if (value != null) {
            writeAttribute(name, value.shortName());
        }
    }

    private void writeAttribute(final String name, final ControlGroup value)
            throws XMLStreamException {
        if (value != null) {
            writeAttribute(name, value.shortName());
        }
    }

    private void writeAttribute(final String name, final Object value)
            throws XMLStreamException {
        if (value != null) {
            w.writeAttribute(name, value.toString());
        }
    }

    private void writeProperty(final String name, final State value)
            throws XMLStreamException {
        if (value != null) {
            writeProperty(name, value.longName());
        }
    }

    private void writeProperty(final String name, final LocalDateTime value)
            throws XMLStreamException {
        if (value != null) {
            writeProperty(name, DateUtil.toString(value));
        }
    }

    private void writeProperty(final String name, final String value)
            throws XMLStreamException {
        if (value != null) {
            w.writeStartElement(Constants.property);
            w.writeAttribute(Constants.NAME, name);
            w.writeAttribute(Constants.VALUE, value);
            w.writeEndElement();
        }
    }

}
