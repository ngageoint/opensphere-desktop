/* Copyright (C) 2006 BIT Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>. */
package io.opensphere.mantle.iconproject.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;

/**
 * Contains the users preferences within each session. When opening icon manager
 * it will use these values.
 */
public class IconManagerPrefs
{
    /** The value used for the tilewidth. */
    private final IntegerProperty myInitTileWidth = new SimpleIntegerProperty();

    /** The tree which will be selected on start up. */
    private final ObjectProperty<TreeItem<String>> myInitTreeSelection = new SimpleObjectProperty<>(
            new TreeItem<>("temp"));

    /**
     * Gets the tilewidth for display icons in the Icon Manager.
     *
     * @return the set value.
     */
    public IntegerProperty getIconWidth()
    {
        return myInitTileWidth;
    }
    /**
     * Gets the value of the {@link #myInitTreeSelection} field.
     *
     * @return the value stored in the {@link #myInitTreeSelection} field.
     */
    public ObjectProperty<TreeItem<String>> getInitTreeSelection()
    {
        return myInitTreeSelection;
    }
}
