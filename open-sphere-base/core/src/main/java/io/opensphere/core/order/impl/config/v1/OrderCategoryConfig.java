package io.opensphere.core.order.impl.config.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/** Configuration for an order category. */
@XmlRootElement(name = "OrderCategory")
@XmlAccessorType(XmlAccessType.NONE)
public class OrderCategoryConfig
{
    /** The id of this category. */
    @XmlAttribute(name = "categoryId")
    private String myCategoryId;

    /** The maximum of the allowable order range for this category. */
    @XmlAttribute(name = "rangeMax")
    private int myRangeMax;

    /** The minimum of the allowable order range for this category. */
    @XmlAttribute(name = "rangeMin")
    private int myRangeMin;

    /**
     * Constructor.
     *
     * @param categoryId The id of this category.
     * @param rangeMax The maximum of the allowable order range for this
     *            category.
     * @param rangeMin The minimum of the allowable order range for this
     *            category.
     */
    public OrderCategoryConfig(String categoryId, int rangeMax, int rangeMin)
    {
        myCategoryId = categoryId;
        myRangeMax = rangeMax;
        myRangeMin = rangeMin;
    }

    /** Default constructor. */
    protected OrderCategoryConfig()
    {
    }

    /**
     * Get the category id.
     *
     * @return The category id.
     */
    public String getCategoryId()
    {
        return myCategoryId;
    }

    /**
     * Get the maximum of the allowable order range for this category.
     *
     * @return The maximum.
     */
    public int getRangeMax()
    {
        return myRangeMax;
    }

    /**
     * Get the minimum of the allowable order range for this category.
     *
     * @return The minimum.
     */
    public int getRangeMin()
    {
        return myRangeMin;
    }
}
