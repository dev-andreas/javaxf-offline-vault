package com.andreas.main;

import com.andreas.main.stages.loginStage.LoginStage;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Andreas Gerasimow.
 */
public final class App extends Application {

    /**
     * This method starts the program.
     */
    @Override
    public void start(Stage stage) throws Exception {

        stage = new LoginStage(this);
        stage.show();
    }
}
