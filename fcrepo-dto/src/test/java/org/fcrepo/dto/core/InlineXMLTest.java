package org.fcrepo.dto.core;

import org.fcrepo.dto.core.InlineXML;
import org.fcrepo.dto.core.io.XMLUtil.XMLException;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;

import java.io.IOException;

/**
 * Unit tests for <code>InlineXML</code>.
 */
public class InlineXMLTest extends FedoraDTOTest {

    @Override
    public Object[] getEqualInstances() {
        return new Object[] {
                new InlineXML("<doc/>"),
                new InlineXML("<doc></doc>")
        };
    }

    @Override
    public Object[] getNonEqualInstances() {
        return new Object[] {
                new InlineXML("<doc/>"),
                new InlineXML("<doctor/>")
        };
    }

    @Test (expected=XMLException.class)
    @SuppressWarnings("unused")
    public void emptyString() throws IOException {
        new InlineXML("");
    }

    @Test (expected=XMLException.class)
    @SuppressWarnings("unused")
    public void emptyBytes() throws IOException {
        new InlineXML(new byte[0]);
    }

    @Test (expected=XMLException.class)
    @SuppressWarnings("unused")
    public void malformedString() throws IOException {
        new InlineXML("<nonClosingElement>");
    }

    @Test (expected=XMLException.class)
    @SuppressWarnings("unused")
    public void malformedBytes() throws IOException {
        new InlineXML(getBytesUtf8("<nonClosingElement>"));
    }

    @Test
    public void canonicalize() {
        InlineXML xml;
        final String expected = "<a b=\"c\"></a>";
        final byte[] expectedBytes = getBytesUtf8(expected);

        // empty elements should be expanded
        xml = new InlineXML("<a b=\"c\"/>");
        Assert.assertTrue(xml.canonical());
        Assert.assertEquals(expected, xml.value());
        Assert.assertArrayEquals(expectedBytes, xml.bytes());

        // comments and leading and trailing whitespace should be dropped
        xml = new InlineXML(" <!-- comment -->\n<a b='c'/>\n<!-- --> ");
        Assert.assertTrue(xml.canonical());
        Assert.assertEquals(expected, xml.value());
        Assert.assertArrayEquals(expectedBytes, xml.bytes());
    }

    @Test
    public void normalize() {
        InlineXML xml;
        final String expected = "<a xmlns=\"b\"/>";
        final byte[] expectedBytes = getBytesUtf8(expected);

        // not canonicalizable because namespace uri is relative. so:
        // quotes around attribute values should be changed to double-quotes,
        // empty elements should be collapsed, and
        // leading and trailing whitespace should be dropped
        xml = new InlineXML("  \n  <a xmlns='b'></a>\n  ");
        Assert.assertFalse(xml.canonical());
        Assert.assertEquals(expected, xml.value());
        Assert.assertArrayEquals(expectedBytes, xml.bytes());
    }
}
