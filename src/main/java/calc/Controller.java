package calc;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    public ImageView iImgAlgoritm;
    public Label iSumBalls2;
    public Label iSumBalls4;
    public Label iResTxt2_80;
    public Label iResTxt2_90;
    public Label iResTxt4_80;
    public Label iResTxt4_90;
    public TextField iPrPriznakName;
    public TextField iPrFastFrom;
    public TextField iPrFastTo;
    public TextField iPrFastCount;
    public HBox iPrIntervalsList;



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

 //       iImgAlgoritm.setImage(new Image("eq_img_error.png"));

        iPriznakiPane.setVisible(true);
        iCalcQEPane.setVisible(true);
        iCalcQEPane.toFront();  // Первое окно - симуляц кальк
//        iPriznakiPane.toFront();

    }


    public void iBtHideAction(ActionEvent actionEvent) {
        System.out.println("aaaaaaaa");
        //iCVintra.
    }

    // Быстрое создание признака
    public void iBtFastCreatePriznakAction(ActionEvent actionEvent) {
        Priznaki.PMapItem pm = priznaki.addPriznakToMap(iPrPriznakName.getText(), Double.valueOf(iPrFastFrom.getText()),
                Double.valueOf(iPrFastTo.getText()), Integer.valueOf(iPrFastCount.getText()));
        ListView lvPI = new ListView();
        lvPI.setItems(pm.getListPI());  // Список признаков для редактирвоания признаков
        lvPI.setPrefWidth(450);
        iPrIntervalsList.getChildren().clear();
        iPrIntervalsList.getChildren().add(0, lvPI);
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
        //     System.out.println("aaaaaaaa");
        Integer sumBalls = priznaki.calcBalls2(true);
        iSumBalls2.setText(""); // Очищаем поле
        if (sumBalls != null) {
            iSumBalls2.setText(String.valueOf(sumBalls));
//        =ЕСЛИ(AI5<=-6;"ЛП НЕэквивалентные";ЕСЛИ(AI5>=3;"ЛП эквивалентные";"НЕ ИНФОРМАТИВНО"))
            if (sumBalls <= -6) {
                iResTxt2_80.setText("ЛП НЕэквивалентные");
                iResTxt2_80.setStyle("-fx-background-color: #f8a75b; -fx-text-fill: red;");
            } else if (sumBalls >= 3) {
                iResTxt2_80.setText("ЛП эквивалентные");
                iResTxt2_80.setStyle("-fx-background-color: #75ff9c; -fx-text-fill: black;");
            } else {
                iResTxt2_80.setText("НЕ ИНФОРМАТИВНО");
                iResTxt2_80.setStyle("-fx-background-color: #dddddd; -fx-text-fill: black;");
            }
//=ЕСЛИ(AI5<=-6;"ЛП НЕэквивалентные";ЕСЛИ(AI5>=5;"ЛП эквивалентные";"НЕ ИНФОРМАТИВНО"))
            if (sumBalls <= -6) {
                iResTxt2_90.setText("ЛП НЕэквивалентные");
                iResTxt2_90.setStyle("-fx-background-color: #f8a75b; -fx-text-fill: red;");
            } else if (sumBalls >= 5) {
                iResTxt2_90.setText("ЛП эквивалентные");
                iResTxt2_90.setStyle("-fx-background-color: #75ff9c; -fx-text-fill: black;");
            } else {
                iResTxt2_90.setText("НЕ ИНФОРМАТИВНО");
                iResTxt2_90.setStyle("-fx-background-color: #dddddd; -fx-text-fill: black;");
            }
        }

        sumBalls = priznaki.calcBalls4();
        iSumBalls4.setText(""); // Очищаем поле
        if (sumBalls != null) {
            iSumBalls4.setText(String.valueOf(sumBalls));
//=ЕСЛИ(AM5<=-6;"ЛП НЕэквивалентные";ЕСЛИ(AM5>=3;"ЛП эквивалентные";"НЕ ИНФОРМАТИВНО"))
            if (sumBalls <= -6) {
                iResTxt4_80.setText("ЛП НЕэквивалентные");
                iResTxt4_80.setStyle("-fx-background-color: #f8a75b; -fx-text-fill: red;");
            } else if (sumBalls >= 3) {
                iResTxt4_80.setText("ЛП эквивалентные");
                iResTxt4_80.setStyle("-fx-background-color: #75ff9c; -fx-text-fill: black;");
            } else {
                iResTxt4_80.setText("НЕ ИНФОРМАТИВНО");
                iResTxt4_80.setStyle("-fx-background-color: #dddddd; -fx-text-fill: black;");
            }
//=ЕСЛИ(AM5<=-6;"ЛП НЕэквивалентные";ЕСЛИ(AM5>=5;"ЛП эквивалентные";"НЕ ИНФОРМАТИВНО"))
            if (sumBalls <= -6) {
                iResTxt4_90.setText("ЛП НЕэквивалентные");
                iResTxt4_90.setStyle("-fx-background-color: #f8a75b; -fx-text-fill: red;");
            } else if (sumBalls >= 5) {
                iResTxt4_90.setText("ЛП эквивалентные");
                iResTxt4_90.setStyle("-fx-background-color: #75ff9c; -fx-text-fill: black;");
            } else {
                iResTxt4_90.setText("НЕ ИНФОРМАТИВНО");
                iResTxt4_90.setStyle("-fx-background-color: #dddddd; -fx-text-fill: black;");
            }
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

    public void iBtnAlg1Action(ActionEvent actionEvent) {
        iImgAlgoritm.setImage(new Image("alg1.png"));
    }

    public void iBtnAlg2Action(ActionEvent actionEvent) {
        iImgAlgoritm.setImage(new Image("alg2.png"));
    }

    public void iBtnAlg3Action(ActionEvent actionEvent) {
        iImgAlgoritm.setImage(new Image("alg3.png"));
    }

    public void iBtnAlg4Action(ActionEvent actionEvent) {
        iImgAlgoritm.setImage(new Image("alg4.png"));
    }


}
