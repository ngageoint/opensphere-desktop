package io.opensphere.myplaces.models;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;

/**
 * A special data group for my places. Allows a heirarchical view within the
 * layer manager.
 */
public class MyPlacesDataGroupInfo extends DefaultDataGroupInfo
{
    /**
     * The kml folder this data group represents.
     */
    private Folder myKmlFolder;

    /**
     * Constructs a new my places data group.
     *
     * @param rootNode Indicates if this is a root node.
     * @param aToolbox The toolbox.
     * @param kmlFolder The kml folder this data group represents.
     */
    public MyPlacesDataGroupInfo(boolean rootNode, Toolbox aToolbox, Folder kmlFolder)
    {
        super(rootNode, aToolbox, "My Places", kmlFolder.getId(), kmlFolder.getName());
        setIsFlattenable(false);

        myKmlFolder = kmlFolder;
    }

    /**
     * Gets the kml folder this data group represents.
     *
     * @return The kml folder.
     */
    public Folder getKmlFolder()
    {
        return myKmlFolder;
    }

    @Override
    public boolean hasDetails()
    {
        return false;
    }

    @Override
    public boolean isDragAndDrop()
    {
        return true;
    }

    @Override
    public boolean isTaggable()
    {
        return false;
    }

    /**
     * Sets a new kml folder for the group.
     *
     * @param kmlFolder The new kml folder.
     */
    public void setKmlFolder(Folder kmlFolder)
    {
        myKmlFolder = kmlFolder;
    }

    @Override
    public boolean userActivationStateControl()
    {
        return false;
    }

    @Override
    public boolean userDeleteControl()
    {
        return true;
    }

    @Override
    protected void assertHasChildren()
    {
    }

    @Override
    protected void assertHasMembers()
    {
    }
}
