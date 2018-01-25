package io.opensphere.controlpanels.styles.model;

import static org.junit.Assert.assertEquals;

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

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link LabelOptions}.
 */
public class LabelOptionsTest
{
    /**
     * Tests serializing the label options.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testSerialize() throws IOException, ClassNotFoundException
    {
        LabelOptions options = new LabelOptions();
        assertEquals(Color.WHITE, options.getColor());
        assertEquals(14, options.getSize());
        options.setColor(Color.BLUE);
        options.setSize(12);

        ColumnLabel label1 = new ColumnLabel();
        label1.setColumn("column1");
        options.getColumnLabels().getColumnsInLabel().add(label1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(options);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        LabelOptions actual = (LabelOptions)objectIn.readObject();

        assertEquals("column1", actual.getColumnLabels().getColumnsInLabel().get(0).getColumn());
        assertEquals(Color.BLUE, actual.getColor());
        assertEquals(12, actual.getSize());
    }

    /**
     * Tests updating the label options and verifies notifications.
     */
    @Test
    public void testUpdate()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer color = createObserver(support, LabelOptions.COLOR_PROP);
        Observer size = createObserver(support, LabelOptions.SIZE_PROP);

        support.replayAll();

        LabelOptions options = new LabelOptions();

        options.addObserver(color);
        options.setColor(Color.blue);
        options.deleteObserver(color);

        options.addObserver(size);
        options.setSize(14);
        options.deleteObserver(size);

        support.verifyAll();
    }

    /**
     * Tests serializing the label options.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testXmlSerialize() throws IOException, ClassNotFoundException, JAXBException
    {
        LabelOptions options = new LabelOptions();
        assertEquals(Color.WHITE, options.getColor());
        assertEquals(14, options.getSize());
        options.setColor(Color.BLUE);
        options.setSize(12);

        ColumnLabel label1 = new ColumnLabel();
        label1.setColumn("column1");
        options.getColumnLabels().getColumnsInLabel().add(label1);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(options, output);

        LabelOptions actual = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), LabelOptions.class);

        assertEquals("column1", actual.getColumnLabels().getColumnsInLabel().get(0).getColumn());
        assertEquals(Color.BLUE, actual.getColor());
        assertEquals(12, actual.getSize());
    }

    /**
     * Creates an easy mocked observer.
     *
     * @param support Used to create the mock.
     * @param property The property expected to be updated.
     * @return The easy mocked observer.
     */
    private Observer createObserver(EasyMockSupport support, String property)
    {
        Observer observer = support.createMock(Observer.class);

        observer.update(EasyMock.isA(LabelOptions.class), EasyMock.cmpEq(property));

        return observer;
    }
}
