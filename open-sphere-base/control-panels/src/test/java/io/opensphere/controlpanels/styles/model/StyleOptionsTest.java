package io.opensphere.controlpanels.styles.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Observer;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link StyleOptions}.
 */
public class StyleOptionsTest
{
    /**
     * Tests serializing the options.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException No class.
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        final StyleOptions options = new StyleOptions();
        assertEquals(Color.red, options.getColor());
        assertEquals(Styles.POINT, options.getStyle());
        assertEquals(5, options.getSize());

        options.setSize(2);
        options.setStyle(Styles.POINT);
        options.setIconId(22);

        assertEquals(8, options.getStyles().size());

        assertEquals(Styles.NONE, options.getStyles().get(0));
        assertEquals(Styles.POINT, options.getStyles().get(1));
        assertEquals(Styles.SQUARE, options.getStyles().get(2));
        assertEquals(Styles.TRIANGLE, options.getStyles().get(3));
        assertEquals(Styles.ICON, options.getStyles().get(4));
        assertEquals(Styles.ELLIPSE, options.getStyles().get(5));
        assertEquals(Styles.LINE_OF_BEARING, options.getStyles().get(6));
        assertEquals(Styles.ELLIPSE_WITH_CENTER, options.getStyles().get(7));

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(options);

        final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        final ObjectInputStream objectIn = new ObjectInputStream(in);
        final StyleOptions actual = (StyleOptions)objectIn.readObject();

        assertEquals(Color.red, actual.getColor());
        assertEquals(2, actual.getSize());
        assertEquals(Styles.POINT, actual.getStyle());
        assertEquals(22, actual.getIconId());

        assertEquals(8, actual.getStyles().size());

        assertEquals(Styles.NONE, actual.getStyles().get(0));
        assertEquals(Styles.POINT, actual.getStyles().get(1));
        assertEquals(Styles.SQUARE, actual.getStyles().get(2));
        assertEquals(Styles.TRIANGLE, actual.getStyles().get(3));
        assertEquals(Styles.ICON, actual.getStyles().get(4));
        assertEquals(Styles.ELLIPSE, actual.getStyles().get(5));
        assertEquals(Styles.LINE_OF_BEARING, actual.getStyles().get(6));
        assertEquals(Styles.ELLIPSE_WITH_CENTER, actual.getStyles().get(7));
    }

    /**
     * Tests updating the options and verifies notifications occur.
     */
    @Test
    public void testUpdates()
    {
        final EasyMockSupport support = new EasyMockSupport();

        final Observer color = createObserver(support, StyleOptions.COLOR_PROP);
        final Observer size = createObserver(support, StyleOptions.SIZE_PROP);
        final Observer style = createObserver(support, StyleOptions.STYLE_PROP);
        final Observer icon = createObserver(support, StyleOptions.ICON_PROP);

        support.replayAll();

        final StyleOptions options = new StyleOptions();

        options.addObserver(color);
        options.setColor(Color.RED);
        options.deleteObserver(color);

        options.addObserver(size);
        options.setSize(4);
        options.deleteObserver(size);

        options.addObserver(style);
        options.setStyle(Styles.ICON);
        options.deleteObserver(style);

        options.addObserver(icon);
        options.setIconId(22);
        options.deleteObserver(icon);

        support.verifyAll();
    }

    /**
     * Tests serializing the options.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException No class.
     * @throws JAXBException Bad JAXB.
     */
    @Test
    public void testXmlSerialization() throws IOException, ClassNotFoundException, JAXBException
    {
        final StyleOptions options = new StyleOptions();
        assertFalse(options.hasSizeBeenSet());
        assertEquals(Color.red, options.getColor());
        assertEquals(Styles.POINT, options.getStyle());
        assertEquals(5, options.getSize());

        options.setSize(2);
        assertTrue(options.hasSizeBeenSet());
        options.setStyle(Styles.POINT);
        options.setIconId(22);

        assertEquals(8, options.getStyles().size());

        assertEquals(Styles.NONE, options.getStyles().get(0));
        assertEquals(Styles.POINT, options.getStyles().get(1));
        assertEquals(Styles.SQUARE, options.getStyles().get(2));
        assertEquals(Styles.TRIANGLE, options.getStyles().get(3));
        assertEquals(Styles.ICON, options.getStyles().get(4));
        assertEquals(Styles.ELLIPSE, options.getStyles().get(5));
        assertEquals(Styles.LINE_OF_BEARING, options.getStyles().get(6));
        assertEquals(Styles.ELLIPSE_WITH_CENTER, options.getStyles().get(7));

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(options, output);

        final StyleOptions actual = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()),
                StyleOptions.class);

        assertEquals(Color.red, actual.getColor());
        assertEquals(2, actual.getSize());
        assertTrue(actual.hasSizeBeenSet());
        assertEquals(Styles.POINT, actual.getStyle());
        assertEquals(22, actual.getIconId());

        assertEquals(8, actual.getStyles().size());

        assertEquals(Styles.NONE, actual.getStyles().get(0));
        assertEquals(Styles.POINT, actual.getStyles().get(1));
        assertEquals(Styles.SQUARE, actual.getStyles().get(2));
        assertEquals(Styles.TRIANGLE, actual.getStyles().get(3));
        assertEquals(Styles.ICON, actual.getStyles().get(4));
        assertEquals(Styles.ELLIPSE, actual.getStyles().get(5));
        assertEquals(Styles.LINE_OF_BEARING, actual.getStyles().get(6));
        assertEquals(Styles.ELLIPSE_WITH_CENTER, actual.getStyles().get(7));
    }

    /**
     * Creates an easy mocked observer.
     *
     * @param support Used to create the mock.
     * @param property The property expected to be updated.
     * @return The easy mocked observer.
     */
    private Observer createObserver(final EasyMockSupport support, final String property)
    {
        final Observer observer = support.createMock(Observer.class);

        observer.update(EasyMock.isA(StyleOptions.class), EasyMock.cmpEq(property));

        return observer;
    }
}
