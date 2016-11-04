package pokemon.game;

/**
 * This class is responsible for storing Supply station data and perform
 * operations related to supply station
 */
public class Station extends Cell {
	private int ballsProvided;

    /**
     * Constructor
     *
     * @param pos The location of the supply station
     * @param data The information of the supply station
     * @throws NumberFormatException Check if the information is in correct format
     */
	public Station(String[] pos, String[] data) throws NumberFormatException {
		super(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
		this.ballsProvided = Integer.parseInt(data[1]);
	}

	/**
	 * Get the Poke ball(s) provided by the supply station
	 * 
	 * @return The Poke ball(s) provided by the supply station
	 */
	public int getBallsProvided() {
		return ballsProvided;
	}
}
