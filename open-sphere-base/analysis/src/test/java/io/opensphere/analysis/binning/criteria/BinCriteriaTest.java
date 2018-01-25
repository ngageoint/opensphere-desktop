package io.opensphere.analysis.binning.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Observer;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link BinCriteria}.
 */
public class BinCriteriaTest
{
    /**
     * Tests add and remove criterias.
     */
    @Test
    public void testAddRemoveCriterias()
    {
        EasyMockSupport support = new EasyMockSupport();

        BinCriteria criteria = new BinCriteria();
        BinCriteriaElement element = new BinCriteriaElement();

        Observer observer = support.createMock(Observer.class);
        observer.update(EasyMock.eq(criteria), EasyMock.cmpEq(BinCriteria.CRITERIAS_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(1, criteria.getCriterias().size());
            assertEquals(element, criteria.getCriterias().get(0));
            return null;
        });

        observer.update(EasyMock.eq(criteria), EasyMock.cmpEq(BinCriteria.CRITERIAS_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals("field1", criteria.getCriterias().get(0).getField());
            return null;
        });

        observer.update(EasyMock.eq(criteria), EasyMock.cmpEq(BinCriteria.CRITERIAS_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertTrue(criteria.getCriterias().isEmpty());
            return null;
        });

        support.replayAll();

        criteria.addObserver(observer);
        criteria.getCriterias().add(element);
        element.setField("field1");
        criteria.getCriterias().remove(element);
        assertEquals(0, element.countObservers());

        support.verifyAll();
    }

    /**
     * Tests update events after being serialized.
     *
     * @throws JAXBException Bad Jaxb.
     */
    @Test
    public void testNotificationsAfterSerialization() throws JAXBException
    {
        EasyMockSupport support = new EasyMockSupport();

        BinCriteria criterias = new BinCriteria();
        criterias.setDataTypeKey("layerid2");

        BinCriteriaElement unique = new BinCriteriaElement();
        unique.setCriteriaType(new UniqueCriteria());
        unique.setField("field1");

        criterias.getCriterias().add(unique);

        BinCriteriaElement range = new BinCriteriaElement();
        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setBinWidth(10);
        range.setCriteriaType(rangeCriteria);
        range.setField("field2");

        criterias.getCriterias().add(range);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(criterias, output);

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        BinCriteria actual = XMLUtilities.readXMLObject(input, BinCriteria.class);

        Observer observer = support.createMock(Observer.class);
        observer.update(EasyMock.eq(actual), EasyMock.cmpEq(BinCriteria.CRITERIAS_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals("field1", actual.getCriterias().get(0).getField());
            return null;
        });

        support.replayAll();

        actual.addObserver(observer);
        actual.getCriterias().get(0).setField("field1");

        support.verifyAll();
    }

    /**
     * Tests serialization.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testSerialization() throws JAXBException
    {
        BinCriteria criterias = new BinCriteria();
        criterias.setDataTypeKey("layerid2");

        BinCriteriaElement unique = new BinCriteriaElement();
        unique.setCriteriaType(new UniqueCriteria());
        unique.setField("field1");

        criterias.getCriterias().add(unique);

        BinCriteriaElement range = new BinCriteriaElement();
        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setBinWidth(10);
        range.setCriteriaType(rangeCriteria);
        range.setField("field2");

        criterias.getCriterias().add(range);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(criterias, output);

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        BinCriteria actual = XMLUtilities.readXMLObject(input, BinCriteria.class);

        assertEquals("layerid2", actual.getDataTypeKey());

        List<BinCriteriaElement> elements = actual.getCriterias();
        assertEquals(2, elements.size());

        assertEquals("field1", elements.get(0).getField());
        assertTrue(elements.get(0).getCriteriaType() instanceof UniqueCriteria);

        assertEquals("field2", elements.get(1).getField());

        RangeCriteria actualRange = (RangeCriteria)elements.get(1).getCriteriaType();
        assertEquals(10, actualRange.getBinWidth(), 0d);
    }

    /**
     * Tests setting the data type key.
     */
    @Test
    public void testSetDataTypeKey()
    {
        EasyMockSupport support = new EasyMockSupport();

        BinCriteria criteria = new BinCriteria();

        Observer observer = support.createMock(Observer.class);
        observer.update(EasyMock.eq(criteria), EasyMock.cmpEq(BinCriteria.DATA_TYPE_KEY_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals("layerId1", criteria.getDataTypeKey());
            return null;
        });

        support.replayAll();

        criteria.addObserver(observer);
        criteria.setDataTypeKey("layerId1");

        support.verifyAll();
    }
}
