/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 * This class is the model for the credit transactions TableView
 *
 * @author Kevin Mock
 */
public class AccountOutflowTransactionsModel {
    private String creditTransDate;
    private String creditTransDescription;
    private String creditTransAccountFrom;
    private String creditTransAccountTo;
    private String creditTransAmount;

    public AccountOutflowTransactionsModel(String date, String desc, String from, String to, String amount) {
        this.creditTransDate = date;
        this.creditTransDescription = desc;
        this.creditTransAccountFrom = from;
        this.creditTransAccountTo = to;
        this.creditTransAmount = amount;
    }

    /**
     * The date of the current transaction.
     *
     * @return creditTransDate
     */
    public String getOutflowTransDate() {
        return creditTransDate;
    }

    /**
     * The description of the current transaction.
     *
     * @return creditTransDescription
     */
    public String getOutflowTransDescription() {
        return creditTransDescription;
    }

    /**
     * The account the money is coming out of for the current transaction
     *
     * @return creditTransAccountFrom
     */
    public String getOutflowTransAccountFrom() {
        return creditTransAccountFrom;
    }

    /**
     * The account the money is going into for the current transaction.
     *
     * @return creditTransAccountTo
     */
    public String getOutflowTransAccountTo() {
        return creditTransAccountTo;
    }

    /**
     * The amount of money that is being transferred for the current transaction.
     *
     * @return creditTransAmount
     */
    public String getOutflowTransAmount() {
        return creditTransAmount;
    }

}
