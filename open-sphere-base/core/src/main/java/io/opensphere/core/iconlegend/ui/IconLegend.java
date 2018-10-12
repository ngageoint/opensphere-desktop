package io.opensphere.core.iconlegend.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.iconlegend.IconLegendListener;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GridBagPanel;
import net.jcip.annotations.GuardedBy;

/**
 * The Class IconLegend. Sets up a table that displays details about icons that
 * are added to the panel.
 */
public final class IconLegend implements IconLegendListener
{
    /** The Constant TITLE. */
    public static final String TITLE = "Icon Legend";

    /** The Icon legend frame. */
    private AbstractInternalFrame myIconLegendFrame;

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /** The Icon set. */
    private transient Map<String, IconSet> myIconSet;

    /** The Panel. */
    private GridBagPanel myMainPanel;

    /** The Icon table. */
    private JXTable myIconTable;

    /** The plan lock. */
    private final ReentrantReadWriteLock myModelLock = new ReentrantReadWriteLock();

    /** The Model. */
    @GuardedBy("myModelLock")
    private DefaultTableModel myModel;

    /**
     * Instantiates a new icon legend.
     *
     * @param toolbox the toolbox
     */
    public IconLegend(Toolbox toolbox)
    {
        super();
        myToolbox = toolbox;
        myToolbox.getUIRegistry().getIconLegendRegistry().addIconLegendListener(this);
        initialize();
    }

    /**
     * Lazily create the main frame.
     *
     * @return the main frame
     */
    public AbstractInternalFrame getMainFrame()
    {
        if (myIconLegendFrame == null)
        {
            myIconLegendFrame = new AbstractInternalFrame();
            myIconLegendFrame.setSize(800, 500);
            myIconLegendFrame.setPreferredSize(myIconLegendFrame.getSize());
            myIconLegendFrame.setMinimumSize(myIconLegendFrame.getSize());
            myIconLegendFrame.setTitle(TITLE);
            myIconLegendFrame.setOpaque(false);
            // TODO It is not clear how minimizing can be done for the HUD.
            // Might
            // need to look into this later
            myIconLegendFrame.setIconifiable(false);
            myIconLegendFrame.setClosable(true);
            myIconLegendFrame.setResizable(true);
            myIconLegendFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            myIconLegendFrame.setContentPane(getMainPanel());
            myIconLegendFrame.setLocation(200, 100);
        }
        return myIconLegendFrame;
    }

    @Override
    public void iconLegendIconAdded(final Icon icon, final String iconName, final String description)
    {
        WriteLock modelLock = myModelLock.writeLock();
        modelLock.lock();
        try
        {
            getIconTableModel().getDataVector().removeAllElements();
            if (!myIconSet.containsKey(iconName))
            {
                myIconSet.put(iconName, new IconSet(icon, iconName, description));
            }
            List<IconSet> iconList = new ArrayList<>(myIconSet.values());
            Collections.sort(iconList);
            for (IconSet set : iconList)
            {
                getIconTableModel().addRow(new Object[] { set.getIcon(), set.getIconName(), set.getIconDescription() });
            }
        }
        finally
        {
            modelLock.unlock();
        }
    }

    /**
     * Gets the icon table.
     *
     * @return the icon table
     */
    private JXTable getIconTable()
    {
        if (myIconTable == null)
        {
            myIconTable = new JXTable(getIconTableModel())
            {
                /** Serial. */
                private static final long serialVersionUID = 1L;

                @Override
                public Class<?> getColumnClass(int column)
                {
                    return getValueAt(0, column).getClass();
                }
            };
            myIconTable.setEditable(false);
            myIconTable.setGridColor(Color.BLACK);
            myIconTable.setRowSelectionAllowed(false);
            myIconTable.setShowHorizontalLines(true);
            myIconTable.setShowVerticalLines(true);
            myIconTable.getTableHeader().setReorderingAllowed(false);
            myIconTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            myIconTable.getColumn("Icon").setMinWidth(22);
            myIconTable.getColumn("Icon").setMaxWidth(40);
            myIconTable.getColumn("Name").setMinWidth(200);
            myIconTable.getColumn("Name").setMaxWidth(500);
            myIconTable.addHighlighter(
                    HighlighterFactory.createAlternateStriping(Colors.LF_INNER_BACKGROUND, Colors.LF_INNER_BACKGROUND_ALT));
            myIconTable.getColumn("Description").setCellRenderer(new IconDescriptionCellRenderer());
        }
        return myIconTable;
    }

    /**
     * Gets the icon table model.
     *
     * @return the icon table model
     */
    private DefaultTableModel getIconTableModel()
    {
        if (myModel == null)
        {
            myModel = new DefaultTableModel();
            String[] colNames = new String[] { "Icon", "Name", "Description" };
            myModel.setColumnIdentifiers(colNames);
        }
        return myModel;
    }

    /**
     * Gets the main panel.
     *
     * @return the main panel
     */
    private JPanel getMainPanel()
    {
        if (myMainPanel == null)
        {
            myMainPanel = new GridBagPanel();
            JScrollPane jsp = new JScrollPane(getIconTable());
            myMainPanel.setGridx(0).setGridy(0).fillBoth().add(jsp);
        }
        return myMainPanel;
    }

    /**
     * Initialize.
     */
    private void initialize()
    {
        myIconSet = New.map();
        getIconTableModel();
    }

    /**
     * The Class IconDescriptionCellRenderer. JTextArea renderer that set row
     * heights based on text in the JTextArea.
     */
    protected static class IconDescriptionCellRenderer extends JTextArea implements TableCellRenderer
    {
        /** Serial. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new icon description cell renderer.
         */
        public IconDescriptionCellRenderer()
        {
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setEditable(false);
            setLineWrap(true);
            setWrapStyleWord(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                int column)
        {
            setText(value.toString());
            setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
            if (table.getRowHeight(row) != getPreferredSize().height)
            {
                table.setRowHeight(row, getPreferredSize().height);
            }
            return this;
        }
    }

    /**
     * Helper class to contain/compare icon details.
     */
    static class IconSet implements Comparable<IconSet>
    {
        /** The Icon. */
        private final Icon myIcon;

        /** The Icon name. */
        private final String myIconName;

        /** The Icon desc. */
        private final String myIconDesc;

        /**
         * Instantiates a new icon set.
         *
         * @param icon the icon
         * @param iconName the icon name
         * @param description the description
         */
        public IconSet(Icon icon, String iconName, String description)
        {
            myIcon = icon;
            myIconName = iconName;
            myIconDesc = description;
        }

        @Override
        public int compareTo(IconSet o)
        {
            return myIconName.compareTo(o.getIconName());
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }

            IconSet other = (IconSet)obj;
            if (!getIcon().equals(other.getIcon()))
            {
                return false;
            }
            if (!getIconName().equals(other.getIconName()))
            {
                return false;
            }
            return getIconDescription().equals(other.getIconDescription());
        }

        /**
         * Gets the icon.
         *
         * @return the icon
         */
        public Icon getIcon()
        {
            return myIcon;
        }

        /**
         * Gets the icon description.
         *
         * @return the icon description
         */
        public String getIconDescription()
        {
            return myIconDesc;
        }

        /**
         * Gets the icon name.
         *
         * @return the icon name
         */
        public String getIconName()
        {
            return myIconName;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (getIcon() == null ? 0 : getIcon().hashCode());
            result = prime * result + (getIconName() == null ? 0 : getIconName().hashCode());
            result = prime * result + (getIconDescription() == null ? 0 : getIconDescription().hashCode());

            return result;
        }
    }
}
