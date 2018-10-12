package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Basically a ButtonGroup for the {@link SelectableLabel}.
 */
public class SelectableLabelGroup implements ActionListener
{
    /** The Group. */
    private final List<SelectableLabel> myGroup;

    /**
     * Instantiates a new selectable label group.
     */
    public SelectableLabelGroup()
    {
        myGroup = New.list();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() instanceof SelectableLabel)
        {
            SelectableLabel lb = (SelectableLabel)e.getSource();
            if (lb.isSelected())
            {
                deselectAllButLabel(lb);
            }
        }
    }

    /**
     * Adds the label to the group.
     *
     * @param lb the {@link SelectableLabel} to add.
     */
    public void addLabel(SelectableLabel lb)
    {
        synchronized (myGroup)
        {
            myGroup.add(lb);
            lb.addActionListener(this);
        }
    }

    /**
     * Clear selection of all labels in group.
     */
    public void clearSelection()
    {
        deselectAllButLabel(null);
    }

    /**
     * Gets the selected label.
     *
     * @return the selected label
     */
    public SelectableLabel getSelectedLabel()
    {
        synchronized (myGroup)
        {
            return myGroup.stream().filter(SelectableLabel::isSelected).findFirst().orElse(null);
        }
    }

    /**
     * Removes the all {@link SelectableLabel} from the group.
     */
    public void removeAllLabels()
    {
        synchronized (myGroup)
        {
            myGroup.forEach(lb -> lb.removeActionListener(this));
            myGroup.clear();
        }
    }

    /**
     * Removes the label from the group.
     *
     * @param lb the {@link SelectableLabel} to remove.
     * @return true, if successful, false if not in group.
     */
    public boolean removeLabel(SelectableLabel lb)
    {
        boolean removed = false;
        synchronized (myGroup)
        {
            removed = myGroup.remove(lb);
            if (removed)
            {
                lb.removeActionListener(this);
            }
        }
        return removed;
    }

    /**
     * Deselect all but the specified label.
     *
     * @param lb the {@link SelectableLabel}
     */
    private void deselectAllButLabel(SelectableLabel lb)
    {
        synchronized (myGroup)
        {
            myGroup.stream().filter(l -> !Utilities.sameInstance(l, lb) && lb.isSelected())
                    .forEach(l -> l.setSelected(false, true));
        }
    }
}
