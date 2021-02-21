package calc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
//import java.time.LocalDate;

public class CalcArc extends KSQL {

    TableView<CalcRecord> tableArc = null;; // Таблица для показа на экране
    ObservableList<CalcRecord> obsCR;  // СПисок для TableView - список расчетов
    ObservableList<PriznakArc> obsPR;  // СПисок для ListView - список признаков

    public CalcArc() {
        super();
        // Таблица
        this.tableArc = new TableView<CalcRecord>();
        TableViewDecorate(); // оформили внешний вид
    }

    public TableView<CalcRecord> getTableArc() {
        return tableArc;
    }


    public class CalcRecord {  // Одна строка - один расчет
        private Long id;
        private java.sql.Date data;
        private String name;

        public CalcRecord(Long pid, java.sql.Date data, String name) {
            this.id = pid;
            this.data = data;
            this.name = name;
        }

        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
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
    }

    public class PriznakArc extends HBox {  // Одна строка - признак, на странице Признаки
        public PriznakArc(String name, Integer val) {
            //  this.name = name;
            Label ll = new Label(name);
            ll.setPrefWidth(350);
            ll.setMaxWidth(350);
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
                = new TableColumn<>("Сохранено");
        dataCol.setCellValueFactory(new PropertyValueFactory<>("data"));
        dataCol.setMaxWidth(70);

        TableColumn<CalcRecord, String> nameCol //
                = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(140);

        this.tableArc.getColumns().addAll(dataCol, nameCol);
    }

    public TableView getCalcsFromSQL(String where) { // Создает из БД список расчетов
        ResultSet rs = this.ksqlSELECT("SELECT * FROM PUBLIC.PUBLIC.ARCHIVE ORDER BY DATA,NAME");
        List<CalcRecord> pr = new ArrayList<>();
        if (rs != null) {
            try {
                while (rs.next()) {
                    pr.add(new CalcRecord(rs.getLong("ID"), rs.getDate("DATA"), rs.getString("NAME")));
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
        String q = "SELECT * FROM PUBLIC.PUBLIC.ARCHIVE_PR WHERE ARC_ID=" + arcId + " ORDER BY NAME";
        ResultSet rs = this.ksqlSELECT(q);
        System.out.println(q);
        List<PriznakArc> pr = new ArrayList<>();
        if (rs != null) {
            try {
                while (rs.next()) {
                    pr.add(new PriznakArc(rs.getString("NAME"), rs.getInt("VAL")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        obsPR = FXCollections.observableArrayList(pr);
        return obsPR;
    }
}
