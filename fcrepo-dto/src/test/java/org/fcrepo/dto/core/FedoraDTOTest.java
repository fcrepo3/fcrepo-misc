package org.fcrepo.dto.core;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.time.LocalDateTime;

/**
 * Common tests and convenience methods for Fedora DTO classes.
 */
public abstract class FedoraDTOTest {

    @Test
    public void equality() {
        final Object[] instances = getEqualInstances();
        for (int i = 0; i < instances.length; i+=2) {
            Assert.assertEquals(instances[i], instances[i+1]);
            Assert.assertEquals(instances[i+1].hashCode(),
                    instances[i+1].hashCode());
        }
    }

    @Test
    public void inequality() {
        final Object[] instances = getNonEqualInstances();
        Assert.assertFalse(instances[0].equals(""));
        for (int i = 0; i < instances.length; i+=2) {
            Assert.assertFalse(instances[i].equals(instances[i+1]));
        }
    }

    /**
     * Get 1 or more pairs of equal instances of this class.
     *
     * @return the equal instance pairs to test.
     */
    abstract Object[] getEqualInstances();

    /**
     * Get 1 or more pairs of non-equal instances of this class.
     *
     * @return the non-equal instance pairs to test.
     */
    abstract Object[] getNonEqualInstances();

    static void checkStringField(final Object o, final String field) {
        try {
            final Method setter = o.getClass().getMethod(field, String.class);
            final Method getter = o.getClass().getMethod(field);
            // value starts null
            Assert.assertNull(getter.invoke(o));
            // set value, get same value
            setter.invoke(o, "a");
            Assert.assertEquals("a", getter.invoke(o));
            // set " a", get normalized "a"
            setter.invoke(o, " a");
            Assert.assertEquals("a", getter.invoke(o));
            // set "a ", get normalized "a"
            setter.invoke(o, "a ");
            Assert.assertEquals("a", getter.invoke(o));
            // set "", get normalized null
            setter.invoke(o, "");
            Assert.assertNull(getter.invoke(o));
            // set " ", get normalized null
            setter.invoke(o, " ");
            Assert.assertNull(getter.invoke(o));
            // set null, get null
            setter.invoke(o, new Object[] { null });
            Assert.assertNull(getter.invoke(o));
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static void checkDateField(final Object o, final String field) {
        try {
            final Method setter = o.getClass().getMethod(field, LocalDateTime.class);
            final Method getter = o.getClass().getMethod(field);
            // value starts null
            Assert.assertNull(getter.invoke(o));
            // set value, get same value
            final LocalDateTime date = LocalDateTime.now();
            setter.invoke(o, date);
            Assert.assertEquals(date, getter.invoke(o));
            // set null, get null
            setter.invoke(o, new Object[] { null });
            Assert.assertNull(getter.invoke(o));
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static void checkURIField(final Object o, final String field) {
        try {
            final Method setter = o.getClass().getMethod(field, URI.class);
            final Method getter = o.getClass().getMethod(field);
            // value starts null
            Assert.assertNull(getter.invoke(o));
            // set value, get same value
            setter.invoke(o, URI.create("urn:a"));
            Assert.assertEquals(URI.create("urn:a"), getter.invoke(o));
            // set null, get null
            setter.invoke(o, new Object[] { null });
            Assert.assertNull(getter.invoke(o));
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}