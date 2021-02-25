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

    final int USERS_ACCESS_ADMIN = 1;   // Код статуса админа
    final int USERS_ACCESS_COMMON = 0;  //  Код статуса бесправного пользователя

    TableView<User> tableUsers = null;; // Таблица для показа на экране
    ObservableList<User> obsUSR;// СПисок для TableView - список расчетов
    private User currentUser;   // Текущий пользователь
    private User tmpUser;       // Временный пользователь

    public Users() {
        super();
        // Таблица
        this.tableUsers = new TableView<User>();
        TableViewDecorate(); // оформили внешний вид
        currentUser = new User();
    }

    // Возвращает временного пользователя
    public User getTmpUser() {
        return this.tmpUser;
    }

    // Сохраняем данные в временного пользователя
    public void setTmpUser(User user) {
        this.tmpUser = user;
    }

    // Очищает данные временного пользователя
    public void clearTmpUser() {
        this.tmpUser = new User();
    }


    // Возвращает текущего пользователя
    public User getCurrentUser() {
        return this.currentUser;
    }

    // Сохраняем данные в текущего пользователя
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // Очищает данные текущего пользователя
    public void clearCurrentUser() {
        this.currentUser = new User();
    }

    // Авторизация
    public boolean auth(String login, String password) {
        for (User u: obsUSR) {  // Ищем такого в списке
            if ((u.getLogin().compareTo(login) == 0) && (u.getPassword().compareTo(password) == 0)) {
                this.currentUser = u;
                return true;  // Авторизация успешно
            }
        }
        return false;  // враг - прогоняем
    }

    public TableView<User> getTableUsers() {
        return tableUsers;
    }

    public class User extends HBox {  // Одна строка - один user
        private Long pid;
        private String name;
        private String lastName;
        private String login;
        private String password;
        private String accessString;  // Это для дальнейшего развития. Пока int
        private int access;
        private ImageView IV; // Кнопка "Удалить"
        private HBox actionHb;

        public User() {  // Пустой пользователь. Еще никто не авторизовался
            this.pid = -1l;
            this.access = USERS_ACCESS_COMMON;  // До авторизации эмулируем user без прав
            this.actionHb = new HBox();  // Панель кнопок
            this.IV = new ImageView(new Image("del.png"));
            this.IV.setFitWidth(15);
            this.IV.setFitHeight(15);
            this.actionHb.getChildren().add(IV);
        }

        public User(Long pid, String name, String lastName, String login, String password, String access) {
            this.pid = pid;
            this.name = name;
            this.lastName = lastName;
            this.login = login;
            this.password = password;
            if (access.compareTo("1") == 0) {
                this.access = USERS_ACCESS_ADMIN;  // Админ
            } else {
                this.access = USERS_ACCESS_COMMON;  // user
            }
            this.actionHb = new HBox();  // Панель кнопок
            this.IV = new ImageView(new Image("del.png"));
            this.IV.setFitWidth(15);
            this.IV.setFitHeight(15);
            this.actionHb.getChildren().add(IV);
        }

        // Заполняем панель для показа в строке названия модуля - с кнопкой выхода
        public void getHiUserPane(HBox hb) {
            Label ll = new Label("Здравствуйте,");
            ll.getStyleClass().clear();
            ll.getStyleClass().add("hi-user-pane");
            hb.getChildren().add(ll);

            if (getLastName().compareTo("") <= 0) {
                ll = new Label(getName());
            } else {
                ll = new Label(getName() + " " + getLastName());
            }
            ll.getStyleClass().clear();
            ll.getStyleClass().add("hi-user-pane");
            hb.getChildren().add(ll);

            ImageView i = new ImageView(new Image("exit.png"));
            i.setFitWidth(30);
            i.setFitHeight(30);
            i.setOnMouseClicked((new EventHandler<MouseEvent>() { // Выход
                public void handle(MouseEvent event) {  //
                    if (event.getButton().name().equals("PRIMARY"))  // по левой кнопке мыши
                    {
                        hb.getChildren().clear();
                        hb.setVisible(false);
                        clearCurrentUser();
                    }
                }
            }));

            hb.getChildren().add(i);
        }

        // Спрашиваем, можно ли зайти в раздел текущему пользователю
        public boolean canComeIn(int moduleAccessMode) {
            if (this.getAccess() >= moduleAccessMode) {  // Авторизован и прав хватает
                return true;
            }
            return false;
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
        public int getAccess() {
            return access;
        }
        public void setAccess(int access) {
            this.access = access;
        }
        public HBox getActionHb() {
            return actionHb;
        }
        public void setActionHb(HBox actionHb) {
            this.actionHb = actionHb;
        }
    }

    // Сохраняем добавленного или изменненого пользователя
    public void saveUser(String name, String lastName, String login, String password) {
        if (getTmpUser().getPid() >= 0) { // Измененный
            getTmpUser().setName(name);
            getTmpUser().setLastName(lastName);
            getTmpUser().setLogin(login);
            getTmpUser().setPassword(password);
            String q = "UPDATE PUBLIC.PUBLIC.USERS SET NAME='" + name + "'" +
                    ", LASTNAME='" + lastName + "'"+
                    ", LOGIN='" + login + "'"+
                    ", PASSWORD='" + password + "' WHERE ID=" + getTmpUser().getPid() + ";";
           // System.out.println(q);
            this.ksqlUPDATE(q);  // Изменяем признак в БД
        } else {  // Добавленный
            // ACCESS = "1" всем, т.к. в списке только админы
            String q = "INSERT INTO PUBLIC.PUBLIC.USERS (NAME, LASTNAME, LOGIN, PASSWORD, ACCESS) VALUES('" + name + "', '" +
                    lastName + "', '" + login + "', '" + password + "', '1');";
            System.out.println(q);
            long id = this.ksqlINSERT(q);  // Кладем в БД
            obsUSR.add(new User(id, name, lastName, login, password, "1"));
        }
    }

    // Формирует внешний вид таблицы - список users
    public void TableViewDecorate() {

/*        TableColumn<User, Long> idCol //
                = new TableColumn<>("id");
        idCol.setCellValueFactory(new PropertyValueFactory<>("pid"));
*/
        TableColumn<User, String> nameCol //
                = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(100);

        TableColumn<User, String> lastNameCol //
                = new TableColumn<>("Фамилия");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(160);

        TableColumn<User, String> loginCol //
                = new TableColumn<>("Логин");
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));
        loginCol.setPrefWidth(65);

        TableColumn<User, String> passwordCol //
                = new TableColumn<>("Пароль");
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordCol.setPrefWidth(65);

/*        TableColumn<User, String> accessdCol //
                = new TableColumn<>("Доступ");
        accessdCol.setCellValueFactory(new PropertyValueFactory<>("access"));
//        nameCol.setPrefWidth(150);
*/
        TableColumn actionCol = //
                new TableColumn<>("");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("actionHb"));
        actionCol.setMaxWidth(20);

//        this.tableUsers.getColumns().addAll(idCol, nameCol, lastNameCol, loginCol, passwordCol, accessdCol, actionCol);
        this.tableUsers.getColumns().addAll(nameCol, lastNameCol, loginCol, passwordCol, actionCol);
        this.tableUsers.setOnMouseClicked((new EventHandler<MouseEvent>() { // Удаление пользователя
            public void handle(MouseEvent event) {  //
                if (event.getButton().name().equals("PRIMARY"))  // по левой кнопке мыши
                {
                    // Убедимся, что клик по кнопке
                    if (event.getTarget().getClass().getName().indexOf("ImageView") >= 0) {
                        if (obsUSR.size() <=1 ) {  // Последнего не удаляем. Иначе как авторизоваться потом
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Нелья удалять последнего пользователя. После этого невозможно будет авторизоваться. Сначала создайте другого пользователя.");
                            alert.setTitle("Внимание");
                            alert.setHeaderText("Нелья удалять последнего пользователя");
                            alert.show();
                            return;
                        }
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
                            ksqlDELETE(q);
                            obsUSR.remove(tableUsers.getFocusModel().getFocusedItem());
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