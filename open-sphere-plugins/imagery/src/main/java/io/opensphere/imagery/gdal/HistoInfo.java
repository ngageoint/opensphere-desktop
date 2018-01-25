package io.opensphere.imagery.gdal;

import java.util.ArrayList;

/**
 * The Class HistoInfo.
 */
public class HistoInfo extends ArrayList<BandHistoBean>
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new histo info.
     */
    public HistoInfo()
    {
        super(3);
    }

    /**
     * Adds the band histo.
     *
     * @param bean the bean
     * @return the index number of the added band.
     */
    public int addBandHisto(BandHistoBean bean)
    {
        this.add(bean);
        return size() - 1;
    }

    /**
     * Clear image histogram.
     */
    public void clearImageHistogram()
    {
        clear();
    }

    /**
     * Gets the band.
     *
     * @param bandNum the band num
     * @return the band
     */
    public BandHistoBean getBand(int bandNum)
    {
        return get(bandNum);
    }
}
