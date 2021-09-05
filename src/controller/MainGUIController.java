/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.AccountByCategoryModel;
import model.AccountTableModel;
import toolkit.DatabaseController;
import toolkit.SecurityController;
import com.jfoenix.controls.JFXTabPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class runs all the events needed to prepare the controls and TableViews
 * for each tab as each tab is clicked.
 *
 * @author Kevin Mock
 */
public class MainGUIController implements Initializable {

    @FXML
    private Tab categoriesTabMain;
    @FXML
    private Tab ledgerTabMain;
    @FXML
    private MenuItem helpMenuItem;
    @FXML
    private Tab reportsTabMain;
    @FXML
    private Label name;
    @FXML
    private MenuBar menuBar;
    @FXML
    private JFXTabPane mainMenuTabs;
    @FXML
    private LedgerTabController ledgerTabController;
    @FXML
    private AccountsTabController accountsTabController;
    @FXML
    private CategoriesTabController categoriesTabController;

    public static String email;
    private static String currentUserId = null;
    private static String currentUserFName = null;
    private static String currentUserLName = null;

    static void setCurrentUserId(String currentUserId) {
        MainGUIController.currentUserId = currentUserId;
    }

    static String getCurrentUserId() {
        return currentUserId;
    }

    static String getCurrentUserFName() {
        return currentUserFName;
    }

    static String getCurrentUserLName() {
        return currentUserLName;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ledgerTabController.injectMainController(this);
        accountsTabController.injectMainController(this);
        categoriesTabController.injectMainController(this);
        categoriesTabMain.setDisable(true);
        reportsTabMain.setDisable(true);
        ledgerTabMain.setDisable(true);
    }

    Tab getCategoriesTabMain() {
        return categoriesTabMain;
    }

    Tab getLedgerTabMain() {
        return ledgerTabMain;
    }

    Tab getReportsTabMain() {
        return reportsTabMain;
    }

    /**
     * Show the window of failed login attempts on the current users account
     *
     * @param event
     * @throws FileNotFoundException
     */
    @FXML
    private void showFailedLoginAttemptsWindowOnAction(ActionEvent event) throws FileNotFoundException {
        SecurityController sc = new SecurityController();
        sc.failedLoginAttemptsDialog();
    }

    /**
     * Show the about window.
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void showAboutWindowOnAction(ActionEvent event) throws IOException {
        Parent helpWindow = FXMLLoader.load(getClass().getResource("/view/fxml/About.fxml"));
        Scene aboutScene = new Scene(helpWindow);
        Stage mainWindow = (Stage) menuBar.getScene().getWindow();
        Stage aboutStage = new Stage();
        aboutStage.setTitle("About Window");
        aboutStage.setScene(aboutScene);
        aboutStage.setResizable(false);
        aboutStage.initModality(Modality.WINDOW_MODAL);
        aboutStage.initOwner(mainWindow);
        aboutStage.show();
    }

    /**
     * Show the help window
     *
     * @param event
     * @throws IOException
     */
    @FXML
    private void viewHelpOnMenuItemAction(ActionEvent event) throws IOException {

        Parent helpWindow = FXMLLoader.load(getClass().getResource("/view/fxml/Help.fxml"));
        Scene helpScene = new Scene(helpWindow);
        Stage mainWindow = (Stage) menuBar.getScene().getWindow();
        Stage helpStage = new Stage();
        helpStage.setTitle("Help Window");
        helpStage.setScene(helpScene);
        helpStage.setResizable(false);
        helpStage.initModality(Modality.WINDOW_MODAL);
        helpStage.initOwner(mainWindow);
        helpStage.show();

    }

    /**
     * This method will listen to the tabs for a click, so when the tab is
     * selected it will enact different actions to prepare the tab for use.
     * Whether it is filling a ComboBox with the latest accounts or displaying
     * the latest information into a TableView or TreeTable View.
     *
     * @param event
     */
    @FXML
    private void onMouseClicked(MouseEvent event) {

        final int ACCOUNTS_TAB = 0;
        final int CATEGORIES_TAB = 1;
        final int LEDGER_TAB = 2;

        /*
         * ************************* LEDGER TAB ***********************
         */
        String delimiter = "-";
        if (mainMenuTabs.getSelectionModel().isSelected(LEDGER_TAB)) {

            /* Make sure the new and edit transactions tabs start on the new tab when the user selects the Ledger Tab*/
            ledgerTabController.getTransactionOptionsTabPane().getSelectionModel().select(ledgerTabController.getNewTransactionTab());

            /* Fill the account names observable list with updated information that might
            have changed when the user was on the accounts tab */
            try (Connection conn = DatabaseController.dBConnector()) {
                String currentUserAccountNamesQuery = "SELECT institution, account_type FROM accounts WHERE user_id = " + currentUserId;
                PreparedStatement currentUserAccountNamesPrepStmt = conn.prepareStatement(currentUserAccountNamesQuery);
                ResultSet currentUserAccountNamesResultSet = currentUserAccountNamesPrepStmt.executeQuery();

                if (!LedgerTabController.accountNamesObservableList.isEmpty()) {
                    LedgerTabController.accountNamesObservableList.clear();
                }

                if (!LedgerTabController.toAccountNamesObservableList.isEmpty()) {
                    LedgerTabController.toAccountNamesObservableList.clear();
                }

                while (currentUserAccountNamesResultSet.next()) {
                    /* Add all accounts to the accountNamesObservableList*/
                    if (!currentUserAccountNamesResultSet.getString("account_type").equals("Bill Default Account")) {
                        LedgerTabController.accountNamesObservableList.add(currentUserAccountNamesResultSet.getString("institution") + delimiter + currentUserAccountNamesResultSet.getString("account_type"));
                    }

                    /* Add to the "to" account ComboBox accounts that are not Deafult Accounts*/
                    if (!currentUserAccountNamesResultSet.getString("account_type").equals("Default Account")) {
                        LedgerTabController.toAccountNamesObservableList.add(currentUserAccountNamesResultSet.getString("institution") + delimiter + currentUserAccountNamesResultSet.getString("account_type"));
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(MainGUIController.class.getName()).log(Level.SEVERE, null, e);
            }

            /* Reset the Controls */
            ledgerTabController.getAccountsLedgerComboBox().setItems(LedgerTabController.accountNamesObservableList);
            ledgerTabController.getNewTransAccFromLabel().setText(ledgerTabController.getAccountsLedgerComboBox().getValue());
            ledgerTabController.getNewTransAccToComboBox().setItems(LedgerTabController.toAccountNamesObservableList);
            ledgerTabController.getAccountsLedgerComboBox().setValue(LedgerTabController.accountNamesObservableList.get(0));
            ledgerTabController.getNewTransAccToComboBox().setValue(LedgerTabController.toAccountNamesObservableList.get(0));
        }

        /*
         * ************************* CATEGORIES TAB ***********************
         */
        if (mainMenuTabs.getSelectionModel().isSelected(CATEGORIES_TAB)) {

            /* Associate an integer that will signify the index position in the array of categories */
            Map<String, Integer> count = new HashMap<>();

            categoriesTabController.getAccByCat().setCellValueFactory(new TreeItemPropertyValueFactory<AccountByCategoryModel, String>("account"));
            categoriesTabController.getAccountBal().setCellValueFactory(new TreeItemPropertyValueFactory<AccountByCategoryModel, String>("accountTotal"));

            if (!CategoriesTabController.root.getChildren().isEmpty()) {
                CategoriesTabController.root.getChildren().clear();
            }

            /* Get all the category names*/
            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {
                    String currentUserAccountNamesQuery = "SELECT cat_name FROM categories WHERE user_id = " + MainGUIController.getCurrentUserId();
                    PreparedStatement currentUserAccountNamesPrepStmt = conn.prepareStatement(currentUserAccountNamesQuery);
                    ResultSet currentUserAccountNamesResultSet = currentUserAccountNamesPrepStmt.executeQuery();
                    int i = 0;

                    if (!CategoriesTabController.accByCatModTblObservableList.isEmpty()) {
                        CategoriesTabController.accByCatModTblObservableList.clear();
                    }

                    while (currentUserAccountNamesResultSet.next()) {
                        /* Add all the categories as children of the root*/
                        CategoriesTabController.accByCatModTblObservableList.add(
                                new TreeItem<>(new AccountByCategoryModel(
                                        currentUserAccountNamesResultSet.getString("cat_name"), "")));

                        CategoriesTabController.root.getChildren().add(CategoriesTabController.accByCatModTblObservableList.get(i));
                        count.put(currentUserAccountNamesResultSet.getString("cat_name"), i);
                        i++;
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(MainGUIController.class.getName()).log(Level.SEVERE, null, e);
            }

            String accountQuery = "SELECT institution, account_type, cat_name, balance, categories.category_id as cat_cat_id, accounts.acc_cat_id as acc_cat_id "
                    + "FROM accounts INNER JOIN categories ON accounts.user_id = categories.user_id "
                    + "WHERE accounts.acc_cat_id = categories.category_id AND accounts.user_id = " + MainGUIController.getCurrentUserId()
                    + " ORDER BY categories.category_id;";
            String category;
            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(accountQuery);

                    while (rs.next()) {
                        category = rs.getString("cat_name");
                        /* Get the category index from the category name and assign the account associated with it */
                        CategoriesTabController.accByCatModTblObservableList.get(count.get(category)).getChildren().add(new TreeItem<>(new AccountByCategoryModel(
                                rs.getString("institution") + "-" + rs.getString("account_type"),
                                rs.getString("balance")
                        )));
                    }
                }

            } catch (Exception e) {
                Logger.getLogger(MainGUIController.class.getName()).log(Level.SEVERE, null, e);
            }

            categoriesTabController.getCategoriesTreeTableView().setRoot(CategoriesTabController.root);
            categoriesTabController.getCategoriesTreeTableView().setShowRoot(false);

            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {
                    String currentUserAccountNamesQuery = "SELECT institution, account_type FROM accounts WHERE user_id = " + currentUserId;
                    PreparedStatement prepStmt = conn.prepareStatement(currentUserAccountNamesQuery);
                    ResultSet resultSet = prepStmt.executeQuery();

                    if (!LedgerTabController.accountNamesObservableList.isEmpty()) {
                        LedgerTabController.accountNamesObservableList.clear();
                    }

                    while (resultSet.next()) {
                        /* Populate the accountNamesObservable List */
                        LedgerTabController.accountNamesObservableList.add(resultSet.getString("institution") + delimiter + resultSet.getString("account_type"));
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(MainGUIController.class.getName()).log(Level.SEVERE, null, e);
            }

            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {
                    String currentUserAccountNamesQuery = "SELECT cat_name FROM categories WHERE user_id = " + currentUserId;
                    PreparedStatement prepStmt = conn.prepareStatement(currentUserAccountNamesQuery);
                    ResultSet resultSet = prepStmt.executeQuery();

                    if (!CategoriesTabController.categoriesObservableList.isEmpty()) {
                        CategoriesTabController.categoriesObservableList.clear();
                    }

                    while (resultSet.next()) {
                        /* Populate the categoriesObservableList */
                        CategoriesTabController.categoriesObservableList.add(resultSet.getString("cat_name"));
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(MainGUIController.class.getName()).log(Level.SEVERE, null, e);
            }

            /* Expand all nodes */
            CategoriesTabController.expandTreeView(CategoriesTabController.root);

            /* Set up the ComboBoxes */
            categoriesTabController.getEditCatNameTextField().setText("");
            categoriesTabController.getAccountSelectedLabel().setText("");
            categoriesTabController.getNewAccAssignToCatComboBox().setItems(LedgerTabController.accountNamesObservableList);
            categoriesTabController.getNewAccAssignToCatComboBox().setValue(LedgerTabController.accountNamesObservableList.get(0));
            categoriesTabController.getEditCatAssignToAccComboBox().setItems(CategoriesTabController.categoriesObservableList);
            categoriesTabController.getEditCatAssignToAccComboBox().setValue("(Choose Category)");
            categoriesTabController.getNewAddAccToCatComboBox().setItems(LedgerTabController.accountNamesObservableList);
            categoriesTabController.getNewAddAccToCatComboBox().setValue(LedgerTabController.accountNamesObservableList.get(0));
            if (!CategoriesTabController.root.getChildren().isEmpty()) {
                categoriesTabController.getCategoriesTreeTableView().getSelectionModel().getTreeTableView().getSelectionModel().select(CategoriesTabController.root.getChildren().get(0));
            }
        }

        /*
         * ************************* ACCOUNTS TAB ***********************
         */
        if (mainMenuTabs.getSelectionModel().isSelected(ACCOUNTS_TAB)) {
            accountsTabController.getAccountOptionsTabPane().getSelectionModel().select(accountsTabController.getNewAccountTab());

            if (currentUserId == null) {
                String currentUserIdQuery = "SELECT user_id FROM users WHERE email LIKE '" + email + "'";
                try {
                    currentUserId = DatabaseController.selectDBQueryReturnOneString(currentUserIdQuery, "user_id");
                } catch (SQLException ex) {
                    Logger.getLogger(MainGUIController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            accountsTabController.getDateAdd().setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
            accountsTabController.getAccNum().setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
            accountsTabController.getAccName().setCellValueFactory(new PropertyValueFactory<>("accountInst"));
            accountsTabController.getAccType().setCellValueFactory(new PropertyValueFactory<>("accountType"));
            accountsTabController.getAccSubtype().setCellValueFactory(new PropertyValueFactory<>("accountSubtype"));
            accountsTabController.getBalance().setCellValueFactory(new PropertyValueFactory<>("balance"));

            /* Fill in the Accounts Table */
            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {
                    String accountQuery = "SELECT date_added, account_number, institution, account_type, account_subtype, balance FROM accounts WHERE user_id = " + MainGUIController.currentUserId;
                    PreparedStatement accountPrepStmt = conn.prepareStatement(accountQuery);
                    ResultSet accountResultSet = accountPrepStmt.executeQuery();

                    if (!AccountsTabController.accModelTableObservList.isEmpty()) {
                        AccountsTabController.accModelTableObservList.clear();
                    }

                    while (accountResultSet.next()) {

                        boolean defaultAccount = accountResultSet.getString("account_type").equals("Default Account");
                        boolean defaultBillAccount = accountResultSet.getString("account_type").equals("Bill Default Account");
                        boolean defaultExpenseAccount = accountResultSet.getString("account_type").equals("Expense Default Account");
                        /* Display the account in the Accounts TableView if the account is not a Default Account */
                        if (!defaultAccount && !defaultBillAccount && !defaultExpenseAccount) {
                            AccountsTabController.accModelTableObservList.add(new AccountTableModel(
                                    accountResultSet.getString("date_added"),
                                    accountResultSet.getString("account_number"),
                                    accountResultSet.getString("institution"),
                                    accountResultSet.getString("account_type"),
                                    accountResultSet.getString("account_subtype"),
                                    accountResultSet.getString("balance")
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(MainGUIController.class.getName()).log(Level.SEVERE, null, e);
            }
            accountsTabController.getAccountsTableView().setItems(AccountsTabController.accModelTableObservList);
        }
    }

    /**
     * This accessor method will set the text in the UI based on the current
     * user's first name that is logged in.
     *
     * @param name
     */
    void setText(String name) {
        this.name.setText(name);
    }

    /**
     * This will set the current user's email to a property of the class for use
     * to identify the current user so only their records are accessed.
     *
     * @param email
     */
    void setEmail(String email) {
        this.email = email;
    }

    void setCurrentUserFName(String fName) {
        this.currentUserFName = fName;
    }

    void setCurrentUserLName(String lName) {
        this.currentUserLName = lName;
    }
}
