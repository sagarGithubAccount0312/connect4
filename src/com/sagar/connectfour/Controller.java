package com.sagar.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int CIRCLE_DIAMETER = 80;
    private static final String discColor1 = "#24303E";
    private static final String discColor2 = "#4CAA88";

    private static   String playerOne = "Player One";
    private static String playerTwo = "Player Two";
    private boolean isPlayerOneTurn = true;
    private boolean isAllowed = true;

    private Disc[][] insertedDiscArray = new Disc[ROWS][COLUMNS];






    @FXML
    public GridPane rootGridPane;
    @FXML
    public Pane insertedDiskPane;
    @FXML
    public Label playerName;
    @FXML
    public TextField firstPlayerName;
    @FXML
    public TextField secondPlayerName;
    @FXML
    public Button setButton;


    public void createPlayGround(){
        Shape rectangleWithHoles = createGameStructuralGrid();


        rootGridPane.add(rectangleWithHoles,0,1);
       List<Rectangle> rectangleList = createClickableColumn();
       for(Rectangle rectangle: rectangleList){
           rootGridPane.add(rectangle,0,1);
       }


    }
    private Shape createGameStructuralGrid(){
        Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);
        for(int row = 0; row < ROWS; row++){
            for(int col = 0; col < COLUMNS; col++){
                Circle circle = new Circle();
                circle.setRadius(CIRCLE_DIAMETER/2);
                circle.setCenterX(CIRCLE_DIAMETER/2);
                circle.setCenterY(CIRCLE_DIAMETER/2);
                circle.setSmooth(true);

                circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + 20);
                circle.setCenterY(row * (CIRCLE_DIAMETER + 5) + 60);

                rectangleWithHoles = Shape.subtract(rectangleWithHoles,circle);
            }
        }

        rectangleWithHoles.setFill(Color.WHITE);
        return rectangleWithHoles;

    }
    private List<Rectangle> createClickableColumn(){
        List<Rectangle> rectangleList = new ArrayList<>();
        for(int col=0; col<COLUMNS;col++){
            Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + 20);
            rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
            rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));
            final   int column = col;
            rectangle.setOnMouseClicked(event -> {
                if (isAllowed) {
                    isAllowed = false;
                    insertDisc(new Disc(isPlayerOneTurn), column);


                }
            });
            rectangleList.add(rectangle);

        }

        return rectangleList;

    }

    private void insertDisc(Disc disc,int col){
        int row = ROWS-1;

        while ( row >= 0){

            if (getDiscIfPresent(row,col) == null){
                break;
            }
            row--;
        }
        if (row < 0){
            isAllowed = true;
            return ;
        }


        insertedDiscArray[row][col] = disc;
        insertedDiskPane.getChildren().add(disc);
        disc.setTranslateX(col * (CIRCLE_DIAMETER + 5)+ 20);
         int currentRow = row;
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3),disc);
        translateTransition.setByY(row* (CIRCLE_DIAMETER + 5) + 20);
        translateTransition.setOnFinished(event -> {
            isAllowed = true;
            if (gameEnded(currentRow,col)){

                gameOver();
                return;
            }
            isPlayerOneTurn = !isPlayerOneTurn;
            playerName.setText(isPlayerOneTurn? playerOne: playerTwo);
        });
        translateTransition.play();


    }

    private boolean gameEnded(int row, int col){
        List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3,row + 3)
                                       .mapToObj(r -> new Point2D(r,col))
                                       .collect(Collectors.toList());
        List<Point2D> horizontalPoints = IntStream.rangeClosed(col-3,col+3)
                                        .mapToObj(c -> new Point2D(row,c))
                                        .collect(Collectors.toList());
        Point2D startPoint1 = new Point2D(row - 3, col +3);
        List<Point2D> diagonal1Vertex = IntStream.rangeClosed( 0, 6)
                                       .mapToObj(i-> startPoint1.add(i, -i))
                                       .collect(Collectors.toList());
        Point2D startPoint2 = new Point2D(row -  3, col - 3);
        List<Point2D> diagonal2Vertex = IntStream.rangeClosed( 0, 6)
                .mapToObj(i-> startPoint2.add(i, i))
                .collect(Collectors.toList());

        boolean isEnded = checkCombination(verticalPoints) || checkCombination(horizontalPoints)
                          || checkCombination(diagonal1Vertex) || checkCombination(diagonal2Vertex);

        return isEnded;

    }

    private boolean checkCombination(List<Point2D> points) {

        int chain = 0;
        for (Point2D point: points){

            int rowIndexForArray = (int) point.getX();
            int columnIndexArray = (int) point.getY();
            Disc disc = getDiscIfPresent(rowIndexForArray,columnIndexArray);
            if(disc !=null && disc.isPlayerOneMove == isPlayerOneTurn){
                chain++;
                if(chain==4){
                    return true;
                }
            }else {
                chain =0;
            }

        }
        return false;
    }

    private Disc getDiscIfPresent(int row, int col){

        if (row >= ROWS || row < 0 || col >= COLUMNS || col < 0)
            return null;
        return insertedDiscArray[row][col];
    }
    private void gameOver(){
        String winner = isPlayerOneTurn?  playerOne : playerTwo;
        System.out.println("Winner is: "+ winner);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect four");
        alert.setHeaderText("The winner is " + winner);
        alert.setContentText("Want to play again?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("NO, Exit");
        alert.getButtonTypes().setAll(yesButton,noButton);
       Platform.runLater(()-> {
           Optional<ButtonType> clickedButton =  alert.showAndWait();
           if (clickedButton.isPresent() && clickedButton.get() == yesButton){
               resetGame();

           } else{
               Platform.exit();
               System.exit(0);
           }
       });
    }

    public void resetGame() {
        insertedDiskPane.getChildren().clear();
        for (int row = 0; row < insertedDiscArray.length; row++) {
            for (int col = 0; col < insertedDiscArray[row].length; col++) {

                insertedDiscArray[row][col] = null;

            }

        }
        isPlayerOneTurn = true;
        playerName.setText(playerOne);
        createPlayGround();


    }


    public static class Disc extends Circle {
        private final boolean isPlayerOneMove;

        public Disc(boolean isPlayerOneMove){

            this.isPlayerOneMove = isPlayerOneMove;
            setRadius(CIRCLE_DIAMETER/2);
            setFill(isPlayerOneMove? Color.valueOf(discColor1): Color.valueOf(discColor2));
            setCenterX(CIRCLE_DIAMETER/2);
            setCenterY(CIRCLE_DIAMETER/2);

        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setButton.setOnAction(event -> {
            String string1 = firstPlayerName.getText();
            String string2 = secondPlayerName.getText();
            if (!string1.isEmpty() && !string2.isEmpty()) {
                playerOne = string1;
                playerTwo = string2;
                playerName.setText(playerOne);
            }

        });

    }
}
