package io.opensphere.analysis.baseball;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.mantle.data.element.DataElement;

/**
 * The baseball card dialog.
 */
class BaseballDialog extends JDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The preferences registry. */
    private final PreferencesRegistry myPrefsRegistry;

    /** The table. */
    private final JXTable myTable;

    /** The renderer. */
    private BaseballRenderer myRenderer;

    /**
     * Constructor.
     *
     * @param parent the parent
     * @param prefsRegistry The preferences registry
     */
    public BaseballDialog(Window parent, PreferencesRegistry prefsRegistry)
    {
        super(parent);
        myPrefsRegistry = prefsRegistry;

        myTable = new JXTable();
        myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Color bgColor = new Color(58, 58, 71);
        myTable.setGridColor(bgColor);
        myTable.setHighlighters(HighlighterFactory.createAlternateStriping(bgColor, bgColor.brighter()));

        add(new JScrollPane(myTable));

        setMinimumSize(new Dimension(150, 200));
        setSize(new Dimension(400, 500));
        setLocationRelativeTo(parent);
    }

    /**
     * Sets the data element.
     *
     * @param element the data element
     */
    public void setDataElement(DataElement element)
    {
        if (myRenderer != null)
        {
            myRenderer.close();
        }

        setTitle(element.getDataTypeInfo().getDisplayName() + " - " + element.getId());
        BaseballTableModel tableModel = new BaseballTableModel(element, myPrefsRegistry);
        myTable.setModel(tableModel);
        myRenderer = new BaseballRenderer(tableModel, myTable, myPrefsRegistry);
        myTable.getColumn(1).setCellRenderer(myRenderer);
        myTable.packAll();
    }
}
