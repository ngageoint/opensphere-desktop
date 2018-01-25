package io.opensphere.mantle.data.impl.dgset.v1;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.mantle.data.DataGroupInfoActiveHistoryRecord;

/**
 * The Class DataGroupInfoActiveHistoryRecord.
 */
@XmlRootElement(name = "DataGroupInfoActiveHistoryRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBDataGroupInfoActiveHistoryRecord implements DataGroupInfoActiveHistoryRecord
{
    /** The group ids. */
    @XmlAttribute(name = "date")
    private Date myDate;

    /** The name. */
    @XmlAttribute(name = "id")
    private String myId;

    /**
     * Instantiates a new jAXB data group info active history record.
     *
     * @param rec the rec
     */
    public JAXBDataGroupInfoActiveHistoryRecord(JAXBDataGroupInfoActiveHistoryRecord rec)
    {
        myId = rec.getId();
        myDate = rec.getDate();
    }

    /**
     * Instantiates a new jAXB data group info active history record.
     *
     * @param id the id
     * @param date the date
     */
    public JAXBDataGroupInfoActiveHistoryRecord(String id, Date date)
    {
        myId = id;
        myDate = new Date(date.getTime());
    }

    /**
     * Instantiates a new jAXB data group info active history record.
     */
    protected JAXBDataGroupInfoActiveHistoryRecord()
    {
    }

    @Override
    public Date getDate()
    {
        return new Date(myDate.getTime());
    }

    @Override
    public String getId()
    {
        return myId;
    }

    /**
     * Sets the date.
     *
     * @param date the new date
     */
    protected void setDate(Date date)
    {
        myDate = date;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    protected void setId(String id)
    {
        myId = id;
    }
}
