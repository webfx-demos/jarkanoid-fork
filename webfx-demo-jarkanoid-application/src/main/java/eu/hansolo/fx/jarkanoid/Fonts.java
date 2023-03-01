package eu.hansolo.fx.jarkanoid;

import dev.webfx.platform.resource.Resource;
import javafx.scene.text.Font;


public class Fonts {
    private static final String EMULOGIC_NAME;
    private static       String emulogicName;

    private Fonts() {}


    static {
        try {
            emulogicName  = Font.loadFont(Resource.toUrl("Emulogic-zrEw.ttf", Main.class), 10).getName();
            emulogicName = "Emulogic";
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        EMULOGIC_NAME   = emulogicName;
    }


    // ******************** Methods *******************************************
    public static Font emulogic(final double size) { return new Font(EMULOGIC_NAME, size); }

}
