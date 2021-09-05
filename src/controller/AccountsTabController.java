/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.AccountTableModel;
import model.FormValidation;
import toolkit.DatabaseController;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class does all the work of adding, editing and deleting the user
 * accounts.
 *
 * @author Kevin Mock
 */
public class AccountsTabController implements Initializable {

    @FXML
    private TableView<AccountTableModel> accountsTableView;
    @FXML
    private TableColumn<AccountTableModel, String> dateAdd;
    @FXML
    private TableColumn<AccountTableModel, String> accNum;
    @FXML
    private TableColumn<AccountTableModel, String> accName;
    @FXML
    private TableColumn<AccountTableModel, String> accType;
    @FXML
    private TableColumn<AccountTableModel, String> accSubtype;
    @FXML
    private TableColumn<AccountTableModel, String> balance;
    @FXML
    private JFXTabPane accountOptionsTabPane;
    @FXML
    private JFXDatePicker newAccDateAddedDatePicker;
    @FXML
    private JFXTextField newAccAccNumberTextField;
    @FXML
    private JFXTextField newAccInstNameTextField;
    @FXML
    private ComboBox<String> accSubtypeComboBox;
    @FXML
    private ComboBox<String> accTypeComboBox;
    @FXML
    private Label errMsgDateAddedLabel;
    @FXML
    private Label errMsgAccNumLabel;
    @FXML
    private JFXTextField newAccOpenBalTextField;
    @FXML
    private Label errMsgInstNameLabel;
    @FXML
    private Tab editAccountTab;
    @FXML
    private Tab newAccountTab;
    @FXML
    private JFXButton addAccountButton;
    @FXML
    private JFXDatePicker editDateAddedDatePicker;
    @FXML
    private JFXTextField editAccNumTextField;
    @FXML
    private JFXTextField editInstNameTextField;
    @FXML
    private ComboBox<String> editAccSubtypeComboBox;
    @FXML
    private ComboBox<String> editAccTypeComboBox;
    @FXML
    private Label editErrMsgDateAddLabel;
    @FXML
    private Label editErrMsgAccNumLabel;
    @FXML
    private Label editErrMsgInstNameLabel;

    /**
     * Collects AccountTableModel objects.
     */
    static ObservableList<AccountTableModel> accModelTableObservList = FXCollections.observableArrayList();
    private final ObservableList<String> accSubtypeObservableList = FXCollections.observableArrayList("Asset", "Liability");
    /**
     * The types of accounts
     */
    private final ObservableList<String> accTypeObservableList = FXCollections.observableArrayList("Checking", "Savings", "Credit Card", "Bill", "Miscellaneous Expense");

    /**
     * Used with dependency injection.
     */
    private MainGUIController mainGUIController;

    /**
     * Dependency injection.
     *
     * @param mainGUIController The
     * {@link AccountsTabController#mainGUIController} field
     */
    void injectMainController(MainGUIController mainGUIController) {
        this.mainGUIController = mainGUIController;
    }

    /* Accessor Methods - These will be need by the MainGUIController for initial tab setup */
    /**
     * Accessor Method
     *
     * @return accountsTableView - the instance of
     */
    TableView<AccountTableModel> getAccountsTableView() {
        return accountsTableView;
    }

    /**
     * Accessor Method
     *
     * @return dateAdd - the instance of
     */
    TableColumn<AccountTableModel, String> getDateAdd() {
        return dateAdd;
    }

    /**
     * Accessor Method
     *
     * @return accNum - the instance of
     */
    TableColumn<AccountTableModel, String> getAccNum() {
        return accNum;
    }

    /**
     * Accessor Method
     *
     * @return accName - the instance of
     */
    TableColumn<AccountTableModel, String> getAccName() {
        return accName;
    }

    /**
     * Accessor Method
     *
     * @return accType - the instance of
     */
    TableColumn<AccountTableModel, String> getAccType() {
        return accType;
    }

    /**
     * Accessor Method
     *
     * @return accSubtype - the instance of
     */
    TableColumn<AccountTableModel, String> getAccSubtype() {
        return accSubtype;
    }

    /**
     * Accessor Method
     *
     * @return balance - the instance of
     */
    TableColumn<AccountTableModel, String> getBalance() {
        return balance;
    }

    /**
     * Accessor Method
     *
     * @return newAccountTab - the instance of
     */
    Tab getNewAccountTab() {
        return newAccountTab;
    }

    /**
     * Accessor Method
     *
     * @return accountOptionsTabPane - the instance of
     */
    JFXTabPane getAccountOptionsTabPane() {
        return accountOptionsTabPane;
    }

    /**
     * Initializes the controller class. This will also load the account type
     * and account subtype ComboBoxes for the new accounts tab and edit accounts
     * tab.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Set the ComboBoxes for the "New Account" Tab
        accSubtypeComboBox.setValue("Asset");
        accSubtypeComboBox.setItems(accSubtypeObservableList);
        accTypeComboBox.setValue("Checking");
        accTypeComboBox.setItems(accTypeObservableList);

        // Set the ComboBoxes for the "Edit Account" Tab
        editAccSubtypeComboBox.setValue("Asset");
        editAccSubtypeComboBox.setItems(accSubtypeObservableList);
        editAccTypeComboBox.setValue("Checking");
        editAccTypeComboBox.setItems(accTypeObservableList);

        newAccOpenBalTextField.setText("0");
        addAccountButton.setDisable(true);
        editAccountTab.setDisable(true);

    }

    /**
     * This is the first action the user will initiate to store the current user
     * id in memory, set up the default accounts, list the accounts onto the
     * TableView from the database if there are any, and enable all button and
     * tabs so the user may begin working with the system.
     *
     * @param event
     */
    @FXML
    private void loadAccountsListOnAction(ActionEvent event) {

        /* No action is allowed by the user until they press the 
           button to load the default accounts and any accounts they have made*/
        if (mainGUIController.getCategoriesTabMain().isDisable()) {
            mainGUIController.getCategoriesTabMain().setDisable(false);
        }
        if (mainGUIController.getLedgerTabMain().isDisable()) {
            mainGUIController.getLedgerTabMain().setDisable(false);
        }
        if (mainGUIController.getReportsTabMain().isDisable()) {
            mainGUIController.getReportsTabMain().setDisable(false);
        }
        if (addAccountButton.isDisable()) {
            addAccountButton.setDisable(false);
        }
        if (editAccountTab.isDisable()) {
            editAccountTab.setDisable(false);
        }

        /* Get the current user id from the valid email they logged in with*/
        if (MainGUIController.getCurrentUserId() == null) {
            getUserID();
        }

        /* Check the database to see if the default accounts have already been added*/
        String accountName = null;
        String currentAccountBalanceForCurrentUserOutflowQuery = "SELECT institution FROM accounts WHERE institution "
                + "LIKE '" + "Salary" + "'" + " AND " + "user_id = " + MainGUIController.getCurrentUserId();

        // Find out if the default accounts are already there for the current user
        try {
            accountName = DatabaseController.selectDBQueryReturnOneString(currentAccountBalanceForCurrentUserOutflowQuery, "institution");
        } catch (SQLException ex) {
            Logger.getLogger(LedgerTabController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (accountName == null) {
            insertDefaultAccount("Salary", "0", "Default Account");
            insertDefaultAccount("Bonus", "1", "Default Account");
            insertDefaultAccount("Investment Income", "2", "Default Account");
            insertDefaultAccount("Bill Amount", "2", "Bill Default Account");
            insertDefaultAccount("Purchase", "2", "Expense Default Account");
        }

        dateAdd.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        accNum.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        accName.setCellValueFactory(new PropertyValueFactory<>("accountInst"));
        accType.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        accSubtype.setCellValueFactory(new PropertyValueFactory<>("accountSubtype"));
        balance.setCellValueFactory(new PropertyValueFactory<>("balance"));

        //colorCodeRows(accSubtype);

        /* Collect all the account for the current user and diplay them in the TableView */
        refreshAccountsTable();
    }

    private void colorCodeRows(TableColumn<AccountTableModel, String> accSubtype) {
        accSubtype.setCellFactory(column -> new TableCell<AccountTableModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty ? "" : getItem());
                setGraphic(null);

                TableRow<AccountTableModel> currentRow = getTableRow();

                if (!isEmpty()) {

                    if (item.equals("Liability")) {
                        currentRow.setStyle("-fx-background-color:lightpink");
                    } else {
                        currentRow.setStyle("-fx-background-color:aquamarine");
                    }
                }
            }
        });
    }

    /**
     * When an account is selected all the associated information will be
     * displayed in the date picker, text fields, and combo boxes.
     *
     * @param event
     */
    @FXML
    private void fillFormOnMousePressed(MouseEvent event) {

        String date = "";
        String accountNum = "";
        String inst = "";
        String accountType = "";
        String accountSubType = "";

        /* These method calls may produce Null Pointer Exceptions when the user clicks on something that
        is not a row in the table, implemented a try-catch block to catch them */
        try {
            date = accountsTableView.getSelectionModel().getSelectedItem().getDateAdded();
            accountNum = accountsTableView.getSelectionModel().getSelectedItem().getAccountNumber();
            inst = accountsTableView.getSelectionModel().getSelectedItem().getAccountInst();
            accountType = accountsTableView.getSelectionModel().getSelectedItem().getAccountType();
            accountSubType = accountsTableView.getSelectionModel().getSelectedItem().getAccountSubtype();
        } catch (NullPointerException npe) {
        }

        if ((!date.equals("")) && (!accountNum.equals(""))) {
            /* If the user is here they successfully clicked on a row in the table */
            accountOptionsTabPane.getSelectionModel().select(editAccountTab);

            editDateAddedDatePicker.getEditor().setText(date);
            editAccNumTextField.setText(accountNum);
            editInstNameTextField.setText(inst);
            editAccSubtypeComboBox.setValue(accountSubType);
            editAccTypeComboBox.setValue(accountType);
        }
    }

    /**
     * This method, will add an account and all the necessary information into
     * the database under the current user's id once at least the date, account
     * number and institution name are entered. The TableView will be refreshed
     * with all the current and new account displayed in the TableView. The
     * controls will be cleared for next entry.
     *
     * @param event
     */
    @FXML
    private void addAccountOnAction(ActionEvent event) {

        /* Checking to see required fields are filled out */
        boolean dateBool = FormValidation.dateFieldNotEmpty(newAccDateAddedDatePicker, errMsgDateAddedLabel, "Date Is Required");
        boolean accNumBool = FormValidation.textFieldNotEmpty(newAccAccNumberTextField, errMsgAccNumLabel, "Some Account # Is Required");
        boolean instNameBool = FormValidation.textFieldNotEmpty(newAccInstNameTextField, errMsgInstNameLabel, "Institution/Name Required");

        String sqlQuery = "SELECT account_number FROM accounts WHERE account_number"
                + " = " + newAccAccNumberTextField.getText() + " AND "
                + "user_id = " + MainGUIController.getCurrentUserId();

        int accountNumberFromDb = 0;

        try {
            accountNumberFromDb = DatabaseController.selectDBQueryReturnOneInt(sqlQuery, "account_number");
        } catch (SQLException ex) {
            Logger.getLogger(AccountsTabController.class.getName()).log(Level.SEVERE, null, ex);
        }

        String accNum = Integer.toString(accountNumberFromDb);

        if (dateBool && accNumBool && instNameBool) {

            if (!accNum.equals(newAccAccNumberTextField.getText())) {
                /* Insert the new account */
                String insertAccountQuery = "INSERT INTO accounts (user_id, date_added, account_number, institution, account_type, account_subtype, balance) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?);";

                String accSubType = accSubtypeComboBox.getSelectionModel().getSelectedItem();
                int openingBalStr = Integer.parseInt(newAccOpenBalTextField.getText());

                String startingBal = accSubType.equals("Liability") ? Integer.toString(openingBalStr *= -1) : Integer.toString(openingBalStr);

                try {
                    DatabaseController.insertSevenColumnsIntoDatabase(insertAccountQuery, MainGUIController.getCurrentUserId(),
                            newAccDateAddedDatePicker.getEditor().getText(), newAccAccNumberTextField.getText(),
                            newAccInstNameTextField.getText(), accTypeComboBox.getSelectionModel().getSelectedItem(),
                            accSubType, startingBal);

                } catch (SQLException ex) {
                    Logger.getLogger(AccountsTabController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

                /* Read from the database and show the new account inserted */
                refreshAccountsTable();

                /* Clear the fields */
                newAccAccNumberTextField.clear();
                newAccInstNameTextField.clear();
                accSubtypeComboBox.setValue("Asset");
                accSubtypeComboBox.setItems(accSubtypeObservableList);
                accTypeComboBox.setValue("Checking");
                accTypeComboBox.setItems(accTypeObservableList);
                newAccOpenBalTextField.setText("0");
            } else {
                errMsgAccNumLabel.setText("Account already exists");
            }
        }
    }

    /**
     * This method will find the account selected in the database and update it
     * with the information that was changed from the fields.
     *
     * @param event
     */
    @FXML
    private void editAccountOnAction(ActionEvent event) {

        /* Checking to see required fields are filled out */
        boolean dateBool = FormValidation.dateFieldNotEmpty(editDateAddedDatePicker, editErrMsgDateAddLabel, "Date Is Required");
        boolean accNumBool = FormValidation.textFieldNotEmpty(editAccNumTextField, editErrMsgAccNumLabel, "Account # Is Required");
        boolean instNameBool = FormValidation.textFieldNotEmpty(editInstNameTextField, editErrMsgInstNameLabel, "Institution/Name Required");

        // Are all the required fields filled out?
        if (dateBool && accNumBool && instNameBool) {

            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {

                    /*
                     * Make changes to all the details of the account except for balance
                     */
                    String editAccountQuery = "UPDATE accounts SET date_added = ?, "
                            + "account_number = ?, "
                            + "institution = ?, "
                            + "account_type = ?, "
                            + "account_subtype = ? "
                            + "WHERE account_number LIKE '" + accountsTableView.getSelectionModel().getSelectedItem().getAccountNumber() + "' AND "
                            + "user_id = '" + MainGUIController.getCurrentUserId() + "'";
                    PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                    //Parameters
                    editAccPrepStmt.setString(1, editDateAddedDatePicker.getEditor().getText());
                    editAccPrepStmt.setString(2, editAccNumTextField.getText());
                    editAccPrepStmt.setString(3, editInstNameTextField.getText());
                    editAccPrepStmt.setString(4, editAccTypeComboBox.getSelectionModel().getSelectedItem());
                    editAccPrepStmt.setString(5, editAccSubtypeComboBox.getSelectionModel().getSelectedItem());

                    editAccPrepStmt.executeUpdate();

                    /* Render the new information in the Accounts table*/
                    refreshAccountsTable();

                    /* Reset the controls */
                    editDateAddedDatePicker.getEditor().clear();
                    editAccNumTextField.clear();
                    editInstNameTextField.clear();
                    editAccSubtypeComboBox.setValue("Asset");
                    editAccSubtypeComboBox.setItems(accSubtypeObservableList);
                    editAccTypeComboBox.setValue("Checking");
                    editAccTypeComboBox.setItems(accTypeObservableList);

                }
            } catch (SQLException ex) {
                Logger.getLogger(AccountsTabController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This method will delete the account and all transactions associated with
     * the account from the inflow and outflow account transactions tables. If
     * there are transactions associated with the account a confirmation message
     * will show to make sure the user would like to proceed.
     *
     * @param event
     */
    @FXML
    private void delSelectedAccountOnAction(ActionEvent event) {

        /*
         * Find if there are any transactions associated with the account that are going to be deleted
         */
        String inflowTransactionsForAccount = null;
        String query = "SELECT inflow_trans_account_to FROM account_inflow_transactions WHERE"
                + " inflow_trans_account_to LIKE '" + accountsTableView.getSelectionModel().getSelectedItem().getAccountInst() + "-"
                + accountsTableView.getSelectionModel().getSelectedItem().getAccountType() + "' AND "
                + "user_id = " + MainGUIController.getCurrentUserId();
        try {
            inflowTransactionsForAccount = DatabaseController.selectDBQueryReturnOneString(query, "inflow_trans_account_to");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String outflowTransactionsForAccount = null;
        query = "SELECT outflow_trans_account_from FROM account_outflow_transactions WHERE"
                + " outflow_trans_account_from LIKE '" + accountsTableView.getSelectionModel().getSelectedItem().getAccountInst() + "-"
                + accountsTableView.getSelectionModel().getSelectedItem().getAccountType() + "' AND "
                + "user_id = " + MainGUIController.getCurrentUserId();
        try {
            outflowTransactionsForAccount = DatabaseController.selectDBQueryReturnOneString(query, "outflow_trans_account_from");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if ((inflowTransactionsForAccount == null) && (outflowTransactionsForAccount == null)) {
            /* There are no transactions associated with the account */
            try {
                deleteAccount();

            } catch (SQLException ex) {
                Logger.getLogger(AccountsTabController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            /* There are transactions associated with the account the user will be given a choice for
            confirmation to delete the account and associated transactions */
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Delete Account Confirmation");
            alert.setHeaderText("Warning!!");
            alert.setContentText("Are you sure you want to delete this account? The account and ALL transactions associated with the account will be deleted!!!");
            Optional<ButtonType> action = alert.showAndWait();

            if (action.get() == ButtonType.OK) {
                try {
                    deleteAccount();

                } catch (SQLException ex) {
                    Logger.getLogger(AccountsTabController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * This is called in
     * {@link AccountsTabController#delSelectedAccountOnAction(ActionEvent)}
     * method to carry out the deletion of the account and all associated
     * transactions.
     *
     * @throws SQLException
     */
    private void deleteAccount() throws SQLException {
        /* Delete the account */
        try (Connection conn = DatabaseController.dBConnector()) {
            if (conn != null) {
                String delAccountQuery = "DELETE FROM accounts "
                        + "WHERE institution LIKE '" + accountsTableView.getSelectionModel().getSelectedItem().getAccountInst() + "' AND "
                        + "account_type LIKE '" + accountsTableView.getSelectionModel().getSelectedItem().getAccountType() + "' AND "
                        + "user_id = " + MainGUIController.getCurrentUserId();
                PreparedStatement delAccPrepStmt = conn.prepareStatement(delAccountQuery);
                delAccPrepStmt.executeUpdate();
            }
        }
        /* Delete the transactions associated with the account in the inflow transactions table */
        try (Connection conn = DatabaseController.dBConnector()) {
            if (conn != null) {

                String delAccountTransQuery = "DELETE FROM account_inflow_transactions "
                        + "WHERE inflow_trans_account_to LIKE '" + accountsTableView.getSelectionModel().getSelectedItem().getAccountInst() + "-"
                        + accountsTableView.getSelectionModel().getSelectedItem().getAccountType() + "' AND "
                        + "user_id = " + MainGUIController.getCurrentUserId();
                PreparedStatement delAccPrepStmt = conn.prepareStatement(delAccountTransQuery);
                delAccPrepStmt.executeUpdate();
            }
        }
        /* Delete the transactions associated with the account in the outflow transactions table */
        try (Connection conn = DatabaseController.dBConnector()) {
            if (conn != null) {
                String delAccountTransQuery = "DELETE FROM account_outflow_transactions "
                        + "WHERE outflow_trans_account_from LIKE '" + accountsTableView.getSelectionModel().getSelectedItem().getAccountInst() + "-"
                        + accountsTableView.getSelectionModel().getSelectedItem().getAccountType() + "' AND "
                        + "user_id = " + MainGUIController.getCurrentUserId();
                PreparedStatement delAccPrepStmt = conn.prepareStatement(delAccountTransQuery);
                delAccPrepStmt.executeUpdate();
                refreshAccountsTable();

                editDateAddedDatePicker.getEditor().clear();
                editAccNumTextField.clear();
                editInstNameTextField.clear();
                editAccSubtypeComboBox.setValue("Asset");
                editAccSubtypeComboBox.setItems(accSubtypeObservableList);
                editAccTypeComboBox.setValue("Checking");
                editAccTypeComboBox.setItems(accTypeObservableList);
            }
        }
    }

    /**
     * This method is used after there were changes made in the database. When
     * called it will render all information from the database including the
     * updates.
     */
    private void refreshAccountsTable() {
        accModelTableObservList.clear();

        try (Connection conn = DatabaseController.dBConnector()) {
            if (conn != null) {
                String accountQuery = "SELECT date_added, account_number, institution, account_type, account_subtype, balance from accounts WHERE user_id = " + MainGUIController.getCurrentUserId();
                PreparedStatement accountPrepStmt = conn.prepareStatement(accountQuery);
                ResultSet accountResultSet = accountPrepStmt.executeQuery();

                while (accountResultSet.next()) {

                    boolean defaultAccount = accountResultSet.getString("account_type").equals("Default Account");
                    boolean defaultBillAccount = accountResultSet.getString("account_type").equals("Bill Default Account");
                    boolean defaultExpenseAccount = accountResultSet.getString("account_type").equals("Expense Default Account");
                    /* Display the account in the Accounts TableView if the account is not a Default Account */
                    if (!defaultAccount && !defaultBillAccount && !defaultExpenseAccount) {
                        accModelTableObservList.add(new AccountTableModel(
                                accountResultSet.getString("date_added"),
                                accountResultSet.getString("account_number"),
                                accountResultSet.getString("institution"),
                                accountResultSet.getString("account_type"),
                                accountResultSet.getString("account_subtype"),
                                accountResultSet.getString("balance")
                        ));
                    }
                }
                /* Insert the accounts in the TableView */

                accountsTableView.setItems(accModelTableObservList);
                colorCodeRows(accSubtype);

            }
        } catch (Exception e) {
            Logger.getLogger(AccountsTabController.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * This method is used to insert the default accounts.
     *
     * @param account The name of the account
     * @param accountNumber The account number
     */
    private void insertDefaultAccount(String account, String accountNumber, String accountType) {

        Date date = new Date();
        long time = date.getTime();
        Timestamp ts = new Timestamp(time);
        String timeStamp = new SimpleDateFormat("MM/dd/yyyy").format(ts);

        String insertAccountQuery = "INSERT INTO accounts (user_id, date_added, account_number, institution, account_type, account_subtype, balance) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?);";

        try {
            DatabaseController.insertSevenColumnsIntoDatabase(insertAccountQuery, MainGUIController.getCurrentUserId(),
                    timeStamp, accountNumber,
                    account, accountType,
                    "", "0.0");

        } catch (SQLException ex) {
            Logger.getLogger(AccountsTabController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method gets the user id from the email the current user logged in
     * with from the database.
     */
    private static void getUserID() {
        String currentUserIdQuery = "SELECT user_id FROM users WHERE email LIKE '" + MainGUIController.email + "'";
        try {
            MainGUIController.setCurrentUserId(DatabaseController.selectDBQueryReturnOneString(currentUserIdQuery, "user_id"));

        } catch (SQLException ex) {
            Logger.getLogger(AccountsTabController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

}
