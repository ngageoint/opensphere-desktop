package io.opensphere.featureactions.model;

import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import io.opensphere.core.util.lang.ToStringHelper;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.mantle.data.element.mdfilter.impl.DataFilterEvaluator;

/** The model for a feature action. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({ StyleAction.class, LabelAction.class, CustomColumnAction.class })
public class FeatureAction
{
    /**
     * The action to perform on a feature that meets the filter criteria.
     */
    @XmlElement(name = "actions")
    private final ObservableList<Action> myActions = FXCollections.observableArrayList();

    /**
     * Indicates if this action is enabled or not.
     */
    private final BooleanProperty myEnabled = new SimpleBooleanProperty(true);

    /**
     * The filter of the feature action.
     */
    private final ObjectProperty<Filter> myFilter = new SimpleObjectProperty<>();

    /**
     * The group this feature action belongs to.
     */
    private final StringProperty myGroupName = new ConcurrentStringProperty();

    /**
     * The id of the action.
     */
    private String myId = UUID.randomUUID().toString();

    /**
     * The name of the feature action.
     */
    private final StringProperty myName = new SimpleStringProperty();

    /** Whether the action should be visible in the UI. */
    @XmlAttribute(name = "visible")
    private volatile boolean myVisible = true;

    /** The filter evaluator (stored here for performance). */
    private DataFilterEvaluator myEvaluator;

    /**
     * The enabled property.
     *
     * @return The property for the enabled flag.
     */
    public BooleanProperty enabledProperty()
    {
        return myEnabled;
    }

    /**
     * Gets the filter property.
     *
     * @return The filter of the feature action.
     */
    public ObjectProperty<Filter> filterProperty()
    {
        return myFilter;
    }

    /**
     * Gets the action to perform on a feature that meets the filter criteria.
     *
     * @return the action.
     */
    public ObservableList<Action> getActions()
    {
        return myActions;
    }

    /**
     * Gets the filter of the feature action.
     *
     * @return the filter The filter of the feature action.
     */
    @XmlElement(name = "filter")
    public Filter getFilter()
    {
        return myFilter.get();
    }

    /**
     * Gets the group this feature action belongs to.
     *
     * @return The group name.
     */
    @XmlAttribute(name = "groupName")
    public String getGroupName()
    {
        return myGroupName.get();
    }

    /**
     * Gets the id of the action.
     *
     * @return The action id.
     */
    @XmlAttribute(name = "id")
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the name of the feature action.
     *
     * @return The name.
     */
    @XmlAttribute(name = "name")
    public String getName()
    {
        return myName.get();
    }

    /**
     * Gets the group this feature action belongs to.
     *
     * @return The group name property.
     */
    public StringProperty groupNameProperty()
    {
        return myGroupName;
    }

    /**
     * Indicates if this action is enabled or not.
     *
     * @return True if its on, false is it off.
     */
    @XmlAttribute(name = "enabled")
    public boolean isEnabled()
    {
        return myEnabled.get();
    }

    /**
     * Gets the name of this feature action.
     *
     * @return The name property.
     */
    public StringProperty nameProperty()
    {
        return myName;
    }

    /**
     * Sets if this feature is enabled or not.
     *
     * @param enabled True if its on, false is it off.
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled.set(enabled);
    }

    /**
     * Sets the filter of the feature action.
     *
     * @param filter the filter to set.
     */
    public void setFilter(Filter filter)
    {
        myFilter.set(filter);
        myEvaluator = null;
    }

    /**
     * Sets the group this feature action belongs to.
     *
     * @param groupName The name of the group.
     */
    public void setGroupName(String groupName)
    {
        myGroupName.set(groupName);
    }

    /**
     * Sets the id of the action.
     *
     * @param id Teh action id.
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Sets the name of the feature action.
     *
     * @param name The name.
     */
    public void setName(String name)
    {
        myName.set(name);
    }

    /**
     * Gets the visible.
     *
     * @return the visible
     */
    public boolean isVisible()
    {
        return myVisible;
    }

    /**
     * Sets the visible.
     *
     * @param visible the visible
     */
    public void setVisible(boolean visible)
    {
        myVisible = visible;
    }

    /**
     * Gets the evaluator.
     *
     * @return the evaluator
     */
    public DataFilterEvaluator getEvaluator()
    {
        return myEvaluator;
    }

    /**
     * Sets the evaluator.
     *
     * @param evaluator the evaluator
     */
    public void setEvaluator(DataFilterEvaluator evaluator)
    {
        myEvaluator = evaluator;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("filter", getFilter().getSqlLikeString());
        helper.add("actions", myActions);
        return helper.toString();
    }
}
