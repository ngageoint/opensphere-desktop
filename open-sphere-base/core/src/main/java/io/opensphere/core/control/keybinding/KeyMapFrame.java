package io.opensphere.core.control.keybinding;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.BindingsToListener;
import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * The Class KeyMapFrame. This class will show the control and shortcut keys.
 */
public class KeyMapFrame extends AbstractInternalFrame
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The title of the window. */
    public static final String TITLE = "Key Map";

    /**
     * The Container panel. Since this JInternalFrame can be 'torn off' and uses
     * the JInternalFrame's content pane, set the content pane to a JPanel
     * created in this class.
     */
    private JPanel myMainPanel;

    /** The Tabbed pane. */
    private JTabbedPane myTabbedPane;

    /** The Control key sp. */
    private JScrollPane myControlKeySP;

    /** The Control key panel. */
    private GridBagPanel myControlKeyPanel;

    /** The Shortcut key sp. */
    private JScrollPane myShortcutKeySP;

    /** The Shortcut key panel. */
    private GridBagPanel myShortcutKeyPanel;

    /** The panel that shows the current set of bindings. */
    private GridBagPanel myBindingsSubPanel;

    /** A reference to the control registry. */
    private final ControlRegistry myControlRegistry;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The Button bindings. */
    private final Map<String, ButtonBinding> myButtonBindings;

    /**
     * Instantiates a new key map frame.
     *
     * @param toolbox the toolbox
     */
    public KeyMapFrame(Toolbox toolbox)
    {
        super();
        myToolbox = toolbox;
        myControlRegistry = toolbox.getControlRegistry();
        myButtonBindings = New.map();

        setSize(600, 410);
        setPreferredSize(getSize());
        setMinimumSize(getSize());
        setTitle(TITLE);
        setOpaque(false);
        // TODO It is not clear how minimizing can be done for the HUD. Might
        // need to look into this later
        setIconifiable(false);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setContentPane(getMainPanel());
    }

    /**
     * Add a button for the binding.
     *
     * @param btl The binding to listener.
     * @param rowCounter The row for the button.
     */
    private void addBindingButton(BindingsToListener btl, int rowCounter)
    {
        ButtonBinding bb = null;

        if (!myButtonBindings.containsKey(btl.getListener().getTitle()))
        {
            bb = new ButtonBinding(myToolbox, btl);
            //            bb.addKeyBindingChangeListener(KeyMapFrame.this);
            myButtonBindings.put(btl.getListener().getTitle(), bb);
        }
        else
        {
            bb = myButtonBindings.get(btl.getListener().getTitle());
        }

        GridBagPanel buttonPanel = new GridBagPanel();
        buttonPanel.setGridx(0).setGridy(0).setInsets(0, 50, 0, 0).add(new JLabel(bb.getKeyButton().getText()));
        //        buttonPanel.incrementGridx().setInsets(0, 4, 0, 0).add(bb.getRestoreToDefaultButton());

        getBindingSubPanel().setGridx(1).setInsets(0, 20, 5, 0).setGridy(rowCounter).fillNone().anchorWest().setGridwidth(1)
        .add(buttonPanel);
    }

    /**
     * Add a label for a category.
     *
     * @param category The category.
     * @param rowCounter The row for the label.
     */
    private void addCategoryLabel(String category, int rowCounter)
    {
        JLabel categoryLabel = new JLabel(category + ":");
        categoryLabel.setForeground(Color.ORANGE);

        Insets i = new Insets(0, 10, 0, 0);
        if (rowCounter > 0)
        {
            i = new Insets(20, 10, 0, 0);
        }

        getBindingSubPanel().setGridx(0).setInsets(i).setGridy(rowCounter).setGridwidth(2).fillHorizontal().anchorWest()
        .add(categoryLabel);
    }

    /**
     * Add a label for a listener.
     *
     * @param listener The listener.
     * @param rowCounter The row for the label.
     */
    private void addListenerTitle(BoundEventListener listener, int rowCounter)
    {
        JLabel labelToAdd = new JLabel(listener.getTitle());

        // Set the label to automatically focus the first assignment button when
        // clicked. This allows the user to select a binding to clear or reset,
        // without having to reset the button itself.
        labelToAdd.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent mouseevent)
            {
                super.mouseClicked(mouseevent);
                JLabel source = (JLabel)mouseevent.getSource();
                source.transferFocus();
            }
        });

        // If we don't change the tooltip string into an html string, we lose
        // the ability to do multiline tooltips(!)
        // Also the default html tooltip renderer uses a different font than the
        // regular tooltips, so encoding that inline
        labelToAdd.setToolTipText("<html><font face=\"sansserif\">" + listener.getDescription() + "</font></html>");

        GridBagPanel labelPanel = new GridBagPanel();
        labelPanel.add(labelToAdd);
        labelPanel.setBorder(BorderFactory.createLineBorder(Color.RED));

        getBindingSubPanel().setGridx(0).setGridy(rowCounter).setInsets(0, 100, 5, 0).setGridwidth(1).fillNone().anchorEast()
        .add(labelToAdd);
    }

    /**
     * Gets the binding sub panel.
     *
     * @return the binding sub panel
     */
    private GridBagPanel getBindingSubPanel()
    {
        if (myBindingsSubPanel == null)
        {
            myBindingsSubPanel = new GridBagPanel();
        }
        return myBindingsSubPanel;
    }

    /**
     * Gets the control key panel.
     *
     * @return the control key panel
     */
    private GridBagPanel getControlKeyPanel()
    {
        if (myControlKeyPanel == null)
        {
            myControlKeyPanel = new GridBagPanel();

            GridBagPanel headerPanel = new GridBagPanel();
            headerPanel.setSize(1000, 22);
            headerPanel.setPreferredSize(headerPanel.getSize());
            headerPanel.setGridx(0).setGridy(0).setInsets(3, 20, 3, 0).anchorWest().add(new JLabel("Topic"));
            headerPanel.incrementGridx().setInsets(3, 130, 3, 0).anchorCenter().add(new JLabel("Control"));
            headerPanel.incrementGridx().fillHorizontal().setInsets(3, 140, 3, 0).add(new JLabel("Key"));
            myControlKeyPanel.setGridx(0).add(headerPanel);

            JPanel contentPanel = new JPanel();
            contentPanel.setBorder(new EmptyBorder(5, 7, 6, 7));
            contentPanel.setLayout(new BorderLayout());
            populateBindingSubPanel();
            contentPanel.add(getBindingSubPanel(), BorderLayout.CENTER);
            myControlKeyPanel.setGridy(1).fillBoth().add(contentPanel);
        }
        return myControlKeyPanel;
    }

    /**
     * Gets the control key sp.
     *
     * @return the control key sp
     */
    private JScrollPane getControlKeySP()
    {
        if (myControlKeySP == null)
        {
            myControlKeySP = new JScrollPane(getControlKeyPanel());
            myControlKeySP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        return myControlKeySP;
    }

    /**
     * Gets the main panel.
     *
     * @return the main panel
     */
    private Container getMainPanel()
    {
        if (myMainPanel == null)
        {
            myMainPanel = new JPanel(new BorderLayout());
            myMainPanel.add(getTabPane(), BorderLayout.CENTER);
        }
        return myMainPanel;
    }

    /**
     * Gets the shortcut key header panel.
     *
     * @return the shortcut key header panel
     */
    private GridBagPanel getShortcutKeyHeaderPanel()
    {
        GridBagPanel headerPanel = new GridBagPanel();
        headerPanel.setSize(1000, 22);
        headerPanel.setPreferredSize(headerPanel.getSize());
        headerPanel.setGridx(0).setGridy(0).setInsets(3, 20, 3, 0).anchorWest().add(new JLabel("Menu"));
        headerPanel.incrementGridx().setInsets(3, 130, 3, 0).anchorCenter().add(new JLabel("Control"));
        headerPanel.incrementGridx().fillHorizontal().setInsets(3, 140, 3, 0).add(new JLabel("Shortcut Key"));
        return headerPanel;
    }

    /**
     * Gets the shortcut key panel.
     *
     * @return the shortcut key panel
     */
    private GridBagPanel getShortcutKeyPanel()
    {
        if (myShortcutKeyPanel == null)
        {
            myShortcutKeyPanel = new GridBagPanel();
            myShortcutKeyPanel.setGridx(0).setGridy(0).add(getShortcutKeyHeaderPanel());

            GridBagPanel contentPanel = new GridBagPanel();
            JLabel aLabel = null;
            aLabel = new JLabel("File:");
            aLabel.setForeground(Color.ORANGE);
            contentPanel.setGridx(0).setInsets(0, 20, 0, 0).setGridy(0).setGridwidth(2).fillHorizontal().anchorWest().add(aLabel);

            JLabel openLabel = new JLabel("Open");
            contentPanel.setGridx(0).setGridy(1).setInsets(0, 175, 5, 0).setGridwidth(1).fillNone().add(openLabel);
            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1).add(new JLabel("Ctrl-O"));

            contentPanel.setGridx(0).incrementGridy().setInsets(0, 175, 5, 0).setGridwidth(1).fillNone().add(new JLabel("Quit"));
            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1).add(new JLabel("Ctrl-Q"));

            aLabel = new JLabel("Edit:");
            aLabel.setForeground(Color.ORANGE);
            contentPanel.setGridx(0).setInsets(0, 20, 0, 0).incrementGridy().setGridwidth(2).fillHorizontal().anchorWest()
            .add(aLabel);

            contentPanel.setGridx(0).incrementGridy().setInsets(0, 175, 5, 0).setGridwidth(1).fillNone()
            .add(new JLabel("Set Logger Levels"));
            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1).add(new JLabel("Ctrl-L"));

            contentPanel.setGridx(0).incrementGridy().setInsets(0, 175, 5, 0).setGridwidth(1).fillNone()
            .add(new JLabel("Settings"));
            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1).add(new JLabel("F8"));

            aLabel = new JLabel("View");
            aLabel.setForeground(Color.ORANGE);
            contentPanel.setGridx(0).setInsets(0, 20, 0, 0).incrementGridy().setGridwidth(2).fillHorizontal().anchorWest()
            .add(aLabel);

            aLabel = new JLabel("Map Overlays:");
            aLabel.setForeground(Color.ORANGE);
            contentPanel.setGridx(0).setInsets(0, 50, 0, 0).incrementGridy().setGridwidth(2).fillHorizontal().anchorWest()
            .add(aLabel);

            contentPanel.setGridx(0).incrementGridy().setInsets(0, 175, 5, 0).setGridwidth(1).fillNone()
            .add(new JLabel("Alert Viewer"));
            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1).add(new JLabel("Ctrl-M"));

            //            aLabel = new JLabel("Controls:");
            //            aLabel.setForeground(Color.ORANGE);
            //            contentPanel.setGridx(0).setInsets(0, 50, 0, 0).incrementGridy().setGridwidth(2).fillHorizontal().anchorWest()
            //                    .add(aLabel);
            //
            //            contentPanel.setGridx(0).incrementGridy().setInsets(0, 175, 5, 0).setGridwidth(1).fillNone()
            //                    .add(new JLabel("My Data"));
            //            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1).add(new JLabel("F7"));

            aLabel = new JLabel("Tools:");
            aLabel.setForeground(Color.ORANGE);
            contentPanel.setGridx(0).setInsets(0, 20, 0, 0).incrementGridy().setGridwidth(2).fillHorizontal().anchorWest()
            .add(aLabel);

            contentPanel.setGridx(0).incrementGridy().setInsets(0, 175, 5, 0).setGridwidth(1).fillNone()
            .add(new JLabel("Analyze"));
            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1)
            .add(new JLabel("Ctrl+Shift-X"));

            contentPanel.setGridx(0).incrementGridy().setInsets(0, 175, 5, 0).setGridwidth(1).fillNone()
            .add(new JLabel("ArcLength (Measure)"));
            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1).add(new JLabel("m"));

            contentPanel.setGridx(0).incrementGridy().setInsets(0, 175, 5, 0).setGridwidth(1).fillNone()
            .add(new JLabel("Icon Manager"));
            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1)
            .add(new JLabel("Ctrl+Shift-I"));

            contentPanel.setGridx(0).incrementGridy().setInsets(0, 175, 5, 0).setGridwidth(1).fillNone()
            .add(new JLabel("Styles"));
            contentPanel.setGridx(1).setInsets(0, 65, 5, 0).fillNone().anchorWest().setGridwidth(1)
            .add(new JLabel("Ctrl+Shift-S"));

            contentPanel.incrementGridy().fillVerticalSpace();

            myShortcutKeyPanel.setGridy(1).fillBoth().add(contentPanel);
        }
        return myShortcutKeyPanel;
    }

    /**
     * Gets the shortcut key sp.
     *
     * @return the shortcut key sp
     */
    private JScrollPane getShortcutKeySP()
    {
        if (myShortcutKeySP == null)
        {
            myShortcutKeySP = new JScrollPane(getShortcutKeyPanel());
            myShortcutKeySP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        return myShortcutKeySP;
    }

    /**
     * Gets the tab pane.
     *
     * @return the tab pane
     */
    private JTabbedPane getTabPane()
    {
        if (myTabbedPane == null)
        {
            myTabbedPane = new JTabbedPane();
            myTabbedPane.addTab("Control Keys", null, getControlKeySP(), null);
            myTabbedPane.addTab("ShortCut Keys", null, getShortcutKeySP(), null);
        }
        return myTabbedPane;
    }

    /** Populate the binding panel with the current set of bindings. */
    private void populateBindingSubPanel()
    {
        assert SwingUtilities.isEventDispatchThread();

        ControlContext controlContext = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        Map<String, List<BindingsToListener>> eventListeners = controlContext.getEventListenersByCategory();

        int rowCounter = 0;
        String previousCategory = "";

        List<String> bindingKeys = New.list(eventListeners.keySet());
        Collections.sort(bindingKeys);

        // Make sure the keys are listed alphabetically.
        for (String category : bindingKeys)
        {
            for (BindingsToListener btl : eventListeners.get(category))
            {
                BoundEventListener listener = btl.getListener();
                // Don't list things that can't be reassigned. This should be
                // limited to such things as
                // overlay readouts that are triggered by Mouse Moved events,
                // and other similarly "not specifically user invoked"
                // controls.
                if (listener.isReassignable())
                {
                    if (category.equals(previousCategory))
                    {
                        addListenerTitle(listener, rowCounter);
                        addBindingButton(btl, rowCounter);
                    }
                    else
                    {
                        addCategoryLabel(category, rowCounter);
                        rowCounter++;
                        addListenerTitle(listener, rowCounter);
                        addBindingButton(btl, rowCounter);
                        previousCategory = category;
                    }
                    rowCounter++;
                }
            }
        }
    }
}
