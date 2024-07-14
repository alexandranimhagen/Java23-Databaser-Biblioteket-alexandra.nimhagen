package com.fulkoping;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {
    // Variabel för att hålla reda på inloggad användare
    private int loggedInUserId = -1;

    // Getter och setter för inloggad användar-ID
    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    // Skriver ut alla böcker i databasen
    public void printBook(Connection conn) {
        String sql = "SELECT * FROM books";

        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                System.out.println("book_id: " + rs.getString("id"));
                System.out.println("title: " + rs.getString("title"));
                System.out.println("author_id: " + rs.getString("author_id"));
                System.out.println("status: " + rs.getString("status"));
                System.out.println("");
            }
        } catch (SQLException ex) {
            Database.printSQLException(ex);
        }
    }

    // Skriver ut alla magasin i databasen
    public void printMedia(Connection conn) {
        String sql = "SELECT * FROM magazines";

        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                System.out.println("media_id: " + rs.getString("id"));
                System.out.println("title: " + rs.getString("name"));
                System.out.println("status: " + rs.getString("status"));
                System.out.println("");
            }
        } catch (SQLException ex) {
            Database.printSQLException(ex);
        }
    }

    // Loggar ut användaren
    public void logOut() {
        loggedInUserId = -1;
        System.out.println("Utloggad");
    }

    // Lånar en bok
    public void borrowBook(Connection connection, Scanner scanner) {
        System.out.print("Ange book_id för boken du vill låna: ");
        int bookId = scanner.nextInt();
        scanner.nextLine();

        try {
            PreparedStatement checkAvailability = connection.prepareStatement("SELECT status FROM books WHERE id = ?");
            checkAvailability.setInt(1, bookId);
            ResultSet availabilityResult = checkAvailability.executeQuery();

            if (availabilityResult.next() && availabilityResult.getString("status").equals("Available")) {
                PreparedStatement borrowBook = connection.prepareStatement("UPDATE books SET status = 'Not Available' WHERE id = ?");
                borrowBook.setInt(1, bookId);
                borrowBook.executeUpdate();

                LocalDate dueDate = LocalDate.now().plusDays(30);
                LocalDateTime dueDateTime = dueDate.atTime(18, 0); // Setting time to 18:00
                PreparedStatement recordLoan = connection.prepareStatement("INSERT INTO loanlogg (user_id, authors_books_id, start_date, end_date) VALUES (?, ?, NOW(), ?)");
                recordLoan.setInt(1, loggedInUserId);
                recordLoan.setInt(2, bookId);
                recordLoan.setTimestamp(3, Timestamp.valueOf(dueDateTime));
                recordLoan.executeUpdate();

                System.out.println("Du har lånat boken. Glöm inte att lämna tillbaka i tid!");
            } else {
                System.out.println("Tyvärr, boken är redan utlånad eller existerar inte.");
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    }

    // Lämnar tillbaka en bok
    public void returnBook(Connection connection, Scanner scanner) {
        System.out.print("Ange book_id för boken du vill lämna tillbaka: ");
        int bookId = scanner.nextInt();
        scanner.nextLine();

        try {
            PreparedStatement checkBorrowed = connection.prepareStatement("SELECT * FROM loanlogg WHERE user_id = ? AND authors_books_id = ? AND returned = 0");
            checkBorrowed.setInt(1, loggedInUserId);
            checkBorrowed.setInt(2, bookId);
            ResultSet borrowedResult = checkBorrowed.executeQuery();

            if (borrowedResult.next()) {
                PreparedStatement returnBook = connection.prepareStatement("UPDATE books SET status = 'Available' WHERE id = ?");
                returnBook.setInt(1, bookId);
                returnBook.executeUpdate();

                PreparedStatement recordReturn = connection.prepareStatement("UPDATE loanlogg SET end_date = NOW(), returned = 1 WHERE user_id = ? AND authors_books_id = ?");
                recordReturn.setInt(1, loggedInUserId);
                recordReturn.setInt(2, bookId);
                recordReturn.executeUpdate();

                System.out.println("Du har lämnat tillbaka boken. Tack!");
            } else {
                System.out.println("Du har inte lånat boken med id " + bookId + ".");
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    }


    // Lånar ett magasin
    public void borrowMedia(Connection connection, Scanner scanner) {
        System.out.print("Ange media_id för den media du vill låna: ");
        int mediaId = scanner.nextInt();
        scanner.nextLine();

        try {
            PreparedStatement checkAvailability = connection.prepareStatement("SELECT status FROM magazines WHERE id = ?");
            checkAvailability.setInt(1, mediaId);
            ResultSet availabilityResult = checkAvailability.executeQuery();

            if (availabilityResult.next() && availabilityResult.getString("status").equals("Available")) {
                PreparedStatement borrowMedia = connection.prepareStatement("UPDATE magazines SET status = 'Not Available' WHERE id = ?");
                borrowMedia.setInt(1, mediaId);
                borrowMedia.executeUpdate();

                LocalDate dueDate = LocalDate.now().plusDays(10);
                LocalDateTime dueDateTime = dueDate.atTime(18, 0); // Setting time to 18:00
                PreparedStatement recordLoan = connection.prepareStatement("INSERT INTO loanlogg (user_id, magazine_id, start_date, end_date) VALUES (?, ?, NOW(), ?)");
                recordLoan.setInt(1, loggedInUserId);
                recordLoan.setInt(2, mediaId);
                recordLoan.setTimestamp(3, Timestamp.valueOf(dueDateTime));
                recordLoan.executeUpdate();

                System.out.println("Du har lånat media. Glöm inte att lämna tillbaka i tid!");
            } else {
                System.out.println("Tyvärr, media är redan utlånad eller existerar inte.");
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    }

    // Lämnar tillbaka ett magasin
    public void returnMedia(Connection connection, Scanner scanner) {
        System.out.print("Ange media_id för den media du vill lämna tillbaka: ");
        int mediaId = scanner.nextInt();
        scanner.nextLine();

        try {
            PreparedStatement checkBorrowed = connection.prepareStatement("SELECT * FROM loanlogg WHERE user_id = ? AND magazine_id = ? AND returned = 0");
            checkBorrowed.setInt(1, loggedInUserId);
            checkBorrowed.setInt(2, mediaId);
            ResultSet borrowedResult = checkBorrowed.executeQuery();

            if (borrowedResult.next()) {
                PreparedStatement returnMedia = connection.prepareStatement("UPDATE magazines SET status = 'Available' WHERE id = ?");
                returnMedia.setInt(1, mediaId);
                returnMedia.executeUpdate();

                PreparedStatement recordReturn = connection.prepareStatement("UPDATE loanlogg SET end_date = NOW(), returned = 1 WHERE user_id = ? AND magazine_id = ?");
                recordReturn.setInt(1, loggedInUserId);
                recordReturn.setInt(2, mediaId);
                recordReturn.executeUpdate();

                System.out.println("Du har lämnat tillbaka media. Tack!");
            } else {
                System.out.println("Du har inte lånat media med id " + mediaId + ".");
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    }

    // Uppdatera användarprofilen
    public void updateProfile(Connection connection, Scanner scanner) {
        System.out.println("Välkommen till profilsidan! Vad vill du uppdatera?");
        System.out.println("1. Namn");
        System.out.println("2. Lösenord");
        System.out.println("3. E-post");
        System.out.println("4. Avbryt");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                updateName(connection, scanner);
                break;
            case "2":
                updatePassword(connection, scanner);
                break;
            case "3":
                updateEmail(connection, scanner);
                break;
            case "4":
                System.out.println("Avbryter uppdatering av profil.");
                break;
            default:
                System.out.println("Ogiltigt val.");
        }
    }

    // Uppdaterar användarens namn
    private void updateName(Connection connection, Scanner scanner) {
        System.out.print("Ange ditt nya namn: ");
        String newName = scanner.nextLine();

        try {
            PreparedStatement updateName = connection.prepareStatement("UPDATE users SET name = ? WHERE id = ?");
            updateName.setString(1, newName);
            updateName.setInt(2, loggedInUserId);

            int rowsAffected = updateName.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Namnet har uppdaterats.");
            } else {
                System.out.println("Tyvärr, något gick fel vid uppdatering av namnet.");
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    }

    // Uppdaterar användarens lösenord
    private void updatePassword(Connection connection, Scanner scanner) {
        System.out.print("Ange det nya lösenordet: ");
        String newPassword = scanner.nextLine();

        try {
            PreparedStatement updatePassword = connection.prepareStatement("UPDATE users SET hashed_password = ? WHERE id = ?");
            updatePassword.setString(1, newPassword);
            updatePassword.setInt(2, loggedInUserId);

            int rowsAffected = updatePassword.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Lösenordet har uppdaterats.");
            } else {
                System.out.println("Tyvärr, något gick fel vid uppdatering av lösenordet.");
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    }

    // Uppdaterar användarens e-post
    private void updateEmail(Connection connection, Scanner scanner) {
        System.out.print("Ange din nya e-post: ");
        String newEmail = scanner.nextLine();

        try {
            PreparedStatement updateEmail = connection.prepareStatement("UPDATE users SET email = ? WHERE id = ?");
            updateEmail.setString(1, newEmail);
            updateEmail.setInt(2, loggedInUserId);

            int rowsAffected = updateEmail.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("E-postadressen har uppdaterats.");
            } else {
                System.out.println("Tyvärr, något gick fel vid uppdatering av e-postadressen.");
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    }
    // Visar lånehistorik för böcker
    public void viewBorrowedBooks(Connection connection) {
        try {
            String sql = "SELECT books.title AS item_title, loanlogg.start_date, loanlogg.end_date, loanlogg.returned " +
                    "FROM books " +
                    "JOIN loanlogg ON books.id = loanlogg.authors_books_id " +
                    "WHERE loanlogg.user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, loggedInUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("Dina lånade böcker:");
            while (resultSet.next()) {
                String title = resultSet.getString("item_title");
                String loanDate = resultSet.getString("start_date");
                String dueDate = resultSet.getString("end_date");
                boolean returned = resultSet.getBoolean("returned");

                System.out.println("Titel: " + title);
                System.out.println("Lånedatum: " + loanDate);
                System.out.println("Förfallodatum: " + dueDate);
                System.out.println("Återlämnad: " + (returned ? "Ja" : "Nej"));
                System.out.println();
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    }

    // Visar lånade magasin
    public void viewBorrowedMedia(Connection connection) {
        try {
            String sql = "SELECT magazines.name AS item_title, loanlogg.start_date, loanlogg.end_date, loanlogg.returned " +
                    "FROM magazines " +
                    "JOIN loanlogg ON magazines.id = loanlogg.magazine_id " +
                    "WHERE loanlogg.user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, loggedInUserId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Dina lånade media:");
                do {
                    String title = resultSet.getString("item_title");
                    String loanDate = resultSet.getString("start_date");
                    String dueDate = resultSet.getString("end_date");
                    boolean returned = resultSet.getBoolean("returned");

                    System.out.println("Titel: " + title);
                    System.out.println("Lånedatum: " + loanDate);
                    System.out.println("Förfallodatum: " + dueDate);
                    System.out.println("Återlämnad: " + (returned ? "Ja" : "Nej"));
                    System.out.println();
                } while (resultSet.next());
            } else {
                System.out.println("Du har inga lånade media för närvarande.");
            }
        } catch (SQLException e) {
            Database.printSQLException(e);
        }
    }
}