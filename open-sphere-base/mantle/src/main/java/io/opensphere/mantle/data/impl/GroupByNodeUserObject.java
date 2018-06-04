package io.opensphere.mantle.data.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.controller.util.DataGroupInfoUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * The Class GroupByNodeUserObject.
 */
@SuppressWarnings("PMD.GodClass")
public class GroupByNodeUserObject
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(GroupByNodeUserObject.class);

    /** The Base label. */
    private String myBaseLabel;

    /** The Category count. */
    private int myCategoryCount;

    /** The description cache. */
    private String myDescriptionCache;

    /** The DataGroupInfo. */
    private final DataGroupInfo myDGI;

    /** The DataTypeInfo. */
    private final DataTypeInfo myDTI;

    /** The Id. */
    private String myId;

    /** The is category node. */
    private boolean myIsCategoryNode;

    /** The label. */
    private String myLabel;

    /** The node summary. */
    private String mySummaryCache;

    /** The visible category count. */
    private int myVisibleCategoryCount;

    /**
     * Instantiates a new group by node user object.
     *
     * @param dgi the {@link DataGroupInfo}
     */
    public GroupByNodeUserObject(DataGroupInfo dgi)
    {
        myDGI = dgi;
        myDTI = null;
    }

    /**
     * Instantiates a new group by node user object.
     *
     * @param dgi the {@link DataGroupInfo}
     * @param dti the {@link DataTypeInfo}
     */
    public GroupByNodeUserObject(DataGroupInfo dgi, DataTypeInfo dti)
    {
        myDGI = dgi;
        myDTI = dti;
    }

    /**
     * Instantiates a new group by node user object.
     *
     * @param label the label
     */
    public GroupByNodeUserObject(String label)
    {
        myLabel = label;
        myBaseLabel = label;
        myId = label;
        myDTI = null;
        myDGI = null;
        myIsCategoryNode = true;
    }

    /**
     * Generates the label.
     */
    public void generateLabel()
    {
        if (isCategoryNode())
        {
            myLabel = generateCategoryLabel();
        }
        if (myDGI != null && myDTI == null)
        {
            myLabel = myDGI.getDisplayName();
            setId(myLabel);
        }
        else if (myDTI != null)
        {
            myLabel = myDTI.getDisplayName();
            setId(myLabel);
        }
    }

    /**
     * Gets the actual data type info.
     *
     * @return the actual data type info
     */
    public DataTypeInfo getActualDataTypeInfo()
    {
        return myDTI;
    }

    /**
     * Gets the base label.
     *
     * @return the base label
     */
    public String getBaseLabel()
    {
        return myBaseLabel;
    }

    /**
     * Gets the category count.
     *
     * @return the category count
     */
    public int getCategoryCount()
    {
        return myCategoryCount;
    }

    /**
     * Gets the data group info.
     *
     * @return the data group info
     */
    public DataGroupInfo getDataGroupInfo()
    {
        return myDGI;
    }

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    @SuppressWarnings("PMD.SimplifiedTernary")
    public DataTypeInfo getDataTypeInfo()
    {
        return myDTI != null ? myDTI : myDGI != null && myDGI.numMembers(false) == 1 && myDGI.isFlattenable()
                ? myDGI.getMembers(false).iterator().next() : null;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription()
    {
        if (myDescriptionCache == null)
        {
            if (myDTI != null)
            {
                myDescriptionCache = myDTI.getDescription();
            }
            else if (myDGI != null)
            {
                myDescriptionCache = myDGI.getSummaryDescription();
            }
            else
            {
                myDescriptionCache = myBaseLabel;
            }
        }
        return myDescriptionCache;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId()
    {
        return myId == null ? getLabel() : myId;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Gets the selection state.
     *
     * @return the selection state
     */
    public boolean isSelected()
    {
        if (isSelectable())
        {
            return myDGI.activationProperty().isActiveOrActivating();
        }
        return false;
    }

    /**
     * Gets the tool tip.
     *
     * @return the tool tip
     */
    public String getSummary()
    {
        StringBuilder summaryBuilder = new StringBuilder();
        if (myDGI != null)
        {
            summaryBuilder.append("<html>");

            String summaryDescription = myDGI.getSummaryDescription();
            if (!summaryDescription.startsWith(myDGI.getDisplayName()))
            {
                summaryBuilder.append(myDGI.getDisplayName()).append("<br/>");
            }
            if (!StringUtils.isBlank(summaryDescription))
            {
                summaryDescription = WordUtils.wrap(summaryDescription, 45, "\n", true);
                summaryDescription = summaryDescription.replace("\n", "<br/>");
                summaryBuilder.append(summaryDescription);
                summaryBuilder.append("<br/><br/>");
            }

            TimeSpan timeSpan = DataGroupInfoUtilities.findTimeExtentFromDGICollection(Collections.singleton(myDGI));
            if (!timeSpan.isZero())
            {
                summaryBuilder.append(timeSpan.toDisplayString());
            }
            else
            {
                summaryBuilder.append("Timeless Layer");
            }
            summaryBuilder.append("</html>");
        }
        mySummaryCache = summaryBuilder.toString();
        return mySummaryCache;
    }

    /**
     * Gets the tool tip.
     *
     * @return the tool tip
     */
    public String getToolTip()
    {
        StringBuilder toolTipBuilder = new StringBuilder(32);
        if (myDGI != null)
        {
            DataTypeInfo current = null;
            if (myDTI == null)
            {
                Iterator<DataTypeInfo> dtisIterator = myDGI.getMembers(false).iterator();
                if (dtisIterator.hasNext())
                {
                    current = dtisIterator.next();
                }
            }
            else
            {
                current = myDTI;
            }

            if (current != null)
            {
                toolTipBuilder.append("<html>");
                toolTipBuilder.append(current.getDisplayName());
                toolTipBuilder.append("<br>");
                TimeSpan timeSpan = DataGroupInfoUtilities.findTimeExtentFromDGICollection(Collections.singleton(myDGI));

                if (!timeSpan.isZero())
                {
                    String[] dateTok = timeSpan.toDisplayString().split(" - ");

                    toolTipBuilder.append(dateTok[0]);
                    toolTipBuilder.append("<br>");
                    if (dateTok.length > 1)
                    {
                        toolTipBuilder.append(dateTok[1]);
                        toolTipBuilder.append("<br>");
                    }
                }

                checkVisType(toolTipBuilder, current);
            }
        }
        return toolTipBuilder.toString();
    }

    /**
     * Gets the visible category count.
     *
     * @return the visible category count
     */
    public int getVisibleCategoryCount()
    {
        return myVisibleCategoryCount;
    }

    /**
     * Checks for data group info.
     *
     * @return true, if successful
     */
    public boolean hasDataGroupInfo()
    {
        return myDGI != null;
    }

    /**
     * Checks for data type info.
     *
     * @return true, if successful
     */
    public boolean hasDataTypeInfo()
    {
        return myDGI != null;
    }

    /**
     * Checks if is category node.
     *
     * @return true, if is category node
     */
    public boolean isCategoryNode()
    {
        return myIsCategoryNode;
    }

    /**
     * Checks if is category only.
     *
     * @return true, if is category only
     */
    public boolean isCategoryOnly()
    {
        return myDGI == null && myDTI == null;
    }

    /**
     * Checks if is selectable.
     *
     * @return true, if is selectable
     */
    public boolean isSelectable()
    {
        return myDGI != null || myDTI != null;
    }

    /**
     * Matches pattern.
     *
     * @param searchPattern the search pattern
     * @return true, if successful
     */
    public boolean matchesPattern(Pattern searchPattern)
    {
        return GroupKeywordUtilities.search(searchPattern, myDGI, myDTI, getDescription());
    }

    /**
     * Sets the category count.
     *
     * @param categoryCount the new category count
     */
    public void setCategoryCount(int categoryCount)
    {
        myCategoryCount = categoryCount;
    }

    /**
     * Sets the category node.
     *
     * @param isCategoryNode the new category node
     */
    public void setCategoryNode(boolean isCategoryNode)
    {
        myIsCategoryNode = isCategoryNode;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Sets the visible category count.
     *
     * @param count the new visible category count
     */
    public void setVisibleCategoryCount(int count)
    {
        myVisibleCategoryCount = count;
    }

    @Override
    public String toString()
    {
        if (myLabel == null)
        {
            generateLabel();
        }
        return myLabel;
    }

    /**
     * Generate category label.
     *
     * @return the string
     */
    protected String generateCategoryLabel()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseLabel());
        if (getCategoryCount() > 0)
        {
            sb.append(" (");
            if (getVisibleCategoryCount() < getCategoryCount())
            {
                sb.append(getVisibleCategoryCount()).append(" of ");
            }
            sb.append(getCategoryCount());

            sb.append(')');
        }
        return sb.toString();
    }

    /**
     * Sets the label.
     *
     * @param label the new label
     */
    public void setLabel(String label)
    {
        myLabel = label;
    }

    /**
     * Checks the map visualization type and adds description to the tool tip.
     *
     * @param toolTipBuilder the tool tip builder
     * @param current the current
     */
    private void checkVisType(StringBuilder toolTipBuilder, DataTypeInfo current)
    {
        if (current.getMapVisualizationInfo() != null)
        {
            MapVisualizationType type = current.getMapVisualizationInfo().getVisualizationType();
            switch (type)
            {
                case ANNOTATION_POINTS:
                    toolTipBuilder.append("Point annotation");
                    break;

                case ANNOTATION_REGIONS:
                    toolTipBuilder.append("Region annotation");
                    break;

                case USER_TRACK_ELEMENTS:
                    toolTipBuilder.append("Track annotation");
                    break;

                case POINT_ELEMENTS:
                    toolTipBuilder.append("Feature layer");
                    break;

                case IMAGE_TILE:
                    toolTipBuilder.append("Tile layer");
                    break;

                case TERRAIN_TILE:
                    toolTipBuilder.append("Terrain layer");
                    break;

                case IMAGE:
                    break;

                case PLACE_NAME_ELEMENTS:
                    break;

                default:
                    break;
            }
        }
        else
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Data type " + current.getDisplayName() + " does not contain any visualization info.");
            }
        }
    }
}
