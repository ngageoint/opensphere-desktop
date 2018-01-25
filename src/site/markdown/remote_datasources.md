
Interacting with Remote Data Sources
------------------------------------
The data registry acts as an intermediary buffer for remote systems, providing a caching mechanism for performance improvements. Instead of directly querying a remote system for data, processing the results, and displaying results on the map, a user would make a request to the data registry for data matching a set of parameters. The data registry will then determine if any data matches the request, and if not, search for a configured data provider. For remote systems, the data provide will implement both the `io.opensphere.core.data.DataRegistryDataProvider` and the `io.opensphere.core.api.Envoy` interfaces. An envoy is used to directly interact with remote systems, and is responsible for making and managing remote connections, submitting queries, and processing results from the remote system. When results are received, the data provider (which may be the same class) adds the results to the `DataRegistry` using a known property descriptor. 

Depending on how the query was submitted to the the registry, the results can be returned directly to the caller, or ignored by the caller. The caller may ignore the results if they are to be displayed directly on the map, as there are already data registry listeners configured to react to data in the registry that should be displayed on the map. For data points that do not implement `io.opensphere.core.geometry.Geometry` (which is required for map display), an instance of `io.opensphere.core.api.Transformer` is used to transform from a source type to a Geometry instance. 

![Remote Data Source Interaction](images/DataSourceInteraction.png "Notional Data Source Interaction")

 
