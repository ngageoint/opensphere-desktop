package io.opensphere.analysis.binning.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;

/**
 * Unit test for {@link DataElementCriteriaBinner}.
 */
public class DataElementCriteriaBinnerTest
{
    /**
     * The test color column.
     */
    private static final String ourColorColumn = "Color";

    /**
     * The test data type key.
     */
    private static final String ourDtiKey = "iamkey";

    /**
     * The test name column.
     */
    private static final String ourNameColumn = "Name";

    /**
     * Orange.
     */
    private static final String ourOrange = "orange";

    /**
     * The test taste column.
     */
    private static final String ourTasteColumn = "Taste";

    /**
     * Tests binning with criteria that has multiple criteria elements.
     */
    @Test
    public void testAddAll()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = createDataType(support);
        MantleToolbox mantleBox = createMantleBox(support);
        List<DataElement> data = createFruitData(dataType);
        BinCriteria criteria = createFruitBinCriteria();

        support.replayAll();

        DataElementCriteriaBinner binner = new DataElementCriteriaBinner(mantleBox, criteria, dataType);

        binner.addAll(data);

        List<Bin<DataElement>> bins = binner.getBins();

        Map<String, List<Bin<DataElement>>> colorToBinsMap = New.map();
        for (Bin<DataElement> bin : bins)
        {
            String binColor = bin.getData().get(0).getMetaData().getValue(ourColorColumn).toString();
            if (!colorToBinsMap.containsKey(binColor))
            {
                colorToBinsMap.put(binColor, New.list());
            }

            colorToBinsMap.get(binColor).add(bin);
        }

        assertEquals(5, colorToBinsMap.size());

        // assert red bins.
        List<Bin<DataElement>> redBins = colorToBinsMap.get("red");
        assertEquals(2, redBins.size());
        Bin<DataElement> appleBin = redBins.get(0);
        Bin<DataElement> berryBin = redBins.get(1);
        if (appleBin.getData().size() != 1)
        {
            appleBin = berryBin;
            berryBin = redBins.get(0);
        }

        assertEquals(1, appleBin.getData().size());
        assertEquals("apple", appleBin.getData().get(0).getMetaData().getValue(ourNameColumn));

        Set<String> berries = New.set();
        for (DataElement element : berryBin.getData())
        {
            berries.add(element.getMetaData().getValue(ourNameColumn).toString());
        }

        assertTrue(berries.containsAll(New.list("strawberry", "cherry", "rasberry")));

        // assert blue bin
        List<Bin<DataElement>> blueBins = colorToBinsMap.get("blue");
        assertEquals(1, blueBins.size());
        Bin<DataElement> blueBin = blueBins.get(0);
        assertEquals(1, blueBin.getData().size());
        assertEquals("blueberry", blueBin.getData().get(0).getMetaData().getValue(ourNameColumn));

        // assert green bins
        List<Bin<DataElement>> greenBins = colorToBinsMap.get("green");
        assertEquals(1, greenBins.size());
        Set<String> greenFruit = New.set();
        for (DataElement element : greenBins.get(0).getData())
        {
            greenFruit.add(element.getMetaData().getValue(ourNameColumn).toString());
        }
        assertEquals(2, greenFruit.size());
        assertTrue(greenFruit.containsAll(New.list("kiwi", "grape")));

        // assert yellow bins
        List<Bin<DataElement>> yellowBins = colorToBinsMap.get("yellow");
        assertEquals(1, yellowBins.size());
        Bin<DataElement> yellowBin = yellowBins.get(0);
        assertEquals(1, yellowBin.getData().size());
        assertEquals("bannana", yellowBin.getData().get(0).getMetaData().getValue(ourNameColumn));

        // assert orange bins
        List<Bin<DataElement>> orangeBins = colorToBinsMap.get(ourOrange);
        assertEquals(2, orangeBins.size());
        Bin<DataElement> yummyOrangeBin = orangeBins.get(0);
        Bin<DataElement> cantelopeBin = orangeBins.get(1);
        if (cantelopeBin.getData().size() != 1)
        {
            yummyOrangeBin = cantelopeBin;
            cantelopeBin = orangeBins.get(0);
        }
        assertEquals("cantelope", cantelopeBin.getData().get(0).getMetaData().getValue(ourNameColumn));
        Set<String> yummyOrangeFruit = New.set();
        for (DataElement element : yummyOrangeBin.getData())
        {
            yummyOrangeFruit.add(element.getMetaData().getValue(ourNameColumn).toString());
        }
        assertEquals(2, yummyOrangeFruit.size());
        assertTrue(yummyOrangeFruit.containsAll(New.list(ourOrange, "mango")));

        support.verifyAll();
    }

    /**
     * Tests binning with criteria that has multiple criteria elements.
     */
    @Test
    public void testAddAllUnique()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = createDataType(support);
        MantleToolbox mantleBox = createMantleBox(support);
        List<DataElement> data = createFruitData(dataType);
        addNulls(data, dataType);
        BinCriteria criteria = createFruitBinCriteriaUnique();

        support.replayAll();

        DataElementCriteriaBinner binner = new DataElementCriteriaBinner(mantleBox, criteria, dataType);

        binner.addAll(data);

        Map<Object, Bin<DataElement>> colorToBinsMap = binner.getBinsMap();

        assertEquals(6, colorToBinsMap.size());
        assertEquals(11, colorToBinsMap.get(null).getSize());
        assertEquals(4, colorToBinsMap.get("red").getSize());
        assertEquals(1, colorToBinsMap.get("blue").getSize());
        assertEquals(2, colorToBinsMap.get("green").getSize());
        assertEquals(1, colorToBinsMap.get("yellow").getSize());
        assertEquals(3, colorToBinsMap.get(ourOrange).getSize());

        support.verifyAll();
    }

    /**
     * Tests removing all data.
     */
    @Test
    public void testRemoveAll()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = createDataType(support);
        MantleToolbox mantleBox = createMantleBox(support);
        List<DataElement> data = createFruitData(dataType);
        BinCriteria criteria = createFruitBinCriteria();

        support.replayAll();

        DataElementCriteriaBinner binner = new DataElementCriteriaBinner(mantleBox, criteria, dataType);

        binner.addAll(data);

        List<Bin<DataElement>> bins = binner.getBins();

        assertEquals(7, bins.size());

        for (DataElement element : data)
        {
            binner.remove(element);
        }

        assertTrue(binner.getBins().isEmpty());

        support.verifyAll();
    }

    /**
     * Adds null data to the list of elements.
     *
     * @param elements The list to add to.
     * @param dti The data type of the fruit data.
     */
    private void addNulls(List<DataElement> elements, DataTypeInfo dti)
    {
        Map<String, Serializable> apple = New.map();
        apple.put(ourNameColumn, "apple");
        apple.put(ourColorColumn, null);
        apple.put(ourTasteColumn, Integer.valueOf(7));

        Map<String, Serializable> cherry = New.map();
        cherry.put(ourNameColumn, "cherry");
        cherry.put(ourColorColumn, null);
        cherry.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> strawberry = New.map();
        strawberry.put(ourNameColumn, "strawberry");
        strawberry.put(ourColorColumn, null);
        strawberry.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> blueberry = New.map();
        blueberry.put(ourNameColumn, "blueberry");
        blueberry.put(ourColorColumn, null);
        blueberry.put(ourTasteColumn, Integer.valueOf(1));

        Map<String, Serializable> kiwi = New.map();
        kiwi.put(ourNameColumn, "kiwi");
        kiwi.put(ourColorColumn, null);
        kiwi.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> grape = New.map();
        grape.put(ourNameColumn, "grape");
        grape.put(ourColorColumn, null);
        grape.put(ourTasteColumn, Integer.valueOf(8));

        Map<String, Serializable> bannana = New.map();
        bannana.put(ourNameColumn, "bannana");
        bannana.put(ourColorColumn, null);
        bannana.put(ourTasteColumn, Integer.valueOf(7));

        Map<String, Serializable> mango = New.map();
        mango.put(ourNameColumn, "mango");
        mango.put(ourColorColumn, null);
        mango.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> orange = New.map();
        orange.put(ourNameColumn, ourOrange);
        orange.put(ourColorColumn, null);
        orange.put(ourTasteColumn, Integer.valueOf(8));

        Map<String, Serializable> rasberry = New.map();
        rasberry.put(ourNameColumn, "rasberry");
        rasberry.put(ourColorColumn, null);
        rasberry.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> cantelope = New.map();
        cantelope.put(ourNameColumn, "cantelope");
        cantelope.put(ourColorColumn, null);
        cantelope.put(ourTasteColumn, Integer.valueOf(1));

        DefaultDataElement element = new DefaultDataElement(0, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(apple));
        elements.add(element);

        element = new DefaultDataElement(1, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(cherry));
        elements.add(element);

        element = new DefaultDataElement(2, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(strawberry));
        elements.add(element);

        element = new DefaultDataElement(3, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(blueberry));
        elements.add(element);

        element = new DefaultDataElement(4, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(kiwi));
        elements.add(element);

        element = new DefaultDataElement(5, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(grape));
        elements.add(element);

        element = new DefaultDataElement(6, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(bannana));
        elements.add(element);

        element = new DefaultDataElement(7, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(mango));
        elements.add(element);

        element = new DefaultDataElement(8, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(orange));
        elements.add(element);

        element = new DefaultDataElement(9, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(rasberry));
        elements.add(element);

        element = new DefaultDataElement(0, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(cantelope));
        elements.add(element);
    }

    /**
     * Creates an easy mocked {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link DataTypeInfo}.
     */
    private DataTypeInfo createDataType(EasyMockSupport support)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        return dataType;
    }

    /**
     * Creates the bin criteria used for the fruit binning test.
     *
     * @return The fruit bin criteria.
     */
    private BinCriteria createFruitBinCriteria()
    {
        BinCriteria criteria = createFruitBinCriteriaUnique();

        BinCriteriaElement flavorRange = new BinCriteriaElement();
        RangeCriteria range = new RangeCriteria();
        range.setBinWidth(2);
        flavorRange.setCriteriaType(range);
        flavorRange.setField(ourTasteColumn);
        criteria.getCriterias().add(flavorRange);

        return criteria;
    }

    /**
     * Creates the bin criteria used for the fruit binning test.
     *
     * @return The fruit bin criteria.
     */
    private BinCriteria createFruitBinCriteriaUnique()
    {
        BinCriteria criteria = new BinCriteria();
        criteria.setDataTypeKey(ourDtiKey);

        BinCriteriaElement uniqueColor = new BinCriteriaElement();
        uniqueColor.setCriteriaType(new UniqueCriteria());
        uniqueColor.setField(ourColorColumn);
        criteria.getCriterias().add(uniqueColor);

        return criteria;
    }

    /**
     * Creates some fruit data.
     *
     * @param dti The data type of the fruit data.
     * @return The test fruit data.
     */
    private List<DataElement> createFruitData(DataTypeInfo dti)
    {
        Map<String, Serializable> apple = New.map();
        apple.put(ourNameColumn, "apple");
        apple.put(ourColorColumn, "red");
        apple.put(ourTasteColumn, Integer.valueOf(7));

        Map<String, Serializable> cherry = New.map();
        cherry.put(ourNameColumn, "cherry");
        cherry.put(ourColorColumn, "red");
        cherry.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> strawberry = New.map();
        strawberry.put(ourNameColumn, "strawberry");
        strawberry.put(ourColorColumn, "red");
        strawberry.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> blueberry = New.map();
        blueberry.put(ourNameColumn, "blueberry");
        blueberry.put(ourColorColumn, "blue");
        blueberry.put(ourTasteColumn, Integer.valueOf(1));

        Map<String, Serializable> kiwi = New.map();
        kiwi.put(ourNameColumn, "kiwi");
        kiwi.put(ourColorColumn, "green");
        kiwi.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> grape = New.map();
        grape.put(ourNameColumn, "grape");
        grape.put(ourColorColumn, "green");
        grape.put(ourTasteColumn, Integer.valueOf(8));

        Map<String, Serializable> bannana = New.map();
        bannana.put(ourNameColumn, "bannana");
        bannana.put(ourColorColumn, "yellow");
        bannana.put(ourTasteColumn, Integer.valueOf(7));

        Map<String, Serializable> mango = New.map();
        mango.put(ourNameColumn, "mango");
        mango.put(ourColorColumn, ourOrange);
        mango.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> orange = New.map();
        orange.put(ourNameColumn, ourOrange);
        orange.put(ourColorColumn, ourOrange);
        orange.put(ourTasteColumn, Integer.valueOf(8));

        Map<String, Serializable> rasberry = New.map();
        rasberry.put(ourNameColumn, "rasberry");
        rasberry.put(ourColorColumn, "red");
        rasberry.put(ourTasteColumn, Integer.valueOf(9));

        Map<String, Serializable> cantelope = New.map();
        cantelope.put(ourNameColumn, "cantelope");
        cantelope.put(ourColorColumn, ourOrange);
        cantelope.put(ourTasteColumn, Integer.valueOf(1));

        List<DataElement> elements = New.list();

        DefaultDataElement element = new DefaultDataElement(0, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(apple));
        elements.add(element);

        element = new DefaultDataElement(1, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(cherry));
        elements.add(element);

        element = new DefaultDataElement(2, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(strawberry));
        elements.add(element);

        element = new DefaultDataElement(3, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(blueberry));
        elements.add(element);

        element = new DefaultDataElement(4, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(kiwi));
        elements.add(element);

        element = new DefaultDataElement(5, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(grape));
        elements.add(element);

        element = new DefaultDataElement(6, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(bannana));
        elements.add(element);

        element = new DefaultDataElement(7, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(mango));
        elements.add(element);

        element = new DefaultDataElement(8, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(orange));
        elements.add(element);

        element = new DefaultDataElement(9, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(rasberry));
        elements.add(element);

        element = new DefaultDataElement(0, TimeSpan.TIMELESS, dti, new SimpleMetaDataProvider(cantelope));
        elements.add(element);

        return elements;
    }

    /**
     * Creates an easy mocked {@link MantleToolbox}.
     *
     * @param support Used to create the mock.
     * @return The mocked mantle toolbox.
     */
    private MantleToolbox createMantleBox(EasyMockSupport support)
    {
        MantleToolbox mantle = support.createMock(MantleToolbox.class);

        return mantle;
    }
}
