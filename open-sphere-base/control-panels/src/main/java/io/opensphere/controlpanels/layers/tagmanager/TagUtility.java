package io.opensphere.controlpanels.layers.tagmanager;

import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class TagUtility.
 */
public final class TagUtility
{
    /**
     * Removes the specified tag from all members (and if recursive, all
     * sub-groups members) if the tag exists for the members. Returns true if at
     * least one member had its tag set altered by the call.
     *
     * @param dgi the {@link DataGroupInfo} from which to remove the tag
     * @param tagToRemove the tag to remove
     * @param recursive true to recurse through all sub-groups
     * @param source the source of the request
     * @return true, if at least one member was changed as a result of this call
     */
    public static boolean removeTag(DataGroupInfo dgi, String tagToRemove, boolean recursive, Object source)
    {
        boolean changed = false;
        if (dgi != null)
        {
            for (DataTypeInfo dti : dgi.getMembers(recursive))
            {
                if (dti.removeTag(tagToRemove, source))
                {
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * Shows a modal text entry dialog where tags can be applied to multiple
     * data groups.
     *
     * @param owner the owner of the dialog
     * @param dgiSet the set of {@link DataGroupInfo} to tag.
     * @param addTags the true to add the tags, false to remove them.
     * @param source the source requesting the tag.
     */
    public static void showAddRemoveTagsDataGroupDialog(Component owner, Collection<DataGroupInfo> dgiSet, boolean addTags,
            Object source)
    {
        if (CollectionUtilities.hasContent(dgiSet))
        {
            StringBuilder sb = new StringBuilder();
            List<DataGroupInfo> dgiList = New.list(dgiSet);
            Collections.sort(dgiList, DataGroupInfo.CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR);
            for (DataGroupInfo dgi : dgiList)
            {
                sb.append("<center><bold>").append(dgi.getDisplayName()).append("</bold></center>");
            }
            String applicationType = addTags ? "applied to" : "removed from";

            String result = JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(owner),
                    "<html><center>Please enter the tag(s) to be " + applicationType + " layers:</center><br>" + sb.toString()
                            + "<br><center>(Use a comma separated list for multiple tags)</center><br></html>",
                    "");
            if (!StringUtils.isBlank(result))
            {
                if (result.indexOf(',') != -1)
                {
                    StringTokenizer st = new StringTokenizer(result, ",");
                    while (st.hasMoreTokens())
                    {
                        String tag = st.nextToken().trim();
                        for (DataGroupInfo dgi : dgiList)
                        {
                            if (addTags)
                            {
                                tagDataGroup(dgi, tag, false, source);
                            }
                            else
                            {
                                removeTag(dgi, tag, false, source);
                            }
                        }
                    }
                }
                else
                {
                    for (DataGroupInfo dgi : dgiList)
                    {
                        if (addTags)
                        {
                            tagDataGroup(dgi, result, false, source);
                        }
                        else
                        {
                            removeTag(dgi, result, false, source);
                        }
                    }
                }
            }
        }
    }

    /**
     * Shows a modal text entry dialog where tags can be applied to a data
     * group.
     *
     * @param owner the owner of the dialog
     * @param dgi the {@link DataGroupInfo} that is the target of the tag.
     * @param source the source requesting the tag.
     */
    public static void showTagDataGroupDialog(Component owner, DataGroupInfo dgi, Object source)
    {
        if (dgi != null)
        {
            String result = (String)JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(owner),
                    "<html><center>Please enter the tag(s) to be applied to:</center><br><center><bold>" + dgi.getDisplayName()
                            + "<bold></center><br><center>(Use a comma separated list for multiple tags)</center><br></html>",
                    "Add Tag", JOptionPane.QUESTION_MESSAGE, null, null, "");
            if (!StringUtils.isBlank(result))
            {
                if (result.indexOf(',') != -1)
                {
                    StringTokenizer st = new StringTokenizer(result, ",");
                    while (st.hasMoreTokens())
                    {
                        String tag = st.nextToken().trim();
                        tagDataGroup(dgi, tag, false, source);
                    }
                }
                else
                {
                    tagDataGroup(dgi, result, false, source);
                }
            }
        }
    }

    /**
     * Shows a modal text entry dialog where tags can be applied to a data type.
     *
     * @param owner the owner of the dialog
     * @param dti the {@link DataTypeInfo} that is the target of the tag.
     * @param source the source requesting the tag.
     */
    public static void showTagDataTypeDialog(Component owner, DataTypeInfo dti, Object source)
    {
        if (dti != null)
        {
            String result = JOptionPane
                    .showInputDialog(SwingUtilities.getWindowAncestor(owner),
                            "<html><center>Please enter the tag(s) to be applied to:</center><br><center>" + dti.getDisplayName()
                                    + "</center><br><center>(Use a comma separated list for multiple tags)</center><br></html>",
                    "");
            if (!StringUtils.isBlank(result))
            {
                if (result.indexOf(',') != -1)
                {
                    StringTokenizer st = new StringTokenizer(result, ",");
                    while (st.hasMoreTokens())
                    {
                        String tag = st.nextToken().trim();
                        dti.addTag(tag, source);
                    }
                }
                else
                {
                    dti.addTag(result, source);
                }
            }
        }
    }

    /**
     * Show tag manager for a specific {@link DataGroupInfo}.
     *
     * @param tb the {@link Toolbox}
     * @param owner the {@link Component} owner of the dialog.
     * @param dgi the {@link DataGroupInfo} to manage.
     * @param source the requester of the management.
     */
    public static void showTagManagerForGroup(Toolbox tb, Component owner, DataGroupInfo dgi, Object source)
    {
        TagManagerPanel tmp = new TagManagerPanel(tb, dgi);
        JOptionPane.showMessageDialog(
                owner == null ? tb.getUIRegistry().getMainFrameProvider().get() : SwingUtilities.getWindowAncestor(owner), tmp,
                "Manage Tags", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Show tag manager for a specific {@link DataTypeInfo}.
     *
     * @param tb the {@link Toolbox}
     * @param owner the {@link Component} owner of the dialog.
     * @param dti the {@link DataTypeInfo} to manage.
     * @param source the requester of the management.
     */
    public static void showTagManagerForType(Toolbox tb, Component owner, DataTypeInfo dti, Object source)
    {
        TagManagerPanel tmp = new TagManagerPanel(tb, dti);
        JOptionPane.showMessageDialog(
                owner == null ? tb.getUIRegistry().getMainFrameProvider().get() : SwingUtilities.getWindowAncestor(owner), tmp,
                "Manage Tags", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Applies a tag to all {@link DataTypeInfo} that are members of the
     * specified data group. If recursive applies to all sub-groups and
     * sub-members.
     *
     * @param dgi the {@link DataGroupInfo}
     * @param tagToAdd the tag to add
     * @param recursive true if recursing into sub-groups and types is desired.
     * @param source the source of the tag request.
     */
    public static void tagDataGroup(DataGroupInfo dgi, String tagToAdd, boolean recursive, Object source)
    {
        if (dgi != null)
        {
            for (DataTypeInfo dti : dgi.getMembers(recursive))
            {
                dti.addTag(tagToAdd, source);
            }
        }
    }

    /**
     * Disallow instantiation.
     */
    private TagUtility()
    {
    }
}
