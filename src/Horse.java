import java.sql.*;

public class Horse {
    static String DB_URL = "jdbc:postgresql://localhost:5432/horse_racing_sim";

    int id;
    int topSpeed;
    int slowestSpeed;
    String name;

    int speed;


    public Horse(int id, String name, int topSpeed, int lowSpeed) {
        this.id = id;
        this.topSpeed = topSpeed;
        this.slowestSpeed = lowSpeed;
        this.name = name;
        this.speed = (int)Math.floor(Math.random()*(topSpeed - slowestSpeed + 1) + slowestSpeed);


    }

    public void setNewSpeed() {
        this.speed = (int)Math.floor(Math.random()*(topSpeed - slowestSpeed + 1) + slowestSpeed);
    }

    public int getSpeed() {

        return this.speed;
    }

    public void checkWinsTotal(){
        String CHECK_FIRST_PLACE_TOTAL_SQL = "SELECT COUNT(*) as TOTAL FROM races WHERE first_place_horse_id = ?;";
        String CHECK_SECOND_PLACE_TOTAL_SQL = "SELECT COUNT(*) as TOTAL FROM races WHERE second_place_horse_id = ?;";
        String CHECK_THIRD_PLACE_TOTAL_SQL = "SELECT COUNT(*) as TOTAL FROM races WHERE third_place_horse_id = ?;";
        String CHECK_FOURTH_PLACE_TOTAL_SQL = "SELECT COUNT(*) as TOTAL FROM races WHERE fourth_place_horse_id = ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement CHECK_FIRST_PLACE_TOTAL_STMT = conn.prepareStatement(CHECK_FIRST_PLACE_TOTAL_SQL);
            PreparedStatement CHECK_SECOND_PLACE_TOTAL_STMT = conn.prepareStatement(CHECK_SECOND_PLACE_TOTAL_SQL);
            PreparedStatement CHECK_THIRD_PLACE_TOTAL_STMT = conn.prepareStatement(CHECK_THIRD_PLACE_TOTAL_SQL);
            PreparedStatement CHECK_FOURTH_PLACE_TOTAL_STMT = conn.prepareStatement(CHECK_FOURTH_PLACE_TOTAL_SQL);
            CHECK_FIRST_PLACE_TOTAL_STMT.setInt(1, this.id);
            CHECK_SECOND_PLACE_TOTAL_STMT.setInt(1, this.id);
            CHECK_THIRD_PLACE_TOTAL_STMT.setInt(1, this.id);
            CHECK_FOURTH_PLACE_TOTAL_STMT.setInt(1, this.id);
            ResultSet rsFirstPlace = CHECK_FIRST_PLACE_TOTAL_STMT.executeQuery();
            ResultSet rsSecondPlace = CHECK_SECOND_PLACE_TOTAL_STMT.executeQuery();
            ResultSet rsThirdPlace = CHECK_THIRD_PLACE_TOTAL_STMT.executeQuery();
            ResultSet rsFourthPlace = CHECK_FOURTH_PLACE_TOTAL_STMT.executeQuery();
            if (rsFirstPlace.next()) {
                System.out.println(this.name + " got first place " + rsFirstPlace.getInt("TOTAL") + " times.");
            }
            if (rsSecondPlace.next()) {
                System.out.println(this.name + " got second place " + rsSecondPlace.getInt("TOTAL") + " times.");
            }
            if (rsThirdPlace.next()) {
                System.out.println(this.name + " got third place " + rsThirdPlace.getInt("TOTAL") + " times.");
            }
            if (rsFourthPlace.next()) {
                System.out.println(this.name + " got fourth place " + rsFourthPlace.getInt("TOTAL") + " times.");
            }



        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public void checkWinsWithUser(User user){
        String CHECK_FIRST_PLACE_TOTAL_SQL = "SELECT COUNT(*) as TOTAL FROM races WHERE first_place_horse_id = ? AND username = ?;";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement CHECK_FIRST_PLACE_TOTAL_STMT = conn.prepareStatement(CHECK_FIRST_PLACE_TOTAL_SQL);
            CHECK_FIRST_PLACE_TOTAL_STMT.setInt(1, this.id);
            CHECK_FIRST_PLACE_TOTAL_STMT.setString(2, user.userName);
            ResultSet rsFirstPlace = CHECK_FIRST_PLACE_TOTAL_STMT.executeQuery();
            if (rsFirstPlace.next()) {
                System.out.println("You and " + this.name + " got first place " + rsFirstPlace.getInt("TOTAL") + " times.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }



}
