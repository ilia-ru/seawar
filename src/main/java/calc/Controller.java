package calc;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;

import static calc.Main.priznaki;

public class Controller {

    @FXML
    public Button iBtn;
    public Button iBtnHide;
    public BorderPane iAscPane;
    public MenuBar iMainMenu;
//    public TableView iTblPrizn;
    public Pane iTblPane;
    public Pane iListPane;
    public HBox iHBoxMenu;
    public VBox iCalcQEPane;
    public VBox iStudentPane;
    public VBox iPriznakiPane;
    public StackPane iStackPane;



    private class TopMenu {
        private MenuBar menu;

        public TopMenu() {
            menu = new MenuBar();

            Label ll = new Label("Симуляционный калькулятор");
            ll.setOnMouseClicked(event -> {
//                System.out.println("Симуляционный калькулятор " + event);
                //iCalcQEPane.setVisible(false);
                iCalcQEPane.toFront();
            });
            Menu mi = new Menu("", ll);
            menu.getMenus().add(mi);

            ll = new Label("CVintra");
            ll.setOnMouseClicked(event -> {
                System.out.println("CVintra " + event);
            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);

            ll = new Label("Расчет доверительных интервалов");
            ll.setOnMouseClicked(event -> {
                System.out.println("Расчет доверительных интервалов " + event);
            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);

            ll = new Label("Архив");
            ll.setOnMouseClicked(event -> {
                System.out.println("Архив " + event);
            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);

            ll = new Label("Признаки");
            ll.setOnMouseClicked(event -> {
 //               System.out.println("Признаки " + event);
                iPriznakiPane.toFront();
            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);

            ll = new Label("Критерии Стьюдента");
            ll.setOnMouseClicked(event -> {
                System.out.println("Критерии Стьюдента " + event);
                iStudentPane.toFront();

            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);

            ll = new Label("Пользователи");
            ll.setOnMouseClicked(event -> {
                System.out.println("Пользователи " + event);
            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);

        }

        public MenuBar getMenu() {
            return menu;
        };
    }

    public void initialize() {
        TopMenu t = new TopMenu();
        iHBoxMenu.getChildren().add(t.getMenu());

        priznaki = new Priznaki();
        priznaki.createFromSQL("");
        ListView lv =  new ListView();
        lv.setItems(priznaki.getListEQ());  // Список признаков для calc_eq
        lv.setPrefWidth(450);
        iTblPane.getChildren().add(0, lv);

        ListView lvEdit = new ListView();
        lvEdit.setItems(priznaki.getListPR());  // Список признаков для редактирвоания признаков
        lvEdit.setPrefWidth(450);
        iListPane.getChildren().add(0, lvEdit);


        iCalcQEPane.toFront();  // Первое окно - симуляц кальк

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

    // Расчет Симуляц. калькулятора
    public void iBtnCalcEqGoAction(ActionEvent actionEvent) {
        System.out.println("aaaaaaaa");
        ObservableList<Priznaki.PriznakEQ> ol = priznaki.getList();
        System.out.println("222222 " + ol);
        for (Priznaki.PriznakEQ pr: ol) {
            System.out.println("ПРизнак " + pr.getName() + " - " + pr.getBalls());


        }


    }

    public void iBtnAction(ActionEvent actionEvent) {
        System.out.println("aaaaaaaa");

        //String aaa = new String();
        //aaa.matches();

        Connection conn = null;
        try {
            conn = DriverManager
                    .getConnection("jdbc:hsqldb:file:/d:\\_work\\ilia\\_java\\calc_eq\\src\\main\\db\\cached;ifexists=true",
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
