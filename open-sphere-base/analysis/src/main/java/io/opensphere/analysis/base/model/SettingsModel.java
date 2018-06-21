package io.opensphere.analysis.base.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import net.jcip.annotations.NotThreadSafe;

import io.opensphere.analysis.binning.criteria.TimeBinType;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentDoubleProperty;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import io.opensphere.mantle.data.DataTypeInfo;

/** Model for a tool's settings. */
@NotThreadSafe
public class SettingsModel
{
    /** The maximum allowed bin count. */
    public static final int MAX_BIN_COUNT = 1000;

    /** The common settings. */
    private final CommonSettingsModel myCommonSettings;

    /** Whether the panel's data type is locked to the current data type. */
    private final BooleanProperty myLocked = new ConcurrentBooleanProperty(this, "locked", true);

    /** The current layer in the tool. */
    private final ObjectProperty<DataTypeInfo> myCurrentLayer = new ConcurrentObjectProperty<>(this, "currentLayer");

    /** The currently selected column. */
    private final StringProperty mySelectedColumn = new ConcurrentStringProperty(this, "selectedColumn");

    /** The available columns. */
    private final ObservableList<String> myAvailableColumns = FXCollections.observableArrayList();

    /** The data type. */
    private final ObjectProperty<DataType> myDataType = new ConcurrentObjectProperty<>(this, "dataType");

    /** Whether to show data for all time, or just the active time. */
    private final BooleanProperty myAllTime = new ConcurrentBooleanProperty(this, "allTime", true);

    /** The bin type. */
    private final ObjectProperty<BinType> myBinType = new SimpleObjectProperty<>(this, "binType");

    /** The bin width. */
    private final DoubleProperty myBinWidth = new ConcurrentDoubleProperty(this, "binWidth", 10);

    /** The numeric bin type. */
    private final ObjectProperty<BinType> myNumericBinType = new SimpleObjectProperty<>(this, "numericBinType", BinType.RANGE);

    /** The time bin type. */
    private final ObjectProperty<TimeBinType> myTimeBinType = new SimpleObjectProperty<>(this, "timeBinType",
            TimeBinType.HOUR_OF_DAY);

    /** Whether to show empty bins. */
    private final BooleanProperty myShowEmptyBins = new ConcurrentBooleanProperty(this, "showEmptyBins", true);

    /** Whether to show the N/A bin. */
    private final BooleanProperty myShowNABin = new ConcurrentBooleanProperty(this, "showNABin", true);

    /** The bin sort method. */
    private final ObjectProperty<SortMethod> mySortMethod = new ConcurrentObjectProperty<>(this, "sortMethod",
            SortMethod.VALUE_ASCENDING);

    /** The chart orientation. */
    private final ObjectProperty<Orientation> myOrientation = new SimpleObjectProperty<>(this, "orientation",
            Orientation.VERTICAL);

    /** The background color. */
    private final ObjectProperty<Color> myBackgroundColor = new SimpleObjectProperty<>(this, "backgroundColor", null);

    /** The foreground color. */
    private final ObjectProperty<Color> myForegroundColor = new SimpleObjectProperty<>(this, "foregroundColor", null);

    /** Whether to show the title. */
    private final BooleanProperty myShowTitle = new ConcurrentBooleanProperty(this, "showTitle", false);

    /** The title text. */
    private final StringProperty myTitleText = new SimpleStringProperty(this, "titleText");

    /** The category axis label text. */
    private final StringProperty myCategoryAxisText = new SimpleStringProperty(this, "categoryText");

    /** The count axis label text. */
    private final StringProperty myCountAxisText = new SimpleStringProperty(this, "countText");

    /** The label models. */
    private final List<LabelModel> myLabelModels = new ArrayList<LabelModel>();

    /** The title label model. */
    private final LabelModel myTitleLabelModel = new LabelModel(LabelType.TITLE);

    /** The axis label model. */
    private final LabelModel myAxisLabelModel = new LabelModel(LabelType.AXIS);

    /**
     * Constructor.
     *
     * @param commonSettings The common settings
     */
    public SettingsModel(CommonSettingsModel commonSettings)
    {
        myCommonSettings = commonSettings;
        myLabelModels.add(myTitleLabelModel);
        myLabelModels.add(myAxisLabelModel);
    }

    /**
     * Gets the commonSettings.
     *
     * @return the commonSettings
     */
    public CommonSettingsModel getCommonSettings()
    {
        return myCommonSettings;
    }

    /**
     * Gets the locked property.
     *
     * @return the locked property
     */
    public BooleanProperty lockedProperty()
    {
        return myLocked;
    }

    /**
     * Gets the currentLayer property.
     *
     * @return the currentLayer property
     */
    public ObjectProperty<DataTypeInfo> currentLayerProperty()
    {
        return myCurrentLayer;
    }

    /**
     * Gets the selected column property.
     *
     * @return the selected column property
     */
    public StringProperty selectedColumnProperty()
    {
        return mySelectedColumn;
    }

    /**
     * Gets the available columns property.
     *
     * @return the available columns property
     */
    public ObservableList<String> availableColumnsProperty()
    {
        return myAvailableColumns;
    }

    /**
     * Gets the dataType property.
     *
     * @return the dataType property
     */
    public ObjectProperty<DataType> dataTypeProperty()
    {
        return myDataType;
    }

    /**
     * Gets the allTime property.
     *
     * @return the allTime property
     */
    public BooleanProperty allTimeProperty()
    {
        return myAllTime;
    }

    /**
     * Gets the binType property.
     *
     * @return the binType property
     */
    public ObjectProperty<BinType> binTypeProperty()
    {
        return myBinType;
    }

    /**
     * Gets the binWidth property.
     *
     * @return the binWidth property
     */
    public DoubleProperty binWidthProperty()
    {
        return myBinWidth;
    }

    /**
     * Gets the numericBinType property.
     *
     * @return the numericBinType property
     */
    public ObjectProperty<BinType> numericBinTypeProperty()
    {
        return myNumericBinType;
    }

    /**
     * Gets the timeBinType property.
     *
     * @return the timeBinType property
     */
    public ObjectProperty<TimeBinType> timeBinTypeProperty()
    {
        return myTimeBinType;
    }

    /**
     * Gets the showEmptyBins property.
     *
     * @return the showEmptyBins property
     */
    public BooleanProperty showEmptyBinsProperty()
    {
        return myShowEmptyBins;
    }

    /**
     * Gets the showNABin property.
     *
     * @return the showNABin property
     */
    public BooleanProperty showNABinProperty()
    {
        return myShowNABin;
    }

    /**
     * Gets the SortMethod property.
     *
     * @return the SortMethod property
     */
    public ObjectProperty<SortMethod> sortMethodProperty()
    {
        return mySortMethod;
    }

    /**
     * Gets the orientation property.
     *
     * @return the orientation property
     */
    public ObjectProperty<Orientation> orientationProperty()
    {
        return myOrientation;
    }

    /**
     * Gets the backgroundColor property.
     *
     * @return the backgroundColor property
     */
    public ObjectProperty<Color> backgroundColorProperty()
    {
        return myBackgroundColor;
    }

    /**
     * Gets the foregroundColor property.
     *
     * @return the foregroundColor property
     */
    public ObjectProperty<Color> foregroundColorProperty()
    {
        return myForegroundColor;
    }

    /**
     * Gets the showTitle property.
     *
     * @return the showTitle property
     */
    public BooleanProperty showTitleProperty()
    {
        return myShowTitle;
    }

    /**Gets the titleText property.
     *
     * @return the titleText property
     */
    public StringProperty titleTextProperty()
    {
        return myTitleText;
    }

    /**Gets the categoryText property.
     *
     * @return the categoryText property
     */
    public StringProperty categoryAxisTextProperty()
    {
        return myCategoryAxisText;
    }

    /**Gets the countText property.
     *
     * @return the countTextProperty
     */
    public StringProperty countAxisTextProperty()
    {
        return myCountAxisText;
    }

    /**
     * Gets the title label model.
     *
     * @return the titleLabelModel
     */
    public LabelModel getTitleLabelModel()
    {
        return myTitleLabelModel;
    }

    /**
     * Gets the axis label model.
     *
     * @return the axisLabelModel
     */
    public LabelModel getAxisLabelModel()
    {
        return myAxisLabelModel;
    }

    /**
     * Gets the label models.
     *
     * @return the label models
     */
    public List<LabelModel> getLabelModels()
    {
        return myLabelModels;
    }
}
