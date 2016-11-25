package pokemon.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import pokemon.game.Cell;
import pokemon.game.Game;
import pokemon.game.Map;
import pokemon.game.Station;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * The runnable class of supply station
 */
public class StationRunnable implements Runnable {
    private GridPane mapPane;
    private HashMap<Station, Node> stationViews;
    private Game game;
    public static boolean gamePause = false;
    private Station station;
    private boolean hidden = false;
    private PokemonScreen pokemonScreen;

    /**
     * Constructor
     *
     * @param station       The station object
     * @param mapPane       The game map pane
     * @param stationViews  The collection of the station image views
     * @param game          The game object
     * @param pokemonScreen The main layout
     */
    public StationRunnable(Station station, GridPane mapPane, HashMap<Station, Node> stationViews, Game game, PokemonScreen pokemonScreen) {
        this.mapPane = mapPane;
        this.stationViews = stationViews;
        this.game = game;
        this.station = station;
        this.pokemonScreen = pokemonScreen;
    }


    /**
     * The override method of Runnable
     */
    @Override
    public void run() {
        System.out.println("Station Thread (ID: " + Thread.currentThread().getId() + ") Start!");
        Random random = new Random();
        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (gamePause)
                continue;

            if (game.player.currentPos().equals(station) && !hidden) {
                System.out.println("Obtain Pokeballs!");
                Node node = stationViews.get(station);

                synchronized (game.map) {
                    Platform.runLater(() -> {
                        synchronized (mapPane) {
                            mapPane.getChildren().remove(node);
                        }
                    });
                    game.map.setMap(station, Map.PATH);
                    game.map.getExistingStations().remove(station);
                }
                hidden = true;

                Platform.runLater(() -> {
                    pokemonScreen.updateScorePane(PokemonScreen.Msg.NONE);
                });

                try {
                    int timeRemaining = 5000 + random.nextInt(5000);
                    while (timeRemaining > 0) {
                        Thread.sleep(1);
                        if (!gamePause)
                            timeRemaining--;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (game.map) {
                    ArrayList<Cell> cells = new ArrayList<>();
                    for (int i = 0; i < game.map.getDimension().getM(); i++)
                        for (int j = 0; j < game.map.getDimension().getN(); j++)
                            if (game.map.getMap()[i][j] == Map.PATH && !game.player.currentPos().equals(new Cell(i, j)))
                                cells.add(new Cell(i, j));
                    Cell newPos = cells.get(random.nextInt(cells.size()));
                    station.setCoordinate(newPos);
                    game.map.setMap(station, Map.SUPP);
                    game.map.getExistingStations().add(station);
                    Platform.runLater(() -> {
                        synchronized (mapPane) {
                            mapPane.add(node, station.getN(), station.getM());
                        }
                    });
                }
                hidden = false;
            }
        }
    }
}
