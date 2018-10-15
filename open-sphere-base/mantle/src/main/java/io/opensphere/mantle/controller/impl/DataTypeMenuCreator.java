package io.opensphere.mantle.controller.impl;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.TextViewDialog;

/** The data type menu creator. */
public class DataTypeMenuCreator
{
    /** The Constant CHOOSE_DATA_TYPE. */
    private static final String CHOOSE_DATA_TYPE = "Choose Data Type";

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The data types supplier. */
    private final Supplier<List<DataTypeInfo>> myDataTypesSupplier;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param dataTypesSupplier The data types supplier
     */
    public DataTypeMenuCreator(Toolbox toolbox, Supplier<List<DataTypeInfo>> dataTypesSupplier)
    {
        myToolbox = toolbox;
        myDataTypesSupplier = dataTypesSupplier;
    }

    /** Creates the menus on the swing thread. */
    public void createMenus()
    {
        EventQueueUtilities.runOnEDT(this::createMenusNow);
    }

    /** Creates the menus now. */
    private void createMenusNow()
    {
        JMenu debugMenu = myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                MenuBarRegistry.DEBUG_MENU);
        debugMenu.add(SwingUtilities.newMenuItem("DataTypeInfo - Print Summary", e -> printSummary()));
        debugMenu.add(SwingUtilities.newMenuItem("DataTypeInfo - Tag Data Type", e -> tagDataType()));
    }

    /** Prints a summary of a data type. */
    private void printSummary()
    {
        List<DataTypeInfo> dtiList = myDataTypesSupplier.get();
        if (!dtiList.isEmpty())
        {
            List<DataTypeInfoDisplayNameProxy> proxyList = dtiList.stream().map(s -> new DataTypeInfoDisplayNameProxy(s))
                    .collect(Collectors.toList());
            Collections.sort(proxyList, (o1, o2) -> o1.toString().compareTo(o2.toString()));

            Object selected = JOptionPane.showInputDialog(null, CHOOSE_DATA_TYPE, CHOOSE_DATA_TYPE, JOptionPane.QUESTION_MESSAGE,
                    null, proxyList.toArray(), proxyList.get(0));

            if (selected != null)
            {
                final DataTypeInfoDisplayNameProxy fStyle = (DataTypeInfoDisplayNameProxy)selected;

                TextViewDialog dvd = new TextViewDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                        "DataTypeInfo Summary: " + fStyle.toString(), fStyle.getItem().toString(), false,
                        myToolbox.getPreferencesRegistry());
                dvd.setLocationRelativeTo(myToolbox.getUIRegistry().getMainFrameProvider().get());
                dvd.setVisible(true);
            }
        }
    }

    /** Tags a data type. */
    private void tagDataType()
    {
        List<DataTypeInfo> dtList = myDataTypesSupplier.get();
        if (!dtList.isEmpty())
        {
            List<DataTypeInfoDisplayNameProxy> pxyList = DataTypeInfoDisplayNameProxy.toProxyList(dtList, null);
            Object selected = JOptionPane.showInputDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                    CHOOSE_DATA_TYPE, CHOOSE_DATA_TYPE, JOptionPane.QUESTION_MESSAGE, null, pxyList.toArray(), pxyList.get(0));

            if (selected != null)
            {
                DataTypeInfoDisplayNameProxy proxy = (DataTypeInfoDisplayNameProxy)selected;
                StringBuilder sb = new StringBuilder(128);
                sb.append("Data Type: ").append(proxy.getItem().getDisplayName()).append("\n" + "Current Tags Are:\n ")
                        .append(proxy.getItem().getTags().toString())
                        .append("\n\n" + "Add a tag by typing in a NEW string that is not in the list.\n"
                                + "Remove a tag by typing in an EXISTING tag name.");
                String result = JOptionPane.showInputDialog(myToolbox.getUIRegistry().getMainFrameProvider(), sb.toString());
                if (result != null)
                {
                    if (proxy.getItem().hasTag(result))
                    {
                        proxy.getItem().removeTag(result, this);
                    }
                    else
                    {
                        proxy.getItem().addTag(result, this);
                    }
                }
            }
        }
    }
}
