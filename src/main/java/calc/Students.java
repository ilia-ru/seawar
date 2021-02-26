package calc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Students extends KSQL {
//    private ArrayList<Student> students;  // Значения
    ObservableList<Students.Student> obsST;// СПисок для TableView

    public Students() {
       // students = new ArrayList<Student>();
        getFromSQL();
    }

    public class Student {
        private long id;
        private double value;

        public Student(long id, double value) {
            this.id = id;
            this.value = value;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    public Double getVal(int i) {
//        if (i<1 || i>100 ) { return null; }
        if (i<1) { i = 1; }
        if (i>100) { i = 100; }
        // id начинаются с 1, а элементы в List с 0. Поэтому -1
        return obsST.get(i-1).getValue();
    }

    // Формирует внешний вид таблицы
    public void TableViewDecorate(TableView tableView) {

        TableColumn<Student, Long> idCol //
                = new TableColumn<>("Номер");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
//        TableColumn idCol = new TableColumn<>("Text");
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: center-right;");

        TableColumn<Student, Number> valueCol = new TableColumn<>("Значение");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(130);
        valueCol.setStyle("-fx-alignment: center-right;");
        valueCol.setCellFactory(TextFieldTableCell.<Student, Number>forTableColumn(
                new StringConverter<Number>() {
                    @Override
                    public Number fromString(String string) {
                        try {
                            return Double.valueOf(string);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                    @Override
                    public String toString(Number object) {
                        Double d = (Double) object;
                        return object == null ? "" : String.format("%.4f", d).replace(',','.');
//                        return object == null ? "" : object.toString();
//                        return object == null ? "" : "11";
                    }
                }));

        valueCol.addEventHandler(
                TableColumn.<Student, Number>editCommitEvent(), event -> {
                System.out.println("editCommitEvent " + event.getRowValue().getId());
                System.out.println("editCommitEvent " + event.getNewValue() + " old " + event.getOldValue());
                String q = "UPDATE PUBLIC.PUBLIC.STUDENT SET VAL=" + event.getNewValue() + " WHERE ID=" + event.getRowValue().getId() + ";";
                     System.out.println(q);
                    this.ksqlUPDATE(q);  // Изменяем признак в БД

                });

        tableView.getColumns().clear();
        tableView.getColumns().addAll(idCol, valueCol);
        tableView.setItems(obsST);
    }

    private void getFromSQL() { // Создает из БД список значений
        ArrayList<Student> st = new ArrayList<Student>();
        ResultSet rs = this.ksqlSELECT("SELECT * FROM PUBLIC.PUBLIC.STUDENT ORDER BY ID");
        if (rs != null) {
//            students.add(new Student(0, 0.0));  // не используемое значение. ArrayList начинается с 0
            try {
                while (rs.next()) {
                    st.add(new Student((int) rs.getLong("ID"), rs.getDouble("VAL")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        obsST = FXCollections.observableArrayList(st);
    }
}
