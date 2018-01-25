package io.opensphere.filterbuilder2.manager;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.filterbuilder.controller.FilterBuilderToolbox;
import io.opensphere.filterbuilder2.common.Constants;

/**
 * View for managing a single spatial filter.
 */
public class FilterManagerSpatialFilterPanel extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The filter builder toolbox. */
    private final transient FilterBuilderToolbox myFbToolbox;

    /** The spatial filter. */
    private final Geometry mySpatialFilter;

    /** The filter source. */
    private final String myTypeKey;

    /** The display name. */
    private final String myDisplayName;

    /**
     * Constructor.
     *
     * @param fbToolbox the filter builder toolbox
     * @param spatialFilter the spatial filter
     * @param typeKey the type key
     * @param displayName the layer display name
     */
    public FilterManagerSpatialFilterPanel(FilterBuilderToolbox fbToolbox, Geometry spatialFilter, String typeKey,
            String displayName)
    {
        myFbToolbox = fbToolbox;
        mySpatialFilter = spatialFilter;
        myTypeKey = typeKey;
        myDisplayName = displayName;
        buildPanel();
    }

    /**
     * Builds the filter panel.
     */
    private void buildPanel()
    {
        final IconButton deleteButton = new IconButton(IconType.CLOSE, Color.RED);
        deleteButton.setToolTipText("Remove the spatial filter from this layer");
        deleteButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteFilter(deleteButton);
            }
        });

        Point centroid = mySpatialFilter.getCentroid();
        StringBuilder label = new StringBuilder(45);
        label.append("Spatial filter (center ");
        label.append(LatLonAlt.latToDMSString(centroid.getY(), 0));
        label.append(' ');
        label.append(LatLonAlt.lonToDMSString(centroid.getX(), 0));
        label.append(')');

        setInsets(0, Constants.INSET, 0, Constants.INSET);
        fillNone();
        add(new JLabel(label.toString()));
        fillHorizontalSpace().fillNone();
        setInsets(Constants.INSET, 0, Constants.INSET, Constants.INSET);
        add(deleteButton);
    }

    /**
     * Deletes a filter.
     *
     * @param parent the parent component
     */
    private void deleteFilter(Component parent)
    {
        String message = StringUtilities.concat("<html>Remove spatial filter for <b>", myDisplayName, "</b> ?</html>");
        int decision = JOptionPane.showConfirmDialog(parent, message, "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (decision == JOptionPane.YES_OPTION)
        {
            myFbToolbox.getMainToolBox().getDataFilterRegistry().removeSpatialLoadFilter(myTypeKey);
        }
    }
}
