create schema schema;

comment on schema public is 'standard public schema';

alter schema public owner to pg_database_owner;

grant usage on schema public to public;

grant create, usage on schema public to "Alyx C";


/*  ========== Query One Searching if Username is Unique:
   "SELECT id FROM users WHERE username = ?;"

   =========== Query Two Searching if username and password are same for login:
    "SELECT id FROM users WHERE username = ? AND password = crypt(?, password);"

    ========== Query Three Inserting New User in Users Table:
    "INSERT INTO users(username, password, balance, account_type) " +
            "VALUES(?,crypt(?, gen_salt('bf')),100, ?)"

    ========== Query Four Inserting New Horse in Horses Table:
    "INSERT INTO horses(name, upperBoundSpeed, lowerBoundSpeed)
    "VALUES(?, ?, ?);"

    ========== Query Five Inserting New Race Results in Races Table:
    "INSERT INTO races (username, user_selection_horse_id, first_place_horse_id, second_place_horse_id, third_place_horse_id, fourth_place_horse_id)
    VALUES (?, ?, ?, ?, ?, ?);";


    ========== Query Six Searching how many times horse has won:
    "SELECT COUNT(*) FROM races WHERE firstPlace = ?;*

    ========== Query Seven Update User Balance:
    "UPDATE users SET balance = ? WHERE username = ?;"

    ========== Query Eight Randomly Selecting 8 Horses to Race:
    "SELECT * FROM horses ORDER BY RANDOM() LIMIT 8;"

    ========== Query Nine Update User Password:
    "UPDATE users SET password = crypt(?, gen_salt('bf')) where username = ?;"

    ========== Query Delete User:
    "DELETE FROM users WHERE username = ?;"

    ========== Query Join:
    "SELECT firstPlace FROM races INNER JOIN horses ON races.firstPlace = horses.id;"

    ========== Query Join:
    "SELECT user FROM races INNER JOIN users ON races.user = user.id:"
 */