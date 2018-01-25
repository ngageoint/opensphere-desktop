/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.nio.ByteBuffer;

/**
 * Inheritance, yay!
 */
public class NullRecord extends ShapeRecord
{
    @Override
    public int getShapeType()
    {
        return 0;
    }

    @Override
    public int getContentLengthInWords()
    {
        return 0;
    }

    @Override
    public double[] getBox()
    {
        return null;
    }

    @Override
    public boolean parseRecord(ByteBuffer buffer)
    {
        return false;
    }

    @Override
    public boolean writeRecord(ByteBuffer buffer)
    {
        return false;
    }
}
