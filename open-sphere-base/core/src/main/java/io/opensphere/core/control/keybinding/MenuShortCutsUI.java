package io.opensphere.core.control.keybinding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

/**
 * Creates a table/legend of the current Opensphere shortcut keys which pertain
 * to "File","Edit","Tools" Menus.
 */
public class MenuShortCutsUI extends HBox
{
    /**
     * Creates the region containing the UI.
     * 
     * @param width the horizontal size desired.
     * @param height the vertical size desired.
     */
    public MenuShortCutsUI(int width, int height)
    {
        setMinSize(width, height);
        setStyle("-fx-background-color : derive(-fx-base, 18%)");
        getChildren().add(createTableView(width, height));
    }

    /**
     * Creates a JavaFX TableView for the "Menu ShortCuts" tab.
     * 
     * @param width the int to specify the total table width.
     * @param height the int to specify the total table height.
     * @return theTable a JavaFX TableView containing the Menu Shortcuts.
     */
    @SuppressWarnings("unchecked")
    public TableView<MenuShortCut> createTableView(int width, int height)
    {
        TableView<MenuShortCut> theTable = new TableView<>();
        theTable.setMouseTransparent(true);
        theTable.setMinWidth(width);
        theTable.setMinHeight(height);

        theTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<MenuShortCut, String> topicCol = customColumn("Menu", "menu", 10);
        topicCol.setStyle("-fx-font-weight: bold;");
        TableColumn<MenuShortCut, String> controlCol = customColumn("Item", "item", 30);
        TableColumn<MenuShortCut, String> keybindCol = customColumn("Shortcut", "shortcut", 60);
        theTable.getColumns().addAll(topicCol, controlCol, keybindCol);
        ObservableList<MenuShortCut> data = FXCollections.observableArrayList(new MenuShortCut("File", "", ""));
        theTable.setItems(populateMenu(data));
        return theTable;
    }

    /**
     * creates a column with custom width, styling, and ability to handle
     * MenuShortCut items as elements.
     * 
     * @param name the text to be placed inside the column.
     * @param propname the text to reference this element by.
     * @param width the double to specicy the width of each column as a
     *            percentage of the whole.
     * @return theColumn a column item which can then be added to a JavaFX
     *         TableView.
     */
    public TableColumn<MenuShortCut, String> customColumn(String name, String propname, double percent)
    {
        TableColumn<MenuShortCut, String> theColumn = new TableColumn<MenuShortCut, String>(name);
        theColumn.setCellValueFactory(new PropertyValueFactory<>(propname));
        theColumn.setMaxWidth(1f * Integer.MAX_VALUE * percent);
        theColumn.setSortable(false);
        return theColumn;
    }

    /**
     * Fills in the table with all the remaining shortcuts.
     * 
     * @param data the existing observable list to append to.
     * @return final_data the finalized list.
     */
    private ObservableList<MenuShortCut> populateMenu(ObservableList<MenuShortCut> data)
    {
        ObservableList<MenuShortCut> final_data = data;
        final_data.add(new MenuShortCut("", "Open", "Ctrl - O "));
        final_data.add(new MenuShortCut("", "Quit", "Ctrl - Q "));
        final_data.add(new MenuShortCut("", "", ""));
        final_data.add(new MenuShortCut("Edit", "", ""));
        final_data.add(new MenuShortCut("", "Set Logger Levels", "Ctrl - L"));
        final_data.add(new MenuShortCut("", "Settings", "F8"));
        final_data.add(new MenuShortCut("", "", ""));
        final_data.add(new MenuShortCut("View", "", ""));
        final_data.add(new MenuShortCut("", "Alert Viewer", "Ctrl - M"));
        final_data.add(new MenuShortCut("", "", ""));
        final_data.add(new MenuShortCut("Tools", "", ""));
        final_data.add(new MenuShortCut("", "Analyze", "Ctrl + Shift - X"));
        final_data.add(new MenuShortCut("", "Icon Manager", "Ctrl + Shift - I"));
        final_data.add(new MenuShortCut("", "Styles", "Ctrl + Shift - S"));
        return data;
    }
}
