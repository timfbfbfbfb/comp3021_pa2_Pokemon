package pokemon.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pokemon.game.*;

import java.io.File;
import java.util.HashMap;

import static javafx.scene.input.KeyCode.*;

public class PokemonScreen extends Application {

    private Game game;

    private enum View {TREE, EXIT, BALL, PATH, POKE}

    public enum Msg {NONE, UNCAUGHT, CAUGHT, ENDGAME}

    private static final int STEP_SIZE = 40;
    private final BorderPane mainPane;
    private final GridPane mapPane;
    private final VBox scorePane;
    private final Button resumeBtn, pauseBtn;
    private final ImageView avatar;
    private final Stage catchWindow;

    // this are the urls of the images
    private static final String avatarFront = new File("icons/front.png").toURI().toString();
    private static final String avatarBack = new File("icons/back.png").toURI().toString();
    private static final String avatarLeft = new File("icons/left.png").toURI().toString();
    private static final String avatarRight = new File("icons/right.png").toURI().toString();
    private static final String treePath = new File("icons/tree.png").toURI().toString();
    private static final String exitPath = new File("icons/exit.png").toURI().toString();
    private static final String ballPath = new File("icons/ball_ani.gif").toURI().toString();
    private static final String catchFailedAnimationPath = new File("icons/catch_fail.gif").toURI().toString();
    private static final String catchSuccessfulAnimationPath = new File("icons/catch_successful.gif").toURI().toString();

    private boolean avatarPause = false;
    private boolean gamePause = false;
    private KeyCode lastKeyPressed = null;
    private final HashMap<Pokemon, Node> pokemonViews;
    private final HashMap<Station, Node> stationViews;
    private Thread catchAnimationThread;

    {
        avatar = new ImageView(new Image(avatarFront));
        avatar.setFitWidth(STEP_SIZE);
        avatar.setFitHeight(STEP_SIZE);
        avatar.setPreserveRatio(true);

        catchWindow = new Stage();
        catchWindow.setOnCloseRequest(e -> {
            catchAnimationThread.interrupt();
        });

        game = new Game();
        pokemonViews = new HashMap<>();
        stationViews = new HashMap<>();

        mainPane = new BorderPane();
        mapPane = new GridPane();

        resumeBtn = new Button("Resume");
        resumeBtn.setFocusTraversable(false);
        resumeBtn.setOnAction(e -> {
            if (gamePause) {
                gamePause = false;
                PokemonRunnable.gamePause = StationRunnable.gamePause = false;
            }
        });
        pauseBtn = new Button("Pause");
        pauseBtn.setFocusTraversable(false);
        pauseBtn.setOnAction(e -> {
            if (!gamePause) {
                gamePause = true;
                PokemonRunnable.gamePause = StationRunnable.gamePause = true;
            }
        });

        scorePane = new VBox();
        scorePane.setPadding(new Insets(15));
    }

    /**
     * Constructor
     */
    public PokemonScreen() {
        File file = new File("sampleIn.txt");
        try {
            game.initialize(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map map = game.map;
        Cell dimension = map.getDimension();
        char[][] mapArr = map.getMap();
        for (int i = 0; i < dimension.getM(); i++)
            for (int j = 0; j < dimension.getN(); j++) {
                switch (mapArr[i][j]) {
                    case Map.PATH:
                        mapPane.add(viewFactory(View.PATH), j, i);
                        break;
                    case Map.DEST:
                        mapPane.add(viewFactory(View.EXIT), j, i);
                        break;
                    case Map.SUPP:
                        mapPane.add(viewFactory(View.PATH), j, i);
                        stationViews.put(map.getStation(new Cell(i, j)), viewFactory(View.BALL));
                        mapPane.add(stationViews.get(map.getStation(new Cell(i, j))), j, i);
                        break;
                    case Map.WALL:
                        mapPane.add(viewFactory(View.TREE), j, i);
                        break;
                    case Map.POKE:
                        mapPane.add(viewFactory(View.PATH), j, i);
                        pokemonViews.put(map.getPokemon(new Cell(i, j)), viewFactory(View.POKE, new Cell(i, j)));
                        mapPane.add(pokemonViews.get(map.getPokemon(new Cell(i, j))), j, i);
                        break;
                    case Map.START:
                        mapPane.add(viewFactory(View.PATH), j, i);
                        mapPane.add(avatar, j, i);
                        game.map.setMap(new Cell(i, j), Map.PATH);
                        break;
                }
            }
        game.player.move(map.getStart(), map);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainPane.setCenter(mapPane);
        mainPane.setRight(scorePane);
        updateScorePane(Msg.NONE);

        Scene scene = new Scene(mainPane);

        scene.setOnKeyPressed(e -> {
            if (!avatarPause && !gamePause) {
                Cell pos = game.player.currentPos();
                Map map = game.map;
                synchronized (mapPane) {
                    switch (e.getCode()) {
                        case UP:
                            avatar.setImage(new Image(avatarBack));
                            if (!map.isOutOfBound(pos.up()) && !map.isWall(pos.up())) {
                                game.player.move(pos.up(), map);
                                mapPane.getChildren().remove(avatar);
                                mapPane.add(avatar, pos.up().getN(), pos.up().getM());
                            }
                            lastKeyPressed = UP;
                            break;
                        case DOWN:
                            avatar.setImage(new Image(avatarFront));
                            if (!map.isOutOfBound(pos.down()) && !map.isWall(pos.down())) {
                                game.player.move(pos.down(), map);
                                mapPane.getChildren().remove(avatar);
                                mapPane.add(avatar, pos.down().getN(), pos.down().getM());
                            }
                            lastKeyPressed = DOWN;
                            break;
                        case LEFT:
                            avatar.setImage(new Image(avatarLeft));
                            if (!map.isOutOfBound(pos.left()) && !map.isWall(pos.left())) {
                                game.player.move(pos.left(), map);
                                mapPane.getChildren().remove(avatar);
                                mapPane.add(avatar, pos.left().getN(), pos.left().getM());
                            }
                            lastKeyPressed = LEFT;
                            break;
                        case RIGHT:
                            avatar.setImage(new Image(avatarRight));
                            if (!map.isOutOfBound(pos.right()) && !map.isWall(pos.right())) {
                                game.player.move(pos.right(), map);
                                mapPane.getChildren().remove(avatar);
                                mapPane.add(avatar, pos.right().getN(), pos.right().getM());
                            }
                            lastKeyPressed = RIGHT;
                            break;
                    }
                    if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT)
                        updateScorePane(Msg.NONE);
                }

                if (lastKeyPressed == UP || lastKeyPressed == DOWN || lastKeyPressed == LEFT || lastKeyPressed == RIGHT) {
                    if (map.isDestination(game.player.currentPos())) {
                        updateScorePane(Msg.ENDGAME);
                        resumeBtn.setDisable(true);
                        pauseBtn.setDisable(true);
                        avatarPause = true;
                        gamePause = true;
                        PokemonRunnable.gamePause = StationRunnable.gamePause = true;
                    }
                    avatarPause = true;
                }

            }
        });

        scene.setOnKeyReleased(e ->
        {
            if (e.getCode() == lastKeyPressed)
                avatarPause = false;
        });

        stage.setScene(scene);
        stage.show();
        for (java.util.Map.Entry<Pokemon, Node> entry : pokemonViews.entrySet()) {
            Thread t = new Thread(new PokemonRunnable(entry.getKey(), mapPane, pokemonViews, game, this));
            t.setDaemon(true);
            t.start();
        }
        for (java.util.Map.Entry<Station, Node> entry : stationViews.entrySet()) {
            Thread t = new Thread(new StationRunnable(entry.getKey(), mapPane, stationViews, game, this));
            t.setDaemon(true);
            t.start();
        }
    }

    private Node viewFactory(View k) {
        switch (k) {
            case TREE:
                ImageView TREE = new ImageView(new Image(treePath));
                TREE.setFitHeight(STEP_SIZE);
                TREE.setFitWidth(STEP_SIZE);
                TREE.setPreserveRatio(true);
                return TREE;

            case EXIT:
                ImageView EXIT = new ImageView(new Image(exitPath));
                EXIT.setFitHeight(STEP_SIZE);
                EXIT.setFitWidth(STEP_SIZE);
                EXIT.setPreserveRatio(true);
                return EXIT;

            case BALL:
                ImageView BALL = new ImageView(new Image(ballPath));
                BALL.setFitHeight(STEP_SIZE);
                BALL.setFitWidth(STEP_SIZE);
                BALL.setPreserveRatio(true);
                return BALL;

            case PATH:
                Pane PATH = new Pane();
                PATH.setPadding(new Insets(STEP_SIZE / 2f));
                return PATH;

            default:
                return null;
        }
    }

    private Node viewFactory(View k, Cell cell) {
        switch (k) {
            case POKE:
                Pokemon pokemon = game.map.getPokemon(cell);
                int id = PokemonList.getIdOfFromName(pokemon.getPokemonName());
                Image img = new Image(new File("icons/" + id + ".png").toURI().toString());
                ImageView pokemonImg = new ImageView(img);
                pokemonImg.setFitHeight(STEP_SIZE);
                pokemonImg.setFitWidth(STEP_SIZE);
                pokemonImg.setPreserveRatio(true);
                return pokemonImg;
            default:
                return viewFactory(k);
        }
    }

    public void updateScorePane(Msg msg) {
        synchronized (scorePane) {
            Player player = game.player;
            Label line1 = new Label("Current Score: " + player.getScore());
            Label line2 = new Label("# of Pokemons caught: " + player.getNumOfPokemons());
            Label line3 = new Label("# of Pokeballs owned: " + player.getNumOfBalls());
            Label line4 = new Label();
            switch (msg) {
                case NONE:
                    line4.setText("");
                    line4.setTextFill(Color.valueOf("black"));
                    break;
                case UNCAUGHT:
                    line4.setText("NOT enough pokemon ball");
                    line4.setTextFill(Color.valueOf("red"));
                    break;
                case CAUGHT:
                    line4.setText("Pokemon caught!");
                    line4.setTextFill(Color.valueOf("green"));
                    break;
                case ENDGAME:
                    line4.setText("End Game!");
                    line4.setTextFill(Color.valueOf("lightgreen"));
                    break;
            }
            HBox btnGp = new HBox(resumeBtn, pauseBtn);
            btnGp.setSpacing(10);
            scorePane.getChildren().clear();
            scorePane.getChildren().addAll(line1, line2, line3, line4, btnGp);
            scorePane.setSpacing(10);
        }
    }

    public void showCatchAnimation(boolean caught) {
        BorderPane bp = new BorderPane(new ImageView(new Image(caught ? catchSuccessfulAnimationPath : catchFailedAnimationPath)));
        catchWindow.setScene(new Scene(bp));
        catchAnimationThread = new Thread(() -> {
            Platform.runLater(catchWindow::show);
            gamePause = true;
            PokemonRunnable.gamePause = StationRunnable.gamePause = true;
            try {
                Thread.sleep(caught ? 11733 : 10200);
            } catch (InterruptedException e) {
                System.out.println("Sleep interrupted, continue the game.");
            } finally {
                gamePause = false;
                PokemonRunnable.gamePause = StationRunnable.gamePause = false;
                Platform.runLater(() -> {
                    catchWindow.close();
                    mainPane.requestFocus();
                });
            }
        });
        catchAnimationThread.setDaemon(true);
        catchAnimationThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
