package calc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.List;

public class Main extends Application {

    public static Connection connSQL = null;

    public static Priznaki priznaki = null;  // Признаки во всех видах


    @Override
    public void start(Stage primaryStage) throws Exception{
/*        List<String> params = getParameters().getRaw();
        System.out.println("params.get(0) " + params.size());
*/
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Калькулятор");
        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.getScene().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
