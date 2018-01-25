package io.opensphere.wps;

import java.util.function.Consumer;

import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import net.opengis.wps._100.WPSCapabilitiesType;

/**
 * A change listener designed to listen to the Data Registry for new WPS GetCapabilities objects.
 */
public class WpsGetCapabilitiesDataRegistryChangeListener extends DataRegistryListenerAdapter<WPSCapabilitiesType>
{
    /**
     * The consumer used to process new WPS Capabilities documents received through the data registry.
     */
    private final Consumer<WPSCapabilitiesType> myNewCapabilitiesConsumer;

    /**
     * The consumer used to process removed WPS capabilities documents received through the data registry.
     */
    private final Consumer<WPSCapabilitiesType> myRemoveCapabilitiesConsumer;

    /**
     * Creates a new listener, accepting the supplied consumer.
     *
     * @param pNewCapabilitiesConsumer the consumer which will receive the newly located capabilities document.
     * @param pRemoveCapabilitiesConsumer the consumer which will receive each capabilities document to be removed.
     */
    public WpsGetCapabilitiesDataRegistryChangeListener(Consumer<WPSCapabilitiesType> pNewCapabilitiesConsumer,
            Consumer<WPSCapabilitiesType> pRemoveCapabilitiesConsumer)
    {
        myNewCapabilitiesConsumer = pNewCapabilitiesConsumer;
        myRemoveCapabilitiesConsumer = pRemoveCapabilitiesConsumer;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryListener#isIdArrayNeeded()
     */
    @Override
    public boolean isIdArrayNeeded()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryListenerAdapter#valuesAdded(io.opensphere.core.data.util.DataModelCategory,
     *      long[], java.lang.Iterable, java.lang.Object)
     */
    @Override
    public void valuesAdded(DataModelCategory pDataModelCategory, long[] pIds, Iterable<? extends WPSCapabilitiesType> pNewValues,
            Object pSource)
    {
        for (WPSCapabilitiesType wpsCapabilitiesType : pNewValues)
        {
            myNewCapabilitiesConsumer.accept(wpsCapabilitiesType);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryListenerAdapter#valuesRemoved(io.opensphere.core.data.util.DataModelCategory,
     *      long[], java.lang.Iterable, java.lang.Object)
     */
    @Override
    public void valuesRemoved(DataModelCategory pDataModelCategory, long[] pIds,
            Iterable<? extends WPSCapabilitiesType> pRemovedValues, Object pSource)
    {
        for (WPSCapabilitiesType wpsCapabilitiesType : pRemovedValues)
        {
            myRemoveCapabilitiesConsumer.accept(wpsCapabilitiesType);
        }
    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @see io.opensphere.core.data.DataRegistryListenerAdapter#valuesRemoved(io.opensphere.core.data.util.DataModelCategory,
//     *      long[], java.lang.Iterable, java.lang.Object)
//     */
//    @Override
//    public void valuesRemoved(DataModelCategory pDataModelCategory, long[] pIds,
//            Iterable<? extends WPSCapabilitiesType> pRemovedValues, Object pSource)
//    {
//        LOG.info("Should remove " + pIds.length + " process items, with items supplied as values.");
//        for (WPSCapabilitiesType wpsCapabilitiesType : pRemovedValues)
//        {
//            String serverTitle = "";
//            for (LanguageStringType title : wpsCapabilitiesType.getServiceIdentification().getTitle())
//            {
//                serverTitle = StringUtilities.concat(serverTitle, title.getValue());
//            }
//
//            Map<WpsRequestType, Operation> operationDefinitions = New.map();
//            for (Operation operation : wpsCapabilitiesType.getOperationsMetadata().getOperation())
//            {
//                operationDefinitions.put(WpsRequestType.fromValue(operation.getName()), operation);
//            }
//
//            if (operationDefinitions.containsKey(WpsRequestType.DESCRIBE_PROCESS_TYPE))
//            {
//                for (DCP dcp : operationDefinitions.get(WpsRequestType.DESCRIBE_PROCESS_TYPE).getDCP())
//                {
//                    HTTP httpConfiguration = dcp.getHTTP();
//                    for (JAXBElement<RequestMethodType> requestMethodType : httpConfiguration.getGetOrPost())
//                    {
//                        String url = requestMethodType.getValue().getHref();
//
//                        ProcessOfferings processOfferings = wpsCapabilitiesType.getProcessOfferings();
//                        List<ProcessBriefType> processes = processOfferings.getProcess();
//
//                        for (ProcessBriefType process : processes)
//                        {
//                            myRemoveProcessTypeConsumer.accept(url, serverTitle, process);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * {@inheritDoc}
//     *
//     * @see io.opensphere.core.data.DataRegistryListenerAdapter#valuesAdded(io.opensphere.core.data.util.DataModelCategory,
//     *      long[], java.lang.Iterable, java.lang.Object)
//     */
//    @Override
//    public void valuesAdded(DataModelCategory pDataModelCategory, long[] pIds, Iterable<? extends WPSCapabilitiesType> pNewValues,
//            Object pSource)
//    {
//        int itemsAdded = 0;
//        for (WPSCapabilitiesType wpsCapabilitiesType : pNewValues)
//        {
//            String serverTitle = "";
//            for (LanguageStringType title : wpsCapabilitiesType.getServiceIdentification().getTitle())
//            {
//                serverTitle = StringUtilities.concat(serverTitle, title.getValue());
//            }
//
//            Map<WpsRequestType, Operation> operationDefinitions = New.map();
//            for (Operation operation : wpsCapabilitiesType.getOperationsMetadata().getOperation())
//            {
//                operationDefinitions.put(WpsRequestType.fromValue(operation.getName()), operation);
//            }
//
//            if (operationDefinitions.containsKey(WpsRequestType.DESCRIBE_PROCESS_TYPE))
//            {
//                for (DCP dcp : operationDefinitions.get(WpsRequestType.DESCRIBE_PROCESS_TYPE).getDCP())
//                {
//                    HTTP httpConfiguration = dcp.getHTTP();
//                    for (JAXBElement<RequestMethodType> requestMethodType : httpConfiguration.getGetOrPost())
//                    {
//                        String url = requestMethodType.getValue().getHref();
//
//                        ProcessOfferings processOfferings = wpsCapabilitiesType.getProcessOfferings();
//                        List<ProcessBriefType> processes = processOfferings.getProcess();
//
//                        myNewProcessTypeConsumer.accept(url, url, processes);
//                        itemsAdded += processes.size();
//                    }
//                }
//            }
//        }
//
//        LOG.info("Added " + itemsAdded + " datatypes as processes.");
//    }
}
