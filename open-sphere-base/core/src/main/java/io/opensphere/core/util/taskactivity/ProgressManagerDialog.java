package io.opensphere.core.util.taskactivity;

import java.awt.Window;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jcip.annotations.ThreadSafe;
import javax.swing.JDialog;

import io.opensphere.core.util.ThreadConfined;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/**
 * A dialog that can display the running tasks and allow the user to take action
 * on them.
 */
@ThreadSafe
public class ProgressManagerDialog extends JDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Flag indicating if the display tasks need to be refreshed. */
    private final AtomicBoolean myDirty = new AtomicBoolean();

    /** The displayed tasks. */
    @ThreadConfined("JavaFX")
    private final ObservableList<TaskActivity> myDisplayTasks = FXCollections.observableArrayList();

    /** The table view. */
    @ThreadConfined("JavaFX")
    private TableView<TaskActivity> myTable;

    /** The tasks. */
    @ThreadConfined("JavaFX")
    private final ObservableList<TaskActivity> myTasks = FXCollections.observableArrayList();

    /**
     * Constructor.
     *
     * @param parent The parent window.
     */
    public ProgressManagerDialog(Window parent)
    {
        super(parent);

        setSize(400, 200);
        setLocationRelativeTo(parent);
        setTitle("Progress");

        JFXPanel panel = new JFXPanel();
        setContentPane(panel);

        Platform.runLater(() -> initPanel(panel));
    }

    /**
     * Adds the task.
     *
     * @param ta The task activity.
     */
    public void addTask(TaskActivity ta)
    {
        Platform.runLater(() -> myTasks.add(ta));
    }

    /**
     * Adds the tasks.
     *
     * @param taskActivities The task activities.
     */
    public void addTasks(Collection<? extends TaskActivity> taskActivities)
    {
        // Make a copy to send to the FX thread.
        Collection<? extends TaskActivity> copy = New.collection(taskActivities);
        Platform.runLater(() -> myTasks.addAll(copy));
    }

    /**
     * Refresh the displayed tasks at a later time.
     */
    public final void refresh()
    {
        myDirty.set(true);
        Platform.runLater(this::refreshNow);
    }

    /**
     * Removes the task.
     *
     * @param taskActivity The task activity.
     */
    public void removeTask(TaskActivity taskActivity)
    {
        Platform.runLater(() -> myTasks.remove(taskActivity));
    }

    /**
     * Initialize the JavaFX panel.
     *
     * @param panel The panel.
     */
    private void initPanel(JFXPanel panel)
    {
        TableColumn<TaskActivity, Void> cancelCol = new TableColumn<>();
        cancelCol.setCellFactory(data -> new CancelTableCell());
        cancelCol.setPrefWidth(30);

        TableColumn<TaskActivity, String> textCol = new TableColumn<>();
        textCol.setCellValueFactory(data -> data.getValue().labelProperty());
        textCol.setPrefWidth(200);

        TableColumn<TaskActivity, Double> progressCol = new TableColumn<>();
        progressCol.setCellFactory(ProgressBarTableCell.forTableColumn());
        progressCol.setCellValueFactory(new PropertyValueFactory<TaskActivity, Double>("progress")
        {
            @Override
            public ObservableValue<Double> call(CellDataFeatures<TaskActivity, Double> param)
            {
                return param.getValue() instanceof CancellableTaskActivity ? super.call(param) : null;
            }
        });

        myTable = new TableView<>(myDisplayTasks);
        myTable.setPlaceholder(new Label());
        myTable.getColumns().add(cancelCol);
        myTable.getColumns().add(textCol);
        myTable.getColumns().add(progressCol);
        myTable.setSelectionModel(null);

        progressCol.setResizable(false);

        myTable.getStylesheets().add(ProgressManagerDialog.class.getResource("ProgressManagerDialog.css").toExternalForm());

        textCol.prefWidthProperty().bind(myTable.widthProperty().subtract(135));
        progressCol.prefWidthProperty().bind(myTable.widthProperty().subtract(textCol.widthProperty()).subtract(35));
        cancelCol.prefWidthProperty().bind(
                myTable.widthProperty().subtract(textCol.widthProperty()).subtract(progressCol.widthProperty()).subtract(5));

        BorderPane root = new BorderPane(myTable);

        Scene scene = new Scene(root, 300, 400);
        FXUtilities.addDesktopStyle(scene);

        panel.setScene(scene);

        myTasks.addListener((InvalidationListener)o -> refresh());
    }

    /**
     * Refresh the display tasks if the dirty flag is set.
     */
    private void refreshNow()
    {
        assert Platform.isFxApplicationThread();

        if (myDirty.compareAndSet(true, false))
        {
            /* Work around a bug in FX that causes an NPE. */
            myTable.getFocusModel().focus(-1);

            myDisplayTasks.setAll(myTasks.filtered(a -> a.isActive() && !a.isComplete()
                    && !(a instanceof CancellableTaskActivity && ((CancellableTaskActivity)a).isCancelled())));
        }
    }

    /**
     * Table cell that contains the cancel button.
     */
    private static final class CancelTableCell extends TableCell<TaskActivity, Void>
    {
        /** The delete button. */
        private final Button myDeleteButton;

        /** Constructor. */
        public CancelTableCell()
        {
            myDeleteButton = FXUtilities.newIconButton(IconType.CLOSE, Color.RED);
            myDeleteButton.setTooltip(new Tooltip("Cancel"));
            myDeleteButton.setOnAction(e -> ((CancellableTaskActivity)getTableRow().getItem()).setCancelled(true));
        }

        @Override
        protected void updateItem(Void item, boolean empty)
        {
            super.updateItem(item, empty);

            setText(null);

            if (getTableRow().getItem() instanceof CancellableTaskActivity)
            {
                setGraphic(myDeleteButton);
            }
            else
            {
                setGraphic(null);
            }
        }
    }
}
