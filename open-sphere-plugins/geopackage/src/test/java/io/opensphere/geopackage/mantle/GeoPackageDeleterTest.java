package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Tests the {@link GeoPackageDeleter} class.
 */
public class GeoPackageDeleterTest
{
    /**
     * The test package file.
     */
    private static final String ourPackageFile = "c:\\somePackage.gpkg";

    /**
     * Tests the can delete group.
     */
    @Test
    public void testCanDeleteGroup()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo dataGroup = support.createMock(DataGroupInfo.class);
        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);
        DataRegistry dataRegistry = support.createMock(DataRegistry.class);
        PackageDeleteListener deleteListener = support.createMock(PackageDeleteListener.class);

        support.replayAll();

        GeoPackageDeleter deleter = new GeoPackageDeleter(mantleToolbox, dataRegistry, deleteListener);

        assertTrue(deleter.canDeleteGroup(dataGroup));

        support.verifyAll();
    }

    /**
     * Tests deleting the group.
     */
    @Test
    public void testDeleteGroup()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo dataGroup = createDataGroup(support);
        MantleToolbox mantleToolbox = createMantleToolbox(support, dataGroup);
        DataRegistry registry = createRegistry(support);
        PackageDeleteListener deleteListener = support.createMock(PackageDeleteListener.class);
        deleteListener.packageDeleted();

        support.replayAll();

        GeoPackageDeleter deleter = new GeoPackageDeleter(mantleToolbox, registry, deleteListener);
        deleter.deleteGroup(dataGroup, this);

        support.verifyAll();
    }

    /**
     * Tests deleting the group when the listener is null.
     */
    @Test
    public void testDeleteGroupNullListener()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataGroupInfo dataGroup = createDataGroup(support);
        MantleToolbox mantleToolbox = createMantleToolbox(support, dataGroup);
        DataRegistry registry = createRegistry(support);

        support.replayAll();

        GeoPackageDeleter deleter = new GeoPackageDeleter(mantleToolbox, registry, null);
        deleter.deleteGroup(dataGroup, this);

        support.verifyAll();
    }

    /**
     * Creates a mocked {@link DataGroupInfo}.
     *
     * @param support Used to create the mock.
     * @return The mocked data group.
     */
    private DataGroupInfo createDataGroup(EasyMockSupport support)
    {
        DataGroupInfo dataGroup = support.createMock(DataGroupInfo.class);

        EasyMock.expect(dataGroup.getId()).andReturn(ourPackageFile);
        EasyMock.expect(dataGroup.getChildren()).andReturn(New.list());
        EasyMock.expect(dataGroup.getMembers(EasyMock.eq(false))).andReturn(New.set());

        DataGroupInfo parent = support.createMock(DataGroupInfo.class);
        EasyMock.expect(parent.removeChild(EasyMock.eq(dataGroup), EasyMock.isA(Object.class))).andReturn(true);

        EasyMock.expect(dataGroup.getParent()).andReturn(parent);

        return dataGroup;
    }

    /**
     * Creates an easy mocked {@link MantleToolbox}.
     *
     * @param support Used to create the mock.
     * @param expected The expected data group to be deleted.
     * @return The mocked {@link MantleToolbox}.
     */
    private MantleToolbox createMantleToolbox(EasyMockSupport support, DataGroupInfo expected)
    {
        DataGroupController controller = support.createMock(DataGroupController.class);
        controller.cleanUpGroup(EasyMock.eq(expected));

        MantleToolbox mantleToolbox = support.createMock(MantleToolbox.class);

        EasyMock.expect(mantleToolbox.getDataGroupController()).andReturn(controller);

        return mantleToolbox;
    }

    /**
     * Creates the data registry.
     *
     * @param support Used to create the mock.
     * @return The mocked data registry.
     */
    private DataRegistry createRegistry(EasyMockSupport support)
    {
        DataRegistry registry = support.createMock(DataRegistry.class);

        EasyMock.expect(registry.removeModels(EasyMock.eq(new DataModelCategory(ourPackageFile, null, null)), EasyMock.eq(false)))
                .andReturn(new long[] {});

        return registry;
    }
}
