package pokemon.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import pokemon.game.Cell;
import pokemon.game.Game;
import pokemon.game.Map;
import pokemon.game.Pokemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * The runnable class of pokemon
 */
public class PokemonRunnable implements Runnable {

    private Pokemon pokemon;
    private GridPane mapPane;
    private HashMap<Pokemon, Node> pokemonViews;
    private Game game;
    private boolean hidden = false;
    private long lastSleep = 0;
    public static boolean gamePause = false;
    private PokemonScreen pokemonScreen;
    private int lastCaught = 0;
    private boolean caught = false;

    /**
     * Constructor
     *
     * @param pkm          The pokemon object
     * @param mapPane      The game map pane
     * @param pokemonViews The collection of the station image views
     * @param game         The game object
     * @param pkmScreen    The main layout
     */
    public PokemonRunnable(Pokemon pkm, GridPane mapPane, HashMap<Pokemon, Node> pokemonViews, Game game, PokemonScreen pkmScreen) {
        pokemon = pkm;
        this.mapPane = mapPane;
        this.pokemonViews = pokemonViews;
        this.game = game;
        this.pokemonScreen = pkmScreen;
    }

    /**
     * The override method of Runnable
     */
    @Override
    public void run() {
        System.out.println("Pokemon Thread (ID: " + Thread.currentThread().getId() + ") Start!");
        lastSleep = System.currentTimeMillis();
        Random random = new Random();
        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //check if the game is being paused, if yes, do nothing
            if (gamePause) {
                lastSleep++;
                continue;
            }

            //if the player and the pokemon are at the same location, trigger the catch event
            if (game.player.currentPos().equals(pokemon) && !hidden) {
                System.out.println("Trying to catch " + pokemon.getPokemonName() + "!");
                Node node = pokemonViews.get(pokemon);

                synchronized (game.map) {
                    //remove the pokemon from the map
                    Platform.runLater(() -> {
                        synchronized (mapPane) {
                            mapPane.getChildren().remove(node);
                        }
                    });
                    game.map.getExistingPokemons().remove(pokemon);
                    game.map.setMap(pokemon, Map.PATH);
                    hidden = true;
                }

                caught = game.player.getNumOfPokemons() > lastCaught;

                if (!caught && pokemon.canBeCaught(game.player.getNumOfBalls())) {
                    game.player.catchPokemon(pokemon);
                    caught = true;
                }

                Platform.runLater(() -> {
                    pokemonScreen.showCatchAnimation(caught);
                });

                //if successfully catch the pokemon, end this thread, else respawn the pokemon
                if (caught) {
                    Platform.runLater(() -> {
                        pokemonScreen.updateScorePane(PokemonScreen.Msg.CAUGHT);
                    });
                    break;
                } else
                    Platform.runLater(() -> {
                        pokemonScreen.updateScorePane(PokemonScreen.Msg.UNCAUGHT);
                    });

                //counting the time of respawn, 3 to 5 seconds
                try {
                    int timeRemaining = 3000 + random.nextInt(2000);
                    while (timeRemaining > 0) {
                        Thread.sleep(1);
                        if (!gamePause)
                            timeRemaining--;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //add the pokemon to the map
                ArrayList<Cell> cells = new ArrayList<>();
                synchronized (game.map) {
                    for (int i = 0; i < game.map.getDimension().getM(); i++)
                        for (int j = 0; j < game.map.getDimension().getN(); j++)
                            if (game.map.getMap()[i][j] == Map.PATH && !game.player.currentPos().equals(new Cell(i, j)))
                                cells.add(new Cell(i, j));
                    Cell newPos = cells.get(random.nextInt(cells.size()));
                    pokemon.setCoordinate(newPos);
                    game.map.setMap(pokemon, Map.POKE);
                    game.map.getExistingPokemons().add(pokemon);

                    Platform.runLater(() -> {
                        synchronized (mapPane) {
                            mapPane.add(node, pokemon.getN(), pokemon.getM());
                        }
                    });
                    hidden = false;
                }
            }

            //make the pokemon walks around every 1 to 2 second(s)
            if ((System.currentTimeMillis() - lastSleep) > (random.nextInt(1000) + 1000)) {
                lastSleep = System.currentTimeMillis();
                Node node = pokemonViews.get(pokemon);

                //remove the pokemon from the map
                ArrayList<Cell> cells = new ArrayList<>();
                synchronized (game.map) {
                    Platform.runLater(() -> {
                        synchronized (mapPane) {
                            mapPane.getChildren().remove(node);
                        }
                    });
                    game.map.setMap(pokemon, Map.PATH);
                    game.map.getExistingPokemons().remove(pokemon);
                }
                synchronized (game.map) {
                    if (!game.map.isOutOfBound(pokemon.up()) && game.map.getMap()[pokemon.up().getM()][pokemon.up().getN()] == Map.PATH)
                        cells.add(pokemon.up());
                    if (!game.map.isOutOfBound(pokemon.down()) && game.map.getMap()[pokemon.down().getM()][pokemon.down().getN()] == Map.PATH)
                        cells.add(pokemon.down());
                    if (!game.map.isOutOfBound(pokemon.left()) && game.map.getMap()[pokemon.left().getM()][pokemon.left().getN()] == Map.PATH)
                        cells.add(pokemon.left());
                    if (!game.map.isOutOfBound(pokemon.right()) && game.map.getMap()[pokemon.right().getM()][pokemon.right().getN()] == Map.PATH)
                        cells.add(pokemon.right());

                    Cell newPos;
                    if (!cells.isEmpty())
                        newPos = cells.get(random.nextInt(cells.size()));
                    else
                        newPos = pokemon;
                    pokemon.setCoordinate(newPos);
                    game.map.setMap(newPos, Map.POKE);
                    game.map.addPokemon(pokemon);

                    //add the pokemon to the map
                    Platform.runLater(() -> {
                        synchronized (mapPane) {
                            mapPane.add(node, pokemon.getN(), pokemon.getM());
                        }
                    });
                }
            }
            lastCaught = game.player.getNumOfPokemons();
        }
    }
}
