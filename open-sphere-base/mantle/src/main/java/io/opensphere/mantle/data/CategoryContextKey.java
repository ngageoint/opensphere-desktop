package io.opensphere.mantle.data;

/**
 * Context key used for operations on a category in the layers panel.
 */
public class CategoryContextKey
{
    /** The category. */
    private final String myCategory;

    /**
     * Constructor.
     *
     * @param category The category.
     *
     */
    public CategoryContextKey(String category)
    {
        myCategory = category;
    }

    /**
     * Get the category.
     *
     * @return The category.
     */
    public String getCategory()
    {
        return myCategory;
    }
}
