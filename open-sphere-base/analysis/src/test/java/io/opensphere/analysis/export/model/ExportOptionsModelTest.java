package io.opensphere.analysis.export.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Observer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

/**
 * Unit test for {@link ExportOptionsModel}.
 */
public class ExportOptionsModelTest
{
    /**
     * Tests the add wkt property.
     */
    @Test
    public void testSetAddWkt()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, ExportOptionsModel.ADD_WKT_PROP);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();
        model.addObserver(observer);
        assertFalse(model.isAddWkt());
        model.setAddWkt(true);
        assertTrue(model.isAddWkt());

        support.verifyAll();
    }

    /**
     * Tests the include meta columns property.
     */
    @Test
    public void testSetIncludeMetaColumns()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, ExportOptionsModel.INCLUDE_META_COLUMNS_PROP);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();
        model.addObserver(observer);
        assertFalse(model.isIncludeMetaColumns());
        model.setIncludeMetaColumns(true);
        assertTrue(model.isIncludeMetaColumns());

        support.verifyAll();
    }

    /**
     * Tests the color format property.
     */
    @Test
    public void testSetSelectedColorFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, ExportOptionsModel.SELECTED_COLOR_FORMAT_PROP);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();
        model.addObserver(observer);
        assertEquals(ColorFormat.HEXADECIMAL, model.getSelectedColorFormat());
        model.setSelectedColorFormat(ColorFormat.RGB_CODED);
        assertEquals(ColorFormat.RGB_CODED, model.getSelectedColorFormat());

        support.verifyAll();
    }

    /**
     * Tests the lat lon format property.
     */
    @Test
    public void testSetSelectedLatLonFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, ExportOptionsModel.SELECTED_LAT_LON_FORMAT_PROP);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();
        model.addObserver(observer);
        assertEquals(LatLonFormat.DECIMAL, model.getSelectedLatLonFormat());
        model.setSelectedLatLonFormat(LatLonFormat.DMS);
        assertEquals(LatLonFormat.DMS, model.getSelectedLatLonFormat());

        support.verifyAll();
    }

    /**
     * Tests the selected rows only property.
     */
    @Test
    public void testSetSelectedRowsOnly()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, ExportOptionsModel.SELECTED_ROWS_ONLY_PROP);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();
        model.addObserver(observer);
        assertFalse(model.isSelectedRowsOnly());
        model.setSelectedRowsOnly(true);
        assertTrue(model.isSelectedRowsOnly());

        support.verifyAll();
    }

    /**
     * Tests the seperate date time columns property.
     */
    @Test
    public void testSetSeperateDateTimeColumns()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = createObserver(support, ExportOptionsModel.SEPERATE_DATE_TIME_COLUMNS_PROP);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();
        model.addObserver(observer);
        assertFalse(model.isSeparateDateTimeColumns());
        model.setSeparateDateTimeColumns(true);
        assertTrue(model.isSeparateDateTimeColumns());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked observer.
     *
     * @param support Used to create the mock.
     * @param expectedProp The expected property name to be notified of change.
     * @return The mocked observer.
     */
    private Observer createObserver(EasyMockSupport support, String expectedProp)
    {
        Observer observer = support.createMock(Observer.class);

        observer.update(EasyMock.isA(ExportOptionsModel.class), EasyMock.cmpEq(expectedProp));

        return observer;
    }
}
