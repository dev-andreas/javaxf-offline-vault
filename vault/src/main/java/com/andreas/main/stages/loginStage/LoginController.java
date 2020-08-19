package com.andreas.main.stages.loginStage;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import com.andreas.main.app.AppController;
import com.andreas.main.cryptography.RSA;
import com.andreas.main.save.Save;
import com.andreas.main.stages.createKeyStage.CreateKeyStage;
import com.andreas.main.stages.newSaveStage.NewSaveStage;
import com.andreas.main.stages.removeSaveStage.RemoveSaveStage;
import com.andreas.main.stages.renameSaveStage.RenameSaveStage;
import com.andreas.main.stages.saveStage.SaveStage;

import org.jdom2.Element;

import java.io.File;
import java.security.KeyPair;
import java.util.Arrays;

import com.andreas.main.Utils;

public class LoginController extends AppController {

    @FXML
    public PasswordField password;

    @FXML
    public Label saveName;

    @FXML
    public ListView<String> savesList;

    @FXML
    public TextField keyPath;

    @FXML
    public Label keyNotification;

    @FXML
    public Label notification;

    private int index;

    @Override
    public void init() {

        ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = new MenuItem("Create new");
        MenuItem item2 = new MenuItem("Delete");
        MenuItem item3 = new MenuItem("Rename");

        item1.setOnAction(e -> {
            addSave();
        });

        item2.setOnAction(e -> {
            deleteSave();
        });

        item3.setOnAction(e -> {
            renameSave();
        });

        contextMenu.getItems().addAll(item1, item2, item3);
        savesList.setContextMenu(contextMenu);
        setEditable();

        keyPath.setText(readKeyPath());
    }

    public void addSavePressed(MouseEvent event) {
        addSave();
    }

    public void openPressed(MouseEvent event) {

        Save save = new Save();
        save.readFromFile("data/saves/" + ((LoginStage) stage).getSaves().get(index).getId() + ".xml");

        if (!keyPathReadable()) {
            notification.setText("Key file not found!");
            return;
        }

        if (passwordMatches(save, password.getText())) {

            save.open(keyPath.getText());
            if (!Arrays.equals(save.getPublicKey().getEncoded(),
                    RSA.readKeyPair(keyPath.getText()).getPublic().getEncoded())) {
                notification.setText("Selected key file is not compatible!");
                return;
            }

            SaveStage saveStage = new SaveStage(this.stage.getApp(), save);

            ButtonType openAnyway = new ButtonType("Open anyway");
            ButtonType removed = new ButtonType("I removed it");
            Alert alert = new Alert(AlertType.WARNING, "Please remove your storage medium in which the key is located.", 
            openAnyway, removed);
            alert.showAndWait();

            if (alert.getResult() == openAnyway) {
                stage.close();
                saveStage.show();
            }

            if (alert.getResult() == removed) {

                if (keyPathReadable()) {
                    notification.setText("Storage medium not removed!");
                    return;
                }
                saveStage.show();
                stage.close();
            }
        } else {
            notification.setText("Password is incorrect!");
        }
    }

    public void savePressed(MouseEvent event) {

        index = savesList.getSelectionModel().getSelectedIndex();
        if (index < 0) return;

        saveName.setText(savesList.getSelectionModel().getSelectedItem());

        setEditable();
    }

    public void browsePressed(MouseEvent event) {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(null);

        if (file != null) {
            String path = file.getAbsolutePath();
            keyPath.setText(path);
            keyNotification.setText("");

            if (!keyPathReadable()) {
                keyPath.setText("");
                keyNotification.setText("File doesn't contain the necessary data.");
            } else 
                saveKeyPath(path);
        }
    }

    public void addKeyPressed(MouseEvent event) {
        CreateKeyStage stage = new CreateKeyStage(this.stage.getApp());
        stage.show();
    }

    public boolean passwordMatches(Save save, String password) {

        if (RSA.verify(password, save.getPasswordCertificate(), save.getPublicKey()))
            return true;
        return false;
    }

    private void addSave() {
        NewSaveStage stage = new NewSaveStage(this.stage.getApp(), (LoginStage)this.stage);
        stage.show();
    }

    private void deleteSave() {
        RemoveSaveStage stage = new RemoveSaveStage(this.stage.getApp(), (LoginStage)this.stage);
        stage.show();
    }

    public void renameSave() {
        RenameSaveStage stage = new RenameSaveStage(this.stage.getApp(), (LoginStage)this.stage);
        stage.show();
    }

    public void setEditable() {
        password.setEditable(savesList.getItems().size() > 0);
    }

    private void saveKeyPath(String keyPath) {
        Element root = Utils.readXmlFile("data/saves.xml");
        root.getChild("keyPath").setText(keyPath);
        Utils.createXmlFile("data/saves.xml", root);
    }

    private String readKeyPath() {
        Element root = Utils.readXmlFile("data/saves.xml");
        return root.getChildText("keyPath");
    }


    private boolean keyPathReadable() {
        try {
            KeyPair kp = RSA.readKeyPair(keyPath.getText());
            return !(kp.getPrivate() == null || kp.getPublic() == null);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean nameExists(String name) {
        for (String n : savesList.getItems())
            if (n.equals(name))
                return true;
        return false;
    }
}