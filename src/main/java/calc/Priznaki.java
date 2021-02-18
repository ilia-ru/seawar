package calc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Priznaki extends KSQL {

//    TableView<PriznakEQ> table = null;; // Таблица для показа на экране
    ListView<PriznakEQ> listView = null;; // Таблица для показа на экране
    private ObservableList<PriznakEQ> oPriznaki = null; // Список студентов
    Image EQ_IMG_OK = new Image("add.png");
    Image EQ_IMG_ERROR = new Image("del.png");
    public final double EMPTY_DOUBLE_VALUE = 10^12; // Для обозначения пустых значений в полях ввода признаков

    HashMap<Long, pMapItem> pMap;  // Массив признаков

    public class pInterval {  // Один интервал из описания признака
        private long id;
        private double val;
        private int balls;  // Баллы - храним введенное на cqlc_eq, чтобы обновлять страницу

        public pInterval(long id, double val, int balls) {
            this.id = id;
            this.val = val;
            this.balls = balls;
        }

        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }
        public double getVal() {
            return val;
        }
        public void setVal(double val) {
            this.val = val;
        }
        public int getBalls() {
            return balls;
        }
        public void setBalls(int balls) {
            this.balls = balls;
        }
    }

    public class pMapItem {  // Элемент массива признаков
        private String name;  // Название признака
        private ArrayList<pInterval> pMapIntervals;  // Массив интервалов

        public pMapItem(String name) {
            this.name = name;
            pMapIntervals = new ArrayList<>();
        }

        public Double minVal() {  // Начало диапазона (Массив сортирован)
            if (pMapIntervals == null || pMapIntervals.size() == 0) return EMPTY_DOUBLE_VALUE;
            return pMapIntervals.get(0).getVal();
        }

        public Double maxVal() {  // Конец диапазона (Массив сортирован)
            if (pMapIntervals == null || pMapIntervals.size() == 0) return EMPTY_DOUBLE_VALUE;
            return pMapIntervals.get(pMapIntervals.size()-1).getVal();
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public ArrayList<pInterval> getpMapIntervals() {
            return pMapIntervals;
        }
        public void setpMapIntervals(ArrayList<pInterval> pMapIntervals) {
            this.pMapIntervals = pMapIntervals;
        }
    }

    public class PriznakEQ extends HBox {  // Одна строка - признак, на странице Калькулятора эквив.

        private Long id;
        private String name;
        private Double minVal;
        private Double inputVal;  // Введенное значение
        private TextField iInputVal;
        private double maxVal;
        private int balls;  // Баллы
        private Label iBalls;

        private ImageView IV; // Индикатор: не введено/используется/ошибка (выход за диапазон)
//        private HBox listItem;

        public PriznakEQ(Long id, String name, //
                         double minVal,
                         double maxVal) {
            this.id = id;
            Label ll = new Label(id.toString());
            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            this.name = name;
            ll = new Label(name);
            ll.setPrefWidth(150);
            ll.setMaxWidth(150);
            ll.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().add(ll);

            this.minVal = minVal;
            ll = new Label(String.valueOf(minVal));
            ll.setPrefWidth(50);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            this.inputVal = EMPTY_DOUBLE_VALUE;
            iInputVal = new TextField("");
            iInputVal.setPrefWidth(50);
            iInputVal.setAlignment(Pos.CENTER_RIGHT);
            iInputVal.textProperty().addListener(
                    (observable, oldValue, newValue) -> {
                       // System.out.println("observable " + observable);
                        this.inputVal = Double.valueOf(newValue);
                        this.balls = this.inputVal.intValue() + 2;
                        this.iBalls.setText(String.valueOf(this.balls));
                        this.IV.setImage(EQ_IMG_ERROR);
                    });
            this.getChildren().add(iInputVal);

            this.maxVal = maxVal;
            ll = new Label(String.valueOf(maxVal));
            ll.setPrefWidth(50);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            this.balls = 0;
            iBalls = new Label("");
            iBalls.setPrefWidth(50);
            this.getChildren().add(iBalls);

            this.IV = new ImageView();
            this.IV.setFitWidth(15);
            this.IV.setFitHeight(15);
            this.getChildren().add(IV);
//            this.IV = null;  //***** Вставлять серый индикатор

            this.setSpacing(5);
            this.setAlignment(Pos.CENTER_LEFT);
//            this.setPadding(new Insets(0, 5, 0, 0));

        }
/*        public HBox getlistItem() {
            listItem.add

            return listItem;
        }
*/
        public Long get1111Id() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String userName) {
            this.name = userName;
        }
        public double getMinVal() {
            return minVal;
        }
        public void setMinVal(double minVal) {
            this.minVal = minVal;
        }
        public double getMaxVal() {
            return maxVal;
        }
        public void setMaxVal(double maxVal) {
            this.maxVal = maxVal;
        }
        public Double getInputVal() {
            return inputVal;
        }
        public void setInputVal(Double inputVal) {
            this.inputVal = inputVal;
        }

        public int getBalls() { return balls; }
        public void setBalls(int balls) { this.balls = balls; }

        public ImageView getIV() { return IV; }
        public void setIV(ImageView IV) { this.IV = IV; }
    }

    public Priznaki() {
        super();
        // Таблица
//        this.table = new TableView<PriznakEQ>();
        this.listView = new ListView<PriznakEQ>();
        this.listView.setPrefWidth(450);
        pMap = new HashMap<>();
    }

/*
    public TableView<PriznakEQ> getTableView() {  // заполняет table данными из списка и возвращает ее
//        table.setItems(this.getListSQL(""));
        table.setItems(this.oPriznaki);
        table.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {  // редактирование данных студента
                if(event.getButton().name().equals("PRIMARY"))  // по левой кнопке мыши
                {
                    System.out.println("getClass "+event.getTarget().getClass().getName());
                }
            }
        }));

        return table;
    }
*/
    public ListView<PriznakEQ> getListView() {  // заполняет table данными из списка и возвращает ее
        return listView;
    }

    public ObservableList<PriznakEQ> getList() { // Возвращает список студентов
            return oPriznaki;
    }

/*    public ArrayList<Student> getNext() { // Возвращает очередного студента из списка
        ArrayList<Student> st = new ArrayList<>();

        return st;
    }

    public void initList() {  // Переводим индекс очередного элемента в 0
       // this.currentStudent = 0;
        this.oStudents.

    }
*/

    public ObservableList<PriznakEQ> createFromSQL(String where) { // Создает из БД и возвращает список студентов
  //      ResultSet rs = this.ksqlSELECT("SELECT ID, NAME, MINVAL, MAXVAL FROM PUBLIC.PUBLIC.PR;"+" "+where); // where - фильтры
        ResultSet rs = this.ksqlSELECT("SELECT ID, NAME FROM PUBLIC.PUBLIC.PRIZNAKI"+" "+where); // where - фильтры
        if (rs != null) {
            try {
                while (rs.next()) {  // Признаки
                    pMap.put(rs.getLong("ID"), new pMapItem(rs.getString("NAME")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            // Формируем Интервалы для каждого признака
//            for (Map.Entry priznak: pMap.entrySet()) {
            rs = this.ksqlSELECT("SELECT ID, PRIZNAK, VAL, BALLS FROM PUBLIC.PUBLIC.PRIZ_INTERVAL" +
                    //                      " WHERE PRIZNAK="+priznak.getKey()+
                    " ORDER BY VAL;");
            if (rs != null) {
                try {
                    while (rs.next()) {
                        pMap.get(rs.getLong("PRIZNAK")).pMapIntervals
                                .add(new pInterval(rs.getLong("id"),
                                        rs.getDouble("VAL"),
                                        rs.getInt("BALLS")));
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
            


  //      HashMap<Integer, pMapItem> pMap;  // Массив признаков

//        public class pMapItem {  // Элемент массива признаков


//        ObservableList<Student> oStudents = null;
        rs = this.ksqlSELECT("SELECT ID, NAME, MINVAL, MAXVAL FROM PUBLIC.PUBLIC.PR;"+" "+where); // where - фильтры
        List<PriznakEQ> priznaki = new ArrayList<>();
        if (rs != null) {
            try {
                while (rs.next()) {
                    priznaki.add(new PriznakEQ( //
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getDouble("minVal"),
                            rs.getDouble("maxVal")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        oPriznaki = FXCollections.observableArrayList(priznaki);
        listView.setItems(this.oPriznaki);
        return oPriznaki;
    }

}
