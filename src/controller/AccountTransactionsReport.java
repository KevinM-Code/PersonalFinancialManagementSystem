/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.core.layout.LayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.util.SortUtils;
import model.Account;
import model.Transaction;
import toolkit.DatabaseController;
import javafx.fxml.FXML;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class constructs the transactions report
 *
 * @author Kevin Mock
 */
public class AccountTransactionsReport {

    JasperPrint jp;
    private final Map params = new HashMap();
    private DynamicReport dr;

    /**
     * Used with dependency injection
     */
    @FXML
    private final ReportsTabController reportsTabController;

    /**
     * Dependency injection.
     *
     * @param reportsTabController
     */
    AccountTransactionsReport(ReportsTabController reportsTabController) {
        this.reportsTabController = reportsTabController;
    }

    /**
     * This method changes the format of the date from the DatePicker controls
     * into a format for the report title and timestamp when the report was
     * created. This method also creates the columns of the report and builds it.
     *
     * @return DynamicReport
     * @throws ClassNotFoundException
     */
    private DynamicReport buildReport() throws ClassNotFoundException {

        // Change the format of the date to display in the title of the report
        final String DATE_PICKER_FORMAT = "M/d/yyyy";
        final String SQLITE_DATE_FORMAT = "EEE MMM d, yyyy";

        String fromDatePickerFormat = reportsTabController.getReportFromDatePicker().getEditor().getText();
        String sqliteFromDateFormat = null;

        SimpleDateFormat sdfFrom = new SimpleDateFormat(DATE_PICKER_FORMAT);
        sqliteFromDateFormat = getSqliteDateFormat(SQLITE_DATE_FORMAT, fromDatePickerFormat, sqliteFromDateFormat, sdfFrom);

        String toDatePickerFormat = reportsTabController.getReportToDatePicker().getEditor().getText();
        String sqliteToDateFormat = null;

        SimpleDateFormat sdfTo = new SimpleDateFormat(DATE_PICKER_FORMAT);
        sqliteToDateFormat = getSqliteDateFormat(SQLITE_DATE_FORMAT, toDatePickerFormat, sqliteToDateFormat, sdfTo);

        // For the time the report was generated
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm:mm a z");
        Date date = new Date();

        FastReportBuilder drb = new FastReportBuilder();
        drb.addColumn("Account", "account", String.class.getName(), 40)
                .addColumn("Transaction Type", "transType", String.class.getName(), 35)
                .addColumn("Transaction Date", "transDate", String.class.getName(), 30)
                .addColumn("Transactions Description", "transDesc", String.class.getName(), 40)
                .addColumn("Transaction From Account", "transFromAcc", String.class.getName(), 40)
                .addColumn("Transaction To Account", "transToAcc", String.class.getName(), 40)
                .addColumn("Transaction Amount", "transAmount", String.class.getName(), 30)
                .addGroups(2)
                .setTitle("Transactions Report " + "for dates " + sqliteFromDateFormat + " to " + sqliteToDateFormat + " for " + MainGUIController.getCurrentUserFName() + " " + MainGUIController.getCurrentUserLName())
                .setSubtitle("This report was generated at " + dateFormat.format(date))
                .setPrintBackgroundOnOddRows(true)
                .setUseFullPageWidth(true);

        return drb.build();
    }

    /**
     * This method returns the ClassicLayoutManager
     *
     * @return ClassicLayoutManager
     */
    private LayoutManager getLayoutManager() {
        return new ClassicLayoutManager();
    }

    /**
     * This method retrieves the data from the {@link AccountTransactionsReport#dataList()} method a puts it into a
     * Collection. The data is sorted into the columns. Once the Collection is
     * sorted in the columns, the Sorted Collection object becomes a parameter
     * of a JRBeanCollectionDataSource object and that is what is returned.
     *
     * @return JRDataSource
     */
    private JRDataSource getDataSource() {
        Collection data = dataList();
        data = SortUtils.sortCollection(data, dr.getColumns());
        return new JRBeanCollectionDataSource(data);
    }

    /**
     * This method is called when the Create Report Button is pressed and takes
     * all the information gathered and runs the report.
     *
     * @throws JRException
     * @throws ClassNotFoundException
     */
    void runReport() throws JRException, ClassNotFoundException {

        dr = buildReport();
        // Get a JRDataSource implementation         
        JRDataSource ds = getDataSource();

        /*
         * Creates the JasperReport object, passed as a Parameter the
         * DynamicReport, a new ClassicLayoutManager instance and the JRDataSource
         */
        JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, getLayoutManager(), params);

        /*
         * Creates the JasperPrint object, passed as a Parameter the
         * JasperReport object, and the JRDataSource
         */
        jp = JasperFillManager.fillReport(jr, params, ds);
    }

    /**
     * This is where all the information is gathered for the report. The date
     * format is changed from DatePicker format to a format SQLite will
     * understand when comparing the in between dates that the user selected to
     * show transactions. Once they are converted this method creates and stores
     * rows of transactions that are in between the dates selected.
     *
     * @return ArrayList
     */
    List dataList() {

        // Change the format of the date to a format that SQLite can understand
        final String DATE_PICKER_FORMAT = "MM/dd/yyyy";
        final String SQLITE_DATE_FORMAT = "yyyy-MM-dd";

        /* From Date */
        String fromDatePickerFormat = reportsTabController.getReportFromDatePicker().getEditor().getText();
        String sqliteFromDateFormat = null;

        SimpleDateFormat sdfFrom = new SimpleDateFormat(DATE_PICKER_FORMAT);
        sqliteFromDateFormat = getSqliteDateFormat(SQLITE_DATE_FORMAT, fromDatePickerFormat, sqliteFromDateFormat, sdfFrom);

        /* To Date */
        String toDatePickerFormat = reportsTabController.getReportToDatePicker().getEditor().getText();
        String sqliteToDateFormat = null;

        SimpleDateFormat sdfTo = new SimpleDateFormat(DATE_PICKER_FORMAT);
        sqliteToDateFormat = getSqliteDateFormat(SQLITE_DATE_FORMAT, toDatePickerFormat, sqliteToDateFormat, sdfTo);

        // ArrayList of transactions
        ArrayList rowList = new ArrayList();
        // ArrayList of accounts
        ArrayList<Account> accountsList = new ArrayList<>();

        try (Connection conn = DatabaseController.dBConnector()) {
            String accountQuery = "SELECT institution, account_type from accounts WHERE user_id = " + MainGUIController.getCurrentUserId();
            PreparedStatement accountPrepStmt = conn.prepareStatement(accountQuery);
            ResultSet accountResultSet = accountPrepStmt.executeQuery();

            while (accountResultSet.next()) {
                // If they are not any of the default accounts then add the account to the ArrayList of accounts
                if (!accountResultSet.getString("institution").equals("Salary")
                        | !accountResultSet.getString("institution").equals("Bonus")
                        | !accountResultSet.getString("institution").equals("Investment Income")) {
                    accountsList.add(new Account(
                            accountResultSet.getString("institution"),
                            accountResultSet.getString("account_type")));
                }
            }
        } catch (Exception e) {
            Logger.getLogger(AccountTransactionsReport.class.getName()).log(Level.SEVERE, null, e);
        }

        /* This for loop collects all the inflow transactions within the dates
        selected by the user for each account and adds them to the rowList for the report */
        for (int i = 0; i < accountsList.size(); i++) {

            try (Connection conn = DatabaseController.dBConnector()) {
                String accountQuery = "SELECT inflow_trans_date, inflow_trans_description, inflow_trans_account_from, "
                        + "inflow_trans_account_to, inflow_trans_amount"
                        + " FROM account_inflow_transactions WHERE"
                        + " substr(inflow_trans_date,7,4)"
                        + "||'-'"
                        + "|| substr(inflow_trans_date,1,2)"
                        + "||'-'"
                        + "||substr(inflow_trans_date,4,2) BETWEEN '" + sqliteFromDateFormat + "' AND '" + sqliteToDateFormat + "' AND"
                        + " inflow_trans_account_to = '" + accountsList.get(i).getInstitution() + "-" + accountsList.get(i).getAccountType() + "' AND"
                        + " user_id = " + MainGUIController.getCurrentUserId();
                PreparedStatement accountPrepStmt = conn.prepareStatement(accountQuery);
                ResultSet accountResultSet = accountPrepStmt.executeQuery();

                // Add the Transaction Object to an element of the rowList
                while (accountResultSet.next()) {
                    rowList.add(new Transaction(accountsList.get(i).getInstitution() + "-" + accountsList.get(i).getAccountType(), "Inflow Transactions",
                            accountResultSet.getString("inflow_trans_date"), accountResultSet.getString("inflow_trans_description"),
                            accountResultSet.getString("inflow_trans_account_from"), accountResultSet.getString("inflow_trans_account_to"),
                            accountResultSet.getString("inflow_trans_amount")
                    ));
                }
            } catch (Exception e) {
                Logger.getLogger(AccountTransactionsReport.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        /* This for loop collects all the outflow transactions within the dates
        selected by the user for each account and adds them to the rowList*/
        for (int i = 0; i < accountsList.size(); i++) {
            try (Connection conn = DatabaseController.dBConnector()) {
                String accountQuery = "SELECT outflow_trans_date, outflow_trans_description, outflow_trans_account_from, "
                        + "outflow_trans_account_to, outflow_trans_amount"
                        + " FROM account_outflow_transactions WHERE"
                        + " substr(outflow_trans_date,7,4)"
                        + "||'-'"
                        + "||substr(outflow_trans_date,1,2)"
                        + "||'-'"
                        + "||substr(outflow_trans_date,4,2)"
                        + " BETWEEN '" + sqliteFromDateFormat + "' AND '" + sqliteToDateFormat + "' AND"
                        + " outflow_trans_account_from = '" + accountsList.get(i).getInstitution() + "-" + accountsList.get(i).getAccountType() + "' AND"
                        + " user_id = " + MainGUIController.getCurrentUserId();
                PreparedStatement accountPrepStmt = conn.prepareStatement(accountQuery);
                ResultSet accountResultSet = accountPrepStmt.executeQuery();

                // Add the Transaction Object to an element of the rowList
                while (accountResultSet.next()) {
                    rowList.add(new Transaction(accountsList.get(i).getInstitution() + "-" + accountsList.get(i).getAccountType(), "Outflow Transactions",
                            accountResultSet.getString("outflow_trans_date"), accountResultSet.getString("outflow_trans_description"),
                            accountResultSet.getString("outflow_trans_account_from"), accountResultSet.getString("outflow_trans_account_to"),
                            accountResultSet.getString("outflow_trans_amount")
                    ));

                }
            } catch (Exception e) {
                Logger.getLogger(AccountTransactionsReport.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        // return rowList an ArrayList Collection of transactions        
        if (rowList.size() == 0) {
            return null;
        }
        return rowList;
    }

    /**
     * Changes the format of the date from DatePicker format to SQLite date
     * format.
     *
     * @param SQLITE_DATE_FORMAT
     * @param fromDatePickerFormat
     * @param sqliteFromDateFormat
     * @param sdfFrom
     * @return sqliteFromDateFormat
     */
    static String getSqliteDateFormat(String SQLITE_DATE_FORMAT, String fromDatePickerFormat, String sqliteFromDateFormat, SimpleDateFormat sdfFrom) {
        Date d;
        try {
            d = sdfFrom.parse(fromDatePickerFormat);
            sdfFrom.applyPattern(SQLITE_DATE_FORMAT);
            sqliteFromDateFormat = sdfFrom.format(d);
        } catch (ParseException ex) {
            Logger.getLogger(AccountTransactionsReport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sqliteFromDateFormat;
    }
}
