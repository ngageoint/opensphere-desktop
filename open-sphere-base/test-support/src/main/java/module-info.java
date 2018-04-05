/**
 *
 */
module opensphere.testsupport
{
    exports io.opensphere.test.core.server;

    exports io.opensphere.test.core.matchers;

    exports io.opensphere.test.core.testutils;

    exports io.opensphere.test;

    exports io.opensphere.test.core;

    requires annotations;

    requires commons.collections;

    requires easymock;

    requires java.desktop;

    requires java.xml;

    requires java.xml.bind;

    requires jdk.httpserver;

    requires junit;

    requires log4j;
}