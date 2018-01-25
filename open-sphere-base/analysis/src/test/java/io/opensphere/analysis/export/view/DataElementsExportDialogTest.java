package io.opensphere.analysis.export.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.core.util.MimeType;

/**
 * Unit test for the {@link DataElementsExportDialog}.
 */
public class DataElementsExportDialogTest
{
    /**
     * Tests that the view and the model stay in sync.
     */
    @Test
    public void test()
    {
        ExportOptionsModel model = new ExportOptionsModel();
        MimeTypeFileFilter fileFilter = new MimeTypeFileFilter(MimeType.KML);
        DataElementsExportDialog exportDialog = new DataElementsExportDialog(null, fileFilter, model);

        ExportOptionsView optionsView = exportDialog.getExportOptions();

        assertFalse(model.isAddWkt());
        optionsView.getAddWKT().setSelected(true);
        assertEquals(fileFilter, exportDialog.getChoosableFileFilters()[1]);
        assertEquals(fileFilter, exportDialog.getFileFilter());

        assertTrue(model.isAddWkt());
    }
}
