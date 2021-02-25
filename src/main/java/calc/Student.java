package calc;

import javafx.collections.FXCollections;
import javafx.scene.control.TableView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Student extends KSQL {
    private ArrayList<Double> students;  // Значения

    public Student() {
        students = new ArrayList<Double>();
        getFromSQL();
    }

    public Double getVal(int i) {
        if (i<1 || i>100 ) { return null; }
//        if (i<1) { i = 1; }
//        if (i>100) { i = 100; }
        return students.get(i);
    }

    private void getFromSQL() { // Создает из БД список значений
        ResultSet rs = this.ksqlSELECT("SELECT * FROM PUBLIC.PUBLIC.STUDENT ORDER BY ID");
        if (rs != null) {
            students.add(0, 0.0);  // не используемое значение. ArrayList начинается с 0
            try {
                while (rs.next()) {
                    students.add((int) rs.getLong("ID"), rs.getDouble("VAL"));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}
