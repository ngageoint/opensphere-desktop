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
     * @param width the int to specify the total table width.
     * @param height the int to specify the total table height.
     * @return theTable a JavaFX TableView containing the Menu Shortcuts.
     */
    @SuppressWarnings("unchecked")
    public TableView<MenuShortCut> createTableView(int width, int height)
    {
        TableView<MenuShortCut> theTable = new TableView<>();
        theTable.setSelectionModel(null);
        theTable.setMinWidth(width);
        theTable.setMinHeight(height);

        TableColumn<MenuShortCut, String> topicCol = customColumn("Menu", "topic", width);
        topicCol.setStyle("-fx-font-weight: bold;");
        TableColumn<MenuShortCut, String> controlCol = customColumn("Control", "control", width);
        TableColumn<MenuShortCut, String> keybindCol = customColumn("ShortCut", "key", width);
        theTable.getColumns().addAll(topicCol, controlCol, keybindCol);

        ObservableList<MenuShortCut> data = FXCollections.observableArrayList(new MenuShortCut("File", "", ""));
        theTable.setItems(populateMenu(data));
        return theTable;
    }

    /**
     * @param name the text to be placed inside the column.
     * @param propname the text to reference this element by.
     * @param width the integer to specicy the width of each column.
     * @return theColumn a column item which can then be added to a JavaFX
     *         TableView.
     */
    public TableColumn<MenuShortCut, String> customColumn(String name, String propname, int width)
    {
        TableColumn<MenuShortCut, String> theColumn = new TableColumn<MenuShortCut, String>(name);
        theColumn.setMinWidth(width / 3);
        theColumn.setCellValueFactory(new PropertyValueFactory<>(propname));
        theColumn.setSortable(false);

        return theColumn;
    }

    /**
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