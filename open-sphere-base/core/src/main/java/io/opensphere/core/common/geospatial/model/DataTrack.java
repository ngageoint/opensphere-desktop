package io.opensphere.core.common.geospatial.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.opensphere.core.common.geospatial.model.interfaces.IDataPoint;
import io.opensphere.core.common.geospatial.model.interfaces.IDataTrack;

/**
 * Represents a line/track of data Used as a transfer object between the ogc
 * server and any clients that want to acquire feature results in a more
 * optimized way. This class is used on both client and server side.
 *
 */
public class DataTrack extends DataPointCollection implements io.opensphere.core.common.geospatial.model.interfaces.IDataTrack
{
    private static final long serialVersionUID = 1L;

    /**
     * Properties that are correlated
     */
    protected String[] correlatedProperties;

    /**
     * Statistics array
     */
    private double[] statistics;

    private double[] independents;

    private double[] xCoefficients;

    private double[] yCoefficients;

    /**
     * Stuff for Dan's prediction algorithm
     */
    private IDataPoint lastSmooth;

    private IDataPoint lastB;

    private List<IDataPoint> predictedPoints;

    private double Alpha;

    private double Gamma;

    private double scale;

    /**
     * Default constructor needed to be serialization friendly.
     */
    public DataTrack()
    {
        super();

        // Stuff for exponential smoothing
        lastSmooth = null;
        lastB = null;
        Alpha = .95;
        Gamma = .88;
        scale = 666;
        predictedPoints = new ArrayList<IDataPoint>();
    }

    /**
     * returns the list of predicted points by exponential smoothing in the
     * track.
     */

    public List<? extends IDataPoint> getPredictedPoints()
    {
        return predictedPoints;
    }

    /**
     * gets last predicted point
     */
    public IDataPoint getLastPredict()
    {
        return predictedPoints.get(predictedPoints.size() - 1);
    }

    /**
     * sets last predicted point
     */
    public void setLastPredict(IDataPoint lastPredict)
    {
        predictedPoints.add(lastPredict);
        ;
    }

    @Override
    public void addTrack(IDataTrack track)
    {
        addPoints(track.getPoints());
        sortPointsByDate();
    }

    public void addPoints(Collection<? extends IDataPoint> list)
    {
        for (IDataPoint point : list)
        {
            add(point);
        }
        sortPointsByDate();
    }

    private void sortPointsByDate()
    {
        // points must be sorted by date for correlation to work
        IDataPoint[] sortable = new IDataPoint[points.size()];
        sortable = points.toArray(sortable);
        Arrays.sort(sortable, new Comparator<IDataPoint>()
        {

            @Override
            public int compare(IDataPoint p1, IDataPoint p2)
            {
                int ret = 0;
                if (p1 == null || p1.getDate() == null)
                {
                    ret = 1;
                }
                else if (p2 == null || p2.getDate() == null)
                {
                    ret = -1;
                }

                else if (p1.getDate().getTime() < p2.getDate().getTime())
                {
                    ret = -1;
                }
                else if (p1.getDate().getTime() > p2.getDate().getTime())
                {
                    ret = 1;
                }

                return ret;
            }

        });
        points.clear();
        for (int i = 0; i < sortable.length; i++)
        {
            add(sortable[i]);
        }
    }

    @Override
    public void addPoint(IDataPoint point)
    {
        IDataPoint endPoint = getEndPoint();
        add(point);
        if (point.getDate() != null && endPoint != null && endPoint.getDate() != null
                && point.getDate().getTime() < endPoint.getDate().getTime())
        {
            sortPointsByDate();
        }
    }

    private void add(IDataPoint point)
    {
        points.add(point);

        updateStatistics(point);

    }

    protected void updateStatistics(IDataPoint point)
    {
        if (correlatedProperties != null)
        {
            for (int i = 0; i < correlatedProperties.length; i++)
            {
                double value = 0.0;
                if (point.getProperty(correlatedProperties[i]) instanceof BigDecimal)
                {
                    value = ((BigDecimal)point.getProperty(correlatedProperties[i])).doubleValue();
                }
                else if (point.getProperty(correlatedProperties[i]) != null)
                {
                    value = Double.parseDouble(point.getProperty(correlatedProperties[i]).toString());
                }
                double newAvg = ((statistics[i] * (points.size() - 1)) + value) / points.size();
                statistics[i] = newAvg;
            }
        }
    }

    @Override
    public IDataPoint getStartPoint()
    {
        IDataPoint point = null;
        if (points.size() > 0)
        {
            point = points.get(0);
        }
        return point;
    }

    @Override
    public IDataPoint getEndPoint()
    {
        IDataPoint point = null;
        if (points.size() > 0)
        {
            point = points.get(points.size() - 1);
        }
        return point;
    }

    public String[] getCorrelatedProperties()
    {
        return correlatedProperties;
    }

    public void setCorrelatedProperties(String[] correlatedProperties)
    {
        if (correlatedProperties != null)
        {
            this.correlatedProperties = correlatedProperties;
            statistics = new double[correlatedProperties.length];
            for (int i = 0; i < statistics.length; i++)
            {
                statistics[i] = 0.0;
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Track " + this.getId() + "\n");
        buf.append("------------\n");
        for (int i = 0; i < points.size(); i++)
        {
            buf.append("Point " + i + "\n");
            buf.append("-- Date: " + (points.get(i).getDate() == null ? "" : points.get(i).getDate().toString()) + "\n");
            buf.append("-- Lat: " + points.get(i).getLat() + "\n");
            buf.append("-- Lon: " + points.get(i).getLon() + "\n");
        }
        buf.append("------------\n");
        return buf.toString();
    }

    public double getStatistic(String string)
    {
        int index = -1;
        for (int i = 0; i < correlatedProperties.length; i++)
        {
            if (correlatedProperties[i].equals(string))
            {
                index = i;
                break;
            }
        }
        return statistics[index];
    }

    /**
     * Get points surrounding this track of points by data. Gets points that
     * have times greater than the given date
     *
     * @param predictionDate Date by which to base what is surrounding or not
     * @return List of data points
     */
    @Override
    public List<? extends IDataPoint> getSurroundingPoints(Date predictionDate)
    {
        List<IDataPoint> ret = new ArrayList<IDataPoint>();
        IDataPoint lastPoint = null;
        if (points.size() == 0)
        {
            return ret;
        }
        else if (predictionDate.getTime() >= getEndPoint().getDate().getTime())
        {
            ret.add(getEndPoint());
        }
        else if (predictionDate.getTime() <= getStartPoint().getDate().getTime())
        {
            ret.add(getStartPoint());
        }
        else
        {
            for (int i = 0; i < points.size(); i++)
            {
                IDataPoint currentPoint = points.get(i);
                if (currentPoint != null && currentPoint.getDate() != null
                        && currentPoint.getDate().getTime() >= predictionDate.getTime())
                {
                    ret.add(lastPoint);
                    ret.add(currentPoint);
                    break;
                }
                lastPoint = currentPoint;
            }
        }

        return ret;
    }

    /**
     * Get the most recent n points
     *
     * @param n number of points to get
     * @return Collection of DataPoints
     */
    @Override
    public List<? extends IDataPoint> getMostRecentPoints(int n)
    {
        if (n >= points.size())
        {
            return points;
        }
        int count = 0;
        List<IDataPoint> ret = new ArrayList<IDataPoint>();
        for (int i = points.size() - 1; count < n && i > 0; i--)
        {
            count++;
            ret.add(points.get(i));
        }
        // reverse the list so they're in temporal order again
        Collections.reverse(ret);
        return ret;
    }

    /**
     * Get points between two dates
     *
     * @param start
     * @param end
     * @return list of points between the given dates
     */
    @Override
    public List<IDataPoint> getPointsBetweenDates(Date start, Date end)
    {
        List<IDataPoint> ret = new ArrayList<IDataPoint>();
        for (int i = 0; i < points.size(); i++)
        {
            IDataPoint point = points.get(i);
            if (point.getDate() != null && point.getDate().getTime() >= start.getTime()
                    && point.getDate().getTime() <= end.getTime())
            {
                ret.add(point);
            }
            else if (point.getDate() != null && point.getDate().getTime() > end.getTime())
            {
                // past the end date, so stop
                break;
            }
        }
        return ret;
    }

    /**
     * Get the least recent n points
     *
     * @param n The number of points to get
     * @return Collection of data points
     */
    @Override
    public List<IDataPoint> getLeastRecentPoints(int n)
    {
        if (n >= points.size())
        {
            return points;
        }
        int count = 0;
        List<IDataPoint> ret = new ArrayList<IDataPoint>();
        for (int i = 0; count < n && i < points.size(); i++)
        {
            count++;
            ret.add(points.get(i));
        }
        return ret;
    }

    public double[] getIndependents()
    {
        return independents;
    }

    public void setIndependents(double[] independents)
    {
        this.independents = independents;
    }

    public double[] getXCoefficients()
    {
        return xCoefficients;
    }

    public void setXCoefficients(double[] dependentXs)
    {
        this.xCoefficients = dependentXs;
    }

    public double[] getYCoefficients()
    {
        return yCoefficients;
    }

    public void setYCoefficients(double[] dependentYs)
    {
        this.yCoefficients = dependentYs;
    }

    public String toShortString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Track " + id + ": [Start: " + getStartPoint().getDate().toString() + " - (" + getStartPoint().getLat() + ","
                + getStartPoint().getLon() + ")]");
        buf.append(" [End: " + getEndPoint().getDate().toString() + " - (" + getEndPoint().getLat() + "," + getEndPoint().getLon()
                + ")]");

        return buf.toString();
    }

    public double getAlpha()
    {
        return Alpha;
    }

    public void setAlpha(double alpha)
    {
        Alpha = alpha;
    }

    public double getGamma()
    {
        return Gamma;
    }

    public void setGamma(double gamma)
    {
        Gamma = gamma;
    }

    public IDataPoint getLastSmooth()
    {
        return lastSmooth;
    }

    public void setLastSmooth(IDataPoint lastSmooth)
    {
        this.lastSmooth = lastSmooth;
    }

    public IDataPoint getLastB()
    {
        return lastB;
    }

    public void setLastB(IDataPoint lastB)
    {
        this.lastB = lastB;
    }

    public double getScale()
    {
        return scale;
    }

    public void setScale(double scale)
    {
        this.scale = scale;
    }

}
