package pokemon.game;

import java.util.ArrayList;

/**
 * This class is responsible for storing the map data and perform operations
 * related to the map
 */
public class Map {

    public static final char WALL = '#', PATH = ' ', START = 'B', DEST = 'D', SUPP = 'S', POKE = 'P';

    private int m, n;
    private char[][] map;
    private Cell start, destination;
    private ArrayList<Pokemon> pokemons;
    private ArrayList<Station> stations;

    {
        pokemons = new ArrayList<Pokemon>();
        stations = new ArrayList<Station>();
    }

    /**
     * Constructor Initialize the character array, and store the dimension into
     * m and n
     *
     * @param m Number of rows
     * @param n Number of columns
     */
    public Map(int m, int n) {
        map = new char[m][n];
        this.m = m;
        this.n = n;
    }

    /**
     * Read the file line by line and store the data in to the map
     *
     * @param m Indicate which line of the file s contains
     * @param s The n th line of the file
     */
    public void readFileLine(int m, String s) {
        for (int i = 0; i < n; i++) {
            char temp = s.charAt(i);

            if (temp != Map.DEST && temp != Map.START && temp != Map.SUPP && temp != Map.POKE && temp != Map.PATH
                    && temp != Map.WALL) {
                System.out.println("The input file is written in invalid format.");
                System.exit(-1);
            }

            this.map[m][i] = temp;
            if (temp == DEST)
                this.destination = new Cell(m, i);
            else if (temp == START)
                this.start = new Cell(m, i);
        }
    }

    public Cell getDimension() {
        return new Cell(this.m, this.n);
    }

    public char[][] getMap() {
        return map;
    }

    public void setMap(Cell cell, char type) {
        map[cell.getM()][cell.getN()] = type;
    }

    /**
     * Get the Pokemon at the given location
     *
     * @param cell Location of the Pokemon
     * @return The Pokemon at the given location, if it is not exist, return null
     */
    public Pokemon getPokemon(Cell cell) {
        for (Pokemon pkm : this.pokemons)
            if (cell.equals(pkm))
                return pkm;
        return null;
    }

    public Station getStation(Cell cell) {
        for (Station stn : this.stations)
            if (cell.equals(stn))
                return stn;
        return null;
    }

    /**
     * Get the Poke ball(s) at the supply station at the given location
     *
     * @param cell Location of the supply center
     * @return The number of Poke balls provided, if the supply station is not exist, return 0
     */
    public int getBall(Cell cell) {
        for (Station stn : this.stations)
            if (cell.equals(stn))
                return stn.getBallsProvided();
        return 0;
    }

    /**
     * Add new pokemon to the ArrayList
     *
     * @param pkm The Pokemon which is going to be added
     */
    public void addPokemon(Pokemon pkm) {
        pokemons.add(pkm);
    }

    public ArrayList<Pokemon> getExistingPokemons() {
        return this.pokemons;
    }

    /**
     * Add new station to the ArrayList
     *
     * @param stn The station which is going to be added
     */
    public void addStation(Station stn) {
        stations.add(stn);
    }

    public ArrayList<Station> getExistingStations() {
        return this.stations;
    }

    /**
     * Get the starting point of the map
     *
     * @return The starting point of the map
     */
    public Cell getStart() {
        return start;
    }

    /**
     * Get the destination of the map
     *
     * @return The destination of the map
     */
    public Cell getDestination() {
        return destination;
    }

    /**
     * Check if the location input is out of bound
     *
     * @param cell The location input
     * @return Whether the location input is out of bound
     */
    public boolean isOutOfBound(Cell cell) {
        return cell.getM() >= this.m || cell.getN() >= this.n || cell.getM() < 0 || cell.getN() < 0;
    }

    /**
     * Check if the cell is wall
     *
     * @param cell The cell being checked
     * @return Whether the cell is wall
     */
    public boolean isWall(Cell cell) {
        return map[cell.getM()][cell.getN()] == Map.WALL;
    }

    /**
     * Check if the cell is Pokemon
     *
     * @param cell The cell being checked
     * @return Whether the cell is pokemon
     */
    public boolean isPokemon(Cell cell) {
        return map[cell.getM()][cell.getN()] == Map.POKE;
    }

    /**
     * Check if the cell is supply station
     *
     * @param cell The cell being checked
     * @return Whether the cell is supply station
     */
    public boolean isSupplyStation(Cell cell) {
        return map[cell.getM()][cell.getN()] == Map.SUPP;
    }

    /**
     * Check if the cell is the destination
     *
     * @param cell The cell being checked
     * @return Whether the cell is the destination
     */
    public boolean isDestination(Cell cell) {
        return map[cell.getM()][cell.getN()] == Map.DEST;
    }

    public void print() {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++)
                System.out.print(map[i][j]);
            System.out.println();
        }
    }
}
