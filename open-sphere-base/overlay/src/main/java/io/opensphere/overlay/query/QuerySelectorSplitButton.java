package io.opensphere.overlay.query;

import java.util.Map;

import javax.swing.JMenuItem;

import io.opensphere.core.util.AwesomeIconRegular;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.SelectionMode;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.SplitButton;
import io.opensphere.overlay.OverlayToolbox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

/**
 * A button in which the available query geometries and options are defined. The
 * various options are available in a pulldown submenu, and selection of an item
 * changes the default behavior of the button. Pressing the button initiates a
 * query of the currently selected type.
 */
public class QuerySelectorSplitButton extends SplitButton
{
    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = 6753950820522867830L;

    /** The property in which the current selection mode is defined. */
    private final ObjectProperty<SelectionMode> myCurrentSelectionMode;

    /** The property in which the toggle state is maintained. */
    private final BooleanProperty myToggledProperty;

    /** A lookup table of icons and metadata. */
    private static final Map<SelectionMode, SelectionModeQueryActionDefinition> myModeIcons = Map.of(SelectionMode.BOUNDING_BOX,
            new SelectionModeQueryActionDefinition("Box", AwesomeIconRegular.SQUARE, SelectionMode.BOUNDING_BOX),
            SelectionMode.CIRCLE,
            new SelectionModeQueryActionDefinition("Circle", AwesomeIconRegular.CIRCLE, SelectionMode.CIRCLE),
            SelectionMode.POLYGON,
            new SelectionModeQueryActionDefinition("Polygon", AwesomeIconRegular.STAR, SelectionMode.POLYGON), SelectionMode.LINE,
            new SelectionModeQueryActionDefinition("Line", AwesomeIconSolid.LONG_ARROW_ALT_RIGHT, SelectionMode.LINE));

    /**
     * A dictionary of additional query actions, mapped to their corresponding
     * menu items. The use of these menu items will not change the value of the
     * {@link #myCurrentSelectionMode} property, but instead trigger the action
     * stored in the {@link QueryActionDefinition}.
     */
    private final Map<QueryActionDefinition, JMenuItem> myQueryActions;

    /**
     * Creates a new button, bound to the supplied overlay toolbox. The overlay
     * toolbox is used to retrieve the addition query actions. By way of the
     * {@link QueryActionManager}, the button binds itself to the manager's
     * {@link ObservableList} of {@link QueryActionDefinition}s, and upon
     * change, new actions are immediately added, and obsolete actions are
     * immediately removed. This allows for external plugins to contribute
     * additional query actions without introducing a direct external
     * dependency.
     *
     * @param toolbox the overlay toolbox from which the
     *            {@link QueryActionManager} is accessed.
     */
    public QuerySelectorSplitButton(final OverlayToolbox toolbox)
    {
        super(null, new GenericFontIcon(AwesomeIconRegular.SQUARE, IconUtil.DEFAULT_ICON_FOREGROUND), true);
        setRolloverIcon(new GenericFontIcon(AwesomeIconRegular.SQUARE, IconUtil.DEFAULT_ICON_ROLLOVER));

        myQueryActions = New.map();
        final QueryActionManager queryActionManager = toolbox.getQueryActionManager();
        queryActionManager.getQueryActions().addListener((final Change<? extends QueryActionDefinition> c) ->
        {
            while (c.next())
            {
                c.getAddedSubList().stream().forEach(d -> addQueryAction(d));
                c.getRemoved().forEach(d -> removeQueryAction(d));
            }
        });

        myCurrentSelectionMode = new ConcurrentObjectProperty<>(SelectionMode.BOUNDING_BOX);
        myCurrentSelectionMode.addListener((obs, ov, nv) ->

        {
            setIcon(myModeIcons.get(nv).iconProperty().get());
            setRolloverIcon(myModeIcons.get(nv).rolloverIconProperty().get());
        });

        myToggledProperty = new ConcurrentBooleanProperty(false);
        myToggledProperty.addListener((obs, ov, nv) ->
        {
            super.setSelected(nv);
            if (nv)
            {
                setIcon(myModeIcons.get(myCurrentSelectionMode.get()).selectedIconProperty().get());
                setRolloverIcon(myModeIcons.get(myCurrentSelectionMode.get()).rolloverIconProperty().get());
            }
            else
            {
                setIcon(myModeIcons.get(myCurrentSelectionMode.get()).iconProperty().get());
                setRolloverIcon(myModeIcons.get(myCurrentSelectionMode.get()).rolloverIconProperty().get());
            }
        });

        createIconModifyingMenuItem(myModeIcons.get(SelectionMode.BOUNDING_BOX));
        createIconModifyingMenuItem(myModeIcons.get(SelectionMode.CIRCLE));
        createIconModifyingMenuItem(myModeIcons.get(SelectionMode.POLYGON));
        createIconModifyingMenuItem(myModeIcons.get(SelectionMode.LINE));
    }

    /**
     * Gets the value of the {@link #myCurrentSelectionMode} field.
     *
     * @return the value stored in the {@link #myCurrentSelectionMode} field.
     */
    public ObjectProperty<SelectionMode> currentSelectionModeProperty()
    {
        return myCurrentSelectionMode;
    }

    /**
     * Gets the value of the {@link #myToggledProperty} field.
     *
     * @return the value stored in the {@link #myToggledProperty} field.
     */
    public BooleanProperty toggledProperty()
    {
        return myToggledProperty;
    }

    /**
     * Creates a new menu item that can modify the current selection mode
     * property.
     *
     * @param metadata the metadata defining the selection mode.
     */
    private void createIconModifyingMenuItem(final SelectionModeQueryActionDefinition metadata)
    {
        final JMenuItem item = new JMenuItem(metadata.labelProperty().get());
        item.setIcon(metadata.iconProperty().get());
        item.setSelectedIcon(metadata.selectedIconProperty().get());
        item.setRolloverIcon(metadata.rolloverIconProperty().get());
        item.addActionListener(e ->
        {
            setIcon(metadata.selectedIconProperty().get());
            setRolloverIcon(metadata.rolloverIconProperty().get());
            currentSelectionModeProperty().set(metadata.getMode());
            fireActionPerformed(e);
        });

        addMenuItem(item);
    }

    /**
     * Adds a new query action using the supplied definition. Note that query
     * actions added in this way will not modify the current selection mode, and
     * the action taken must be defined in the supplied definition. This method
     * has a side-effect of adding a separator to the menu if this is the first
     * query action to be added.
     *
     * @param definition the definition of the query action to add.
     */
    private void addQueryAction(final QueryActionDefinition definition)
    {
        if (myQueryActions.isEmpty())
        {
            addSeparator();
        }

        final JMenuItem item = new JMenuItem(definition.labelProperty().get(), definition.iconProperty().get());
        if (definition.selectedIconProperty().get() != null)
        {
            item.setSelectedIcon(definition.selectedIconProperty().get());
        }
        item.setRolloverIcon(definition.rolloverIconProperty().get());

        item.addActionListener(e -> definition.getEventListener().accept(new QueryEvent(item)));
        myQueryActions.put(definition, item);
        addMenuItem(item);
    }

    /**
     * Removes the supplied query action from the button.
     *
     * @param definition the definition of the action to remove.
     */
    private void removeQueryAction(final AbstractQueryActionDefinition definition)
    {
        if (myQueryActions.containsKey(definition))
        {
            remove(myQueryActions.remove(definition));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.AbstractButton#setSelected(boolean)
     */
    @Override
    public void setSelected(final boolean b)
    {
        // intercepts the super implementation to bind behavior to the toggled
        // property.
        toggledProperty().set(b);
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.AbstractButton#isSelected()
     */
    @Override
    public boolean isSelected()
    {
        return toggledProperty().get();
    }
}
