/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class performs all the actions associated with the help window.
 *
 * @author Kevin Mock
 */
public class HelpController implements Initializable {

    @FXML
    private JFXHamburger hamburger;
    @FXML
    private VBox box;
    @FXML
    private JFXDrawer drawer;
    @FXML
    private JFXButton accountsHelpButton;
    @FXML
    private JFXButton catHelpButton;
    @FXML
    private JFXButton ledgerHelpButton;
    @FXML
    private Pane accTabHelpPane;
    @FXML
    private Pane catTabHelpPane;
    @FXML
    private Pane ledgerTabHelpPane;
    @FXML
    private ScrollPane helpScrollPane;

    /**
     * Initializes the HelpController Class. Sets the events for the hamburger drawer toggle and buttons in the drawer
     * that show the instructions for each tab.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        /* Toggle animation */
        HamburgerBackArrowBasicTransition burgerBackArrow = new HamburgerBackArrowBasicTransition(hamburger);

        drawer.setSidePane(box);
        burgerBackArrow.setRate(-1);
        /* Set the drawer to open initially for the user to see */
        drawer.open();

        /* Based on which button the user selects, show the instructions for that main menu tab  */
        box.getChildren().stream().filter((node) -> (node.getAccessibleText() != null)).forEachOrdered((node) -> {
            node.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
                switch (node.getAccessibleText()) {
                    case "accountTabHelpButton":
                        helpScrollPane.setVisible(true);
                        accTabHelpPane.setVisible(true);
                        burgerBackArrow.setRate(burgerBackArrow.getRate() * -1);
                        if (drawer.isOpened()) {
                            drawer.close();
                        }
                        break;
                    case "categoriesTabHelpButton":
                        helpScrollPane.setVisible(true);
                        catTabHelpPane.setVisible(true);
                        burgerBackArrow.setRate(burgerBackArrow.getRate() * -1);
                        if (drawer.isOpened()) {
                            drawer.close();
                        }
                        break;
                    case "ledgerTabHelpButton":
                        helpScrollPane.setVisible(true);
                        ledgerTabHelpPane.setVisible(true);
                        burgerBackArrow.setRate(burgerBackArrow.getRate() * -1);
                        if (drawer.isOpened()) {
                            drawer.close();
                        }
                        break;
                }
            });
        });

        /* Toggle the hamburger */
        hamburger.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            burgerBackArrow.setRate(burgerBackArrow.getRate() * -1);
            burgerBackArrow.play();
            if (drawer.isOpened()) {
                drawer.close();
            }
            /* If the user clicks the hamburger, toggle the drawer open and clear any help panes */
            if (drawer.isClosed()) {
                drawer.open();
                if (accTabHelpPane.isVisible()) {
                    accTabHelpPane.setVisible(false);
                    helpScrollPane.setVisible(false);
                }
                if (catTabHelpPane.isVisible()) {
                    catTabHelpPane.setVisible(false);
                    helpScrollPane.setVisible(false);
                }
                if (ledgerTabHelpPane.isVisible()) {
                    ledgerTabHelpPane.setVisible(false);
                    helpScrollPane.setVisible(false);
                }
            }
        });
    }
}
