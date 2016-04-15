package com.github.cwilper.fcrepo.dto.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for <code>ControlGroup</code>.
 */
public class ControlGroupTest {

    @Test
    public void fourPossibleValues() {
        assertEquals(4, ControlGroup.values().length);
    }

    @Test
    public void expectedShortNames() {
        assertEquals("E", ControlGroup.EXTERNAL.shortName());
        assertEquals("M", ControlGroup.MANAGED.shortName());
        assertEquals("R", ControlGroup.REDIRECT.shortName());
        assertEquals("X", ControlGroup.INLINE_XML.shortName());
    }

    @Test
    public void getByShortNames() {
        assertEquals(ControlGroup.EXTERNAL, ControlGroup.forShortName("E"));
        assertEquals(ControlGroup.MANAGED, ControlGroup.forShortName("M"));
        assertEquals(ControlGroup.REDIRECT, ControlGroup.forShortName("R"));
        assertEquals(ControlGroup.INLINE_XML, ControlGroup.forShortName("X"));
    }

    @Test (expected=IllegalArgumentException.class)
    public void bogusShortName() {
        ControlGroup.forShortName("Bogus");
    }

}
