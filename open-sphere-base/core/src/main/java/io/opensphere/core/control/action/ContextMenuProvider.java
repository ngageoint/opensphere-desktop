package io.opensphere.core.control.action;

import java.awt.Component;
import java.util.Collection;

/**
 * Interface for providers of menu items for context menus.
 *
 * @param <T> The context menu key type.
 */
public interface ContextMenuProvider<T>
{
    /**
     * Provide a list of menu items for the context menu associated with this
     * key.
     *
     * @param contextId the context for the menus.
     * @param key Key for which the context menu will be created.
     * @return Menu items to supply in the context menu.
     */
    Collection<? extends Component> getMenuItems(String contextId, T key);

    /**
     * Gets the priority. This will ensure that the context menus are displayed
     * in a consistent order between sessions. Priorities for context providers
     * are currently mapped as follows:
     * SelectionHandler.myGeometryContextMenuProvider 10000
     * SelectionHandler.myMultiGeometryContextMenuProvider 10001
     *
     * AnnotationContextMenuProvider 11000 SelectionRegionTransformer 11100
     * BoundingBoxPanel 11200 ArcLengthTransformer 11300
     *
     * RegionsController.myGeometryContextMenuProvider 11400
     * RegionsController.myGeometryContextMenuProvider 11401
     *
     * MapManagerMenuProvider.myGeometryContextMenuProvider 11500
     * MapManagerMenuProvider.myMultiGeometryContextMenuProvider 11501
     *
     * ImageryTransformer.myMenuProvider(imagery) 11600
     *
     * LidarEnvoy.myGeometryContextMenuProvider 11700
     *
     * QuerySupportPlugin.myWorldQueryMenuProvider 11800
     * QuerySupportPlugin.myClearQueryRegionsMenuProvider 11900
     *
     * @return the priority
     */
    int getPriority();
}
