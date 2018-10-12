package io.opensphere.mantle.data.impl.dgset.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoActiveHistoryRecord;

/**
 * The Class JAXBDataGroupInfoActiveSet.
 */
@XmlRootElement(name = "DataGroupInfoActiveHistoryList")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBDataGroupInfoActiveHistoryList
{
    /** The history list. */
    @XmlElement(name = "activeHistory", required = false)
    private final List<JAXBDataGroupInfoActiveHistoryRecord> myHistoryList;

    /** The history list lock. */
    private final transient ReentrantLock myHistoryListLock = new ReentrantLock();

    /**
     * Instantiates a new jAXB data group info active set.
     */
    public JAXBDataGroupInfoActiveHistoryList()
    {
        myHistoryList = New.list();
    }

    /**
     * Instantiates a new jAXB data group info active set.
     *
     * @param other the other
     */
    public JAXBDataGroupInfoActiveHistoryList(JAXBDataGroupInfoActiveHistoryList other)
    {
        this();
        Utilities.checkNull(other, "other");
        myHistoryListLock.lock();
        try
        {
            other.myHistoryList.stream().map(JAXBDataGroupInfoActiveHistoryRecord::new).forEach(myHistoryList::add);
        }
        finally
        {
            myHistoryListLock.unlock();
        }
    }

    /**
     * Clear activity history.
     */
    public void clearActivityHistory()
    {
        myHistoryListLock.lock();
        try
        {
            myHistoryList.clear();
        }
        finally
        {
            myHistoryListLock.unlock();
        }
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
        JAXBDataGroupInfoActiveHistoryList other = (JAXBDataGroupInfoActiveHistoryList)obj;
        return Objects.equals(myHistoryList, other.myHistoryList);
    }

    /**
     * Gets the activity history.
     *
     * @return the activity history
     */
    public List<DataGroupInfoActiveHistoryRecord> getActivityHistory()
    {
        List<DataGroupInfoActiveHistoryRecord> result = null;
        myHistoryListLock.lock();
        try
        {
            result = new ArrayList<>(myHistoryList);
        }
        finally
        {
            myHistoryListLock.unlock();
        }
        return result;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myHistoryList == null ? 0 : myHistoryList.hashCode());
        return result;
    }

    /**
     * Note a set of data group info have been active in the history.
     *
     * @param dgiCollection the dgi collection to note active.
     */
    public void noteActive(Collection<DataGroupInfo> dgiCollection)
    {
        if (dgiCollection != null && !dgiCollection.isEmpty())
        {
            List<JAXBDataGroupInfoActiveHistoryRecord> recsToAdd = New.list(dgiCollection.size());
            Date aDate = new Date();
            Set<String> idSet = New.set(dgiCollection.size());
            for (DataGroupInfo dgi : dgiCollection)
            {
                idSet.add(dgi.getId());
                recsToAdd.add(new JAXBDataGroupInfoActiveHistoryRecord(dgi.getId(), aDate));
            }

            myHistoryListLock.lock();
            try
            {
                Iterator<JAXBDataGroupInfoActiveHistoryRecord> recItr = myHistoryList.iterator();
                while (recItr.hasNext())
                {
                    JAXBDataGroupInfoActiveHistoryRecord rec = recItr.next();
                    if (idSet.contains(rec.getId()))
                    {
                        idSet.remove(rec.getId());
                        recItr.remove();
                    }
                }
                myHistoryList.addAll(recsToAdd);
            }
            finally
            {
                myHistoryListLock.unlock();
            }
        }
    }

    /**
     * Note that a data group info has been active active.
     *
     * @param dgi the DataGroupInfo
     */
    public void noteActive(DataGroupInfo dgi)
    {
        Date aDate = new Date();
        String id = dgi.getId();
        myHistoryListLock.lock();
        try
        {
            Iterator<JAXBDataGroupInfoActiveHistoryRecord> recItr = myHistoryList.iterator();
            boolean found = false;
            while (!found && recItr.hasNext())
            {
                JAXBDataGroupInfoActiveHistoryRecord rec = recItr.next();
                if (Objects.equals(rec.getId(), id))
                {
                    found = true;
                    recItr.remove();
                }
            }
            myHistoryList.add(new JAXBDataGroupInfoActiveHistoryRecord(id, aDate));
        }
        finally
        {
            myHistoryListLock.unlock();
        }
    }

    /**
     * Sort and de-duplicate activity history.
     */
    public void sortAndDeduplicateActivityHistory()
    {
        myHistoryListLock.lock();
        try
        {
            Collections.sort(myHistoryList, (o1, o2) -> o1.getDate().compareTo(o2.getDate()));
            Set<String> idSet = New.set();
            Iterator<JAXBDataGroupInfoActiveHistoryRecord> recItr = myHistoryList.iterator();
            while (recItr.hasNext())
            {
                JAXBDataGroupInfoActiveHistoryRecord rec = recItr.next();
                if (idSet.contains(rec.getId()))
                {
                    recItr.remove();
                }
                else
                {
                    idSet.add(rec.getId());
                }
            }
        }
        finally
        {
            myHistoryListLock.unlock();
        }
    }
}
