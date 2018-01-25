package io.opensphere.core.control.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.AutosizePanel;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.DefaultAction;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;

/**
 * This class handles arranging and adding tool bar components to the tool bars.
 */
@SuppressWarnings("PMD.GodClass")
public class ToolbarManager
{
    /** Style constant for use with {@link GridBagPanel}. */
    private static final String BASIC_STYLE = "basic";

    /** Style constant for use with {@link GridBagPanel}. */
    private static final String SEPARATOR_STYLE = "separatorStyle";

    /** Preference key for the user preference to show the icon button text. */
    private static final String SHOW_ICON_BUTTON_TEXT_PREF_KEY = "showIconButtonText";

    /** Listener for right-click on icon buttons that can have text. */
    private final MouseListener myIconButtonMouseListener = new MouseAdapter()
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (e.getButton() == MouseEvent.BUTTON3)
            {
                JPopupMenu popup = new JPopupMenu();
                JCheckBoxMenuItem mi = new JCheckBoxMenuItem("Show text", myShowIconButtonText);
                mi.addActionListener(ev ->
                {
                    myShowIconButtonText ^= true;
                    myPreferences.putBoolean(SHOW_ICON_BUTTON_TEXT_PREF_KEY, myShowIconButtonText, this);
                    myIconButtons.forEach(b -> b.setTextPainted(myShowIconButtonText));
                });
                popup.add(mi);

                if (myIconButtons.stream().anyMatch(b -> !b.isVisible()))
                {
                    popup.add(new JMenuItem(
                            new DefaultAction("Show all buttons", e1 -> myIconButtons.forEach(b -> setVisible(b, true)))));
                }
                JMenu visMenu = new JMenu("Show buttons");
                for (IconButton btn : myIconButtons)
                {
                    visMenu.add(new JCheckBoxMenuItem(
                            new DefaultAction(getText(btn), btn.isVisible(), ev -> setVisible(btn, !btn.isVisible()))));
                }
                popup.add(visMenu);
                popup.show((Component)e.getSource(), e.getX(), e.getY());
                e.consume();
            }
        }
    };

    /** Icon buttons that have been added to the toolbar and can have text. */
    private final Collection<IconButton> myIconButtons = New.collection();

    /** The North bottom toolbar. */
    private GridBagPanel myNorthBottomToolbar;

    /** The North bottom toolbar components. */
    private final List<ToolbarComponent> myNorthBottomToolbarComponents = New.list();

    /** The North tool bar. */
    private JPanel myNorthToolbar;

    /** Map of tool bar component names to the tool bar component. */
    private final List<ToolbarComponent> myNorthToolbarComponents = New.list();

    /** The preferences. */
    private final Preferences myPreferences;

    /** Flag indicating if the text should be shown on the icon buttons. */
    private boolean myShowIconButtonText;

    /** The South tool bar. */
    private GridBagPanel mySouthToolbar;

    /** Map of tool bar component names to the tool bar component. */
    private final List<ToolbarComponent> mySouthToolbarComponents = New.list();

    /**
     * Instantiates a new toolbar manager.
     *
     * @param preferencesRegistry The preferences registry.
     */
    public ToolbarManager(PreferencesRegistry preferencesRegistry)
    {
        myPreferences = preferencesRegistry.getPreferences(ToolbarManager.class);
        myShowIconButtonText = myPreferences.getBoolean(SHOW_ICON_BUTTON_TEXT_PREF_KEY, false);
    }

    /**
     * Get the text from an icon button.
     *
     * @param button The button.
     * @return The text.
     */
    protected String getText(IconButton button)
    {
        String text = button.getText();
        if (text == null)
        {
            button.setTextPainted(true);
            text = button.getText();
            button.setTextPainted(false);
        }
        return text;
    }

    /**
     * Adds a component to a toolbar.
     *
     * @param loc The toolbar location.
     * @param componentName The component name, which may be used to remove the
     *            component later.
     * @param comp The component.
     * @param order Used to order components within the same toolbar.
     * @param separatorLocation The separator location.
     * @param insets Optional insets to be used when laying out the component in
     *            the toolbar.
     */
    public void addToolbarComponent(ToolbarLocation loc, String componentName, JComponent comp, int order,
            SeparatorLocation separatorLocation, Insets insets)
    {
        switch (loc)
        {
            case NORTH:
                addNorthToolbarComponent(componentName, comp, order, separatorLocation, insets);
                getNorthToolbar().revalidate();
                getNorthToolbar().repaint();
                break;

            case NORTH_BOTTOM:
                addNorthBottomToolbarComponent(componentName, comp, order, separatorLocation, insets);
                getNorthBottomToolbar().revalidate();
                getNorthBottomToolbar().repaint();
                break;

            case SOUTH:
                addSouthToolbarComponent(componentName, comp, order, separatorLocation, insets);
                getSouthToolbar().revalidate();
                getSouthToolbar().repaint();
                break;

            default:
                break;
        }
    }

    /**
     * Gets the north bottom toolbar.
     *
     * @return the north bottom toolbar
     */
    public GridBagPanel getNorthBottomToolbar()
    {
        if (myNorthBottomToolbar == null)
        {
            myNorthBottomToolbar = new GridBagPanel();
            myNorthBottomToolbar.setBorder(BorderFactory.createEtchedBorder());
            myNorthBottomToolbar.addInvalidationListener(() -> EventQueue
                    .invokeLater(() -> myNorthBottomToolbar.setVisible(Arrays.asList(myNorthBottomToolbar.getComponents())
                            .stream().anyMatch(c -> c.isVisible() && c.getWidth() > 0 && c.getHeight() > 0))));
        }
        return myNorthBottomToolbar;
    }

    /**
     * Gets the north toolbar.
     *
     * @return the north toolbar
     */
    public JPanel getNorthToolbar()
    {
        if (myNorthToolbar == null)
        {
            myNorthToolbar = new JPanel(new BorderLayout());
            myNorthToolbar.setBorder(null);
        }
        return myNorthToolbar;
    }

    /**
     * Gets the south toolbar.
     *
     * @return the south toolbarPa
     */
    public GridBagPanel getSouthToolbar()
    {
        if (mySouthToolbar == null)
        {
            mySouthToolbar = new GridBagPanel();
            mySouthToolbar.setBorder(BorderFactory.createEtchedBorder());
        }
        return mySouthToolbar;
    }

    /**
     * Removes a component from a toolbar.
     *
     * @param loc the loc
     * @param componentName the component name
     */
    public void removeToolbarComponent(ToolbarLocation loc, String componentName)
    {
        ToolbarComponent comp = null;
        switch (loc)
        {
            case NORTH:
                for (int i = myNorthToolbarComponents.size() - 1; i >= 0; i--)
                {
                    comp = myNorthToolbarComponents.get(i);
                    if (comp.getName().equals(componentName))
                    {
                        myNorthToolbarComponents.remove(comp);
                        getNorthToolbar().remove(comp.getComponent());
                        getNorthToolbar().repaint();
                        deregisterIconButtons(comp.getComponent());
                        break;
                    }
                }
                break;

            case NORTH_BOTTOM:
                for (int i = myNorthBottomToolbarComponents.size() - 1; i >= 0; i--)
                {
                    comp = myNorthBottomToolbarComponents.get(i);
                    if (comp.getName().equals(componentName))
                    {
                        myNorthBottomToolbarComponents.remove(comp);
                        getNorthBottomToolbar().remove(comp.getComponent());
                        getNorthBottomToolbar().repaint();
                        deregisterIconButtons(comp.getComponent());
                        break;
                    }
                }
                break;

            case SOUTH:
                for (int i = mySouthToolbarComponents.size() - 1; i >= 0; i--)
                {
                    comp = mySouthToolbarComponents.get(i);
                    if (comp.getName().equals(componentName))
                    {
                        mySouthToolbarComponents.remove(comp);
                        getSouthToolbar().remove(comp.getComponent());
                        getSouthToolbar().repaint();
                        deregisterIconButtons(comp.getComponent());
                        break;
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * Adds the component.
     *
     * @param toolbar the toolbar
     * @param tbc the tbc
     */
    private void addComponent(GridBagPanel toolbar, ToolbarComponent tbc)
    {
        toolbar.style(SEPARATOR_STYLE).setInsets(1, 3, 0, 3).fillVertical();
        toolbar.style(BASIC_STYLE).fillNone();
        Insets insets = tbc.getInsets();
        if (insets == null)
        {
            toolbar.style(BASIC_STYLE).setInsets(0, 0, 0, 0);
        }
        else
        {
            toolbar.style(BASIC_STYLE).setInsets(insets);
        }

        switch (tbc.getSeparatorLocation())
        {
            case LEFT:
                toolbar.style(SEPARATOR_STYLE).add(getSeparator());
                toolbar.incrementGridx();
                toolbar.style(BASIC_STYLE).add(tbc.getComponent());
                break;

            case RIGHT:
                toolbar.add(tbc.getComponent());
                toolbar.incrementGridx();
                toolbar.style(SEPARATOR_STYLE).add(getSeparator());
                toolbar.style(BASIC_STYLE);
                break;

            case BOTH:
                toolbar.style(SEPARATOR_STYLE).add(getSeparator());
                toolbar.incrementGridx();
                toolbar.add(tbc.getComponent());
                toolbar.incrementGridx();
                toolbar.style(SEPARATOR_STYLE).add(getSeparator());
                toolbar.style(BASIC_STYLE);
                break;

            case NONE:
                toolbar.add(tbc.getComponent());
                break;

            default:
                break;
        }
    }

    /**
     * Adds the component.
     *
     * @param toolbar the toolbar
     * @param tbc the tbc
     */
    private void addComponent(JPanel toolbar, ToolbarComponent tbc)
    {
        switch (tbc.getSeparatorLocation())
        {
            case LEFT:
                toolbar.add(getSeparator());
                toolbar.add(tbc.getComponent());
                break;

            case RIGHT:
                toolbar.add(tbc.getComponent());
                toolbar.add(getSeparator());
                break;

            case BOTH:
                toolbar.add(getSeparator());
                toolbar.add(tbc.getComponent());
                toolbar.add(getSeparator());
                break;

            case NONE:
                toolbar.add(tbc.getComponent());
                break;

            default:
                break;
        }
    }

    /**
     * Adds the north bottom toolbar component.
     *
     * @param componentName the component name
     * @param comp the comp
     * @param relativeLoc the relative loc
     * @param separatorLocation the separator location
     * @param insets Optional insets to be used when laying out the component in
     *            the toolbar.
     */
    private void addNorthBottomToolbarComponent(String componentName, JComponent comp, int relativeLoc,
            SeparatorLocation separatorLocation, Insets insets)
    {
        myNorthBottomToolbarComponents
                .add(new ToolbarComponent(getNorthBottomToolbar(), componentName, comp, relativeLoc, separatorLocation, insets));
        registerIconButtons(comp);
        Collections.sort(myNorthBottomToolbarComponents);

        getNorthBottomToolbar().setGridx(0);
        getNorthBottomToolbar().removeAll();

        for (ToolbarComponent tbc : myNorthBottomToolbarComponents)
        {
            getNorthBottomToolbar().fillNone().setInsets(0, 0, 0, 0);

            if (tbc.getName().equals("TimeBrowser"))
            {
//                getNorthBottomToolbar().fillHorizontalSpace();
//                getNorthBottomToolbar().incrementGridx();
                addComponent(getNorthBottomToolbar(), tbc);
                getNorthBottomToolbar().incrementGridx();
                getNorthBottomToolbar().fillHorizontalSpace();
                getNorthBottomToolbar().incrementGridx();
            }
            else
            {
                addComponent(getNorthToolbar(), tbc);
                getNorthBottomToolbar().incrementGridx();
            }
        }
    }

    /**
     * Adds the north toolbar component.
     *
     * @param componentName The component name, which may be used to remove the
     *            component later.
     * @param comp The component.
     * @param order Used to order components within the same toolbar.
     * @param separatorLocation The separator location.
     * @param insets Optional insets to be used when laying out the component in
     *            the toolbar.
     */
    private void addNorthToolbarComponent(String componentName, JComponent comp, int order, SeparatorLocation separatorLocation,
            Insets insets)
    {
        myNorthToolbarComponents
                .add(new ToolbarComponent(getNorthToolbar(), componentName, comp, order, separatorLocation, insets));
        registerIconButtons(comp);
        Collections.sort(myNorthToolbarComponents);
        getNorthToolbar().removeAll();

        FlowLayout layout = new FlowLayout();
        layout.setVgap(2);
        layout.setAlignment(FlowLayout.LEFT);
        JPanel centerPanel = new AutosizePanel(layout);
        getNorthToolbar().add(centerPanel);
        JPanel eastPanel = new JPanel();
        ((FlowLayout)eastPanel.getLayout()).setVgap(2);
        getNorthToolbar().add(eastPanel, BorderLayout.EAST);

        JPanel panel = centerPanel;
        for (ToolbarComponent tbc : myNorthToolbarComponents)
        {
            if (tbc.getName().equals("SearchGoto"))
            {
                panel = eastPanel;
            }
            addComponent(panel, tbc);
        }
    }

    /**
     * Adds the south toolbar component.
     *
     * @param componentName the component name
     * @param comp the comp
     * @param relativeLoc the relative loc
     * @param separatorLocation the separator location
     * @param insets Optional insets to be used when laying out the component in
     *            the toolbar.
     */
    private void addSouthToolbarComponent(final String componentName, final JComponent comp, final int relativeLoc,
            final SeparatorLocation separatorLocation, Insets insets)
    {
        mySouthToolbarComponents
                .add(new ToolbarComponent(getSouthToolbar(), componentName, comp, relativeLoc, separatorLocation, insets));
        registerIconButtons(comp);
        Collections.sort(mySouthToolbarComponents);

        getSouthToolbar().setGridx(0);
        getSouthToolbar().removeAll();

        for (ToolbarComponent tbc : mySouthToolbarComponents)
        {
            getSouthToolbar().setFill(GridBagConstraints.NONE).setWeighty(0).setInsets(1, 0, 0, 0);
            if (tbc.getName().equals("ViewerPosition"))
            {
                getSouthToolbar().fillHorizontalSpace();
                getSouthToolbar().incrementGridx();
                addComponent(getSouthToolbar(), tbc);
                getSouthToolbar().incrementGridx();
                getSouthToolbar().fillHorizontalSpace();
                getSouthToolbar().incrementGridx();
            }
            else
            {
                addComponent(getSouthToolbar(), tbc);
                getSouthToolbar().incrementGridx();
            }
        }
    }

    /**
     * Remove the icon buttons that are children of the input component from my
     * collection.
     *
     * @param component The component.
     */
    private void deregisterIconButtons(JComponent component)
    {
        Collection<Component> buttons = ComponentUtilities.getAllComponents(component, c -> c instanceof IconButton);
        if (!buttons.isEmpty())
        {
            myIconButtons.removeAll(New.set(buttons));
        }
    }

    /**
     * Gets the separator.
     *
     * @return the separator
     */
    private JSeparator getSeparator()
    {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(3, 1));
        return sep;
    }

    /**
     * Add the icon buttons that are children of the input component to my
     * collection, if they have text.
     *
     * @param component The component.
     */
    private void registerIconButtons(JComponent component)
    {
        for (Component button : ComponentUtilities.getAllComponents(component, c -> c instanceof IconButton))
        {
            IconButton btn = (IconButton)button;
            if (StringUtils.isNotEmpty(btn.getText()))
            {
                btn.setVisible(myPreferences.getBoolean(btn.getText(), true));
                btn.setTextPainted(myShowIconButtonText);
                btn.addMouseListener(myIconButtonMouseListener);
                myIconButtons.add(btn);
            }
        }
    }

    /**
     * Set a button visible.
     *
     * @param button The button.
     * @param vis If the button is visible.
     */
    public void setVisible(IconButton button, boolean vis)
    {
        button.setVisible(vis);
        myPreferences.putBoolean(getText(button), vis, this);
    }

    /**
     * The Enum SeparatorLocation.
     */
    public enum SeparatorLocation
    {
        /** The BOTH. */
        BOTH,

        /** The LEFT. */
        LEFT,

        /** The NONE. */
        NONE,

        /** The RIGHT. */
        RIGHT;
    }

    /**
     * The Enum ToolbarLocation.
     */
    public enum ToolbarLocation
    {
        /** North. */
        NORTH,

        /** North bottom. */
        NORTH_BOTTOM,

        /** South. */
        SOUTH;
    }
}
