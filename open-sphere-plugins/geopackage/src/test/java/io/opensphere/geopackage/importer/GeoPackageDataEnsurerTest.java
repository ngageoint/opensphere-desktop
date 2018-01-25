package io.opensphere.geopackage.importer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.util.collections.New;
import mil.nga.geopackage.GeoPackageConstants;

/**
 * Tests the {@link GeoPackageDataEnsurer} class.
 */
public class GeoPackageDataEnsurerTest
{
    /**
     * Tests the ensure data when everything is all there, nothing is there, and
     * when the file has moved.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testEnsureDataAllThere() throws IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        File allThere = File.createTempFile("allThere", "." + GeoPackageConstants.GEOPACKAGE_EXTENSION);
        allThere.deleteOnExit();

        File nada = File.createTempFile("nada", "." + GeoPackageConstants.GEOPACKAGE_EXTENSION);
        nada.deleteOnExit();

        File nonExistent = new File(
                System.getProperty("nonexistentFile", "c:\\nonexistent." + GeoPackageConstants.GEOPACKAGE_EXTENSION));

        Set<String> imports = New.set(allThere.toString(), nada.toString(), nonExistent.toString());

        FileOrURLImporter importer = createImporter(support, nada);
        DataRegistry registry = createRegistry(support, allThere, nada);

        support.replayAll();

        GeoPackageDataEnsurer ensurer = new GeoPackageDataEnsurer(importer, registry);
        ensurer.ensureData(imports);

        support.verifyAll();
    }

    /**
     * Creates a mocked {@link FileOrURLImporter}.
     *
     * @param support Used to create the mock.
     * @param nada The expected file to be imported.
     * @return The mocked {@link FileOrURLImporter}.
     */
    private FileOrURLImporter createImporter(EasyMockSupport support, File nada)
    {
        FileOrURLImporter importer = support.createMock(FileOrURLImporter.class);

        importer.importFile(EasyMock.eq(nada), EasyMock.isNull());

        return importer;
    }

    /**
     * Creates a mocked {@link DataRegistry}.
     *
     * @param support Used to create the mock.
     * @param allThere The file to expect for all data being in the registry.
     * @param nada The file to expect for no data to be in the registry.
     * @return The mocked {@link DataRegistry}.
     */
    private DataRegistry createRegistry(EasyMockSupport support, File allThere, File nada)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        EasyMock.expect(registry.performLocalQuery(EasyMock.isA(DefaultQuery.class))).andAnswer(() -> localQueryAnswer(allThere))
                .times(2);
        EasyMock.expect(
                registry.removeModels(EasyMock.eq(new DataModelCategory(nada.toString(), null, null)), EasyMock.eq(false)))
                .andReturn(new long[0]);

        return registry;
    }

    /**
     * The answer for the local query mock call.
     *
     * @param allThere The file to expect for all data being in the registry.
     * @return The model ids.
     */
    private long[] localQueryAnswer(File allThere)
    {
        long[] ids = new long[0];

        DefaultQuery query = (DefaultQuery)EasyMock.getCurrentArguments()[0];

        assertTrue(query.getPropertyValueReceivers().isEmpty());
        DataModelCategory category = query.getDataModelCategory();
        assertNull(category.getFamily());
        assertNull(category.getCategory());

        if (allThere.equals(new File(category.getSource())))
        {
            ids = new long[1];
        }

        return ids;
    }
}
