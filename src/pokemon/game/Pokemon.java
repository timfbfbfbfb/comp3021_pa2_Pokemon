package pokemon.game;

/**
 * This class is responsible for storing the Pokemon data and perform operations
 * related to Pokemon
 */
public class Pokemon extends Cell {

    private String name, type;
    private int power, ballsRequired;

    /**
     * Constructor
     *
     * @param pos  The location of the Pokemon
     * @param data The information of the Pokemon
     * @throws NumberFormatException Check if the information is in correct format
     */
    public Pokemon(String[] pos, String[] data) throws NumberFormatException {
        super(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
        this.name = data[1];
        this.type = data[2];
        this.power = Integer.parseInt(data[3]);
        this.ballsRequired = Integer.parseInt(data[4]);
    }

    /**
     * Get the Pokemon name
     *
     * @return The Pokemon name
     */
    public String getPokemonName() {
        return name;
    }

    /**
     * Get the Pokemon species
     *
     * @return The Pokemon species
     */
    public String getType() {
        return type;
    }

    /**
     * Get the combat power of the Pokemon
     *
     * @return The combat power of the Pokemon
     */
    public int getPower() {
        return power;
    }

    /**
     * Get the Poke balls required to catch this Pokemon
     *
     * @return The Poke balls required to catch this Pokemon
     */
    public int getBallsRequired() {
        return ballsRequired;
    }

    /**
     * Check if the player has sufficient Poke balls to catch this Pokemon
     *
     * @param n Number of Poke ball the player owned
     * @return Whether this Pokemon can be caught by the player
     */
    public boolean canBeCaught(int n) {
        return n >= this.ballsRequired;
    }

    /**
     * Override the hashCode method, used in Player and PathFinder
     *
     * @return The hash code of the Pokemon
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + power;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ballsRequired;
        return result;
    }
}
