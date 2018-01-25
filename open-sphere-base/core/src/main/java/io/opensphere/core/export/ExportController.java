package io.opensphere.core.export;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JMenu;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.util.Service;

/**
 * The controller that adds certain exporter context menus to the context action
 * manager.
 */
public class ExportController implements Service
{
    /** The context action manager. */
    private final ContextActionManager myActionManager;

    /** The geometry context menu provider. */
    private final ContextMenuProvider<GeometryContextKey> myGeometryContextProvider;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public ExportController(Toolbox toolbox)
    {
        myActionManager = toolbox.getUIRegistry().getContextActionManager();
        myGeometryContextProvider = new ContextMenuProvider<GeometryContextKey>()
        {
            @Override
            public Collection<? extends Component> getMenuItems(String contextId, GeometryContextKey key)
            {
                Collection<? extends Component> menuItems = Exporters.getMenuItems(contextId, key, toolbox);
                if (!menuItems.isEmpty())
                {
                    JMenu exportMenu = new JMenu("Export");
                    for (Component item : menuItems)
                    {
                        exportMenu.add(item);
                    }
                    return Collections.singleton(exportMenu);
                }
                return null;
            }

            @Override
            public int getPriority()
            {
                return 11505;
            }
        };
    }

    @Override
    public void open()
    {
        myActionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT, GeometryContextKey.class,
                myGeometryContextProvider);
        myActionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                myGeometryContextProvider);
    }

    @Override
    public void close()
    {
        myActionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT, GeometryContextKey.class,
                myGeometryContextProvider);
        myActionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                myGeometryContextProvider);
    }
}
