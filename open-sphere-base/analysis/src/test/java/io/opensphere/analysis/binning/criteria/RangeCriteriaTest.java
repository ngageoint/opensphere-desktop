package io.opensphere.analysis.binning.criteria;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Observer;

import javax.xml.bind.JAXBException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link RangeCriteria}.
 */
public class RangeCriteriaTest
{
    /**
     * Tests the {@link RangeCriteria} notifying of changes.
     */
    @Test
    public void testNotification()
    {
        EasyMockSupport support = new EasyMockSupport();

        RangeCriteria rangeCriteria = new RangeCriteria();

        assertEquals(10, rangeCriteria.getBinWidth(), 0);
        Observer observer = support.createMock(Observer.class);
        observer.update(EasyMock.eq(rangeCriteria), EasyMock.cmpEq(RangeCriteria.BIN_WIDTH_PROP));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            assertEquals(10, rangeCriteria.getBinWidth(), 0d);
            return null;
        });

        support.replayAll();

        rangeCriteria.addObserver(observer);
        rangeCriteria.setBinWidth(10);

        support.verifyAll();
    }

    /**
     * Tests the {@link RangeCriteria} serialization.
     *
     * @throws JAXBException Bad jaxb.
     */
    @Test
    public void testSerialization() throws JAXBException
    {
        RangeCriteria rangeCriteria = new RangeCriteria();
        rangeCriteria.setBinWidth(100.1);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(rangeCriteria, output);

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        rangeCriteria = XMLUtilities.readXMLObject(input, RangeCriteria.class);

        assertEquals(100.1, rangeCriteria.getBinWidth(), 0d);
        assertEquals("Range", rangeCriteria.getCriteriaType());
    }
}
