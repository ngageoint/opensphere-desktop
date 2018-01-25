package io.opensphere.filterbuilder2.editor.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.filterbuilder.filter.v1.CommonFieldGroup;
import io.opensphere.filterbuilder.filter.v1.Criteria;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Group;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * A GUI model for a filter group.
 */
@SuppressWarnings("PMD.GodClass")
public class GroupModel extends WrappedModel<Group>
{
    /** The collection of converters. */
    private static final Collection<SpecialCriterionConverter> SPECIAL_CONVERTERS = New.unmodifiableList(InListConverter.IN_LIST,
            InListConverter.NOT_IN_LIST, InListConverter.LIKE_LIST, InListConverter.NOT_LIKE_LIST, new InRangeConverter());

    /** The data type. */
    private final DataTypeInfo myDataType;

    /** The operator. */
    private final ChoiceModel<Logical> myOperator = new ChoiceModel<>();

    /** The groups. */
    private final List<GroupModel> myGroups = new ArrayList<>();

    /** The criteria. */
    private final List<CriterionModel> myCriteria = new ArrayList<>();

    /** The filter. */
    private Filter myFilter;

    /** The error message. */
    private String myError;

    /** The parent. */
    private GroupModel myParent;

    /** The current action, if any. */
    private String myAction;

    /**
     * Gets the converter that accepts the given group.
     *
     * @param group the group
     * @return the converter
     */
    private static SpecialCriterionConverter getAcceptingConverter(CommonFieldGroup group)
    {
        SpecialCriterionConverter groupConverter = null;
        for (SpecialCriterionConverter converter : SPECIAL_CONVERTERS)
        {
            if (converter.accepts(group))
            {
                groupConverter = converter;
                break;
            }
        }
        return groupConverter;
    }

    /**
     * Constructor.
     *
     * @param dataType the data type
     */
    public GroupModel(DataTypeInfo dataType)
    {
        myDataType = dataType;

        myOperator.setOptions(Logical.values());

        myOperator.setNameAndDescription("Grouping Operator", "The grouping operator");

        addModel(myOperator);
    }

    /**
     * Adds a criterion.
     *
     * @param criterion the criterion
     */
    public void addCriterion(CriterionModel criterion)
    {
        addCriterionNoEvent(criterion);
        setChanged(true);
        myAction = "criteriaListChange";
        fireChangeEvent();
        myAction = null;
    }

    /**
     * Adds a group.
     *
     * @param group the group
     */
    public void addGroup(GroupModel group)
    {
        addGroupNoEvent(group);
        setChanged(true);
        fireChangeEvent();
    }

    /**
     * Adds a new criterion to the model.
     */
    public void addNewCriterion()
    {
        String field = null;
        Conditional operator = null;
        // Use the previous field/operator if there is one
        if (!myCriteria.isEmpty())
        {
            CriterionModel previousModel = myCriteria.get(myCriteria.size() - 1);
            field = previousModel.getField().get();
            operator = previousModel.getOperator().get();
        }
        // Use the first field in the list
        else if (!myFilter.getColumns().isEmpty())
        {
            field = myFilter.getColumns().iterator().next();
        }

        CriterionModel criterion = new CriterionModel(myFilter, myDataType);
        criterion.set(new Criteria(field));
        if (operator != null)
        {
            criterion.getOperator().set(operator);
        }
        addCriterion(criterion);
    }

    /**
     * Adds a new group to the model.
     */
    public void addNewGroup()
    {
        GroupModel group = new GroupModel(myDataType);
        group.setFilter(myFilter);
        group.set(new Group(Logical.AND));
        addGroup(group);
    }

    /**
     * Gets the current action.
     *
     * @return the action
     */
    public String getAction()
    {
        return myAction;
    }

    /**
     * Gets the count of direct groups and criteria under this group.
     *
     * @return the count
     */
    public int getCount()
    {
        return myGroups.size() + myCriteria.size();
    }

    /**
     * Gets the criteria.
     *
     * @return the criteria
     */
    public List<CriterionModel> getCriteria()
    {
        return myCriteria;
    }

    @Override
    public String getErrorMessage()
    {
        return myError != null ? myError : super.getErrorMessage();
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    public List<GroupModel> getGroups()
    {
        return myGroups;
    }

    /**
     * Gets the operator.
     *
     * @return the operator
     */
    public ChoiceModel<Logical> getOperator()
    {
        return myOperator;
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    public GroupModel getParent()
    {
        return myParent;
    }

    /**
     * Gets the count of all groups and criteria under this group (recursively).
     *
     * @return the count
     */
    public int getTotalCount()
    {
        int count = 1 + myCriteria.size();
        for (GroupModel group : myGroups)
        {
            count += group.getTotalCount();
        }
        return count;
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        myError = null;
        if (super.getValidationStatus() == ValidationStatus.VALID)
        {
            if (!isValidating())
            {
                return ValidationStatus.VALID;
            }

            if (myGroups.isEmpty() && myCriteria.isEmpty())
            {
                myError = "At least one expression is required in a group.";
                return ValidationStatus.ERROR;
            }

            if (myOperator.get() == Logical.NOT && getCount() > 1)
            {
                myError = "A NOT group may only contain one expression or group.";
                return ValidationStatus.ERROR;
            }

            return ValidationStatus.VALID;
        }
        return ValidationStatus.ERROR;
    }

    /**
     * Removes a criterion.
     *
     * @param criterion the criterion
     */
    public void removeCriterion(CriterionModel criterion)
    {
        removeCriterionNoEvent(criterion);
        setChanged(true);
        myAction = "criteriaListChange";
        fireChangeEvent();
        myAction = null;
    }

    /**
     * Removes a group.
     *
     * @param group the group
     */
    public void removeGroup(GroupModel group)
    {
        removeGroupNoEvent(group);
        setChanged(true);
        fireChangeEvent();
    }

    /**
     * Sets the filter.
     *
     * @param filter the filter
     */
    public void setFilter(Filter filter)
    {
        myFilter = filter;
    }

    /**
     * Sets the parent.
     *
     * @param parent the parent
     */
    public void setParent(GroupModel parent)
    {
        myParent = parent;
    }

    @Override
    protected void updateDomainModel(Group domainModel)
    {
        domainModel.setLogicOperator(myOperator.get());

        domainModel.getStdGroups().clear();
        for (GroupModel groupModel : myGroups)
        {
            groupModel.applyChanges();
            domainModel.getStdGroups().add(groupModel.get());
        }

        domainModel.getCriteria().clear();
        domainModel.getCommonFieldGroups().clear();
        for (CriterionModel criterionModel : myCriteria)
        {
            criterionModel.applyChanges();

            SpecialCriterionConverter groupConverter = null;
            for (SpecialCriterionConverter converter : SPECIAL_CONVERTERS)
            {
                if (criterionModel.getOperator().get() == converter.getOperator())
                {
                    groupConverter = converter;
                    break;
                }
            }

            if (groupConverter != null)
            {
                domainModel.getCommonFieldGroups().add(groupConverter.getGroup(criterionModel.get()));
            }
            else
            {
                domainModel.getCriteria().add(criterionModel.get());
            }
        }
    }

    @Override
    protected void updateViewModel(Group domainModel)
    {
        myOperator.set(domainModel.getLogicOperator());

        for (GroupModel group : myGroups)
        {
            removeModel(group);
        }
        myGroups.clear();
        for (Group group : domainModel.getStdGroups())
        {
            GroupModel groupModel = new GroupModel(myDataType);
            groupModel.setFilter(myFilter);
            groupModel.set(group);
            addGroupNoEvent(groupModel);
        }

        for (CriterionModel criterion : myCriteria)
        {
            removeModel(criterion);
        }
        myCriteria.clear();
        for (Criteria criterion : domainModel.getCriteria())
        {
            CriterionModel criterionModel = new CriterionModel(myFilter, myDataType);
            criterionModel.set(criterion);
            addCriterionNoEvent(criterionModel);
        }
        for (CommonFieldGroup group : domainModel.getCommonFieldGroups())
        {
            SpecialCriterionConverter converter = getAcceptingConverter(group);
            if (converter != null)
            {
                CriterionModel criterionModel = new CriterionModel(myFilter, myDataType);
                criterionModel.set(converter.getCriteria(group));
                addCriterionNoEvent(criterionModel);
            }
        }
    }

    /**
     * Adds a criterion, without firing an event or setting changed.
     *
     * @param criterion the criterion
     */
    private void addCriterionNoEvent(CriterionModel criterion)
    {
        myCriteria.add(criterion);
        criterion.setParent(this);
        addModel(criterion);
    }

    /**
     * Adds a group, without firing an event or setting changed.
     *
     * @param group the group
     */
    private void addGroupNoEvent(GroupModel group)
    {
        myGroups.add(group);
        group.setParent(this);
        addModel(group);
    }

    /**
     * Removes a criterion, without firing an event or setting changed.
     *
     * @param criterion the criterion
     */
    private void removeCriterionNoEvent(CriterionModel criterion)
    {
        myCriteria.remove(criterion);
        criterion.setParent(null);
        removeModel(criterion);
    }

    /**
     * Removes a group, without firing an event or setting changed.
     *
     * @param group the group
     */
    private void removeGroupNoEvent(GroupModel group)
    {
        myGroups.remove(group);
        group.setParent(null);
        removeModel(group);
    }
}
