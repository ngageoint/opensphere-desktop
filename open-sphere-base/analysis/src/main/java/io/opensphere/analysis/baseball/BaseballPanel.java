package io.opensphere.analysis.baseball;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.analysis.baseball.BaseballUtils.CoordType;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.GeographicPositionFormat;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class BaseballPanel extends GridPane
{
    private static final String BUTTON_STYLE = "-fx-background-color: #0096C9";

	private DataElement myActiveDataElement;

    private final Button myDDButton = new Button("DD");

    private final Button myDMSButton = new Button("DMS");

    private final Button myDDMButton = new Button("DDM");

    private final Button myMGRSButton = new Button("MGRS");

    private final List<DataElement> myElements;

    private final ListView<DataElement> myElementsView = new ListView<>();

    private final ListView<Pair<String, String>> myDataView = new ListView<>();

    private final Label myCoordinates = new Label();

    private final Label myFeatureCount = new Label();

    private GeographicPositionFormat myPositionFormat = GeographicPositionFormat.DECDEG;

    private final Toolbox myToolbox;

    public BaseballPanel(Toolbox toolbox, List<DataElement> elements)
    {
        myToolbox = toolbox;
        myElements = elements.stream().sorted((f, s) -> s.getTimeSpan().compareTo(f.getTimeSpan())).collect(Collectors.toList());
        add(createElementList(), 0, 2);
        add(createSearchBar(), 0, 0);
        add(createDataArea(), 1, 2);
        add(createTopRight(), 1, 0);
        myActiveDataElement = myElements.get(0);
        setDataView();
        add(myFeatureCount, 0, 1);
        setFeatureCount(myElements.size());
        add(myCoordinates, 1, 1);
        setHgap(10);
        setVgap(10);
    }

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

    private void setDataView()
    {
        if (myActiveDataElement == null)
        {
            myDataView.setItems(null);
        }
        else
        {
            List<Pair<String, String>> data = New.list();
            GeographicPositionFormat tempFormat = myPositionFormat;
            myPositionFormat = GeographicPositionFormat.DECDEG;
            data.add(new Pair<>("Field", "Value"));
            myActiveDataElement.getMetaData().getKeys().stream().forEach(key ->
            {
                Object elementValue = myActiveDataElement.getMetaData().getValue(key);
                String pairValue = getValueAsString(key, elementValue, myActiveDataElement);
            	data.add(new Pair<>(key, pairValue));
            });
//            for (String key : myActiveDataElement.getMetaData().getKeys())
//            {
//                Object value = myActiveDataElement.getMetaData().getValue(key);
//                String secondElement = getValueAsString(key, value, myActiveDataElement);
//            	data.add(new Pair<>(key, secondElement));
//            }
            myDataView.setItems(FXCollections.observableList(data));
            myPositionFormat = tempFormat;
//            System.out.println(dataElement.getDataTypeInfo().getMetaDataInfo().getSpecialKeyToTypeMap());
        }
        setCoordinates();
	}

    private void setFeatureCount(int numberOfFeatures)
    {
        myFeatureCount.setText(numberOfFeatures + " Features");
    }

    private Node createElementList()
    {
        myElementsView.setCellFactory((param) ->
        {
        	BaseballTimeRow row = new BaseballTimeRow();
        	row.setOnMousePressed(e ->
        	{
        	    myActiveDataElement = row.getDataElement();
        	    setDataView();
        	});
        	return row;
        });
        
        myElementsView.setItems(FXCollections.observableList(myElements));
        GridPane.setVgrow(myElementsView, Priority.ALWAYS);
        return myElementsView;
    }

    private Node createSearchBar()
    {
        TextField search = new TextField();
        search.setPromptText("search features");
        search.setOnKeyTyped(e -> 
        {
            List<DataElement> filteredDataList = myElements.stream().filter(t ->
                    StringUtils.containsIgnoreCase(t.getTimeSpan().toDisplayString(), search.getText())).sorted((f, s) ->
                    s.getTimeSpan().compareTo(f.getTimeSpan())).collect(Collectors.toList());
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

    private Node createDataArea()
    {
        myDataView.setCellFactory((param) ->
        {
        	BaseballDataRow row = new BaseballDataRow();
        	return row;
        });
        GridPane.setHgrow(myDataView, Priority.ALWAYS);
        GridPane.setVgrow(myDataView, Priority.ALWAYS);
        return myDataView;
    }

    private Node createTopRight()
    {
        HBox box = new HBox();
        myDDButton.setOnAction(e ->
        {
            myPositionFormat = GeographicPositionFormat.DECDEG;
            setCoordinates();
            clearButtonStyles();
            myDDButton.setStyle(BUTTON_STYLE);
        });
        myDDButton.setStyle(BUTTON_STYLE);
        myDMSButton.setOnAction(e ->
        {
            myPositionFormat = GeographicPositionFormat.DMSDEG;
            setCoordinates();
            clearButtonStyles();
            myDMSButton.setStyle(BUTTON_STYLE);
        });
        myDDMButton.setOnAction(e ->
        {
            myPositionFormat = GeographicPositionFormat.DEG_DMIN;
            setCoordinates();
            clearButtonStyles();
            myDDMButton.setStyle(BUTTON_STYLE);
        });
        myMGRSButton.setOnAction(e ->
        {
            myPositionFormat = GeographicPositionFormat.MGRS;
            setCoordinates();
            clearButtonStyles();
            myMGRSButton.setStyle(BUTTON_STYLE);
        });
        box.getChildren().addAll(myDDButton, myDMSButton, myDDMButton, myMGRSButton);
        return box;
    }

    /**
     * Gets the special key for the given row.
     *
     * @param rowIndex the row index
     * @return the specialKey
     */
    private SpecialKey getSpecialKey(String key, DataElement dataElement)
    {
        return dataElement.getDataTypeInfo().getMetaDataInfo().getSpecialTypeForKey(key);
    }

    private String getValueAsString(String key, Object value, DataElement dataElement)
    {
        String returnValue;
    	if (value == null)
        {
            returnValue = null;
        }
        else if (value instanceof Double)
        {
            SpecialKey specialType = getSpecialKey(key, dataElement);
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

    private void clearButtonStyles()
    {
        myDDButton.setStyle("");
        myDMSButton.setStyle("");
        myDDMButton.setStyle("");
        myMGRSButton.setStyle("");
    }
}
