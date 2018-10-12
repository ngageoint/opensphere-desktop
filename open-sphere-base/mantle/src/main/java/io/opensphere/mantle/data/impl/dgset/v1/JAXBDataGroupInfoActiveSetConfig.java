package io.opensphere.mantle.data.impl.dgset.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoActiveHistoryRecord;
import io.opensphere.mantle.data.DataGroupInfoActiveSet;

/**
 * The Class JAXBDataGroupInfoActiveSetConfig.
 */
@XmlRootElement(name = "DataGroupInfoActiveSetConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBDataGroupInfoActiveSetConfig
{
    /** The history. */
    @XmlElement(name = "activeHistory")
    private final JAXBDataGroupInfoActiveHistoryList myHistoryList;

    /** The set lock. */
    private final transient ReentrantLock mySetLock = new ReentrantLock();

    /** The sets. */
    @XmlElement(name = "activeSet", required = true)
    private final List<JAXBDataGroupInfoActiveSet> mySets;

    /**
     * Instantiates a new jAXB data group info active set config.
     */
    public JAXBDataGroupInfoActiveSetConfig()
    {
        mySets = New.list();
        myHistoryList = new JAXBDataGroupInfoActiveHistoryList();
    }

    /**
     * Instantiates a new jAXB data group info active set config.
     *
     * @param other the other
     */
    public JAXBDataGroupInfoActiveSetConfig(JAXBDataGroupInfoActiveSetConfig other)
    {
        Utilities.checkNull(other, "other");
        mySets = New.list();
        other.mySetLock.lock();
        try
        {
            other.mySets.stream().map(JAXBDataGroupInfoActiveSet::new).forEach(mySets::add);
        }
        finally
        {
            other.mySetLock.unlock();
        }
        myHistoryList = new JAXBDataGroupInfoActiveHistoryList(other.myHistoryList);
    }

    /**
     * Adds the set.
     *
     * @param setToAdd the set to add
     */
    public void addSet(DataGroupInfoActiveSet setToAdd)
    {
        mySetLock.lock();
        try
        {
            mySets.add(new JAXBDataGroupInfoActiveSet(setToAdd));
        }
        finally
        {
            mySetLock.unlock();
        }
    }

    /**
     * Clear activity history.
     */
    public void clearActivityHistory()
    {
        myHistoryList.clearActivityHistory();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        JAXBDataGroupInfoActiveSetConfig other = (JAXBDataGroupInfoActiveSetConfig)obj;
        return EqualsHelper.equals(mySets, other.mySets, myHistoryList, other.myHistoryList);
    }

    /**
     * Gets the activity history.
     *
     * @return the activity history
     */
    public List<DataGroupInfoActiveHistoryRecord> getActivityHistory()
    {
        return myHistoryList.getActivityHistory();
    }

    /**
     * Returns the first set found that has the given name.
     *
     * @param name the name to search with
     * @return the first found {@link DataGroupInfoActiveSet} with the given
     *         name.
     */
    public DataGroupInfoActiveSet getSetByName(String name)
    {
        Utilities.checkNull(name, "name");
        mySetLock.lock();
        try
        {
            return mySets.stream().filter(set -> name.equals(set.getName())).findFirst().orElse(null);
        }
        finally
        {
            mySetLock.unlock();
        }
    }

    /**
     * Gets the names of all the sets.
     *
     * @return the names
     */
    public List<String> getSetNames()
    {
        List<String> result = New.list();
        mySetLock.lock();
        try
        {
            mySets.stream().map(s -> s.getName()).forEach(result::add);
        }
        finally
        {
            mySetLock.unlock();
        }

        return result.isEmpty() ? Collections.<String>emptyList() : Collections.unmodifiableList(result);
    }

    /**
     * Gets an unmodifiable copy of the sets list.
     *
     * @return the sets
     */
    public List<DataGroupInfoActiveSet> getSets()
    {
        List<DataGroupInfoActiveSet> result = null;
        mySetLock.lock();
        try
        {
            result = Collections.unmodifiableList(new ArrayList<DataGroupInfoActiveSet>(mySets));
        }
        finally
        {
            mySetLock.unlock();
        }
        return result;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mySets == null ? 0 : mySets.hashCode());
        result = prime * result + (myHistoryList == null ? 0 : myHistoryList.hashCode());
        return result;
    }

    /**
     * Checks for name.
     *
     * Stops on first found, may be more than one.
     *
     * @param name the name
     * @return true, if successful
     */
    public boolean hasName(String name)
    {
        return getSetByName(name) != null;
    }

    /**
     * Note a set of data group info have been active in the history.
     *
     * @param dgiCollection the dgi collection to note active.
     */
    public void noteActive(Collection<DataGroupInfo> dgiCollection)
    {
        myHistoryList.noteActive(dgiCollection);
    }

    /**
     * Note that a data group info has been active active.
     *
     * @param dgi the DataGroupInfo
     */
    public void noteActive(DataGroupInfo dgi)
    {
        myHistoryList.noteActive(dgi);
    }

    /**
     * Removes the set by name. Note will remove all sets with the specified
     * name.
     *
     * @param setName the set name
     * @return the sets that were removed.
     */
    public Set<DataGroupInfoActiveSet> removeSet(String setName)
    {
        Utilities.checkNull(setName, "setName");
        Set<DataGroupInfoActiveSet> removeSet = New.set();
        mySetLock.lock();
        try
        {
            mySets.stream().filter(set -> setName.equals(set.getName())).forEach(removeSet::add);
            if (!removeSet.isEmpty())
            {
                mySets.removeAll(removeSet);
            }
        }
        finally
        {
            mySetLock.unlock();
        }
        return removeSet.isEmpty() ? Collections.<DataGroupInfoActiveSet>emptySet() : Collections.unmodifiableSet(removeSet);
    }

    /**
     * Sort and de-duplicate activity history.
     */
    public void sortAndDeduplicateActivityHistory()
    {
        myHistoryList.sortAndDeduplicateActivityHistory();
    }
}
