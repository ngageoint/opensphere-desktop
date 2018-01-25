package io.opensphere.analysis.binning.criteria;

import java.util.Observable;
import java.util.Observer;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains all the criteria information needed to bin on a specific layer. This
 * contains a list of bin criteria that identifies a unique bin so that every
 * record within the bin represents the same object in the real world.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BinCriteria extends Observable implements Observer, ListChangeListener<BinCriteriaElement>
{
    /**
     * The criterias property.
     */
    public static final String CRITERIAS_PROP = "criterias";

    /**
     * The data type key property.
     */
    public static final String DATA_TYPE_KEY_PROP = "dataTypeKey";

    /**
     * The list of criteria that makes a unique bin of records.
     */
    @XmlElement(name = "criteria")
    private final ObservableList<BinCriteriaElement> myCriterias = FXCollections.observableArrayList();

    /**
     * The layer id the criteria pertains to.
     */
    @XmlAttribute(name = "dataTypeKey")
    private String myDataTypeKey;

    /**
     * Default constructor.
     */
    public BinCriteria()
    {
        myCriterias.addListener(this);
    }

    /**
     * Gets the list of criteria that makes a unique bin of records.
     *
     * @return The list of criteria that makes a unique bin of records.
     */
    public ObservableList<BinCriteriaElement> getCriterias()
    {
        return myCriterias;
    }

    /**
     * Gets the layer id.
     *
     * @return The id of the layer the criteria was built for.
     */
    public String getDataTypeKey()
    {
        return myDataTypeKey;
    }

    /**
     * Set the layer id.
     *
     * @param dataTypeKey The layer id the criteria pertains to.
     */
    public void setDataTypeKey(String dataTypeKey)
    {
        myDataTypeKey = dataTypeKey;
        setChanged();
        notifyObservers(DATA_TYPE_KEY_PROP);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        setChanged();
        notifyObservers(CRITERIAS_PROP);
    }

    @Override
    public void onChanged(javafx.collections.ListChangeListener.Change<? extends BinCriteriaElement> c)
    {
        while (c.next())
        {
            for (BinCriteriaElement criteria : c.getAddedSubList())
            {
                criteria.addObserver(this);
            }

            for (BinCriteriaElement element : c.getRemoved())
            {
                element.deleteObserver(this);
            }

            setChanged();
            notifyObservers(CRITERIAS_PROP);
        }
    }
}
