package io.opensphere.core.common.lobintersect;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.opensphere.core.common.geospatial.model.DataEllipse;
import io.opensphere.core.common.time.DateUtils;

/**
 * Container class for an ellipse plus additional fields utilized in Lob
 * Intersection calculations.
 */
public class ClusterEllipse extends DataEllipse
{

    private static final long serialVersionUID = 1L;

    private int clusterId;

    private Date timeStamp;

    private Date localTime;

    private String name;

    private double freq;

    private double range;

    private List<String> lobIdList;

    private String contextLink = "No Link";

    // For GMT

    private SimpleDateFormat sdf;

    // For local time
    private SimpleDateFormat ldf;

    private static double NMToMeter = 1852.0;

    private static double meterToNM = 1.0 / NMToMeter;

    private static final String header = "Cluster,Date,Sites,RF0(MHz),Rng/TOD,LocalDate,"
            + "CenterLat(deg),CenterLon(deg),Orientation(deg),SMA(nmi),SMI(nmi),ContextLink,LayerQuery,LobQuery,Launch";

    private String layerQuery;

    private String lobQuery;

    private String launchCommand;

    @SuppressWarnings("unused")
    private ClusterEllipse()
    {
    }

    /**
     * Constructor
     *
     * @param clusterId
     * @param e
     * @param name
     * @param time
     * @param freq
     * @param range
     * @param localTime
     * @param lobIdList
     */
    public ClusterEllipse(int clusterId, DataEllipse e, String name, Date time, double freq, double range, Date localTime,
            List<String> lobIdList)
    {
        super(e.getSemiMajorAxis() * NMToMeter, e.getSemiMinorAxis() * NMToMeter, e.getOrientation(), e.getLon(), e.getLat());
        this.name = name;
        this.clusterId = clusterId;
        timeStamp = time;
        this.localTime = localTime;
        this.freq = freq;
        this.range = range;
        this.lobIdList = lobIdList;

        TimeZone.setDefault(DateUtils.GMT_TIME_ZONE);
        sdf = new SimpleDateFormat("yyyy/MM/dd/HHmmss.SSS'Z'");
        sdf.setTimeZone(DateUtils.GMT_TIME_ZONE);

        ldf = new SimpleDateFormat("yyyy/MM/dd/HHmmss.SSS'L'");
        ldf.setTimeZone(DateUtils.GMT_TIME_ZONE);

    }

    public int getClusterId()
    {
        return clusterId;
    }

    public void setClusterId(int clusterId)
    {
        this.clusterId = clusterId;
    }

    public String getContextLink()
    {
        return contextLink;
    }

    public Date getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getTime()
    {
        return timeStamp;
    }

    public void setTime(Date time)
    {
        timeStamp = time;
    }

    public Date getLocalTime()
    {
        return localTime;
    }

    public void setLocalTime(Date localTime)
    {
        this.localTime = localTime;
    }

    public double getFreq()
    {
        return freq;
    }

    public void setFreq(double freq)
    {
        this.freq = freq;
    }

    public double getRange()
    {
        return range;
    }

    public void setRange(double range)
    {
        this.range = range;
    }

    public void setContextLink(String contextLink)
    {
        this.contextLink = contextLink;
    }

    /**
     * Returns the header.
     *
     * @return
     */
    public String getHeader()
    {
        return header;
    }

    public List<String> getLobIdList()
    {
        return lobIdList;
    }

    public void setLobIdList(List<String> lobIdList)
    {
        this.lobIdList = lobIdList;
    }

    /**
     * @return the layer
     */
    public String getLayer()
    {
        return layerQuery;
    }

    /**
     * @param layer1 the layer1 to set
     */
    public void setLayerQuery(String val)
    {
        layerQuery = val;
    }

    /**
     * @return the lobQuery
     */
    public String getLobQuery()
    {
        return lobQuery;
    }

    /**
     * @param lobQuery the lobQuery to set
     */
    public void setLobQuery(String lobQuery)
    {
        this.lobQuery = lobQuery;
    }

    /**
     * @return the launchCommand
     */
    public String getLaunchCommand()
    {
        return launchCommand;
    }

    /**
     * @param launchCommand the launchCommand to set
     */
    public void setLaunchCommand(String launchCommand)
    {
        this.launchCommand = launchCommand;
    }

    /**
     * Creates a string of values in CSV format. The order must correlate with
     * the header.
     *
     * @return String
     */
    public String toCSV()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(clusterId);
        builder.append("," + sdf.format(timeStamp));
        builder.append("," + name);
        // MHz
        builder.append("," + freq / 1000000);
        // km
        builder.append("," + range / 1000);
        builder.append("," + ldf.format(localTime));
        // Deg
        builder.append("," + lat);
        // Deg
        builder.append("," + lon);
        // Deg
        builder.append("," + getOrientation());
        // NM
        builder.append("," + getSemiMajorAxis() * meterToNM);
        // NM
        builder.append("," + getSemiMinorAxis() * meterToNM);
        builder.append("," + contextLink);
        builder.append("," + layerQuery);
        builder.append("," + lobQuery);
        builder.append("," + launchCommand);

        builder.append("\n");

        return builder.toString();
    }
}
