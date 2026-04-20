module ai.auralog.slf4j {
    requires ai.auralog.core;
    requires org.slf4j;
    requires static org.jspecify;

    exports ai.auralog.slf4j;

    provides org.slf4j.spi.SLF4JServiceProvider
        with ai.auralog.slf4j.AuralogSlf4jServiceProvider;
}
