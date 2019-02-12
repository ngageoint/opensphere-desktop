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
import io.opensphere.core.util.awt.BrowserUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.net.Linker;
import io.opensphere.core.util.net.PreferencesLinkerFactory;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import javafx.collections.FXCollections;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
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
    /** The background color for selected buttons. */
    private static final String BUTTON_STYLE = "-fx-background-color: #0096C9";

    /** The currently displayed data element. */
    private DataElement myActiveDataElement;

    /** The keystroke combination to copy a cell's contents to the clipboard. */
    private final KeyCombination myCopyCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

    /** The decimal-degrees button. */
    private final Button myDDButton = new Button("DD");

    /** The degrees-minutes-seconds button. */
    private final Button myDMSButton = new Button("DMS");

    /** The decimal-degrees-minutes button. */
    private final Button myDDMButton = new Button("DDM");

    /** The military grid reference system button. */
    private final Button myMGRSButton = new Button("MGRS");

    /** The list of all DataElements that can be displayed. */
    private final List<DataElement> myElements;

    /** The list of DataElements that are currently shown. */
    private final ListView<DataElement> myElementsView = new ListView<>();

    /** The list of information being displayed about the active data element. */
    private final TableView<BaseballDataRow> myDataView = new TableView<>();

    /** The label that displays the coordinate information about the active data element */
    private final Label myCoordinates = new Label();

    /** The label that displays the current number of features shown. */
    private final Label myFeatureCount = new Label();
    
    /** URL format matcher. */
    private final Linker myLinker;

    /** Which format to display coordinates in. */
    private GeographicPositionFormat myPositionFormat = GeographicPositionFormat.DECDEG;

    /** The toolbox. */
    private final Toolbox myToolbox;

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
        add(createCoordinateButtons(), 1, 0);

        myActiveDataElement = myElements.get(0);
        myLinker = PreferencesLinkerFactory.getLinker(
        		myActiveDataElement.getDataTypeInfo().getTypeKey(),
    			myToolbox.getPreferencesRegistry()
		);
        
        setDataView();
        add(myFeatureCount, 0, 1);
        setFeatureCount(myElements.size());
        add(myCoordinates, 1, 1);
        setHgap(10);
        setVgap(10);
    }

    /**
     * Copies the currently selected cell data to the clipboard.
     *
     * @param displayMessage whether to display the message that a
     *        copy has been performed
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
            else
            {
                content.putString(baseballData.getValue().getText());
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
    		String text = baseballData.getValue().getText();

			Map<String, URL> urls = myLinker.getURLs(text, text);
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
        myDataView.getColumns().add(fieldColumn);

        TableColumn<BaseballDataRow, Text> valueColumn = new TableColumn<>("Value");
        valueColumn.setCellValueFactory(data -> data.getValue().valueProperty());
        myDataView.getColumns().add(valueColumn);

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
     * Creates the coordinate button section of the panel.
     *
     * @return the coordinate buttons node
     */
    private Node createCoordinateButtons()
    {
        HBox box = new HBox();
        myDDButton.setOnAction(e ->
        {
            myPositionFormat = GeographicPositionFormat.DECDEG;
            setCoordinates();
            setButtonStyles(myDDButton);
        });
        myDDButton.setStyle(BUTTON_STYLE);
        myDMSButton.setOnAction(e ->
        {
            myPositionFormat = GeographicPositionFormat.DMSDEG;
            setCoordinates();
            setButtonStyles(myDMSButton);
        });
        myDDMButton.setOnAction(e ->
        {
            myPositionFormat = GeographicPositionFormat.DEG_DMIN;
            setCoordinates();
            setButtonStyles(myDDMButton);
        });
        myMGRSButton.setOnAction(e ->
        {
            myPositionFormat = GeographicPositionFormat.MGRS;
            setCoordinates();
            setButtonStyles(myMGRSButton);
        });
        box.getChildren().addAll(myDDButton, myDMSButton, myDDMButton, myMGRSButton);
        return box;
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
        if (value == null)
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
            returnValue = BaseballUtils.formatDate((Date)value, myToolbox.getPreferencesRegistry().getPreferences(ListToolPreferences.class)
                    .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0));
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
    
    private Text getValueAsTextObject(String value)
    {
    	Text returnValue = new Text();
    	returnValue.setText(value);
    	returnValue.setFill(Color.WHITE);
    	
    	if (myLinker != null && myLinker.hasURLFor(value, value))
        {
        	returnValue.setFill(Color.BLUE);
        	returnValue.setUnderline(true);
        	returnValue.setOnMouseMoved(event -> getScene().setCursor(Cursor.HAND));
        	returnValue.setOnMouseExited(event -> getScene().setCursor(Cursor.DEFAULT));
        }
    	
    	return returnValue;
    }

    /**
     * Clears the styles for the coordinate buttons, then sets the style for selected button.
     *
     * @param button the button
     */
    private void setButtonStyles(Button button)
    {
        myDDButton.setStyle("");
        myDMSButton.setStyle("");
        myDDMButton.setStyle("");
        myMGRSButton.setStyle("");
        button.setStyle(BUTTON_STYLE);
    }

    /**
     * Sets the coordinate label based off the currently active data element
     * and the selected coordinate format.
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
        }
        else
        {
            List<BaseballDataRow> data = New.list();
            GeographicPositionFormat tempFormat = myPositionFormat;
            myPositionFormat = GeographicPositionFormat.DECDEG;
            myActiveDataElement.getMetaData().getKeys().stream().forEach(key ->
            {
                Object elementValue = myActiveDataElement.getMetaData().getValue(key);
                String pairValue = getValueAsString(key, elementValue, myActiveDataElement);
                data.add(new BaseballDataRow(key, pairValue == null ? new Text() : getValueAsTextObject(pairValue)));
            });
            myDataView.setItems(FXCollections.observableList(data));
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
     * Filters DataElements based on if any value in the DataElement contains the filter.
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
}
