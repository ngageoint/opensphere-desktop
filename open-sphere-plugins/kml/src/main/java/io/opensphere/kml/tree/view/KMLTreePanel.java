package io.opensphere.kml.tree.view;

import java.awt.BorderLayout;

import com.jidesoft.swing.CheckBoxTree;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.util.swing.AbstractHUDPanel;

/**
 * The KML tree panel that shows up in Layer Manager.
 */
public class KMLTreePanel extends AbstractHUDPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The tree. */
    private final CheckBoxTree myTree;

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry
     */
    public KMLTreePanel(DataRegistry dataRegistry)
    {
        super(new BorderLayout());
        setOpaque(false);
        myTree = new KMLTree(dataRegistry);
        add(getJScrollPane(myTree), BorderLayout.CENTER);
    }

    /**
     * Getter for tree.
     *
     * @return the tree
     */
    public CheckBoxTree getTree()
    {
        return myTree;
    }
}
