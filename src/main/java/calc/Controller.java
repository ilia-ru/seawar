package calc;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.sql.*;
import java.util.Optional;

import static calc.Main.priznaki;

public class Controller {

    @FXML
    public Button iBtn;
    public Button iBtnHide;
    public VBox iVBCalcSave;
    public TextField iCalcNameToSave;
    public BorderPane iAscPane;
    public MenuBar iMainMenu;
    //    public TableView iTblPrizn;
    public HBox iTblPane;
    public Pane iListPane;
    public HBox iHBoxMenu;
    public VBox iCalcQEPane;
    public VBox iStudentPane;
    public VBox iPriznakiPane;
    public StackPane iStackPane;
    public ImageView iImgAlgoritm;
    public Label iSumBalls;
    public Label iResTxt2_80;
    public Label iResTxt2_90;
    public Label iResTxt4_80;
    public Label iResTxt4_90;
    public RadioButton iPower1;
    public RadioButton iPower2;
    public RadioButton iPower3;
    public RadioButton iPower4;
    public TextField iPrPriznakName;
    public TextField iPrFastFrom;
    public TextField iPrFastTo;
    public TextField iPrFastCount;
    public HBox iPrIntervalsList;
    public VBox iPrCreateEditPane;

    ListView lvEQ;      // Для EQ
    ListView lvPR;      // Для PR
    ListView lvPI;      // Для интервалов

    public final long NOT_IN_PMAP = -1l;   // В pMapItem храним ID для разных нужнд. Для тех, кого нет в БД - это значение

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
        }

        ;
    }

    public void initialize() {
        TopMenu t = new TopMenu();
        iHBoxMenu.getChildren().add(t.getMenu());

        priznaki = new Priznaki();
        priznaki.createFromSQL("");
        lvEQ = new ListView();
        lvEQ.setItems(priznaki.getListEQ());  // Список признаков для calc_eq
        lvEQ.setPrefWidth(450);
        lvEQ.setPrefHeight(600);
        iTblPane.getChildren().add(0, lvEQ);
        iTblPane.setPrefHeight(600);

        lvPR = new ListView();
        lvPR.setItems(priznaki.getListPR());  // Список признаков для редактирвоания признаков
        lvPR.getFocusModel().focusedItemProperty().addListener((obj, oldValue, newValue) -> {
            System.out.println("lll" + obj + " " + oldValue + " " + newValue);
            if (newValue != null) { //
                clearPrEditPane();
                Priznaki.PriznakPR p = (Priznaki.PriznakPR) newValue;
                Priznaki.PMapItem pm = priznaki.copyToPMapTmp(p.getPid());
                iPrPriznakName.setText(pm.getName());
                lvPI.setItems(pm.getListPI());
                iPrIntervalsList.getChildren().clear();
                iPrIntervalsList.getChildren().add(0, lvPI);
                iPrCreateEditPane.setVisible(true);
            }
        });
        lvPR.setPrefWidth(450);
        lvEQ.setPrefHeight(550);
        iListPane.getChildren().add(0, lvPR);
        iListPane.setPrefHeight(600);

        //       iImgAlgoritm.setImage(new Image("eq_img_error.png"));

        iPrCreateEditPane.setVisible(false);  // Чтобы не бросалась в глаза до поры

        lvPI = new ListView();
        lvPI.setPrefWidth(500);

        iPriznakiPane.setVisible(true);
        iCalcQEPane.setVisible(true);
        iCalcQEPane.toFront();  // Первое окно - симуляц кальк
//        iPriznakiPane.toFront();

    }

    public void iBtHideAction(ActionEvent actionEvent) {
        System.out.println("aaaaaaaa");
        //iCVintra.
    }

    // Быстрое создание интервалов
    public void iBtFastCreatePriznakAction(ActionEvent actionEvent) {
        if (iPrFastFrom.getText().compareTo("") == 0) { iPrFastFrom.setText("0"); }
        if (iPrFastTo.getText().compareTo("") == 0) { iPrFastTo.setText("0"); }
        if (iPrFastCount.getText().compareTo("") == 0) { iPrFastCount.setText("10"); }

        if (iPrFastFrom.getText().compareTo("") == 0 || iPrFastTo.getText().compareTo("") == 0 ||
                iPrFastCount.getText().compareTo("") == 0 ||
                Double.valueOf(iPrFastFrom.getText()) >= Double.valueOf(iPrFastTo.getText()) ||
                Integer.valueOf(iPrFastCount.getText()) <=0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Условия на данные: \n1. Введите \"Min\", \"Max\" и \"Кол-во\"\n"+
                    "2. Должно быть: Min < Max, Кол-во > 0\n"+
                    "Введите корректные данные");
            alert.setTitle("Внимание");
            alert.setHeaderText("Некорректно введены данные");
            alert.show();
        } else {
            Priznaki.PMapItem pm = priznaki.getPMapTmp();
            pm.fastIntervalFill(Double.valueOf(iPrFastFrom.getText()),
                    Double.valueOf(iPrFastTo.getText()), Integer.valueOf(iPrFastCount.getText()));
            //    lvPI = new ListView();
            lvPI.setItems(pm.getListPI());  // Список
            iPrIntervalsList.getChildren().clear();
            iPrIntervalsList.getChildren().add(0, lvPI);
        }
    }

    // Сохранение нового признака или после редактирования
    public void iBtnPrNewSaveAction(ActionEvent actionEvent) {
        if (iPrPriznakName.getText().compareTo("") == 0) { // Не введено название признака
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Введите название признака");
            alert.setTitle("Внимание");
            alert.setHeaderText("Введите название признака");
            alert.show();
        } else if (priznaki.getPMapTmp().getPMapIntervals().size() < 2) { // Интервалы не введены
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Интервалы признака не введены, или вввдены неверно");
            alert.setTitle("Внимание");
            alert.setHeaderText("Введите интервалы");
            alert.show();
        } else {  // Все хорошо
            int focus = lvPR.getFocusModel().getFocusedIndex();
            priznaki.getPMapTmp().setName(iPrPriznakName.getText());  // Добавляем имя признака
            // Сохраняем баллы с экрана во временный PMapItem
            ObservableList<Priznaki.PIntervalPR> opr = (ObservableList<Priznaki.PIntervalPR>) lvPI.getItems();
            // Если == NOT_IN_PMAP - значит добавление т.к. нету ID. Если есть ID - то правка
            if (priznaki.getPMapTmp().getTmpId() == NOT_IN_PMAP) {  // Добавляем
                // Сохраняем в мапу и БД
                System.out.println("add");
                priznaki.addPriznak(priznaki.getPMapTmp(), opr);  // Пишем в мапу и БД
            } else {  // Изменяем
                System.out.println("edit");
                priznaki.changePriznak(priznaki.getPMapTmp(), opr);  // Пишем в мапу и БД
            }
            lvPR.setItems(priznaki.getListPR());  // Обновляем список признаков на экране
            lvEQ.setItems(priznaki.getListEQ());      // Обновляем список признаков на экране
            lvPR.getFocusModel().focus(focus);
            lvPR.requestFocus();


//            iPrCreateEditPane.setVisible(false);  // Чтобы не бросалась в глаза до поры
        }
    }

    public void iBtnPRCreateEditCancelAction(ActionEvent actionEvent) {  // Отказ от сохранения
        iPrCreateEditPane.setVisible(false);
    }

    public void clearPrEditPane() {  // Чистим поля в панели
        iPrPriznakName.setText("");
        iPrFastFrom.setText("");
        iPrFastTo.setText("");
        iPrFastCount.setText("");
        lvPI.getItems().clear();
        iPrIntervalsList.getChildren().clear();
        iPrIntervalsList.getChildren().add(0, lvPI);
    }

    public void iBtnPRCreatePR(ActionEvent actionEvent) {  // Создаем новый признак
        Priznaki.PMapItem pm = priznaki.newPMapTmp("");
        // Чистим поля перед открытием
        clearPrEditPane();
        iPrCreateEditPane.setVisible(true);
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
        boolean calc_ok = false;
        //     System.out.println("aaaaaaaa");
        if (!(iPower1.isSelected() || iPower2.isSelected() || iPower3.isSelected() || iPower4.isSelected())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Не введены исходные данные. Введите данные и повторите расчет");
            alert.setTitle("Внимание");
            alert.setHeaderText("Ошибка во входных данных");
            alert.show();
            return;
        }
        iSumBalls.setText(""); // Очищаем поля
        iResTxt2_80.setText("");
        iResTxt2_80.setStyle("");
        iResTxt2_90.setText("");
        iResTxt2_90.setStyle("");
        iResTxt4_80.setText("");
        iResTxt4_80.setStyle("");
        iResTxt4_90.setText("");
        iResTxt4_90.setStyle("");
//        =ЕСЛИ(AI5<=-6;"ЛП НЕэквивалентные";ЕСЛИ(AI5>=3;"ЛП эквивалентные";"НЕ ИНФОРМАТИВНО"))
        if (iPower1.isSelected()) {
            Integer sumBalls = priznaki.calcBalls2(true);
            if (sumBalls != null) {
                calc_ok = true;
                iSumBalls.setText(String.valueOf(sumBalls));
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
                iImgAlgoritm.setImage(new Image("alg1.png"));
            }
        }
//=ЕСЛИ(AI5<=-6;"ЛП НЕэквивалентные";ЕСЛИ(AI5>=5;"ЛП эквивалентные";"НЕ ИНФОРМАТИВНО"))
        if (iPower2.isSelected()) {
            Integer sumBalls = priznaki.calcBalls2(true);
            if (sumBalls != null) {
                calc_ok = true;
                iSumBalls.setText(String.valueOf(sumBalls));
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
                iImgAlgoritm.setImage(new Image("alg2.png"));
            }
        }

        if (iPower3.isSelected()) {
            Integer sumBalls = priznaki.calcBalls4();
            if (sumBalls != null) {
                calc_ok = true;
                iSumBalls.setText(String.valueOf(sumBalls));
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
                iImgAlgoritm.setImage(new Image("alg3.png"));
            }
        }

        if (iPower4.isSelected()) {
            Integer sumBalls = priznaki.calcBalls4();
            if (sumBalls != null) {
//=ЕСЛИ(AM5<=-6;"ЛП НЕэквивалентные";ЕСЛИ(AM5>=5;"ЛП эквивалентные";"НЕ ИНФОРМАТИВНО"))
                calc_ok = true;
                iSumBalls.setText(String.valueOf(sumBalls));
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
                iImgAlgoritm.setImage(new Image("alg4.png"));
            }
        }
        // Включаем панель сохранения данных
        if (calc_ok) {  // Расчет был сделан - выводим панель сохранения
            iVBCalcSave.setVisible(true);
        }
    }

    //Сохраняем расчет в БД
    public void iVBCalcSaveaction(ActionEvent actionEvent) {
        Alert alert;
        if (iCalcNameToSave.getText().compareTo("") == 0) {
            alert = new Alert(Alert.AlertType.INFORMATION, "Введите уникальное название расчета.");
            alert.setTitle("Информация");
            alert.setHeaderText("Не введено название расчета");
            alert.show();
            return;
        }

        alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Сохранение расчета в базу данных");
        alert.setTitle("Сохранение данных");
        alert.setHeaderText("Подтвердите сохранение данных");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && (result.get().getButtonData().name() == "OK_DONE")) {
            System.out.println("Сохраняем");
            String design = "";
            String power = "";
            String power_val = "";
            String power_val_style = "";
            int alg = 0;
            if (iPower1.isSelected()) {
                design = "2x2x2";
                power = "Мощность 80%";
                power_val = iResTxt2_80.getText();
                power_val_style = iResTxt2_80.getStyle(); // Цвет оформления
                alg = 1;
            }
            if (iPower2.isSelected()) {
                design = "2x2x2";
                power = "Мощность 90%";
                power_val = iResTxt2_90.getText();
                power_val_style = iResTxt2_90.getStyle(); // Цвет оформления
                alg = 2;
            }
            if (iPower3.isSelected()) {
                design = "2x2x4";
                power = "Мощность 80%";
                power_val = iResTxt4_80.getText();
                power_val_style = iResTxt4_80.getStyle(); // Цвет оформления
                alg = 3;
            }
            if (iPower4.isSelected()) {
                design = "2x2x4";
                power = "Мощность 90%";
                power_val = iResTxt4_90.getText();
                power_val_style = iResTxt4_90.getStyle(); // Цвет оформления
                alg = 4;
            }


            priznaki.saveСalculation(iCalcNameToSave.getText(), design, power, power_val,
                    power_val_style, Integer.valueOf(iSumBalls.getText()), alg);

            alert = new Alert(Alert.AlertType.INFORMATION, "Расчет сохранен в базу данных. Вы можете просмотреть его на странице \"Архив\"");
            alert.setTitle("Информация");
            alert.setHeaderText("Расчет сохранен в базу данных");
            alert.show();

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
