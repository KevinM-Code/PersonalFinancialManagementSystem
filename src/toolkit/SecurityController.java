/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package toolkit;

import controller.MainGUIController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Level;

/**
 * This Class handles all the security features of the P.F.M.S.
 *
 * @author Kevin Mock
 */
public class SecurityController {

    public static final String PATH_TO_DEFAULT_DB = System.getProperty("user.home")
            + File.separator + "PersFinManSysDir_Do_Not_Delete" + File.separator + "DefaultPersFinManSysDb.db";
    private static final String PATH_TO_ZIPPED_DB_FILE = System.getProperty("user.home")
            + File.separator + "PersFinManSysDir_Do_Not_Delete" + File.separator + "DefaultPersFinManSysDb.zip";
    private static final String DECRYPT_KEY_FILE = System.getProperty("user.home")
            + File.separator + "Data_Do_Not_Delete" + File.separator + "file.txt";
    public static final String PATH_TO_DEFAULT_FOLDER = System.getProperty("user.home")
            + File.separator + "PersFinManSysDir_Do_Not_Delete";
    public static final String PATH_TO_DECRYPT_KEY_FOLDER = System.getProperty("user.home")
            + File.separator + "Data_Do_Not_Delete";
    public static final String PATH_TO_LOG_FILE = System.getProperty("user.home")
            + File.separator + "Data_Do_Not_Delete" + File.separator + "Log.txt";

    public static final File DEFAULT_DB = new File(PATH_TO_DEFAULT_DB);
    public static final File DB_ZIP_FILE = new File(PATH_TO_ZIPPED_DB_FILE);
    public static final File LOG_FILE = new File(PATH_TO_LOG_FILE);

    private static String encryptedPassword;
    private static String salt = "";

    public static String password;

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Generate a random alphanumeric string of a certain length.
     *
     * @param length
     * @return
     */
    public static String generateString(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return builder.toString();
    }

    /**
     * Encrypt a string value.
     *
     * @param key
     * @param initVector
     * @param value
     * @return
     */
    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SecurityController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Decrypt a string value.
     *
     * @param key
     * @param initVector
     * @param encrypted
     * @return
     */
    public static String decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(SecurityController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * This will decrypt the zip file with the database in it by reading a file
     * for an encrypted password, decrypting the password from the text file,
     * then decrypting the zip file using the decrypted password.
     *
     * @throws FileNotFoundException
     */
    public static void decryptDatabase() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(DECRYPT_KEY_FILE));
        List<String> lines = new ArrayList<>();
        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }

        String[] arr = lines.toArray(new String[0]);

        for (int i = 0; i < 2; i++) {
            if (arr[0].equals(arr[i])) {
                encryptedPassword = arr[i];
            }
            if (arr[1].equals(arr[i])) {
                salt = arr[i];
            }
        }

        String key = salt.substring(0, Math.min(salt.length(), 16));
        String iv = salt.substring(16, Math.min(salt.length(), 32));

        String passwordForDecryption = decrypt(key, iv, encryptedPassword);

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(PATH_TO_ZIPPED_DB_FILE);

            if (zipFile.isEncrypted()) {
                zipFile.setPassword(passwordForDecryption);
            }

            zipFile.extractAll(PATH_TO_DEFAULT_FOLDER);
        } catch (ZipException ex) {
            java.util.logging.Logger.getLogger(SecurityController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (DB_ZIP_FILE.exists()) {
            DB_ZIP_FILE.delete();
        }

    }

    /**
     * This method will encrypt the database in an encrypted zip file. First, it
     * will put the database file in a zip file encrypted with an alphanumeric
     * password. Next the alphanumeric password is encrypted and stored in a
     * hidden text file.
     */
    public static void encyptDatabase() {

        try {
            ZipFile zipFile = new ZipFile(PATH_TO_ZIPPED_DB_FILE);

            ArrayList<File> filesToAdd = new ArrayList<>();
            filesToAdd.add(new File(PATH_TO_DEFAULT_DB));

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            //Set the encryption flag to true
            parameters.setEncryptFiles(true);

            //Set the encryption method to AES Zip Encryption
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

            password = generateString(15);

            //Set password
            parameters.setPassword(password);

            //Now add files to the zip file
            zipFile.addFiles(filesToAdd, parameters);

            if (DEFAULT_DB.exists()) {
                DEFAULT_DB.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(DECRYPT_KEY_FILE, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(SecurityController.class.getName()).log(Level.SEVERE, null, ex);
        }

        String key = generateString(16);
        String iv = generateString(16);
        String encryptPassword = encrypt(key, iv, password);
        writer.println(encryptPassword);
        writer.println(key + iv);
        writer.close();
    }

    /**
     * This method is called when a user closes a window. It puts the database
     * file in an encrypted zip file.
     */
    public static void windowCloseEvent() {
        if (!DB_ZIP_FILE.exists()) {
            encyptDatabase();
        }
        if (DEFAULT_DB.exists()) {
            DEFAULT_DB.deleteOnExit();
        }
        Platform.exit();
    }

    /**
     * This method shows a dialog and informs the user of any failed login
     * attempts that have been made on their account. If there were failed login
     * attempts each will be timestamped with a date and time.
     *
     * @throws FileNotFoundException
     */
    public void failedLoginAttemptsDialog() throws FileNotFoundException {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Account Security");
        alert.setHeaderText("Login Attempts On Your Account");
        alert.setContentText("Please review this information.");

        /* Store each line in a list */
        Scanner sc = new Scanner(LOG_FILE);
        List<String> lines = new ArrayList<>();
        while (sc.hasNextLine()) {
            String logLine = sc.nextLine();
            lines.add(logLine);
        }
        sc.close();

        StringBuilder attempts = new StringBuilder();
        lines.forEach((line) -> {
            boolean hasCurrentUserEmail = line.contains(MainGUIController.email);
            if (hasCurrentUserEmail) {
                attempts.append(line).append("\n");
            }
        });

        Label label = new Label("Login attempts:");
        TextArea textArea = new TextArea();
        textArea.setText(attempts.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();

        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        PrintWriter writer = new PrintWriter(LOG_FILE);
        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        ButtonType delLogs = new ButtonType("Delete Logs and Close");
        ButtonType ok = new ButtonType("Save and Close", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(delLogs, ok);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == delLogs) {
            lines.forEach((line) -> {
                boolean hasCurrentUserEmail = line.contains(MainGUIController.email);
                if (!hasCurrentUserEmail) {
                    writer.println(line);
                }
            });
            writer.close();
        } else {
            lines.forEach((line) -> {
                writer.println(line);
            });
            writer.close();
        }
    }
}
