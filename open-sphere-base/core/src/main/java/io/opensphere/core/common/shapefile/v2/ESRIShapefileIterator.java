package io.opensphere.core.common.shapefile.v2;

import java.util.Iterator;

import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;

/**
 * Iterates over the components of the <code>ESRIShapefile</code>
 */
public class ESRIShapefileIterator implements Iterator<ShapefileRecord>
{
    ESRIShapefile parent = null;

    Iterator<ShapeRecord> shpItr = null;

    Iterator<Object[]> dbfItr = null;

    public ESRIShapefileIterator(ESRIShapefile es)
    {
        parent = es;
        shpItr = parent.getShp().iterator();
        dbfItr = parent.getDbf().iterator();
    }

    public boolean hasNext()
    {
        return (shpItr.hasNext() && dbfItr.hasNext());
    }

    public ShapefileRecord next()
    {
        return new ShapefileRecord(shpItr.next(), dbfItr.next());
    }

    public void remove()
    {
        throw (new UnsupportedOperationException());
    }
}
