package io.opensphere.core.control;

/** Abstract base class for event adapters. */
public abstract class BoundEventAdapter implements BoundEventListener
{
    /**
     * The category under which this event will be displayed on the key binding
     * frame. For example, "View".
     */
    private final String myCategory;

    /**
     * The description the user will see. This can be long.
     */
    private final String myDescription;

    /** The Is reassignable. */
    private boolean myIsReassignable = true;

    /**
     * The title of this event. The summary title the user will see. It should
     * be short, a few words at most.
     */
    private final String myTitle;

    /**
     * Constructor.
     *
     * @param category the category under which this event will be displayed on
     *            the key binding frame. For example, "View".
     * @param title the title The summary title the user will see. It should be
     *            short, a few words at most.
     * @param description the description the user will see. This can be several
     *            sentences long. It is usually shown in a tooltip, so limited
     *            html is allowed. The description, once in the tooltip, will be
     *            contained between the tags: "
     *            <code>&lt;html&gt;&lt;font face=\"sansserif\"&gt;" + description + "&lt;/font&gt;&lt;/html&gt;</code>
     *            "
     */
    public BoundEventAdapter(String category, String title, String description)
    {
        myTitle = title;
        myDescription = description;
        myCategory = category;
    }

    @Override
    public String getCategory()
    {
        return myCategory;
    }

    @Override
    public String getDescription()
    {
        return myDescription;
    }

    @Override
    public int getTargetPriority()
    {
        return -1;
    }

    @Override
    public String getTitle()
    {
        return myTitle;
    }

    @Override
    public boolean isReassignable()
    {
        return myIsReassignable;
    }

    @Override
    public boolean isTargeted()
    {
        return false;
    }

    @Override
    public boolean mustBeTargeted()
    {
        return false;
    }

    /**
     * Sets the reassignable.
     *
     * @param isReassignable the new reassignable
     */
    public void setReassignable(boolean isReassignable)
    {
        myIsReassignable = isReassignable;
    }
}
