package com.andreas.main.stages.renameSaveStage;

import com.andreas.main.app.AppController;
import com.andreas.main.cryptography.RSA;
import com.andreas.main.save.Save;
import com.andreas.main.stages.loginStage.LoginController;
import com.andreas.main.stages.loginStage.LoginStage;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class RenameSaveController extends AppController {

    @FXML
    public TextField newName;

    @FXML
    public PasswordField password;

    @FXML
    public Label notification;

    @FXML
    public Button rename;

    @Override
    public void init() {
        rename.requestFocus();
    }

    public void renamePressed(MouseEvent event) {
        LoginStage stage = ((RenameSaveStage)this.stage).getLoginStage();
        LoginController controller = (LoginController)stage.getController();

        int index = controller.savesList.getSelectionModel().getSelectedIndex();
        
        Save save = new Save();
        save.readFromFile("data/saves/" + stage.getSaves().get(index).getId() + ".xml");

        if (!RSA.verify(password.getText(), save.getPasswordCertificate(), save.getPublicKey())) {
            notification.setText("Passwords do not match!");
            return;
        }

        if (controller.nameExists(newName.getText())) {
            notification.setText("Name already exists!");
            return;
        }

        stage.renameSave(save, index, newName.getText());
        this.stage.close();
        
    }

    public void cancelPressed(MouseEvent event) {
        stage.close();
    }
}