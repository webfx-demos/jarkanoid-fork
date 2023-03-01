// File managed by WebFX (DO NOT EDIT MANUALLY)

module webfx.demo.jarkanoid.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires webfx.extras.scalepane;
    requires webfx.kit.util.scene;
    requires webfx.platform.audio;
    requires webfx.platform.resource;
    requires webfx.platform.scheduler;
    requires webfx.platform.shutdown;
    requires webfx.platform.storage;
    requires webfx.platform.useragent;
    requires webfx.platform.windowlocation;

    // Exported packages
    exports eu.hansolo.fx.jarkanoid;

    // Resources packages
    opens eu.hansolo.fx.jarkanoid;

    // Provided services
    provides javafx.application.Application with eu.hansolo.fx.jarkanoid.Main;

}