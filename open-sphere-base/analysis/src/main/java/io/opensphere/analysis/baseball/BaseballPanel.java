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
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class BaseballPanel extends BorderPane
{
    private final List<DataElement> myElements;

    private ListView<DataElement> myElementsView;

    private ListView<Pair<String, String>> myDataView;

    private final Toolbox myToolbox;

    public BaseballPanel(Toolbox toolbox, List<DataElement> elements)
    {
        myToolbox = toolbox;
        myElements = elements;
        createLeftPane();
        myElementsView.setItems(FXCollections.observableList(myElements));
        createTopPane();
//        setRight(new BaseballTableModel(elements.get(0), toolbox.getPreferencesRegistry()));
        createRightPane();
//        myDataView.setItems(FXCollections.observableList(Collections.singletonList(elements.get(0))));
        setDataView(elements.get(0));
    }

    private void setDataView(DataElement dataElement)
    {
        if (dataElement == null)
        {
            myDataView.setItems(null);
        }
        else
        {
            List<Pair<String, String>> data = New.list();
            data.add(new Pair<>("Field", "Value"));
            for (String key : dataElement.getMetaData().getKeys())
            {
                Object value = dataElement.getMetaData().getValue(key);
                String secondElement = getValueAsString(key, value, dataElement);
            	data.add(new Pair<>(key, secondElement));
            } 
            myDataView.setItems(FXCollections.observableList(data));
//            System.out.println(dataElement.getDataTypeInfo().getMetaDataInfo().getSpecialKeyToTypeMap());
        }
	}

	private void createLeftPane()
    {
        setLeft(createElementList());
    }

    private void createTopPane()
    {
        setTop(createSearchBar());
    }
    
    private void createRightPane()
    {
        setCenter(createDataArea());
    }

    private Node createElementList()
    {
        myElementsView = new ListView<>();
        myElementsView.setCellFactory((param) ->
        {
        	BaseballTimeRow row = new BaseballTimeRow();
        	row.setOnMousePressed(e -> setDataView(row.getDataElement()));
        	return row;
        });
        return myElementsView;
    }

    private Node createSearchBar()
    {
        TextField search = new TextField();
        search.setPromptText("search features");
        search.setOnKeyTyped(e -> 
        {
            List<DataElement> filteredDataList = myElements.stream().filter(t ->
                    StringUtils.contains(t.getTimeSpan().toDisplayString(), search.getText())).collect(Collectors.toList());
            myElementsView.setItems(FXCollections.observableList(filteredDataList));
            if (filteredDataList.size() > 0)
            {
            	setDataView(filteredDataList.get(0));
            }
            else
            {
                setDataView(null);
            }
        });
        BorderPane.setAlignment(search, Pos.TOP_LEFT);
        search.setMaxWidth(250);
        return search;
    }

    private Node createDataArea()
    {
        myDataView = new ListView<>();
        myDataView.setCellFactory((param) ->
        {
        	BaseballDataRow row = new BaseballDataRow();
        	return row;
        });
        myDataView.autosize();
        return myDataView;
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
        if (value == null)
        {
            return null;
        }
        else if (value instanceof Double)
        {
            SpecialKey specialType = getSpecialKey(key, dataElement);
            if (specialType instanceof LatitudeKey)
            {
                return BaseballUtils.formatCoordinate(value, GeographicPositionFormat.DECDEG, CoordType.LAT);
            }
            else if (specialType instanceof LongitudeKey)
            {
                return BaseballUtils.formatCoordinate(value, GeographicPositionFormat.DECDEG, CoordType.LON);
            }
            return BaseballUtils.formatNumber((Number)value);
        }
        else if (value instanceof Number)
        {
            return BaseballUtils.formatNumber((Number)value);
        }
        else if (value instanceof Date)
        {
            return BaseballUtils.formatDate((Date)value, myToolbox.getPreferencesRegistry().getPreferences(ListToolPreferences.class)
                    .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0));
        }
        else if (value instanceof TimeSpan)
        {
        	return BaseballUtils.formatTimeSpan((TimeSpan)value, myToolbox.getPreferencesRegistry()
        	        .getPreferences(ListToolPreferences.class).getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0));
        }
        return value.toString();
    }
}
