/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class displays information about the developer and license.
 *
 * @author Kevin Mock
 */
public class AboutController {

    private final Desktop desktop = Desktop.getDesktop();

    /**
     * Initializes the controller class.
     */
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * When the image of the GNU GPL logo is clicked, the default browser is
     * open and shows the website with the license of this software.
     *
     * @param event
     */
    @FXML
    void displayLicenseOnImgMouseClick(MouseEvent event) {
        try {
            desktop.browse(new URI("https://www.gnu.org/licenses/gpl-3.0.html"));
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(AboutController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * When the GNU GPL link is clicked, the default browser is open and shows
     * the website with the license of this software.
     *
     * @param event 
     */
    @FXML
    void displayLicenseOnTextMouseClick(MouseEvent event) {
        try {
            desktop.browse(new URI("https://www.gnu.org/licenses/gpl-3.0.html"));
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(AboutController.class.getName()).log(Level.SEVERE, null, ex);            
        }
    }
}
