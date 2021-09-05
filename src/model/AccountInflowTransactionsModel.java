/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 * This class is the model for the debit transactions TableView
 *
 * @author Kevin Mock
 */
public class AccountInflowTransactionsModel {

    private String debitTransDate;
    private String debitTransDescription;
    private String debitTransAccountFrom;
    private String debitTransAccountTo;
    private String debitTransAmount;


    public AccountInflowTransactionsModel(String date, String desc, String from, String to, String amount) {

        this.debitTransDate = date;
        this.debitTransDescription = desc;
        this.debitTransAccountFrom = from;
        this.debitTransAccountTo = to;
        this.debitTransAmount = amount;

    }

    /**
     * This method will return the transaction date of current transaction in the debit transactions TableView.
     *
     * @return debitTransDate
     */
    public String getInflowTransDate() {
        return debitTransDate;
    }

    /**
     * This method will return the description of the current transaction in the debit transactions TableView.
     *
     * @return debitTransDescription
     */
    public String getInflowTransDescription() {
        return debitTransDescription;
    }

    /**
     * This method will return the account of the selected transaction from which the transaction amount is coming from,
     * in the debit transactions TableView.
     *
     * @return debitTransAccountFrom
     */
    public String getInflowTransAccountFrom() {
        return debitTransAccountFrom;
    }

    /**
     * This method will return the account of the selected transaction from which the transaction amount is going to,
     * in the debit transactions TableView.
     *
     * @return
     */
    public String getInflowTransAccountTo() {
        return debitTransAccountTo;
    }

    /**
     * This method will return the amount of the selected transaction in the debit transaction
     * Table View.
     *
     * @return
     */
    public String getInflowTransAmount() {
        return debitTransAmount;
    }

}
