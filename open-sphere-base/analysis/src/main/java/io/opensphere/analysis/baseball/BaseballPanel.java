package io.opensphere.analysis.baseball;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.analysis.baseball.BaseballUtils.CoordType;
import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.GeographicPositionFormat;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.awt.BrowserUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.net.Linker;
import io.opensphere.core.util.net.PreferencesLinkerFactory;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * The main panel for the baseball dialog.
 */
public class BaseballPanel extends GridPane
{
    /** The image tag prefix. */
    private static final String IMAGE_PREFIX = "<img src=\"";

    /** The image tag suffix. */
    private static final String IMAGE_SUFFIX = "\"/>";

    /** The border for the currently selected coordinate label. */
    private static final Border MAIN_COORDINATE_BORDER = new Border(new BorderStroke(FXUtilities.fromAwtColor(Colors.OPENSPHERE_LIGHT),
            BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(3)));

    /** The border for non-active, mouse-hovered coordinate labels. */
    private static final Border SECONDARY_COORDINATE_BORDER = new Border(new BorderStroke(FXUtilities.fromAwtColor(Colors.OPENSPHERE_LIGHT),
            BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1.5)));

    /**
     * The border for non-active, non-mouse-hovered coordinate labels. Matches
     * the background color of the dialog. Used to prevent constant minor
     * resizing of label space when hovering over them.
     */
    private static final Border INVISIBLE_COORDINATE_BORDER = new Border(new BorderStroke(FXUtilities.fromAwtColor(Colors.LF_SECONDARY3),
            BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1.5)));

    /** The currently displayed data element. */
    private DataElement myActiveDataElement;

    /** The keystroke combination to copy a cell's contents to the clipboard. */
    private final KeyCombination myCopyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

    /** The currently selected label that describes the coordinate style. */
    private Label mySelectedCoordinateLabel;

    /** The list of all DataElements that can be displayed. */
    private final List<DataElement> myElements;

    /** The list of DataElements that are currently shown. */
    private final ListView<DataElement> myElementsView = new ListView<>();

    /**
     * The list of information being displayed about the active data element.
     */
    private final TableView<BaseballDataRow> myDataView = new TableView<>();

    /**
     * The label that displays the coordinate information about the active data
     * element.
     */
    private final Label myCoordinates = new Label();

    /** The list of listeners for displaying images at the correct size. */
    private final List<ImageListener> myListenerList = New.list();

    /**
     * The Label that displays which layer the currently selected feature is
     * from.
     */
    private final Label myLayer = new Label();

    /** The label that displays the current number of features shown. */
    private final Label myFeatureCount = new Label();

    /** URL format matcher. */
    private final Linker myLinker;

    /** Which format to display coordinates in. */
    private GeographicPositionFormat myPositionFormat = GeographicPositionFormat.DECDEG;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The TableColumn for the half of the table representing values. */
    private final TableColumn<BaseballDataRow, Object> myValueColumn = new TableColumn<>("Value");

    /**
     * Constructs and initializes a new panel.
     *
     * @param toolbox the toolbox
     * @param elements the list of elements
     */
    public BaseballPanel(Toolbox toolbox, List<DataElement> elements)
    {
        myToolbox = toolbox;
        myElements = sortDataElementsByTime(elements);
        add(createDataElementList(), 0, 2);
        add(createSearchBar(), 0, 0);
        add(createDataArea(), 1, 2);
        add(myLayer, 1, 0);
        GridPane.setHalignment(myLayer, HPos.LEFT);

        myActiveDataElement = myElements.get(0);
        myLinker = PreferencesLinkerFactory.getLinker(myActiveDataElement.getDataTypeInfo().getTypeKey(),
                myToolbox.getPreferencesRegistry());

        setDataView();
        add(myFeatureCount, 0, 1);
        setFeatureCount(myElements.size());
        add(createCoordinateArea(), 1, 1);
        setHgap(10);
        setVgap(10);
    }

    /**
     * Copies the currently selected cell data to the clipboard.
     *
     * @param displayMessage whether to display the message that a copy has been
     *            performed
     */
    private void copyCellToClipboard(boolean displayMessage)
    {
        BaseballDataRow baseballData = myDataView.getFocusModel().getFocusedItem();
        if (baseballData != null)
        {
            @SuppressWarnings("rawtypes")
            TablePosition position = myDataView.getFocusModel().getFocusedCell();
            ClipboardContent content = new ClipboardContent();
            if (position.getColumn() == 0)
            {
                content.putString(baseballData.getField());
            }
            else if (baseballData.getValue() instanceof ImageView)
            {
                content.putString(((ImageView)baseballData.getValue()).getImage().getUrl());
            }
            else
            {
                content.putString(((Text)baseballData.getValue()).getText());
            }
            Clipboard.getSystemClipboard().setContent(content);
            if (displayMessage)
            {
                Notify.info("Copied value to clipboard: " + content.getString(), Method.POPUP);
            }
        }
    }

    /**
     * Navigates to a URL when clicked.
     */
    private void fireUrlClickEvent()
    {
        if (myLinker != null)
        {
            BaseballDataRow baseballData = myDataView.getFocusModel().getFocusedItem();
            if (baseballData != null && baseballData.getValue() instanceof Text)
            {
                String key = baseballData.getField();
                String text = ((Text)baseballData.getValue()).getText();

                Map<String, URL> urls = myLinker.getURLs(key, text);
                if (!urls.isEmpty())
                {
                    for (URL url : urls.values())
                    {
                        if (url != null)
                        {
                            BrowserUtilities.browse(url, null);
                            return;
                        }
                    }
                    Notify.info("Could not open URL: " + text, Method.POPUP);
                }
            }
        }
    }

    /**
     * Creates the DataElement list section of the panel.
     *
     * @return the DataElement node
     */
    private Node createDataElementList()
    {
        myElementsView.setCellFactory((param) ->
        {
            BaseballElementRow row = new BaseballElementRow();
            row.setOnMousePressed(e ->
            {
                if (row.getDataElement() != null)
                {
                    myActiveDataElement = row.getDataElement();
                    setDataView();
                }
            });
            return row;
        });

        myElementsView.setItems(FXCollections.observableList(myElements));
        GridPane.setVgrow(myElementsView, Priority.ALWAYS);
        return myElementsView;
    }

    /**
     * Creates the data information section of the panel.
     *
     * @return the data information node
     */
    private Node createDataArea()
    {
        TableColumn<BaseballDataRow, String> fieldColumn = new TableColumn<>("Field");
        fieldColumn.setCellValueFactory(data -> data.getValue().fieldProperty());
        fieldColumn.setReorderable(false);
        myDataView.getColumns().add(fieldColumn);

        myValueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
        myValueColumn.setReorderable(false);
        myDataView.getColumns().add(myValueColumn);

        myDataView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        myDataView.getSelectionModel().setCellSelectionEnabled(true);

        myDataView.setOnMouseClicked(event ->
        {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
            {
                copyCellToClipboard(true);
            }
            fireUrlClickEvent();
        });

        myDataView.setOnKeyPressed(event ->
        {
            if (myCopyCombination.match(event))
            {
                copyCellToClipboard(false);
            }
        });

        myDataView.setOnSort(event ->
        {
            myDataView.getSelectionModel().clearSelection();
        });

        GridPane.setHgrow(myDataView, Priority.ALWAYS);
        GridPane.setVgrow(myDataView, Priority.ALWAYS);
        return myDataView;
    }

    /**
     * Creates the coordinate area section of the panel.
     *
     * @return the coordinate area node
     */
    private Node createCoordinateArea()
    {
        myCoordinates.setOnMouseClicked(e ->
        {
            if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2)
            {
                ClipboardContent content = new ClipboardContent();
                content.putString(myCoordinates.textProperty().get());
                Clipboard.getSystemClipboard().setContent(content);
                Notify.info("Copied value to clipboard: " + content.getString(), Method.POPUP);
            }
        });

        GridPane gridPane = new GridPane();
        HBox buttonBox = new HBox(8);

        Label ddLabel = new Label("DD");
        mySelectedCoordinateLabel = ddLabel;
        ddLabel.setBorder(MAIN_COORDINATE_BORDER);
        setupCoordinateLabelFunctions(ddLabel, GeographicPositionFormat.DECDEG);

        Label dmsLabel = new Label("DMS");
        dmsLabel.setBorder(INVISIBLE_COORDINATE_BORDER);
        setupCoordinateLabelFunctions(dmsLabel, GeographicPositionFormat.DMSDEG);

        Label ddmLabel = new Label("DDM");
        ddmLabel.setBorder(INVISIBLE_COORDINATE_BORDER);
        setupCoordinateLabelFunctions(ddmLabel, GeographicPositionFormat.DEG_DMIN);

        Label mgrsLabel = new Label("MGRS");
        mgrsLabel.setBorder(INVISIBLE_COORDINATE_BORDER);
        setupCoordinateLabelFunctions(mgrsLabel, GeographicPositionFormat.MGRS);

        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(ddLabel, dmsLabel, ddmLabel, mgrsLabel);
        gridPane.add(myCoordinates, 0, 0);
        gridPane.add(buttonBox, 1, 0);
        GridPane.setHgrow(buttonBox, Priority.ALWAYS);
        return gridPane;
    }

    /**
     * Creates the search bar section of the panel.
     *
     * @return the search bar node
     */
    private Node createSearchBar()
    {
        TextField search = new TextField();
        search.setPromptText("search features");
        search.setOnKeyTyped(e ->
        {
            if (StringUtils.isEmpty(search.getText()))
            {
                myElementsView.setItems(FXCollections.observableList(myElements));
                myActiveDataElement = myElements.get(0);
                setDataView();
                setFeatureCount(myElements.size());
                return;
            }

            List<DataElement> filteredDataList = searchFilterDataElements(search.getText());
            myElementsView.setItems(FXCollections.observableList(filteredDataList));
            if (filteredDataList.size() > 0)
            {
                myActiveDataElement = filteredDataList.get(0);
                setDataView();
                setFeatureCount(filteredDataList.size());
            }
            else
            {
                myActiveDataElement = null;
                setDataView();
                setFeatureCount(0);
            }
        });
        search.setMaxWidth(250);
        search.setPrefWidth(250);

        return search;
    }

    /**
     * Gets a String representation of the value.
     *
     * @param key the key name
     * @param value the value
     * @param dataElement the DataElement
     * @return the String representation of the value
     */
    private String getValueAsString(String key, Object value, DataElement dataElement)
    {
        String returnValue;

        if (key.equals("TIME"))
        {
            // this check is needed since the TIME metadata element isn't always set,
            // and can alternately be retrieved through the TimeSpan of the element
            returnValue = dataElement.getTimeSpan().toDisplayString();
        }
        else if (value == null)
        {
            returnValue = null;
        }
        else if (value instanceof Double)
        {
            SpecialKey specialType = dataElement.getDataTypeInfo().getMetaDataInfo().getSpecialTypeForKey(key);
            if (specialType instanceof LatitudeKey)
            {
                returnValue = BaseballUtils.formatCoordinate((Double)value, myPositionFormat, CoordType.LAT);
            }
            else if (specialType instanceof LongitudeKey)
            {
                returnValue = BaseballUtils.formatCoordinate((Double)value, myPositionFormat, CoordType.LON);
            }
            else
            {
                returnValue = BaseballUtils.formatNumber((Number)value);
            }
        }
        else if (value instanceof Number)
        {
            returnValue = BaseballUtils.formatNumber((Number)value);
        }
        else if (value instanceof Date)
        {
            returnValue = BaseballUtils.formatDate((Date)value, myToolbox.getPreferencesRegistry()
                    .getPreferences(ListToolPreferences.class).getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0));
        }
        else if (value instanceof TimeSpan)
        {
            returnValue = BaseballUtils.formatTimeSpan((TimeSpan)value, myToolbox.getPreferencesRegistry()
                    .getPreferences(ListToolPreferences.class).getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0));
        }
        else
        {
            returnValue = value.toString();
        }
        return returnValue;
    }

    /**
     * Turns the given string into a JavaFX Text object, and attaches hyperlink
     * functionality to it if applicable.
     *
     * @param key the name of the field with which the value is associated.
     * @param value the string to turn into a Text
     * @return the value as a Text object
     */
    private Text getValueAsTextObject(String key, String value)
    {
        Text returnValue = new Text();
        returnValue.setText(value);
        returnValue.setFill(Color.WHITE);

        if (myLinker != null && myLinker.hasURLFor(key, value))
        {
            returnValue.setFill(Color.web("#FFFF33"));
            returnValue.setUnderline(true);
            returnValue.setOnMouseEntered(event -> getScene().setCursor(Cursor.HAND));
            returnValue.setOnMouseExited(event -> getScene().setCursor(Cursor.DEFAULT));
        }

        return returnValue;
    }

    /**
     * Turns the old coordinate label border invisible, then sets the border for
     * selected label to the main border.
     *
     * @param label the newly selected label
     */
    private void setCoordinateSelectionBorders(Label label)
    {
        mySelectedCoordinateLabel.setBorder(INVISIBLE_COORDINATE_BORDER);
        label.setBorder(MAIN_COORDINATE_BORDER);
        mySelectedCoordinateLabel = label;
    }

    /**
     * Sets up several mouse functions for the given coordinate label.
     *
     * @param label the coordinate label to give functionality
     * @param format the geographic position format associated with the label
     */
    private void setupCoordinateLabelFunctions(Label label, GeographicPositionFormat format)
    {
        label.setOnMouseClicked(e ->
        {
            myPositionFormat = format;
            setCoordinates();
            setCoordinateSelectionBorders(label);
        });

        label.setOnMouseEntered(e ->
        {
            if (!label.equals(mySelectedCoordinateLabel))
            {
                label.setBorder(SECONDARY_COORDINATE_BORDER);
            }
        });

        label.setOnMouseExited(e ->
        {
            if (!label.equals(mySelectedCoordinateLabel))
            {
                label.setBorder(INVISIBLE_COORDINATE_BORDER);
            }
        });
    }

    /**
     * Sets the coordinate label based off the currently active data element and
     * the selected coordinate format.
     */
    private void setCoordinates()
    {
        if (myActiveDataElement == null)
        {
            myCoordinates.setText("");
        }
        else if (myPositionFormat == GeographicPositionFormat.MGRS)
        {
            myCoordinates.setText(BaseballUtils.formatMGRS(myActiveDataElement));
        }
        else
        {
            String latitudeKey = myActiveDataElement.getDataTypeInfo().getMetaDataInfo().getLatitudeKey();
            Object latitudeValue = myActiveDataElement.getMetaData().getValue(latitudeKey);
            String latitude = getValueAsString(latitudeKey, latitudeValue, myActiveDataElement);

            String longitudeKey = myActiveDataElement.getDataTypeInfo().getMetaDataInfo().getLongitudeKey();
            Object longitudeValue = myActiveDataElement.getMetaData().getValue(longitudeKey);
            String longitude = getValueAsString(longitudeKey, longitudeValue, myActiveDataElement);

            myCoordinates.setText(latitude + " " + longitude);
        }
    }

    /**
     * Sets the data information section based off the active data element.
     */
    private void setDataView()
    {
        if (myActiveDataElement == null)
        {
            myDataView.setItems(null);
            myLayer.setText("");
        }
        else
        {
            List<BaseballDataRow> data = New.list();
            GeographicPositionFormat tempFormat = myPositionFormat;
            myPositionFormat = GeographicPositionFormat.DECDEG;
            myListenerList.forEach(e -> myValueColumn.widthProperty().removeListener(e));
            myListenerList.clear();
            myActiveDataElement.getMetaData().getKeys().stream().filter(key -> !key.equals(MetaDataInfo.MGRS_DERIVED)).forEach(key ->
            {
                Object elementValue = myActiveDataElement.getMetaData().getValue(key);
                String pairValue = getValueAsString(key, elementValue, myActiveDataElement);
                if (pairValue != null && StringUtils.startsWith(pairValue, IMAGE_PREFIX))
                {
                    String imagePath = StringUtils.substringBetween(pairValue, IMAGE_PREFIX, IMAGE_SUFFIX);
                    Image image = new Image(imagePath);
                    ImageView imageView = new ImageView(image);
                    imageView.setPreserveRatio(true);

                    if (image.getWidth() > myValueColumn.getWidth() - 8)
                    {
                        imageView.setFitWidth(myValueColumn.getWidth() - 8);
                    }

                    ImageListener listener = new ImageListener(image, imageView);
                    myListenerList.add(listener);
                    myValueColumn.widthProperty().addListener(listener);

                    data.add(new BaseballDataRow(key, imageView));
                }
                else
                {
                    data.add(new BaseballDataRow(key, pairValue == null ? new Text() : getValueAsTextObject(key, pairValue)));
                }
            });
            myDataView.setItems(FXCollections.observableList(data));
            myLayer.setText(myActiveDataElement.getDataTypeInfo().getDisplayName());
            myPositionFormat = tempFormat;
        }
        setCoordinates();
    }

    /**
     * Sets the number of features label.
     *
     * @param numberOfFeatures the number of features
     */
    private void setFeatureCount(int numberOfFeatures)
    {
        myFeatureCount.setText(numberOfFeatures + " Features");
    }

    /**
     * Filters DataElements based on if any value in the DataElement contains
     * the filter.
     *
     * @param filter the filter
     * @return the time-based sorted list of filtered DataElements
     */
    private List<DataElement> searchFilterDataElements(String filter)
    {
        List<DataElement> filteredList = New.list();
        myElements.stream().forEach(e -> e.getMetaData().getKeys().stream().forEach(key ->
        {
            if (StringUtils.containsIgnoreCase(getValueAsString(key, e.getMetaData().getValue(key), e), filter)
                    && !filteredList.contains(e))
            {
                filteredList.add(e);
            }
        }));
        return sortDataElementsByTime(filteredList);
    }

    /**
     * Sorts the list of DataElements by their timespan, latest first.
     *
     * @param elements the list of DataElements
     * @return the sorted list of DataElements
     */
    private List<DataElement> sortDataElementsByTime(List<DataElement> elements)
    {
        return elements.stream().sorted((f, s) -> s.getTimeSpan().compareTo(f.getTimeSpan())).collect(Collectors.toList());
    }

    /**
     * The listener used for resizing images in the data fields.
     */
    private class ImageListener implements ChangeListener<Number>
    {
        /** The Image the listener is associated with. */
        private final Image myImage;

        /** The ImageView the listener is associated with. */
        private final ImageView myImageView;

        /**
         * Constructs a new ImageListener with the given Image and ImageView
         *
         * @param image the image
         * @param imageView the imageview
         */
        public ImageListener(Image image, ImageView imageView)
        {
            myImage = image;
            myImageView = imageView;
        }

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
        {
            if (myImage.getWidth() > newValue.doubleValue() - 8)
            {
                myImageView.setFitWidth(newValue.doubleValue() - 8);
            }
        }
    }
}
