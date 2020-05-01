package edu.wpi.N.views.admin;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import edu.wpi.N.algorithms.FuzzySearchAlgorithm;
import edu.wpi.N.database.DBException;
import edu.wpi.N.database.DoctorDB;
import edu.wpi.N.database.LoginDB;
import edu.wpi.N.database.ServiceDB;
import edu.wpi.N.entities.DbNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.LinkedList;

public class AccountHandleController {

    @FXML JFXTextField txtf_adminuser;
    @FXML JFXTextField txtf_adminpass;
    @FXML JFXTextField txtf_cpoldpass;
    @FXML JFXTextField txtf_cpnewpass;
    @FXML JFXTextField txtf_cpuser;
    @FXML JFXTextField txtf_rmuser;

    public void addAdmin() throws DBException {
        try {

            if (txtf_adminuser.getText().equals("") | txtf_adminpass.getText().equals("")) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setContentText("Empty Username or Password");
                errorAlert.show();

                return;
            }

            LoginDB.createAdminLogin(txtf_adminuser.getText(), txtf_adminpass.getText());

            Alert acceptReq = new Alert(Alert.AlertType.CONFIRMATION);
            acceptReq.setContentText("Admin " + txtf_adminuser.getText() + " was added.");
            acceptReq.show();

        } catch (DBException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText(e.getMessage());
            errorAlert.show();
        }

        txtf_adminuser.clear();
        txtf_adminpass.clear();
    }

    public void changeAccountPass() throws DBException {
        try {
            if (txtf_cpnewpass.getText().equals(txtf_cpoldpass.getText()))
                throw new DBException("New password same as old password");
            LoginDB.changePass(txtf_cpuser.getText(), txtf_cpoldpass.getText(), txtf_cpnewpass.getText());

            Alert acceptReq = new Alert(Alert.AlertType.CONFIRMATION);
            acceptReq.setContentText("Password Changed");
            acceptReq.show();

        } catch (DBException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText(e.getMessage());
            errorAlert.show();
        }

        txtf_cpnewpass.clear();
        txtf_cpoldpass.clear();
        txtf_cpuser.clear();
    }

    public void removeLogin() throws DBException {
        try {

            if (txtf_rmuser.getText().equals("")) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setContentText("Invalid Username or Input");
                errorAlert.show();

                return;
            }
            LoginDB.removeLogin(txtf_rmuser.getText());
        } catch (DBException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText(e.getMessage());
            errorAlert.show();
        }

        Alert acceptReq = new Alert(Alert.AlertType.CONFIRMATION);
        acceptReq.setContentText("Login " + txtf_rmuser.getText() + " was removed.");
        acceptReq.show();

        txtf_rmuser.clear();
    }

}
