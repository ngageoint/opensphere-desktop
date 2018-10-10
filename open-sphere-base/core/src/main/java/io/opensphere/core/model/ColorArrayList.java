package io.opensphere.core.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SharedObjectPool;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTByteArrayList;

/**
 * A storage space optimized list of colors. This collection is unmodifiable.
 */
@io.opensphere.core.util.Immutable
@net.jcip.annotations.Immutable
public final class ColorArrayList extends AbstractList<Color> implements Serializable, SizeProvider
{
    /** Object pool for colors. */
    private static final SharedObjectPool<Color> COLOR_POOL = new SharedObjectPool<>();

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The color storage. */
    private final Storage myStorage;

    /**
     * Create a collection from an interleaved array of RGBA bytes.
     *
     * @param data The interleaved data (red, green, blue, alpha, red, etc.),
     *            which will be defensively cloned by this call.
     * @return The collection.
     */
    public static ColorArrayList createFromRGBABytes(byte[] data)
    {
        return new ColorArrayList(new RGBAByteStorage(new PetrifyableTByteArrayList(data)));
    }

    /**
     * Create a collection from an interleaved array of RGBA bytes.
     *
     * @param data The interleaved data (red, green, blue, alpha, red, etc.),
     *            which will be petrified by this call.
     * @return The collection.
     */
    public static ColorArrayList createFromRGBABytes(PetrifyableTByteArrayList data)
    {
        return new ColorArrayList(new RGBAByteStorage(data));
    }

    /**
     * Create a collection from an interleaved array of RGB bytes.
     *
     * @param data The interleaved data (red, green, blue, red, etc.), which
     *            will be defensively cloned by this call.
     * @return The collection.
     */
    public static ColorArrayList createFromRGBBytes(byte[] data)
    {
        return new ColorArrayList(new RGBByteStorage(new PetrifyableTByteArrayList(data)));
    }

    /**
     * Create a collection from an interleaved array of RGB bytes.
     *
     * @param data The interleaved data (red, green, blue, red, etc.), which
     *            will be petrified by this call.
     * @return The collection.
     */
    public static ColorArrayList createFromRGBBytes(PetrifyableTByteArrayList data)
    {
        return new ColorArrayList(new RGBByteStorage(data));
    }

    /**
     * Create a collection from another collection of colors. If the input is
     * already a {@link ColorArrayList}, simply return the input.
     *
     * @param input The input collection.
     * @return The color array list.
     */
    public static ColorArrayList getColorArrayList(Collection<? extends Color> input)
    {
        if (input == null || input instanceof ColorArrayList)
        {
            return (ColorArrayList)input;
        }
        PetrifyableTByteArrayList bytes = new PetrifyableTByteArrayList(input.size() * 4);
        for (Color color : input)
        {
            bytes.add((byte)color.getRed());
            bytes.add((byte)color.getGreen());
            bytes.add((byte)color.getBlue());
            bytes.add((byte)color.getAlpha());
        }

        return createFromRGBABytes(bytes);
    }

    /**
     * Construct a collection of geographic positions.
     *
     * @param storage The storage for the coordinates.
     */
    private ColorArrayList(Storage storage)
    {
        myStorage = storage;
    }

    @Override
    public Color get(int index)
    {
        return myStorage.get(index);
    }

    /**
     * Get a color as an ARGB int.
     *
     * @param index The index of the color to return.
     * @return The int.
     */
    public int getARGB(int index)
    {
        return myStorage.getARGB(index);
    }

    /**
     * Get the colors as an array of bytes, in either RGB or RGBA order
     * depending on the underlying storage.
     *
     * @return The array.
     */
    public byte[] getBytes()
    {
        return myStorage.getBytes();
    }

    /**
     * Get the number of bytes per color stored in this list.
     *
     * @return The number of bytes.
     */
    public int getBytesPerColor()
    {
        return myStorage.getBytesPerColor();
    }

    /**
     * Get the colors as an array of bytes in RGBA order.
     *
     * @return The array.
     */
    public byte[] getRGBABytes()
    {
        return myStorage.getRGBABytes();
    }

    @Override
    public long getSizeBytes()
    {
        return myStorage.getSizeBytes();
    }

    @Override
    public int size()
    {
        return myStorage.size();
    }

    /**
     * Storage for colors represented as bytes.
     */
    private abstract static class AbstractByteStorage implements Storage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The number of bytes in each color. */
        private final int myBytesPerColor;

        /**
         * Array of colors.
         */
        private final PetrifyableTByteArrayList myData;

        /**
         * Constructor.
         *
         * @param data The interleaved data (red, green, blue, alpha, red,
         *            etc.), which will be petrified by this call.
         * @param bytesPerColor The number of bytes in each color.
         */
        public AbstractByteStorage(PetrifyableTByteArrayList data, int bytesPerColor)
        {
            if (data.size() == 0)
            {
                throw new IllegalArgumentException("Collection size cannot be zero.");
            }
            if (data.size() % bytesPerColor != 0)
            {
                throw new IllegalArgumentException(
                        "Collection size must be a multiple of " + bytesPerColor + " (size is " + data.size() + ")");
            }
            data.petrify();
            myData = data;
            myBytesPerColor = bytesPerColor;
        }

        @Override
        public byte[] getBytes()
        {
            return myData.toArray();
        }

        @Override
        public int getBytesPerColor()
        {
            return myBytesPerColor;
        }

        @Override
        public long getSizeBytes()
        {
            return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES + Constants.INT_SIZE_BYTES,
                    Constants.MEMORY_BLOCK_SIZE_BYTES) + myData.getSizeBytes();
        }

        @Override
        public int size()
        {
            return myData.size() / getBytesPerColor();
        }
    }

    /**
     * Storage for RGBA byte colors.
     */
    private static class RGBAByteStorage extends AbstractByteStorage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param data The interleaved data (red, green, blue, alpha, red,
         *            etc.), which will be petrified by this call.
         */
        public RGBAByteStorage(PetrifyableTByteArrayList data)
        {
            super(data, 4);
        }

        @Override
        public Color get(int index)
        {
            int r = getBytes()[index * 4] & 0xff;
            int g = getBytes()[index * 4 + 1] & 0xff;
            int b = getBytes()[index * 4 + 2] & 0xff;
            int a = getBytes()[index * 4 + 3] & 0xff;
            return COLOR_POOL.get(new Color(r, g, b, a));
        }

        @Override
        public int getARGB(int index)
        {
            int r = getBytes()[index * 4] & 0xff;
            int g = getBytes()[index * 4 + 1] & 0xff;
            int b = getBytes()[index * 4 + 2] & 0xff;
            int a = getBytes()[index * 4 + 3] & 0xff;
            return (a << 24) + (r << 16) + (g << 8) + b;
        }

        @Override
        public byte[] getRGBABytes()
        {
            return getBytes();
        }
    }

    /**
     * Storage for RGB byte colors.
     */
    private static class RGBByteStorage extends AbstractByteStorage
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param data The interleaved data (red, green, blue, red, etc.), which
         *            will be petrified by this call.
         */
        public RGBByteStorage(PetrifyableTByteArrayList data)
        {
            super(data, 3);
        }

        @Override
        public Color get(int index)
        {
            int r = getBytes()[index * 4] & 0xff;
            int g = getBytes()[index * 4 + 1] & 0xff;
            int b = getBytes()[index * 4 + 2] & 0xff;
            return COLOR_POOL.get(new Color(r, g, b));
        }

        @Override
        public int getARGB(int index)
        {
            int r = getBytes()[index * 4] & 0xff;
            int g = getBytes()[index * 4 + 1] & 0xff;
            int b = getBytes()[index * 4 + 2] & 0xff;
            return 0xff000000 + (r << 16) + (g << 8) + b;
        }

        @Override
        public byte[] getRGBABytes()
        {
            byte[] result = new byte[size() * 4];
            for (int index = 0; index < size(); ++index)
            {
                System.arraycopy(getBytes(), index * 3, result, index * 4, 3);
                result[index * 4 + 3] = -1;
            }
            return result;
        }
    }

    /**
     * Interface for internal color storage.
     */
    private interface Storage extends Serializable, SizeProvider
    {
        /**
         * Get a color at the specified index.
         *
         * @param index The index.
         * @return The color.
         * @throws ArrayIndexOutOfBoundsException if the index is out of bounds.
         */
        Color get(int index);

        /**
         * Get a color at the specified index as an ARGB integer.
         *
         * @param index The index.
         * @return The integer.
         */
        int getARGB(int index);

        /**
         * Get the colors as an array of bytes in either RGB or RGBA order
         * depending on the underlying storage.
         *
         * @return The array.
         */
        byte[] getBytes();

        /**
         * Get the number of bytes per color.
         *
         * @return The number of bytes per color.
         */
        int getBytesPerColor();

        /**
         * Get the colors as an array of bytes in RGBA order.
         *
         * @return The array.
         */
        byte[] getRGBABytes();

        /**
         * Get the number of colors.
         *
         * @return The size.
         */
        int size();
    }
}
