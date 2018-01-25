package io.opensphere.wps.ui.detail.provider;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * A GUICE Module in which {@link WpsInputControlProvider} instances are bound using the {@link javax.inject.Named} annotation to
 * their respective datatypes. In the future, these dependencies should be either injected or set via configuration.
 */
public class WpsInputControlProviderModule extends AbstractModule
{
    /**
     * {@inheritDoc}
     *
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure()
    {
        bind(WpsInputControlProvider.class).annotatedWith(Names.named("string")).to(WpsStringInputProvider.class);
        bind(WpsInputControlProvider.class).annotatedWith(Names.named("boolean")).to(WpsBooleanInputProvider.class);
        bind(WpsInputControlProvider.class).annotatedWith(Names.named("date")).to(WpsDateInputProvider.class);
        bind(WpsInputControlProvider.class).annotatedWith(Names.named("time")).to(WpsTimeInputProvider.class);
        bind(WpsInputControlProvider.class).annotatedWith(Names.named("dateTime")).to(WpsDateTimeInputProvider.class);
        bind(WpsInputControlProvider.class).annotatedWith(Names.named("integer")).to(WpsIntegerInputProvider.class);
        bind(WpsInputControlProvider.class).annotatedWith(Names.named("double")).to(WpsDecimalInputProvider.class);
        bind(WpsInputControlProvider.class).annotatedWith(Names.named("BBOX")).to(WpsBoundingBoxInputProvider.class);
        bind(WpsInputControlProvider.class).annotatedWith(Names.named("color")).to(WpsColorInputProvider.class);
    }
}
