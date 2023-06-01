import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class User {

    static final String DB_URL = "jdbc:postgresql://localhost:5432/horse_racing_sim";

    static final String UPDATE_USER_BALANCE_SQL = "UPDATE users SET balance = ? where username = ?;";

    public String userName;
    public String password;

    public Integer balance = 100;

    public String accountType;

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.accountType = "basic";
    }

    public void recordBalanceSQL(){
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement UPDATE_USER_BALANCE_STMT = conn.prepareStatement(UPDATE_USER_BALANCE_SQL);
            UPDATE_USER_BALANCE_STMT.setInt(1, this.balance);
            UPDATE_USER_BALANCE_STMT.setString(2, this.userName);
            UPDATE_USER_BALANCE_STMT.executeUpdate();
            System.out.println("New Balance Recorded.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
