package io.opensphere.featureactions.editor.controller;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.paint.Color;

import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.controlpanels.styles.model.Styles;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.StyleAction;

/**
 * Adapts the style values from the {@link SimpleFeatureAction} to the
 * {@link FeatureAction} and keeps those two models in sync.
 */
public class StyleActionAdapter implements ListChangeListener<Action>, Observer
{
    /** The {@link SimpleFeatureAction} to keep in sync. */
    private final SimpleFeatureAction myAction;

    /** Handles color changes from the {@link SimpleFeatureAction}. */
    private final ChangeListener<Color> myColorListener = (o, v0, v1) -> fromSimpleColor();

    /** Handles icon changes from the {@link SimpleFeatureAction}. */
    private final ChangeListener<Number> myIconListener = (o, v0, v1) -> fromSimpleIcon();

    /** The styles that we are currently observing and keeping in sync. */
    private final List<StyleAction> myObservedStyles = New.list();

    /**
     * Constructs a new adapter.
     *
     * @param action The action to adapt.
     */
    public StyleActionAdapter(SimpleFeatureAction action)
    {
        myAction = action;
        toSimpleColor();
        toSimpleIcon();
        myAction.iconIdProperty().addListener(myIconListener);
        myAction.colorProperty().addListener(myColorListener);
        myAction.getFeatureAction().getActions().addListener(this);
        registerStyleActionChanges();
    }

    /** Stops listening for changes in all models. */
    public void close()
    {
        myAction.iconIdProperty().removeListener(myIconListener);
        myAction.colorProperty().removeListener(myColorListener);
        myAction.getFeatureAction().getActions().removeListener(this);
        unregisterStyleActionChanges();
    }

    @Override
    public void onChanged(Change<? extends Action> c)
    {
        while (c.next())
        {
            if (c.wasRemoved())
            {
                for (Action action : c.getRemoved())
                {
                    if (action instanceof StyleAction)
                    {
                        unregisterStyleActionChanges();
                        myAction.setColor(null);
                        myAction.setIconId(-1);
                    }
                }
            }
            else
            {
                registerStyleActionChanges();
            }
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (StyleOptions.COLOR_PROP.equals(arg))
        {
            toSimpleColor();
        }
        else if (StyleOptions.ICON_PROP.equals(arg))
        {
            toSimpleIcon();
        }
    }

    /**
     * Takes the color value from the simple feature action and applies them to
     * the feature action.
     */
    private void fromSimpleColor()
    {
        StyleOptions styleOptions = getStyleOptions();
        if (styleOptions != null)
        {
            styleOptions.setColor(FXUtilities.toAwtColor(myAction.getColor()));
        }
    }

    /**
     * Takes the icon value from the simple feature action and applies them to
     * the feature action.
     */
    private void fromSimpleIcon()
    {
        StyleOptions styleOptions = getStyleOptions();
        if (styleOptions == null)
        {
            return;
        }
        styleOptions.setIconId(myAction.getIconId());
        styleOptions.setStyle(Styles.ICON);
        styleOptions.setSize(16);
    }

    /**
     * Gets the style options from the style action if it finds one.
     *
     * @return The style options contained in the {@link FeatureAction}.
     */
    private StyleOptions getStyleOptions()
    {
        return myAction.getFeatureAction().getActions().stream().filter(a -> a instanceof StyleAction)
                .map(a -> ((StyleAction)a).getStyleOptions()).findAny().orElse(null);
    }

    /** Registers for any {@link StyleOptions} changes. */
    private void registerStyleActionChanges()
    {
        for (Action action : myAction.getFeatureAction().getActions())
        {
            if (action instanceof StyleAction && !myObservedStyles.contains(action))
            {
                StyleAction style = (StyleAction)action;
                style.getStyleOptions().addObserver(this);
                myObservedStyles.add(style);
            }
        }
    }

    /**
     * Takes the color value from the feature action and applies them to the
     * simple feature action.
     */
    private void toSimpleColor()
    {
        for (Action action : myAction.getFeatureAction().getActions())
        {
            if (action instanceof StyleAction)
            {
                myAction.setColor(FXUtilities.fromAwtColor(((StyleAction)action).getStyleOptions().getColor()));
                break;
            }
        }
    }

    /**
     * Takes the icon values from the feature action and applies them to the
     * simple feature action.
     */
    private void toSimpleIcon()
    {
        for (Action action : myAction.getFeatureAction().getActions())
        {
            if (action instanceof StyleAction)
            {
                myAction.setIconId(((StyleAction)action).getStyleOptions().getIconId());
                break;
            }
        }
    }

    /** Unregisters for {@link StyleOptions} changes. */
    private void unregisterStyleActionChanges()
    {
        for (StyleAction style : myObservedStyles)
        {
            style.getStyleOptions().deleteObserver(this);
        }
        myObservedStyles.clear();
    }
}
