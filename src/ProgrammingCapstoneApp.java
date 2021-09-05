/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import toolkit.SecurityController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * This is the start of the application.
 *
 * @author Kevin Mock
 */
public class ProgrammingCapstoneApp extends Application {

    /**
     * This is the main method that will evaluate whether an encrypted zip file
     * is there and based on that condition start the sign up form or sign in
     * form. If for some reason the program did not close correctly (power failure to the computer, etc.)
     * the application will look for an exposed database file first, encrypt the db file
     * and proceed normally.
     *
     * @param primaryStage
     * @throws IOException cannot read from the file
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        
        /* In case the program did not close correctly (do to computer power failure or otherwise)
        and the DB file is exposed. This line will encryt the database file so the program can 
        proceed normally */
        if (SecurityController.DEFAULT_DB.exists()) {
            SecurityController.encyptDatabase();
        }

        if (SecurityController.DB_ZIP_FILE.exists()) {
            Stage signInStage = new Stage();
            Parent signIn = FXMLLoader.load(getClass().getResource("/view/fxml/SignIn.fxml"));
            Scene loginScene = new Scene(signIn);
            signInStage.setTitle("Login Form");
            signInStage.setScene(loginScene);
            signInStage.setResizable(false);
            signInStage.setOnCloseRequest((WindowEvent event) -> {
                if (!SecurityController.DB_ZIP_FILE.exists() && SecurityController.DEFAULT_DB.exists()) {
                    SecurityController.windowCloseEvent();
                }
            });
            signInStage.initModality(Modality.WINDOW_MODAL);
            signInStage.show();

        } else {
            Stage signUpStage = new Stage();
            Parent signUp = FXMLLoader.load(getClass().getResource("/view/fxml/SignUp.fxml"));
            Scene loginScene = new Scene(signUp);
            signUpStage.setTitle("Signup Form");
            signUpStage.setScene(loginScene);
            signUpStage.setResizable(false);
            signUpStage.setOnCloseRequest((WindowEvent event) -> {
                if (!SecurityController.DB_ZIP_FILE.exists() && SecurityController.DEFAULT_DB.exists()) {
                    SecurityController.windowCloseEvent();
                }
            });
            signUpStage.initModality(Modality.WINDOW_MODAL);
            signUpStage.show();
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
