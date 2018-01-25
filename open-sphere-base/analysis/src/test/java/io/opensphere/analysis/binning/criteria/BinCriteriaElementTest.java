package io.opensphere.analysis.binning.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Observer;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link BinCriteriaElement} class.
 */
public class BinCriteriaElementTest
{
    /**
     * Tests notifications.
     */
    @Test
    public void testNotification()
    {
        EasyMockSupport support = new EasyMockSupport();

        BinCriteriaElement criteria = new BinCriteriaElement();

        Observer fieldObserver = support.createMock(Observer.class);
        fieldObserver.update(EasyMock.eq(criteria), EasyMock.cmpEq(BinCriteriaElement.FIELD_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals("field2", criteria.getField());
            return null;
        });

        Observer criteriaObserver = support.createMock(Observer.class);
        criteriaObserver.update(EasyMock.eq(criteria), EasyMock.cmpEq(BinCriteriaElement.CRITERIA_TYPE_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertTrue(criteria.getCriteriaType() instanceof UniqueCriteria);
            return null;
        });

        Observer criteriaObserver2 = support.createMock(Observer.class);
        criteriaObserver2.update(EasyMock.eq(criteria), EasyMock.cmpEq(BinCriteriaElement.CRITERIA_TYPE_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            RangeCriteria range = (RangeCriteria)criteria.getCriteriaType();
            assertEquals(100, range.getBinWidth(), 0d);
            return null;
        });

        support.replayAll();

        criteria.addObserver(fieldObserver);
        criteria.setField("field2");
        criteria.deleteObserver(fieldObserver);

        criteria.addObserver(criteriaObserver);
        UniqueCriteria unique = new UniqueCriteria();
        criteria.setCriteriaType(unique);

        criteria.deleteObserver(criteriaObserver);
        RangeCriteria range = new RangeCriteria();
        criteria.setCriteriaType(range);
        criteria.addObserver(criteriaObserver2);
        assertEquals(0, unique.countObservers());
        range.setBinWidth(100);

        support.verifyAll();
    }

    /**
     * Tests notifications.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testNotificationAfterSerialization() throws JAXBException
    {
        EasyMockSupport support = new EasyMockSupport();

        BinCriteriaElement original = new BinCriteriaElement();
        original.setField("field1");
        RangeCriteria aRange = new RangeCriteria();
        original.setCriteriaType(aRange);
        aRange.setBinWidth(101);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(original, output);

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        BinCriteriaElement criteria = XMLUtilities.readXMLObject(input, BinCriteriaElement.class);

        Observer criteriaObserver2 = support.createMock(Observer.class);
        criteriaObserver2.update(EasyMock.eq(criteria), EasyMock.cmpEq(BinCriteriaElement.CRITERIA_TYPE_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            RangeCriteria range = (RangeCriteria)criteria.getCriteriaType();
            assertEquals(100, range.getBinWidth(), 0d);
            return null;
        });

        support.replayAll();

        criteria.addObserver(criteriaObserver2);
        RangeCriteria range = (RangeCriteria)criteria.getCriteriaType();
        range.setBinWidth(100);

        support.verifyAll();
    }

    /**
     * Tests setting a null criteria type.
     */
    @Test
    public void testNullCriteriaType()
    {
        BinCriteriaElement element = new BinCriteriaElement();
        element.setCriteriaType(null);
    }

    /**
     * Tests serializing the class.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testSerialization() throws JAXBException
    {
        BinCriteriaElement criteria = new BinCriteriaElement();
        criteria.setCriteriaType(new UniqueCriteria());
        criteria.setField("field1");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(criteria, output);

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        BinCriteriaElement actual = XMLUtilities.readXMLObject(input, BinCriteriaElement.class);

        assertEquals("field1", actual.getField());
        assertTrue(actual.getCriteriaType() instanceof UniqueCriteria);

        criteria.setCriteriaType(new RangeCriteria());
        output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(criteria, output);

        input = new ByteArrayInputStream(output.toByteArray());
        actual = XMLUtilities.readXMLObject(input, BinCriteriaElement.class);

        assertEquals("field1", actual.getField());
        assertTrue(actual.getCriteriaType() instanceof RangeCriteria);
    }
}
