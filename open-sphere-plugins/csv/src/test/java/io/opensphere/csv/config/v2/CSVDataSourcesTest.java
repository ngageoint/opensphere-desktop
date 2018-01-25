package io.opensphere.csv.config.v2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.csvcommon.config.v2.CSVColumnFormat;

/** Tests for {@link CSVDataSources}. */
public class CSVDataSourcesTest
{
    /**
     * Test marshalling and unmarshalling.
     *
     * @throws JAXBException If there is a JAXB error.
     */
    @Test
    public void testMarshalling() throws JAXBException
    {
        CSVDataSources input = new CSVDataSources();
        CSVDataSource source1 = CSVDataSourceTest.getTestObject();
        CSVDataSource source2 = CSVDataSourceTest.getTestObject();
        source2.setName("source2");
        CSVDataSource source3 = CSVDataSourceTest.getTestObject();
        source3.setName("source3");
        input.addSource(source1);
        input.addSource(source2);
        input.addSource(source3);

        Assert.assertEquals(3, input.getSourceList().size());

        input.removeSource(source3);

        Assert.assertEquals(2, input.getSourceList().size());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(input, outputStream, CSVDataSources.class.getPackage(), CSVColumnFormat.class.getPackage());
        CSVDataSources result = XMLUtilities.readXMLObject(new ByteArrayInputStream(outputStream.toByteArray()),
                CSVDataSources.class, CSVDataSources.class.getPackage());

        Assert.assertEquals(input, result);
    }

    /**
     * Test clone.
     */
    @Test
    public void testClone()
    {
        CSVDataSources input = new CSVDataSources();
        input.addSource(CSVDataSourceTest.getTestObject());
        CSVDataSources clone = input.clone();
        Assert.assertTrue(Utilities.notSameInstance(input, clone));
        Assert.assertTrue(Utilities.sameInstance(input.getClass(), clone.getClass()));
        Assert.assertEquals(input, clone);
    }
}
