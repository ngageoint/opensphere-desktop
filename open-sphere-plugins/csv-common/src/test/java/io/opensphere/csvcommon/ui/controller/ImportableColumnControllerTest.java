package io.opensphere.csvcommon.ui.controller;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;

/**
 * Tests the ImportableColumnController class.
 *
 */
@SuppressWarnings("boxing")
public class ImportableColumnControllerTest
{
    /**
     * Tests the ImportableColumnController class.
     */
    @Test
    public void testDetectNonImportableColumns()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = createRegistry(support);

        CSVParseParameters parameters = new CSVParseParameters();
        parameters.setColumnNames(New.list("Column1", "Column2", "Column3", "Column4", "Column5"));

        support.replayAll();

        ImportableColumnController controller = new ImportableColumnController();
        controller.detectNonImportableColumns(parameters, registry);

        assertTrue(parameters.getColumnsToIgnore().contains(1));
        assertTrue(parameters.getColumnsToIgnore().contains(2));

        support.verifyAll();
    }

    /**
     * Creates the easy mocked preferences registry.
     *
     * @param support The easy mock support.
     * @return The preferences registry.
     */
    @SuppressWarnings("unchecked")
    private PreferencesRegistry createRegistry(EasyMockSupport support)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.getStringList(EasyMock.cmpEq("NonImportable_column1"), (List<String>)EasyMock.isNull());
        EasyMock.expectLastCall().andReturn(New.list("COLUMN2", "COLUMN3", "COLUMN6"));

        preferences.getStringList(EasyMock.isA(String.class), (List<String>)EasyMock.isNull());
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.expectLastCall().atLeastOnce();

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.eq(CSVColumnPrefsUtil.class));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().atLeastOnce();

        return registry;
    }
}
