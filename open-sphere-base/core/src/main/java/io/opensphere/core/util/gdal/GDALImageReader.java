package io.opensphere.core.util.gdal;

import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;

/** A GDAL image reader. */
@SuppressWarnings("PMD.GodClass")
public class GDALImageReader extends ImageReader
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GDALImageReader.class);

    /**
     * The GDAL data set. This is populated once the image is loaded from the
     * temp file.
     */
    private Dataset myDataset;

    /** Temporary file used so that GDAL can read the image. */
    private File myFile;

    /**
     * Constructor.
     *
     * @param originatingProvider The provider.
     */
    GDALImageReader(ImageReaderSpi originatingProvider)
    {
        super(originatingProvider);
    }

    @Override
    public void dispose()
    {
        Dataset dataset = myDataset;
        if (dataset != null)
        {
            dataset.delete();
            myDataset = null;
        }
        File file = myFile;
        if (file != null)
        {
            if (!file.delete())
            {
                LOGGER.error("Could not delete file " + file);
            }
            myFile = null;
        }
    }

    @Override
    public int getHeight(int imageIndex) throws IOException
    {
        Dataset dataset = getDataset();
        return dataset == null ? 0 : dataset.getRasterYSize();
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException
    {
        return null;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException
    {
        Dataset dataset = getDataset();

        TObjectIntMap<? extends RasterType> rasterTypes = getRasterTypeMap(dataset);

        TIntList bankIndices = getBankIndices(rasterTypes);
        if (bankIndices.isEmpty())
        {
            logRasterTypes(dataset);
            return Collections.<ImageTypeSpecifier>emptyList().iterator();
        }

        Band firstBand = dataset.GetRasterBand(bankIndices.get(0));
        int bandDataType = firstBand.getDataType();
        int dataType = getDataBufferType(bandDataType);
        if (dataType == -1)
        {
            return Collections.<ImageTypeSpecifier>emptyList().iterator();
        }

        ColorModel colorModel = createColorModel(rasterTypes, firstBand, bandDataType, dataType);
        if (colorModel == null)
        {
            LOGGER.warn("Unsupported color bands in image.");
            logRasterTypes(dataset);
            return Collections.<ImageTypeSpecifier>emptyList().iterator();
        }

        SampleModel sampleModel = createSampleModel(dataset.getRasterXSize(), dataset.getRasterYSize(), bankIndices.size(),
                dataType);

        return Collections.singleton(new ImageTypeSpecifier(colorModel, sampleModel)).iterator();
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException
    {
        return 1;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException
    {
        return null;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException
    {
        Dataset dataset = getDataset();
        return dataset == null ? 0 : dataset.getRasterXSize();
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException
    {
        Dataset dataset = getDataset();
        if (dataset == null)
        {
            return null;
        }

        TObjectIntMap<? extends RasterType> rasterTypes = getRasterTypeMap(dataset);
        TIntList bankIndices = getBankIndices(rasterTypes);

        if (bankIndices.isEmpty())
        {
            LOGGER.warn("No color bands found.");
            logRasterTypes(dataset);
            return null;
        }

        int width = dataset.getRasterXSize();
        int height = dataset.getRasterYSize();
        Band firstBand = dataset.GetRasterBand(1);
        int bandDataType = firstBand.getDataType();

        List<? extends ByteBuffer> bands = createBands(dataset, bankIndices, bandDataType, width, height);

        BufferedImage image = param == null ? null : param.getDestination();
        DataBuffer dataBuffer;
        if (image == null)
        {
            int dataType = getDataBufferType(bandDataType);
            if (dataType == -1)
            {
                return null;
            }
            SampleModel sampleModel = createSampleModel(width, height, bankIndices.size(), dataType);
            dataBuffer = sampleModel.createDataBuffer();
            WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
            ColorModel colorModel = createColorModel(rasterTypes, firstBand, bandDataType, dataBuffer.getDataType());
            image = new BufferedImage(colorModel, raster, false, null);
        }
        else
        {
            dataBuffer = image.getTile(0, 0).getDataBuffer();
            if (dataBuffer.getNumBanks() != bands.size())
            {
                throw new IllegalArgumentException("Raster has wrong number of banks.");
            }
        }
        populateDataBuffer(dataBuffer, bands);
        return image;
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata)
    {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        myDataset = null;
    }

    /**
     * Read the bands from the dataset.
     *
     * @param dataset The dataset.
     * @param bankIndices The raster indices for the color bands.
     * @param expectedBandDataType The GDAL data type of the bands.
     * @param width The width of the image in pixels.
     * @param height The height of the image in pixels.
     * @return An array of byte buffers, one for each band.
     */
    protected List<? extends ByteBuffer> createBands(Dataset dataset, TIntList bankIndices, int expectedBandDataType, int width,
            int height)
    {
        int bufSizeB = width * height * (gdal.GetDataTypeSize(expectedBandDataType) / Constants.BITS_PER_BYTE);

        List<ByteBuffer> bands = New.list(bankIndices.size());

        ByteBuffer buf = null;
        for (int bankIndex : bankIndices.toArray())
        {
            Band band = dataset.GetRasterBand(bankIndex);

            ByteBuffer data;
            int bandDataType = band.getDataType();
            if (bandDataType == expectedBandDataType)
            {
                if (buf == null)
                {
                    buf = ByteBuffer.allocateDirect(bufSizeB);
                    buf.order(ByteOrder.nativeOrder());
                }
                else
                {
                    buf.reset();
                }

                int returnVal = band.ReadRaster_Direct(0, 0, band.getXSize(), band.getYSize(), width, height,
                        expectedBandDataType, buf);

                if (returnVal == gdalconstConstants.CE_None)
                {
                    data = buf;
                    buf = null;
                }
                else
                {
                    LOGGER.error("An error occurred while trying to read band " + bankIndex);
                    GDALGenericUtilities.logLastError(LOGGER, Level.ERROR);
                    data = null;
                }
            }
            else
            {
                LOGGER.warn("Band " + bankIndex + " has wrong data type " + bandDataType + " (expecting " + expectedBandDataType
                        + ")");
                data = null;
            }
            bands.add(data);
        }
        return bands;
    }

    /**
     * Create the color model for the image.
     *
     * @param rasterTypes A map of raster types to raster indices.
     * @param firstBand The first color band.
     * @param bandDataType The band data type.
     * @param dataType The data buffer type.
     * @return The color model, or {@code null} if no compatible color model
     *         could be created.
     */
    protected ColorModel createColorModel(TObjectIntMap<? extends RasterType> rasterTypes, Band firstBand, int bandDataType,
            int dataType)
    {
        ColorModel colorModel;
        if (rasterTypes.containsKey(RasterType.PALETTE))
        {
            colorModel = firstBand.GetRasterColorTable().getIndexColorModel(gdal.GetDataTypeSize(bandDataType));
        }
        else
        {
            ColorSpace colorSpace = getColorSpace(rasterTypes);
            if (colorSpace == null)
            {
                colorModel = null;
            }
            else
            {
                boolean hasAlpha = rasterTypes.containsKey(RasterType.ALPHA);
                colorModel = new ComponentColorModel(colorSpace, hasAlpha, false,
                        hasAlpha ? ColorModel.TRANSLUCENT : ColorModel.OPAQUE, dataType);
            }
        }
        return colorModel;
    }

    /**
     * Create the sample model for the AWT image.
     *
     * @param width The image width.
     * @param height The image height.
     * @param numBands The number of bands in the image.
     * @param dataType The data type of the image (e.g.,
     *            {@link DataBuffer#TYPE_BYTE}).
     * @return The sample model.
     */
    protected SampleModel createSampleModel(int width, int height, int numBands, int dataType)
    {
        // Initialized to all 0. There are no offsets for any of the bands.
        int[] offsets = new int[numBands];

        // The indices are in order.
        int[] bankIndices = new int[numBands];
        for (int band = 0; band < numBands; ++band)
        {
            bankIndices[band] = band;
        }

        return new BandedSampleModel(dataType, width, height, width, bankIndices, offsets);
    }

    /**
     * Get the bank indices for the image. These must match the selected color
     * model.
     *
     * @param rasterTypes The map of raster types to raster indices.
     * @return The bank indices to match the color model.
     */
    protected TIntList getBankIndices(TObjectIntMap<? extends RasterType> rasterTypes)
    {
        TIntList bankIndices = new TIntArrayList();
        if (rasterTypes.containsKey(RasterType.PALETTE))
        {
            bankIndices.add(rasterTypes.get(RasterType.PALETTE));
        }
        else if (rasterTypes.containsKey(RasterType.RED) && rasterTypes.containsKey(RasterType.GREEN)
                && rasterTypes.containsKey(RasterType.BLUE))
        {
            bankIndices.add(rasterTypes.get(RasterType.RED));
            bankIndices.add(rasterTypes.get(RasterType.GREEN));
            bankIndices.add(rasterTypes.get(RasterType.BLUE));
        }
        else if (rasterTypes.containsKey(RasterType.GRAY))
        {
            bankIndices.add(rasterTypes.get(RasterType.GRAY));
        }
        if (rasterTypes.containsKey(RasterType.ALPHA))
        {
            bankIndices.add(rasterTypes.get(RasterType.ALPHA));
        }
        return bankIndices;
    }

    /**
     * Get the color space for the image.
     *
     * @param rasterTypes The map of raster types to raster indices.
     * @return The selected color space.
     */
    protected ColorSpace getColorSpace(TObjectIntMap<? extends RasterType> rasterTypes)
    {
        ColorSpace colorSpace;
        if (rasterTypes.containsKey(RasterType.PALETTE))
        {
            throw new IllegalArgumentException("Palette type is not supported.");
        }
        else if (rasterTypes.containsKey(RasterType.RED) && rasterTypes.containsKey(RasterType.GREEN)
                && rasterTypes.containsKey(RasterType.BLUE))
        {
            colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        }
        else if (rasterTypes.containsKey(RasterType.GRAY))
        {
            colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        }
        else
        {
            colorSpace = null;
        }
        return colorSpace;
    }

    /**
     * Determine the data type to use for the data buffer.
     *
     * @param bandDataType The GDAL band data type.
     * @return The data buffer data type ({@link DataBuffer#TYPE_BYTE},
     *         {@link DataBuffer#TYPE_USHORT}, or {@link DataBuffer#TYPE_INT}).
     */
    protected int getDataBufferType(int bandDataType)
    {
        if (bandDataType == gdalconstConstants.GDT_Byte)
        {
            return DataBuffer.TYPE_BYTE;
        }
        else if (bandDataType == gdalconstConstants.GDT_UInt16)
        {
            return DataBuffer.TYPE_USHORT;
        }
        else if (bandDataType == gdalconstConstants.GDT_Int16)
        {
            return DataBuffer.TYPE_SHORT;
        }
        else if (bandDataType == gdalconstConstants.GDT_Int32)
        {
            return DataBuffer.TYPE_INT;
        }
        else
        {
            LOGGER.error("Unrecognized band data type : " + bandDataType);
            return -1;
        }
    }

    /**
     * Load the image into GDAL and return the data set.
     *
     * @return The data set.
     * @throws IOException If there is an error reading the image.
     */
    protected Dataset getDataset() throws IOException
    {
        if (myDataset == null)
        {
            ImageInputStream imageStream = (ImageInputStream)input;
            if (imageStream == null)
            {
                throw new IllegalStateException("Input has not been set.");
            }

            if (myFile == null)
            {
                myFile = File.createTempFile("gdalImageReader", Nulls.STRING);
            }
            FileOutputStream fos = new FileOutputStream(myFile);
            try
            {
                byte[] data = new byte[2048];
                int numBytes;
                while ((numBytes = imageStream.read(data)) >= 0)
                {
                    if (abortRequested())
                    {
                        return null;
                    }
                    fos.write(data, 0, numBytes);
                }
            }
            finally
            {
                fos.close();
            }

            Dataset dataset = gdal.Open(myFile.getAbsolutePath(), gdalconst.GA_ReadOnly);
            if (dataset == null)
            {
                GDALGenericUtilities.logLastError(LOGGER, Level.ERROR);
                throw new IOException("Failed to create GDAL dataset for file: " + myFile + ": " + gdal.GetLastErrorMsg());
            }
            else
            {
                myDataset = dataset;
            }
        }
        return myDataset;
    }

    /**
     * Get the raster type map for a dataset.
     *
     * @param dataset The dataset.
     * @return A map of raster types to raster indices.
     */
    protected TObjectIntMap<? extends RasterType> getRasterTypeMap(Dataset dataset)
    {
        TObjectIntMap<RasterType> result = new TObjectIntHashMap<>();
        for (int raster = 1; raster <= dataset.getRasterCount(); ++raster)
        {
            int colorInterp = dataset.GetRasterBand(raster).GetColorInterpretation();
            RasterType rasterType = RasterType.getRasterType(colorInterp);
            if (rasterType != RasterType.UNKNOWN)
            {
                result.put(rasterType, raster);
            }
            else if (!result.containsKey(RasterType.GRAY))
            {
                result.put(RasterType.GRAY, raster);
            }
        }
        return result;
    }

    /**
     * Log the raster color interpretations in a dataset.
     *
     * @param dataset The dataset.
     */
    protected void logRasterTypes(Dataset dataset)
    {
        int rasterCount = dataset.getRasterCount();
        if (rasterCount == 0)
        {
            LOGGER.warn("No bands found in image.");
        }
        else
        {
            for (int raster = 1; raster <= rasterCount; ++raster)
            {
                int colorInterp = dataset.GetRasterBand(raster).GetColorInterpretation();
                LOGGER.info("Band " + raster + " has color interpretation " + gdal.GetColorInterpretationName(colorInterp));
            }
        }
    }

    /**
     * Populate the data buffer with the extracted image bands.
     *
     * @param dataBuffer The image data buffer.
     * @param bands The bands from the image.
     */
    protected void populateDataBuffer(DataBuffer dataBuffer, List<? extends ByteBuffer> bands)
    {
        if (dataBuffer instanceof DataBufferByte)
        {
            DataBufferByte byteData = (DataBufferByte)dataBuffer;
            for (int band = 0; band < bands.size(); band++)
            {
                ByteBuffer bandData = bands.get(band);
                if (bandData != null)
                {
                    bandData.get(byteData.getData(band));
                }
            }
        }
        else if (dataBuffer instanceof DataBufferShort)
        {
            DataBufferShort shortData = (DataBufferShort)dataBuffer;
            for (int band = 0; band < bands.size(); band++)
            {
                ByteBuffer bandData = bands.get(band);
                if (bandData != null)
                {
                    bandData.asShortBuffer().get(shortData.getData(band));
                }
            }
        }
        else if (dataBuffer instanceof DataBufferUShort)
        {
            DataBufferUShort shortData = (DataBufferUShort)dataBuffer;
            for (int band = 0; band < bands.size(); band++)
            {
                ByteBuffer bandData = bands.get(band);
                if (bandData != null)
                {
                    bandData.asShortBuffer().get(shortData.getData(band));
                }
            }
        }
        else if (dataBuffer instanceof DataBufferInt)
        {
            DataBufferInt intData = (DataBufferInt)dataBuffer;
            for (int band = 0; band < bands.size(); band++)
            {
                ByteBuffer bandData = bands.get(band);
                if (bandData != null)
                {
                    bandData.asIntBuffer().get(intData.getData(band));
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized data buffer type : " + dataBuffer);
        }
    }

    /** Identification of a raster type in an image. */
    private enum RasterType
    {
        /** Alpha raster. */
        ALPHA(gdalconstConstants.GCI_AlphaBand),

        /** Blue raster. */
        BLUE(gdalconstConstants.GCI_BlueBand),

        /** Grayscale raster. */
        GRAY(gdalconstConstants.GCI_GrayIndex),

        /** Green raster. */
        GREEN(gdalconstConstants.GCI_GreenBand),

        /** Color index raster. */
        PALETTE(gdalconstConstants.GCI_PaletteIndex),

        /** Red raster. */
        RED(gdalconstConstants.GCI_RedBand),

        /** Unknown raster. */
        UNKNOWN(-1),

        ;

        /** The GDAL color interpretation code. */
        private int myGdalColorInterpretation;

        /**
         * Get the raster type for a given GDAL color interpretation.
         *
         * @param gdalColorInterpretation The GDAL color interpretation code.
         * @return The raster type.
         */
        public static RasterType getRasterType(int gdalColorInterpretation)
        {
            for (RasterType type : RasterType.values())
            {
                if (type.getGdalColorInterpretation() == gdalColorInterpretation)
                {
                    return type;
                }
            }
            return UNKNOWN;
        }

        /**
         * Enum constructor.
         *
         * @param gdalColorInterpretation The GDAL color interpretation code.
         */
        RasterType(int gdalColorInterpretation)
        {
            myGdalColorInterpretation = gdalColorInterpretation;
        }

        /**
         * Get the GDAL color interpretation code.
         *
         * @return The code.
         */
        private int getGdalColorInterpretation()
        {
            return myGdalColorInterpretation;
        }
    }
}
