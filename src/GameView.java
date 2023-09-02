import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameView extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private BoardSquare[][] fields;
    private Label label;
    private GameModel model;
    private Label timer;
    private ChoiceBox<Integer> timeChooser;
    private Button startButton;
    private Button stopButton;

    @Override
    public void start(Stage stage) throws Exception {
        fields = new BoardSquare[GameParams.ROW_COUNT][GameParams.COL_COUNT];
        Scene scene = new Scene(createView(), 490, 670);
        model = new GameModel(this);
        stage.setTitle("Fox and Hounds");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createView() {
        VBox mainView = new VBox();
        mainView.getChildren().addAll(createMenuBox(), createInfoBox(), createBoard(), createTimerBox());
        return mainView;
    }

    private VBox createMenuBox() {

        MenuItem newGame = new MenuItem("New Game");
        MenuItem saveGame = new MenuItem("Save Game");
        MenuItem loadGame = new MenuItem("Load Game");
        MenuItem exit = new MenuItem("Exit");

        newGame.setOnAction(e -> showNewGameDialog());
        saveGame.setOnAction(e -> model.saveGame());
        loadGame.setOnAction(e -> model.loadGame());
        exit.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        Menu menu = new Menu("Game");
        menu.getItems().addAll(newGame, saveGame, loadGame, exit);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);

        VBox menuBox = new VBox();

        menuBox.getChildren().add(menuBar);
        return menuBox;
    }

    private VBox createInfoBox() {
        VBox infoBox = new VBox();
        label = new Label();
        label.setText("To start: Game -> New Game");
        label.setFont(Font.font("Roboto", 24));
        infoBox.setPadding(new Insets(35, 25, 10, 25));
        infoBox.setAlignment(Pos.CENTER);
        infoBox.getChildren().add(label);
        return infoBox;
    }

    private GridPane createBoard() {
        GridPane board = new GridPane();
        board.setAlignment(Pos.CENTER);
        board.setPadding(new Insets(25));
        for (int row = 0; row < GameParams.ROW_COUNT; row++) {
            for (int col = 0; col < GameParams.COL_COUNT; col++) {
                board.add(createBoardSquare(row, col), col, row);
            }
        }

        for (int row = 0; row < GameParams.ROW_COUNT; row++) {
            RowConstraints constraints = new RowConstraints();
            constraints.setPercentHeight(20);
            board.getRowConstraints().add(constraints);
        }

        for (int col = 0; col < GameParams.COL_COUNT; col++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(20);
            board.getColumnConstraints().add(constraints);
        }
        return board;
    }

    private VBox createTimerBox() {

        Label title = new Label("Set Timer for turns (in sec)");
        title.setFont(Font.font("Roboto", 18));

        timer = new Label("10s left");
        timer.setPadding(new Insets(20, 0, 0, 0));
        timer.setFont(Font.font("Roboto", 24));
        timer.setTextFill(Color.GRAY);

        timeChooser = new ChoiceBox(FXCollections.observableArrayList(
                5, 10, 15, 20));
        timeChooser.setValue(10);
        timeChooser.setDisable(true);

        startButton = new Button("Start");
        startButton.setDisable(true);
        stopButton = new Button("Stop");
        stopButton.setDisable(true);

        HBox timerBox = new HBox();
        timerBox.setSpacing(10);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.getChildren().addAll(title, timeChooser, startButton, stopButton);

        startButton.setOnAction(e -> {
            model.startTimer();
        });
        stopButton.setOnAction(e -> {
            model.stopTimer();
        });

        VBox root = new VBox();
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(timerBox, timer);

        return root;
    }

    private Color getSquareBackgroundColor(int row, int col) {
        if ((row % 2 == 0 && col % 2 != 0) || (row % 2 != 0 && col % 2 == 0)) {
            return Color.DARKGRAY;
        } else {
            return Color.LIGHTGRAY;
        }
    }

    private StackPane createBoardSquare(int row, int col) {

        StackPane stackPane = new StackPane();

        BoardSquare square = new BoardSquare(getSquareBackgroundColor(row, col));
        fields[row][col] = square;
        square.setCircleBlue();

        stackPane.setOnMouseClicked(e -> {
            model.attemptMove(row, col);
        });
        stackPane.getChildren().addAll(square);
        return stackPane;
    }

    protected void highlightSquare(int row, int col) {
        fields[row][col].highlight();
    }

    protected void blackenSquare(int row, int col) {
        fields[row][col].blacken();
    }

    protected void showRedCircle(int row, int col) {
        fields[row][col].setCircleRed();
        fields[row][col].showCircle();
    }

    protected void showBlueCircle(int row, int col) {
        fields[row][col].setCircleBlue();
        fields[row][col].showCircle();
    }

    protected void hideCircle(int row, int col) {
        fields[row][col].hideCircle();
    }

    protected void resetView() {
        for (int row = 0; row < GameParams.ROW_COUNT; row++) {
            for (int col = 0; col < GameParams.COL_COUNT; col++) {
                hideCircle(row, col);
                if (fields[row][col].isHighlighted()) {
                    fields[row][col].blacken();
                }
            }
        }
    }

    protected void setLabelText(String text) {
        this.label.setText(text);
    }

    protected void showWinningAlert(Square color) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulation");
        alert.setHeaderText(null);
        alert.setContentText(color + " won!");
        alert.showAndWait();
    }

    protected void showErrorAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void showNewGameDialog() {
        List<Square> choices = new ArrayList<>();
        choices.add(Square.BLUE);
        choices.add(Square.RED);

        ChoiceDialog<Square> dialog = new ChoiceDialog<>(Square.BLUE, choices);
        dialog.setTitle("New Game");
        dialog.setHeaderText("Choose your pawn");
        dialog.setContentText("Do you want to play as \nFox (BLUE) or Hounds (RED)?");

        Optional<Square> result = dialog.showAndWait();
        if (result.isPresent()) {
            model.startNewGame(result.get());
        }
    }

    protected Integer getChosenTime() {
        return timeChooser.getSelectionModel().getSelectedItem();
    }

    protected Label getTimer() {
        return timer;
    }

    protected void enableTimer() {
        startButton.setDisable(false);
        stopButton.setDisable(false);
        timeChooser.setDisable(false);
        timer.setTextFill(Color.BLACK);
    }

    protected void disableTimer() {
        startButton.setDisable(true);
        stopButton.setDisable(true);
        timeChooser.setDisable(true);
        timer.setTextFill(Color.GRAY);
    }
}