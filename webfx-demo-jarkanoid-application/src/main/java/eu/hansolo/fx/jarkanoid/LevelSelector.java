package eu.hansolo.fx.jarkanoid;

import dev.webfx.platform.resource.Resource;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * @author Bruno Salmon
 */
public class LevelSelector extends Pane {

    private static final Font levelFont = Fonts.emulogic(60);

    private final Consumer<Integer> levelConsumer;

    public LevelSelector(Consumer<Integer> levelConsumer) {
        getChildren().setAll(IntStream.range(1, 33).mapToObj(this::createLevelThumb).toArray(Node[]::new));
        this.levelConsumer = levelConsumer;
        setBackground(Background.fill(Color.BLACK));
        setMaxSize(Main.WIDTH, Main.HEIGHT);
        setOnMouseReleased(e -> {
            setVisible(false);
            e.consume();
        });
    }

    @Override
    protected void layoutChildren() {
        int COL = 6, ROW = 6;
        Insets insets = getInsets();
        double width = getWidth() - insets.getLeft() - insets.getRight(), height = getHeight() - insets.getTop() - insets.getBottom(), w = width / COL, h = height / ROW;
        int row = 0, col = 0;
        for (Node child : getManagedChildren()) {
            layoutInArea(child, insets.getLeft() + col * w, insets.getTop() + row * h, w, h, 0, HPos.CENTER, VPos.CENTER);
            if (++col >= COL) {
                col = 0;
                row++;
            }
        }
    }

    private Node createLevelThumb(int level) {
        Text text = new Text("" + level);
        text.setFont(levelFont);
        text.setFill(Color.WHITE);
        Image image = new Image(Resource.toUrl("thumbs/Level" + (level < 10 ? "0" : "") + level + ".png", getClass()), true);
        StackPane imageAndText = new StackPane(new ImageView(image), text);
        imageAndText.setOnMouseReleased(e -> {
            levelConsumer.accept(level);
            e.consume();
        });
        imageAndText.setCursor(Cursor.HAND);
        ScalePane thumb = new ScalePane(imageAndText);
        StackPane.setAlignment(text, Pos.BOTTOM_CENTER);
        StackPane.setMargin(text, new Insets(80));
        thumb.setPadding(new Insets(0, 1, 0, 1));
        return thumb;
    }
}
