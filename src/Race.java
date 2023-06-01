import java.sql.*;
import java.util.*;

public class Race {

    static int i = 1;
    static String DB_URL = "jdbc:postgresql://localhost:5432/horse_racing_sim";


    // Sort the list of horses by fastest speed to slowest.
    public static ArrayList<Horse> raceHorses(ArrayList<Horse> horses) {
        ArrayList<Horse> winningOrderHorses = new ArrayList<Horse>();
        winningOrderHorses.addAll(horses);
        Collections.sort(winningOrderHorses, new Comparator<Horse>() {
            @Override
            public int compare(Horse o1, Horse o2) {
                return Integer.valueOf(o2.getSpeed()).compareTo(o1.getSpeed());
            }
        });
        return winningOrderHorses;
    }

    // Reset race:

    public static ArrayList<Horse> resetRace(ArrayList<Horse> horses) {
        for (int index = 0; index < 8; index ++) {
            horses.get(index).setNewSpeed();
        }
        return horses;
    }

    public static void recordRaceResults(ArrayList<Horse> winningOrder, User user, Horse userSelectedHorse) {
        String INSERT_RACE_RESULTS_SQL = "INSERT INTO races (username, user_selection_horse_id, first_place_horse_id, second_place_horse_id, third_place_horse_id, fourth_place_horse_id) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(DB_URL)){
            PreparedStatement RACE_RESULTS_INSERT_STMT = conn.prepareStatement(INSERT_RACE_RESULTS_SQL);
            RACE_RESULTS_INSERT_STMT.setString(1, user.userName);
            RACE_RESULTS_INSERT_STMT.setInt(2, userSelectedHorse.id);
            int parameterIndex = 3;
            for (int index = 0; index <= 3; index ++) {
                RACE_RESULTS_INSERT_STMT.setInt(parameterIndex, winningOrder.get(index).id);
                parameterIndex ++;
            }
            RACE_RESULTS_INSERT_STMT.executeUpdate();

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
