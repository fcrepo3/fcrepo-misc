package com.github.cwilper.fcrepo.dto.core;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Date;

/**
 * Unit tests for <code>DatastreamVersion</code>.
 */
public class DatastreamVersionTest extends FedoraDTOTest {

    @Override
    Object[] getEqualInstances() {
        Date now = new Date();
        return new Object[] {
                new DatastreamVersion("a", null),
                new DatastreamVersion("a", null),
                new DatastreamVersion("a", now),
                new DatastreamVersion("a", now)
        };
    }

    @Override
    Object[] getNonEqualInstances() {
        return new Object[] {
                new DatastreamVersion("a", null),
                new DatastreamVersion("b", null)
        };
    }

    @Test
    public void idFieldNormal() {
        Assert.assertEquals("a", new DatastreamVersion("a", null).id());
    }

    @Test
    public void idFieldNormalization() {
        Assert.assertEquals("a", new DatastreamVersion(" a", null).id());
        Assert.assertEquals("a", new DatastreamVersion("a ", null).id());
    }

    @Test (expected=NullPointerException.class)
    public void idFieldNull() {
        new DatastreamVersion(null, null);
    }

    @Test (expected=NullPointerException.class)
    public void idFieldEmpty() {
        new DatastreamVersion("", null);
    }

    @Test (expected=NullPointerException.class)
    public void idFieldSpace() {
        new DatastreamVersion(" ", null);
    }

    @Test
    public void createdDateField() {
        DatastreamVersion dsv;
        Date date;
        // set value, get same value
        date = new Date(0);
        dsv = new DatastreamVersion("a", date);
        Assert.assertEquals(date, dsv.createdDate());
        // changing the original date object after setting it
        // doesn't affect the value stored
        date.setTime(1);
        Assert.assertEquals(0, dsv.createdDate().getTime());
        // changing the retrieved date object after getting it
        // shouldn't affect the value stored
        date = dsv.createdDate();
        date.setTime(1);
        Assert.assertEquals(0, dsv.createdDate().getTime());
        // set null, get null
        dsv = new DatastreamVersion("a", null);
        Assert.assertNull(dsv.createdDate());
    }

    @Test
    public void labelField() {
        checkStringField(new DatastreamVersion("a", null), "label");
    }

    @Test
    public void mimeTypeField() {
        checkStringField(new DatastreamVersion("a", null), "mimeType");
    }

    @Test
    public void altIdsField() {
        DatastreamVersion dsv = new DatastreamVersion("a", null);
        // value starts empty
        Assert.assertEquals(0, dsv.altIds().size());
        // add three values, get in sorted order
        dsv.altIds().add(URI.create("urn:c"));
        dsv.altIds().add(URI.create("urn:a"));
        dsv.altIds().add(URI.create("urn:b"));
        Assert.assertEquals(3, dsv.altIds().size());
        Assert.assertEquals(URI.create("urn:a"), dsv.altIds().first());
        Assert.assertEquals(URI.create("urn:c"), dsv.altIds().last());
    }

    @Test
    public void formatURIValue() {
        checkURIField(new DatastreamVersion("a", null), "formatURI");
    }

    @Test
    public void contentDigestField() {
        DatastreamVersion dsv = new DatastreamVersion("a", null);
        // value starts null
        Assert.assertNull(dsv.contentDigest());
        // set value, get same value
        ContentDigest d = new ContentDigest().type("a").hexValue("a");
        dsv.contentDigest(d);
        Assert.assertEquals(d, dsv.contentDigest());
        // set null, get null
        dsv.contentDigest(null);
        Assert.assertNull(dsv.contentDigest());
    }

    @Test
    public void sizeField() {
        DatastreamVersion dsv = new DatastreamVersion("a", null);
        // value starts null
        Assert.assertNull(dsv.size());
        // set value, get same value
        dsv.size(0L);
        Assert.assertEquals(new Long(0), dsv.size());
        dsv.size(1L);
        Assert.assertEquals(new Long(1), dsv.size());
        // set null, get null
        dsv.size(null);
        Assert.assertNull(dsv.size());
    }

    @Test (expected=IllegalArgumentException.class)
    public void negativeSize() {
        new DatastreamVersion("a", null).size(-1L);
    }

    @Test
    public void inlineXMLValue() throws IOException {
        StringWriter sink = new StringWriter();
        DatastreamVersion dsv = new DatastreamVersion("a", null);
        // value starts undefined
        Assert.assertFalse(dsv.hasInlineXML());
        // getting undefined value has no effect
        dsv.getInlineXML(sink);
        Assert.assertEquals(0, sink.toString().length());
        // set value, value is defined, then get same value
        dsv.setInlineXML(new StringReader("a"));
        Assert.assertTrue(dsv.hasInlineXML());
        dsv.getInlineXML(sink);
        Assert.assertEquals("a", sink.toString());
        // can get value multiple times
        dsv.getInlineXML(sink);
        Assert.assertEquals("aa", sink.toString());
        // set null, value is undefined
        dsv.setInlineXML(null);
        Assert.assertFalse(dsv.hasInlineXML());
    }

    @Test
    public void contentLocationValue() {
        checkURIField(new DatastreamVersion("a", null), "contentLocation");
    }

}
