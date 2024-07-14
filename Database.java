package com.fulkoping;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    // Användarnamn för att ansluta till databasen
    public static String username = "root";
    // Lösenord för att ansluta till databasen
    public static String password = "password";
    // Namnet på databasen
    public static String database = "librarydb";
    // Portnummer för databasen
    public static int port = 3306;

    // Metod för att få en anslutning till databasen
    public static Connection getConnection() {
        try {
            // Försök att ansluta till databasen med de angivna uppgifterna
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:" + port + "/" + database,
                    username,
                    password
            );
        } catch (SQLException e) {
            // Om det blir ett fel, skriv ut SQL-felet
            printSQLException(e);
            return null;
        }
    }

    // Metod för att skriva ut detaljerad information om ett SQL-fel
    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                // Skriv ut felmeddelanden och information om felet
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                // Om det finns en bakomliggande orsak, skriv ut den också
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}
