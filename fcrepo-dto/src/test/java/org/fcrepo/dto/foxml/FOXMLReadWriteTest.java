package org.fcrepo.dto.foxml;

import org.apache.commons.io.IOUtils;
import org.fcrepo.dto.core.ContentDigest;
import org.fcrepo.dto.core.ControlGroup;
import org.fcrepo.dto.core.Datastream;
import org.fcrepo.dto.core.DatastreamVersion;
import org.fcrepo.dto.core.FedoraObject;
import org.fcrepo.dto.core.InlineXML;
import org.fcrepo.dto.core.State;
import org.fcrepo.dto.core.io.XMLUtil;
import org.fcrepo.dto.foxml.FOXMLReader;
import org.fcrepo.dto.foxml.FOXMLWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.time.LocalDateTime.parse;
import static org.fcrepo.dto.core.io.DateUtil.PREFERRED_DATE_FORMATTER;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Set;

public class FOXMLReadWriteTest {

    // when test files are missing, if this is defined, they'll be generated
    private static File testResources =
            new File("/Users/cwilper/work/cwilper/fcrepo-misc/"
                    + "fcrepo-dto/fcrepo-dto-foxml/src/test/resources");

    private FedoraObject obj;

    @Before
    public void setUp() {
        obj = new FedoraObject();
    }

    @Test
    public void objEmpty() {
        writeThenReadCheck("objEmpty");
    }

    @Test
    public void objPid() {
        obj.pid("test:objPid");
        writeThenReadCheck("objPid");
    }

    @Test
    public void objStateActive() {
        obj.state(State.ACTIVE);
        writeThenReadCheck("objStateActive");
    }

    @Test
    public void objStateInactive() {
        obj.state(State.INACTIVE);
        writeThenReadCheck("objStateInactive");
    }

    @Test
    public void objStateDeleted() {
        obj.state(State.DELETED);
        writeThenReadCheck("objStateDeleted");
    }

    @Test
    public void objLabel() {
        obj.label("objLabel");
        writeThenReadCheck("objLabel");
    }

    @Test
    public void objOwnerId() {
        obj.ownerId("objOwnerId");
        writeThenReadCheck("objOwnerId");
    }

    @Test
    public void objCreatedDate() {
        obj.createdDate(LocalDateTime.parse("1970-01-01T00:00:00.000Z", PREFERRED_DATE_FORMATTER));
        writeThenReadCheck("objCreatedDate");
    }

    @Test
    public void objLastModifiedDate() {
        obj.lastModifiedDate(LocalDateTime.parse("1970-01-01T00:00:00.000Z", PREFERRED_DATE_FORMATTER));
        writeThenReadCheck("objLastModifiedDate");
    }

    @Test
    public void dsId() {
        obj.pid("demo:999").putDatastream(new Datastream("dsId"));
        writeThenReadCheck("dsId");
        final FedoraObject retrievedObject = readCheck("dsId", new FOXMLReader(), true);
        final Datastream retrievedDatastream = retrievedObject.datastreams().get("dsId");
        assertEquals("demo:999", retrievedDatastream.pid());
    }

    @Test
    public void dsStateActive() {
        obj.putDatastream(new Datastream("ds").state(State.ACTIVE));
        writeThenReadCheck("dsStateActive");
    }

    @Test
    public void dsStateInactive() {
        obj.putDatastream(new Datastream("ds").state(State.INACTIVE));
        writeThenReadCheck("dsStateInactive");
    }

    @Test
    public void dsStateDeleted() {
        obj.putDatastream(new Datastream("ds").state(State.DELETED));
        writeThenReadCheck("dsStateDeleted");
    }

    @Test
    public void dsControlGroupE() {
        obj.putDatastream(new Datastream("ds").controlGroup(
                ControlGroup.EXTERNAL));
        writeThenReadCheck("dsControlGroupE");
    }

    @Test
    public void dsControlGroupM() {
        obj.putDatastream(new Datastream("ds").controlGroup(
                ControlGroup.MANAGED));
        writeThenReadCheck("dsControlGroupM");
    }

    @Test
    public void dsControlGroupR() {
        obj.putDatastream(new Datastream("ds").controlGroup(
                ControlGroup.REDIRECT));
        writeThenReadCheck("dsControlGroupR");
    }

    @Test
    public void dsControlGroupX() {
        obj.putDatastream(new Datastream("ds").controlGroup(
                ControlGroup.INLINE_XML));
        writeThenReadCheck("dsControlGroupX");
    }

    @Test
    public void dsVersionableTrue() {
        obj.putDatastream(new Datastream("ds").versionable(true));
        writeThenReadCheck("dsVersionableTrue");
    }

    @Test
    public void dsVersionableFalse() {
        obj.putDatastream(new Datastream("ds").versionable(false));
        writeThenReadCheck("dsVersionableFalse");
    }

    @Test
    public void dsMulti() {
        obj.putDatastream(new Datastream("ds2"));
        obj.putDatastream(new Datastream("ds1"));
        writeThenReadCheck("dsMulti");
    }

    @Test
    public void dsvId() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(null);
        obj.putDatastream(ds);
        writeThenReadCheck("dsvId");
    }

    @Test
    public void dsvCreatedDate() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(parse("1970-01-01T00:00:00.000Z", PREFERRED_DATE_FORMATTER));
        obj.putDatastream(ds);
        writeThenReadCheck("dsvCreatedDate");
    }

    @Test
    public void dsvLabel() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(null).label("dsvLabel");
        obj.putDatastream(ds);
        writeThenReadCheck("dsvLabel");
    }

    @Test
    public void dsvMimeType() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(null).mimeType("dsvMimeType");
        obj.putDatastream(ds);
        writeThenReadCheck("dsvMimeType");
    }

    @Test
    public void dsvFormatURI() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(null).formatURI(URI.create("urn:dsvFormatURI"));
        obj.putDatastream(ds);
        writeThenReadCheck("dsvFormatURI");
    }

    @Test
    public void dsvSize() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(null).size(0L);
        obj.putDatastream(ds);
        writeThenReadCheck("dsvSize");
    }

    @Test
    public void dsvContentDigest() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(null).contentDigest(new ContentDigest().type("someType").
                hexValue("someHexValue"));
        obj.putDatastream(ds);
        writeThenReadCheck("dsvContentDigest");
    }

    @Test
    public void dsvInlineXML() throws IOException {
        final Datastream ds = new Datastream("ds").controlGroup(
                ControlGroup.INLINE_XML);
        ds.addVersion(null).inlineXML(new InlineXML("<doc></doc>"));
        obj.putDatastream(ds);
        writeThenReadCheck("dsvInlineXML");
    }

    @Test
    public void dsvInlineRDF() throws IOException {
        final Datastream ds = new Datastream("ds").controlGroup(
                ControlGroup.INLINE_XML);
        final String xml = IOUtils.toString(getClass().getClassLoader()
                .getResourceAsStream("inlineRDFInput.xml"));
        ds.addVersion(null).inlineXML(new InlineXML(xml));
        obj.putDatastream(ds);
        writeThenReadCheck("dsvInlineRDF");
    }

    @Test
    public void dsvBinaryContent() throws IOException {
        final String content = "A word starting with c, followed by o, "
                + "followed by n, followed by t, followed by e, "
                + "followed by n, and ending with t.";
        final File tempFile = File.createTempFile("fcrepo-dto-test", null);
        try (final FileOutputStream sink = new FileOutputStream(tempFile)) {
            //
            // Write test
            //
            try (FileOutputStream tempStream = new FileOutputStream(tempFile)) {
                IOUtils.copy(new StringReader(content), tempStream);
            }
            final Datastream ds = new Datastream("ds").controlGroup(
                    ControlGroup.MANAGED);
            ds.addVersion(null).contentLocation(tempFile.toURI());
            final DatastreamVersion origDSV = ds.versions().first();
            obj.putDatastream(ds);
            // serialize obj to FOXML, requesting that the managed content is
            // embedded as binaryContent (base64), and ensure the resulting
            // XML is exactly as expected.
            writeCheck("dsvBinaryContent", obj.datastreams().keySet());
            //
            // Read test
            //
            final FOXMLReader reader = new FOXMLReader();
            final FedoraObject result = readCheck("dsvBinaryContent", reader, false);
            final DatastreamVersion resultDSV = result.datastreams().get("ds").versions().first();
            // should have new file: contentLocation
            final URI resultLocation = resultDSV.contentLocation();
            Assert.assertNotNull(resultLocation);
            Assert.assertFalse(resultLocation.equals(origDSV.contentLocation()));
            Assert.assertEquals(resultLocation.getScheme(), "file");
            // ...whose content matches that of the original
            final File resultFile = new File(resultLocation.getRawSchemeSpecificPart());
            Assert.assertTrue(resultFile.exists());
            try (final FileInputStream resultIn = new FileInputStream(resultFile)) {
                final String resultContent = IOUtils.toString(resultIn, "UTF-8");
                Assert.assertEquals(resultContent, content);
            }
            // other than the contentLocation, the newly-read object should
            // be equivalent to the original one
            resultDSV.contentLocation(origDSV.contentLocation());
            Assert.assertEquals(obj, result);
            // when the reader is closed, it should delete resultFile
            reader.close();
            Assert.assertFalse(resultFile.exists());
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void dsvContentLocation() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(null).contentLocation(URI.create("http://example.org/"));
        obj.putDatastream(ds);
        writeThenReadCheck("dsvContentLocation");
    }

    @Test
    public void dsvContentLocationInternal() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(null).contentLocation(
                URI.create("internal:test:obj+ds+ds.0"));
        obj.putDatastream(ds);
        writeThenReadCheck("dsvContentLocationInternal");
    }

    @Test
    public void dsvAltIds() {
        final Datastream ds = new Datastream("ds");
        ds.addVersion(null).altIds().add(URI.create("urn:a"));
        obj.putDatastream(ds);
        writeThenReadCheck("dsvAltIds");
    }

    @Test
    public void dsvAltIdsMulti() {
        final Datastream ds = new Datastream("ds");
        final DatastreamVersion dsv = ds.addVersion(null);
        dsv.altIds().add(URI.create("urn:c"));
        dsv.altIds().add(URI.create("urn:a"));
        dsv.altIds().add(URI.create("urn:b"));
        obj.putDatastream(ds);
        writeThenReadCheck("dsvAltIdsMulti");
    }

    @Test
    public void dsvMulti() {
        final Datastream ds = new Datastream("ds");
        final DatastreamVersion dsv1 = new DatastreamVersion("ds.1", null);
        final DatastreamVersion dsv2 = new DatastreamVersion("ds.2", null);
        final DatastreamVersion dsv3 =
                        new DatastreamVersion("ds.3", parse("1970-01-01T00:00:00.002Z", PREFERRED_DATE_FORMATTER));
        final DatastreamVersion dsv4 =
                        new DatastreamVersion("ds.4", parse("1970-01-01T00:00:00.001Z", PREFERRED_DATE_FORMATTER));
        ds.versions().add(dsv4);
        ds.versions().add(dsv2);
        ds.versions().add(dsv1);
        ds.versions().add(dsv3);
        // should be in this order: 2, 1, 3, 4
        obj.putDatastream(ds);
        writeThenReadCheck("dsvMulti");
    }

    private void writeThenReadCheck(final String testName) {
        writeCheck(testName, null);
        readCheck(testName, new FOXMLReader(), true);
    }

    private void writeCheck(final String testName, final Set<String> embedIds) {
        final String filename = "foxml/" + testName + ".xml";
        try {
            final byte[] actualBytes = getFOXML(embedIds).getBytes("UTF-8");
            final String actualXML = new String(actualBytes, "UTF-8");
            if (getClass().getClassLoader().getResource(filename) == null) {
                if (testResources != null && testResources.isDirectory()) {
                    final File testFile = new File(testResources, filename);
                    testFile.getParentFile().mkdirs();
                    try (OutputStream out = new FileOutputStream(testFile)) {
                        IOUtils.copy(new StringReader(actualXML), out);
                    }
                    System.out.println("Generated test resource: " + filename);
                } else {
                    Assert.fail("Test resource not found: " + filename);
                }
            } else {
                try (final InputStream expectedStream = getClass().getClassLoader().getResourceAsStream(filename)) {
                    final byte[] expectedBytes = XMLUtil.prettyPrint(IOUtils.toByteArray(expectedStream), false);
                    final String expectedXML = new String(expectedBytes, "UTF-8");
                    Assert.assertEquals(expectedXML, actualXML);
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FedoraObject readCheck(final String testName, final FOXMLReader reader,
                                   final boolean check) {
        final String filename = "foxml/" + testName + ".xml";
        try (final InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            final FedoraObject result = reader.readObject(in);
            if (check) {
                Assert.assertEquals(obj, result);
            }
            return result;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFOXML(final Set<String> embedIds) {
        try {
            final FOXMLWriter writer = new FOXMLWriter();
            writer.setManagedDatastreamsToEmbed(embedIds);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer.writeObject(obj, out);
            final byte[] pretty = XMLUtil.prettyPrint(out.toByteArray(), false);
            return new String(pretty, "UTF-8");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
