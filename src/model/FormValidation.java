/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * This class is used when validating if the text boxes are filled in or not for various forms in the P.F.M.S
 *
 * @author Kevin Mock
 */
public class FormValidation {

    /**
     * Will return a boolean value, whether the value in the JFXTextField is not null or not empty.
     * If textField is not empty it returns true.
     *
     * @param i
     * @return see description
     */
    private static boolean textFieldNotEmpty(JFXTextField i) {
        boolean r = false;
        if (i.getText() != null && !i.getText().isEmpty()) {
            r = true;
        }
        return r;
    }

    /**
     * This will return a boolean value, if above {@link #textFieldNotEmpty(JFXTextField)} is true then no text is assigned to the errorLabel
     * in the UI
     *
     * @param i
     * @param l
     * @param sValidText the error message
     * @return see description
     */
    public static boolean textFieldNotEmpty(JFXTextField i, Label l, String sValidText) {
        boolean r = true;
        String c = null;
        if (!textFieldNotEmpty(i)) {
            r = false;
            c = sValidText;
        }
        l.setText(c);
        return r;
    }

    /**
     * Will return a boolean value, whether the value in the JFXDatePicker is not null or not empty.
     * If JFXDatePicker is not empty it returns true.
     *
     * @param i
     * @return see description
     */
    private static boolean dateFieldNotEmpty(JFXDatePicker i) {
        boolean r = false;
        if (((TextField) i.getEditor()).getText() != null && !((TextField) i.getEditor()).getText().isEmpty()) {
            r = true;
        }
        return r;
    }

    /**
     * This will return a boolean value, if above {@link #dateFieldNotEmpty(JFXDatePicker)} is true then no text is assigned to the errorLabel
     * in the UI.
     *
     * @param i
     * @param l
     * @param sValidText
     * @return see description
     */
    public static boolean dateFieldNotEmpty(JFXDatePicker i, Label l, String sValidText) {
        boolean r = true;
        String c = null;
        if (!dateFieldNotEmpty(i)) {
            r = false;
            c = sValidText;
        }
        l.setText(c);
        return r;
    }

    /**
     * Will return a boolean value, whether the value in the JFXPasswordField is equal to the other JFXPasswordField,
     * not null or not empty. If JFXDatePicker is not empty and the password fields are the same it returns true.
     *
     * @param i
     * @param x
     * @return see description
     */
    private static boolean passwordFieldsNotEqual(JFXPasswordField i, JFXPasswordField x) {
        boolean r = false;
        if (i.getText() != null && !i.getText().isEmpty() && (i.getText().equals(x.getText()))) {
            r = true;
        }
        return r;
    }

    /**
     * This will return a boolean value, if above {@link #passwordFieldsNotEqual(JFXPasswordField, JFXPasswordField)} is true then no text is assigned to the
     * errorLabel in the UI.
     *
     * @param x
     * @param i
     * @param l
     * @param sValidText
     * @return see description
     */
    public static boolean passwordFieldsNotEqual(JFXPasswordField x, JFXPasswordField i, Label l, String sValidText) {
        boolean r = true;
        String c = null;
        if (!passwordFieldsNotEqual(i, x)) {
            r = false;
            c = sValidText;
        }
        l.setText(c);
        return r;
    }

    /**
     * This will return a boolean value, whether two emails are the same.
     *
     * @param i
     * @param x
     * @return see description
     */
    private static boolean emailNotSame(String i, String x) {
        boolean r = false;
        if (!i.equals(x)) {
            r = true;
        }
        return r;
    }

    /**
     * If {@link #emailNotSame(String, String)} is true then no text is assigned to the errorLabel in the UI.
     *
     * @param x
     * @param i
     * @param l
     * @param sValidText
     * @return see description
     */
    public static boolean emailNotSame(String x, String i, Label l, String sValidText) {
        boolean r = true;
        String c = null;
        if (!emailNotSame(i, x)) {
            r = false;
            c = sValidText;
        }
        l.setText(c);
        return r;
    }

    /**
     * Will return a boolean value, whether the value in the JFXPasswordField is not null or not empty.
     * If JFXPasswordField is not empty it returns true.
     *
     * @param i
     * @return see description
     */
    private static boolean passwordFieldNotEmpty(JFXPasswordField i) {
        boolean r = false;
        if (i.getText() != null && !i.getText().isEmpty()) {
            r = true;
        }
        return r;
    }

    /**
     * This will return a boolean value, if above {@link #passwordFieldNotEmpty(JFXPasswordField)} is true then no text is assigned to the
     * errorLabel in the UI.
     *
     * @param i
     * @param l
     * @param sValidText
     * @return see description
     */
    public static boolean passwordFieldNotEmpty(JFXPasswordField i, Label l, String sValidText) {
        boolean r = true;
        String c = null;
        if (!passwordFieldNotEmpty(i)) {
            r = false;
            c = sValidText;
        }
        l.setText(c);
        return r;
    }
}
