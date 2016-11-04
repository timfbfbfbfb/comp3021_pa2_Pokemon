package pokemon.game;

/**
 * This class represent each single cell in the game map, which is the parent
 * class of Pokemon and Station
 */
public class Cell extends Thread{
    private int m, n;

    /**
     * Constructor
     *
     * @param m The m th row
     * @param n The n th column
     */
    public Cell(int m, int n) {
        this.m = m;
        this.n = n;
    }

    /**
     * Copy constructor
     *
     * @param c Another cell
     */
    public Cell(Cell c) {
        this.m = c.m;
        this.n = c.n;
    }

    /**
     * Get the row of the cell
     *
     * @return The row of the cell
     */
    public int getM() {
        return m;
    }

    /**
     * Get the column of the cell
     *
     * @return The column of the cell
     */
    public int getN() {
        return n;
    }

    /**
     * Get the cell above the current cell
     *
     * @return Cell above the current cell
     */
    public Cell up() {
        return new Cell(this.m - 1, this.n);
    }

    /**
     * Get the cell below the current cell
     *
     * @return Cell below the current cell
     */
    public Cell down() {
        return new Cell(this.m + 1, this.n);
    }

    /**
     * Get the cell left to the current cell
     *
     * @return Cell left to the current cell
     */
    public Cell left() {
        return new Cell(this.m, this.n - 1);
    }

    /**
     * Get the cell right to the current cell
     *
     * @return Cell right to the current cell
     */
    public Cell right() {
        return new Cell(this.m, this.n + 1);
    }

    /**
     * Override the toString format in order to print the row and the column of
     * the cell
     *
     * @return The string of the row and the column of the cell
     */
    @Override
    public String toString() {
        return "<" + m + "," + n + ">";
    }

    /**
     * Override the equals in order to check if two cells are at the same
     * location
     *
     * @param o The cell which will be compared
     * @return If two cells are at the same location
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cell))
            return false;
        Cell temp;
        temp = (Cell) o;
        return temp.m == this.m && temp.n == this.n;
    }
}
