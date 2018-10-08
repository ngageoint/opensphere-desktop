package io.opensphere.core.appl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import io.opensphere.core.Toolbox;
import io.opensphere.core.capture.CaptureMenuInit;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultKeyPressedBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.dialog.LoggerDialog;
import io.opensphere.core.event.ImportDataEvent;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.pipeline.Pipeline;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.projection.AbstractProjection;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.SwingUtilities;

/**
 * Initializer for the main menu bar.
 */
public class MenuInit
{
    /** The maximum allowed max memory setting. */
    public static final int MAX_MAX_MEMORY_MB = 16384;

    /** A reference to the kernel. */
    private final Kernel myKernel;

    /** The event listener to show the debug menu. */
    private DiscreteEventAdapter myDebugListener;

    /** Strong references to units change listeners. */
    private final Collection<UnitsChangeListener<?>> myUnitsChangeListeners = New.collection();

    /**
     * Construct the initializer.
     *
     * @param kernel The kernel.
     */
    public MenuInit(Kernel kernel)
    {
        myKernel = kernel;
    }

    /**
     * Add the basic menu items to the main menu bar.
     *
     * @param toolbox The toolbox.
     */
    public void addBasicMenuItems(final Toolbox toolbox)
    {
        addFileMenuItems(toolbox);
        addEditMenuItems(toolbox);
        addViewMenuItems(toolbox);
        addToolsMenuItems(toolbox);
        addDataControlMenuItems(toolbox);
        addDebugMenuItems(toolbox);
    }

    /**
     * Create the menu for manipulating the render rate.
     *
     * @return The menu.
     */
    protected JMenu createRenderRateMenu()
    {
        final JRadioButtonMenuItem asNeeded = new JRadioButtonMenuItem("As needed");
        asNeeded.setSelected(true);
        asNeeded.addActionListener(e ->
        {
            for (final Pipeline pipeline : myKernel.getPipelines())
            {
                pipeline.setFrameRate(0);
            }
        });

        final JRadioButtonMenuItem enterRate = new JRadioButtonMenuItem("Enter rate...");
        enterRate.addActionListener(e ->
        {
            final JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            final JTextField entryField = new JTextField(3);
            entryField.addActionListener(unused ->
            {
                if (entryField.getText().trim().length() == 0)
                {
                    frame.dispose();
                    return;
                }
                try
                {
                    final int rate = Integer.parseInt(entryField.getText());
                    for (final Pipeline pipeline : myKernel.getPipelines())
                    {
                        pipeline.setFrameRate(rate);
                    }
                    frame.dispose();
                }
                catch (final NumberFormatException e1)
                {
                    JOptionPane.showMessageDialog(frame, "Cannot parse rate \"" + entryField.getText() + "\"");
                }
            });
            entryField.addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyTyped(KeyEvent e1)
                {
                    if (e1.getKeyChar() == KeyEvent.VK_ESCAPE)
                    {
                        frame.dispose();
                    }
                }
            });
            final JPanel panel = new JPanel();
            panel.add(new JLabel("Enter target frame rate: "));
            panel.add(entryField);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        final JRadioButtonMenuItem asFastAsPossible = new JRadioButtonMenuItem("As fast as possible");
        asFastAsPossible.addActionListener(e ->
        {
            for (final Pipeline pipeline : myKernel.getPipelines())
            {
                pipeline.setFrameRate(-1);
            }
        });
        final ButtonGroup renderGroup = new ButtonGroup();
        renderGroup.add(asNeeded);
        renderGroup.add(enterRate);
        renderGroup.add(asFastAsPossible);

        final JMenu renderRateMenu = new JMenu("Render rate");
        renderRateMenu.add(asNeeded);
        renderRateMenu.add(asFastAsPossible);
        renderRateMenu.add(enterRate);

        return renderRateMenu;
    }

    /**
     * Adds the Data Control menu items.
     *
     * @param toolbox The toolbox
     */
    private void addDataControlMenuItems(final Toolbox toolbox)
    {
        // Get the menu
        final MenuBarRegistry mbr = toolbox.getUIRegistry().getMenuBarRegistry();
        final JMenu dataControlMenu = mbr.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DATA_CONTROL_MENU);
        if (dataControlMenu == null)
        {
            throw new IllegalStateException("Data Control menu cannot be found.");
        }

        // Add Cancel Tile Downloads
        dataControlMenu.add(getCancelTileDownloadsMenuItem(toolbox));
    }

    /**
     * Adds the Debug menu items.
     *
     * @param toolbox The toolbox
     */
    private void addDebugMenuItems(final Toolbox toolbox)
    {
        // Create the menu
        final MenuBarRegistry mbr = toolbox.getUIRegistry().getMenuBarRegistry();
        final JMenu debugMenu = mbr.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU);
        debugMenu.setVisible(!Boolean.getBoolean("opensphere.productionMode"));

        // Add Render rate
        debugMenu.add(createRenderRateMenu());

        // Add the debug listener
        myDebugListener = new DiscreteEventAdapter("Debug", "Show/Hide Debug Menu", "Show or hide the debug menu.")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                debugMenu.setVisible(!debugMenu.isVisible());
            }
        };
        myDebugListener.setReassignable(false);
        final ControlContext context = toolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        context.addListener(myDebugListener, new DefaultKeyPressedBinding(KeyEvent.VK_D,
                InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
    }

    /**
     * Adds the Edit menu items.
     *
     * @param toolbox The toolbox
     */
    private void addEditMenuItems(final Toolbox toolbox)
    {
        // Get the menu
        MenuBarRegistry mbr = toolbox.getUIRegistry().getMenuBarRegistry();
        JMenu editMenu = mbr.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.EDIT_MENU);
        if (editMenu == null)
        {
            throw new IllegalStateException("Edit menu cannot be found.");
        }

        // Add Set Logger Levels
        editMenu.add(getLoggerMenuItem(toolbox));

        // Add Units
        final JMenu unitsMenu = new JMenu("Units");
        for (UnitsProvider<?> unitsProvider : toolbox.getUnitsRegistry().getUnitsProviders())
        {
            JMenu subMenu = new JMenu(unitsProvider.getSuperType().getSimpleName());
            createMenuItems(unitsProvider, subMenu);
            unitsMenu.add(subMenu);
        }
        unitsMenu.add(SwingUtilities.newMenuItem("Reset all to default", e ->
                {
                Quantify.collectMetric("mist3d.menu-bar.edit.units.reset-to-default");
                toolbox.getUnitsRegistry().resetAllPreferredUnits(e.getSource());
                }));
        editMenu.add(unitsMenu);
    }

    /**
     * Adds the File menu items.
     *
     * @param toolbox The toolbox
     */
    private void addFileMenuItems(final Toolbox toolbox)
    {
        // Get the menu
        final MenuBarRegistry mbr = toolbox.getUIRegistry().getMenuBarRegistry();
        final JMenu fileMenu = mbr.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.FILE_MENU);
        if (fileMenu == null)
        {
            throw new IllegalStateException("File menu cannot be found.");
        }

        // Add Open
        fileMenu.add(SwingUtilities.newMenuItem("Open", e -> toolbox.getEventManager().publishEvent(new ImportDataEvent()),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK)));

        final boolean restartEnabled = Boolean.getBoolean("opensphere.enableRestart");
        if (restartEnabled)
        {
            fileMenu.add(SwingUtilities.newMenuItem("Restart", e -> myKernel.shutdown(2)));

            final JMenuItem restartDifferentMemoryMenuItem = new JMenuItem("Restart with new memory settings...");
            restartDifferentMemoryMenuItem.addActionListener(e ->
            {
                final Preferences prefs = toolbox.getPreferencesRegistry().getPreferences("io.opensphere.core.launch.Launch");
                final int prefMaxMemory = prefs.getInt("opensphere.launch.maxMemory", 1024);
                final int maxMemory = Math.max(1024, prefMaxMemory);

                final List<String> memoryOptions = new ArrayList<>();
                addMemoryOption(memoryOptions, maxMemory / 4);
                addMemoryOption(memoryOptions, maxMemory / 2);
                addMemoryOption(memoryOptions, maxMemory * 3 / 2);
                addMemoryOption(memoryOptions, maxMemory * 2);
                addMemoryOption(memoryOptions, maxMemory * 4);
                final String[] options = memoryOptions.toArray(new String[memoryOptions.size()]);
                final String defaultOption = options[Math.min(2, options.length - 1)];
                final int choice = JOptionPane.showOptionDialog(null,
                        "Memory is currently set to " + prefMaxMemory + " MB. Please choose the new value.", "Restart",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, defaultOption);
                if (choice != -1)
                {
                    prefs.putInt("opensphere.launch.maxMemory", Integer.parseInt(options[choice].split(" ")[0]), this);
                    prefs.waitForPersist();
                    myKernel.shutdown(2);
                }
            });
            fileMenu.add(restartDifferentMemoryMenuItem);
        }

        // Add Quit
        fileMenu.add(SwingUtilities.newMenuItem("Quit", e -> myKernel.shutdown(0),
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK)));
    }

    /**
     * Adds a memory option to the collection.
     *
     * @param c the collection
     * @param val the value
     */
    private static void addMemoryOption(Collection<? super String> c, int val)
    {
        if (val <= MAX_MAX_MEMORY_MB)
        {
            c.add(val + " MB");
        }
    }

    /**
     * Adds the Tools menu items.
     *
     * @param toolbox The toolbox
     */
    private void addToolsMenuItems(final Toolbox toolbox)
    {
        // Get the menu
        final MenuBarRegistry mbr = toolbox.getUIRegistry().getMenuBarRegistry();
        final JMenu toolsMenu = mbr.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.TOOLS_MENU);
        if (toolsMenu == null)
        {
            throw new IllegalStateException("Tools menu cannot be found.");
        }

        // Add Screen Capture
        toolsMenu.add(CaptureMenuInit.getScreenCaptureMenuItem(toolbox));

        // Add Lock Terrain
        toolsMenu.add(getLockTerrainMenuItem());
    }

    /**
     * Adds the View menu items.
     *
     * @param toolbox The toolbox
     */
    private void addViewMenuItems(final Toolbox toolbox)
    {
        // Get the menu
        final MenuBarRegistry mbr = toolbox.getUIRegistry().getMenuBarRegistry();
        final JMenu viewMenu = mbr.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.VIEW_MENU);
        if (viewMenu == null)
        {
            throw new IllegalStateException("View menu cannot be found.");
        }

        viewMenu.add(SwingUtilities.newMenuItem("Reset View", e ->
                {
                    toolbox.getMapManager().getStandardViewer().resetView();
                    Quantify.collectMetric("mist3d.menu-bar.edit.units.reset-to-default");
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK)));

//        // Add toolbar item
//        final JCheckBoxMenuItem toolbarItem = new JCheckBoxMenuItem("Toolbar");
//        toolbarItem.setSelected(toolbox.getUIRegistry().getToolbarComponentRegistry().getToolbarManager().isVisible());
//        toolbarItem.addActionListener(new ActionListener()
//        {
//            @Override
//            public void actionPerformed(ActionEvent arg0)
//            {
//                toolbox.getUIRegistry().getToolbarComponentRegistry().getToolbarManager().setVisible(toolbarItem.isSelected());
//            }
//        });
//        viewMenu.add(toolbarItem);

        // Add Projection
        mbr.getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.VIEW_MENU, MenuBarRegistry.PROJECTION_MENU);
    }

    /**
     * Create menu items for the available units in a units provider.
     *
     * @param <T> The supertype of the units.
     * @param unitsProvider The units provider.
     * @param subMenu The menu to which the items will be added.
     */
    private <T> void createMenuItems(final UnitsProvider<T> unitsProvider, final JMenu subMenu)
    {
        for (final Class<? extends T> units : unitsProvider.getAvailableUnits(true))
        {
            final JMenuItem unitsItem = new JRadioButtonMenuItem(unitsProvider.getSelectionLabel(units));
            unitsItem.addActionListener(e ->
            {
                Quantify.collectMetric("mist3d.menu-bar.edit.units.set-units-to-" + units.getSimpleName());
                unitsProvider.setPreferredUnits(units);
            });
            subMenu.add(unitsItem);
        }
        final UnitsChangeListener<T> listener = new UnitsChangeListener<>()
        {
            @Override
            public void availableUnitsChanged(Class<T> superType, Collection<Class<? extends T>> newTypes)
            {
                subMenu.removeAll();
                createMenuItems(unitsProvider, subMenu);
            }

            @Override
            public void preferredUnitsChanged(Class<? extends T> units)
            {
                final String selectionLabel = unitsProvider.getSelectionLabel(units);
                for (final Component component : subMenu.getMenuComponents())
                {
                    ((AbstractButton)component).setSelected(((AbstractButton)component).getText().equals(selectionLabel));
                }
            }
        };
        listener.preferredUnitsChanged(unitsProvider.getPreferredUnits());
        myUnitsChangeListeners.add(listener);
        unitsProvider.addListener(listener);
    }

    /**
     * Get the cancel tile downloads menu item.
     *
     * @param toolbox The toolbox.
     * @return The menu item.
     */
    private JMenuItem getCancelTileDownloadsMenuItem(final Toolbox toolbox)
    {
        return SwingUtilities.newMenuItem("Cancel Tile Downloads", e ->
        {
            Quantify.collectMetric("mist3d.menu-bar.data-control.cancel-tile-downloads");
            toolbox.getGeometryRegistry().cancelAllImageRetrievals();
        });
    }

    /**
     * Get the lock terrain menu item.
     *
     * @return The menu item.
     */
    private JCheckBoxMenuItem getLockTerrainMenuItem()
    {
        final JCheckBoxMenuItem terrainLock = new JCheckBoxMenuItem("Lock Terrain");
        terrainLock.addActionListener(e ->
        {
            Quantify.collectEnableDisableMetric("mist3d.menu-bar.tools.lock-terrain",
                    terrainLock.isSelected());
            AbstractProjection.setTerrainLocked(terrainLock.isSelected());
        });
        return terrainLock;
    }

    /**
     * Get the logger menu item.
     *
     * @param toolbox The toolbox.
     * @return The menu item.
     */
    private JMenuItem getLoggerMenuItem(final Toolbox toolbox)
    {
        final JMenuItem loggerMenuItem = new JMenuItem("Set Logger Levels...");
        loggerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        loggerMenuItem.addActionListener(new ActionListener()
        {
            /** The logger dialog shown when the action is selected. */
            private HUDJInternalFrame myLoggerFrame;

            @Override
            public void actionPerformed(ActionEvent event)
            {
                if (myLoggerFrame == null)
                {
                    Quantify.collectMetric("mist3d.menu-bar.edit.set-logger-levels");
                    final JComponent source = (JComponent)event.getSource();
                    final LoggerDialog loggerDialog = new LoggerDialog(toolbox.getPreferencesRegistry(), "Set Logger Levels");
                    loggerDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                    loggerDialog.pack();
                    final HUDJInternalFrame.Builder builder = new HUDJInternalFrame.Builder();
                    builder.setInternalFrame(loggerDialog);
                    myLoggerFrame = new HUDJInternalFrame(builder);

                    toolbox.getUIRegistry().getComponentRegistry().addObjectsForSource(source,
                            Collections.singleton(myLoggerFrame));
                }
                myLoggerFrame.getInternalFrame().setVisible(true);
            }
        });
        return loggerMenuItem;
    }
}
