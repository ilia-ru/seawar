package calc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//import java.time.LocalDate;

public class CalcArc extends KSQL {

    TableView<CalcRecord> tableArc = null;; // Таблица для показа на экране
    ObservableList<CalcRecord> obsPR;  // СПисок для ListView в ред-нии признаков

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

    // Формирует внешний вид таблицы - список признаков
    public void TableViewDecorate() {

        // Create column UserName (Data type of String).
        TableColumn<CalcRecord, Long> idCol //
                = new TableColumn<>("id");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<CalcRecord, java.sql.Date> dataCol //
                = new TableColumn<>("Дата сохранения");
        dataCol.setCellValueFactory(new PropertyValueFactory<>("data"));

        TableColumn<CalcRecord, String> nameCol //
                = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        this.tableArc.getColumns().addAll(idCol, dataCol, nameCol);
    }

    public TableView createFromSQL(String where) { // Создает из БД и мапу признаков
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
        obsPR = FXCollections.observableArrayList(pr);
        tableArc.setItems(obsPR);
        return tableArc;
    }
}
