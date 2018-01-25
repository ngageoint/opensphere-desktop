package io.opensphere.controlpanels.columnlabels.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link ColumnLabels}.
 */
public class ColumnLabelsTest
{
    /**
     * Tests serializing the model.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test() throws IOException, ClassNotFoundException
    {
        EasyMockSupport support = new EasyMockSupport();

        ColumnLabel label1 = new ColumnLabel();
        label1.setShowColumnName(true);
        label1.setColumn("column1");

        ColumnLabel label2 = new ColumnLabel();
        label2.setShowColumnName(true);
        label2.setColumn("column2");

        ListDataListener<ColumnLabel> listener = support.createMock(ListDataListener.class);
        listener.elementsAdded(EasyMock.isA(ListDataEvent.class));

        support.replayAll();

        ColumnLabels model = new ColumnLabels();
        assertTrue(model.isAlwaysShowLabels());
        model.setAlwaysShowLabels(false);
        model.getColumnsInLabel().addChangeListener(listener);
        model.getColumnsInLabel().addAll(New.list(label1, label2));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(model);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        ColumnLabels actual = (ColumnLabels)objectIn.readObject();

        assertEquals(2, actual.getColumnsInLabel().size());
        assertEquals("column1", actual.getColumnsInLabel().get(0).getColumn());
        assertEquals("column2", actual.getColumnsInLabel().get(1).getColumn());
        assertFalse(actual.isAlwaysShowLabels());

        support.verifyAll();
    }

    /**
     * Tests serializing the model with an empty list.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testSerializeEmpty() throws IOException, ClassNotFoundException
    {
        EasyMockSupport support = new EasyMockSupport();

        @SuppressWarnings("unchecked")
        ListDataListener<ColumnLabel> listener = support.createMock(ListDataListener.class);

        support.replayAll();

        ColumnLabels model = new ColumnLabels();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(model);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        ColumnLabels actual = (ColumnLabels)objectIn.readObject();

        actual.getColumnsInLabel().addChangeListener(listener);

        support.verifyAll();
    }

    /**
     * Tests updating the label options and verifies notifications.
     */
    @Test
    public void testUpdate()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer alwaysShow = createObserver(support, ColumnLabels.ALWAYS_SHOW_LABELS_PROP);

        support.replayAll();

        ColumnLabels options = new ColumnLabels();

        options.addObserver(alwaysShow);
        options.setAlwaysShowLabels(true);
        options.deleteObserver(alwaysShow);

        support.verifyAll();
    }

    /**
     * Tests serializing the model.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     * @throws JAXBException Bad jaxb.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testXmlSerialization() throws IOException, ClassNotFoundException, JAXBException
    {
        EasyMockSupport support = new EasyMockSupport();

        ColumnLabel label1 = new ColumnLabel();
        label1.setShowColumnName(true);
        label1.setColumn("column1");

        ColumnLabel label2 = new ColumnLabel();
        label2.setShowColumnName(true);
        label2.setColumn("column2");

        ListDataListener<ColumnLabel> listener = support.createMock(ListDataListener.class);
        listener.elementsAdded(EasyMock.isA(ListDataEvent.class));

        support.replayAll();

        ColumnLabels model = new ColumnLabels();
        assertTrue(model.isAlwaysShowLabels());
        model.setAlwaysShowLabels(false);
        model.getColumnsInLabel().addChangeListener(listener);
        model.getColumnsInLabel().addAll(New.list(label1, label2));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(model, output);

        ColumnLabels actual = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), ColumnLabels.class);

        assertEquals(2, actual.getColumnsInLabel().size());
        assertEquals("column1", actual.getColumnsInLabel().get(0).getColumn());
        assertEquals("column2", actual.getColumnsInLabel().get(1).getColumn());
        assertFalse(actual.isAlwaysShowLabels());

        support.verifyAll();
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

        observer.update(EasyMock.isA(ColumnLabels.class), EasyMock.cmpEq(property));

        return observer;
    }
}
