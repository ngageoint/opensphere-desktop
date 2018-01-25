package io.opensphere.core.data.util;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.Nulls;

/**
 * A description of a category of data models. This comprises three parts:
 * <ul>
 * <li>A <b>source</b>, which should be an identifier for the component adding
 * the models; perhaps the {@link Object#toString} result for the object calling
 * to the registry.</li>
 * <li>A <b>family</b>, which should be a string that requesters of the data
 * models can specify, such as the class name of the data models.</li>
 * <li>A <b>category</b>, which is a second level of specifier used by
 * requesters of the data. This is intended to be subordinate to <i>family</i>.
 * </li>
 * </ul>
 * For example, if a Chevy envoy were storing cars in the data registry, the
 * source might be "Chevy", the family might be "Car", and the category might be
 * "Corvette".
 */
public class DataModelCategory
{
    /** A category with all wildcards. */
    public static final DataModelCategory EMPTY = new DataModelCategory(Nulls.STRING, Nulls.STRING, Nulls.STRING);

    /** The category of the models. */
    private final String myCategory;

    /** The family of the models. */
    private final String myFamily;

    /** The source of the models. */
    private final String mySource;

    /**
     * Create a category that only specifies a source.
     *
     * @param source The source.
     * @return The category.
     */
    public static DataModelCategory createWithSource(String source)
    {
        return new DataModelCategory(source, Nulls.STRING, Nulls.STRING);
    }

    /**
     * Constructor.
     *
     * @param source The source of the models.
     * @param family The family of the models.
     * @param category The category of the models. Categories are subordinate to
     *            families.
     */
    public DataModelCategory(String source, String family, String category)
    {
        mySource = source;
        myFamily = family;
        myCategory = category;
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
        DataModelCategory other = (DataModelCategory)obj;
        return EqualsHelper.equals(myCategory, other.myCategory, myFamily, other.myFamily, mySource, other.mySource);
    }

    /**
     * Get the category of the models. Category is subordinate to family.
     *
     * @return The category.
     */
    public String getCategory()
    {
        return myCategory;
    }

    /**
     * Get the family of the models.
     *
     * @return The family.
     */
    public String getFamily()
    {
        return myFamily;
    }

    /**
     * Get the source of the models.
     *
     * @return The source.
     */
    public String getSource()
    {
        return mySource;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myCategory == null ? 0 : myCategory.hashCode());
        result = prime * result + (myFamily == null ? 0 : myFamily.hashCode());
        result = prime * result + (mySource == null ? 0 : mySource.hashCode());
        return result;
    }

    /**
     * Get if this category matches another category, taking wildcards into
     * consideration.
     *
     * @param other The other data model category.
     * @return {@code true} if the categories match.
     */
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean matches(DataModelCategory other)
    {
        if (getSource() != null && other.getSource() != null && !getSource().equals(other.getSource()))
        {
            return false;
        }
        if (getFamily() != null && other.getFamily() != null && !getFamily().equals(other.getFamily()))
        {
            return false;
        }
        if (getCategory() != null && other.getCategory() != null && !getCategory().equals(other.getCategory()))
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(256).append(DataModelCategory.class.getSimpleName()).append("[source[").append(mySource)
                .append("] family[").append(myFamily).append("] category[").append(myCategory).append("]]").toString();
    }

    /**
     * Make a copy of this data model category, but with a different category.
     *
     * @param category The category
     * @return The new data model category.
     */
    public DataModelCategory withCategory(String category)
    {
        return new DataModelCategory(getSource(), getFamily(), category);
    }

    /**
     * Make a copy of this data model category, but with a different source.
     *
     * @param source The source
     * @return The new data model category.
     */
    public DataModelCategory withSource(String source)
    {
        return new DataModelCategory(source, getFamily(), getCategory());
    }
}
