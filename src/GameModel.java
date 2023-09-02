import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;

public class GameModel {

    public class TimerDisplayHandler implements EventHandler<ActionEvent> {

        private Label display;
        private int milis;
        private int seconds;


        public TimerDisplayHandler(Label label, int countdownTime) {
            this.display = label;
            reset(countdownTime);
        }

        public void reset(int countdownTime) {
            milis = 0;
            seconds = countdownTime;
            updateDisplay();
        }

        @Override
        public void handle(ActionEvent arg0) {
            milis++;
            if (milis == 1000 && seconds > 0) {
                milis = 0;
                seconds--;
            }
            if (seconds == 0) {
                changeTurns();
            }
            updateDisplay();
        }

        private void updateDisplay() {
            display.setText(String.format("%02d", seconds) + "s left");
        }
    };

    private GameView view;
    private Square[][] board;
    private Square turn;
    private Integer[] selectedSquarePos;
    private ArrayList<Integer[]> possibleMoves;
    private TimerDisplayHandler displayHandler;
    private KeyFrame keyFrame;
    private Timeline timer;

    public GameModel(GameView view) {
        this.view = view;
        board = new Square[GameParams.ROW_COUNT][GameParams.COL_COUNT];
        possibleMoves = new ArrayList<>();
        displayHandler = new TimerDisplayHandler(view.getTimer(), view.getChosenTime());
        keyFrame = new KeyFrame(Duration.millis(1), displayHandler);
        timer = new Timeline(keyFrame);
        timer.setCycleCount(Animation.INDEFINITE);

        resetModel(Square.BLUE);
    }

    public void resetModel(Square chosenColor) {
        turn = chosenColor;
        selectedSquarePos = null;
        possibleMoves.clear();
        resetBoard();
    }
    private void resetBoard() {
        for (int row = 0; row < board.length; row = row + 2) {
            for (int col = 1; col < board[row].length; col = col + 2) {
                board[row][col] = Square.EMPTY;
            }
        }
        for (int row = 1; row < board.length; row = row + 2) {
            for (int col = 0; col < board[row].length; col = col + 2) {
                board[row][col] = Square.EMPTY;
            }
        }
    }

    protected void startNewGame(Square chosenColor) {
        resetModel(chosenColor);
        view.resetView();
        for (int i=1; i<=board.length; i= i+2) {
            board[0][i] = Square.RED;
            view.showRedCircle(0,i);
        }
        board[7][4] = Square.BLUE;
        view.showBlueCircle(7,4);
        view.setLabelText("Your turn " + chosenColor + "!");
        view.enableTimer();
    }

    private boolean checkTurn(int row, int col) {
        return board[row][col] == turn;
    }
    private void setSelectedSquarePos(int row, int col) {
        selectedSquarePos = new Integer[]{row, col};
    }
    private void tryToSetEmptySquares(int row, int col) {
        try {
            if (board[row][col] == Square.EMPTY) {
                possibleMoves.add(new Integer[]{row, col});
            }
        } catch(ArrayIndexOutOfBoundsException e) {}
    }
    private void findPossibleMoves(int row, int col) {
        if (turn == Square.BLUE) {
            tryToSetEmptySquares(row + 1, col + 1);
            tryToSetEmptySquares(row + 1, col - 1);
            tryToSetEmptySquares(row - 1, col + 1);
            tryToSetEmptySquares(row - 1, col - 1);
        } else {
            tryToSetEmptySquares(row + 1, col + 1);
            tryToSetEmptySquares(row + 1, col - 1);
        }
    }
    private void highlightPossibleMoves() {
        for (int i=0; i<possibleMoves.size(); i++) {
            view.highlightSquare(possibleMoves.get(i)[0],possibleMoves.get(i)[1]);
        }
        view.highlightSquare(selectedSquarePos[0],selectedSquarePos[1]);
    }
    private void blackenPossibleMoves() {
        for (int i=0; i<possibleMoves.size(); i++) {
            view.blackenSquare(possibleMoves.get(i)[0],possibleMoves.get(i)[1]);
        }
        if (selectedSquarePos != null) {
            view.blackenSquare(selectedSquarePos[0],selectedSquarePos[1]);
        }
    }
    private boolean isPossibleMoveSelected(int row, int col) {
        for (int i=0; i<possibleMoves.size(); i++) {
            if (possibleMoves.get(i)[0] == row && possibleMoves.get(i)[1] == col) {
                return true;
            }
        }
        return false;
    }
    private void clearSelection() {
        blackenPossibleMoves();
        selectedSquarePos = null;
        possibleMoves.clear();
    }

    private void checkForBlueWin() {
        for (int i=1; i<=board.length; i= i+2) {
            if (board[0][i] == Square.BLUE) {
                view.resetView();
                view.setLabelText("");
                view.disableTimer();
                stopTimer();
                view.showWinningAlert(Square.BLUE);
            }
        }
    }

    private Integer[] getBluePawnPos() {
        for (int row = 0; row < board.length; row = row + 1) {
            for (int col = 0; col < board[row].length; col = col + 1) {
                if(board[row][col] == Square.BLUE) {
                    return new Integer[]{row, col};
                }
            }
        }
        return new Integer[]{};
    }
    private ArrayList<Integer[]> getBluePawnPossibleMoves (int row,int col) {
        ArrayList<Integer[]> result = new ArrayList<>();
        try {
            if (board[row+1][col+1] == Square.EMPTY) {
                result.add(new Integer[]{row, col});
            }
        } catch(ArrayIndexOutOfBoundsException e) {}
        try {
            if (board[row+1][col-1] == Square.EMPTY) {
                result.add(new Integer[]{row, col});
            }
        } catch(ArrayIndexOutOfBoundsException e) {}
        try {
            if (board[row-1][col+1] == Square.EMPTY) {
                result.add(new Integer[]{row, col});
            }
        } catch(ArrayIndexOutOfBoundsException e) {}
        try {
            if (board[row-1][col-1] == Square.EMPTY) {
                result.add(new Integer[]{row, col});
            }
        } catch(ArrayIndexOutOfBoundsException e) {}
        return result;
    }
    private void checkForRedWin() {
        if (getBluePawnPos().length > 0) {
            int bluePawnPosX = getBluePawnPos()[0];
            int bluePawnPosY = getBluePawnPos()[1];
            if(getBluePawnPossibleMoves(bluePawnPosX, bluePawnPosY).size() == 0) {
                view.resetView();
                view.setLabelText("");
                view.disableTimer();
                stopTimer();
                view.showWinningAlert(Square.RED);
            };
        }

    }

    private void makeMove(int row, int col) {
        view.hideCircle(selectedSquarePos[0], selectedSquarePos[1]);
        board[selectedSquarePos[0]][selectedSquarePos[1]] = Square.EMPTY;
        if (turn == Square.BLUE) {
            view.showBlueCircle(row,col);
            board[row][col] = Square.BLUE;
            turn = Square.RED;
            view.setLabelText("Your turn RED!");
            checkForBlueWin();
        } else {
            view.showRedCircle(row,col);
            board[row][col] = Square.RED;
            turn = Square.BLUE;
            view.setLabelText("Your turn BLUE!");
            checkForRedWin();
        }
        clearSelection();
        resetTimer();
    }
    protected void attemptMove(int row, int col) {
        if (selectedSquarePos != null) {
            if (!isPossibleMoveSelected(row, col)) {
                clearSelection();
            } else {
                makeMove(row, col);
            }
        }

        if (!checkTurn(row, col)) {
            return;
        }

        setSelectedSquarePos(row, col);
        findPossibleMoves(row, col);
        highlightPossibleMoves();
    }

    protected void exportToFile(String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            DataOutputStream dos = new DataOutputStream(fos);

            for (int row = 0; row < board.length; row = row + 1) {
                for (int col = 0; col < board[row].length; col = col + 1) {
                    dos.writeUTF(board[row][col] == null ? "null" : board[row][col].name());
                }
                dos.writeUTF("\n");
            }
            dos.writeUTF(turn.name());
            dos.flush();
            dos.close();
        } catch (Exception e) {
            view.showErrorAlert("Saving unsuccessful :(");
        }
    }
    protected void saveGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Save Files", "*.save")
        );
        fileChooser.setInitialFileName("FoxHounds.save");
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
                exportToFile(selectedFile.toString());
        }
    }

    protected String importFromFile(String path) {
        try {
            FileInputStream is = new FileInputStream(path);
            DataInputStream dis = new DataInputStream(is);
            StringBuffer file = new StringBuffer();
            String tmp;
            while (dis.available()>0) {
                tmp = dis.readUTF();
                file.append(tmp + " ");
            }
            dis.close();
            return file.toString();
        } catch (Exception e) {
            view.showErrorAlert("Loading unsuccessful :(");
        }
        return "";
    }
    protected boolean validateLoadedFile(String file) {
        String[] parts = file.split("\\s+");

        // 64 board squares + info who's turn
        if (parts.length != 65) {
            return false;
        }
        int blueCounter = 0, redCounter = 0, emptyCounter = 0;

        for (int i=0; i<parts.length-1; i++) {
            if ( parts[i].equals("BLUE") ) {
                blueCounter++;
            }
            if ( parts[i].equals("RED") ) {
                redCounter++;
            }
            if ( parts[i].equals("EMPTY") ) {
                emptyCounter++;
            }
        }

        // should be - red: 4 | blue: 1 | empty: 27
        if (blueCounter != 1 || redCounter != 4 || emptyCounter != 27) {
            return false;
        }

        // checking if who's turn element is valid
        if (!(parts[64].equals("RED") || parts[64].equals("BLUE"))) {
            return false;
        }

        return true;
    }
    protected Square[][] createBoard(String file) {
        Square[][] newBoard = new Square[GameParams.ROW_COUNT][GameParams.COL_COUNT];
        String[] parts = file.split("\\s+");
        int counter = 0;

        for (int row = 0; row < newBoard.length; row++) {
            for (int col = 0; col < newBoard[row].length; col++) {

                if (!parts[counter].equals("null")) {
                    newBoard[row][col] = Enum.valueOf(
                            Square.class,
                            parts[counter]
                    );
                }
                counter++;
            }
        }
        return newBoard;
    }
    protected Square getTurnFromFile(String file) {
        String[] parts = file.split("\\s+");
        return Enum.valueOf(
                Square.class,
                parts[64]
        );
    }

    protected void loadGame() {
        FileChooser fileChooser = new FileChooser();
        String fileInString = "";
        fileChooser.setTitle("Load Game");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Save Files", "*.save")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            fileInString = importFromFile(selectedFile.toString());
        }

        if (validateLoadedFile(fileInString)) {
            resetModel(getTurnFromFile(fileInString));
            view.setLabelText("Your turn " + getTurnFromFile(fileInString).name() + "!");
            view.resetView();
            board = createBoard(fileInString);
            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[row].length; col++) {
                    if (board[row][col] == Square.RED) {
                        view.showRedCircle(row,col);
                    }
                    if (board[row][col] == Square.BLUE) {
                        view.showBlueCircle(row,col);
                    }
                }
            }
            view.enableTimer();
        } else {
            view.showErrorAlert("Selected file is not valid :(");
        }


    }

    protected void startTimer() { timer.play(); }
    protected void stopTimer() {
        timer.stop();
        displayHandler.reset(0);
    }
    protected void resetTimer() {
        displayHandler.reset(view.getChosenTime());
    }

    protected void changeTurns () {
        if (turn == Square.BLUE) {
            turn = Square.RED;
            view.setLabelText("Your turn RED!");
        } else {
            turn = Square.BLUE;
            view.setLabelText("Your turn BLUE!");
        }
        if (selectedSquarePos != null) {
            clearSelection();
        }
        resetTimer();
    }

}