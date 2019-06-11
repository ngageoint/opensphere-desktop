package io.opensphere.featureactions.editor.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.featureactions.editor.model.CriteriaOptions;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.model.SimpleFeatureActions;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.paint.Color;

/**
 * When the user adds a new {@link FeatureAction} this class will attempt to
 * default its values based on previously added actions.
 */
public class FeatureActionsDefaulter
{
    /**
     * Listens for new actions to default their values.
     */
    private final ListChangeListener<SimpleFeatureAction> myActionListener = this::actionChanged;

    /**
     * The model being edited.
     */
    private final SimpleFeatureActions myActions;

    /**
     * Listens for groups to be removed or added.
     */
    private final ListChangeListener<SimpleFeatureActionGroup> myGroupListener = this::groupChanged;

    /**
     * Constructs a new defaulter.
     *
     * @param actions The model being edited containing all feature actions.
     */
    public FeatureActionsDefaulter(SimpleFeatureActions actions)
    {
        myActions = actions;
        myActions.getFeatureGroups().addListener(myGroupListener);
        for (SimpleFeatureActionGroup group : myActions.getFeatureGroups())
        {
            group.getActions().addListener(myActionListener);
        }
    }

    /**
     * Stops listening for new actions.
     */
    public void close()
    {
        myActions.getFeatureGroups().removeListener(myGroupListener);
        for (SimpleFeatureActionGroup group : myActions.getFeatureGroups())
        {
            group.getActions().removeListener(myActionListener);

        }
    }

    /**
     * Listens for {@link SimpleFeatureAction}s to be added so we can fill in
     * some values for the user.
     *
     * @param c The event.
     */
    private void actionChanged(Change<? extends SimpleFeatureAction> c)
    {
        while (c.next())
        {
            for (SimpleFeatureAction action : c.getAddedSubList())
            {
                defaultNewAction(c.getList(), action);
            }
        }
    }

    /**
     * Defaults the values for the action.
     *
     * @param actions The existing actions.
     * @param newAction The new actions.
     */
    private void defaultNewAction(List<? extends SimpleFeatureAction> actions, SimpleFeatureAction newAction)
    {
        if (StringUtils.isEmpty(newAction.getFeatureAction().getName()))
        {
            StringBuilder builder = new StringBuilder();
            builder.append("Action ");

            Color color = Color.RED;
            int highestNumber = 0;
            String column = "";
            CriteriaOptions criteriaOption = CriteriaOptions.VALUE;
            String minimum = "";
            String maximum = "";
            int index = 0;
            for (SimpleFeatureAction action : actions)
            {
                if (!action.equals(newAction))
                {
                    if (!StringUtils.isEmpty(action.getFeatureAction().getName())
                            && action.getFeatureAction().getName().startsWith("Action "))
                    {
                        String actionNumberString = action.getFeatureAction().getName().substring(7);
                        int actionNumber = NumberUtilities.parseInt(actionNumberString, -1);
                        if (actionNumber > highestNumber)
                        {
                            highestNumber = actionNumber;
                        }
                    }

                    for (Action anAction : action.getFeatureAction().getActions())
                    {
                        if (anAction instanceof StyleAction)
                        {
                            StyleAction styleAction = (StyleAction)anAction;
                            if (styleAction.getStyleOptions() != null)
                            {
                                Color aColor = FXUtilities.fromAwtColor(styleAction.getStyleOptions().getColor());
                                color = aColor.deriveColor(20, 1, 1, 1);
                                if (index % 2 == 0)
                                {
                                    color = color.brighter().brighter();
                                }
                                else
                                {
                                    color = color.darker().darker();
                                }
                            }
                        }
                    }

                    column = action.getColumn().get();
                    criteriaOption = action.getOption().get();
                    minimum = action.getMinimumValue().get();
                    maximum = action.getMaximumValue().get();
                }

                index++;
            }

            builder.append(highestNumber + 1);
            if (criteriaOption == CriteriaOptions.RANGE && !StringUtils.isEmpty(maximum) && !StringUtils.isEmpty(minimum))
            {
                double max = NumberUtilities.parseDouble(maximum, Double.MIN_VALUE);
                double min = NumberUtilities.parseDouble(minimum, Double.MIN_VALUE);
                if (Double.compare(max, Double.MIN_VALUE) != 0 && Double.compare(min, Double.MIN_VALUE) != 0)
                {
                    double width = max - min;
                    minimum = String.valueOf(max);
                    maximum = String.valueOf(max + width);
                    newAction.getMinimumValue().set(minimum);
                    newAction.getMaximumValue().set(maximum);
                }
            }

            newAction.getFeatureAction().setName(builder.toString());
            newAction.getColumn().set(column);
            newAction.getOption().set(criteriaOption);
            newAction.setColor(color);
        }
    }

    /**
     * Listens for {@link SimpleFeatureActionGroup} to be added or removed so we
     * can listen for when actions are added.
     *
     * @param c The event.
     */
    private void groupChanged(Change<? extends SimpleFeatureActionGroup> c)
    {
        while (c.next())
        {
            for (SimpleFeatureActionGroup group : c.getAddedSubList())
            {
                group.getActions().addListener(myActionListener);
            }

            for (SimpleFeatureActionGroup group : c.getRemoved())
            {
                group.getActions().removeListener(myActionListener);
            }
        }
    }
}
