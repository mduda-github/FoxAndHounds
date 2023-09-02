import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BoardSquare extends StackPane {

    private Color color;
    private Circle circle;
    protected boolean isHighlighted;

    public BoardSquare(Color defaultColor) {
        color = defaultColor;
        setColor(color);
        isHighlighted = false;
        createCircle();
        getChildren().add(circle);
        setPrefSize(50,50);
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public void highlight() {
        setColor(Color.GRAY);
        setHighlighted(true);
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void blacken() {
        setColor(Color.DARKGRAY);
        setHighlighted(false);
    }

    public void setColor(Color color) {
        BackgroundFill bgFill = new BackgroundFill(color, CornerRadii.EMPTY, new Insets(1));
        Background bg = new Background(bgFill);
        setBackground(bg);
    }

    public void createCircle() {
        circle = new Circle();
        NumberBinding radiusProperty = Bindings.when(this.widthProperty().greaterThan(this.heightProperty()))
                .then(this.heightProperty().subtract(12).divide(2))
                .otherwise(this.heightProperty().subtract(12).divide(2));
        circle.radiusProperty().bind(radiusProperty);
        circle.setVisible(false);
    }

    public void showCircle() {
        circle.setVisible(true);
    }
    public void hideCircle() {
        circle.setVisible(false);
    }

    public void setCircleRed() { circle.setFill(Color.RED); }
    public void setCircleBlue() { circle.setFill(Color.BLUE); }
}
