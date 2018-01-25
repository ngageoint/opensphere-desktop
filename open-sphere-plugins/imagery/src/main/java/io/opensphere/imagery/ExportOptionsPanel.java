package io.opensphere.imagery;

import java.awt.Dimension;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

import com.sun.javafx.application.PlatformImpl;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.gui.LayerListView;

/** The export options panel. */
public class ExportOptionsPanel extends JFXPanel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The layer picker list view. */
    private LayerListView myLayerPicker;

    /** The max zoom level combo. */
    private ComboBox<Integer> myMaxZoomCombo;

    /**
     * Constructor.
     *
     * @param dataTypes the data type options
     */
    public ExportOptionsPanel(Collection<? extends DataTypeInfo> dataTypes)
    {
        super();
        setPreferredSize(new Dimension(300, 300));
        Platform.runLater(() -> setScene(createScene(dataTypes)));
    }

    /**
     * Gets the selections. This can be called from any thread.
     *
     * @return the selections
     */
    public Collection<DataTypeInfo> getSelections()
    {
        return myLayerPicker.getSelections();
    }

    /**
     * Gets the max zoom level. This can be called from any thread.
     *
     * @return the max zoom level
     */
    public int getMaxZoomLevel()
    {
        final AtomicInteger level = new AtomicInteger();
        PlatformImpl.runAndWait(() -> level.set(myMaxZoomCombo.getSelectionModel().getSelectedItem().intValue()));
        return level.get();
    }

    /**
     * Creates the JavaFX scene.
     *
     * @param dataTypes the data type options
     * @return the scene
     */
    private Scene createScene(Collection<? extends DataTypeInfo> dataTypes)
    {
        BorderPane pane = new BorderPane();

        pane.setTop(new Label("Select Layers to Export:"));
        BorderPane.setMargin(pane.getTop(), new Insets(0, 0, 5, 0));

        myLayerPicker = new LayerListView(dataTypes);
        pane.setCenter(myLayerPicker);

        List<Integer> zoomLevelOptions = IntStream.range(0, 23).mapToObj(i -> Integer.valueOf(i)).collect(Collectors.toList());
        myMaxZoomCombo = new ComboBox<Integer>(FXCollections.observableArrayList(zoomLevelOptions));
        myMaxZoomCombo.getSelectionModel().select(zoomLevelOptions.get(zoomLevelOptions.size() - 1));
        myMaxZoomCombo.setTooltip(new Tooltip("The maximum tile zoom level. A lower number will result in a smaller file."));
        pane.setBottom(FXUtilities.newHBox(new Label("Max split level:"), myMaxZoomCombo));
        BorderPane.setMargin(pane.getBottom(), new Insets(5, 0, 0, 0));

        pane.setPadding(new Insets(5));
        return FXUtilities.addDesktopStyle(new Scene(pane));
    }
}
