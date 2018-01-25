package io.opensphere.core.common.lobintersect;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import io.opensphere.core.common.time.DateUtils;

/**
 * Container used by the Lob Intersect algorithm for holding intermediate
 * results.
 */
public class LobIntersectPoint implements Comparable<LobIntersectPoint>
{

    private Date timeStamp;

    private Date localTime;

    private double frequency1;

    private double frequency2;

    private double lat;

    private double lon;

    private double range;

    private double theta;

    private double sma;

    private double smi;

    // Lobs contributing to this intersection
    private List<LobPoint> lobs;

    // private String contextLink="None";

    private DoubleMatrix2D Pyy = new DenseDoubleMatrix2D(2, 2);

    private int clusterNumber;

    // Concatenation of site names contributing to intersection.
    private String name;

    // For GMT
    private SimpleDateFormat sdf;

    // For local time
    private SimpleDateFormat ldf;

    private static double meterToNM = 0.53995680345572e-03;

    private static final String header = "Cluster,Date,Sites,CenterLat(deg),CenterLon(deg),RF0(Hz),RF1(Hz),Rng/TOD,"
            + "LocalDate,Orientation(deg),SMA(nmi),SMI(nmi),P00,P01,P10,P11";

    public LobIntersectPoint(LobPoint pt1, LobPoint pt2, double lat, double lon, Date localTime, double range, double theta,
            double sma, double smi, DoubleMatrix2D Pyy)
    {
        super();
        frequency1 = pt1.getFreq();
        frequency2 = pt2.getFreq();

        this.lat = lat;
        this.lon = lon;
        this.range = range;
        timeStamp = pt1.getTimeStamp();
        clusterNumber = -1;
        this.theta = theta;
        this.sma = sma;
        this.smi = smi;
        this.Pyy = Pyy;

        name = pt1.getPtName().concat("/").concat(pt2.getPtName());

        lobs = new ArrayList<>();
        lobs.add(pt1);
        lobs.add(pt2);

        TimeZone.setDefault(DateUtils.GMT_TIME_ZONE);
        sdf = new SimpleDateFormat("yyyy/MM/dd/HHmmss.SSS'Z'");
        sdf.setTimeZone(DateUtils.GMT_TIME_ZONE);

        ldf = new SimpleDateFormat("yyyy/MM/dd/HHmmss.SSS'L'");
        ldf.setTimeZone(DateUtils.GMT_TIME_ZONE);

        this.localTime = localTime;
    }

    public Date getLocalTime()
    {
        return localTime;
    }

    public void setLocalTime(Date localTime)
    {
        this.localTime = localTime;
    }

    public double getTheta()
    {
        return theta;
    }

    public void setTheta(double theta)
    {
        this.theta = theta;
    }

    public double getSma()
    {
        return sma;
    }

    public void setSma(double sma)
    {
        this.sma = sma;
    }

    public double getSmi()
    {
        return smi;
    }

    public void setSmi(double smi)
    {
        this.smi = smi;
    }

    public DoubleMatrix2D getPyy()
    {
        return Pyy;
    }

    public void setPyy(DoubleMatrix2D pyy)
    {
        Pyy = pyy;
    }

    public int getClusterNumber()
    {
        return clusterNumber;
    }

    public void setClusterNumber(int clusterNumber)
    {
        this.clusterNumber = clusterNumber;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public double getLat()
    {
        return lat;
    }

    public void setLat(double lat)
    {
        this.lat = lat;
    }

    public double getLon()
    {
        return lon;
    }

    public void setLon(double lon)
    {
        this.lon = lon;
    }

    public double getRange()
    {
        return range;
    }

    public void setRange(double range)
    {
        this.range = range;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(frequency1);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(frequency2);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(lat);
        result = prime * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(lon);
        result = prime * result + (int)(temp ^ temp >>> 32);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (timeStamp == null ? 0 : timeStamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        LobIntersectPoint other = (LobIntersectPoint)obj;
        if (Double.doubleToLongBits(frequency1) != Double.doubleToLongBits(other.frequency1))
        {
            return false;
        }
        if (Double.doubleToLongBits(frequency2) != Double.doubleToLongBits(other.frequency2))
        {
            return false;
        }

        if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
        {
            return false;
        }
        if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
        {
            return false;
        }
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }
        if (timeStamp == null)
        {
            if (other.timeStamp != null)
            {
                return false;
            }
        }
        else if (!timeStamp.equals(other.timeStamp))
        {
            return false;
        }
        return true;
    }

    public String toCSV()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(clusterNumber);
        builder.append("," + sdf.format(timeStamp));
        builder.append("," + name);
        // Deg
        builder.append("," + Math.toDegrees(lat));
        // Deg
        builder.append("," + Math.toDegrees(lon));
        // MHz
        builder.append("," + frequency1 / 1000000);
        // MHz
        builder.append("," + frequency2 / 1000000);
        // km
        builder.append("," + range / 1000);
        builder.append("," + ldf.format(localTime));
        // Deg
        builder.append("," + Math.toDegrees(theta));
        // NM
        builder.append("," + sma * meterToNM);
        // NM
        builder.append("," + smi * meterToNM);
        builder.append("," + Pyy.getQuick(0, 0));
        builder.append("," + Pyy.getQuick(0, 1));
        builder.append("," + Pyy.getQuick(1, 0));
        builder.append("," + Pyy.getQuick(1, 1));
        // builder.append("," + contextLink);
        builder.append("\n");

        return builder.toString();
    }

    public static String getHeader()
    {
        return LobIntersectPoint.header;
    }

    @Override
    public String toString()
    {
        return "LobIntersectPoint [" + "clusterNumber=" + clusterNumber + ", timeStamp=" + timeStamp + ", name=" + name + ", lat="
                + lat + ", lon=" + lon + ", frequency1=" + frequency1 + ", frequency2=" + frequency2 + ", range=" + range
                + ", localTime=" + localTime + ", theta=" + theta + ", sma=" + sma + ", smi=" + smi + ", Pyy=" + Pyy +
                // ", contextLink=" + contextLink +
                "]";
    }

    public double getFrequency1()
    {
        return frequency1;
    }

    public void setFrequency1(double frequency1)
    {
        this.frequency1 = frequency1;
    }

    public double getFrequency2()
    {
        return frequency2;
    }

    public void setFrequency2(double frequency2)
    {
        this.frequency2 = frequency2;
    }
//    public String getContextLink() {
//        return contextLink;
//    }
//
//    public void setContextLink(String contextLink) {
//        this.contextLink = contextLink;
//    }

    @Override
    public int compareTo(LobIntersectPoint other)
    {

        if (equals(other))
        {
            return 0;
        }

        if (!timeStamp.equals(other.timeStamp))
        {
            return timeStamp.compareTo(other.timeStamp);
        }

        if (frequency1 < other.frequency1)
        {
            return -1;
        }
        else if (frequency1 > other.frequency1)
        {
            return 1;
        }

        if (frequency2 < other.frequency2)
        {
            return -1;
        }
        else if (frequency2 > other.frequency2)
        {
            return 1;
        }

        if (lat < other.lat)
        {
            return -1;
        }
        else if (lat > other.lat)
        {
            return 1;
        }

        if (lon < other.lon)
        {
            return -1;
        }
        else if (lon > other.lon)
        {
            return 1;
        }

        return name.compareTo(other.name);
    }

    public void addLob(LobPoint lob)
    {
        lobs.add(lob);
    }

    public List<LobPoint> getLobs()
    {
        return lobs;
    }
}
