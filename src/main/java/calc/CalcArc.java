package calc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
//import java.time.LocalDate;

public class CalcArc extends KSQL {

    TableView<CalcRecord> tableArc = null;; // Таблица для показа на экране
    ObservableList<CalcRecord> obsCR;  // СПисок для TableView - список расчетов
    ObservableList<PriznakArc> obsPR;  // СПисок для ListView - список признаков
    private LocalDate filterDateFrom;  // Фильтр - дата с
    private LocalDate filterDateTo;    // Фильтр - дата по

    public CalcArc() {
        super();
        // Таблица
        this.tableArc = new TableView<CalcRecord>();
        TableViewDecorate(); // оформили внешний вид
    }

    public LocalDate getFilterDateFrom() {
        return filterDateFrom;
    }

    public void setFilterDateFrom(LocalDate filterDateFrom) {
        this.filterDateFrom = filterDateFrom;
    }

    public LocalDate getFilterDateTo() {
        return filterDateTo;
    }

    public void setFilterDateTo(LocalDate filterDateTo) {
        this.filterDateTo = filterDateTo;
    }

    public TableView<CalcRecord> getTableArc() {
        return tableArc;
    }


    public class CalcRecord extends HBox {  // Одна строка - один расчет
        private Long pid;
        private java.sql.Date data;
        private String name;
        private ImageView IV; // Кнопка "Удалить"
        private HBox actionHb;
        private String DESIGN;
        private String POWER;
        private String POWER_VAL;
        private String POWER_VAL_STYLE;
        private Integer BALLS;
        private Integer ALG;

        public CalcRecord(Long pid, java.sql.Date data, String name,
                          String DESIGN, String POWER, String POWER_VAL,
                          String POWER_VAL_STYLE, Integer BALLS, Integer ALG) {
            this.pid = pid;
            this.data = data;
            this.name = name;
            this.actionHb = new HBox();  // Панель кнопок
            this.IV = new MyImageView(new Image("del.png"));
            this.IV.setFitWidth(15);
            this.IV.setFitHeight(15);
            this.actionHb.getChildren().add(IV);
            this.DESIGN = DESIGN;
            this.POWER = POWER;
            this.POWER_VAL =POWER_VAL;
            this.POWER_VAL_STYLE = POWER_VAL_STYLE;
            this.BALLS = BALLS;
            this.ALG = ALG;
        }

        public class MyImageView extends ImageView {
            public MyImageView(Image img) {
                super(img);
            }
        }

        public String getDESIGN() {
            return DESIGN;
        }

        public String getPOWER() {
            return POWER;
        }

        public String getPOWER_VAL() {
            return POWER_VAL;
        }

        public String getPOWER_VAL_STYLE() {
            return POWER_VAL_STYLE;
        }

        public Integer getBALLS() {
            return BALLS;
        }

        public Integer getALG() {
            return ALG;
        }

        public Long getPid() {
            return pid;
        }
        public void setPid(Long id) {
            this.pid = id;
        }
        public java.sql.Date getData() {
            return data;
        }
        public void setData(java.sql.Date data) {
            this.data = data;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public ImageView getIV() {
            return IV;
        }
        public void setIV(ImageView IV) {
            this.IV = IV;
        }
        public HBox getActionHb() {
            return actionHb;
        }
        public void setActionHb(HBox actionHb) {
            this.actionHb = actionHb;
        }
    }

    public class PriznakArc extends HBox {  // Одна строка - признак, на странице Признаки
        public PriznakArc(String name, Integer val) {
            //  this.name = name;
            Label ll = new Label(name);
            ll.setPrefWidth(250);
            ll.setMaxWidth(250);
            ll.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().add(ll);

            ll = new Label(val.toString());
            ll.setPrefWidth(50);
            ll.setMaxWidth(50);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            this.setSpacing(5);
            this.setAlignment(Pos.CENTER_LEFT);
//            this.setPadding(new Insets(0, 5, 0, 0));
        }
    }

    // Формирует внешний вид таблицы - список признаков
    public void TableViewDecorate() {

        // Create column UserName (Data type of String).
        TableColumn<CalcRecord, Long> idCol //
                = new TableColumn<>("id");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<CalcRecord, java.sql.Date> dataCol //
                = new TableColumn<>("Дата");
        dataCol.setCellValueFactory(new PropertyValueFactory<>("data"));
        dataCol.setMaxWidth(90);

        TableColumn<CalcRecord, String> nameCol //
                = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn actionCol = //
                new TableColumn<>("");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("actionHb"));
        actionCol.setMaxWidth(20);

        this.tableArc.getColumns().addAll(dataCol, nameCol, actionCol);
        this.tableArc.setOnMouseClicked((new EventHandler<MouseEvent>() { // Удаление расчета
            public void handle(MouseEvent event) {  //
                if (event.getButton().name().equals("PRIMARY"))  // по левой кнопке мыши
                {
                    // Убедимся, что клик по кнопке
                    if (event.getTarget().getClass().getName().indexOf("MyImageView") >= 0) {
//                        Student.MyImageView i = (Student.MyImageView) event.getTarget();
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                                "Информация о выбранном расчете будет удалена из базы данных " +
                                        "БЕЗ ВОЗМОЖНОСТИ ВОССТАНОВЛЕНИЯ");
                        alert.setTitle("Удаление данных");
                        alert.setHeaderText("Подтвердите удаление данных о расчете");
                        alert.getButtonTypes().clear();
                        alert.getButtonTypes().addAll(new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE),
                                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && (result.get().getButtonData().name() == "OK_DONE")) {
                            // Удаляем признаки этого расчета
                            String q = "DELETE FROM PUBLIC.PUBLIC.ARCHIVE_PR WHERE ARC_ID=" + tableArc.getFocusModel().getFocusedItem().getPid() + ";";
                            ksqlDELETE(q);
                            // Удаляем расчет
                            q = "DELETE FROM PUBLIC.PUBLIC.ARCHIVE WHERE ID=" + tableArc.getFocusModel().getFocusedItem().getPid() + ";";
                            ksqlDELETE(q);  // Удаляем признаки в БД
                            obsCR.remove(tableArc.getFocusModel().getFocusedItem());
                        }
                    }
                }
            }
        }));
    }

    public TableView getCalcsFromSQL(String where) { // Создает из БД список расчетов
        String q = "SELECT * FROM PUBLIC.PUBLIC.ARCHIVE";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (!(getFilterDateFrom() == null)) {  // есть От
            q += " WHERE DATA >='" + getFilterDateFrom().format(dtf) + "'";
            if (!(getFilterDateTo() == null)) {  // есть До
                q += " AND DATA<='" + getFilterDateTo().format(dtf) + "'";
            }
        } else {  // нет От
            if (!(getFilterDateTo() == null)) {  // есть До
                q += " WHERE DATA<='" + getFilterDateTo().format(dtf) + "'";
            }
        }
        q += " ORDER BY DATA,NAME";
 //       System.out.println(q);
        ResultSet rs = this.ksqlSELECT(q);
        List<CalcRecord> pr = new ArrayList<>();
        if (rs != null) {
            try {
                while (rs.next()) {
                    pr.add(new CalcRecord(rs.getLong("ID"), rs.getDate("DATA"),
                            rs.getString("NAME"), rs.getString("DESIGN"),
                            rs.getString("POWER"), rs.getString("POWER_VAL"),
                            rs.getString("POWER_VAL_STYLE"), rs.getInt("BALLS"),
                            rs.getInt("ALG")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        obsCR = FXCollections.observableArrayList(pr);
        tableArc.setItems(obsCR);
        return tableArc;
    }

    public ObservableList<PriznakArc> getPRFromSQL(Long arcId) { // Создает из БД список расчетов
        String q = "SELECT * FROM PUBLIC.PUBLIC.ARCHIVE_PR WHERE ARC_ID=" + arcId; // + " ORDER BY NAME";
        ResultSet rs = this.ksqlSELECT(q);
  //      System.out.println(q);
        List<PriznakArc> pr = new ArrayList<>();
        PriznakArc pa;
        if (rs != null) {
            try {
                while (rs.next()) {
                    pa = new PriznakArc(rs.getString("NAME"), rs.getInt("VAL"));
 //                   pa.setMaxWidth(300);
                    pr.add(pa);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        obsPR = FXCollections.observableArrayList(pr);
        return obsPR;
    }
}
