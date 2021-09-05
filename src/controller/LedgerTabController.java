/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.AccountOutflowTransactionsModel;
import model.AccountInflowTransactionsModel;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class adds, edits and deletes transactions in the inflow and outflow transactions tables.
 *
 * @author Kevin Mock
 */
public class LedgerTabController implements Initializable {

    /**/
    @FXML
    private TableView<AccountInflowTransactionsModel> inflowTransactionsTableView;
    @FXML
    private TableColumn<AccountInflowTransactionsModel, String> inflowTransactDate;
    @FXML
    private TableColumn<AccountInflowTransactionsModel, String> inflowTransactDesc;
    @FXML
    private TableColumn<AccountInflowTransactionsModel, String> inflowTransactFrom;
    @FXML
    private TableColumn<AccountInflowTransactionsModel, String> inflowTransactTo;
    @FXML
    private TableColumn<AccountInflowTransactionsModel, String> inflowTransactAmount;
    @FXML
    private TableView<AccountOutflowTransactionsModel> outflowTransactionsTableView;
    @FXML
    private TableColumn<AccountOutflowTransactionsModel, String> outflowTransactDate;
    @FXML
    private TableColumn<AccountOutflowTransactionsModel, String> outflowTransactDesc;
    @FXML
    private TableColumn<AccountOutflowTransactionsModel, String> outflowTransactFrom;
    @FXML
    private TableColumn<AccountOutflowTransactionsModel, String> outflowTransactTo;
    @FXML
    private TableColumn<AccountOutflowTransactionsModel, String> outflowTransactAmount;
    /**/
    @FXML
    private ComboBox<String> accountsLedgerComboBox;
    @FXML
    private JFXTabPane transactionOptionsTabPane;
    @FXML
    private JFXDatePicker newTransDateDatePicker;
    @FXML
    private JFXTextField newTransDescTextField;
    @FXML
    private JFXButton addTransactionButton;
    @FXML
    private JFXTextField newTransAmtTextField;
    @FXML
    private ComboBox<String> newTransAccToComboBox;
    @FXML
    private Label newTransAccFromLabel;
    @FXML
    private Label errMsgTransDateLabel;
    @FXML
    private Label errMsgTransDescLabel;
    @FXML
    private Label errMsgTransAmtLabel;
    @FXML
    private Tab editTransactionTab;
    @FXML
    private Tab newTransactionTab;
    @FXML
    private JFXDatePicker editTransDateDatePicker;
    @FXML
    private JFXTextField editTransDescTextField;
    @FXML
    private JFXButton editTransactionButton;
    @FXML
    private JFXTextField editTransAmtTextField;
    @FXML
    private Label editErrMsgTransDateLabel;
    @FXML
    private Label editErrMsgTransDescLabel;
    @FXML
    private Label editErrMsgTransAmtLabel;
    @FXML
    private JFXButton delTransactionButton;
    @FXML
    private Label selectedAccountTotal;
    @FXML
    private Label editTransAccFromLabel;
    @FXML
    private Label editTransAccToLabel;

    private String institution = null;
    private String accountType = null;
    private String accountIdNumber = null;
    private MainGUIController mainGUIController;
    private String currentBal;
    private final String delimiter = "-";
    static ObservableList<String> accountNamesObservableList = FXCollections.observableArrayList();
    static ObservableList<String> toAccountNamesObservableList = FXCollections.observableArrayList();
    private ObservableList<AccountInflowTransactionsModel> inflowTransTableModelObservList = FXCollections.observableArrayList();
    private ObservableList<AccountOutflowTransactionsModel> outflowTransTableModelObservList = FXCollections.observableArrayList();

    /* Dependency Injection */
    void injectMainController(MainGUIController mainGUIController) {
        this.mainGUIController = mainGUIController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /* Accessor Methods - These will be need by the MainGUIController for initial tab setup */
    ComboBox<String> getAccountsLedgerComboBox() {
        return accountsLedgerComboBox;
    }

    Label getNewTransAccFromLabel() {
        return newTransAccFromLabel;
    }

    ComboBox<String> getNewTransAccToComboBox() {
        return newTransAccToComboBox;
    }

    Tab getNewTransactionTab() {
        return newTransactionTab;
    }

    JFXTabPane getTransactionOptionsTabPane() {
        return transactionOptionsTabPane;
    }

    /**
     * Upon selection of an account in the ComboBox this method will render all
     * the outflow transactions in the outflow transactions TableView and all the
     * inflow transactions in the inflow transactions TableView associated to the
     * account.
     *
     * @param event
     */
    @FXML
    private void onClickGetAccountSelectedTransactions(ActionEvent event) {
        refreshTables();
    }

    /**
     * This method will listen to the outflow transactions table and when the
     * user chooses a transaction all the information associated with the
     * transaction will show up in the controls for editing or deleting.
     *
     * @param event
     */
    @FXML
    private void fillFormFromOutflowTransactionsOnMouseClicked(MouseEvent event) {

        String date = "";
        String desc = "";
        String amount = "";
        String accFrom = "";
        String accTo = "";

        /* These method calls may produce Null Pointer Exceptions when the user clicks on something that
        is not a row in the table, Implemented a try-catch block to catch them */
        try {
            date = outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDate();
            desc = outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDescription();
            amount = outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAmount();
            accFrom = outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAccountFrom();
            accTo = outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAccountTo();
        } catch (NullPointerException npe) {
        }

        if ((!date.equals("")) && (!desc.equals(""))) {
            /* If the user is here they successfully clicked on a row in the table */
            transactionOptionsTabPane.getSelectionModel().select(editTransactionTab);
            editTransDateDatePicker.getEditor().setText(date);
            editTransDescTextField.setText(desc);
            editTransAmtTextField.setText(amount);
            editTransAccFromLabel.setText(accFrom);
            editTransAccToLabel.setText(accTo);
        }
    }

    /**
     * This method will listen to the inflow transactions table and when the user
     * chooses a transaction all the information associated with the transaction
     * will show up in the controls for editing or deleting.
     *
     * @param event
     */
    @FXML
    private void fillFormFromInflowTransactionsOnMouseClicked(MouseEvent event) {

        String date = "";
        String desc = "";
        String amount = "";
        String accFrom = "";
        String accTo = "";

        /* These method calls may produce Null Pointer Exceptions when the user clicks on something that
        is not a row in the table, Implemented a try-catch block to catch them */
        try {
            date = inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDate();
            desc = inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDescription();
            amount = inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAmount();
            accFrom = inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAccountFrom();
            accTo = inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAccountTo();
        } catch (NullPointerException npe) {
        }

        if ((!date.equals("")) && (!desc.equals(""))) {
            /* If the user is here they successfully clicked on a row in the table */
            transactionOptionsTabPane.getSelectionModel().select(editTransactionTab);
            editTransDateDatePicker.getEditor().setText(date);
            editTransDescTextField.setText(desc);
            editTransAmtTextField.setText(amount);
            editTransAccFromLabel.setText(accFrom);
            editTransAccToLabel.setText(accTo);
        }
    }

    /**
     * When the form is fully filled out and there are no fields left blank.
     * This method will insert the transaction information into the inflow
     * transactions table for the account that the money is going into and the outflow
     * transactions table for the account where the money is going out of. It
     * will then update the balance of each account with the respective amounts.
     *
     * @param event
     */
    @FXML
    private void addTransactionOnAction(ActionEvent event) {

        /* Check to see if the required fields are empty */
        boolean dateBool = FormValidation.dateFieldNotEmpty(newTransDateDatePicker, errMsgTransDateLabel, "Date Is Required");
        boolean accNumBool = FormValidation.textFieldNotEmpty(newTransDescTextField, errMsgTransDescLabel, "Description Is Required");
        boolean instNameBool = FormValidation.textFieldNotEmpty(newTransAmtTextField, errMsgTransAmtLabel, "Please Enter an Amount");

        if (dateBool && accNumBool && instNameBool) {

            /* Change the date format so the date is uniformly entered into the database with 2 digits for month and day */
            final String DATE_PICKER_FORMAT = "M/d/yyyy";
            final String SQLITE_DATE_FORMAT = "MM/dd/yyyy";

            String datePickerFormat = newTransDateDatePicker.getEditor().getText();
            String dateFormat = null;

            SimpleDateFormat sdfFrom = new SimpleDateFormat(DATE_PICKER_FORMAT);
            dateFormat = AccountTransactionsReport.getSqliteDateFormat(SQLITE_DATE_FORMAT, datePickerFormat, dateFormat, sdfFrom);

            String[] tempAccountToArray = newTransAccToComboBox.getSelectionModel().getSelectedItem().split(delimiter);
            setInstitutionAndAccountType(tempAccountToArray);

            /*
             * *************************** Inflow transactions ***************************
             */
            /* Get the account id of the account where money is going into and insert the inflow transaction under that account */
            if (!accountType.equals("Bill Default Account")) {
                try {
                    String accountIdQuery = "SELECT account_id FROM accounts WHERE institution LIKE '" + institution + "' AND "
                            + "account_type LIKE '" + accountType + "' AND "
                            + "user_id = " + MainGUIController.getCurrentUserId();
                    String currentAccountIdNumber = DatabaseController.selectDBQueryReturnOneString(accountIdQuery, "account_id");

                    String insertInflowTransactionQuery = "INSERT INTO account_inflow_transactions (user_id, account_id, inflow_trans_date, "
                            + "inflow_trans_description, inflow_trans_account_from, inflow_trans_account_to, inflow_trans_amount)"
                            + "VALUES (?, ?, ?, ?, ?, ?, ?);";

                    DatabaseController.insertSevenColumnsIntoDatabase(insertInflowTransactionQuery, MainGUIController.getCurrentUserId(),
                            currentAccountIdNumber, dateFormat,
                            newTransDescTextField.getText(),
                            newTransAccFromLabel.getText(),
                            newTransAccToComboBox.getSelectionModel().getSelectedItem(), newTransAmtTextField.getText());
                } catch (SQLException ex) {
                    Logger.getLogger(LedgerTabController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            String[] tempArray = newTransAccFromLabel.getText().split(delimiter);
            setInstitutionAndAccountType(tempArray);

            /*
             * *************************** Outflow Transactions ***************************
             */
            if (!accountType.equals("Default Account")) {
                /* Get the account id of the account where money is coming from and insert the outflow transaction under that account */
                String insertOutflowAccountQuery = "INSERT INTO account_outflow_transactions (user_id, account_id, outflow_trans_date, "
                        + "outflow_trans_description, outflow_trans_account_from, outflow_trans_account_to, outflow_trans_amount)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?);";
                try {
                    DatabaseController.insertSevenColumnsIntoDatabase(insertOutflowAccountQuery, MainGUIController.getCurrentUserId(),
                            accountIdNumber, dateFormat,
                            newTransDescTextField.getText(),
                            newTransAccFromLabel.getText(),
                            newTransAccToComboBox.getSelectionModel().getSelectedItem(), newTransAmtTextField.getText());
                } catch (SQLException ex) {
                    Logger.getLogger(LedgerTabController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            tempAccountToArray = newTransAccToComboBox.getSelectionModel().getSelectedItem().split(delimiter);
            setInstitutionAndAccountType(tempAccountToArray);

            /* Change the totals for the account based on the account type */
            if (accountType.equals("Credit Card") | accountType.equals("Checking") | accountType.equals("Miscellaneous Expense")
                    | accountType.equals("Bill") | accountType.equals("Savings")) {
                addToOrSubractFromAccount("+");
            }

            if (accountType.equals("Bill")) {
                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {

                        String editTransactionQuery = "UPDATE accounts SET "
                                + "total_payments = total_payments + ? "
                                + "WHERE "
                                + "institution LIKE '" + institution + "' AND "
                                + "account_type LIKE '" + accountType + "' AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);
                        editAccPrepStmt.setString(1, newTransAmtTextField.getText());
                        editAccPrepStmt.executeUpdate();

                    }
                } catch (SQLException ex) {
                    Logger.getLogger(LedgerTabController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            tempArray = newTransAccFromLabel.getText().split(delimiter);
            setInstitutionAndAccountType(tempArray);

            if (accountType.equals("Credit Card") | accountType.equals("Checking") | accountType.equals("Savings") | accountType.equals("Bill") | accountType.equals("Purchase")) {
                addToOrSubractFromAccount("-");
            }

            refreshTables();

            /* Clear the fields */
            newTransDescTextField.clear();
            newTransAmtTextField.clear();
            newTransAccFromLabel.setText(accountsLedgerComboBox.getSelectionModel().getSelectedItem());
            newTransAccToComboBox.setItems(toAccountNamesObservableList);
            newTransAccToComboBox.setValue(toAccountNamesObservableList.get(0));

        }
    }

    /**
     * When a selection is made from one of the tables, the controls are filled
     * with the information from the transaction. When the form is fully filled
     * out and there are no fields left blank. This method will allow the user
     * to make description changes, date changes, and amount changes to the
     * transaction selected in either table. If the amount is changed the
     * difference in the original amount and the new amount will be calculated
     * and associated account totals will be changed respectively.
     *
     * @param event
     */
    @FXML
    private void editTransactionOnAction(ActionEvent event) {

        /* Check to verify that the text fields are not empty */
        boolean dateBool = FormValidation.dateFieldNotEmpty(editTransDateDatePicker, editErrMsgTransDateLabel, "Date Is Required");
        boolean accNumBool = FormValidation.textFieldNotEmpty(editTransDescTextField, editErrMsgTransDescLabel, "Description Is Required");
        boolean instNameBool = FormValidation.textFieldNotEmpty(editTransAmtTextField, editErrMsgTransAmtLabel, "Transaction Amount Is Required");

        if (dateBool && accNumBool && instNameBool) {

            if (!outflowTransactionsTableView.getSelectionModel().isEmpty()) {
                /* Run this code if the user selected the Outflow Transactions Table */

                /* Change the date format so the date is uniformly entered into the database with 2 digits for month and day */
                final String DATE_PICKER_FORMAT = "M/d/yyyy";
                final String SQLITE_DATE_FORMAT = "MM/dd/yyyy";

                String datePickerFormat = editTransDateDatePicker.getEditor().getText();
                String dateFormat = null;

                SimpleDateFormat sdfFrom = new SimpleDateFormat(DATE_PICKER_FORMAT);
                dateFormat = AccountTransactionsReport.getSqliteDateFormat(SQLITE_DATE_FORMAT, datePickerFormat, dateFormat, sdfFrom);

                /*
                 * *************************** Outflow transactions ***************************
                 */
                /* Update all transaction information from the fields that is not the balance in the account outflow transactions table */
                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editTransactionQuery = "UPDATE account_outflow_transactions SET outflow_trans_date = ?, "
                                + "outflow_trans_description = ?, "
                                + "outflow_trans_amount = ?, "
                                + "outflow_trans_account_from = ?, "
                                + "outflow_trans_account_to = ? "
                                + "WHERE "
                                + "outflow_trans_date LIKE '" + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDate() + "' AND "
                                + "outflow_trans_description LIKE '" + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDescription() + "' AND "
                                + "outflow_trans_amount = " + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAmount() + " AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);

                        //Parameters
                        editAccPrepStmt.setString(1, dateFormat);
                        editAccPrepStmt.setString(2, editTransDescTextField.getText());
                        editAccPrepStmt.setString(3, editTransAmtTextField.getText());
                        editAccPrepStmt.setString(4, editTransAccFromLabel.getText());
                        editAccPrepStmt.setString(5, editTransAccToLabel.getText());

                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                String[] tempArray = editTransAccFromLabel.getText().split(delimiter);
                setInstitutionAndAccountType(tempArray);

                /* Start work on updating the balance of the account where the 
                money is coming from according to the change of the value of the transaction */
                String oldAmount = outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAmount();
                String newAmount = editTransAmtTextField.getText();
                /* Get the difference between the old amount and the new amount so
                adjustments of the balance of the account can be made */
                double oldAmountDouble = Double.parseDouble(oldAmount);
                double newAmountDouble = Double.parseDouble(newAmount);

                double newTotalDouble = oldAmountDouble - newAmountDouble;
                double roundedNewTotalDouble = Math.round(newTotalDouble * 100.0) / 100.0;

                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editAccountQuery = "UPDATE accounts SET balance = balance + ? "
                                + "WHERE "
                                + "institution LIKE '" + institution + "' AND "
                                + "account_type LIKE '" + accountType + "' AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                        //Parameters
                        editAccPrepStmt.setString(1, roundedNewTotalDouble + "");

                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                /*
                 * *************************** Inflow transaction ***************************
                 */
                /* Update all transaction information from the fields that is not the balance in the account inflow transactions table */
                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editTransactionQuery = "UPDATE account_inflow_transactions SET inflow_trans_date = ?, "
                                + "inflow_trans_description = ?, "
                                + "inflow_trans_amount = ?, "
                                + "inflow_trans_account_from = ?, "
                                + "inflow_trans_account_to = ? "
                                + "WHERE "
                                + "inflow_trans_date LIKE '" + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDate() + "' AND "
                                + "inflow_trans_description LIKE '" + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDescription() + "' AND "
                                + "inflow_trans_amount = " + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAmount() + " AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);

                        //Parameters
                        editAccPrepStmt.setString(1, dateFormat);
                        editAccPrepStmt.setString(2, editTransDescTextField.getText());
                        editAccPrepStmt.setString(3, editTransAmtTextField.getText());
                        editAccPrepStmt.setString(4, editTransAccFromLabel.getText());
                        editAccPrepStmt.setString(5, editTransAccToLabel.getText());

                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                /* Start work on the updating the balance of the account where the 
                money is going to according to the change of the value of the transaction */
                tempArray = editTransAccToLabel.getText().split(delimiter);
                setInstitutionAndAccountType(tempArray);

                /* Get the difference between the old amount and the new amount so
                adjustments of the balance of the account can be made */
                oldAmount = outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAmount();
                newAmount = editTransAmtTextField.getText();

                oldAmountDouble = Double.parseDouble(oldAmount);
                newAmountDouble = Double.parseDouble(newAmount);

                newTotalDouble = oldAmountDouble - newAmountDouble;
                roundedNewTotalDouble = Math.round(newTotalDouble * 100.0) / 100.0;

                if (!accountType.equals("Bill Default Account")) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {
                            String editAccountQuery = "UPDATE accounts SET balance = balance - ? "
                                    + "WHERE "
                                    + "institution LIKE '" + institution + "' AND "
                                    + "account_type LIKE '" + accountType + "' AND "
                                    + "user_id = " + MainGUIController.getCurrentUserId();
                            PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                            //Parameters
                            editAccPrepStmt.setString(1, roundedNewTotalDouble + "");

                            editAccPrepStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }

                if (accountType.equals("Bill")) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {
                            String editAccountQuery = "UPDATE accounts SET total_payments = total_payments - ? "
                                    + "WHERE "
                                    + "institution LIKE '" + institution + "' AND "
                                    + "account_type LIKE '" + accountType + "' AND "
                                    + "user_id = " + MainGUIController.getCurrentUserId();
                            PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                            //Parameters
                            editAccPrepStmt.setString(1, roundedNewTotalDouble + "");

                            editAccPrepStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }

                /* Reset Controls */
                editTransDescTextField.clear();
                editTransAmtTextField.clear();
                editTransAccFromLabel.setText("");
                editTransAccToLabel.setText("");
                /* Display updated information */
                refreshTables();
                /* Direct the user back to the new transactions tab */
                transactionOptionsTabPane.getSelectionModel().select(newTransactionTab);
            }

            /*
             * ******* The user selected something in the inflow transactions table view *******
             */
            if (!inflowTransactionsTableView.getSelectionModel().isEmpty()) {
                /* Run this code if the user selected the Inflow Transactions Table */

                /* Change the date format so the date is uniformly entered into the database with 2 digits for month and day */
                final String DATE_PICKER_FORMAT = "M/d/yyyy";
                final String SQLITE_DATE_FORMAT = "MM/dd/yyyy";

                String datePickerFormat = editTransDateDatePicker.getEditor().getText();
                String dateFormat = null;

                SimpleDateFormat sdfFrom = new SimpleDateFormat(DATE_PICKER_FORMAT);

                dateFormat = AccountTransactionsReport.getSqliteDateFormat(SQLITE_DATE_FORMAT, datePickerFormat, dateFormat, sdfFrom);

                /* Update the information the user has entered from the fields that are editable */
                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editTransactionQuery = "UPDATE account_inflow_transactions SET inflow_trans_date = ?, "
                                + "inflow_trans_description = ?, "
                                + "inflow_trans_amount = ?, "
                                + "inflow_trans_account_from = ?, "
                                + "inflow_trans_account_to = ? "
                                + "WHERE "
                                + "inflow_trans_date LIKE '" + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDate() + "' AND "
                                + "inflow_trans_description LIKE '" + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDescription() + "' AND "
                                + "inflow_trans_amount = " + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAmount() + " AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);

                        //Parameters
                        editAccPrepStmt.setString(1, dateFormat);
                        editAccPrepStmt.setString(2, editTransDescTextField.getText());
                        editAccPrepStmt.setString(3, editTransAmtTextField.getText());
                        editAccPrepStmt.setString(4, editTransAccFromLabel.getText());
                        editAccPrepStmt.setString(5, editTransAccToLabel.getText());

                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                String[] tempArray = editTransAccToLabel.getText().split(delimiter);
                setInstitutionAndAccountType(tempArray);

                /* Get the difference between the old amount and the new amount so
                adjustments of the balance of the account can be made */
                String oldAmount = inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAmount();
                String newAmount = editTransAmtTextField.getText();

                double oldAmountDouble = Double.parseDouble(oldAmount);
                double newAmountDouble = Double.parseDouble(newAmount);

                double newTotalDouble = oldAmountDouble - newAmountDouble;
                double roundedNewTotalDouble = Math.round(newTotalDouble * 100.0) / 100.0;

                /* Update the balance to the account*/
                if (!accountType.equals("Bill Default Account")) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {
                            String editAccountQuery = "UPDATE accounts SET balance = balance - ? "
                                    + "WHERE "
                                    + "institution LIKE '" + institution + "' AND "
                                    + "account_type LIKE '" + accountType + "' AND "
                                    + "user_id = " + MainGUIController.getCurrentUserId();
                            PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                            //Parameters
                            editAccPrepStmt.setString(1, roundedNewTotalDouble + "");

                            editAccPrepStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }

                if (accountType.equals("Bill")) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {
                            String editAccountQuery = "UPDATE accounts SET total_payments = total_payments - ? "
                                    + "WHERE "
                                    + "institution LIKE '" + institution + "' AND "
                                    + "account_type LIKE '" + accountType + "' AND "
                                    + "user_id = " + MainGUIController.getCurrentUserId();
                            PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                            //Parameters
                            editAccPrepStmt.setString(1, roundedNewTotalDouble + "");

                            editAccPrepStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }

                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editTransactionQuery = "UPDATE account_outflow_transactions SET outflow_trans_date = ?, "
                                + "outflow_trans_description = ?, "
                                + "outflow_trans_amount = ?, "
                                + "outflow_trans_account_from = ?, "
                                + "outflow_trans_account_to = ? "
                                + "WHERE "
                                + "outflow_trans_date LIKE '" + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDate() + "' AND "
                                + "outflow_trans_description LIKE '" + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDescription() + "' AND "
                                + "outflow_trans_amount = " + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAmount() + " AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);

                        //Parameters
                        editAccPrepStmt.setString(1, dateFormat);
                        editAccPrepStmt.setString(2, editTransDescTextField.getText());
                        editAccPrepStmt.setString(3, editTransAmtTextField.getText());
                        editAccPrepStmt.setString(4, editTransAccFromLabel.getText());
                        editAccPrepStmt.setString(5, editTransAccToLabel.getText());

                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                tempArray = editTransAccFromLabel.getText().split(delimiter);
                setInstitutionAndAccountType(tempArray);

                /* Get the difference between the old amount and the new amount so
                adjustments of the balance of the account can be made */
                oldAmount = inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAmount();
                newAmount = editTransAmtTextField.getText();

                oldAmountDouble = Double.parseDouble(oldAmount);
                newAmountDouble = Double.parseDouble(newAmount);

                newTotalDouble = oldAmountDouble - newAmountDouble;
                roundedNewTotalDouble = Math.round(newTotalDouble * 100.0) / 100.0;
                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editAccountQuery = "UPDATE accounts SET balance = balance + ? "
                                + "WHERE "
                                + "institution LIKE '" + institution + "' AND "
                                + "account_type LIKE '" + accountType + "' AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                        //Parameters
                        editAccPrepStmt.setString(1, roundedNewTotalDouble + "");

                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                refreshTables();

                editTransDescTextField.clear();
                editTransAmtTextField.clear();
                editTransAccFromLabel.setText("");
                editTransAccToLabel.setText("");

                transactionOptionsTabPane.getSelectionModel().select(newTransactionTab);
            }
        }
    }

    /**
     * This method deletes the transaction selected by the user. This method
     * deletes the outflow transaction from the outflow transaction table
     * associated with the account where the money was going out of and deletes
     * the inflow transaction from the inflow transaction table associated with
     * the account where the money was going into. The account balances will be
     * updated to the original values.
     *
     * @param event
     */
    @FXML
    private void delTransactionOnAction(ActionEvent event
    ) {

        /* Check to verify that the fields are not empty */
        boolean dateBool = FormValidation.dateFieldNotEmpty(editTransDateDatePicker, editErrMsgTransDateLabel, "Date Is Required");
        boolean accNumBool = FormValidation.textFieldNotEmpty(editTransDescTextField, editErrMsgTransDescLabel, "Description Is Required");
        boolean instNameBool = FormValidation.textFieldNotEmpty(editTransAmtTextField, editErrMsgTransAmtLabel, "Transaction Amount Is Required");

        if (dateBool && accNumBool && instNameBool) {
            /*
             * ******* A row was selected in the Outflow Transactions Table View *************
             */
            if (!outflowTransactionsTableView.getSelectionModel().isEmpty()) {
                /* The user selected something in the outflow transaction table */
                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editTransactionQuery = "DELETE FROM account_outflow_transactions "
                                + "WHERE "
                                + "outflow_trans_date LIKE '" + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDate() + "' AND "
                                + "outflow_trans_description LIKE '" + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDescription() + "' AND "
                                + "outflow_trans_amount = " + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAmount() + " AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);

                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                /*
                 * Update the "from" account balance of the transaction since the transaction will be deleted.
                 */
                String[] tempArray = editTransAccFromLabel.getText().split(delimiter);
                setInstitutionAndAccountType(tempArray);

                String amount = outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAmount();

                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editAccountQuery = "UPDATE accounts SET balance = balance + ? "
                                + "WHERE "
                                + "institution LIKE '" + institution + "' AND "
                                + "account_type LIKE '" + accountType + "' AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                        //Parameters
                        editAccPrepStmt.setString(1, amount);

                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                /* Delete the transaction from inflow transactions table */
                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editTransactionQuery = "DELETE FROM account_inflow_transactions "
                                + "WHERE "
                                + "inflow_trans_date LIKE '" + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDate() + "' AND "
                                + "inflow_trans_description LIKE '" + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransDescription() + "' AND "
                                + "inflow_trans_amount = " + outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAmount() + " AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);
                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                tempArray = editTransAccToLabel.getText().split(delimiter);
                setInstitutionAndAccountType(tempArray);

                amount = outflowTransactionsTableView.getSelectionModel().getSelectedItem().getOutflowTransAmount();

                /* Update the balance of the "to" account since the account will be deleted */
                if (!accountType.equals("Bill Default Account")) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {
                            String editAccountQuery = "UPDATE accounts SET balance = balance - ? "
                                    + "WHERE "
                                    + "institution LIKE '" + institution + "' AND "
                                    + "account_type LIKE '" + accountType + "' AND "
                                    + "user_id = " + MainGUIController.getCurrentUserId();
                            PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                            //Parameters
                            editAccPrepStmt.setString(1, amount);

                            editAccPrepStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }

                if (accountType.equals("Bill")) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {
                            String editAccountQuery = "UPDATE accounts SET total_payments = total_payments - ? "
                                    + "WHERE "
                                    + "institution LIKE '" + institution + "' AND "
                                    + "account_type LIKE '" + accountType + "' AND "
                                    + "user_id = " + MainGUIController.getCurrentUserId();
                            PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                            //Parameters
                            editAccPrepStmt.setString(1, amount);

                            editAccPrepStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }

                /* Clear the controls */
                editTransDescTextField.clear();
                editTransAmtTextField.clear();
                editTransAccFromLabel.setText("");
                editTransAccToLabel.setText("");

                refreshTables();
                transactionOptionsTabPane.getSelectionModel().select(newTransactionTab);
            }

            /*
             * ******* A row was selected in the Inflow Transactions Table View *************
             */
            if (!inflowTransactionsTableView.getSelectionModel().isEmpty()) {
                /* The user selected something in the inflow transaction table */
                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editTransactionQuery = "DELETE FROM account_inflow_transactions "
                                + "WHERE "
                                + "inflow_trans_date LIKE '" + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDate() + "' AND "
                                + "inflow_trans_description LIKE '" + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDescription() + "' AND "
                                + "inflow_trans_amount = " + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAmount() + " AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();

                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);
                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                String[] tempArray = editTransAccToLabel.getText().split(delimiter);
                setInstitutionAndAccountType(tempArray);

                String amount = inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAmount();
                /* Update the balance of the "to" account of the transaction in the accounts table */
                if (!accountType.equals("Bill Default Account")) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {
                            String editAccountQuery = "UPDATE accounts SET balance = balance - ? "
                                    + "WHERE "
                                    + "institution LIKE '" + institution + "' AND "
                                    + "account_type LIKE '" + accountType + "' AND "
                                    + "user_id = " + MainGUIController.getCurrentUserId();
                            PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                            //Parameters
                            editAccPrepStmt.setString(1, amount);

                            editAccPrepStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }

                if (accountType.equals("Bill")) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {
                            String editAccountQuery = "UPDATE accounts SET total_payments = total_payments - ? "
                                    + "WHERE "
                                    + "institution LIKE '" + institution + "' AND "
                                    + "account_type LIKE '" + accountType + "' AND "
                                    + "user_id = " + MainGUIController.getCurrentUserId();
                            PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                            //Parameters
                            editAccPrepStmt.setString(1, amount);

                            editAccPrepStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }

                /* Delete the transaction from the outflow transactions table */
                try (Connection conn = DatabaseController.dBConnector()) {
                    if (conn != null) {
                        String editTransactionQuery = "DELETE FROM account_outflow_transactions "
                                + "WHERE "
                                + "outflow_trans_date LIKE '" + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDate() + "' AND "
                                + "outflow_trans_description LIKE '" + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransDescription() + "' AND "
                                + "outflow_trans_amount = " + inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAmount() + " AND "
                                + "user_id = " + MainGUIController.getCurrentUserId();
                        PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);
                        editAccPrepStmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                tempArray = editTransAccFromLabel.getText().split(delimiter);
                setInstitutionAndAccountType(tempArray);

                amount = inflowTransactionsTableView.getSelectionModel().getSelectedItem().getInflowTransAmount();

                /* Update the balance of the "from" account of the transaction in the accounts table */
                if (!institution.equals("Bill Amount")) {
                    try (Connection conn = DatabaseController.dBConnector()) {
                        if (conn != null) {
                            String editAccountQuery = "UPDATE accounts SET balance = balance + ? "
                                    + "WHERE "
                                    + "institution LIKE '" + institution + "' AND "
                                    + "account_type LIKE '" + accountType + "' AND "
                                    + "user_id = " + MainGUIController.getCurrentUserId();
                            PreparedStatement editAccPrepStmt = conn.prepareStatement(editAccountQuery);

                            //Parameters
                            editAccPrepStmt.setString(1, amount);

                            editAccPrepStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }
                /* render in the ledger table with updated information */
                refreshTables();

                /* reset the controls */
                editTransDescTextField.clear();
                editTransAmtTextField.clear();
                editTransAccFromLabel.setText("");
                editTransAccToLabel.setText("");
                transactionOptionsTabPane.getSelectionModel().select(newTransactionTab);
            }
        }
    }

    /**
     * This method will be used to change the balance of an account where a
     * transaction was added, amount of the transaction was edited, or the transaction was deleted.
     *
     * @param operator
     */
    private void addToOrSubractFromAccount(String operator) {
        try (Connection conn = DatabaseController.dBConnector()) {
            if (conn != null) {
                String editTransactionQuery = "UPDATE accounts SET balance = balance " + operator + " ? "
                        + "WHERE "
                        + "institution LIKE '" + institution + "' AND "
                        + "account_type LIKE '" + accountType + "' AND "
                        + "user_id = " + MainGUIController.getCurrentUserId();
                PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);
                editAccPrepStmt.setString(1, newTransAmtTextField.getText());

                editAccPrepStmt.executeUpdate();
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    /**
     * This method will get all the information from both the outflow
     * transactions table and the inflow transactions table associated with the
     * account that is selected from the ComboBox of accounts the user has made.
     */
    private void refreshTables() {
        String accountSelected = accountsLedgerComboBox.getSelectionModel().getSelectedItem();
        if (accountSelected != null) {
            String[] tempArray = accountSelected.split(delimiter);
            setInstitutionAndAccountType(tempArray);

            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {
                    String currentUserSpecificAccountQuery = "SELECT account_id FROM accounts WHERE institution LIKE '" + institution + "' AND "
                            + "account_type LIKE '" + accountType + "' AND "
                            + "user_id = " + MainGUIController.getCurrentUserId();
                    PreparedStatement currentUserSpecificAccountPrepStmt = conn.prepareStatement(currentUserSpecificAccountQuery);
                    ResultSet currentUserSpecificAccountResultSet = currentUserSpecificAccountPrepStmt.executeQuery();

                    while (currentUserSpecificAccountResultSet.next()) {
                        accountIdNumber = currentUserSpecificAccountResultSet.getString("account_id");
                    }

                    accountsLedgerComboBox.setItems(accountNamesObservableList);
                }
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            }

            inflowTransactDate.setSortType(TableColumn.SortType.DESCENDING);

            inflowTransactDate.setCellValueFactory(new PropertyValueFactory<>("inflowTransDate"));
            inflowTransactDesc.setCellValueFactory(new PropertyValueFactory<>("inflowTransDescription"));
            inflowTransactFrom.setCellValueFactory(new PropertyValueFactory<>("inflowTransAccountFrom"));
            inflowTransactTo.setCellValueFactory(new PropertyValueFactory<>("inflowTransAccountTo"));
            inflowTransactAmount.setCellValueFactory(new PropertyValueFactory<>("inflowTransAmount"));

            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {
                    String transactionQuery = "SELECT inflow_trans_date, inflow_trans_description, inflow_trans_account_to, inflow_trans_account_from, inflow_trans_amount FROM account_inflow_transactions "
                            + "WHERE account_id = " + accountIdNumber + " AND "
                            + "user_id = " + MainGUIController.getCurrentUserId() + " AND "
                            + "inflow_trans_account_to LIKE '" + accountSelected + "'";
                    PreparedStatement transactionPrepStmt = conn.prepareStatement(transactionQuery);
                    ResultSet transactionResultSet = transactionPrepStmt.executeQuery();

                    inflowTransTableModelObservList.clear();

                    while (transactionResultSet.next()) {

                        inflowTransTableModelObservList.add(new AccountInflowTransactionsModel(
                                transactionResultSet.getString("inflow_trans_date"),
                                transactionResultSet.getString("inflow_trans_description"),
                                transactionResultSet.getString("inflow_trans_account_from"),
                                transactionResultSet.getString("inflow_trans_account_to"),
                                transactionResultSet.getString("inflow_trans_amount")
                        ));
                    }
                }
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            }

            inflowTransactionsTableView.setItems(inflowTransTableModelObservList);

            inflowTransactionsTableView.getSortOrder().add(inflowTransactDate);

            newTransAccFromLabel.setText(accountSelected);
            outflowTransactDate.setSortType(TableColumn.SortType.DESCENDING);
            outflowTransactDate.setCellValueFactory(new PropertyValueFactory<>("outflowTransDate"));
            outflowTransactDesc.setCellValueFactory(new PropertyValueFactory<>("outflowTransDescription"));
            outflowTransactFrom.setCellValueFactory(new PropertyValueFactory<>("outflowTransAccountFrom"));
            outflowTransactTo.setCellValueFactory(new PropertyValueFactory<>("outflowTransAccountTo"));
            outflowTransactAmount.setCellValueFactory(new PropertyValueFactory<>("outflowTransAmount"));

            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {
                    String transactionQuery = "SELECT outflow_trans_date, outflow_trans_description, outflow_trans_account_to, outflow_trans_account_from, outflow_trans_amount FROM account_outflow_transactions "
                            + "WHERE account_id = " + accountIdNumber + " AND "
                            + "user_id = " + MainGUIController.getCurrentUserId() + " AND "
                            + "outflow_trans_account_from LIKE '" + accountSelected + "'";
                    PreparedStatement transactionPrepStmt = conn.prepareStatement(transactionQuery);
                    ResultSet transactionResultSet = transactionPrepStmt.executeQuery();

                    outflowTransTableModelObservList.clear();

                    while (transactionResultSet.next()) {

                        outflowTransTableModelObservList.add(new AccountOutflowTransactionsModel(
                                transactionResultSet.getString("outflow_trans_date"),
                                transactionResultSet.getString("outflow_trans_description"),
                                transactionResultSet.getString("outflow_trans_account_from"),
                                transactionResultSet.getString("outflow_trans_account_to"),
                                transactionResultSet.getString("outflow_trans_amount")
                        ));
                    }
                }
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(LedgerTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            }

            String balanceQuery = "SELECT printf('%.2f', balance) AS balance FROM accounts WHERE institution LIKE '" + institution + "' AND "
                    + "account_type LIKE '" + accountType + "' AND "
                    + "user_id = " + MainGUIController.getCurrentUserId();

            try {
                currentBal = DatabaseController.selectDBQueryReturnOneString(balanceQuery, "balance");
            } catch (SQLException ex) {
                Logger.getLogger(LedgerTabController.class.getName()).log(Level.SEVERE, null, ex);
            }

            selectedAccountTotal.setText(currentBal);
            outflowTransactionsTableView.setItems(outflowTransTableModelObservList);
            outflowTransactionsTableView.getSortOrder().add(outflowTransactDate);
            newTransAccFromLabel.setText(accountSelected);
        }
    }

    /**
     * When called this method will set institution and account type to a
     * variable when an AccountName-AccountType combination is present in a
     * control and the name needs to be separated and used for a SQL query.
     *
     * @param tempAccountToArray
     */
    private void setInstitutionAndAccountType(String[] tempAccountToArray) {
        for (int x = 0; x < tempAccountToArray.length; x++) {
            if (tempAccountToArray[x].equals(tempAccountToArray[0])) {
                institution = tempAccountToArray[0];
            }
            if (tempAccountToArray[x].equals(tempAccountToArray[1])) {
                accountType = tempAccountToArray[1];
            }
        }
    }

}
