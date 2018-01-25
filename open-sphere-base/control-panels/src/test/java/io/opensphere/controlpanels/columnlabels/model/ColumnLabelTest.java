package io.opensphere.controlpanels.columnlabels.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Observer;

import javafx.collections.ListChangeListener;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link ColumnLabel}.
 */
public class ColumnLabelTest
{
    /**
     * Tests serializing the model.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        ColumnLabel columnLabel = new ColumnLabel();

        columnLabel.setColumn("column1");
        columnLabel.setShowColumnName(true);
        columnLabel.getAvailableColumns().addAll("column1", "column2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(columnLabel);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        ColumnLabel actual = (ColumnLabel)objectIn.readObject();

        assertEquals("column1", actual.getColumn());
        assertTrue(actual.isShowColumnName());
        assertTrue(actual.getAvailableColumns().isEmpty());
    }

    /**
     * Tests updating the model and notifications.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer enabled = createObserver(support, ColumnLabel.SHOW_COLUMN_NAME_PROP);
        Observer column = createObserver(support, ColumnLabel.COLUMN_PROP);
        ListChangeListener<? super String> listListener = support.createMock(ListChangeListener.class);
        listListener.onChanged(EasyMock.isA(ListChangeListener.Change.class));

        support.replayAll();

        ColumnLabel model = new ColumnLabel();
        model.getAvailableColumns().addListener(listListener);

        model.addObserver(enabled);
        model.setShowColumnName(true);
        model.deleteObserver(enabled);

        model.addObserver(column);
        model.setColumn("column1");
        model.deleteObserver(column);

        model.getAvailableColumns().addAll("column1", "column2");

        support.verifyAll();
    }

    /**
     * Tests serializing the model.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testXmlSerialization() throws IOException, ClassNotFoundException, JAXBException
    {
        ColumnLabel columnLabel = new ColumnLabel();

        columnLabel.setColumn("column3");
        columnLabel.setShowColumnName(true);
        columnLabel.getAvailableColumns().addAll("column3", "column2");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(columnLabel, output);

        ColumnLabel actual = XMLUtilities.readXMLObject(new ByteArrayInputStream(output.toByteArray()), ColumnLabel.class);

        assertEquals("column3", actual.getColumn());
        assertTrue(actual.isShowColumnName());
        assertTrue(actual.getAvailableColumns().isEmpty());
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

        observer.update(EasyMock.isA(ColumnLabel.class), EasyMock.cmpEq(property));

        return observer;
    }
}
