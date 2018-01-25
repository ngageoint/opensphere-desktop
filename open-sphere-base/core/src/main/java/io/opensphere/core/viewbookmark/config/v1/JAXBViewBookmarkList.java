package io.opensphere.core.viewbookmark.config.v1;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewbookmark.ViewBookmark;

/**
 * The Class JAXBViewBookmarkList.
 */
@XmlRootElement(name = "ViewBookmarkList")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBViewBookmarkList
{
    /** The Book mark list. */
    @XmlElement(name = "bookmark")
    private List<JAXBViewBookmark> myBookmarkList;

    /**
     * Instantiates a new jAXB view book mark list.
     */
    public JAXBViewBookmarkList()
    {
        myBookmarkList = New.list();
    }

    /**
     * Instantiates a new jAXB view book mark list.
     *
     * @param bmCollection the {@link ViewBookmark} collection
     */
    public JAXBViewBookmarkList(Collection<? extends ViewBookmark> bmCollection)
    {
        Utilities.checkNull(bmCollection, "bmCollection");
        myBookmarkList = bmCollection.isEmpty() ? New.<JAXBViewBookmark>list() : New.<JAXBViewBookmark>list(bmCollection.size());
        if (!bmCollection.isEmpty())
        {
            for (ViewBookmark bm : bmCollection)
            {
                myBookmarkList.add(new JAXBViewBookmark(bm));
            }
        }
    }

    /**
     * Gets the book mark list.
     *
     * @return the book mark list
     */
    public final List<JAXBViewBookmark> getBookmarkList()
    {
        return myBookmarkList;
    }

    /**
     * Sets the book mark list.
     *
     * @param bookmarkList the new book mark list
     */
    public final void setBookmarkList(List<JAXBViewBookmark> bookmarkList)
    {
        myBookmarkList = bookmarkList;
    }
}
