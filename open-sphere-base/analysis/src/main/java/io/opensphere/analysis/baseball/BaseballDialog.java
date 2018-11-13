package io.opensphere.analysis.baseball;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.mantle.data.element.DataElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

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
    private final JXTable myRightTable;

    private final JXTable myLeftTable;

    /** The renderer. */
    private BaseballRenderer myRenderer;

    private BaseballRenderer myLeftRenderer;
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

        myLeftTable = new JXTable();
        myLeftTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Color bgColor = new Color(58, 58, 71);
        myLeftTable.setGridColor(bgColor);
        myLeftTable.setHighlighters(HighlighterFactory.createAlternateStriping(bgColor, bgColor.brighter()));

        myRightTable = new JXTable();
        myRightTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        myRightTable.setGridColor(bgColor);
        myRightTable.setHighlighters(HighlighterFactory.createAlternateStriping(bgColor, bgColor.brighter()));

        add(new JScrollPane(myLeftTable));
        add(new JScrollPane(myRightTable));

        setMinimumSize(new Dimension(150, 200));
        setSize(new Dimension(400, 500));
        setLocationRelativeTo(parent);
    }

    /**
     * Sets the data element.
     *
     * @param element the data element
     */
    public void setDataElement(List<DataElement> elements)
    {
        if (myRenderer != null)
        {
            myRenderer.close();
        }

        setTitle("Feature Info");

        ObservableList<String> elementTimes = FXCollections.observableArrayList();
        elements.forEach(e -> elementTimes.add(e.getTimeSpan().toDisplayString()));
        
        ListView<String> times = new ListView<String>();
        times.setItems(elementTimes);
        times.setPrefSize(200, 200);
//        myLeftTable;
        BaseballTimeModel timeModel = new BaseballTimeModel(elements);
        elementTimes.forEach(e -> System.out.println(e));
//        myLeftRenderer = new BaseballRenderer(timeModel, myLeftTable, myPrefsRegistry);
        myLeftTable.setModel(new BaseballTimeModel(elements));
        myLeftTable.packAll();

        BaseballTableModel tableModel = new BaseballTableModel(elements.get(0), myPrefsRegistry);
        myRightTable.setModel(tableModel);
        myRenderer = new BaseballRenderer(tableModel, myRightTable, myPrefsRegistry);
        myRightTable.getColumn(1).setCellRenderer(myRenderer);
        myRightTable.packAll();
    }
}
