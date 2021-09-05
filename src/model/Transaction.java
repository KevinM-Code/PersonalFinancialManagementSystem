/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import controller.AccountTransactionsReport;

/**
 * This transaction object is used in {@link AccountTransactionsReport#dataList()} to create each transaction in the report
 *
 * @author Kevin Mock
 */
public class Transaction {
    private final String account;
    private final String transType;
    private final String transDate;
    private final String transDesc;
    private final String transFromAcc;
    private final String transToAcc;
    private final String transAmount;

    public Transaction(String account, String transType, String transDate, String transDesc, String transFromAcc, String transToAcc, String transAmount) {
        this.account = account;
        this.transType = transType;
        this.transDate = transDate;
        this.transDesc = transDesc;
        this.transFromAcc = transFromAcc;
        this.transToAcc = transToAcc;
        this.transAmount = transAmount;
    }

    /**
     * This method will return the account name.
     * @return account
     */
    public String getAccount() {
        return account;
    }

    /**
     * This method will return the transaction type.
     * @return transType
     */
    public String getTransType() {
        return transType;
    }

    /**
     * This method will return the transaction date.
     * @return transDate
     */
    public String getTransDate() {
        return transDate;
    }

    /**
     * This method will return the transaction description.
     * @return transDesc
     */
    public String getTransDesc() {
        return transDesc;
    }

    /**
     * This method will return the account where the money is going to.
     * @return transToAcc
     */
    public String getTransToAcc() {
        return transToAcc;
    }

    /**
     * This method will return the account where the money is coming from.
     * @return transFromAcc
     */
    public String getTransFromAcc() {
        return transFromAcc;
    }

    /**
     * This method will return the amount of the transaction
     * @return transAmount
     */
    public String getTransAmount() {
        return transAmount;
    }

}
