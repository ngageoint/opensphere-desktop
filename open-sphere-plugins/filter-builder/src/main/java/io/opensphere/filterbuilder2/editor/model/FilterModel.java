package io.opensphere.filterbuilder2.editor.model;

import java.util.Set;
import java.util.TreeSet;

import io.opensphere.core.util.swing.input.model.NameModel;
import io.opensphere.core.util.swing.input.model.TextModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * A GUI model for a filter.
 */
public class FilterModel extends WrappedModel<Filter>
{
    /** The data type. */
    private final DataTypeInfo myDataType;

    /** The filter name. */
    private final NameModel myFilterName = new NameModel();

    /** The Filter description. */
    private TextModel myFilterDescription;

    /** The group. */
    private final GroupModel myGroup;

    /**
     * Constructor.
     *
     * @param dataType the data type
     */
    public FilterModel(DataTypeInfo dataType)
    {
        myDataType = dataType;

        myGroup = new GroupModel(dataType);

        myFilterName.setNameAndDescription("Name", "The filter name");

        addModel(myFilterName);
        addModel(getFilterDescription());
        addModel(myGroup);
    }

    /**
     * Gets the fields encapsulated in the stored group.
     *
     * @return the fields extracted from the group.
     */
    public Set<String> getFields()
    {
        Set<String> fields = new TreeSet<>();
        recurseFields(myGroup, fields);
        return fields;
    }

    /**
     * Recurses through the supplied group model, collecting fields into the supplied container.
     *
     * @param g the group to process.
     * @param fields the container in which to collect the fields.
     */
    private void recurseFields(GroupModel g, Set<String> fields)
    {
        g.getCriteria().stream().forEach(c -> fields.add(c.getField().get()));
        g.getGroups().stream().forEach(grp -> recurseFields(grp, fields));
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public DataTypeInfo getDataType()
    {
        return myDataType;
    }

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    public NameModel getFilterName()
    {
        return myFilterName;
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public GroupModel getGroup()
    {
        return myGroup;
    }

    /**
     * Gets the filter description.
     *
     * @return the filter description
     */
    public final TextModel getFilterDescription()
    {
        if (myFilterDescription == null)
        {
            myFilterDescription = new TextModel();
            myFilterDescription.setNameAndDescription("Filter Description", "The filter description.");
            myFilterDescription.setValidating(false);
        }
        return myFilterDescription;
    }

    /**
     * Sets the filter description.
     *
     * @param description the new filter description
     */
    public void setFilterDescription(String description)
    {
        getFilterDescription().set(description);
    }

    @Override
    protected void updateDomainModel(Filter domainModel)
    {
        myGroup.applyChanges();
        domainModel.setFilterGroup(myGroup.get());
        domainModel.setName(myFilterName.get());
        domainModel.setFilterDescription(myFilterDescription.get());
    }

    @Override
    protected void updateViewModel(Filter domainModel)
    {
        myFilterName.set(domainModel.getName());
        getFilterDescription().set(domainModel.getFilterDescription());
        myGroup.setFilter(domainModel);
        myGroup.set(domainModel.getFilterGroup());
    }
}
