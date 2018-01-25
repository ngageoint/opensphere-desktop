package io.opensphere.csvcommon.config.v2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.csvcommon.config.v1.CSVColumnInfo;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;

/** Tests for {@link CSVParseParameters}. */
public class CSVParseParametersTest
{
    /** Column for testing. */
    private static final String COL1 = "col1";

    /** Column for testing. */
    private static final String COL2 = "col2";

    /** Column for testing. */
    private static final String COL3 = "col3";

    /** Column for testing. */
    private static final String COL4 = "col4";

    /** Test {@link CSVParseParameters#equals(Object)}. */
    @Test
    public void testEquals()
    {
        CSVParseParameters aa = new CSVParseParameters();
        CSVParseParameters bb = new CSVParseParameters();

        Assert.assertTrue(aa.equals(bb));

        aa.setColumnFormat(new CSVFixedWidthColumnFormat(new int[] { 1, 4, 7 }));
        Assert.assertFalse(aa.equals(bb));
        bb.setColumnFormat(new CSVFixedWidthColumnFormat(new int[] { 2, 4, 7 }));
        Assert.assertFalse(aa.equals(bb));
        aa.setColumnFormat(new CSVFixedWidthColumnFormat(new int[] { 2, 4, 7 }));
        Assert.assertTrue(aa.equals(bb));
        aa.setColumnFormat(new CSVDelimitedColumnFormat(":", ">", 4));
        Assert.assertFalse(aa.equals(bb));
        bb.setColumnFormat(new CSVDelimitedColumnFormat(":", ">", 5));
        Assert.assertFalse(aa.equals(bb));
        bb.setColumnFormat(new CSVDelimitedColumnFormat(":", "<", 4));
        Assert.assertFalse(aa.equals(bb));
        bb.setColumnFormat(new CSVDelimitedColumnFormat(";", ">", 4));
        Assert.assertFalse(aa.equals(bb));
        bb.setColumnFormat(new CSVDelimitedColumnFormat(":", ">", 4));
        Assert.assertTrue(aa.equals(bb));

        aa.setColumnNames(Arrays.asList(COL1, COL2, COL3));
        Assert.assertFalse(aa.equals(bb));
        bb.setColumnNames(Arrays.asList(COL4, COL2, COL3));
        Assert.assertFalse(aa.equals(bb));
        aa.setColumnNames(Arrays.asList(COL4, COL2, COL3));
        Assert.assertTrue(aa.equals(bb));

        aa.setCommentIndicator("!");
        Assert.assertFalse(aa.equals(bb));
        bb.setCommentIndicator("$");
        Assert.assertFalse(aa.equals(bb));
        aa.setCommentIndicator("$");
        Assert.assertTrue(aa.equals(bb));

        aa.setDataStartLine(Integer.valueOf(4));
        Assert.assertFalse(aa.equals(bb));
        bb.setDataStartLine(Integer.valueOf(5));
        Assert.assertFalse(aa.equals(bb));
        aa.setDataStartLine(Integer.valueOf(5));
        Assert.assertTrue(aa.equals(bb));

        aa.setHeaderLine(Integer.valueOf(2));
        Assert.assertFalse(aa.equals(bb));
        bb.setHeaderLine(Integer.valueOf(1));
        Assert.assertFalse(aa.equals(bb));
        aa.setHeaderLine(Integer.valueOf(1));
        Assert.assertTrue(aa.equals(bb));
    }

    /** Test {@link CSVParseParameters#hashCode()}. */
    @Test
    public void testHashCode()
    {
        CSVParseParameters aa = new CSVParseParameters();
        CSVParseParameters bb = new CSVParseParameters();

        Assert.assertTrue(aa.hashCode() == bb.hashCode());

        aa.setColumnFormat(new CSVFixedWidthColumnFormat(new int[] { 1, 4, 7 }));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        bb.setColumnFormat(new CSVFixedWidthColumnFormat(new int[] { 2, 4, 7 }));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        aa.setColumnFormat(new CSVFixedWidthColumnFormat(new int[] { 2, 4, 7 }));
        Assert.assertTrue(aa.hashCode() == bb.hashCode());
        aa.setColumnFormat(new CSVDelimitedColumnFormat(":", ">", 4));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        bb.setColumnFormat(new CSVDelimitedColumnFormat(":", ">", 5));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        bb.setColumnFormat(new CSVDelimitedColumnFormat(":", "<", 4));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        bb.setColumnFormat(new CSVDelimitedColumnFormat(";", ">", 4));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        bb.setColumnFormat(new CSVDelimitedColumnFormat(":", ">", 4));
        Assert.assertTrue(aa.hashCode() == bb.hashCode());

        aa.setColumnNames(Arrays.asList(COL1, COL2, COL3));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        bb.setColumnNames(Arrays.asList(COL4, COL2, COL3));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        aa.setColumnNames(Arrays.asList(COL4, COL2, COL3));
        Assert.assertTrue(aa.hashCode() == bb.hashCode());

        aa.setCommentIndicator("!");
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        bb.setCommentIndicator("$");
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        aa.setCommentIndicator("$");
        Assert.assertTrue(aa.hashCode() == bb.hashCode());

        aa.setDataStartLine(Integer.valueOf(4));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        bb.setDataStartLine(Integer.valueOf(5));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        aa.setDataStartLine(Integer.valueOf(5));
        Assert.assertTrue(aa.hashCode() == bb.hashCode());

        aa.setHeaderLine(Integer.valueOf(2));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        bb.setHeaderLine(Integer.valueOf(1));
        Assert.assertFalse(aa.hashCode() == bb.hashCode());
        aa.setHeaderLine(Integer.valueOf(1));
        Assert.assertTrue(aa.hashCode() == bb.hashCode());
    }

    /**
     * Test marshalling and unmarshalling.
     *
     * @throws JAXBException If there is a JAXB error.
     */
    @Test
    public void testMarshalling() throws JAXBException
    {
        CSVParseParameters input = getTestObject();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtilities.writeXMLObject(input, baos, CSVParseParameters.class.getPackage());
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        CSVParseParameters result = XMLUtilities.readXMLObject(bais, CSVParseParameters.class,
                CSVParseParameters.class.getPackage());

        Assert.assertTrue(input.equals(result));

        input.setColumnFormat(new CSVFixedWidthColumnFormat(new int[] { 3, 6, 9 }));
        baos.reset();
        XMLUtilities.writeXMLObject(input, baos, CSVParseParameters.class.getPackage());
        bais = new ByteArrayInputStream(baos.toByteArray());
        result = XMLUtilities.readXMLObject(bais, CSVParseParameters.class, CSVParseParameters.class.getPackage());

        Assert.assertTrue(input.equals(result));
    }

    /**
     * Test clone.
     */
    @Test
    public void testClone()
    {
        CSVParseParameters input = getTestObject();
        CSVParseParameters clone = input.clone();
        Assert.assertTrue(Utilities.notSameInstance(input, clone));
        Assert.assertTrue(Utilities.sameInstance(input.getClass(), clone.getClass()));
        Assert.assertEquals(input, clone);
    }

    /**
     * Creates a CSVParseParameters test object.
     *
     * @return the CSVParseParameters
     */
    private static CSVParseParameters getTestObject()
    {
        CSVParseParameters parseParameters = new CSVParseParameters();
        SpecialColumn dateColumn = new SpecialColumn();
        dateColumn.setColumnIndex(5);
        dateColumn.setColumnType(ColumnType.DATE);
        dateColumn.setFormat("format");
        parseParameters.getSpecialColumns().add(dateColumn);
        parseParameters.setColumnFormat(new CSVDelimitedColumnFormat(":", "'", 4));
        parseParameters.setColumnNames(Arrays.asList(COL1, COL2, COL3));
        parseParameters.setCommentIndicator("!");
        parseParameters.setDataStartLine(Integer.valueOf(5));
        parseParameters.setHeaderLine(Integer.valueOf(2));
        parseParameters.getColumnClasses().add(new CSVColumnInfo("java.lang.Float", 1, 2, true));
        return parseParameters;
    }
}
