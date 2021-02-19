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
import java.util.function.UnaryOperator;

import static java.lang.Double.NaN;

public class Priznaki extends KSQL {

    ListView<PriznakEQ> listView = null;; // Таблица для показа на экране
//    private ObservableList<PriznakEQ> oPriznaki = null; // Список студентов
    Image EQ_IMG_OK = new Image("eq_img_ok.png");
    Image EQ_IMG_ERROR = new Image("eq_img_error.png");
    Image EQ_IMG_DELETE = new Image("del.png");
    public final int DATA_OK = 1; // Данные для признака в интервале и что-то введено
    public final int DATA_EMPTY = 2; // Данные не введены - поле пустое (важно, т.к. 0 - это тоже значение)
    public final int DATA_OUT = 3; // Значение за диапазоном min-max
    private PriznakiMap priznakiMap;

    public Integer calcBalls2() {  // Подсчет кол-ва баллов для 2*2*2
        PMapItem pm;
        int balls = 0;
        boolean isDataInput = false; // Есть признаки с введенными значениями - их посчитали
        for (Map.Entry priznak: priznakiMap.dataMap.entrySet()) {
            pm = (PMapItem) priznak.getValue();
            // с DATA_EMPTY -  введеныы интервалы - пропускаем
            if (pm.getDataValid() == DATA_OK) {  // Суммируем баллы по признакам, где введено значение
                balls += pm.getBall();
                isDataInput = true;
            } else if (pm.getDataValid() == DATA_OUT) {  // Выход за границы - сигналим и прерываем расчет
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "В признаке \"" + pm.getName() + "\" значение выходит за границы диапазона данных. Исправьте введенное значение и повторите расчет.");
                alert.setTitle("Внимание");
                alert.setHeaderText("Ошибка во входных данных");
                alert.show();
                return null;
            }
        }
        if (!isDataInput) { // Нет введенных данных - сигналим
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Не введены исходные данные. Введите данные и повторите расчет");
            alert.setTitle("Внимание");
            alert.setHeaderText("Ошибка во входных данных");
            alert.show();
            return null;
        }
        return balls;
    }

    public Integer calcBalls4() {  // Подсчет кол-ва баллов для 2*2*4
        Integer balls2 = calcBalls2();
        Long d;
        if (balls2 != null) {  // (AI5*-0,896+5,04)+AI5
            d = Math.round(Double.valueOf(balls2)*(-0.896)+5.04+Double.valueOf(balls2));
            return  d.intValue();
        }
        return null;
    }

    // Мапа всех признаков и их интервалов
    public class PriznakiMap {
        private HashMap<Long, PMapItem> dataMap;  // Массив признаков
        // name, data_status, pInterval - массив интервалов

        public PriznakiMap() {
            dataMap = new HashMap<>();  // Массив признаков
        }

        public void put(Long i, PMapItem p) { // Помещаем строку данных в мапу
            dataMap.put(i, p);
        }
        public PMapItem get(Long i) { // Берем строку данных в мапу
            return dataMap.get(i);
        }

        public String getName(Long i) {
            return dataMap.get(i).getName();
        }
        public void setName(Long i, String name) {
            dataMap.get(i).setName(name);
        }

/*        public int getDataValid(Long i) {// Флаг - статус введенных данных DATA_OK, DATA_EMPTY, DATA_OUT
            return dataMap.get(i).getDataValid();
        }
        public void setDataValid(Long i, int dataValid) {// Флаг - статус введенных данных DATA_OK, DATA_EMPTY, DATA_OUT
            dataMap.get(i).setDataValid(dataValid);
        }

        public ArrayList<PInterval> getPMapIntervals(Long i) {// Массив интервалов признака
            return dataMap.get(i).getPMapIntervals();
        }
        public void setPMapIntervals(Long i, ArrayList<PInterval> intervals) {// Массив интервалов признака
            dataMap.get(i).setPMapIntervals(intervals);
        }
*/
    }

    public class PMapItem {   // Элемент массива признаков
        private String name;  // Название признака
        private Double inputVal; // Введенное значение для признака
        private int dataValid;// Флаг - статус введенных данных DATA_OK, DATA_EMPTY, DATA_OUT

        private ArrayList<PInterval> pMapIntervals;  // Массив интервалов

        public PMapItem(String name) {
            this.name = name;
            this.dataValid = DATA_EMPTY;  // Сначала ничего не введено
            pMapIntervals = new ArrayList<>();
        }

        public String toString() {
            return this.name;
        }
        public Double minVal() {  // Начало диапазона (Массив сортирован)
            if (pMapIntervals == null || pMapIntervals.size() == 0) return NaN;
            return pMapIntervals.get(0).getVal();
        }

        public Double maxVal() {  // Конец диапазона (Массив сортирован)
            if (pMapIntervals == null || pMapIntervals.size() == 0) return NaN;
            return pMapIntervals.get(pMapIntervals.size()-1).getVal();
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getDataValid() {
            return dataValid;
        }
        public void setDataValid(int dataValid) {
            this.dataValid = dataValid;
        }
        public ArrayList<PInterval> getPMapIntervals() {
            return pMapIntervals;
        }
        public void setPMapIntervals(ArrayList<PInterval> pMapIntervals) {
            this.pMapIntervals = pMapIntervals;
        }

        public Double getInputVal() {
            return inputVal;
        }

        public void setInputVal(Double inputVal) {
            this.inputVal = inputVal;
        }

        public Integer getBall() {  // Возвращаем балл для заданного (ранее введенного) inputVal
            Double prevVal = minVal();
            for (PInterval pi: getPMapIntervals()) {
                if (inputVal >= prevVal && inputVal < pi.val) {
                    return pi.ball;
                } else {
                    prevVal = pi.val;
                }
            }
            return null; // Вызод за диапазон. Тут не может быть, но увы
        }
    }

    public class PInterval {  // Один интервал из описания признака
        private long id;
        private double val;
        private int ball;  // Баллы - храним введенное на cqlc_eq, чтобы обновлять страницу

        public PInterval(long id, double val, int ball) {
            this.id = id;
            this.val = val;
            this.ball = ball;
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
        public int getBall() {
            return ball;
        }
        public void setBall(int ball) {
            this.ball = ball;
        }
    }

    public class PriznakEQ extends HBox {  // Одна строка - признак, на странице Калькулятора эквив.

        private Long id;
//        private String name;
 //       private Double minVal;
//        private Double inputVal;  // Введенное значение
        private TextField iInputVal;
  //      private Double maxVal;
//        private int balls;  // Баллы
        private Label iBalls;
        private ImageView IV; // Индикатор: не введено/используется/ошибка (выход за диапазон)
//        private HBox listItem;

        public PriznakEQ(Long id) {
            this.id = id;
            Label ll = new Label(id.toString());
            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            ll = new Label(priznakiMap.getName(id));
            ll.setPrefWidth(150);
            ll.setMaxWidth(150);
            ll.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().add(ll);

            // Список интервалов пуст, или введена одна строка  интервалах - не даем вводить
            if ((priznakiMap.get(id).minVal().isNaN()) || (priznakiMap.get(id).minVal() == priznakiMap.get(id).maxVal())) {
                ll = new Label("Интервалы признака не введены");
                this.getChildren().add(ll);
            } else {
                ll = new Label(String.valueOf(priznakiMap.get(id).minVal()) + "≤");
                ll.setPrefWidth(50);
                ll.setAlignment(Pos.CENTER_RIGHT);
                this.getChildren().add(ll);

                priznakiMap.get(id).setInputVal(0.0);
                iInputVal = new TextField("");
// Форматтер для ввода только чисел в TextField iInputVal
                UnaryOperator<TextFormatter.Change> iInputValfilter = change -> {
                    String text = change.getText();
                    if (text.compareTo(",") == 0) { text = "."; change.setText(".");}
                    if (text.matches("[0-9.]*")) {
                        if ((text.compareTo(".") == 0) && iInputVal.getText().contains(".")) { return null; }  // вторую точку вводят
                        return change;
                    }
                    return null;
                };
                TextFormatter<String> iInputValFormatter = new TextFormatter<>(iInputValfilter);
                iInputVal.setTextFormatter(iInputValFormatter);

                iInputVal.setPrefWidth(50);
                iInputVal.setAlignment(Pos.CENTER_RIGHT);
                iInputVal.textProperty().addListener(  // расчет баллов
                        (observable, oldValue, newValue) -> {
                            // System.out.println("observable " + observable);
                            if (newValue != null && newValue.compareTo("") > 0) { // Что-то введено
                                priznakiMap.get(id).setInputVal(Double.valueOf(newValue));
                                // В диапазоне?
                                if (priznakiMap.get(id).getInputVal() >= priznakiMap.get(id).minVal()
                                        && priznakiMap.get(id).getInputVal() < priznakiMap.get(id).maxVal()) {
                                    //priznakiMap.get(id).getBall();
                                    this.iBalls.setText(String.valueOf(priznakiMap.get(id).getBall()));
                                    priznakiMap.get(id).setDataValid(DATA_OK);
                                    this.IV.setImage(EQ_IMG_OK);
                                } else {  // Выход данных за интервал
                                    this.iBalls.setText("");
                                    priznakiMap.get(id).setDataValid(DATA_OUT);
                                    this.IV.setImage(EQ_IMG_ERROR);
                                }
                            } else {  // Поле пустое - гасим индикатор и не используем в расчете этот признак
                                priznakiMap.get(id).setInputVal(0.0);
                                this.iBalls.setText("");
                                priznakiMap.get(id).setDataValid(DATA_EMPTY);
                                this.IV.setImage(null);
                            }
                        });
                this.getChildren().add(iInputVal);

                ll = new Label("< "+String.valueOf(priznakiMap.get(id).maxVal()));
                ll.setPrefWidth(50);
                ll.setAlignment(Pos.CENTER_RIGHT);
                this.getChildren().add(ll);

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
        public Long getEQd() {
            return id;
        }
        public void setEQId(Long id) {
            this.id = id;
        }
        public ImageView getIV() { return IV; }
        public void setIV(ImageView IV) { this.IV = IV; }
    }

    public class PriznakPR extends HBox {  // Одна строка - признак, на странице Признаки
        private Long id;

 //       private String name;
        private ImageView IV; // Кнопка "Удалить"

        public PriznakPR(Long id) {
            this.id = id;
            Label ll = new Label(id.toString());
            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

          //  this.name = name;
            ll = new Label(priznakiMap.getName(id));
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
/*        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
*/
    }

    public Priznaki() {
        super();
        // Таблица
//        this.table = new TableView<PriznakEQ>();
        this.listView = new ListView<PriznakEQ>();
        priznakiMap = new PriznakiMap();  // Все признаки с их интервалами

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
    for (Map.Entry priznak: priznakiMap.dataMap.entrySet()) {  //******
        pm = (PMapItem) priznak.getValue();
        pr.add(new PriznakEQ((Long) priznak.getKey()));
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
        for (Map.Entry priznak: priznakiMap.dataMap.entrySet()) {
            pr.add(new PriznakPR((Long) priznak.getKey()));
        }
        ObservableList<PriznakPR> opr = FXCollections.observableArrayList(pr);
        return opr;
    }

/*    public ObservableList<PriznakEQ> getList() { // Возвращает список студентов
            return oPriznaki;
    }
*/

    public void createFromSQL(String where) { // Создает из БД и мапу признаков
        ResultSet rs = this.ksqlSELECT("SELECT ID, NAME FROM PUBLIC.PUBLIC.PRIZNAKI ORDER BY NAME");
        if (rs != null) {
            try {
                while (rs.next()) {  // Признаки
                    priznakiMap.put(rs.getLong("ID"), new PMapItem(rs.getString("NAME")));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            // Формируем Интервалы для каждого признака
            rs = this.ksqlSELECT("SELECT ID, PRIZNAK, VAL, BALLS FROM PUBLIC.PUBLIC.PRIZ_INTERVAL" +
                    //                      " WHERE PRIZNAK="+priznak.getKey()+
                    " ORDER BY VAL;");
            if (rs != null) {
                try {
                    while (rs.next()) {
                        priznakiMap.get(rs.getLong("PRIZNAK")).pMapIntervals
                                .add(new PInterval(rs.getLong("id"),
                                        rs.getDouble("VAL"),
                                        rs.getInt("BALLS")));
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
}
