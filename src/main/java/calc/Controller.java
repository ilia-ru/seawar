package calc;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static calc.Main.priznaki;

public class Controller {

    @FXML
    public Button iBtn;
    public Button iBtnHide;
    public VBox iVBCalcSave;
    public TextField iCalcNameToSave;
    public MenuBar iMainMenu;
    //    public TableView iTblPrizn;
    public HBox iTblPane;
    public BorderPane iAuthorPane;
    public Pane iFonPane;
    public Pane iListPane;
    public HBox iHBoxMenu;
    public HBox iListArCalcPane;
    public VBox iCalcQEPane;
    public VBox iCalcQEArcPane;
    public VBox iStudentPane;
    public VBox iPriznakiPane;
    public StackPane iStackPane;
    public ImageView iImgAlgoritm;
    public Label iSumBalls;
    public Label iCalcName;
    public Label iCalcDate;
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
    public VBox iListPRArc;
    public Label iPrEditCaption;
    public Button iBtnPrNewSave;
    public ImageView iImgAlgoritmArc;
    public Label iArcDesign;
    public Label iArcBalls;
    public Label iArcPowerVal;
    public Label iArcPower;
    public VBox iUsersPane;
    public HBox iUsersTable;
    public Label iUserEditId;
    public Label iUserEditCaption;
    public Label iMsgNoUserFound;
    public TextField iUserEditName;
    public TextField iUserEditLastName;
    public TextField iUserEditLogin;
    public TextField iUserEditPassword;
    public DatePicker iArcDateFrom;
    public DatePicker iArcDateTo;
    public VBox iArcViewPane;
    public RadioButton iPrTypeDecimal;
    public RadioButton iPrTypeLogical;
    public ToggleGroup radioPrType;
    public VBox iPrFastCreatePane;
    public Button iBtIntervalAdd;
    public TextField iLogin;
    public PasswordField iPassword;
    public HBox iHiUserPane;
    public Label iModuleCaption;
    public VBox iUsersEditPane;


//    public VBox iPrCreateEditDecimalPane;
//    public VBox iPrCreateEditLogicalPane;

    ListView lvEQ;      // Для EQ
    ListView lvPR;      // Для PR
    ListView lvPI;      // Для интервалов
    ListView lvPRArc;   // Для архива
    TableView tableCalcs; // Список сохраненных расчетов
    CalcArc calcArc;
    TableView tableUsers; // Список пользователей
    Users users;          // Список пользователей. Там-же и текущий автооризованный, если есть
    int moduleAccessMode; // У каждого модуля свой режим. 0 - общий доступ, 1 - только админ.

    public final long NOT_IN_PMAP = -1l;   // В pMapItem храним ID для разных нужнд. Для тех, кого нет в БД - это значение

    private class TopMenu {
        private MenuBar menu;

        public TopMenu() {
            menu = new MenuBar();

            Label ll = new Label("Симуляционный калькулятор");
            ll.setOnMouseClicked(event -> {
                moduleAccessMode = 0; // Общий модуль
                if (!users.getCurrentUser().canComeIn(moduleAccessMode)) {  // Хватает прав на раздел?
                    iFonPane.toFront(); // Авторизуемся
                    iFonPane.setVisible(true);
                    iAuthorPane.toFront();
                    iAuthorPane.setVisible(true);
                    return;
                }
                iModuleCaption.setText("Симуляционный калькулятор");
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
//                iListArCalcPane.setVisible(true);
                moduleAccessMode = 1; // Закрытый модуль
                if (!users.getCurrentUser().canComeIn(moduleAccessMode)) {  // Хватает прав на раздел?
                    iFonPane.toFront(); // Авторизуемся
                    iFonPane.setVisible(true);
                    iAuthorPane.toFront();
                    iAuthorPane.setVisible(true);
                    return;
                }
                iModuleCaption.setText("Архив");
                tableCalcs = calcArc.getCalcsFromSQL("");
                iCalcQEArcPane.toFront();
                tableCalcs.requestFocus();
            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);

            ll = new Label("Признаки");
            ll.setOnMouseClicked(event -> {
                //               System.out.println("Признаки " + event);
                moduleAccessMode = 1; // Закрытый модуль
                if (!users.getCurrentUser().canComeIn(moduleAccessMode)) {  // Хватает прав на раздел?
                    iFonPane.toFront(); // Авторизуемся
                    iFonPane.setVisible(true);
                    iAuthorPane.toFront();
                    iAuthorPane.setVisible(true);
                    return;
                }
                iModuleCaption.setText("Признаки");
                lvPR.getFocusModel().focusNext();
                lvPR.getFocusModel().focusPrevious();
                iPriznakiPane.toFront();
            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);

            ll = new Label("Критерии Стьюдента");
            ll.setOnMouseClicked(event -> {
                moduleAccessMode = 1; // Закрытый модуль
                if (!users.getCurrentUser().canComeIn(moduleAccessMode)) {  // Хватает прав на раздел?
                    iFonPane.toFront(); // Авторизуемся
                    iFonPane.setVisible(true);
                    iAuthorPane.toFront();
                    iAuthorPane.setVisible(true);
                    return;
                }
                iModuleCaption.setText("Критерии Стьюдента");
                iStudentPane.toFront();

            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);

            ll = new Label("Пользователи");
            ll.setOnMouseClicked(event -> {
                moduleAccessMode = 1; // Закрытый модуль
                if (!users.getCurrentUser().canComeIn(moduleAccessMode)) {  // Хватает прав на раздел?
                    iFonPane.toFront(); // Авторизуемся
                    iFonPane.setVisible(true);
                    iAuthorPane.toFront();
                    iAuthorPane.setVisible(true);
                    return;
                }
                iModuleCaption.setText("Пользователи");
                tableUsers.requestFocus();
//                tableUsers.getFocusModel().focus(tableUsers.getFocusModel().getFocusedIndex());

                Users.User u = (Users.User) tableUsers.getFocusModel().focusedItemProperty().get(); // Первый в списке
                if (u != null) {
                    users.setTmpUser(u);
                    iUserEditName.setText(users.getTmpUser().getName());
                    iUserEditLastName.setText(users.getTmpUser().getLastName());
                    iUserEditLogin.setText(users.getTmpUser().getLogin());
                    iUserEditPassword.setText(users.getTmpUser().getPassword());
                    iUserEditCaption.setText("Редактирование данных пользователя");
                    iUsersEditPane.setVisible(true);
                }
                iUsersPane.toFront();
            });
            mi = new Menu("", ll);
            menu.getMenus().add(mi);
        }

        public MenuBar getMenu() {
            return menu;
        }
    }

    public void initialize() {
        TopMenu t = new TopMenu();
        iHBoxMenu.getChildren().add(t.getMenu());

        // При выходе пользователя, переводим на открытый раздел
        iHiUserPane.visibleProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue != null && !newValue) {
                iModuleCaption.setText("Симуляционный калькулятор");
                iCalcQEPane.toFront();  // Первое окно - симуляц кальк
            }
        });

        //  Убираем красную надпись в авторизации
        iAuthorPane.visibleProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue != null && newValue) {
                iMsgNoUserFound.setVisible(false);
            }
        });
        // Форматтеры для панели быстрого ввода
        // iPrFastFrom
        UnaryOperator<TextFormatter.Change> iPrFastFromFilter = change -> {
            String text = change.getText();
            if (text.compareTo(",") == 0) {
                text = ".";
                change.setText(".");
            }
            if (text.matches("[0-9.-]*")) {
                if ((text.compareTo(".") == 0) && iPrFastFrom.getText().contains(".")) {
                    return null;
                }  // вторую точку вводят
                return change;
            }
            return null;
        };
        TextFormatter<String> iPrFastFromFormatter = new TextFormatter<>(iPrFastFromFilter);
        iPrFastFrom.setTextFormatter(iPrFastFromFormatter);
        iPrFastFrom.setAlignment(Pos.CENTER_RIGHT);
        // iPrFastTo
        UnaryOperator<TextFormatter.Change> iPrFastToFilter = change -> {
            String text = change.getText();
            if (text.compareTo(",") == 0) {
                text = ".";
                change.setText(".");
            }
            if (text.matches("[0-9.-]*")) {
                if ((text.compareTo(".") == 0) && iPrFastTo.getText().contains(".")) {
                    return null;
                }  // вторую точку вводят
                return change;
            }
            return null;
        };
        TextFormatter<String> iPrFastToFormatter = new TextFormatter<>(iPrFastToFilter);
        iPrFastTo.setTextFormatter(iPrFastToFormatter);
        iPrFastTo.setAlignment(Pos.CENTER_RIGHT);
        // iPrFastCount
        UnaryOperator<TextFormatter.Change> iPrFastCountFilter = change -> {
            String text = change.getText();
            if (text.compareTo(",") == 0) { text = "."; change.setText(".");}
            if (text.matches("[0-9-]*")) {  // integer
                return change;
            }
            return null;
        };
        TextFormatter<String> iPrFastCountFormatter = new TextFormatter<>(iPrFastCountFilter);
        iPrFastCount.setTextFormatter(iPrFastCountFormatter);
        iPrFastCount.setAlignment(Pos.CENTER_RIGHT);

        priznaki = new Priznaki();
        priznaki.createFromSQL("");
        lvEQ = new ListView();
        lvEQ.setItems(priznaki.getListEQ());  // Список признаков для calc_eq
        lvEQ.setPrefWidth(490);
        lvEQ.setPrefHeight(600);
        iTblPane.getChildren().add(0, lvEQ);
        iTblPane.setPrefHeight(600);

        iImgAlgoritm.setOnMouseClicked(event -> {  // Разворачиваем в отдельное окно
            if (iImgAlgoritm.getImage() == null) { return; }  // Нет картинки, не нужно окно

            Stage window = new Stage();
            window.setTitle("Алгоритм принятия решений");
            VBox pane = new VBox();
            pane.setAlignment(Pos.CENTER);
            pane.setPadding(new Insets(10.0));

            ImageView img = new ImageView(iImgAlgoritm.getImage());
            img.setPreserveRatio(true);
            img.setFitWidth(900);
            pane.getChildren().add(img);
            pane.setSpacing(15.0);
            pane.setStyle("-fx-background-color: white;");

            Button button = new Button("Закрыть окно с рисунком");
            button.setOnAction(e -> {
                window.close();
            });
            pane.getChildren().add(button);

            Scene scene = new Scene(pane, 1000, 600);
            window.setScene(scene);

//            window.setX(pane.getScene().getWindow().getX()+50);
//            window.setY(pane.getScene().getWindow().getY()-50);
            window.show();
        });

        lvPR = new ListView();
        lvPR.setItems(priznaki.getListPR());  // Список признаков для редактирования признаков
        lvPR.getFocusModel().focusedItemProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue != null) { //
                clearPrEditPane();
                iPrEditCaption.setText("Редактирование признака");
                iBtnPrNewSave.setText("Сохранить изменения");
                Priznaki.PriznakPR p = (Priznaki.PriznakPR) newValue;
                Priznaki.PMapItem pm = priznaki.copyToPMapTmp(p.getPid());
                iPrPriznakName.setText(pm.getName());
                lvPI.setItems(pm.getListPI());
                iPrIntervalsList.getChildren().clear();
                iPrIntervalsList.getChildren().add(0, lvPI);
                // Тип признака 1 - числовой, 2 - логический
                if (pm.getType() == 1) {  // Radio
                    iPrTypeDecimal.setSelected(true);
                } else {
                    iPrTypeLogical.setSelected(true);
                }
                // Тип признака менять не даем
                iPrTypeLogical.setDisable(true);
                iPrTypeDecimal.setDisable(true);

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

        // Архив расчетов
        calcArc = new CalcArc();
 //       CalcArc.getTableArc().setItems();  // Список расчетов
        tableCalcs = calcArc.getCalcsFromSQL("");
        tableCalcs.setPrefWidth(285);
        tableCalcs.setMaxWidth(285);
        tableCalcs.setPrefHeight(1000);
        tableCalcs.getFocusModel().focusedItemProperty().addListener((obj, oldValue, newValue) -> {
 //           System.out.println("lll" + obj + " " + oldValue + " " + newValue);
            if (newValue != null) { // Пока фокус не ушел
                CalcArc.CalcRecord c = (CalcArc.CalcRecord) newValue;
                iCalcName.setText(c.getName());

                SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy");
                iCalcDate.setText(formater.format(c.getData().getTime()));

                lvPRArc = new ListView();
                lvPRArc.setItems(calcArc.getPRFromSQL(c.getPid()));  // Список признаков со значениями
                lvPRArc.setMaxWidth(350);
                lvPRArc.setPrefHeight(1000);
                lvPRArc.setMaxHeight(1000);
                iListPRArc.getChildren().clear();
                iListPRArc.getChildren().add(lvPRArc);
                iListPRArc.setPrefHeight(1000);

                iImgAlgoritmArc.setImage(new Image("alg"+c.getALG()+".png"));

                iArcDesign.setText(c.getDESIGN());
                iArcPower.setText(c.getPOWER());
                iArcPowerVal.setText(c.getPOWER_VAL());
                iArcPowerVal.setStyle(c.getPOWER_VAL_STYLE());
                iArcBalls.setText(c.getBALLS().toString());
                iArcViewPane.setVisible(true);
            }
        });
        iArcViewPane.setVisible(false);
        iListArCalcPane.getChildren().add(0,tableCalcs);

        // Пользователи
        users = new Users();
        //       CalcArc.getTableArc().setItems();  // Список расчетов
        tableUsers = users.getUsersFromSQL("");
        tableUsers.setEditable(true);
//        tableUsers.setPrefWidth(285);
//        tableUsers.setMaxWidth(285);
        tableUsers.setPrefHeight(1000);
        // По смене фокуса заполняем правое окно
        tableUsers.getFocusModel().focusedItemProperty().addListener((obj, oldValue, newValue) -> {
//                       System.out.println("lll" + obj + " " + oldValue + " " + newValue);
            if (newValue != null) {
                users.setTmpUser((Users.User) newValue);
                iUserEditName.setText(users.getTmpUser().getName());
                iUserEditLastName.setText(users.getTmpUser().getLastName());
                iUserEditLogin.setText(users.getTmpUser().getLogin());
                iUserEditPassword.setText(users.getTmpUser().getPassword());
                iUserEditCaption.setText("Редактирование данных пользователя");
                iUsersEditPane.setVisible(true);
            }
        });
        iUsersTable.getChildren().add(0,tableUsers);
        iUsersEditPane.setVisible(false);


// Алгоритм в отдельное окно
        iImgAlgoritmArc.setOnMouseClicked(event -> {  // Разворачиваем в отдельное окно
   //         System.out.println("цштв");
            Stage window = new Stage();
            window.setTitle("Алгоритм принятия решений");
            VBox pane = new VBox();
            pane.setAlignment(Pos.CENTER);
            pane.setPadding(new Insets(10.0));

            ImageView img = new ImageView(iImgAlgoritmArc.getImage());
            img.setPreserveRatio(true);
            img.setFitWidth(900);
            pane.getChildren().add(img);
            pane.setSpacing(15.0);
            pane.setStyle("-fx-background-color: white;");

            Button button = new Button("Закрыть окно с рисунком");
            button.setOnAction(e -> {
                window.close();
            });
            pane.getChildren().add(button);

            Scene scene = new Scene(pane, 1000, 600);
            window.setScene(scene);
            window.show();
        });

        // Тип признака - показ панели ввода признака- логический
        iPrTypeLogical.selectedProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue != null) { //
                iPrFastFrom.setDisable(true);
                iPrFastTo.setDisable(true);
                iPrFastCount.setDisable(true);
                iBtIntervalAdd.setDisable(true);
                priznaki.getPMapTmp().setType(2);  // Логический
            }
        });

        // Тип признака - показ панели ввода признака- числовой
        iPrTypeDecimal.selectedProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue != null) { //
                //iPrFastCreatePane.setDisable(false);
                iPrFastFrom.setDisable(false);
                iPrFastTo.setDisable(false);
                iPrFastCount.setDisable(false);
                iBtIntervalAdd.setDisable(false);
                priznaki.getPMapTmp().setType(1);  // Decimal
           }
        });

        iModuleCaption.setText("Симуляционный калькулятор");

        iCalcQEPane.toFront();  // Первое окно - симуляц кальк

//        iListArCalcPane.setVisible(true);
//        iListArCalcPane.toFront();

    }


    // Создание нового пользователя
    public void iBtUserNewAction(ActionEvent actionEvent) {
        users.clearCurrentUser();
        users.clearTmpUser();
        iUserEditName.setText("");
        iUserEditLastName.setText("");
        iUserEditLogin.setText("");
        iUserEditPassword.setText("");
        iUserEditCaption.setText("Добавление нового пользователя");
        iUsersEditPane.setVisible(true);
    }

    // Быстрое создание интервалов
    public void iBtFastCreatePriznakAction(ActionEvent actionEvent) {
            if (iPrFastFrom.getText().compareTo("") == 0) {
                iPrFastFrom.setText("0");
            }
            if (iPrFastTo.getText().compareTo("") == 0) {
                iPrFastTo.setText("0");
            }
            if (iPrFastCount.getText().compareTo("") == 0) {
                iPrFastCount.setText("0");
            }

        if (iPrTypeDecimal.isSelected()) {
            if (iPrFastFrom.getText().compareTo("") == 0 || iPrFastTo.getText().compareTo("") == 0 ||
                    iPrFastCount.getText().compareTo("") == 0 ||
                    Double.valueOf(iPrFastFrom.getText()) >= Double.valueOf(iPrFastTo.getText()) ||
                    Integer.valueOf(iPrFastCount.getText()) <= 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Условия на данные: \n1. Введите \"Min\", \"Max\" и \"Кол-во\"\n" +
                        "2. Должно быть: Min < Max, Кол-во > 0\n" +
                        "Введите корректные данные");
                alert.setTitle("Внимание");
                alert.setHeaderText("Некорректно введены данные");
                alert.show();
                return;
            }
        }
        Priznaki.PMapItem pm = priznaki.getPMapTmp();
        pm.fastIntervalFill(Double.valueOf(iPrFastFrom.getText()),
                Double.valueOf(iPrFastTo.getText()), Integer.valueOf(iPrFastCount.getText()));
        //    lvPI = new ListView();
        lvPI.setItems(pm.getListPI());  // Список
        iPrIntervalsList.getChildren().clear();
        iPrIntervalsList.getChildren().add(0, lvPI);

    }

    // Архив расчетов - включение фильтра
    public void iBtnArcFilterAction(ActionEvent actionEvent) {
        calcArc.setFilterDateFrom(iArcDateFrom.getValue());
        calcArc.setFilterDateTo(iArcDateTo.getValue());
        tableCalcs = calcArc.getCalcsFromSQL("");
        iArcViewPane.setVisible(false);
    }

    // Архив расчетов - Отключение фильтра
    public void iBtnArcFilterCancelAction(ActionEvent actionEvent) {
        calcArc.setFilterDateFrom(null);
        calcArc.setFilterDateTo(null);
        tableCalcs = calcArc.getCalcsFromSQL("");
        iArcViewPane.setVisible(false);
    }

    // Признаки - добавление нового интервала
    public void iBtIntervalAddAction(ActionEvent actionEvent) {
        priznaki.getPMapTmp().addInterval();
    }


    // Сохранение нового признака или после редактирования
    public void iBtnPrNewSaveAction(ActionEvent actionEvent) {
        if (iPrPriznakName.getText().compareTo("") == 0) { // Не введено название признака
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Введите название признака");
            alert.setTitle("Внимание");
            alert.setHeaderText("Введите название признака");
            alert.show();
        } else if (lvPI.getItems().size() < 2) { // Интервалы не введены
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
                priznaki.addPriznak(priznaki.getPMapTmp(), opr);  // Пишем в мапу и БД
            } else {  // Изменяем
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
        Priznaki.PMapItem pm = priznaki.newPMapTmp("", 1);
        // Чистим поля перед открытием
        clearPrEditPane();
        iPrEditCaption.setText("Создание нового признака");
        iBtnPrNewSave.setText("Сохранить новый признак");
        iPrTypeDecimal.setSelected(true);
        iPrCreateEditPane.setVisible(true);
        iPrTypeLogical.setDisable(false);
        iPrTypeDecimal.setDisable(false);
        iPrTypeDecimal.setSelected(true);
        pm.setType(1); // decimal
    }

    // Сохраняем изменения после редактирования/добавления пользователя
    public void iBtUserSaveAction(ActionEvent actionEvent) {
        users.saveUser(iUserEditName.getText(),
            iUserEditLastName.getText(),
            iUserEditLogin.getText(),
            iUserEditPassword.getText());
        tableUsers.refresh();
        iUsersEditPane.setVisible(false);
    }

    // Отмена редактирования/добавления пользователя
    public void iBtUserSaveCancelAction(ActionEvent actionEvent) {
        iUsersEditPane.setVisible(false);
    }

    // Авторизация - входим
    public void iBtLoginLogin(ActionEvent actionEvent) {
        //System.out.println("Login - ok");
        if (users.auth(iLogin.getText(), iPassword.getText())) {
            iFonPane.setVisible(false);  // Авторизовались
            iAuthorPane.setVisible(false);
            iMsgNoUserFound.setVisible(false);
            iHiUserPane.setVisible(true);
            users.getCurrentUser().getHiUserPane(iHiUserPane);
        } else {
            iMsgNoUserFound.setVisible(true);
        }
    }

    public void iBtLoginCancel(ActionEvent actionEvent) {  // Авторизация - Отказ
        iFonPane.setVisible(false);
        iAuthorPane.setVisible(false);
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
