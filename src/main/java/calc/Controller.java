package calc;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.sql.*;
import java.util.List;

public class Controller {

    @FXML
    public Button iBtn;
    public Button iBtnHide;
    public TabPane iTabPane;
    public BorderPane iAscPane;
    public Tab iTabSym;
    public Tab iTabArhive;
    public Tab iTabPriznak;
    public Tab iTabStudent;
    public Tab iTabUsers;

    public void initialize() {

        iTabSym.setOnSelectionChanged(event -> {  // Пришли на этот таб
            System.out.println("setOnSelectionChanged " + event);
            Tab t;
            t = (Tab) event.getSource();
            Label ll = (Label) t.getGraphic();
            if (t.isSelected()) {  //Говорит тот, к кому пришел фокус
                System.out.println("setOnSelectionChanged " + event + " - " + ll.getText());
            }
        });
        iTabArhive.setOnSelectionChanged(event -> {  //
            Tab t;
            t = (Tab) event.getSource();
            Label ll = (Label) t.getGraphic();
            if (t.isSelected()) {  //Говорит тот, к кому пришел фокус
                System.out.println("iTabArhive setOnSelectionChanged " + t.isSelected() + " - " + t.getText() + " - " + ll.isDisable());
                if (ll.isDisable()) { // Нет прав - ничего не делаем
                    System.out.println("Нету прав");

                    event.consume();
                }
            }
        });
        iTabPriznak.setOnSelectionChanged(event -> {  //
            Tab t;
            t = (Tab) event.getSource();
            if (t.isSelected()) {  //Говорит тот, к кому пришел фокус
                System.out.println("iCVintra setOnSelectionChanged " + t.isSelected() + " - " + t.getText());
            }

        });


        // Ловим переходы на скрытые вкладки чтобы спросить пароль
 /*       iTabPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            System.out.println("iTabPane "  + " - " + event);
            if (event.getTarget().getClass().getName().compareTo("Label") == 0) {  // Заголовок таб-вкладки - Label
           //     if ()
                iAscPane.setVisible(true);
            }

        });
*/

    }


    public void iBtHideAction(ActionEvent actionEvent) {
        System.out.println("aaaaaaaa");
        //iCVintra.
    }


    public void iBtLoginLogin(ActionEvent actionEvent) {  // Авторизация - входим
        System.out.println("Login - ok");
        iAscPane.setVisible(false);
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
