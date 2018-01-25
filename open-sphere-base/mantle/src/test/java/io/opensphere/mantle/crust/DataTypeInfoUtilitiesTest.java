package io.opensphere.mantle.crust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.util.PropertyChangeException;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;

/**
 * Unit test for the {@link DataTypeInfoUtilities} class.
 */
public class DataTypeInfoUtilitiesTest
{
    /**
     * Tests copying information of one data type to another.
     *
     * @throws PropertyChangeException Exception the mocked calls can throw.
     * @throws InterruptedException Don't interupt.
     */
    @Test
    public void testCopyDataTypeInfo() throws PropertyChangeException, InterruptedException
    {
        EasyMockSupport support = new EasyMockSupport();

        DefaultMetaDataInfo mdi = new DefaultMetaDataInfo();
        mdi.setGeometryColumn("geom");
        mdi.addKey("column1", Integer.class, this);
        mdi.addKey("geom", String.class, this);
        mdi.addKey("Lat", Double.class, this);
        mdi.setSpecialKey("Lat", LatitudeKey.DEFAULT, this);

        DefaultDataTypeInfo toCopy = new DefaultDataTypeInfo(null, "one", "first", "theFirst", "I am First", false, mdi);
        DefaultMapFeatureVisualizationInfo visInfo = new DefaultMapFeatureVisualizationInfo(MapVisualizationType.POINT_ELEMENTS);
        toCopy.setMapVisualizationInfo(visInfo);
        DefaultDataGroupInfo group = new DefaultDataGroupInfo(false, null, "test", "first");
        toCopy.setParent(group);
        group.activationProperty().setActive(false);

        ActivationListener listener = createListener(support);
        group.activationProperty().addListener(listener);

        DefaultMetaDataInfo copiedMdi = new DefaultMetaDataInfo();
        DefaultDataTypeInfo theCopy = new DefaultDataTypeInfo(null, "two", "two", "theFirst", "I am second", false, copiedMdi);
        DefaultMapFeatureVisualizationInfo theCopyVisInfo = new DefaultMapFeatureVisualizationInfo(MapVisualizationType.UNKNOWN);
        theCopy.setMapVisualizationInfo(theCopyVisInfo);

        support.replayAll();

        DataTypeInfoUtilities.copyDataTypeInfo(toCopy, theCopy, this);

        assertNotSame(mdi, theCopy.getMetaDataInfo());
        assertEquals("geom", theCopy.getMetaDataInfo().getGeometryColumn());
        assertEquals(theCopy, ((DefaultMetaDataInfo)theCopy.getMetaDataInfo()).getDataTypeInfo());
        assertEquals(Integer.class, theCopy.getMetaDataInfo().getKeyClassType("column1"));
        assertEquals(String.class, theCopy.getMetaDataInfo().getKeyClassType("geom"));
        assertEquals(Double.class, theCopy.getMetaDataInfo().getKeyClassType("Lat"));
        assertEquals("Lat", theCopy.getMetaDataInfo().getLatitudeKey());
        assertEquals(MapVisualizationType.POINT_ELEMENTS, theCopy.getMapVisualizationInfo().getVisualizationType());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ActivationListener}.
     *
     * @param support Used to create the mock.
     * @return The mocked listener.
     * @throws PropertyChangeException Exception the mocked calls can throw.
     * @throws InterruptedException Don't interupt.
     */
    private ActivationListener createListener(EasyMockSupport support) throws PropertyChangeException, InterruptedException
    {
        ActivationListener listener = support.createMock(ActivationListener.class);

        EasyMock.expect(Boolean.valueOf(listener.preCommit(EasyMock.isA(DataGroupActivationProperty.class),
                EasyMock.eq(ActivationState.ACTIVE), EasyMock.isNull()))).andReturn(Boolean.TRUE);
        listener.commit(EasyMock.isA(DataGroupActivationProperty.class),
                EasyMock.eq(ActivationState.INACTIVE), EasyMock.isNull());
        return listener;
    }
}
