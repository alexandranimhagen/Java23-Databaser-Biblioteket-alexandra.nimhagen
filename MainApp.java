package com.fulkoping;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class MainApp extends Application {
    private static Main mainInstance = new Main();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Biblioteket");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Välkommen till Fulköpings bibliotek!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button loginButton = new Button("Logga in");
        Button registerButton = new Button("Registrera ny användare");

        registerButton.setOnAction(e -> showRegisterDialog());
        loginButton.setOnAction(e -> showLoginDialog());

        root.getChildren().addAll(welcomeLabel, registerButton, loginButton);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void showRegisterDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Registrera ny användare");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));

        TextField nameField = new TextField();
        nameField.setPromptText("Namn");
        TextField emailField = new TextField();
        emailField.setPromptText("E-post");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Lösenord");
        Button registerButton = new Button("Registrera");

        registerButton.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            registerUser(name, email, password);
            dialog.close();
        });

        dialogVBox.getChildren().addAll(nameField, emailField, passwordField, registerButton);

        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showLoginDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Logga in");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));

        TextField emailField = new TextField();
        emailField.setPromptText("E-post");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Lösenord");
        Button loginButton = new Button("Logga in");

        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            logInUser(email, password);
            dialog.close();
        });

        dialogVBox.getChildren().addAll(emailField, passwordField, loginButton);

        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void registerUser(String name, String email, String password) {
        Connection connection = Database.getConnection();
        if (connection == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Kunde inte ansluta till databasen");
            return;
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users (name, email, hashed_password) VALUES (?, ?, ?)", java.sql.Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Ny användare registrerad med användar-ID: " + generatedKeys.getInt(1));
                    mainInstance.setLoggedInUserId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }
    }

    private void logInUser(String email, String password) {
        Connection connection = Database.getConnection();
        if (connection == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Kunde inte ansluta till databasen");
            return;
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM users WHERE email = ? AND hashed_password = ?");
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                mainInstance.setLoggedInUserId(resultSet.getInt("id"));
                showAlert(Alert.AlertType.INFORMATION, "Success", "Inloggning lyckades. Användare inloggad med ID: " + mainInstance.getLoggedInUserId());
                showDashboard();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Inloggning misslyckades. Felaktigt e-post eller lösenord.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }
    }

    private void showDashboard() {
        Stage dashboardStage = new Stage();
        dashboardStage.setTitle("Dashboard");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);

        Button listBooksButton = new Button("Visa lista över böcker");
        Button listMagazinesButton = new Button("Visa lista över magasin");
        Button borrowButton = new Button("Låna med ID");
        Button returnItemButton = new Button("Lämna tillbaka med ID");
        Button currentLoansButton = new Button("Mina aktuella lån");
        Button loanHistoryButton = new Button("Lånehistorik");
        Button updateProfileButton = new Button("Uppdatera profil");
        Button logOutButton = new Button("Logga ut");

        listBooksButton.setMinWidth(200);
        listMagazinesButton.setMinWidth(200);
        borrowButton.setMinWidth(200);
        returnItemButton.setMinWidth(200);
        currentLoansButton.setMinWidth(200);
        loanHistoryButton.setMinWidth(200);
        updateProfileButton.setMinWidth(200);
        logOutButton.setMinWidth(200);

        listBooksButton.setOnAction(e -> showBookListDialog());
        listMagazinesButton.setOnAction(e -> showMagazineListDialog());
        borrowButton.setOnAction(e -> showBorrowDialog());
        returnItemButton.setOnAction(e -> showReturnDialog());
        currentLoansButton.setOnAction(e -> viewLoanStatus(Database.getConnection()));
        loanHistoryButton.setOnAction(e -> viewLoanHistory(Database.getConnection()));
        updateProfileButton.setOnAction(e -> updateProfile(Database.getConnection()));
        logOutButton.setOnAction(e -> {
            logOut();
            dashboardStage.close();
        });

        root.getChildren().addAll(listBooksButton, listMagazinesButton, borrowButton, returnItemButton, currentLoansButton, loanHistoryButton, updateProfileButton, logOutButton);

        dashboardStage.setScene(scene);
        dashboardStage.setMaximized(true);
        dashboardStage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showBookListDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Lista över böcker");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));

        TextField searchField = new TextField();
        searchField.setPromptText("Sök böcker...");

        ListView<String> bookListView = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList();

        Connection connection = Database.getConnection();
        if (connection == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Kunde inte ansluta till databasen");
            return;
        }

        try {
            String sql = "SELECT books.id, books.title, authors.firstname, authors.lastname, books.status " +
                    "FROM books " +
                    "JOIN authors ON books.author_id = authors.id";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String bookDetails = "ID: " + rs.getString("id") + ", Titel: " + rs.getString("title") +
                        ", Författare: " + rs.getString("firstname") + " " + rs.getString("lastname") +
                        ", Status: " + (rs.getString("status").equals("Available") ? "Tillgänglig" : "Inte tillgänglig");
                items.add(bookDetails);
            }
        } catch (SQLException ex) {
            Database.printSQLException(ex);
        }

        bookListView.setItems(items);

        Button borrowButton = new Button("Låna");
        Button closeButton = new Button("Återgå");

        borrowButton.setOnAction(e -> {
            String selectedItem = bookListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String bookId = selectedItem.split(",")[0].split(":")[1].trim();
                borrowBookWithId(connection, bookId);
            }
        });

        closeButton.setOnAction(e -> dialog.close());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<String> filteredItems = FXCollections.observableArrayList();
            for (String item : items) {
                if (item.toLowerCase().contains(newValue.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
            bookListView.setItems(filteredItems);
        });

        dialogVBox.getChildren().addAll(searchField, bookListView, borrowButton, closeButton);

        Scene dialogScene = new Scene(dialogVBox, 800, 600);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showMagazineListDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Lista över magasin");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));

        TextField searchField = new TextField();
        searchField.setPromptText("Sök magasin...");

        ListView<String> magazineListView = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList();

        Connection connection = Database.getConnection();
        if (connection == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Kunde inte ansluta till databasen");
            return;
        }

        try {
            String sql = "SELECT * FROM magazines";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String magazineDetails = "ID: " + rs.getString("id") + ", Titel: " + rs.getString("name") +
                        ", Status: " + (rs.getString("status").equals("Available") ? "Tillgänglig" : "Inte tillgänglig");
                items.add(magazineDetails);
            }
        } catch (SQLException ex) {
            Database.printSQLException(ex);
        }

        magazineListView.setItems(items);

        Button borrowButton = new Button("Låna");
        Button closeButton = new Button("Återgå");

        borrowButton.setOnAction(e -> {
            String selectedItem = magazineListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String magazineId = selectedItem.split(",")[0].split(":")[1].trim();
                borrowMediaWithId(connection, magazineId);
            }
        });

        closeButton.setOnAction(e -> dialog.close());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<String> filteredItems = FXCollections.observableArrayList();
            for (String item : items) {
                if (item.toLowerCase().contains(newValue.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
            magazineListView.setItems(filteredItems);
        });

        dialogVBox.getChildren().addAll(searchField, magazineListView, borrowButton, closeButton);

        Scene dialogScene = new Scene(dialogVBox, 800, 600);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showBorrowDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Låna");
        dialog.setHeaderText("Låna en bok eller magasin");
        dialog.setContentText("Ange ID för det du vill låna:");

        dialog.showAndWait().ifPresent(id -> {
            if (!id.trim().isEmpty()) {
                Connection connection = Database.getConnection();
                if (connection != null) {
                    if (isBook(connection, id)) {
                        borrowBookWithId(connection, id);
                    } else if (isMagazine(connection, id)) {
                        borrowMediaWithId(connection, id);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Tyvärr, det finns inget objekt med det ID.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Kunde inte ansluta till databasen");
                }
            }
        });
    }

    private void showReturnDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Lämna tillbaka");
        dialog.setHeaderText("Lämna tillbaka en bok eller magasin");
        dialog.setContentText("Ange ID för det du vill lämna tillbaka:");

        dialog.showAndWait().ifPresent(id -> {
            if (!id.trim().isEmpty()) {
                Connection connection = Database.getConnection();
                if (connection != null) {
                    if (isBook(connection, id)) {
                        returnBookWithId(connection, id);
                    } else if (isMagazine(connection, id)) {
                        returnMediaWithId(connection, id);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Tyvärr, det finns inget objekt med det ID.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Kunde inte ansluta till databasen");
                }
            }
        });
    }

    private boolean isBook(Connection connection, String id) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM books WHERE id = ?");
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }
        return false;
    }

    private boolean isMagazine(Connection connection, String id) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM magazines WHERE id = ?");
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }
        return false;
    }

    private void borrowBookWithId(Connection connection, String bookId) {
        try {
            PreparedStatement checkAvailability = connection.prepareStatement("SELECT status FROM books WHERE id = ?");
            checkAvailability.setInt(1, Integer.parseInt(bookId));
            ResultSet availabilityResult = checkAvailability.executeQuery();

            if (availabilityResult.next() && availabilityResult.getString("status").equals("Available")) {
                PreparedStatement borrowBook = connection.prepareStatement("UPDATE books SET status = 'Not Available' WHERE id = ?");
                borrowBook.setInt(1, Integer.parseInt(bookId));
                borrowBook.executeUpdate();

                LocalDate dueDate = LocalDate.now().plusDays(30);
                PreparedStatement recordLoan = connection.prepareStatement("INSERT INTO loanlogg (user_id, authors_books_id, start_date, end_date) VALUES (?, ?, NOW(), ?)");
                recordLoan.setInt(1, mainInstance.getLoggedInUserId());
                recordLoan.setInt(2, Integer.parseInt(bookId));
                recordLoan.setDate(3, Date.valueOf(dueDate));
                recordLoan.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Du har lånat boken. Glöm inte att lämna tillbaka i tid!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Tyvärr, boken är redan utlånad eller existerar inte.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }
    }

    private void borrowMediaWithId(Connection connection, String mediaId) {
        try {
            PreparedStatement checkAvailability = connection.prepareStatement("SELECT status FROM magazines WHERE id = ?");
            checkAvailability.setInt(1, Integer.parseInt(mediaId));
            ResultSet availabilityResult = checkAvailability.executeQuery();

            if (availabilityResult.next() && availabilityResult.getString("status").equals("Available")) {
                PreparedStatement borrowMedia = connection.prepareStatement("UPDATE magazines SET status = 'Not Available' WHERE id = ?");
                borrowMedia.setInt(1, Integer.parseInt(mediaId));
                borrowMedia.executeUpdate();

                LocalDate dueDate = LocalDate.now().plusDays(10);
                PreparedStatement recordLoan = connection.prepareStatement("INSERT INTO loanlogg (user_id, magazine_id, start_date, end_date) VALUES (?, ?, NOW(), ?)");
                recordLoan.setInt(1, mainInstance.getLoggedInUserId());
                recordLoan.setInt(2, Integer.parseInt(mediaId));
                recordLoan.setDate(3, Date.valueOf(dueDate));
                recordLoan.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Du har lånat media. Glöm inte att lämna tillbaka i tid!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Tyvärr, media är redan utlånad eller existerar inte.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }
    }

    private void returnBookWithId(Connection connection, String bookId) {
        try {
            PreparedStatement checkBorrowed = connection.prepareStatement("SELECT * FROM loanlogg WHERE user_id = ? AND authors_books_id = ? AND returned = 0");
            checkBorrowed.setInt(1, mainInstance.getLoggedInUserId());
            checkBorrowed.setInt(2, Integer.parseInt(bookId));
            ResultSet borrowedResult = checkBorrowed.executeQuery();

            if (borrowedResult.next()) {
                PreparedStatement returnBook = connection.prepareStatement("UPDATE books SET status = 'Available' WHERE id = ?");
                returnBook.setInt(1, Integer.parseInt(bookId));
                returnBook.executeUpdate();

                PreparedStatement recordReturn = connection.prepareStatement("UPDATE loanlogg SET end_date = NOW(), returned = 1 WHERE user_id = ? AND authors_books_id = ?");
                recordReturn.setInt(1, mainInstance.getLoggedInUserId());
                recordReturn.setInt(2, Integer.parseInt(bookId));
                recordReturn.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Du har lämnat tillbaka boken. Tack!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Du har inte lånat boken med id " + bookId + ".");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }
    }

    private void returnMediaWithId(Connection connection, String mediaId) {
        try {
            PreparedStatement checkBorrowed = connection.prepareStatement("SELECT * FROM loanlogg WHERE user_id = ? AND magazine_id = ? AND returned = 0");
            checkBorrowed.setInt(1, mainInstance.getLoggedInUserId());
            checkBorrowed.setInt(2, Integer.parseInt(mediaId));
            ResultSet borrowedResult = checkBorrowed.executeQuery();

            if (borrowedResult.next()) {
                PreparedStatement returnMedia = connection.prepareStatement("UPDATE magazines SET status = 'Available' WHERE id = ?");
                returnMedia.setInt(1, Integer.parseInt(mediaId));
                returnMedia.executeUpdate();

                PreparedStatement recordReturn = connection.prepareStatement("UPDATE loanlogg SET end_date = NOW(), returned = 1 WHERE user_id = ? AND magazine_id = ?");
                recordReturn.setInt(1, mainInstance.getLoggedInUserId());
                recordReturn.setInt(2, Integer.parseInt(mediaId));
                recordReturn.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Du har lämnat tillbaka media. Tack!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Du har inte lånat media med id " + mediaId + ".");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }
    }


    private void viewLoanStatus(Connection connection) {
        Stage dialog = new Stage();
        dialog.setTitle("Mina aktuella lån");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));

        ListView<String> loanListView = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            String sqlBooks = "SELECT books.id, books.title AS item_title, loanlogg.start_date, loanlogg.end_date " +
                    "FROM books " +
                    "JOIN loanlogg ON books.id = loanlogg.authors_books_id " +
                    "WHERE loanlogg.user_id = ? AND loanlogg.returned = 0 " +
                    "ORDER BY loanlogg.end_date ASC";
            PreparedStatement preparedStatementBooks = connection.prepareStatement(sqlBooks);
            preparedStatementBooks.setInt(1, mainInstance.getLoggedInUserId());

            ResultSet resultSetBooks = preparedStatementBooks.executeQuery();

            while (resultSetBooks.next()) {
                LocalDateTime startDateTime = resultSetBooks.getTimestamp("start_date").toLocalDateTime();
                String startDateFormatted = startDateTime.toLocalDate().format(dateFormatter) + " " + startDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES).format(timeFormatter);

                LocalDateTime endDateTime = resultSetBooks.getTimestamp("end_date").toLocalDateTime();
                String endDateFormatted = endDateTime.toLocalDate().format(dateFormatter) + ", " + "18:00";

                String loanDetails = "Bok: " + resultSetBooks.getString("item_title") +
                        ", Lånedatum: " + startDateFormatted +
                        ", Återlämnas senast: " + endDateFormatted +
                        ", ID: " + resultSetBooks.getString("id");
                items.add(loanDetails);
            }

            String sqlMagazines = "SELECT magazines.id, magazines.name AS item_title, loanlogg.start_date, loanlogg.end_date " +
                    "FROM magazines " +
                    "JOIN loanlogg ON magazines.id = loanlogg.magazine_id " +
                    "WHERE loanlogg.user_id = ? AND loanlogg.returned = 0 " +
                    "ORDER BY loanlogg.end_date ASC";
            PreparedStatement preparedStatementMagazines = connection.prepareStatement(sqlMagazines);
            preparedStatementMagazines.setInt(1, mainInstance.getLoggedInUserId());

            ResultSet resultSetMagazines = preparedStatementMagazines.executeQuery();

            while (resultSetMagazines.next()) {
                LocalDateTime startDateTime = resultSetMagazines.getTimestamp("start_date").toLocalDateTime();
                String startDateFormatted = startDateTime.toLocalDate().format(dateFormatter) + " " + startDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES).format(timeFormatter);

                LocalDateTime endDateTime = resultSetMagazines.getTimestamp("end_date").toLocalDateTime();
                String endDateFormatted = endDateTime.toLocalDate().format(dateFormatter) + ", " + "18:00";

                String loanDetails = "Magasin: " + resultSetMagazines.getString("item_title") +
                        ", Lånedatum: " + startDateFormatted +
                        ", Återlämnas senast: " + endDateFormatted +
                        ", ID: " + resultSetMagazines.getString("id");
                items.add(loanDetails);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }

        loanListView.setItems(items);

        Button returnButton = new Button("Lämna tillbaka");
        returnButton.setOnAction(e -> {
            String selectedItem = loanListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String itemId = selectedItem.split(", ID: ")[1].trim();
                if (selectedItem.startsWith("Bok:")) {
                    returnBookWithId(connection, itemId);
                } else if (selectedItem.startsWith("Magasin:")) {
                    returnMediaWithId(connection, itemId);
                }
                items.remove(selectedItem);
            }
        });

        Button closeButton = new Button("Återgå");
        closeButton.setOnAction(e -> dialog.close());

        dialogVBox.getChildren().addAll(loanListView, returnButton, closeButton);

        Scene dialogScene = new Scene(dialogVBox, 800, 600);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void viewLoanHistory(Connection connection) {
        Stage dialog = new Stage();
        dialog.setTitle("Lånehistorik");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));

        ListView<String> loanListView = new ListView<>();
        ObservableList<String> items = FXCollections.observableArrayList();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            String sqlBooks = "SELECT books.id, books.title AS item_title, loanlogg.start_date, loanlogg.end_date " +
                    "FROM books " +
                    "JOIN loanlogg ON books.id = loanlogg.authors_books_id " +
                    "WHERE loanlogg.user_id = ? AND loanlogg.returned = 1 " +
                    "ORDER BY loanlogg.end_date DESC";
            PreparedStatement preparedStatementBooks = connection.prepareStatement(sqlBooks);
            preparedStatementBooks.setInt(1, mainInstance.getLoggedInUserId());

            ResultSet resultSetBooks = preparedStatementBooks.executeQuery();

            while (resultSetBooks.next()) {
                LocalDateTime startDateTime = resultSetBooks.getTimestamp("start_date").toLocalDateTime();
                String startDateFormatted = startDateTime.toLocalDate().format(dateFormatter) + " " + startDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES).format(timeFormatter);

                LocalDateTime endDateTime = resultSetBooks.getTimestamp("end_date").toLocalDateTime();
                String endDateFormatted = endDateTime.toLocalDate().format(dateFormatter) + " " + endDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES).format(timeFormatter);

                String loanDetails = "Bok: " + resultSetBooks.getString("item_title") +
                        ", Lånedatum: " + startDateFormatted +
                        ", Återlämnad: " + endDateFormatted +
                        ", ID: " + resultSetBooks.getString("id");
                items.add(loanDetails);
            }

            String sqlMagazines = "SELECT magazines.id, magazines.name AS item_title, loanlogg.start_date, loanlogg.end_date " +
                    "FROM magazines " +
                    "JOIN loanlogg ON magazines.id = loanlogg.magazine_id " +
                    "WHERE loanlogg.user_id = ? AND loanlogg.returned = 1 " +
                    "ORDER BY loanlogg.end_date DESC";
            PreparedStatement preparedStatementMagazines = connection.prepareStatement(sqlMagazines);
            preparedStatementMagazines.setInt(1, mainInstance.getLoggedInUserId());

            ResultSet resultSetMagazines = preparedStatementMagazines.executeQuery();

            while (resultSetMagazines.next()) {
                LocalDateTime startDateTime = resultSetMagazines.getTimestamp("start_date").toLocalDateTime();
                String startDateFormatted = startDateTime.toLocalDate().format(dateFormatter) + " " + startDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES).format(timeFormatter);

                LocalDateTime endDateTime = resultSetMagazines.getTimestamp("end_date").toLocalDateTime();
                String endDateFormatted = endDateTime.toLocalDate().format(dateFormatter) + " " + endDateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES).format(timeFormatter);

                String loanDetails = "Magasin: " + resultSetMagazines.getString("item_title") +
                        ", Lånedatum: " + startDateFormatted +
                        ", Återlämnad: " + endDateFormatted +
                        ", ID: " + resultSetMagazines.getString("id");
                items.add(loanDetails);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
        }

        loanListView.setItems(items);

        Button closeButton = new Button("Återgå");
        closeButton.setOnAction(e -> dialog.close());

        dialogVBox.getChildren().addAll(loanListView, closeButton);

        Scene dialogScene = new Scene(dialogVBox, 800, 600);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void updateProfile(Connection connection) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Namn", "Namn", "Lösenord", "E-post");
        dialog.setTitle("Uppdatera din profil");
        dialog.setHeaderText("Uppdatera din profil");
        dialog.setContentText("Välj vad du vill uppdatera:");

        dialog.showAndWait().ifPresent(choice -> {
            switch (choice) {
                case "Namn":
                    updateName(connection);
                    break;
                case "Lösenord":
                    updatePassword(connection);
                    break;
                case "E-post":
                    updateEmail(connection);
                    break;
                default:
                    showAlert(Alert.AlertType.INFORMATION, "Avbruten", "Ingen uppdatering gjordes.");
                    break;
            }
        });
    }

    private void updateName(Connection connection) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Uppdatera namn");
        dialog.setHeaderText("Uppdatera namn");
        dialog.setContentText("Ange ditt nya namn:");

        dialog.showAndWait().ifPresent(newName -> {
            try {
                PreparedStatement updateName = connection.prepareStatement("UPDATE users SET name = ? WHERE id = ?");
                updateName.setString(1, newName);
                updateName.setInt(2, mainInstance.getLoggedInUserId());

                int rowsAffected = updateName.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Namnet har uppdaterats.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Tyvärr, något gick fel vid uppdatering av namnet.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
            }
        });
    }

    private void updatePassword(Connection connection) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Uppdatera lösenord");
        dialog.setHeaderText("Uppdatera lösenord");
        dialog.setContentText("Ange det nya lösenordet:");

        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                PreparedStatement updatePassword = connection.prepareStatement("UPDATE users SET hashed_password = ? WHERE id = ?");
                updatePassword.setString(1, newPassword);
                updatePassword.setInt(2, mainInstance.getLoggedInUserId());

                int rowsAffected = updatePassword.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Lösenordet har uppdaterats.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Tyvärr, något gick fel vid uppdatering av lösenordet.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
            }
        });
    }

    private void updateEmail(Connection connection) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Uppdatera e-post");
        dialog.setHeaderText("Uppdatera e-post");
        dialog.setContentText("Ange din nya e-post:");

        dialog.showAndWait().ifPresent(newEmail -> {
            try {
                PreparedStatement updateEmail = connection.prepareStatement("UPDATE users SET email = ? WHERE id = ?");
                updateEmail.setString(1, newEmail);
                updateEmail.setInt(2, mainInstance.getLoggedInUserId());

                int rowsAffected = updateEmail.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "E-postadressen har uppdaterats.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Tyvärr, något gick fel vid uppdatering av e-postadressen.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "SQL Error", e.getMessage());
            }
        });
    }

    private void logOut() {
        mainInstance.setLoggedInUserId(-1);
        showAlert(Alert.AlertType.INFORMATION, "Utloggad", "Du har loggats ut.");
    }
}
