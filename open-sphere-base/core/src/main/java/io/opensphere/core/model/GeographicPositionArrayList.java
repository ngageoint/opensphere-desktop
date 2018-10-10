package io.opensphere.core.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.Petrifyable;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTDoubleArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTFloatArrayList;

/**
 * A storage space optimized list of positions on the globe. This collection is
 * unmodifiable.
 */
@io.opensphere.core.util.Immutable
@net.jcip.annotations.Immutable
public final class GeographicPositionArrayList extends AbstractList<GeographicPosition>
implements PositionList<GeographicPosition>, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The coordinate data. */
    private final Storage myStorage;

    /**
     * Create a collection from an interleaved array of double precision degree
     * latitudes and longitudes. Altitudes will be zero and
     * {@link ReferenceLevel#TERRAIN} referenced.
     *
     * @param data The interleaved data (lat, lon, lat, lon, etc.), which will
     *            be cloned by this call.
     * @return The collection.
     */
    public static GeographicPositionArrayList createFromDegrees(double[] data)
    {
        return new GeographicPositionArrayList(new LatLonDoubleStorage(new PetrifyableTDoubleArrayList(data)));
    }

    /**
     * Create a collection from an interleaved array of single precision degree
     * latitudes and longitudes. Altitudes will be zero and
     * {@link ReferenceLevel#TERRAIN} referenced.
     *
     * @param data The interleaved data (lat, lon, lat, lon, etc.), which will
     *            be cloned by this call.
     * @return The collection.
     */
    public static GeographicPositionArrayList createFromDegrees(float[] data)
    {
        return new GeographicPositionArrayList(new LatLonFloatStorage(new PetrifyableTFloatArrayList(data)));
    }

    /**
     * Create a collection from an interleaved array of double precision degree
     * latitudes and longitudes and meters altitudes.
     *
     * @param data The interleaved data (lat, lon, alt, lat, lon, alt, etc.),
     *            which will be cloned by this call.
     * @param referenceLevel The reference level for the altitudes.
     * @return The collection.
     */
    public static GeographicPositionArrayList createFromDegreesMeters(double[] data, Altitude.ReferenceLevel referenceLevel)
    {
        return new GeographicPositionArrayList(new LatLonAltDoubleStorage(new PetrifyableTDoubleArrayList(data), referenceLevel));
    }

    /**
     * Create a collection from an interleaved array of single precision degree
     * latitudes and longitudes and meters altitudes.
     *
     * @param data The interleaved data (lat, lon, alt, lat, lon, alt, etc.),
     *            which will be cloned by this call.
     * @param referenceLevel The reference level for the altitudes.
     * @return The collection.
     */
    public static GeographicPositionArrayList createFromDegreesMeters(float[] data, Altitude.ReferenceLevel referenceLevel)
    {
        return new GeographicPositionArrayList(new LatLonAltFloatStorage(new PetrifyableTFloatArrayList(data), referenceLevel));
    }

    /**
     * Create a list from a petrifyable list containing an interleaved array of
     * double precision degree latitudes and longitudes and meters altitudes.
     *
     * @param data The interleaved data (lat, lon, alt, lat, lon, alt, etc.),
     *            which will be petrified by this call.
     * @param referenceLevel The reference level for the altitudes.
     * @return The collection.
     */
    public static GeographicPositionArrayList createFromDegreesMeters(PetrifyableTDoubleArrayList data,
            ReferenceLevel referenceLevel)
    {
        return new GeographicPositionArrayList(new LatLonAltDoubleStorage(data, referenceLevel));
    }

    /**
     * Create a list from a petrifyable list containing an interleaved array of
     * single precision degree latitudes and longitudes and meters altitudes.
     *
     * @param data The interleaved data (lat, lon, alt, lat, lon, alt, etc.),
     *            which will be petrified by this call.
     * @param referenceLevel The reference level for the altitudes.
     * @return The collection.
     */
    public static GeographicPositionArrayList createFromDegreesMeters(PetrifyableTFloatArrayList data,
            ReferenceLevel referenceLevel)
    {
        return new GeographicPositionArrayList(new LatLonAltFloatStorage(data, referenceLevel));
    }

    /**
     * Create a collection from a collection of {@link LatLonAlt}.
     *
     * @param llas The collection of {@link LatLonAlt}s.
     * @return The collection.
     */
    public static GeographicPositionArrayList createFromLLAs(Collection<? extends LatLonAlt> llas)
    {
        return new GeographicPositionArrayList(new LatLonAltObjectStorage(llas));
    }

    /**
     * Construct a collection of geographic positions.
     *
     * @param storage The storage for the coordinates.
     */
    private GeographicPositionArrayList(Storage storage)
    {
        myStorage = storage;
    }

    @Override
    public GeographicPosition get(int index)
    {
        return myStorage.get(index);
    }

    @Override
    public int size()
    {
        return myStorage.size();
    }

    /**
     * Storage for double coordinates.
     */
    private abstract static class AbstractDoubleStorage extends AbstractStorage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Array of coordinates.
         */
        private final PetrifyableTDoubleArrayList myData;

        /**
         * Constructor.
         *
         * @param data Array of double-precision interleaved coordinates.
         * @param dimensions The number of dimensions in each set of
         *            coordinates.
         */
        public AbstractDoubleStorage(PetrifyableTDoubleArrayList data, int dimensions)
        {
            super(data, data.size(), dimensions);
            myData = data;
        }

        /**
         * Accessor for the data.
         *
         * @return The data.
         */
        public PetrifyableTDoubleArrayList getData()
        {
            return myData;
        }

        @Override
        public int size()
        {
            return myData.size() / getDim();
        }
    }

    /**
     * Storage for float coordinates.
     */
    private abstract static class AbstractFloatStorage extends AbstractStorage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Array of coordinates.
         */
        private final PetrifyableTFloatArrayList myData;

        /**
         * Constructor.
         *
         * @param data Array of single-precision interleaved coordinates, which
         *            will be petrified by this call.
         * @param dimensions The number of dimensions in each set of
         *            coordinates.
         */
        public AbstractFloatStorage(PetrifyableTFloatArrayList data, int dimensions)
        {
            super(data, data.size(), dimensions);
            myData = data;
        }

        /**
         * Accessor for the data.
         *
         * @return The data.
         */
        public PetrifyableTFloatArrayList getData()
        {
            return myData;
        }

        @Override
        public int size()
        {
            return myData.size() / getDim();
        }
    }

    /**
     * Common behavior for {@link Storage} implementations.
     */
    private abstract static class AbstractStorage implements Storage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The number of dimensions interleaved in the data array. */
        private final int myDim;

        /**
         * Constructor.
         *
         * @param data The petrifyable data.
         * @param size The number of elements in the data.
         * @param dimensions The number of dimensions in each set of
         *            coordinates.
         */
        public AbstractStorage(Petrifyable data, int size, int dimensions)
        {
            if (size == 0)
            {
                throw new IllegalArgumentException("Size cannot be zero.");
            }
            if (size % dimensions != 0)
            {
                throw new IllegalArgumentException("Size must be a multiple of " + dimensions + " (size is " + size + ")");
            }
            data.petrify();
            myDim = dimensions;
        }

        /**
         * Get the number of dimensions in the data array.
         *
         * @return The dimension count.
         */
        public int getDim()
        {
            return myDim;
        }
    }

    /**
     * Storage for double coordinates.
     */
    private static class LatLonAltDoubleStorage extends AbstractDoubleStorage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The reference level for the altitudes. */
        private final Altitude.ReferenceLevel myReferenceLevel;

        /**
         * Constructor.
         *
         * @param data Array of interleaved coordinates (lat, lon, alt, lat lon,
         *            alt etc.)
         * @param referenceLevel The reference level for the altitudes.
         */
        public LatLonAltDoubleStorage(PetrifyableTDoubleArrayList data, Altitude.ReferenceLevel referenceLevel)
        {
            super(data, 3);
            myReferenceLevel = referenceLevel;
        }

        @Override
        public GeographicPosition get(int index)
        {
            return new GeographicPosition(LatLonAlt.createFromDegreesMeters(getData().get(index * 3),
                    getData().get(index * 3 + 1), getData().get(index * 3 + 2), myReferenceLevel));
        }
    }

    /**
     * Storage for float coordinates.
     */
    private static class LatLonAltFloatStorage extends AbstractFloatStorage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The reference level for the altitudes. */
        private final Altitude.ReferenceLevel myReferenceLevel;

        /**
         * Constructor.
         *
         * @param data Array of interleaved coordinates (lat, lon, alt, lat lon,
         *            alt etc.), which will be petrified by this call.
         * @param referenceLevel The reference level for the altitudes.
         */
        public LatLonAltFloatStorage(PetrifyableTFloatArrayList data, Altitude.ReferenceLevel referenceLevel)
        {
            super(data, 3);
            myReferenceLevel = referenceLevel;
        }

        @Override
        public GeographicPosition get(int index)
        {
            return new GeographicPosition(LatLonAlt.createFromDegreesMeters(getData().get(index * 3),
                    getData().get(index * 3 + 1), getData().get(index * 3 + 2), myReferenceLevel));
        }
    }

    /**
     * Storage for LLA coordinates.
     */
    private static class LatLonAltObjectStorage implements Storage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The nested data. */
        private final List<? extends LatLonAlt> myData;

        /**
         * Constructor.
         *
         * @param data Array of interleaved coordinates (lat, lon, alt, lat lon,
         *            alt etc.)
         */
        public LatLonAltObjectStorage(Collection<? extends LatLonAlt> data)
        {
            myData = New.unmodifiableList(data);
        }

        @Override
        public GeographicPosition get(int index)
        {
            LatLonAlt lla = getData().get(index);
            return new GeographicPosition(
                    LatLonAlt.createFromDegreesMeters(lla.getLatD(), lla.getLonD(), lla.getAltM(), lla.getAltitudeReference()));
        }

        @Override
        public int size()
        {
            return getData().size();
        }

        /**
         * Accessor for the nested data.
         *
         * @return The data.
         */
        private List<? extends LatLonAlt> getData()
        {
            return myData;
        }
    }

    /**
     * Storage for double coordinates.
     */
    private static class LatLonDoubleStorage extends AbstractDoubleStorage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param data Array of interleaved coordinates (lat, lon, lat lon,
         *            etc.), which will be petrified by this call.
         */
        public LatLonDoubleStorage(PetrifyableTDoubleArrayList data)
        {
            super(data, 2);
        }

        @Override
        public GeographicPosition get(int index)
        {
            return new GeographicPosition(LatLonAlt.createFromDegrees(getData().get(index * 2), getData().get(index * 2 + 1)));
        }
    }

    /**
     * Storage for float coordinates.
     */
    private static class LatLonFloatStorage extends AbstractFloatStorage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param data Array of interleaved coordinates (lat, lon, lat lon,
         *            etc.), which will be petrified by this call.
         */
        public LatLonFloatStorage(PetrifyableTFloatArrayList data)
        {
            super(data, 2);
        }

        @Override
        public GeographicPosition get(int index)
        {
            return new GeographicPosition(LatLonAlt.createFromDegrees(getData().get(index * 2), getData().get(index * 2 + 1)));
        }
    }

    /**
     * Interface for internal position storage.
     */
    private interface Storage extends Serializable
    {
        /**
         * Get a geographic position at the specified index.
         *
         * @param index The index.
         * @return The position.
         * @throws ArrayIndexOutOfBoundsException if the index is out of bounds.
         */
        GeographicPosition get(int index);

        /**
         * Get the number of positions.
         *
         * @return The size.
         */
        int size();
    }
}
