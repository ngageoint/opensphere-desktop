package io.opensphere.featureactions.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 *
 */
@XmlRegistry
public class FeatureActionsFactory
{
    /** The {@link QName} instance for feature actions. */
    private final static QName FEATURE_ACTIONS_QNAME = new QName("http://www.bit-sys.com/state/v2", "featureActions");

    /** The {@link QName} instance for a single feature action. */
    private final static QName FEATURE_ACTION_QNAME = new QName("http://www.bit-sys.com/state/v2", "featureActions");

    /**
     * Create a new {@link FeatureActionsFactory} that can be used to create new
     * instances of schema derived classes for package
     * io.opensphere.featureactions.model.
     */
    public FeatureActionsFactory()
    {
        /* intentionally blank */
    }

    /**
     * Create an instance of {@link FeatureAction}.
     *
     * @return a new instance of {@link FeatureAction}.
     */
    public FeatureAction createFeatureAction()
    {
        return new FeatureAction();
    }

    /**
     * Create an instance of {@link FeatureActions}.
     *
     * @return a new instance of {@link FeatureActions}.
     */
    public FeatureActions createFeatureActions()
    {
        return new FeatureActions();
    }

    /**
     * Create an instance of {@link LabelAction}.
     *
     * @return a new instance of {@link LabelAction}.
     */
    public LabelAction createLabelAction()
    {
        return new LabelAction();
    }

    /**
     * Create an instance of {@link StyleAction}.
     *
     * @return a new instance of {@link StyleAction}.
     */
    public StyleAction createStyleAction()
    {
        return new StyleAction();
    }

    /**
     * Create an instance of {@link CustomColumnAction}.
     *
     * @return a new instance of {@link CustomColumnAction}.
     */
    public CustomColumnAction createCustomColumnAction()
    {
        return new CustomColumnAction();
    }

    /**
     * Create an instance of
     * {@link JAXBElement}{@code <}{@link FeatureActions}{@code >}
     *
     * @param value the value of the {@link JAXBElement} to create.
     * @return a JAXBElement populated with the correct namespace and value.
     *
     */
    @XmlElementDecl(namespace = "http://www.bit-sys.com/state/v2", name = "featureActions")
    public JAXBElement<FeatureActions> createFeatureActions(FeatureActions value)
    {
        return new JAXBElement<>(FEATURE_ACTIONS_QNAME, FeatureActions.class, null, value);
    }

    /**
     * Create an instance of
     * {@link JAXBElement}{@code <}{@link FeatureAction}{@code >}
     *
     * @param value the value of the {@link JAXBElement} to create.
     * @return a JAXBElement populated with the correct namespace and value.
     */
    @XmlElementDecl(namespace = "http://www.bit-sys.com/state/v2", name = "featureAction")
    public JAXBElement<FeatureAction> createFeatureAction(FeatureAction value)
    {
        return new JAXBElement<>(FEATURE_ACTION_QNAME, FeatureAction.class, null, value);
    }
}
