import java.util.*;
import java.sql.*;

public class Main {
    static final String DB_URL = "jdbc:postgresql://localhost:5432/horse_racing_sim";
    static final UserOptions userOptions = new UserOptions();

    static String INSERT_HORSE_SQL = "INSERT INTO horses(name, topSpeed, slowestSpeed, color, age) VALUES(?, ?, ?, ?, ?);";

    // Main User Workflow;
    public static void main(String[] args) {
        Scanner userScan = new Scanner(System.in);
        User user = initialLoginWorkflow(userScan, userOptions);
        String userResponse = "";
        Race race = new Race();
        ArrayList<Horse> horses;
        printWelcome();
        horses = horsesRacingSelection();
        while (!userResponse.equalsIgnoreCase("Q")) {
            printHorsesList(horses);
            System.out.println("\nCurrent Balance: " + user.balance);

            // Basic User Account WorkFlow
            if (user.accountType.equalsIgnoreCase("basic")) {
                userResponse = userWorkflowValidation(userScan);
            } else if (user.accountType.equalsIgnoreCase("admin")) {
                userResponse = adminUserWorkflowValidation(userScan);
            }
                // RACE OPTION ==============================================================
                if (userResponse.equalsIgnoreCase("R")) {
                    Horse horseSelected = horses.get(horseSelectionValidation(userScan) - 1);
                    int betNumber = placeBet(user.balance, userScan);
                    ArrayList<Horse> horsesInRaceOrder = race.raceHorses(horses);
                    printWinnersPodium(horsesInRaceOrder);
                    user.balance += betResults(betNumber, horseSelected, horsesInRaceOrder);
                    user.recordBalanceSQL();
                    race.recordRaceResults(horsesInRaceOrder, user, horseSelected);
                    race.resetRace(horses);
                    horses = horsesRacingSelection();

                    // QUIT OPTION ===========================================================
                } else if (userResponse.equalsIgnoreCase("Q")) {
                    System.out.println("You quit the program.");

                    // CHECK WINS OPTION =====================================================
                } else if (userResponse.equalsIgnoreCase("W")) {
                    Horse horseSelected = horses.get(horseSelectionValidation(userScan) - 1);
                    userResponse = checkWinsHorseValidation(userScan);

                    // CHECK WINS WITH YOU ===================================================
                    if (userResponse.equalsIgnoreCase("Y")) {
                        horseSelected.checkWinsWithUser(user);

                        // CHECK WINS WITH ANYONE ============================================
                    } else if (userResponse.equalsIgnoreCase("T")) {
                        horseSelected.checkWinsTotal();
                    }

                    // Admin Options

                    // Delete a User
                } else if (userResponse.equalsIgnoreCase("D")) {
                    userOptions.deleteUser(userScan);

                    // Update a User Password
                } else if (userResponse.equalsIgnoreCase("U")) {
                    userOptions.updateUserPassword(userScan);

                    // Add a New Horse
                } else if (userResponse.equalsIgnoreCase("A")) {
                    addHorse(userScan);
                } else if (userResponse.equalsIgnoreCase("N")) {
                    checkHorsesNeverWon();
                }

                // IF USER GOES NEGATIVE OR AT 0.
                if (user.balance <= 0) {
                    System.out.println("You begged for some coin outside. Somebody paid off your debts and gave you a few more coins.");
                    user.balance = 10;
                }







            }

        }



    // User Workflow for Logging In and Creation of User
    public static User initialLoginWorkflow(Scanner userScan, UserOptions userOptions){
        String userRes = "";
        User user = null;
        while (!userRes.equalsIgnoreCase("c") && !userRes.equalsIgnoreCase("l")) {
            System.out.print("Welcome to Alyx's Horse Racing Track! Would you like to [L]og In or [C]reate a new account? > ");
            userRes = userScan.nextLine();
            if (userRes.equalsIgnoreCase("c")) {
                user = userOptions.userCreation(userScan);
            } else if (userRes.equalsIgnoreCase("l")) {
                user = userOptions.loggingIn(userScan);

            } else {
                System.out.println("That is an incorrect response. Please put an L or a C. > ");
            }
        }
        return user;
    }

    public static ArrayList<Horse> horsesRacingSelection() {
        ArrayList<Horse>  horsesArray = new ArrayList<>();
        String SQL_TEST = "SELECT * FROM horses ORDER BY RANDOM() LIMIT 8;";
        try(Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement stmt = conn.prepareStatement(SQL_TEST);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
            horsesArray.add(new Horse(rs.getInt("id"), rs.getString("name"), rs.getInt("topSpeed"), rs.getInt("slowestSpeed")));
        }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return horsesArray;
    }

    // Opening Intro to Program:
    public static void printWelcome(){
        System.out.println("Welcome to Alyx's Horse Racetrack. Where we hope you lose all your digital currency.");
        System.out.println("Let me tell you how this game works. You start with 100 coins. You can bet any number of coins on any of the given horses.");
        System.out.println("If the horse you bet on gets 4th place, you get your coins back. If it gets 3rd place, you get double your coins.");
        System.out.println("If the horse you bet on gets 2nd place, you get triple your coins back. If your horse wins, you get quadruple your coins back.");

    }

    // Horse Racing List:

    public static void printHorsesList(ArrayList<Horse> horses) {
        System.out.println("\nThe horses racing today are:");

        for (int i = 0; i <= 7; i++) {
            int num = i + 1;
            System.out.println((num) + ". " + horses.get(i).name);
        }
    }

    public static void printWinnersPodium(ArrayList<Horse> horsesInRacingOrder) {
        System.out.println("\nThe Race Results are:");

        for (int i = 0; i <= 7; i++) {
            int num = i + 1;
            System.out.println((num) + ". " + horsesInRacingOrder.get(i).name);
        }
    }

    // Horse Selection Validation:
    public static int horseSelectionValidation(Scanner userScan){
        int horseSelection = -1;
        while (horseSelection <= 0 || horseSelection > 8) {
            System.out.print("Type the number next to the horse that you would like to select (1 - 8): ");
            String userResponse = userScan.nextLine();
            try {
                horseSelection = Integer.parseInt(userResponse);
                if (horseSelection <= 0 || horseSelection > 8) {
                    System.out.println("Pick a number between 1 and 8");
                }
            } catch (NumberFormatException e) {
                System.out.println("Pick a number between 1 and 8");
            }
        }
        return horseSelection;
    }
    // Bet Number Validation;
    public static int placeBet(int balance, Scanner userScan) {
        int bet = -1;
        while (bet > balance || bet <= 0) {
            System.out.print("Type the number of coins you would like to bet (must be positive and less than your balance) >");
            String betString = userScan.nextLine();
            try {
                bet = Integer.parseInt(betString);
                if (bet > balance) {
                    System.out.println("Please give a value less than your current balance total.");
                } else if (bet <= 0) {
                    System.out.println("Please give a value greater than 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid response.");
            }
        }
        return bet;
    }

    // Core User Workflow Validation
    public static String userWorkflowValidation(Scanner userScan) {
        String prompt = "Would you like to start a [r]ace, check a horse's number of [w]ins, check which horses have [n]ot won a race, or [q]uit?  >";
        System.out.println(prompt);
        String userResponse = userScan.nextLine().toUpperCase().trim();
        ArrayList<String> validResponses = new ArrayList<>(Arrays.asList("R", "Q", "W", "N"));
        while (!validResponses.contains("R") && !validResponses.contains("Q") && !validResponses.contains("W") && !validResponses.contains("N")) {
            System.out.println(prompt);
            userResponse = userScan.nextLine().toUpperCase().trim();
        }
        return userResponse;
    }

    // Admin User Workflow Validation
    public static String adminUserWorkflowValidation(Scanner userScan) {
        String prompt = "Would you like to [d]elete a user, [a]dd a horse, [u]pdate a user password, [r]ace, check [w]ins of a horse, check which horses have [n]ot won a race, or [q]uit? > ";
        System.out.println(prompt);
        String userResponse = userScan.nextLine().toUpperCase().trim();
        ArrayList<String> validResponses = new ArrayList<>(Arrays.asList("R", "Q", "W", "U", "A", "D", "N"));
        boolean validInput = !validResponses.contains("R") && !validResponses.contains("Q") && !validResponses.contains("W") && !validResponses.contains("A") && !validResponses.contains("D") && !validResponses.contains("U") && !validResponses.contains("N");
        while (validInput) {
            System.out.println(prompt);
            userResponse = userScan.nextLine().toUpperCase().trim();
            validInput = !validResponses.contains("R") && !validResponses.contains("Q") && !validResponses.contains("W") && !validResponses.contains("A") && !validResponses.contains("D") && !validResponses.contains("U") && !validResponses.contains("N");

        }
        return userResponse;
    }



    // Checking Number of Wins of a Horse WorkFlow Validation:
    public static String checkWinsHorseValidation(Scanner userScan) {
        String prompt = "Would you like to check the number of wins [y]ou've gotten with this horse or how many [t]otal wins it has gotten? >";
        System.out.println(prompt);
        String userResponse = userScan.nextLine().toUpperCase().trim();
        ArrayList<String> validResponses = new ArrayList<>(Arrays.asList("Y", "T"));
        while (!validResponses.contains("Y") && !validResponses.contains("T")) {
            System.out.println(prompt);
            userResponse = userScan.nextLine().toUpperCase().trim();
        }
        return userResponse;
    }

    // Bet Results

    public static int betResults(int bet, Horse horseSelection, ArrayList<Horse> horsesInRacingOrder) {
        HashMap<String, Integer> placingBetValues = new HashMap<>();
        placingBetValues.put("first", bet * 3);
        placingBetValues.put("second", bet * 2);
        placingBetValues.put("third", bet);
        placingBetValues.put("fourth", 0);
        placingBetValues.put("else", -bet);
        if (horseSelection.equals(horsesInRacingOrder.get(0))) {
            System.out.println("Your horse, " + horseSelection.name + " won the race!");
            System.out.println("You bet " + bet + " coins and so you collect " + placingBetValues.get("first") + "!");
            return placingBetValues.get("first");
        } else if (horseSelection.equals(horsesInRacingOrder.get(1))) {
            System.out.println("Your horse, " + horseSelection.name + " got second place!");
            System.out.println("You bet " + bet + " coins and so you collect " + placingBetValues.get("second") + "!");
            return placingBetValues.get("second");
        } else if (horseSelection.equals(horsesInRacingOrder.get(2))) {
            System.out.println("Your horse, " + horseSelection.name + " got third place!");
            System.out.println("You bet " + bet + " coins and so you collect " + placingBetValues.get("third") + "!");
            return placingBetValues.get("third");

        } else if (horseSelection.equals(horsesInRacingOrder.get(3))) {
            System.out.println("Your horse, " + horseSelection.name + " got fourth place!");
            System.out.println("You bet " + bet + " coins and so you do not lose any funds!");
            return placingBetValues.get("fourth");
        } else {
            System.out.println("Your horse did not get on the podium. Better luck next time.");
            return placingBetValues.get("else");
        }
    }

    // Add a New Horse Workflow

    // top Speed user input validation:

    public static int topSpeedValidation(Scanner userScan) {
        int topSpeed = -1;
        while (topSpeed > 150 || topSpeed < 75) {
            System.out.println("What would you like the top speed of this horse to be? (Value must be between 150 and 75) > ");
            String userResponse = userScan.nextLine();
            try {
                topSpeed = Integer.parseInt(userResponse);
                if (topSpeed > 150) {
                    System.out.println("The top speed given is too high. Please put a number between 150 and 75.");
                } else if (topSpeed < 75) {
                    System.out.println("The top speed given is too low. Please put a number between 150 and 75.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please put a number.");
            }
        }
        return topSpeed;
    }


    public static int lowSpeedValidation(Scanner userScan) {
        int lowSpeed = -1;
        while (lowSpeed > 74 || lowSpeed < 25) {
            System.out.println("What would you like the low speed of this horse to be? (Value must be between 74 and 25) > ");
            String userResponse = userScan.nextLine();
            try {
                lowSpeed = Integer.parseInt(userResponse);
                if (lowSpeed > 74) {
                    System.out.println("The low speed given is too high. Please put a number between 74 and 25.");
                } else if (lowSpeed < 25) {
                    System.out.println("The low speed given is too low. Please put a number between 74 and 25.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please put a number.");
            }
        }
        return lowSpeed;
    }

    // age validation for Horse profile
    public static int ageValidation(Scanner userScan) {
        int age = -1;
        while (age > 100 || age <= 0) {
            System.out.println("What would you like the age of this horse to be? > ");
            String userResponse = userScan.nextLine();
            try {
                age = Integer.parseInt(userResponse);
                if (age > 100) {
                    System.out.println("The age given is too high.");
                } else if (age <= 0) {
                    System.out.println("The age given is too low. Please put a number greater than 0.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please put a number.");
            }
        }
        return age;
    }

    // Check which horses have never won:

    public static void checkHorsesNeverWon(){
        String CHECK_HORSES_NEVER_WON_QUERY = "SELECT horses.id, name FROM horses LEFT JOIN races ON horses.id = races.first_place_horse_id WHERE first_place_horse_id IS NULL;";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement CHECK_HORSES_NEVER_WON_STMT = conn.createStatement();
            ResultSet horsesNeverWonRS = CHECK_HORSES_NEVER_WON_STMT.executeQuery(CHECK_HORSES_NEVER_WON_QUERY);
            System.out.println("The horses that have never won are: ");

            try {
                while (horsesNeverWonRS.next()) {
                    if (horsesNeverWonRS.getInt("id") == 0) {
                        System.out.println("All horses have won at least once.");
                    } else {
                        System.out.println(horsesNeverWonRS.getString("name"));
                    }


                }
            } catch (SQLException error) {
                System.out.println(error.getMessage());
            }
        }catch(SQLException e){
                System.out.println(e.getMessage());
            }
        }

    // Adding a horse as an admin

    public static void addHorse(Scanner userScan) {
        System.out.println("What would you like to name the horse? > ");
        String name = userScan.nextLine();

        int topSpeed = topSpeedValidation(userScan);
        int lowSpeed = lowSpeedValidation(userScan);

        System.out.println("What color is the horse? > ");
        String color = userScan.nextLine();

        int age = ageValidation(userScan);


        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            PreparedStatement INSERT_HORSE_STMT = conn.prepareStatement(INSERT_HORSE_SQL);
            INSERT_HORSE_STMT.setString(1, name);
            INSERT_HORSE_STMT.setInt(2, topSpeed);
            INSERT_HORSE_STMT.setInt(3, lowSpeed);
            INSERT_HORSE_STMT.setString(4, color);
            INSERT_HORSE_STMT.setInt(5, age);

            INSERT_HORSE_STMT.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}

