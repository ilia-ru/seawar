package seawar;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.net.URI;
import java.nio.file.Paths;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Морской бой");

        try {  // Значок в шапке программы
            URI localUrl = Paths.get("image\\favicon.png").toAbsolutePath().toUri();
            primaryStage.getIcons().add(new Image(localUrl.toString()));
        } catch (Exception e) {
            System.out.println("Не найден файл favicon.png");
        }

        primaryStage.setScene(new Scene(root, 850, 550));
        primaryStage.show();
        SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null) {
            splash.close();
        }
    }


    public static void main(String[] args) {
        //  System.out.println("main1 ");


        launch(args);

        //  System.out.println("main2 ");
    }
}
