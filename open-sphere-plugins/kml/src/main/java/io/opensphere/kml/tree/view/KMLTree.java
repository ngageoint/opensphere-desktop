package io.opensphere.kml.tree.view;

import javax.swing.tree.DefaultMutableTreeNode;

import com.jidesoft.swing.CheckBoxTree;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.util.swing.tree.TreeDecorator;

/**
 * The KML tree.
 */
public class KMLTree extends CheckBoxTree
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry.
     */
    public KMLTree(DataRegistry dataRegistry)
    {
        super(new DefaultMutableTreeNode("KML Data Sources"));
        TreeDecorator.decorate(this);
        setCellRenderer(new KMLTreeCellRenderer(dataRegistry));
    }
}
