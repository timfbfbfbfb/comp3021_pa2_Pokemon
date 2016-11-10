package pokemon.game;

import java.util.ArrayList;

/**
 * This class is responsible for storing player data and perform operations
 * related to player
 */
public class Player {

    private int balls;
    private ArrayList<Pokemon> pokemons;
    private ArrayList<Cell> route;

    {
        pokemons = new ArrayList<Pokemon>();
        route = new ArrayList<Cell>();
    }

    /**
     * Default constructor
     */
    public Player() {
    }

    /**
     * Copy constructor
     *
     * @param p The player being copied
     */
    public Player(Player p) {
        this.balls = p.balls;
        this.pokemons.clear();
        this.pokemons.addAll(p.pokemons);
        this.route.clear();
        this.route.addAll(p.route);
    }

    /**
     * Get the current location of the player
     *
     * @return The current location of the player
     */
    public Cell currentPos() {
        if (this.route.isEmpty())
            return null;
        else
            return this.route.get(this.route.size() - 1);
    }

    /**
     * Get the number of Poke balls
     *
     * @return Number of Poke balls
     */
    public int getNumOfBalls() {
        return balls;
    }

    public int getNumOfPokemons() {
        return this.pokemons.size();
    }

    /**
     * Get the route of player
     *
     * @return The route of player
     */
    public ArrayList<Cell> getRoute() {
        return route;
    }

    /**
     * Calculate the score of the player
     *
     * @return The total score of the player
     */
    public int getScore() {
        return getNB() + 5 * getNP() + 10 * getNS() + getMCP() - getSteps();
    }

    /**
     * Get number of Poke balls
     *
     * @return number of Poke balls
     */
    private int getNB() {
        return this.balls;
    }

    /**
     * Get number of Pokemons
     *
     * @return number of Pokemons
     */
    private int getNP() {
        return this.pokemons.size();
    }

    /**
     * Get number of species of Pokemons
     *
     * @return number of species of Pokemons
     */
    private int getNS() {
        ArrayList<String> temp = new ArrayList<String>();
        for (Pokemon pokemon : pokemons)
            if (!temp.contains(pokemon.getType()))
                temp.add(pokemon.getType());
        return temp.size();
    }

    /**
     * Get maximum combat power of all Pokemons
     *
     * @return maximum combat power of all Pokemons
     */
    private int getMCP() {
        int temp = 0;
        for (Pokemon pokemon : pokemons)
            if (pokemon.getPower() > temp)
                temp = pokemon.getPower();
        return temp;
    }

    /**
     * Get steps
     *
     * @return Steps
     */
    private int getSteps() {
        return this.route.size() - 1;
    }

    public void move(Cell c, Map map) {
        if (map.isSupplyStation(c))
            this.balls += map.getBall(c);
        else if (map.isPokemon(c)) {
            Pokemon pkm = map.getPokemon(c);
            if (pkm.canBeCaught(this.balls))
                catchPokemon(pkm);
        }
        this.route.add(c);
    }

    public void catchPokemon(Pokemon pkm) {
        pokemons.add(pkm);
        balls-=pkm.getBallsRequired();
    }

    /**
     * Override the toString method in order to print the scores, subscores and
     * the path in certain format
     *
     * @return The string of the scores, subscores and the path in certain
     * format
     */
    @Override
    public String toString() {
        String s = getScore() + "\n" + getNB() + ":" + getNP() + ":" + getNS() + ":" + getMCP() + "\n";
        for (int i = 0; i < this.route.size() - 1; i++)
            s += this.route.get(i).toString() + "->";
        if (!this.route.isEmpty())
            s += this.route.get(this.route.size() - 1).toString();
        return s;
    }

    /**
     * Override the hashCode method in order to identify the player, used in PathFinder
     *
     * @return The hash code of the player
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pokemons == null) ? 0 : pokemons.hashCode());
        result = prime * result + balls;
        result = prime * result + (currentPos().getM());
        result = prime * result + (currentPos().getN());
        return result;
    }
}
