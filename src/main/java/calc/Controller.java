package calc;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;

import java.sql.*;
import java.util.List;

public class Controller {

    @FXML
    public Button iBtn;
    public Button iBtnHide;
    public TabPane iTabPane;
    public Tab iCVintra;
    public Tab iTabArhive;
    public Tab iTabSym;

    public void initialize() {
        iTabSym.setOnSelectionChanged(event -> {  //
            System.out.println("setOnSelectionChanged");

        });
        iTabSym.setOnCloseRequest(event -> {  //
            System.out.println("setOnCloseRequest");

        });

        iTabPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {  // Ловим переходы на скрытые вкладки
            System.out.println("iTabPane " + event);

        });

    }


    public void iBtHideAction(ActionEvent actionEvent) {
        System.out.println("aaaaaaaa");
        //iCVintra.
    }


    public void iBtLoginLogin(ActionEvent actionEvent) {  // Авторизация - входим
        System.out.println("Login - ok");
        //iCVintra.
    }

    public void iBtLoginCancel(ActionEvent actionEvent) {  // Авторизация - Отказ
        System.out.println("Login - Cancel");
        //iCVintra.
    }

    public void iBtnAction(ActionEvent actionEvent) {
        System.out.println("aaaaaaaa");

        //String aaa = new String();
        //aaa.matches();

        Connection conn = null;
        try {
            conn = DriverManager
                    .getConnection("jdbc:hsqldb:file:/d:/_temp/_2/cached;ifexists=true",
                            "user", "111");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        PreparedStatement selectStatement = null;
        try {
            selectStatement = conn.prepareStatement("select * from \"PUBLIC\".\"USERS\"");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ResultSet rs = null;
        try {
            rs = selectStatement.executeQuery();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //       List<User> users = new ArrayList<>();

/*            while (true) {
                try {
                    if (!rs.next()) break;
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } // will traverse through all rows
*/
        try {
//            rs.next();
            rs.next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        Integer id = null;
        String name = null;
        try {
            id = rs.getInt("id");
            name = rs.getString("name");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
//                String lastName = rs.getString("last_name");

        //              User user = new User(id, firstName, lastName);
        //            users.add(user);
        System.out.println("Кнопка " + id + " " + name);

    }




}
