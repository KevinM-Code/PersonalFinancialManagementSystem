/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 * This class is the model that will be used in the Categories Tab TreeTableView for the list of categories and the accounts
 * assigned to the categories with their balances.
 *
 * @author Kevin Mock
 */
public class AccountByCategoryModel {

    private final String account;
    private final String accountTotal;

    public AccountByCategoryModel(String account, String accountTotal) {
        this.account = account;
        this.accountTotal = accountTotal;
    }

    /**
     * @return the current account name
     */
    public String getAccount() {
        return account;
    }

    /**
     * @return the current account total
     */
    public String getAccountTotal() {
        return accountTotal;
    }
}
