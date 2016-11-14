package pokemon.ui;

import javafx.animation.AnimationTimer;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static javafx.scene.input.KeyCode.*;

public class PokemonScreen extends Application {

    private Game game;

    private enum View {TREE, EXIT, BALL, PATH, POKE}

    private enum Msg {NONE, UNCAUGHT, CAUGHT, ENDGAME}

    private static final int STEP_SIZE = 40;
    private final BorderPane mainPane;
    private final GridPane mapPane;
    private final VBox scorePane;
    private final Button resumeBtn, pauseBtn;
    private final ImageView avatar;

    // this are the urls of the images
    private static final String avatarFront = new File("icons/front.png").toURI().toString();
    private static final String avatarBack = new File("icons/back.png").toURI().toString();
    private static final String avatarLeft = new File("icons/left.png").toURI().toString();
    private static final String avatarRight = new File("icons/right.png").toURI().toString();
    private static final String treePath = new File("icons/tree.png").toURI().toString();
    private static final String exitPath = new File("icons/exit.png").toURI().toString();
    private static final String ballPath = new File("icons/ball_ani.gif").toURI().toString();

    private AnimationTimer animationTimer;
    private boolean avatarPause = false;
    private boolean gamePause = false;
    private KeyCode lastKeyPressed = null;
    private int previousPlayerNoOfPokemons = 0;
    private final HashMap<Pokemon, Node> pokemonViews, pokemonPendingViews;
    private final HashMap<Station, Node> stationViews, stationPendingViews;

    {
        avatar = new ImageView(new Image(avatarFront));
        avatar.setFitWidth(STEP_SIZE);
        avatar.setFitHeight(STEP_SIZE);
        avatar.setPreserveRatio(true);

        game = new Game();
        pokemonViews = new HashMap<>();
        pokemonPendingViews = new HashMap<>();
        stationViews = new HashMap<>();
        stationPendingViews = new HashMap<>();

        mainPane = new BorderPane();
        mapPane = new GridPane();

        resumeBtn = new Button("Resume");
        resumeBtn.setOnAction(e -> {
            if (gamePause)
                animationTimer.start();
            gamePause = false;
            clearFocus();
        });
        pauseBtn = new Button("Pause");
        pauseBtn.setOnAction(e -> {
            if (!gamePause)
                animationTimer.stop();
            gamePause = true;
            clearFocus();
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

        animationTimer = new AnimationTimer() {
            long lastFrameUpdateTime = 0;

            @Override
            public void handle(long l) {
                if (game.map.isDestination(game.player.currentPos()))
                    return;

                //check if at least one second is passed
                if (Math.abs(l - lastFrameUpdateTime) > Math.pow(10, 9))
                    lastFrameUpdateTime = l;
                else
                    return;

//                game.map.print();
//                System.out.println("pokemonViews: " + pokemonViews);
//                System.out.println("stationViews: " + stationViews);
//                System.out.println("pokemonPendingViews: " + pokemonPendingViews);
//                System.out.println("stationPendingViews: " + stationPendingViews);

                //Randomly place the pokemons
                HashMap<Pokemon, Node> newPokemonViews = new HashMap<>();
                for (HashMap.Entry<Pokemon, Node> entry : pokemonViews.entrySet()) {
                    Pokemon pkm = entry.getKey();
                    Node node = entry.getValue();

                    //randomly assign coordinate to pokemons and put into the new hashmap
                    ArrayList<Cell> cells = new ArrayList<>();
                    int m = pkm.getM(), n = pkm.getN();
                    synchronized (game.map) {
                        if (!game.map.isOutOfBound(pkm.up()) &&
                                (pkm.up().equals(game.player.currentPos()) || game.map.getMap()[m - 1][n] == Map.PATH))
                            cells.add(pkm.up());
                        if (!game.map.isOutOfBound(pkm.right()) &&
                                (pkm.right().equals(game.player.currentPos()) || game.map.getMap()[m][n + 1] == Map.PATH))
                            cells.add(pkm.right());
                        if (!game.map.isOutOfBound(pkm.down()) &&
                                (pkm.down().equals(game.player.currentPos()) || game.map.getMap()[m + 1][n] == Map.PATH))
                            cells.add(pkm.down());
                        if (!game.map.isOutOfBound(pkm.left()) &&
                                (pkm.left().equals(game.player.currentPos()) || game.map.getMap()[m][n - 1] == Map.PATH))
                            cells.add(pkm.left());
                    }

                    Random random = new Random();

                    synchronized (game.map) {
                        game.map.getExistingPokemons().remove(pkm);
                        game.map.setMap(pkm, Map.PATH);
                    }
                    synchronized (mapPane) {
                        mapPane.getChildren().remove(node);
                    }
                    pkm.setCoordinate(cells.get(random.nextInt(cells.size())));

                    if (pkm.equals(game.player.currentPos())) {
                        if (pkm.canBeCaught(game.player.getNumOfBalls())) {
                            game.player.catchPokemon(pkm);
                            previousPlayerNoOfPokemons++;
                            synchronized (scorePane) {
                                updateScorePane(Msg.CAUGHT);
                            }
                        } else {
                            synchronized (pokemonPendingViews) {
                                pokemonPendingViews.put(pkm, node);
                            }
                            synchronized (scorePane) {
                                updateScorePane(Msg.UNCAUGHT);
                            }
                            respawnPokemon(pkm);
                        }
                    } else {
                        newPokemonViews.put(pkm, node);
                        synchronized (game.map) {
                            game.map.getExistingPokemons().add(pkm);
                            game.map.setMap(pkm, Map.POKE);
                        }
                        synchronized (mapPane) {
                            mapPane.add(node, pkm.getN(), pkm.getM());
                        }
                    }
                }
                synchronized (pokemonViews) {
                    pokemonViews.clear();
                    pokemonViews.putAll(newPokemonViews);
                }
            }
        };

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
                }

                if (lastKeyPressed == UP || lastKeyPressed == DOWN || lastKeyPressed == LEFT || lastKeyPressed == RIGHT) {
                    if (map.isDestination(game.player.currentPos())) {
                        synchronized (scorePane) {
                            updateScorePane(Msg.ENDGAME);
                        }
                        resumeBtn.setDisable(true);
                        pauseBtn.setDisable(true);
                        avatarPause = true;
                        gamePause = true;
                    } else if (map.isPokemon(game.player.currentPos())) {
                        Pokemon pkm = game.map.getPokemon(game.player.currentPos());
                        Node temp;
                        synchronized (mapPane) {
                            mapPane.getChildren().remove(pokemonViews.get(pkm));
                        }
                        synchronized (game.map) {
                            game.map.getExistingPokemons().remove(pkm);
                            game.map.setMap(pkm, Map.PATH);
                        }
                        synchronized (pokemonViews) {
                            temp = pokemonViews.remove(pkm);
                        }

                        if (previousPlayerNoOfPokemons < game.player.getNumOfPokemons()) {
                            synchronized (scorePane) {
                                updateScorePane(Msg.CAUGHT);
                            }
                            previousPlayerNoOfPokemons++;
                        } else {
                            synchronized (scorePane) {
                                updateScorePane(Msg.UNCAUGHT);
                            }
                            synchronized (pokemonPendingViews) {
                                pokemonPendingViews.put(pkm, temp);
                            }
                            respawnPokemon(pkm);
                        }
                    } else if (map.isSupplyStation(game.player.currentPos())) {
                        Station stn = game.map.getStation(game.player.currentPos());
                        Node temp;
                        synchronized (mapPane) {
                            mapPane.getChildren().remove(stationViews.get(stn));
                        }
                        synchronized (scorePane) {
                            updateScorePane(Msg.NONE);
                        }
                        synchronized (game.map) {
                            game.map.getExistingStations().remove(stn);
                            game.map.setMap(stn, Map.PATH);
                        }
                        synchronized (stationViews) {
                            temp = stationViews.remove(stn);
                        }
                        synchronized (stationPendingViews) {
                            stationPendingViews.put(stn, temp);
                        }
                        respawnStation(stn);
                    } else
                        synchronized (scorePane) {
                            updateScorePane(Msg.NONE);
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

        clearFocus();
        stage.setScene(scene);
        stage.show();
        animationTimer.start();
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

    private void updateScorePane(Msg msg) {
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

    private void respawnPokemon(Pokemon pkm) {
        Thread t = new Thread(() -> {
            //suspend 3 to 5 seconds
            Random random = new Random();
            try {
                Thread.sleep(3000 + random.nextInt(2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ArrayList<Cell> possibleLocations = getAllEmptyCellLocations();

            Node temp;
            synchronized (pokemonPendingViews) {
                temp = pokemonPendingViews.remove(pkm);
            }

            pkm.setCoordinate(possibleLocations.get(random.nextInt(possibleLocations.size())));
            synchronized (pokemonViews) {
                pokemonViews.put(pkm, temp);
            }

            synchronized (game.map) {
                game.map.getExistingPokemons().add(pkm);
                game.map.setMap(pkm, Map.POKE);
            }

            if (!game.map.isDestination(game.player.currentPos()))
                Platform.runLater(() -> {
                    synchronized (mapPane) {
                        mapPane.add(temp, pkm.getN(), pkm.getM());
                    }
                });

        });
        t.setDaemon(true);
        t.start();
    }

    private void respawnStation(Station stn) {
        Thread t = new Thread(() -> {
            //suspend 5 to 10 seconds
            Random random = new Random();
            try {
                Thread.sleep(5000 + random.nextInt(5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ArrayList<Cell> possibleLocations = getAllEmptyCellLocations();

            Node temp;
            synchronized (stationPendingViews) {
                temp = stationPendingViews.remove(stn);
            }

            stn.setCoordinate(possibleLocations.get(random.nextInt(possibleLocations.size())));
            synchronized (stationViews) {
                stationViews.put(stn, temp);
            }

            synchronized (game.map) {
                game.map.getExistingStations().add(stn);
                game.map.setMap(stn, Map.SUPP);
            }

            if (!game.map.isDestination(game.player.currentPos()))
                Platform.runLater(() -> {
                    synchronized (mapPane) {
                        mapPane.add(temp, stn.getN(), stn.getM());
                    }
                });

        });
        t.setDaemon(true);
        t.start();
    }

    private ArrayList<Cell> getAllEmptyCellLocations() {
        //find possible locations for respawning
        ArrayList<Cell> possibleLocations = new ArrayList<>();
        synchronized (game.map) {
            for (int i = 0; i < game.map.getDimension().getM(); i++)
                for (int j = 0; j < game.map.getDimension().getN(); j++)
                    if (game.map.getMap()[i][j] == Map.PATH && !game.player.currentPos().equals(new Cell(i, j)))
                        possibleLocations.add(new Cell(i, j));
        }
        return possibleLocations;
    }

    private void clearFocus() {
        mainPane.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
