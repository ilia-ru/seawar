package calc;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.UnaryOperator;

import static java.lang.Double.NaN;

public class Priznaki extends KSQL {

    ListView<PriznakEQ> listView = null;; // Таблица для показа на экране
    Image EQ_IMG_OK = new Image("eq_img_ok.png");
    Image EQ_IMG_ERROR = new Image("eq_img_error.png");
    Image EQ_IMG_DELETE = new Image("del.png");
    public final int DATA_OK = 1;    // Данные для признака в интервале и что-то введено
    public final int DATA_EMPTY = 2; // Данные не введены - поле пустое (важно, т.к. 0 - это тоже значение)
    public final int DATA_OUT = 3;   // Значение за диапазоном min-max
    private PriznakiMap priznakiMap; // Мапа всех признаков и их интервалов
    private PMapItem pMapTmp;        // Временный PMapItem для редактирования и ввода признаков

    public void addPriznak(Long id, PMapItem p) {  // Добаление признака в мапу
        priznakiMap.put(id, p);
        // id вынес в параметры, чтобы не ошибаться. Чтобы каждый раз помнить, что надо id
    }

    public PMapItem getPriznak(Long id) {  // Берем признак их мапы
        return priznakiMap.get(id);
    }

    // Создает (new) и возвращает временный PMapItem для нужностей
    // Ибо чистить - ошибки будут, а сборщик мусора не ошибается
    public PMapItem newPMapTmp(String name) {
        pMapTmp = new PMapItem(name);
        return this.pMapTmp;
    }

    // Возвращает временный PMapItem для нужностей
    public PMapItem getPMapTmp() {
        return this.pMapTmp;
    }

    // КОпируем указанный pMapItem во временный объект
    public PMapItem copyToPMapTmp(Long id){
        this.pMapTmp = new PMapItem(priznakiMap.get(id).getName());
        this.pMapTmp.setTmpId(priznakiMap.get(id).getTmpId());
        this.pMapTmp.setDataValid(priznakiMap.get(id).getDataValid());
//        this.pMapTmp.setPMapIntervals(priznakiMap.get(id).getPMapIntervals());   --- надо циклом
        this.pMapTmp = priznakiMap.get(id);
        return this.pMapTmp;
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

    public class PMapItem {      // Элемент массива признаков
        private Long tmpId = 0l; // Временный id для редактирования признака
        private String name;     // Название признака
        private Double inputVal; // Введенное значение для признака
        private int dataValid;   // Флаг - статус введенных данных DATA_OK, DATA_EMPTY, DATA_OUT

        private ArrayList<PInterval> pMapIntervals;  // Массив интервалов

        public PMapItem(String name) {
            this.name = name;
            this.dataValid = DATA_EMPTY;  // Сначала ничего не введено
            pMapIntervals = new ArrayList<>();
        }

        // Формирует список интервалов по началу, концу и кол-ву шагов и добавляет в данный PMapItem
        public void fastIntervalFill(Double from, Double to, int count) {
            Double step, val;
            val = from;
            step = (to - from) / (count); // Кол-во интервалов +1, Отдельная строка чтобы указать max
            this.pMapIntervals.clear();   // Очищаем от прежних безобразий
 //  System.out.println(step);
            for(int i=0;i<=count;i++) {
 //               System.out.println("--" + i + " val " + val);
                this.pMapIntervals.add(new PInterval(i, val, 0));
                val += step;
            }
        }

        // Возвращает список интервалов для ввода/ред-ния признаков
        public ObservableList<PIntervalPR> getListPI() {
            boolean isFirst;
            List<PIntervalPR> pr = new ArrayList<>();
            if (pMapIntervals.size() >= 2) {  // Элементов в массиве должно быть >=2, чтобы описать концы интервала
                isFirst = true;  // В первом интервале 2 поля для ввода, в остальных - по 1
                pr.add(new PIntervalPR(pMapIntervals.get(0), pMapIntervals.get(1), isFirst));
                for (int i = 2; i < pMapIntervals.size(); i++) {
                    pr.add(new PIntervalPR(pMapIntervals.get(i - 1), pMapIntervals.get(i), false));
                }
            }
            ObservableList<PIntervalPR> opr = FXCollections.observableArrayList(pr);
            return opr;
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

        public Long getTmpId() {
            return tmpId;
        }
        public void setTmpId(Long tmpId) {
            this.tmpId = tmpId;
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

    //***********
    public class PIntervalPR extends HBox {  // Строка для таблицы (List) интервалов для ввода/правки
        private TextField iInputVal, iBallVal;
        private ImageView IV; // Кнопка "Удалить"

        public PIntervalPR(PInterval pi, PInterval pi2, boolean isFirst) {
            Label ll = new Label(String.valueOf(pi.getId()));
            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            // Первое поле ввода
            if (isFirst) {
                iInputVal = new TextField(String.valueOf(pi.getVal()));
 //               iInputVal.setTextFormatter(iInputValFormatter);

                iInputVal.setPrefWidth(50);
                iInputVal.setAlignment(Pos.CENTER_RIGHT);
                this.getChildren().add(iInputVal);
            } else { // Не первый интервал  - не даем вводить
                ll = new Label(String.valueOf(pi.getVal()));
                ll.setPrefWidth(50);
                ll.setAlignment(Pos.CENTER_RIGHT);
                this.getChildren().add(ll);
            }

            ll = new Label("≤ X <");
//            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            // Второе поле ввода
            iInputVal = new TextField(String.valueOf(pi2.getVal()));
//            iInputVal.setTextFormatter(iInputValFormatter);

            iInputVal.setPrefWidth(50);
            iInputVal.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(iInputVal);

            ll = new Label(" балл:");
//            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            if (pi.getBall() > 0) {
                iBallVal = new TextField(String.valueOf(pi.getBall()));
            } else {
                iBallVal = new TextField("");
            }
// Форматтер для ввода только чисел в TextField iBallVal
            UnaryOperator<TextFormatter.Change> iBallValFilter = change -> {
                String text = change.getText();
                if (text.compareTo(",") == 0) { text = "."; change.setText(".");}
                if (text.matches("[0-9.]*")) {
                    if ((text.compareTo(".") == 0) && iBallVal.getText().contains(".")) { return null; }  // вторую точку вводят
                    return change;
                }
                return null;
            };
            TextFormatter<String> iBallValFormatter = new TextFormatter<>(iBallValFilter);
            iBallVal.setTextFormatter(iBallValFormatter);

            iBallVal.setPrefWidth(50);
            iBallVal.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(iBallVal);

            this.IV = new ImageView(EQ_IMG_DELETE);
            this.IV.setFitWidth(20);
            this.IV.setFitHeight(20);
            IV.setOnMouseClicked(event -> {  // Удаление признака
                System.out.println("delete " + pi.getId());
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "\"Интервал\" будет удален без возможности восстановления");
                alert.setTitle("Удаление данных");
                alert.setHeaderText("Подтвердите удаление интервала");
                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE),
                        new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && (result.get().getButtonData().name() == "OK_DONE")) {
                    System.out.println("delete 2 " + pi.getId());
                }
            });
            this.getChildren().add(IV);

            this.setSpacing(5);
            this.setAlignment(Pos.CENTER_LEFT);
//            this.setPadding(new Insets(0, 5, 0, 0));
        }

        public PIntervalPR(PInterval pi, boolean isFirst) {
            Label ll = new Label(String.valueOf(pi.getId()));
            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            if (isFirst) {  // Первый интервал
                iInputVal = new TextField(String.valueOf(pi.getVal()));
// Форматтер для ввода только чисел в TextField iInputVal
                UnaryOperator<TextFormatter.Change> iInputValfilter = change -> {
                    String text = change.getText();
                    if (text.compareTo(",") == 0) {
                        text = ".";
                        change.setText(".");
                    }
                    if (text.matches("[0-9.]*")) {
                        if ((text.compareTo(".") == 0) && iInputVal.getText().contains(".")) {
                            return null;
                        }  // вторую точку вводят
                        return change;
                    }
                    return null;
                };
                TextFormatter<String> iInputValFormatter = new TextFormatter<>(iInputValfilter);
                iInputVal.setTextFormatter(iInputValFormatter);

                iInputVal.setPrefWidth(50);
                iInputVal.setAlignment(Pos.CENTER_RIGHT);
                this.getChildren().add(iInputVal);
            } else { // Остальные интервалы  - не даем вводить
                ll = new Label(String.valueOf(pi.getVal()));
                ll.setPrefWidth(50);
                ll.setAlignment(Pos.CENTER_RIGHT);
                this.getChildren().add(ll);
            }

            ll = new Label("≤ X <");
            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

            iBallVal = new TextField(String.valueOf(pi.getBall()));
// Форматтер для ввода только чисел в TextField iBallVal
            UnaryOperator<TextFormatter.Change> iBallValFilter = change -> {
                String text = change.getText();
                if (text.compareTo(",") == 0) { text = "."; change.setText(".");}
                if (text.matches("[0-9.]*")) {
                    if ((text.compareTo(".") == 0) && iBallVal.getText().contains(".")) { return null; }  // вторую точку вводят
                    return change;
                }
                return null;
            };
            TextFormatter<String> iBallValFormatter = new TextFormatter<>(iBallValFilter);
            iBallVal.setTextFormatter(iBallValFormatter);

            iBallVal.setPrefWidth(50);
            iBallVal.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(iBallVal);

            this.IV = new ImageView(EQ_IMG_DELETE);
            this.IV.setFitWidth(20);
            this.IV.setFitHeight(20);
            IV.setOnMouseClicked(event -> {  // Удаление признака
                System.out.println("delete " + pi.getId());
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "\"Интервал\" будет удален без возможности восстановления");
                alert.setTitle("Удаление данных");
                alert.setHeaderText("Подтвердите удаление интервала");
                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE),
                        new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && (result.get().getButtonData().name() == "OK_DONE")) {
                    System.out.println("delete 2 " + pi.getId());
                }
            });
            this.getChildren().add(IV);

            this.setSpacing(5);
            this.setAlignment(Pos.CENTER_LEFT);
//            this.setPadding(new Insets(0, 5, 0, 0));
        }
    }
    //*************

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
        private Long pid;

 //       private String name;
        private ImageView IV; // Кнопка "Удалить"

        public PriznakPR(Long pid) {
            this.pid = pid;
            Label ll = new Label(pid.toString());
            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(ll);

          //  this.name = name;
            ll = new Label(priznakiMap.getName(pid));
            ll.setPrefWidth(150);
            ll.setMaxWidth(150);
            ll.setAlignment(Pos.CENTER_LEFT);
            this.getChildren().add(ll);

            this.IV = new ImageView(EQ_IMG_DELETE);
            this.IV.setFitWidth(20);
            this.IV.setFitHeight(20);
            IV.setOnMouseClicked(event -> {  // Удаление признака
                         System.out.println("delete " + this.pid);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "\"Признак\" будет удален без возможности восстановления");
                alert.setTitle("Удаление данных");
                alert.setHeaderText("Подтвердите удаление \"Признака\"");
                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE),
                        new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && (result.get().getButtonData().name() == "OK_DONE")) {
                    System.out.println("delete 2 " + this.pid);
                }
            });
            this.getChildren().add(IV);

            this.setSpacing(5);
            this.setAlignment(Pos.CENTER_LEFT);
//            this.setPadding(new Insets(0, 5, 0, 0));

        //    this.addEventFilter();

        }

        public Long getPid() {
            return pid;
        }
        public void setPid(Long pid) {
            this.pid = pid;
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
        this.listView = new ListView<PriznakEQ>();
        priznakiMap = new PriznakiMap();  // Все признаки с их интервалами

    }

    // Возвращает список в виде EQ для модуля calc_eq
    public ObservableList<PriznakEQ> getListEQ() {
        List<PriznakEQ> pr = new ArrayList<>();
        PMapItem pm;
        for (Map.Entry priznak : priznakiMap.dataMap.entrySet()) {  //******
            pm = (PMapItem) priznak.getValue();
            pr.add(new PriznakEQ((Long) priznak.getKey()));
        }
        ObservableList<PriznakEQ> opr = FXCollections.observableArrayList(pr);
        return opr;
    }

    // Возвращает список в виде PR для модуля Признаки
    public ObservableList<PriznakPR> getListPR() {
        List<PriznakPR> pr = new ArrayList<>();
        for (Map.Entry priznak: priznakiMap.dataMap.entrySet()) {
            pr.add(new PriznakPR((Long) priznak.getKey()));
        }
        ObservableList<PriznakPR> opr = FXCollections.observableArrayList(pr);
        return opr;
    }

//    public PMapItem addPriznakToMap(String name, Double from, Double to, int count) {

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

    public Integer calcBalls2(boolean needMessage) {  // Подсчет кол-ва баллов для 2*2*2
        // needMessage , чтобы вызывать из calcBalls4 не повторяя сообщения об ошибках, т.к. расчет на тех же данных и ошибки те-же
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
                if (needMessage) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "В признаке \"" + pm.getName() + "\" значение выходит за границы диапазона данных. Исправьте введенное значение и повторите расчет.");
                    alert.setTitle("Внимание");
                    alert.setHeaderText("Ошибка во входных данных");
                    alert.show();
                }
                return null;
            }
        }
        if (!isDataInput) { // Нет введенных данных - сигналим
            if (needMessage) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Не введены исходные данные. Введите данные и повторите расчет");
                alert.setTitle("Внимание");
                alert.setHeaderText("Ошибка во входных данных");
                alert.show();
            }
            return null;
        }
        return balls;
    }

    public Integer calcBalls4() {  // Подсчет кол-ва баллов для 2*2*4
        Integer balls2 = calcBalls2(false);
        Long d;
        if (balls2 != null) {  // (AI5*-0,896+5,04)+AI5
            d = Math.round(Double.valueOf(balls2)*(-0.896)+5.04+Double.valueOf(balls2));
            return  d.intValue();
        }
        return null;
    }
}
