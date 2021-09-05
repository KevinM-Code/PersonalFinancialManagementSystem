/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import toolkit.DatabaseController;
import toolkit.SecurityController;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * This class enacts all the events involved with the sign-in window.
 *
 * @author Kevin Mock
 */
public class SignInController implements Initializable {

    @FXML
    private JFXTextField email;
    @FXML
    private JFXPasswordField password;
    @FXML
    private Button signInButton;
    @FXML
    private Label signInErrLabel;

    private String userFName = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * This method will open the sign up window for another user to sign up only
     * when the correct credentials are verified from the original (or admin)
     * user.
     *
     * @param event
     */
    @FXML
    void newUserSignUp(ActionEvent event) {

        try {
            SecurityController.decryptDatabase();
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(SignInController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        String passwordFromForm, userPassword, userSalt, key, iv = "";
        String passwordString = "";

        try (Connection conn = DatabaseController.dBConnector()) {

            String isUserQuery = "SELECT password, salt FROM users where email LIKE '" + email.getText() + "'";
            PreparedStatement isUserPrepStmt = conn.prepareStatement(isUserQuery);
            ResultSet isUserResultSet = isUserPrepStmt.executeQuery();

            userPassword = isUserResultSet.getString("password");
            userSalt = isUserResultSet.getString("salt");

            key = userSalt.substring(0, Math.min(userSalt.length(), 16));
            iv = userSalt.substring(16, Math.min(userSalt.length(), 32));
            passwordString = SecurityController.decrypt(key, iv, userPassword);

        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(SignInController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        passwordFromForm = password.getText();

        if (passwordFromForm.equals(passwordString)) {
            Stage stage = (Stage) signInButton.getScene().getWindow();
            stage.close();

            Stage signUpStage = new Stage();
            Parent signUp;
            try {
                signUp = FXMLLoader.load(getClass().getResource("/view/fxml/SignUp.fxml"));
                Scene loginScene = new Scene(signUp);
                signUpStage.setTitle("Signup Form");
                signUpStage.setScene(loginScene);
                signUpStage.setResizable(false);
                signUpStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        if (!SecurityController.DB_ZIP_FILE.exists() && SecurityController.DEFAULT_DB.exists()) {
                            SecurityController.windowCloseEvent();
                        }
                    }
                });
                signUpStage.initModality(Modality.WINDOW_MODAL);
                signUpStage.show();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            signInErrLabel.setText("Wrong Email/Password Combination");

            Date curDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd, yyyy  hh:mm:ss a z");
            String now = sdf.format(curDate);

            String nextLog = " \r\n" + now + " - Wrong Email/Password Combination attempt on email " + email.getText();

            if (!SecurityController.LOG_FILE.exists()) {
                try {
                    SecurityController.LOG_FILE.createNewFile();
                    nextLog = now + " - Wrong Email/Password Combination attempt on email " + email.getText() + "\n";
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            FileWriter fw;
            try {
                fw = new FileWriter(SecurityController.PATH_TO_LOG_FILE, true); //the true will append the new data
                fw.write(nextLog + "\n");//appends the string to the file
                fw.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, null, ex);
            }

            SecurityController.encyptDatabase();
        }
    }

    /**
     * This method will check to see if the credentials are correct from the
     * email and password entered to the ones stored in the database if they are
     * not correct a message will be displayed and a timestamped record of the
     * attempt will go in the log file with the email that was attempted. If
     * they are correct the Main UI will load.
     *
     * @param event
     */
    @FXML
    void signInButtonAction(ActionEvent event) {

        try {
            SecurityController.decryptDatabase();
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(SignInController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        String passwordFromForm, userPassword, userSalt;
        String userEmail = "";
        String key = "";
        String iv = "";
        String userLName = "";

        String passwordString = "";

        String url = "jdbc:sqlite:" + SecurityController.PATH_TO_DEFAULT_DB;
        try (Connection conn = DatabaseController.dBConnector()) {
            if (conn != null) {
                String isUserQuery = "SELECT * FROM users where email LIKE '" + email.getText() + "'";
                PreparedStatement isUserPrepStmt = conn.prepareStatement(isUserQuery);
                ResultSet isUserResultSet = isUserPrepStmt.executeQuery();

                userEmail = isUserResultSet.getString("email");
                userPassword = isUserResultSet.getString("password");
                userSalt = isUserResultSet.getString("salt");
                userFName = isUserResultSet.getString("first_name");
                userLName = isUserResultSet.getString("last_name");

                key = userSalt.substring(0, Math.min(userSalt.length(), 16));
                iv = userSalt.substring(16, Math.min(userSalt.length(), 32));
                passwordString = SecurityController.decrypt(key, iv, userPassword);
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(SignInController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        passwordFromForm = password.getText();

        if (passwordFromForm.equals(passwordString)) {

            String lastNameString = SecurityController.decrypt(key, iv, userLName);

            Stage stage = (Stage) signInButton.getScene().getWindow();
            stage.close();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/fxml/MainGUI.fxml"));

            try {
                loader.load();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SignInController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

            MainGUIController display = loader.getController();
            display.setText("Hello, " + userFName + " Welcome Back!!");
            display.setEmail(userEmail);
            display.setCurrentUserFName(userFName);
            display.setCurrentUserLName(lastNameString);

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
            primaryStage.getIcons().add(new Image(SignInController.class.getResourceAsStream("/view/img/Ledger.png")));
            primaryStage.setScene(new Scene(main));
            primaryStage.show();

        } else {
            signInErrLabel.setText("Wrong Email/Password Combination");

            Date curDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd, yyyy  hh:mm:ss a z");
            String now = sdf.format(curDate);

            String nextLog = " \r\n" + now + " - Wrong Email/Password Combination attempt on email " + email.getText();

            if (!SecurityController.LOG_FILE.exists()) {
                try {
                    SecurityController.LOG_FILE.createNewFile();
                    nextLog = now + " - Wrong Email/Password Combination attempt on email " + email.getText() + "\n";
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            FileWriter fw;
            try {
                fw = new FileWriter(SecurityController.PATH_TO_LOG_FILE, true); //the true will append the new data
                fw.write(nextLog + "\n");//appends the string to the file
                fw.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(SignInController.class.getName()).log(Level.SEVERE, null, ex);
            }

            SecurityController.encyptDatabase();
        }
    }

}
