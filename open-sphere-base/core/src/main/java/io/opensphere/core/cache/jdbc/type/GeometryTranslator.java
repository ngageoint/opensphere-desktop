package io.opensphere.core.cache.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.util.Utilities;

/**
 * A value translator implementation for Geometry values.
 */
public class GeometryTranslator extends AbstractValueTranslator<Geometry>
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getType()
     */
    @Override
    public Class<Geometry> getType()
    {
        return Geometry.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#getValue(java.lang.Class,
     *      long, int, java.sql.ResultSet,
     *      io.opensphere.core.cache.matcher.PropertyMatcher,
     *      java.util.Collection)
     */
    @Override
    public int getValue(Class<? extends Geometry> type, long sizeBytes, int column, ResultSet rs,
            PropertyMatcher<? extends Geometry> filter, Collection<? super Geometry> results)
        throws CacheException
    {
        try
        {
            Geometry obj = new WKBReader().read(rs.getBytes(column));
            if (filter == null || filter.matches(obj))
            {
                results.add(type.isInstance(obj) ? obj : null);
            }
            return column + 1;
        }
        catch (ParseException e)
        {
            throw new CacheException("Failed to parse geometry: " + e, e);
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to get geometry from result set: " + e, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.cache.jdbc.type.ValueTranslator#setValue(java.sql.PreparedStatement,
     *      int, java.lang.Object, boolean)
     */
    @Override
    public int setValue(PreparedStatement pstmt, int column, Geometry value, boolean forInsert) throws SQLException
    {
        Utilities.checkNull(value, "value");

        pstmt.setBytes(column, new WKBWriter().write(value));

        return column + 1;
    }
}
