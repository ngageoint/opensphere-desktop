package io.opensphere.geopackage.importer;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.importer.ImporterRegistry;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;

/**
 * Tests the {@link GeoPackageImportController}.
 */
public class GeoPackageImportControllerTest
{
    /**
     * Tests that the {@link GeoPackageImportController} adds the importer to
     * the system correctly.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);

        support.replayAll();

        try (GeoPackageImportController controller = new GeoPackageImportController(toolbox, New.set()))
        {
            controller.open();
        }

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link Toolbox}.
     */
    private Toolbox createToolbox(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        UIRegistry uiRegistry = support.createMock(UIRegistry.class);

        Service service = support.createMock(Service.class);
        service.open();
        service.close();

        ImporterRegistry importerRegistry = support.createMock(ImporterRegistry.class);
        EasyMock.expect(importerRegistry.createImporterService(EasyMock.isA(GeoPackageImporter.class))).andReturn(service);

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getDataRegistry()).andReturn(registry);
        EasyMock.expect(toolbox.getUIRegistry()).andReturn(uiRegistry);
        EasyMock.expect(toolbox.getImporterRegistry()).andReturn(importerRegistry);

        return toolbox;
    }
}
