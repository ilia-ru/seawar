package calc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Users extends KSQL {

    TableView<User> tableUsers = null;; // Таблица для показа на экране
    ObservableList<User> obsUSR;  // СПисок для TableView - список расчетов

    public Users() {
        super();
        // Таблица
        this.tableUsers = new TableView<User>();
        TableViewDecorate(); // оформили внешний вид
    }

    public TableView<User> getTableArc() {
        return tableUsers;
    }


    public class User extends HBox {  // Одна строка - один user
        private Long pid;
        private String name;
        private String lastName;
        private String login;
        private String password;
        private String access;
        private ImageView IV; // Кнопка "Удалить"
        private HBox actionHb;

        public User(Long pid, String name, String lastName, String login, String password, String access) {
            this.pid = pid;
            this.name = name;
            this.lastName = lastName;
            this.login = login;
            this.password = password;
            this.access = access;
            this.actionHb = new HBox();  // Панель кнопок
            this.IV = new ImageView(new Image("del.png"));
            this.IV.setFitWidth(15);
            this.IV.setFitHeight(15);
            this.actionHb.getChildren().add(IV);
        }

        public Long getPid() {
            return pid;
        }
        public void setPid(Long pid) {
            this.pid = pid;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public String getLogin() {
            return login;
        }
        public void setLogin(String login) {
            this.login = login;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
       }
        public String getAccess() {
            return access;
        }
        public void setAccess(String access) {
            this.access = access;
        }
        public HBox getActionHb() {
            return actionHb;
        }
        public void setActionHb(HBox actionHb) {
            this.actionHb = actionHb;
        }
    }

    // Формирует внешний вид таблицы - список users
    public void TableViewDecorate() {

        TableColumn<User, Long> idCol //
                = new TableColumn<>("id");
        idCol.setCellValueFactory(new PropertyValueFactory<>("pid"));

        TableColumn<User, String> nameCol //
                = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
//        nameCol.setPrefWidth(150);

        TableColumn<User, String> lastNameCol //
                = new TableColumn<>("Фамилия");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
//        nameCol.setPrefWidth(150);

        TableColumn<User, String> loginCol //
                = new TableColumn<>("Логин");
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));
//        nameCol.setPrefWidth(150);

        TableColumn<User, String> passwordCol //
                = new TableColumn<>("Пароль");
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
//        nameCol.setPrefWidth(150);

        TableColumn<User, String> accessdCol //
                = new TableColumn<>("Доступ");
        accessdCol.setCellValueFactory(new PropertyValueFactory<>("access"));
//        nameCol.setPrefWidth(150);

        TableColumn actionCol = //
                new TableColumn<>("");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("actionHb"));
        actionCol.setMaxWidth(20);

        this.tableUsers.getColumns().addAll(idCol, nameCol, lastNameCol, loginCol, passwordCol, accessdCol, actionCol);
        this.tableUsers.setOnMouseClicked((new EventHandler<MouseEvent>() { // Удаление расчета
            public void handle(MouseEvent event) {  //
                if (event.getButton().name().equals("PRIMARY"))  // по левой кнопке мыши
                {
                    // Убедимся, что клик по кнопке
                    if (event.getTarget().getClass().getName().indexOf("ImageView") >= 0) {
//                        Student.MyImageView i = (Student.MyImageView) event.getTarget();
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                                "Информация о пользователе будет удалена из базы данных " +
                                        "БЕЗ ВОЗМОЖНОСТИ ВОССТАНОВЛЕНИЯ");
                        alert.setTitle("Удаление данных");
                        alert.setHeaderText("Подтвердите удаление пользователя");
                        alert.getButtonTypes().clear();
                        alert.getButtonTypes().addAll(new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE),
                                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && (result.get().getButtonData().name() == "OK_DONE")) {
                            // Удаляем признаки этого расчета
                            String q = "DELETE FROM PUBLIC.PUBLIC.USERS WHERE ID=" + tableUsers.getFocusModel().getFocusedItem().getPid() + ";";
                            System.out.println(q);
//                            ksqlDELETE(q);
//                            obsUSR.remove(tableUsers.getFocusModel().getFocusedItem());
                        }
                    }
                }
            }
        }));
    }

    public TableView getUsersFromSQL(String where) { // Создает из БД список users
        ResultSet rs = this.ksqlSELECT("SELECT * FROM PUBLIC.PUBLIC.USERS ORDER BY NAME");
        List<User> pr = new ArrayList<>();
        if (rs != null) {
            try {
                while (rs.next()) {
    //     public User(Long pid, String name, String lastName, String login, String password, String access) {
                        pr.add(new User(rs.getLong("ID"), rs.getString("NAME"),
                            rs.getString("LASTNAME"), rs.getString("LOGIN"),
                            rs.getString("PASSWORD"), rs.getString("ACCESS")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        obsUSR = FXCollections.observableArrayList(pr);
        tableUsers.setItems(obsUSR);
        return tableUsers;
    }

    }