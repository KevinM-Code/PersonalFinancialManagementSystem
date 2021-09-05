/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 * This is the model for the accounts TableView in the Accounts Tab
 *
 * @author Kevin Mock
 */
public class AccountTableModel {

    private String dateAdded;
    private String accountNumber;
    private String accountInst;
    private String accountType;
    private String accountSubtype;
    private String balance;

    public AccountTableModel(String date, String num, String inst, String type, String subType, String bal) {

        this.dateAdded = date;
        this.accountNumber = num;
        this.accountInst = inst;
        this.accountType = type;
        this.accountSubtype = subType;
        this.balance = bal;
    }

    /**
     * This will return the date the account was added for the current account.
     *
     * @return dateAdded
     */
    public String getDateAdded() {
        return dateAdded;
    }

    /**
     * This will return the account number of the current account.
     *
     * @return accountNumber
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * This will return the account institution or name of the current account.
     *
     * @return accountInst
     */
    public String getAccountInst() {
        return accountInst;
    }

    /**
     * This will return the account type for the current account.
     *
     * @return
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * This will return the account subtype for the current account.
     *
     * @return
     */
    public String getAccountSubtype() {
        return accountSubtype;
    }

    /**
     * This will get the current balance for the account.
     *
     * @return
     */
    public String getBalance() {
        return balance;
    }
}

