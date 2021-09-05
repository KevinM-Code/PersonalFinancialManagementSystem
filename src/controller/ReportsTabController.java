/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.FormValidation;
import toolkit.DatabaseController;
import com.jfoenix.controls.JFXDatePicker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DateCell;
import javafx.scene.control.Label;
import net.sf.jasperreports.view.JasperViewer;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class renders the pie chart of bills, miscellaneous expenses and initiates the
 * "create a report" event on button click.
 *
 * @author Kevin Mock
 */
public class ReportsTabController implements Initializable {

    private final ObservableList<PieChart.Data> expenseObservableList = FXCollections.observableArrayList();
    @FXML
    private PieChart pieChart;
    @FXML
    private JFXDatePicker reportFromDatePicker;
    @FXML
    private JFXDatePicker reportToDatePicker;
    @FXML
    private Label errFromDateLabel;
    @FXML
    private Label errToDateLabel;

    private AccountTransactionsReport accountTransactionsReport;

    /**
     * This is an accessor method used in {@link AccountTransactionsReport#buildReport()} and {@link AccountTransactionsReport#dataList()}
     *
     * @return
     */
    JFXDatePicker getReportFromDatePicker() {
        return reportFromDatePicker;
    }

    /**
     * This is an accessor method used in {@link AccountTransactionsReport#buildReport()} and {@link AccountTransactionsReport#dataList()}
     *
     * @return
     */
    JFXDatePicker getReportToDatePicker() {
        return reportToDatePicker;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /**
     * This method waits for the user to select a date in the "from" DatePicker
     * before enabling the "to" DatePicker. Once the date is selected it enables
     * the "to" DatePicker and only allows the user to chose dates pass the date
     * selected in the "from" DatePicker.
     *
     * @param event
     */
    @FXML
    private void enableToDatePickerOnAction(ActionEvent event) {
        if (reportToDatePicker.isDisabled()) {
            reportToDatePicker.setDisable(false);
            if (reportFromDatePicker.getValue() != null) {
                reportToDatePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        LocalDate fromDate = reportFromDatePicker.getValue();

                        setDisable(empty || date.compareTo(fromDate) < 0);
                    }
                });
            }
        }
    }

    /**
     * This method will gather the balances from all the accounts that are Bills
     * and Miscellaneous Expenses from the database. With this information a
     * pie chart of expenses will be rendered.
     *
     * @param event
     */
    @FXML
    void renderPieChartOnAction(ActionEvent event) {

        String institution;
        String accountType;
        String balance;
        String totalPayments;
        if (!expenseObservableList.isEmpty()) {
            expenseObservableList.clear();
        }

        /* Get the accounts that are expenses and bills for the pie chart */
        String selectQuery = "SELECT institution, account_type, balance, total_payments "
                + "FROM accounts "
                + "WHERE (account_type LIKE 'Bill') AND "
                + "user_id = " + MainGUIController.getCurrentUserId();
        try (Connection conn = DatabaseController.dBConnector()) {
            if (conn != null) {
                PreparedStatement prepStmt = conn.prepareStatement(selectQuery);
                ResultSet resultSet = prepStmt.executeQuery();

                while (resultSet.next()) {
                    institution = resultSet.getString("institution");
                    accountType = resultSet.getString("account_type");
                    balance = resultSet.getString("balance");
                    totalPayments = resultSet.getString("total_payments");              

                    if (accountType.equals("Bill")) {
                        double balanceDouble = Double.parseDouble(totalPayments);
                        expenseObservableList.add(new PieChart.Data(institution + "-" + accountType, balanceDouble));
                    } else {
                        double balanceDouble = Double.parseDouble(balance);
                        expenseObservableList.add(new PieChart.Data(institution + "-" + accountType, balanceDouble));
                    }
                }

                /* Render the pie chart */
                pieChart.setData(expenseObservableList);
                pieChart.setTitle("Pie Chart of Bill Payments");
                pieChart.setLegendSide(Side.BOTTOM);
                pieChart.setLabelsVisible(true);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ReportsTabController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method will gather all the Accounts and Transactions and display
     * them in a list report format grouped by account, and within the accounts they will be grouped as to whether they are
     * a outflow or inflow transaction.
     *
     * @param event
     */
    @FXML
    void showReportOnAction(ActionEvent event) throws Exception {

        boolean fromDateBool = FormValidation.dateFieldNotEmpty(reportFromDatePicker, errFromDateLabel, "From Date Is Required");
        boolean toDateBool = FormValidation.dateFieldNotEmpty(reportToDatePicker, errToDateLabel, "To Date Is Required");

        /* Make sure both dates are filled in */
        if (fromDateBool && toDateBool) {
            AccountTransactionsReport report = new AccountTransactionsReport(this);
            /* See if there were any transactions that were between the dates selected by the user
             * if not display a message that there were no transactions found */
            if (report.dataList() != null) {
                report.runReport();
                JasperViewer.viewReport(report.jp, false);
            } else {
                errToDateLabel.setText("NO TRANSACTIONS FOUND!!");
            }
        }
    }
}
