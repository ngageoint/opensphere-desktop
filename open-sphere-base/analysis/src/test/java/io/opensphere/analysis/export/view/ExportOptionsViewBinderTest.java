package io.opensphere.analysis.export.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.analysis.export.model.ColorFormat;
import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.export.model.LatLonFormat;

/**
 * Unit tests the {@link ExportOptionsViewBinder} class.
 */
public class ExportOptionsViewBinderTest
{
    /**
     * Verifies that when the include meta columns is checked, that the color
     * format is shown, and vice versa.
     */
    @Test
    public void testColorFormatVisibility()
    {
        EasyMockSupport support = new EasyMockSupport();

        ExportOptionsView view = createVeiw(support);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();

        ExportOptionsViewBinder binder = new ExportOptionsViewBinder(view, model);

        assertFalse(view.getColorFormat().isVisible());
        assertFalse(view.getColorFormatLabel().isVisible());

        view.getIncludeMetaColumns().setSelected(true);
        assertTrue(model.isIncludeMetaColumns());
        assertTrue(view.getColorFormat().isVisible());
        assertTrue(view.getColorFormatLabel().isVisible());

        view.getIncludeMetaColumns().setSelected(false);
        assertFalse(model.isIncludeMetaColumns());
        assertFalse(view.getColorFormat().isVisible());
        assertFalse(view.getColorFormatLabel().isVisible());

        binder.close();

        support.verifyAll();
    }

    /**
     * Verifies changes to model get applied to the view.
     */
    @Test
    public void testUpdateModel()
    {
        EasyMockSupport support = new EasyMockSupport();

        ExportOptionsView view = createVeiw(support);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();

        ExportOptionsViewBinder binder = new ExportOptionsViewBinder(view, model);

        assertFalse(view.getAddWKT().isSelected());

        assertEquals(ColorFormat.HEXADECIMAL, view.getColorFormat().getSelectedItem());
        assertEquals(ColorFormat.HEXADECIMAL, view.getColorFormat().getItemAt(0));
        assertEquals(ColorFormat.RGB_CODED, view.getColorFormat().getItemAt(1));
        assertEquals(2, view.getColorFormat().getItemCount());
        assertFalse(view.getColorFormat().isVisible());
        assertFalse(view.getColorFormat().isVisible());

        assertFalse(view.getIncludeMetaColumns().isSelected());

        assertEquals(LatLonFormat.DECIMAL, view.getLatLonFormat().getSelectedItem());
        assertEquals(LatLonFormat.DECIMAL, view.getLatLonFormat().getItemAt(0));
        assertEquals(LatLonFormat.DMS, view.getLatLonFormat().getItemAt(1));
        assertEquals(LatLonFormat.DMS_CUSTOM, view.getLatLonFormat().getItemAt(2));
        assertEquals(3, view.getLatLonFormat().getItemCount());

        assertFalse(view.getSelectedRowsOnly().isSelected());
        assertFalse(view.getSeparateDateTimeColumns().isSelected());

        model.setAddWkt(true);
        assertTrue(view.getAddWKT().isSelected());

        model.setIncludeMetaColumns(true);
        assertTrue(view.getIncludeMetaColumns().isSelected());
        assertTrue(view.getColorFormat().isVisible());
        assertTrue(view.getColorFormatLabel().isVisible());

        model.setSelectedColorFormat(ColorFormat.RGB_CODED);
        assertEquals(ColorFormat.RGB_CODED, view.getColorFormat().getSelectedItem());

        model.setSelectedLatLonFormat(LatLonFormat.DMS);
        assertEquals(LatLonFormat.DMS, view.getLatLonFormat().getSelectedItem());

        model.setSelectedRowsOnly(true);
        assertTrue(view.getSelectedRowsOnly().isSelected());

        model.setSeparateDateTimeColumns(true);
        assertTrue(view.getSeparateDateTimeColumns().isSelected());

        binder.close();

        support.verifyAll();
    }

    /**
     * Verifies changes to model get applied to the view.
     */
    @Test
    public void testUpdateView()
    {
        EasyMockSupport support = new EasyMockSupport();

        ExportOptionsView view = createVeiw(support);

        support.replayAll();

        ExportOptionsModel model = new ExportOptionsModel();

        ExportOptionsViewBinder binder = new ExportOptionsViewBinder(view, model);

        view.getAddWKT().setSelected(true);
        assertTrue(model.isAddWkt());

        view.getIncludeMetaColumns().setSelected(true);
        assertTrue(model.isIncludeMetaColumns());
        assertTrue(view.getColorFormat().isVisible());
        assertTrue(view.getColorFormatLabel().isVisible());

        view.getColorFormat().setSelectedItem(ColorFormat.RGB_CODED);
        assertEquals(ColorFormat.RGB_CODED, model.getSelectedColorFormat());

        view.getLatLonFormat().setSelectedItem(LatLonFormat.DMS);
        assertEquals(LatLonFormat.DMS, model.getSelectedLatLonFormat());

        view.getSelectedRowsOnly().setSelected(true);
        assertTrue(model.isSelectedRowsOnly());

        view.getSeparateDateTimeColumns().setSelected(true);
        assertTrue(model.isSeparateDateTimeColumns());

        binder.close();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ExportOptionsView}.
     *
     * @param support Used to create the mock.
     * @return The mock.
     */
    private ExportOptionsView createVeiw(EasyMockSupport support)
    {
        ExportOptionsView view = support.createMock(ExportOptionsView.class);

        EasyMock.expect(view.getAddWKT()).andReturn(new JCheckBox()).atLeastOnce();
        EasyMock.expect(view.getColorFormat()).andReturn(new JComboBox<>()).atLeastOnce();
        EasyMock.expect(view.getColorFormatLabel()).andReturn(new JLabel()).atLeastOnce();
        EasyMock.expect(view.getIncludeMetaColumns()).andReturn(new JCheckBox()).atLeastOnce();
        EasyMock.expect(view.getLatLonFormat()).andReturn(new JComboBox<>()).atLeastOnce();
        EasyMock.expect(view.getSelectedRowsOnly()).andReturn(new JCheckBox()).atLeastOnce();
        EasyMock.expect(view.getSeparateDateTimeColumns()).andReturn(new JCheckBox()).atLeastOnce();

        return view;
    }
}
