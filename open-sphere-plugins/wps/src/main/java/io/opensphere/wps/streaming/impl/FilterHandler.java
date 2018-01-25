package io.opensphere.wps.streaming.impl;

import javax.xml.bind.JAXBElement;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.mantle.data.element.mdfilter.OGCFilterGenerator;
import io.opensphere.mantle.data.element.mdfilter.OGCFilterParameters;
import io.opensphere.wps.streaming.StreamingConstants;
import net.opengis.ogc._110.FilterType;
import net.opengis.ows._110.CodeType;
import net.opengis.wps._100.ComplexDataType;
import net.opengis.wps._100.DataInputsType;
import net.opengis.wps._100.DataType;
import net.opengis.wps._100.InputType;

/**
 * Serializes the filter so it can be passed to the server during a subscribe
 * request.
 */
public class FilterHandler
{
    /**
     * Serializes the passed in filter into a format the WPS process expects.
     *
     * @param dataInputs The object to put the filter data into.
     * @param filter The filter to serialize or null.
     * @param spatialFilter The spatial filter to serialize or null.
     * @param layerName The layer name.
     */
    public void serializeFilter(DataInputsType dataInputs, DataFilter filter, Geometry spatialFilter, String layerName)
    {
        if (filter != null || spatialFilter != null)
        {
            OGCFilterParameters parameters = new OGCFilterParameters();
            parameters.setUserFilter(filter);
            parameters.setRegion(spatialFilter);
            parameters.setGeometryTagName("GEOM");

            FilterType ogcFilter = OGCFilterGenerator.buildQuery(parameters, layerName);

            net.opengis.ogc._110.ObjectFactory ogcObjectFactory = new net.opengis.ogc._110.ObjectFactory();
            JAXBElement<FilterType> rootFilter = ogcObjectFactory.createFilter(ogcFilter);

            InputType input = new InputType();

            DataType data = new DataType();

            ComplexDataType complexData = new ComplexDataType();
            complexData.setMimeType("text/xml");
            complexData.getContent().add(rootFilter);

            data.setComplexData(complexData);

            CodeType codeType = new CodeType();
            codeType.setValue(StreamingConstants.LAYER_FILTER);
            input.setIdentifier(codeType);
            input.setData(data);

            dataInputs.getInput().add(input);
        }
    }
}
