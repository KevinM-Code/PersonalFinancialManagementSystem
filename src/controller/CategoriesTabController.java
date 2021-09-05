/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.AccountByCategoryModel;
import toolkit.DatabaseController;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class adds, deletes, and edits categories and their account assignments.
 *
 * @author Kevin Mock
 */
public class CategoriesTabController implements Initializable {

    @FXML
    private JFXTextField newCatTextField;
    @FXML
    private ComboBox<String> newAccAssignToCatComboBox;
    @FXML
    private JFXButton newCatWithAssignAccountButton;
    @FXML
    private TreeTableView<AccountByCategoryModel> categoriesTreeTableView;
    @FXML
    private TreeTableColumn<AccountByCategoryModel, String> accByCat;
    @FXML
    private TreeTableColumn<AccountByCategoryModel, String> accountBal;
    @FXML
    private ComboBox<String> editCatAssignToAccComboBox;
    @FXML
    private JFXTabPane categoryOptionsTabPane;
    @FXML
    private Tab editCategoryTab;
    @FXML
    private JFXTextField editCatNameTextField;
    @FXML
    private JFXButton editNameOfCatButton;
    @FXML
    private JFXButton delCatSelectedButton;
    @FXML
    private JFXButton editAccAssignedButton;
    @FXML
    private JFXButton delAccFromCatButton;
    @FXML
    private Label accountSelectedLabel;
    @FXML
    private Label errNewCategoryNameLabel;
    @FXML
    private JFXButton newAccountToCategoryButton;
    @FXML
    private Label newCatNameLabel;
    @FXML
    private ComboBox<String> newAddAccToCatComboBox;

    private String institution = "";
    private String accountType = "";

    static TreeItem<AccountByCategoryModel> root = new TreeItem<>(new AccountByCategoryModel("Root", ""));
    static ObservableList<TreeItem<AccountByCategoryModel>> accByCatModTblObservableList = FXCollections.observableArrayList();
    static ObservableList<String> accToAssignToCatObservableList = FXCollections.observableArrayList("Checking", "Savings", "Credit Card", "Bill", "Miscellaneous Expense");
    static ObservableList<String> categoriesObservableList = FXCollections.observableArrayList();

    private MainGUIController mainGUIController;

    /* Dependency Injection */
    void injectMainController(MainGUIController mainGUIController) {
        this.mainGUIController = mainGUIController;
    }

    /**
     * Accessor method
     *
     * @return newAccAssignToCatComboBox
     */
    ComboBox<String> getNewAccAssignToCatComboBox() {
        return newAccAssignToCatComboBox;
    }

    /**
     * Accessor method
     *
     * @return categoriesTreeTableView
     */
    TreeTableView<AccountByCategoryModel> getCategoriesTreeTableView() {
        return categoriesTreeTableView;
    }

    /**
     * Accessor method
     *
     * @return accByCat
     */
    TreeTableColumn<AccountByCategoryModel, String> getAccByCat() {
        return accByCat;
    }

    /**
     * Accessor method
     *
     * @return accountBal
     */
    TreeTableColumn<AccountByCategoryModel, String> getAccountBal() {
        return accountBal;
    }

    /**
     * Accessor method
     *
     * @return editCatAssignToAccComboBox
     */
    ComboBox<String> getEditCatAssignToAccComboBox() {
        return editCatAssignToAccComboBox;
    }

    /**
     * Accessor method
     *
     * @return newAddAccToCatComboBox
     */
    ComboBox<String> getNewAddAccToCatComboBox() {
        return newAddAccToCatComboBox;
    }

    /**
     * Accessor method
     *
     * @return accountSelectedLabel
     */
    Label getAccountSelectedLabel() {
        return accountSelectedLabel;
    }

    /**
     * Accessor method
     *
     * @return editCatNameTextField
     */
    JFXTextField getEditCatNameTextField() {
        return editCatNameTextField;
    }


    /**
     * Initializes the Categories Controller Class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * When called this method will set institution and account type to a variable when an AccountName-AccountType
     * combination is present in a control and the name needs to be separated and used for a SQL query.
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

    /**
     * When this method is called it will add a category to the categories table and assign the category id to
     * the account selected by the user in the accounts table. The entire TreeTableView will render showing the
     * updated information inserted.
     *
     * @param event
     */
    @FXML
    void newCategoryOnAction(ActionEvent event) {

        // Controls for the new category and the assigned account
        String newCategory = newCatTextField.getText();
        String accountSelected = newAccAssignToCatComboBox.getSelectionModel().getSelectedItem();

        String[] tempArray = accountSelected.split("-");
        setInstitutionAndAccountType(tempArray);
        String catId = null;

        /* Query to find out if the new category name being entered already exists */
        String query = "SELECT category_id FROM categories WHERE cat_name = '"
                + newCategory + "' AND "
                + "user_id = " + MainGUIController.getCurrentUserId();
        try {
            catId = DatabaseController.selectDBQueryReturnOneString(query, "category_id");
        } catch (SQLException ex) {
            Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* From the code above and this conditional statement we are making sure there is something in the textbox and
         * and that the new category the user is going to make is unique to the user's categories already made*/
        if ((catId == null) && !newCategory.isEmpty()) {

            /*
             * Now that the category being entered is unique insert the category into the table
             */
            errNewCategoryNameLabel.setText("");
            String insertCatQuery = "INSERT INTO categories (user_id, cat_name) VALUES(?,?)";

            try (Connection conn = DatabaseController.dBConnector()) {
                if (conn != null) {
                    PreparedStatement pstmt = conn.prepareStatement(insertCatQuery);
                    pstmt.setString(1, MainGUIController.getCurrentUserId());
                    pstmt.setString(2, newCategory);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, e);
            }

            /*
             * Get the category id the database created for the newly entered category and update the account assigned
             * with the category id in the accounts table.
             */
            query = "SELECT category_id FROM categories WHERE cat_name = '"
                    + newCategory + "' AND "
                    + "user_id = " + MainGUIController.getCurrentUserId();
            try {
                catId = DatabaseController.selectDBQueryReturnOneString(query, "category_id");
            } catch (SQLException ex) {
                Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try (Connection conn = DatabaseController.dBConnector()) {
                String editCatQuery = "UPDATE accounts SET acc_cat_id = ? "
                        + "WHERE "
                        + "institution LIKE '" + institution + "' AND "
                        + "account_type LIKE '" + accountType + "' AND "
                        + "user_id = " + MainGUIController.getCurrentUserId();
                PreparedStatement editAccPrepStmt = conn.prepareStatement(editCatQuery);
                //Parameters
                editAccPrepStmt.setString(1, catId);
                editAccPrepStmt.executeUpdate();

            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(CategoriesTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }

        } else {
            /* The category name already exist */
            errNewCategoryNameLabel.setText("Existing Category Name or Empty. Choose Another");
        }

        newCatTextField.setText("");
        newAccAssignToCatComboBox.getSelectionModel().select(0);
        if (!newCatWithAssignAccountButton.isDisabled()) {
            newCatWithAssignAccountButton.setDisable(true);
        }

        /* Render the Categories Table View with the new information */
        refreshCategories();
        expandTreeView(root);
    }

    /**
     * The user has clicked the new category text field to enter a new category name, this method enables the button
     * the user will use to submit the new category with the new account assigned to it.
     *
     * @param event
     */
    @FXML
    void newCatTextFieldOnMouseClick(MouseEvent event) {
        if (newCatWithAssignAccountButton.isDisabled()) {
            newCatWithAssignAccountButton.setDisable(false);
        }
    }

    /**
     * This method assigns another account to an already created category. When a user selects a category from the TreeTableView
     * it will show in the UI fields. The user will then have the opportunity to select one of their accounts to be assigned
     * to the category. In the database the account will be updated with the associated category id.
     *
     * @param event
     */
    @FXML
    private void assignNewAccountToCatOnAction(ActionEvent event) {

        // Controls for the existing category and the account being assigned
        String existingCategory = newCatNameLabel.getText();
        String accountSelected = newAddAccToCatComboBox.getSelectionModel().getSelectedItem();

        /*
         * Divide accountSelected to individual array elements one being the institution and the other is accountType
         * and set the institution and accountType fields extracted from accountSelected
         */
        String[] tempArray = accountSelected.split("-");
        setInstitutionAndAccountType(tempArray);

        String catId = null;

        /* Use the newly assigned fields to query the database for the category id of the new category and assigned
        the new category id to the account  */
        String query = "SELECT category_id FROM categories WHERE cat_name = '"
                + existingCategory + "' AND "
                + "user_id = " + MainGUIController.getCurrentUserId();
        try {
            catId = DatabaseController.selectDBQueryReturnOneString(query, "category_id");
        } catch (SQLException ex) {
            Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try (Connection conn = DatabaseController.dBConnector()) {
            String editCatQuery = "UPDATE accounts SET acc_cat_id = ? "
                    + "WHERE "
                    + "institution LIKE '" + institution + "' AND "
                    + "account_type LIKE '" + accountType + "' AND "
                    + "user_id = " + MainGUIController.getCurrentUserId();
            PreparedStatement editAccPrepStmt = conn.prepareStatement(editCatQuery);
            //Parameters
            editAccPrepStmt.setString(1, catId);
            editAccPrepStmt.executeUpdate();

        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(CategoriesTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        if (!newAccountToCategoryButton.isDisabled()) {
            newAccountToCategoryButton.setDisable(true);
        }

        errNewCategoryNameLabel.setText("");

        refreshCategories();
        expandTreeView(root);
    }

    /**
     * This method will fill in the form based on weather the user clicks a root element (category) or a leaf (account)
     * and displays the appropriate information in the controls for editing, adding to or deleting.
     *
     * @param event
     */
    @FXML
    void getAccAndCatFillFormOnMouseClick(MouseEvent event) {

        String getCategory = "";
        String getAccount = "";
        try {
            getCategory = categoriesTreeTableView.getSelectionModel().getTreeTableView().getSelectionModel()
                    .getSelectedItem().getParent().getValue().getAccount();
            getAccount = categoriesTreeTableView.getSelectionModel().getTreeTableView()
                    .getSelectionModel().getSelectedItem().getValue().getAccount();
        } catch (NullPointerException npe) {
            Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, npe);
        }

        /* Actions if a category is selected */
        if ("Root".equals(getCategory)) {

            if (!newCatWithAssignAccountButton.isDisabled()) {
                newCatWithAssignAccountButton.setDisable(true);
            }

            if (!delAccFromCatButton.isDisabled()) {
                delAccFromCatButton.setDisable(true);
            }

            if (!editAccAssignedButton.isDisabled()) {
                editAccAssignedButton.setDisable(true);
            }

            editNameOfCatButton.setDisable(false);
            delCatSelectedButton.setDisable(false);
            editCatNameTextField.setText(categoriesTreeTableView.getSelectionModel().getTreeTableView()
                    .getSelectionModel().getSelectedItem().getValue().getAccount());

            newCatNameLabel.setText(categoriesTreeTableView.getSelectionModel().getTreeTableView()
                    .getSelectionModel().getSelectedItem().getValue().getAccount());

            if (newAccountToCategoryButton.isDisabled()) {
                newAccountToCategoryButton.setDisable(false);
            }
        }
        /* Actions if an account is selected */
        else {
            if (!newAccountToCategoryButton.isDisabled()) {
                newAccountToCategoryButton.setDisable(true);
            }

            if (!getAccount.equals("")) {
                categoryOptionsTabPane.getSelectionModel().select(editCategoryTab);
            }

            if (!delCatSelectedButton.isDisabled()) {
                delCatSelectedButton.setDisable(true);
            }

            if (!editNameOfCatButton.isDisabled()) {
                editNameOfCatButton.setDisable(true);
            }

            delAccFromCatButton.setDisable(false);
            editAccAssignedButton.setDisable(false);

            accountSelectedLabel.setText(getAccount);
            editCatAssignToAccComboBox.setValue(getCategory);
        }
    }

    /**
     * This method will delete an account assignment from a category.
     *
     * @param event
     */
    @FXML
    void delAccFromCatOnAction(ActionEvent event) {

        String catId = null;

        String query = "SELECT category_id FROM categories WHERE cat_name = '"
                + categoriesTreeTableView.getSelectionModel().getTreeTableView().getSelectionModel()
                .getSelectedItem().getParent().getValue().getAccount() + "' AND "
                + "user_id = " + MainGUIController.getCurrentUserId();
        try {
            catId = DatabaseController.selectDBQueryReturnOneString(query, "category_id");
        } catch (SQLException ex) {
            Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, ex);
        }

        String accountSelected = categoriesTreeTableView.getSelectionModel().getTreeTableView().getSelectionModel().getSelectedItem().getValue().getAccount();

        /*
         * Divide accountSelected to individual array elements one being the institution and the other is accountType
         * and set the institution and accountType fields extracted from accountSelected
         */
        String[] tempArray = accountSelected.split("-");
        setInstitutionAndAccountType(tempArray);
        try (Connection conn = DatabaseController.dBConnector()) {
            if (conn != null) {
                String editCatQuery = "UPDATE accounts SET acc_cat_id = ? "
                        + "WHERE "
                        + "acc_cat_id = " + catId + " AND "
                        + "institution LIKE '" + institution + "' AND "
                        + "account_type LIKE '" + accountType + "' AND "
                        + "user_id = " + MainGUIController.getCurrentUserId();
                PreparedStatement editAccPrepStmt = conn.prepareStatement(editCatQuery);
                //Parameters
                editAccPrepStmt.setString(1, "");
                editAccPrepStmt.executeUpdate();
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(CategoriesTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        refreshCategories();
        expandTreeView(root);

        if (!delAccFromCatButton.isDisabled()) {
            delAccFromCatButton.setDisable(true);
        }

        if (!editAccAssignedButton.isDisabled()) {
            editAccAssignedButton.setDisable(true);
        }
    }

    /**
     * In this method, when a category name is selected it will delete the category and all account assignment(s) to the category.
     *
     * @param event
     */
    @FXML
    void deleteCategorySelectedOnAction(ActionEvent event) {

        String catId = null;
        String accountSelected = categoriesTreeTableView.getSelectionModel().getTreeTableView().getSelectionModel().getSelectedItem().getValue().getAccount();
        
        /* Get the category ID of the category, so you can query the accounts table and
        unassign the current category ID that is assigned to the account*/
        String query = "SELECT category_id FROM categories WHERE cat_name = '"
                + accountSelected + "' AND "
                + "user_id = " + MainGUIController.getCurrentUserId();
        try {
            catId = DatabaseController.selectDBQueryReturnOneString(query, "category_id");
        } catch (SQLException ex) {
            Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* The following must be in this order due to foreign key constraints */
        /* Delete the category assignment to the account(s) */
        try (Connection conn = DatabaseController.dBConnector()) {
            String editCatQuery = "UPDATE accounts SET acc_cat_id = ? "
                    + "WHERE "
                    + "acc_cat_id = " + catId + " AND "
                    + "user_id = " + MainGUIController.getCurrentUserId();
            PreparedStatement editAccPrepStmt = conn.prepareStatement(editCatQuery);
            //Parameters
            editAccPrepStmt.setString(1, "");
            editAccPrepStmt.executeUpdate();

        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(CategoriesTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Delete the category from the categories table */
        try (Connection conn = DatabaseController.dBConnector()) {
            String editTransactionQuery = "DELETE FROM categories "
                    + "WHERE "
                    + "category_id = " + catId;
            PreparedStatement editAccPrepStmt = conn.prepareStatement(editTransactionQuery);
            editAccPrepStmt.executeUpdate();

        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(CategoriesTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        refreshCategories();

        expandTreeView(root);

        if (!delCatSelectedButton.isDisabled()) {
            delCatSelectedButton.setDisable(true);
        }

        if (!editNameOfCatButton.isDisabled()) {
            editNameOfCatButton.setDisable(true);
        }
    }

    /**
     * This method will allow the user to reassign a different category to an account, than the one already assigned.
     *
     * @param event
     */
    @FXML
    private void editAccountAssignedToCatOnAction(ActionEvent event) {

        /* The controls associated with editing the account assigned to the category*/
        String categorySelected = editCatAssignToAccComboBox.getSelectionModel().getSelectedItem();
        String accountSelected = accountSelectedLabel.getText();

        /* Get the IDs of the old and the new categories */
        String selectedCatId = null;
        String newCatId = null;

        String query = "SELECT category_id FROM categories WHERE cat_name = '"
                + categoriesTreeTableView.getSelectionModel().getTreeTableView().getSelectionModel()
                .getSelectedItem().getParent().getValue().getAccount() + "' AND "
                + "user_id = " + MainGUIController.getCurrentUserId();

        try {
            selectedCatId = DatabaseController.selectDBQueryReturnOneString(query, "category_id");
        } catch (SQLException ex) {
            Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, ex);
        }

        String sqlQuery = "SELECT category_id FROM categories WHERE cat_name = '"
                + categorySelected + "' AND "
                + "user_id = " + MainGUIController.getCurrentUserId();

        try {
            newCatId = DatabaseController.selectDBQueryReturnOneString(sqlQuery, "category_id");
        } catch (SQLException ex) {
            Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, ex);
        }

        String[] tempArray = accountSelected.split("-");
        setInstitutionAndAccountType(tempArray);

        /* Update the category assignment to the new accounts */
        try (Connection conn = DatabaseController.dBConnector()) {
            String editCatQuery = "UPDATE accounts SET acc_cat_id = ? "
                    + "WHERE "
                    + "acc_cat_id = " + selectedCatId + " AND "
                    + "institution LIKE '" + institution + "' AND "
                    + "account_type LIKE '" + accountType + "' AND "
                    + "user_id = " + MainGUIController.getCurrentUserId();
            PreparedStatement editAccPrepStmt = conn.prepareStatement(editCatQuery);
            //Parameters
            editAccPrepStmt.setString(1, newCatId);
            editAccPrepStmt.executeUpdate();

        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(CategoriesTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        refreshCategories();
        expandTreeView(root);
    }

    /**
     * This method will allow the user to edit the name of the category and keep the account associations with it.
     *
     * @param event
     */
    @FXML
    private void editNameOfSelectedCatOnAction(ActionEvent event) {

        String editedCategory = editCatNameTextField.getText();

        /* Update the category name */
        try (Connection conn = DatabaseController.dBConnector()) {
            String editCatQuery = "UPDATE categories SET cat_name = ? "
                    + "WHERE "
                    + "cat_name LIKE '" + categoriesTreeTableView.getSelectionModel().getTreeTableView()
                    .getSelectionModel().getSelectedItem().getValue().getAccount() + "' AND "
                    + "user_id = " + MainGUIController.getCurrentUserId();
            PreparedStatement editAccPrepStmt = conn.prepareStatement(editCatQuery);

            //Parameters
            editAccPrepStmt.setString(1, editedCategory);

            editAccPrepStmt.executeUpdate();

        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(CategoriesTabController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        if (!delCatSelectedButton.isDisabled()) {
            delCatSelectedButton.setDisable(true);
        }

        if (!editNameOfCatButton.isDisabled()) {
            editNameOfCatButton.setDisable(true);
        }

        refreshCategories();

        expandTreeView(root);
    }

    /**
     * When this method is called it will expand all the tree items recursively so the categories and accounts will be displayed to allow
     * convenience for the user.
     *
     * @param item
     */
    static void expandTreeView(TreeItem<AccountByCategoryModel> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            item.getChildren().forEach((child) -> {
                expandTreeView(child);
            });
        }
    }

    /**
     * This method will refresh the TreeTableView with all the categories and associated accounts to the category.
     */
    private void refreshCategories() {

        Map<String, Integer> catIndex = new HashMap<>();

        if (!categoriesTreeTableView.getRoot().getChildren().isEmpty()) {
            categoriesTreeTableView.getRoot().getChildren().clear();
        }

        accByCat.setCellValueFactory(new TreeItemPropertyValueFactory<AccountByCategoryModel, String>("account"));
        accountBal.setCellValueFactory(new TreeItemPropertyValueFactory<AccountByCategoryModel, String>("accountTotal"));

        /* Get all the categories for the current user */
        try (Connection conn = DatabaseController.dBConnector()) {
            String currentUserCatNamesQuery = "SELECT cat_name FROM categories WHERE user_id = " + MainGUIController.getCurrentUserId();
            PreparedStatement currentUserCatNamesPrepStmt = conn.prepareStatement(currentUserCatNamesQuery);
            ResultSet currentUserCatNamesResultSet = currentUserCatNamesPrepStmt.executeQuery();
            int i = 0;

            if (!accByCatModTblObservableList.isEmpty()) {
                accByCatModTblObservableList.clear();
            }

            while (currentUserCatNamesResultSet.next()) {

                accByCatModTblObservableList.add(
                        new TreeItem<>(new AccountByCategoryModel(
                                currentUserCatNamesResultSet.getString("cat_name"), "")));
                /* Add all the categories as children to the root */
                root.getChildren().add(accByCatModTblObservableList.get(i));
                /* Store the category name as a key to the value of the place or index the category is in the list */
                catIndex.put(currentUserCatNamesResultSet.getString("cat_name"), i);
                i++;
            }
        } catch (Exception e) {
            Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, e);
        }

        /* Get all the accounts that have a category associated with the account */
        String accountQuery = "SELECT institution, account_type, cat_name, balance, categories.category_id as cat_cat_id, accounts.acc_cat_id as acc_cat_id "
                + "FROM accounts INNER JOIN categories ON accounts.user_id = categories.user_id "
                + "WHERE accounts.acc_cat_id = categories.category_id AND accounts.user_id = " + MainGUIController.getCurrentUserId()
                + " ORDER BY categories.category_id;";

        try (Connection conn = DatabaseController.dBConnector();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(accountQuery)) {

            while (rs.next()) {
                String category = rs.getString("cat_name");
                // get the index associated with name of the category
                accByCatModTblObservableList.get(catIndex.get(category)).getChildren().add(new TreeItem<>(new AccountByCategoryModel(
                        rs.getString("institution") + "-" + rs.getString("account_type"),
                        rs.getString("balance")
                )));
            }
        } catch (Exception e) {
            Logger.getLogger(CategoriesTabController.class.getName()).log(Level.SEVERE, null, e);
        }

        categoriesTreeTableView.setRoot(root);
        categoriesTreeTableView.setShowRoot(false);
    }
}
