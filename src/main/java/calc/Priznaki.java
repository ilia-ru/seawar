package calc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Priznaki extends KSQL {

//    TableView<PriznakEQ> table = null;; // Таблица для показа на экране
    ListView<PriznakEQ> listView = null;; // Таблица для показа на экране
    private ObservableList<PriznakEQ> oPriznaki = null; // Список студентов
    Image EQ_IMG_OK = new Image("add.png");
    Image EQ_IMG_ERROR = new Image("del.png");
    Image EQ_IMG_DELETE = new Image("del.png");
    public final double EMPTY_DOUBLE_VALUE = 10^12; // Для обозначения пустых значений в полях ввода признаков

    HashMap<Long, PMapItem> pMap;  // Массив признаков

    public class PInterval {  // Один интервал из описания признака
        private long id;
        private double val;
        private int balls;  // Баллы - храним введенное на cqlc_eq, чтобы обновлять страницу

        public PInterval(long id, double val, int balls) {
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

    public class PMapItem {  // Элемент массива признаков
        private String name;  // Название признака
        private ArrayList<PInterval> pMapIntervals;  // Массив интервалов

        public PMapItem(String name) {
            this.name = name;
            pMapIntervals = new ArrayList<>();
        }

        public String toString() {
            return this.name;
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
        public ArrayList<PInterval> getpMapIntervals() {
            return pMapIntervals;
        }
        public void setpMapIntervals(ArrayList<PInterval> pMapIntervals) {
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

            if ((minVal == EMPTY_DOUBLE_VALUE) || (minVal == maxVal)) {  // Список интервалов пуст - не даем вводить
                ll = new Label("Введите интервалы этого признака");  // или введена одна строка
                this.getChildren().add(ll);
            } else {
                this.minVal = minVal;
                if (minVal == EMPTY_DOUBLE_VALUE) {
                    ll = new Label("");
                } else {
                    ll = new Label(String.valueOf(minVal));
                }
                ll.setPrefWidth(50);
                ll.setAlignment(Pos.CENTER_RIGHT);
                this.getChildren().add(ll);

                this.inputVal = EMPTY_DOUBLE_VALUE;
                iInputVal = new TextField("");
                iInputVal.setPrefWidth(50);
                iInputVal.setAlignment(Pos.CENTER_RIGHT);
                iInputVal.textProperty().addListener(  // расчет баллов
                        (observable, oldValue, newValue) -> {
                            // System.out.println("observable " + observable);
                            if (newValue != null && newValue.compareTo("") > 0) { // Что-то введено
                                this.inputVal = Double.valueOf(newValue);
                                if (this.inputVal >= minVal && this.inputVal < maxVal) {
                                    this.IV.setImage(EQ_IMG_OK);
                                } else {  // Выход данных за интервал
                                    this.IV.setImage(EQ_IMG_ERROR);
                                }
                                this.balls = this.inputVal.intValue() + 2;
                                this.iBalls.setText(String.valueOf(this.balls));
                            } else {  // Поле пустое - гасим индикатор и не используем в расчете этот признак
                                this.inputVal = EMPTY_DOUBLE_VALUE;
                                this.IV.setImage(null);
                            }

                        });

                this.getChildren().add(iInputVal);

                this.maxVal = maxVal;
                if (maxVal == EMPTY_DOUBLE_VALUE) {
                    ll = new Label("");
                } else {
                    ll = new Label(String.valueOf(maxVal));
                }
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
            }
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

    public class PriznakPR extends HBox {  // Одна строка - признак, на странице Признаки
        private Long id;
        private String name;
        private ImageView IV; // Кнопка "Удалить"

        public PriznakPR(Long id, String name) {
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

            this.IV = new ImageView(EQ_IMG_DELETE);
            this.IV.setFitWidth(20);
            this.IV.setFitHeight(20);
            IV.setOnMouseClicked(event -> {  // Удаление признака
                         System.out.println("delete " + this.id);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "\"Признак\" будет удален без возможности восстановления");
                alert.setTitle("Удаление данных");
                alert.setHeaderText("Подтвердите удаление \"Признака\"");
                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE),
                        new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && (result.get().getButtonData().name() == "OK_DONE")) {
                    System.out.println("delete 2 " + this.id);
                }
            });
            this.getChildren().add(IV);

            this.setSpacing(5);
            this.setAlignment(Pos.CENTER_LEFT);
//            this.setPadding(new Insets(0, 5, 0, 0));
        }

        public Long get1Id() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    public Priznaki() {
        super();
        // Таблица
//        this.table = new TableView<PriznakEQ>();
        this.listView = new ListView<PriznakEQ>();
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
    // Возвращает список в виде EQ для модуля calc_eq
    public ObservableList<PriznakEQ> getListEQ() {
    List<PriznakEQ> pr = new ArrayList<>();
    PMapItem pm;
    for (Map.Entry priznak: pMap.entrySet()) {
        pm = (PMapItem) priznak.getValue();
        pr.add(new PriznakEQ((Long) priznak.getKey(), pm.getName(), pm.minVal(), pm.maxVal()));
    }
    ObservableList<PriznakEQ> opr = FXCollections.observableArrayList(pr);
    return opr;
}
/*    public ListView<PriznakEQ> getListEQ() {
        return listView;
    }

 */

    // Возвращает список в виде PR для модуля Признаки
    public ObservableList<PriznakPR> getListPR() {
        List<PriznakPR> pr = new ArrayList<>();
        for (Map.Entry priznak: pMap.entrySet()) {
            pr.add(new PriznakPR((Long) priznak.getKey(), priznak.getValue().toString()));
        }
        ObservableList<PriznakPR> opr = FXCollections.observableArrayList(pr);
        return opr;
    }

    public ObservableList<PriznakEQ> getList() { // Возвращает список студентов
            return oPriznaki;
    }

    public ObservableList<PriznakEQ> createFromSQL(String where) { // Создает из БД и возвращает список студентов
  //      ResultSet rs = this.ksqlSELECT("SELECT ID, NAME, MINVAL, MAXVAL FROM PUBLIC.PUBLIC.PR;"+" "+where); // where - фильтры
        ResultSet rs = this.ksqlSELECT("SELECT ID, NAME FROM PUBLIC.PUBLIC.PRIZNAKI ORDER BY NAME");
        if (rs != null) {
            try {
                while (rs.next()) {  // Признаки
                    pMap.put(rs.getLong("ID"), new PMapItem(rs.getString("NAME")));
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
                                .add(new PInterval(rs.getLong("id"),
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
