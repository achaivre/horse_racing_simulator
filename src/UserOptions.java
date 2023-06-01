import java.util.*;
import java.sql.*;
public class UserOptions {

    // Database URL for ease of access:
    static final String DB_URL = "jdbc:postgresql://localhost:5432/horse_racing_sim";

    // User Query for username being unique:
    static final String USER_QUERY = "SELECT id FROM users WHERE username = ?;";

    // User Creation SQL statement that also encrypts the password for storage in the table for security reasons:
    static final String INSERT_USER_SQL = "INSERT INTO users(username, password, balance, account_type) " +
            "VALUES(?,crypt(?, gen_salt('bf')),100, ?)";

    // User Query for logging in:
    static final String USER_LOGIN_SQL = "SELECT id, balance, account_type FROM users WHERE username = ? AND password = crypt(?, password);";

    // Delete User SQL:
    static final String DELETE_USER_SQL = "DELETE FROM users WHERE username = ?;";

    // Update User Password SQL:
    static final String UPDATE_USER_PASSWORD_SQL = "UPDATE users SET password = crypt(?, gen_salt('bf')) where username = ?;";

    // Creating a new user for the program.
    public User userCreation(Scanner userScan) {
        boolean notValidUserCreation = true;
        User newUser = null;
        try {
            while (notValidUserCreation) {
                long id = 0;
                Connection connection = DriverManager.getConnection(DB_URL);
                System.out.print("Please provide a username: ");
                String userName = userScan.nextLine();
                PreparedStatement USER_UNIQUE_QUERY = connection.prepareStatement(USER_QUERY);
                USER_UNIQUE_QUERY.setString(1, userName);
                ResultSet userUniqueRS = USER_UNIQUE_QUERY.executeQuery();
                try {
                    if (!userUniqueRS.next()) {

                        // User giving password and creating a new user object (newUser).
                        notValidUserCreation = false;
                        System.out.print("Please provide a password: ");
                        String passWord = userScan.nextLine();
                        newUser = new User(userName, passWord);


                        // Preparing statement for use in adding a new row.
                        PreparedStatement USER_CREATION_STMT = connection.prepareStatement(INSERT_USER_SQL);
                        USER_CREATION_STMT.setString(1, newUser.userName);
                        USER_CREATION_STMT.setString(2, newUser.password);
                        USER_CREATION_STMT.setString(3, newUser.accountType);


                        // Adding a new user row in the table:
                        int affectedRows = USER_CREATION_STMT.executeUpdate();
                        if (affectedRows > 0) {
                            try (ResultSet rs = USER_CREATION_STMT.getGeneratedKeys()) {
                                if (rs.next()) {
                                    id = rs.getLong(1);
                                }
                            } catch (SQLException e) {
                                System.out.println(e.getMessage());
                            }
                        }

                    } else {
                        System.out.println("Your username, " + userName + " is not unique. Please try something else.");
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return newUser;
    }

    // Logging In Workflow (((still need to think about this workflow))):
    public User loggingIn(Scanner userScan) {
        boolean notLoggedIn = true;
        String userInputName;
        String inputPassword;
        String userLoginQuery;
        User loggedInUser = null;
        try {
            Connection connection = DriverManager.getConnection(DB_URL);

            // Taking in user input:
            while (notLoggedIn) {
                System.out.println("Username: ");
                userInputName = userScan.nextLine();
                System.out.println("password: ");
                inputPassword = userScan.nextLine();

                // Preparing statement for search through table;
                PreparedStatement userLoginQueryStmt = connection.prepareStatement(USER_LOGIN_SQL);
                userLoginQueryStmt.setString(1, userInputName);
                userLoginQueryStmt.setString(2, inputPassword);
                ResultSet rs = userLoginQueryStmt.executeQuery();

                // Checks to see if user and password combination does in fact exist.
                    if (rs.next()) {
                        notLoggedIn = false;
                        System.out.println("Logged In.");

                        // Creating a new user to return that has balance updated.
                        loggedInUser = new User(userInputName, inputPassword);
                        loggedInUser.balance = rs.getInt("balance");
                        loggedInUser.accountType = rs.getString("account_type");
                    }
                if (notLoggedIn) {
                    System.out.println("You didn't login");
                }
            }

        } catch (SQLException e) {
            System.out.print("This bitch didn't work." + e.getMessage());
        }
        return loggedInUser;
    }

    public void deleteUser(Scanner userScan){
        String userResponse = "";
        try(Connection conn = DriverManager.getConnection(DB_URL)) {
            while (!userResponse.equalsIgnoreCase("Q")) {
                System.out.println("What is the username of the user you would like to delete or [q]uit? > ");
                userResponse = userScan.nextLine();

                if (!userResponse.equalsIgnoreCase("q")) {
                    PreparedStatement CHECK_IF_USER_EXISTS_STMT = conn.prepareStatement(USER_QUERY);
                    CHECK_IF_USER_EXISTS_STMT.setString(1, userResponse);
                    ResultSet rsUserExists = CHECK_IF_USER_EXISTS_STMT.executeQuery();
                    if (rsUserExists.next()) {
                        PreparedStatement DELETE_USER_STMT = conn.prepareStatement(DELETE_USER_SQL);
                        DELETE_USER_STMT.setString(1, userResponse);
                        int rsDeleteUser = DELETE_USER_STMT.executeUpdate();
                        System.out.println("User, " + userResponse + ", deleted.");
                    } else {
                        System.out.println("That user doesn't exist.");
                    }
                }

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateUserPassword(Scanner userScan) {
        String userResponse = "";
        try(Connection conn = DriverManager.getConnection(DB_URL)) {
            while (!userResponse.equalsIgnoreCase("Q")) {
                System.out.println("What is the username of the user you would like to change the password of or [q]uit? > ");
                userResponse = userScan.nextLine();

                if (!userResponse.equalsIgnoreCase("q")) {
                    PreparedStatement CHECK_IF_USER_EXISTS_STMT = conn.prepareStatement(USER_QUERY);
                    CHECK_IF_USER_EXISTS_STMT.setString(1, userResponse);
                    ResultSet rsUserExists = CHECK_IF_USER_EXISTS_STMT.executeQuery();
                    if (rsUserExists.next()) {
                        PreparedStatement UPDATE_USER_STMT = conn.prepareStatement(UPDATE_USER_PASSWORD_SQL);
                        System.out.println("What would you like to change the password to? > ");
                        String userInputPassword = userScan.nextLine();
                        UPDATE_USER_STMT.setString(1, userInputPassword);
                        UPDATE_USER_STMT.setString(2, userResponse);
                        int rsUpdateUser = UPDATE_USER_STMT.executeUpdate();
                        System.out.println("User, " + userResponse + "'s password has been updated.");
                    } else {
                        System.out.println("That user doesn't exist.");
                    }
                }

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
