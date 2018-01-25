package io.opensphere.imagery.gdal;

/**
 * The Class BandHistoBean.
 */
public class BandHistoBean
{
    /** The Max. */
    private final double myMax;

    /** The Mean. */
    private final double myMean;

    /** The Min. */
    private final double myMin;

    /** The Stddev. */
    private final double myStddev;

    /**
     * Instantiates a new band histo bean.
     *
     * @param min the min
     * @param max the max
     * @param mean the mean
     * @param stddev the stddev
     * @param histo the histo
     */
    public BandHistoBean(double min, double max, double mean, double stddev, int[] histo)
    {
        super();
        myMin = min;
        myMax = max;
        myMean = mean;
        myStddev = stddev;
    }

    /**
     * Gets the max.
     *
     * @return the max
     */
    public double getMax()
    {
        return myMax;
    }

    /**
     * Gets the mean.
     *
     * @return the mean
     */
    public double getMean()
    {
        return myMean;
    }

    /**
     * Gets the min.
     *
     * @return the min
     */
    public double getMin()
    {
        return myMin;
    }

    /**
     * Gets the stddev.
     *
     * @return the stddev
     */
    public double getStddev()
    {
        return myStddev;
    }

//    /**
//     * Gets the histo.
//     *
//     * @return the histo
//     */
//    public int[] getHisto()
//    {
//        return myHisto;
//    }
//
//    /**
//     * Write histo to file.
//     *
//     * @param imageHistogram the image histogram
//     */
//    private void writeHistoToFile(ArrayList<int[]> imageHistogram)
//    {
//        final int numBands = imageHistogram.size();
//        for (int band = 0; band < numBands; band++)
//        {
//            writeHistoBand(imageHistogram, band);
//        }
//    }
//
//    /**
//     * Write histo band.
//     *
//     * @param imageHistogram the image histogram
//     * @param band the band
//     */
//    private void writeHistoBand(ArrayList<int[]> imageHistogram, int band)
//    {
//        File output = new File("band" + band + "Histo" + Thread.currentThread().getId());
//        FileWriter fw = null;
//        try
//        {
//            fw = new FileWriter(output);
//            int meanAccumulator = 0;
//            int countedValues = 0;
//            for (int i = 0; i < 256; i++)
//            {
//                Integer value = imageHistogram.get(band)[i];
//                if (value == null)
//                {
//                    fw.write("0\n");
//                }
//                else
//                {
//                    fw.write(value + "\n");
//                    meanAccumulator += value * i;
//                    countedValues += value;
//                }
//            }
//
//            fw.write("\n" + meanAccumulator / countedValues + " mean\n");
//            int numInZeroBin = imageHistogram.get(band)[0];
//            boolean isOnlyZeros = countedValues - numInZeroBin == 0 ? true : false;
//            if (!isOnlyZeros)
//            {
//                fw.write("\n" + meanAccumulator / (countedValues - numInZeroBin) + " mean ignoring zeros\n");
//            }
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            try
//            {
//                fw.flush();
//                fw.close();
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }
//    }
}
