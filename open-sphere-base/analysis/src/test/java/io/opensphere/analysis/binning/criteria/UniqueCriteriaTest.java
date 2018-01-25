package io.opensphere.analysis.binning.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import io.opensphere.core.util.XMLUtilities;

/**
 * Unit test for {@link UniqueCriteria}.
 */
public class UniqueCriteriaTest
{
    /**
     * Tests the criteria type.
     */
    @Test
    public void testGetCriteriaType()
    {
        assertEquals("Unique", new UniqueCriteria().getCriteriaType());
    }

    /**
     * Verifies this object can move in and out of xml java space.
     *
     * @throws JAXBException Bad Jaxb.
     */
    @Test
    public void testSerialization() throws JAXBException
    {
        UniqueCriteria uniqueCriteria = new UniqueCriteria();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(uniqueCriteria, output);

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        uniqueCriteria = XMLUtilities.readXMLObject(input, UniqueCriteria.class);

        assertNotNull(uniqueCriteria);
    }
}
