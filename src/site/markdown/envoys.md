#Envoys
An Envoy is a component that retrieves data and creates models to represent them in the system. The models are published to the data registry. The models are of arbitrary type by design to allow for serialized objects from external sources to be used. All Envoy implementations should implement the `io.opensphere.core.api.Envoy` interface.
