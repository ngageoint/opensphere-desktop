package io.opensphere.core.common.geospatial.model.interfaces;

import java.util.List;

public interface IDataObjectCollection extends IDataObject
{

    /**
     *
     * @return
     */
    public List<? extends IDataObject> getGeometries();

    /**
     *
     * @param geometry
     */
    public void addGeometry(IDataObject geometry);

    /**
     *
     * @param geometries
     */
    public void addGeometries(List<? extends IDataObject> geometries);

}
