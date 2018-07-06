package io.opensphere.mantle.data.export;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.export.ExportUtilities;
import io.opensphere.core.export.Exporter;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.collections.New;

/**
 * Mantle export menu provider.
 */
public class ExportMenuProvider
{
    /**
     * Gets the export menu items.
     *
     * @param toolbox the toolbox
     * @param prefix the prefix text in the menu item
     * @param exporters the available exporters
     * @return the menu items
     */
    public Collection<JMenuItem> getMenuItems(final Toolbox toolbox, String prefix, Collection<? extends Exporter> exporters)
    {
        Collection<JMenuItem> menuItems = New.list(exporters.size());
        for (final Exporter exporter : exporters)
        {
            String mimeType = exporter.getMimeTypeString();
            StringBuilder name = new StringBuilder(prefix).append(mimeType).append("...");

            Action action = new AbstractAction(name.toString())
            {
                /** Serial version UID. */
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    QuantifyToolboxUtils.collectMetric(toolbox,
                            "mist3d.export-menu." + exporter.getMimeType().toString().toLowerCase().replaceAll(" ", "-"));
                    Component parentComponent = toolbox.getUIRegistry().getMainFrameProvider().get();
                    PreferencesRegistry prefsRegistry = toolbox.getPreferencesRegistry();
                    ExportUtilities.export(parentComponent, prefsRegistry, exporter);
                }
            };

            menuItems.add(new JMenuItem(action));
        }
        return menuItems;
    }
}
