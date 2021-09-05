/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import controller.AccountTransactionsReport;

/**
 * This account object is used in {@link AccountTransactionsReport#dataList()} to organize the transactions first by the
 * account name, next by whether it is a inflow or outflow transaction.
 *
 * @author Kevin Mock
 */
public class Account {

    private String institution;
    private String accountType;

    public Account(String institution, String accountType) {
        this.institution = institution;
        this.accountType = accountType;
    }

    /**
     * @return institution - The name where the account resides (e.g. Regions Bank)
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * @return accountType - The type of the account (e.g. Checking)
     */
    public String getAccountType() {
        return accountType;
    }


}
