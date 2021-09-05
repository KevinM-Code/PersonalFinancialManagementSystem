/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.FormValidation;
import toolkit.DatabaseController;
import toolkit.SecurityController;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * This class enacts all the events involved with the sign-up window.
 *
 * @author Kevin Mock
 */
public class SignUpController implements Initializable {

    @FXML
    private ResourceBundle resources; // ResourceBundle that was given to the FXMLLoader
    @FXML
    private URL location; // URL location of the FXML file that was given to the FXMLLoader
    @FXML
    private JFXTextField first_name; // fx:id="first_name" Value injected by FXMLLoader
    @FXML
    private JFXTextField last_name; // Value injected by FXMLLoader - fx:id="last_name"
    @FXML
    private JFXTextField email; // Value injected by FXMLLoader - fx:id="email"
    @FXML
    private JFXTextField address; // Value injected by FXMLLoader - fx:id="address"
    @FXML
    private JFXTextField city; // Value injected by FXMLLoader - fx:id="city"
    @FXML
    private JFXTextField state; // Value injected by FXMLLoader - fx:id="state"
    @FXML
    private JFXTextField zip; // Value injected by FXMLLoader - fx:id="zip"
    @FXML
    private JFXTextField telephone; // Value injected by FXMLLoader - fx:id="telephone"
    @FXML
    private JFXPasswordField password; // Value injected by FXMLLoader - fx:id="password"    
    @FXML
    private JFXPasswordField confirm_password;

    @FXML
    private Label fnameLabel;
    @FXML
    private Label lnameLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label stateLabel;
    @FXML
    private Label zipLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private Label confirmPasswordLabel;
    @FXML
    private Button signUpButton;

    private String userEmail = "";

    private boolean isEmailNotDuplicate = false;

    /**
     * When this method is called it will verify that all the fields are filled
     * out. When all the field are filled out it will proceed to find out if the
     * email entered by the new user is in the database, if so a message will
     * say so. If all is correct it will create all the necessary tables in the
     * database, insert the users information into the database and load the
     * Main UI. In the Main UI a welcome message will appear with the first name
     * of the current user.
     *
     * @param event
     */
    @FXML
    void signUpButtonAction(ActionEvent event) {

        // FORM VALIDATION        
        boolean fnameBool = FormValidation.textFieldNotEmpty(first_name, fnameLabel, "First Name is Required");
        boolean lnameBool = FormValidation.textFieldNotEmpty(last_name, lnameLabel, "Last Name is Required");
        boolean addressBool = FormValidation.textFieldNotEmpty(address, addressLabel, "Address is Required");
        boolean cityBool = FormValidation.textFieldNotEmpty(city, cityLabel, "City Name is Required");
        boolean stateBool = FormValidation.textFieldNotEmpty(state, stateLabel, "State is Required");
        boolean zipBool = FormValidation.textFieldNotEmpty(zip, zipLabel, "Zip is Required");
        boolean phoneBool = FormValidation.textFieldNotEmpty(telephone, phoneLabel, "Phone Number is Required");
        boolean emailBool = FormValidation.textFieldNotEmpty(email, emailLabel, "Email is Required");

        boolean passwordBool = FormValidation.passwordFieldsNotEqual(password, confirm_password, passwordLabel, "Passwords do not match or are empty");
        boolean confPassBool = FormValidation.passwordFieldNotEmpty(confirm_password, confirmPasswordLabel, "Please Confirm Password");

        if (fnameBool && lnameBool && addressBool && cityBool && stateBool && zipBool && phoneBool && emailBool && passwordBool && confPassBool) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("License Information Disclaimer");
            alert.setHeaderText("Please, agree to the terms in the GPLv3 License");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(
                    new Image(this.getClass().getResource("/view/img/gplv3.png").toString()));

            Label label = new Label("\n"
                    + "This program is free software; you can redistribute it and/or modify it under the terms of the\n"
                    + "GNU General Public License version 3 as published by the Free Software Foundation.\n"
                    + "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;\n"
                    + "without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n"
                    + "By choosing accept, you understand and agree to the terms in the General Public License Version 3.\n");

            label.setFont(new Font("Times New Roman", 15));

            Desktop d = Desktop.getDesktop();

            Hyperlink link = new Hyperlink();
            link.setText("Link to GNU GENERAL PUBLIC LICENSE Version 3");
            link.setFont(new Font("Times New Roman", 15));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    try {
                        d.browse(new URI("https://www.gnu.org/licenses/gpl-3.0.html"));
                    } catch (URISyntaxException | IOException ex) {
                        java.util.logging.Logger.getLogger(AboutController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(link, 0, 1);

            alert.getDialogPane().setContent(expContent);

            ButtonType decline = new ButtonType("Decline", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType accept = new ButtonType("Accept");
            alert.getButtonTypes().setAll(accept, decline);
            Optional<ButtonType> action = alert.showAndWait();

            if (action.get() == accept) {
                if (!SecurityController.DEFAULT_DB.exists()) {

                    File mkDir = new File(SecurityController.PATH_TO_DEFAULT_FOLDER);
                    mkDir.mkdir();
                    File mkDataDir = null;
                    if (System.getProperty("os.name").equals("Windows 10")) {
                        mkDataDir = new File(SecurityController.PATH_TO_DECRYPT_KEY_FOLDER);
                    }   
                    if (System.getProperty("os.name").equals("Linux")) {
                        mkDataDir = new File("." + SecurityController.PATH_TO_DECRYPT_KEY_FOLDER);
                    }    
                    
                    if (!mkDataDir.exists()) {
                        mkDataDir.mkdir();
                    }
                    if (System.getProperty("os.name").equals("Windows 10")) {
                        try {
                            Runtime.getRuntime().exec("attrib +H " + SecurityController.PATH_TO_DECRYPT_KEY_FOLDER);
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(SecurityController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                    }                    

                    String createUserTableSql = "CREATE TABLE IF NOT EXISTS users (\n"
                            + " user_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
                            + " first_name TEXT,\n"
                            + " last_name TEXT,\n"
                            + " street TEXT,\n"
                            + " city TEXT,\n"
                            + " state TEXT NOT NULL,\n"
                            + " zip TEXT,\n"
                            + " telephone TEXT,\n"
                            + " email TEXT NOT NULL UNIQUE,\n"
                            + " password TEXT NOT NULL,\n"
                            + " salt TEXT NOT NULL\n"
                            + ");";

                    String createCategoriesTableSql = "CREATE TABLE IF NOT EXISTS categories (\n"
                            + " category_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
                            + " user_id INTEGER,\n"
                            + " cat_name TEXT,\n"
                            + " FOREIGN KEY(user_id) REFERENCES users(user_id)\n"
                            + ");";

                    String createAccountsTableSql = "CREATE TABLE IF NOT EXISTS accounts (\n"
                            + " account_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
                            + " user_id INTEGER,\n"
                            + " date_added TEXT,\n"
                            + " account_number INTEGER,\n"
                            + " institution TEXT,\n"
                            + " account_type TEXT,\n"
                            + " account_subtype TEXT,\n"
                            + " balance REAL DEFAULT 0,\n"
                            + " total_payments REAL DEFAULT 0,\n"
                            + " acc_cat_id INTEGER,\n"
                            + " FOREIGN KEY(user_id) REFERENCES users(user_id),\n"
                            + " FOREIGN KEY(acc_cat_id) REFERENCES categories(category_id)\n"
                            + ");";

                    String createAccountInflowTransactionsTableSql = "CREATE TABLE IF NOT EXISTS account_inflow_transactions (\n"
                            + " inflow_transaction_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
                            + " user_id INTEGER,\n"
                            + " account_id INTEGER,\n"
                            + " inflow_trans_date INTEGER,\n"
                            + " inflow_trans_description TEXT,\n"
                            + " inflow_trans_account_from TEXT,\n"
                            + " inflow_trans_account_to TEXT,\n"
                            + " inflow_trans_amount REAL DEFAULT 0,\n"
                            + " FOREIGN KEY(account_id) REFERENCES accounts(account_id),\n"
                            + " FOREIGN KEY(user_id) REFERENCES users(user_id)\n"
                            + ");";

                    String createAccountOutflowTransactionsTableSql = "CREATE TABLE IF NOT EXISTS account_outflow_transactions (\n"
                            + " outflow_transaction_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n"
                            + " user_id INTEGER,\n"
                            + " account_id INTEGER,\n"
                            + " outflow_trans_date TEXT,\n"
                            + " outflow_trans_description TEXT,\n"
                            + " outflow_trans_account_from TEXT,\n"
                            + " outflow_trans_account_to TEXT,\n"
                            + " outflow_trans_amount REAL DEFAULT 0,\n"
                            + " FOREIGN KEY(account_id) REFERENCES accounts(account_id),\n"
                            + " FOREIGN KEY(user_id) REFERENCES users(user_id)\n"
                            + ");";

                    DatabaseController.createNewTable(createUserTableSql);
                    DatabaseController.createNewTable(createCategoriesTableSql);
                    DatabaseController.createNewTable(createAccountsTableSql);
                    DatabaseController.createNewTable(createAccountInflowTransactionsTableSql);
                    DatabaseController.createNewTable(createAccountOutflowTransactionsTableSql);
                }

                String enteredFirstName = first_name.getText();
                String enteredLastName = last_name.getText();
                String enteredStreet = address.getText();
                String enteredCity = city.getText();
                String enteredState = state.getText();
                String enteredZip = zip.getText();
                String enteredTelephone = telephone.getText();
                String enteredEMail = email.getText();
                String enteredPassword = password.getText();

                try (Connection con = DatabaseController.dBConnector()) {
                    if (con != null) {
                        String isUserQuery = "SELECT email FROM users where email LIKE '" + enteredEMail + "'";
                        PreparedStatement isUserPrepStmt = con.prepareStatement(isUserQuery);
                        ResultSet isUserResultSet = isUserPrepStmt.executeQuery();

                        if (isUserResultSet.next()) {
                            userEmail = isUserResultSet.getString("email");
                        }
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(SignUpController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                if (userEmail != null) {
                    isEmailNotDuplicate = FormValidation.emailNotSame(userEmail, enteredEMail, emailLabel, "Email already in use");
                }

                if (isEmailNotDuplicate) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {

                            String insertQuery = "INSERT INTO users (first_name, last_name, street, city, state, zip, telephone, email, password, salt) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                            PreparedStatement insertPrepStmt = conn.prepareStatement(insertQuery);

                            String iv = SecurityController.generateString(16);
                            String key = SecurityController.generateString(16);
                            String salt = key + iv;
                            String encryptPassword = SecurityController.encrypt(key, iv, enteredPassword);
                            String encryptCity = SecurityController.encrypt(key, iv, enteredCity);
                            String encryptStreet = SecurityController.encrypt(key, iv, enteredStreet);
                            String encryptZip = SecurityController.encrypt(key, iv, enteredZip);
                            String encryptTelephone = SecurityController.encrypt(key, iv, enteredTelephone);
                            String encryptLastName = SecurityController.encrypt(key, iv, enteredLastName);
                            //Parameters
                            insertPrepStmt.setString(1, enteredFirstName);
                            insertPrepStmt.setString(2, encryptLastName);
                            insertPrepStmt.setString(3, encryptStreet);
                            insertPrepStmt.setString(4, encryptCity);
                            insertPrepStmt.setString(5, enteredState);
                            insertPrepStmt.setString(6, encryptZip);
                            insertPrepStmt.setString(7, encryptTelephone);
                            insertPrepStmt.setString(8, enteredEMail);
                            insertPrepStmt.setString(9, encryptPassword);
                            insertPrepStmt.setString(10, salt);

                            insertPrepStmt.executeUpdate();
                            conn.close();
                        }

                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(SignUpController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }

                    Stage stage = (Stage) signUpButton.getScene().getWindow();

                    stage.close();

                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/view/fxml/MainGUI.fxml"));
                    try {
                        loader.load();
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(SignUpController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }

                    MainGUIController display = loader.getController();
                    display.setText("Hello, " + enteredFirstName + "... Welcome!!");
                    display.setEmail(email.getText());
                    display.setCurrentUserFName(enteredFirstName);
                    display.setCurrentUserLName(enteredLastName);

                    Parent main = loader.getRoot();
                    Stage primaryStage = new Stage();

                    primaryStage.setTitle("Personal Financial Management System");
                    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            if (!SecurityController.DB_ZIP_FILE.exists() && SecurityController.DEFAULT_DB.exists()) {
                                SecurityController.windowCloseEvent();
                            }
                        }
                    });
                    primaryStage.getIcons().add(new Image(SignUpController.class.getResourceAsStream("/view/img/Ledger.png")));
                    primaryStage.setScene(new Scene(main));
                    primaryStage.show();
                }
            } else {
                Platform.exit();
            }

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
