package io.opensphere.core.dialog.alertviewer;

import java.awt.Dimension;
import java.util.Date;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JDialog;

import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.TableViewAutosizer;
import io.opensphere.core.util.image.IconUtil.IconType;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.converter.DateStringConverter;

/** Alert viewer dialog. */
class AlertViewerDialog extends JDialog
{
    /** The title. */
    public static final String TITLE = "Alerts";

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The alerts. */
    private final ObservableList<Alert> myAlerts;

    /** Auto-sizes the table columns. */
    private TableViewAutosizer<Alert> myTableViewAutosizer;

    /**
     * Constructor.
     *
     * @param owner The owner
     * @param alerts The alerts
     */
    public AlertViewerDialog(java.awt.Window owner, ObservableList<Alert> alerts)
    {
        super(owner, TITLE);
        setMinimumSize(new Dimension(400, 89));
        setSize(700, 305);
        setLocationRelativeTo(owner);

        myAlerts = alerts;

        final JFXPanel fxPanel = new JFXPanel();
        add(fxPanel);

        Platform.runLater(() -> fxPanel.setScene(createScene()));
    }

    @Override
    public void dispose()
    {
        super.dispose();
        myTableViewAutosizer.close();
    }

    /**
     * Creates the button panel.
     *
     * @return the button panel
     */
    private Node createButtonPanel()
    {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(4, 4, 4, 4));

        Button clearButton = FXUtilities.newIconButton("Clear Alerts", IconType.CLOSE, Color.RED);
        clearButton.setTooltip(new Tooltip("Clear all alerts"));
        clearButton.setOnAction(e -> myAlerts.clear());
        clearButton.setDisable(myAlerts.isEmpty());
        myAlerts.addListener((ListChangeListener<? super Alert>)c -> clearButton.setDisable(myAlerts.isEmpty()));

        box.getChildren().addAll(FXUtilities.newHSpacer(), clearButton);
        return box;
    }

    /**
     * Creates the list view.
     *
     * @return the node
     */
    private Node createMessageView()
    {
        TableColumn<Alert, Date> timeCol = new TableColumn<>();
        timeCol.setCellValueFactory(v -> new ReadOnlyObjectWrapper<>(new Date(v.getValue().getTime())));
        timeCol.setCellFactory(TextFieldTableCell.forTableColumn(new DateStringConverter(DateTimeFormats.TIME_FORMAT)));
        timeCol.setPrefWidth(0);

        TableColumn<Alert, io.opensphere.core.dialog.alertviewer.event.Type> severityCol = new TableColumn<>();
        severityCol.setCellValueFactory(
                v -> new ReadOnlyObjectWrapper<>(v.getValue().getLevel()));
        severityCol.setCellFactory(v -> new SeverityCell());
        severityCol.setPrefWidth(0);

        TableColumn<Alert, String> messageCol = new TableColumn<>();
        messageCol.setCellValueFactory(v -> new ReadOnlyObjectWrapper<>(v.getValue().getMessage()));

        TableView<Alert> table = new TableView<>(myAlerts);
        table.setPlaceholder(new Label());
        table.getColumns().add(timeCol);
        table.getColumns().add(severityCol);
        table.getColumns().add(messageCol);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setOnKeyTyped(e -> handleKeyTyped(e, () -> table.getSelectionModel().getSelectedItems().stream()
                .map(a -> a.toString()).collect(Collectors.joining(System.lineSeparator()))));

        table.getStylesheets().add(AlertViewerDialog.class.getResource("AlertViewerDialog.css").toExternalForm());

        myTableViewAutosizer = new TableViewAutosizer<>(table);
        myTableViewAutosizer.open();

        return table;
    }

    /**
     * Creates the scene.
     *
     * @return the scene
     */
    private Scene createScene()
    {
        BorderPane root = new BorderPane();
        root.setCenter(createMessageView());
        root.setBottom(createButtonPanel());

        return FXUtilities.addDesktopStyle(new Scene(root, 440, 330));
    }

    /**
     * Handles a key typed event.
     *
     * @param event the event
     * @param selection Supplier for the selected text
     */
    private void handleKeyTyped(KeyEvent event, Supplier<String> selection)
    {
        if (event.isControlDown() && event.getCharacter().charAt(0) + 96 == 'c')
        {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selection.get());
            clipboard.setContent(content);
        }
    }

    /** Table cell that displays the alert level. */
    private static class SeverityCell extends TableCell<Alert, io.opensphere.core.dialog.alertviewer.event.Type>
    {
        @Override
        protected void updateItem(io.opensphere.core.dialog.alertviewer.event.Type item, boolean empty)
        {
            super.updateItem(item, empty);

            if (empty || item == null)
            {
                setText(null);
                setGraphic(null);
            }
            else
            {
                Color color;
                switch (item)
                {
                    case ERROR:
                        color = Color.ORANGERED;
                        break;
                    case WARNING:
                        color = Color.YELLOW;
                        break;
                    default:
                        color = Color.WHITE;
                        break;
                }
                setTextFill(color);
                setText(item.toString());
            }
        }
    }
}
