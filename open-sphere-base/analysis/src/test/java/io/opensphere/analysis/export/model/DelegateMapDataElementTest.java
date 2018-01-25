package io.opensphere.analysis.export.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;

/**
 * Unit test for {@link DelegateMapDataElement}.
 */
public class DelegateMapDataElementTest
{
    /**
     * Tests the delegation.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        support.replayAll();

        SimpleMapPointGeometrySupport geometry = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10, 11));
        TimeSpan time = TimeSpan.get();
        SimpleMetaDataProvider metadataProvider = new SimpleMetaDataProvider();
        DefaultMapDataElement original = new DefaultMapDataElement(10, time, dataType, metadataProvider, geometry);

        SimpleMetaDataProvider another = new SimpleMetaDataProvider();
        DelegateMapDataElement delegate = new DelegateMapDataElement(original, another);

        assertEquals(dataType, delegate.getDataTypeInfo());
        assertEquals(10, delegate.getId());
        delegate.setIdInCache(12);
        assertEquals(12, original.getIdInCache());
        assertEquals(12, delegate.getIdInCache());
        assertEquals(geometry, delegate.getMapGeometrySupport());
        assertEquals(another, delegate.getMetaData());
        assertEquals(metadataProvider, original.getMetaData());
        assertEquals(time, delegate.getTimeSpan());
        assertEquals(original.getVisualizationState(), delegate.getVisualizationState());
        assertTrue(delegate.isDisplayable());
        assertTrue(delegate.isMappable());

        SimpleMapPointGeometrySupport newGeometry = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(12, 13));
        delegate.setMapGeometrySupport(newGeometry);
        assertEquals(newGeometry, original.getMapGeometrySupport());
        assertEquals(newGeometry, delegate.getMapGeometrySupport());

        support.verifyAll();
    }
}
