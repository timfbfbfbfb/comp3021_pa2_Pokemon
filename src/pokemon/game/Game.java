package pokemon.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Game {

    public Map map;
    public Player player = new Player();

    /**
     * Initialize all the objects inside the game
     *
     * @param inputFile The given game information
     * @throws Exception
     */
    public void initialize(File inputFile) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));

        // Read the first of the input file
        String line = br.readLine();
        int M = Integer.parseInt(line.split(" ")[0]);
        int N = Integer.parseInt(line.split(" ")[1]);

        // To do: define a map
        map = new Map(M, N);

        // Read the following M lines of the Map
        for (int i = 0; i < M; i++) {
            line = br.readLine();

            // to do
            // Read the map line by line
            map.readFileLine(i, line);
        }

        // to do
        // Find the number of stations and pokemons in the map
        // Continue read the information of all the stations and pokemons by
        // using br.readLine();
        try {
            while ((line = br.readLine()) != null) {
                String[] temp = line.split(", ");
                String[] temp2 = temp[0].split(",");
                temp2[0] = temp2[0].replace("<", "");
                temp2[1] = temp2[1].replace(">", "");

                if (temp[0].length() - 3 != temp2[0].length() + temp2[1].length())
                    throw new Exception();

                if (temp.length == 5)
                    map.addPokemon(new Pokemon(temp2, temp));
                else if (temp.length == 2)
                    map.addStation(new Station(temp2, temp));
                else
                    throw new Exception();
            }
        } catch (Exception e) {
            System.out.println("The input file is written in invalid format.");
            System.exit(-1);
        } finally {
            br.close();
        }
    }
}
