package calc;

import javafx.collections.FXCollections;
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
    ObservableList<PriznakPR> obsPR;  // СПисок для ListView в ред-нии признаков
    ObservableList<PriznakEQ> obsEQ;  // СПисок для ListView в EQ
    ObservableList<PIntervalPR> obsPI;  // СПисок для ListView интервалов
    Image EQ_IMG_OK = new Image("eq_img_ok.png");
    Image EQ_IMG_ERROR = new Image("eq_img_error.png");
    Image EQ_IMG_DELETE = new Image("del.png");
    public final int DATA_OK = 1;    // Данные для признака в интервале и что-то введено
    public final int DATA_EMPTY = 2; // Данные не введены - поле пустое (важно, т.к. 0 - это тоже значение)
    public final int DATA_OUT = 3;   // Значение за диапазоном min-max
    public final long NOT_IN_PMAP = -1l;   // В pMapItem храним ID для разных нужнд. Для тех, кого нет в БД - это значение
    private PriznakiMap priznakiMap; // Мапа всех признаков и их интервалов
    private PMapItem pMapTmp;        // Временный PMapItem для редактирования и ввода признаков

    // Добаление признака в мапу и в БД
    // intervals нужно, чтобы взять из формы ввода новые значения
    public Long addPriznak(PMapItem p, ObservableList<Priznaki.PIntervalPR> intervals) {  // Возвращаем id новой записи в БД
        String q = "INSERT INTO PUBLIC.PUBLIC.PRIZNAKI (NAME) VALUES('" + p.getName() + "');";
//        System.out.println("q " + q);
        Long newId = this.ksqlINSERT(q);  // Кладем признак в БД
        Long iId;
        if (newId >=0) { // Добавляем интервалы
            // Значения берем из list, который был на экране
            pMapTmp.getPMapIntervals().clear();  // чистим на всякий случай
            PIntervalPR pi  = intervals.get(0);  // Первый интервал В нем 2 значения
            q = "INSERT INTO PUBLIC.PUBLIC.PRIZ_INTERVAL (PRIZNAK, VAL, BALLS) VALUES(" +
                    newId +","+ pi.getInputValLeft() +"," + pi.getIBallVal() +");";
            System.out.println("q " + q);
            iId = this.ksqlINSERT(q);  // Кладем интервал в БД
            pMapTmp.getPMapIntervals().add(new PInterval(iId, pi.getInputValLeft(), pi.getIBallVal()));
            // Первый интервал положили выше, т.к. там 2 значения. Теперь остальные
            for(int i=0;i<intervals.size();i++) {
                pi = intervals.get(i);  // Снова с начала, но теперь правые поля
                q = "INSERT INTO PUBLIC.PUBLIC.PRIZ_INTERVAL (PRIZNAK, VAL, BALLS) VALUES(" +
                        newId + "," + pi.getInputVal() + "," + pi.getIBallVal() + ");";
                System.out.println("q " + q);
                iId = this.ksqlINSERT(q);  // Кладем интервал в БД
                pMapTmp.getPMapIntervals().add(new PInterval(iId, pi.getInputVal(), pi.getIBallVal()));
            }
            p.setTmpId(newId);
            priznakiMap.put(newId, p);    // Кладем признак в мапу
            p = new PMapItem("");   // Разрываем связь м-ду p и мапой
        }
        return newId;
    }

    // Изменение существующего признака в мапе и в БД
    // intervals нужно, чтобы взять из формы ввода новые значения
    public void changePriznak(PMapItem p, ObservableList<Priznaki.PIntervalPR> intervals) {
        String q = "UPDATE PUBLIC.PUBLIC.PRIZNAKI SET NAME='" + p.getName() + "' WHERE ID=" + p.getTmpId() + ";";
        Long count = this.ksqlUPDATE(q);  // Изменяем признак в БД
        Long iId;
        if (count >= 0) {
            // Удаляем старые интервалы
            q = "DELETE FROM PUBLIC.PUBLIC.PRIZ_INTERVAL WHERE PRIZNAK=" + p.getTmpId() + ";";
            this.ksqlDELETE(q);  // Удаляем интервалы в БД

            // Сохраняем новые интервалы
            // Значения интервалов берем из list, который был на экране и пишем в БД и в pMapTmp
            // Чтобы потом удобно скопировать pMapTmp в мапу
            p.getPMapIntervals().clear();  // чистим на всякий случай
            PIntervalPR pi = intervals.get(0);  // Первый интервал В нем 2 значения
            q = "INSERT INTO PUBLIC.PUBLIC.PRIZ_INTERVAL (PRIZNAK, VAL, BALLS) VALUES(" +
                    p.getTmpId() + "," + pi.getInputValLeft() + "," + pi.getIBallVal() + ");";
            iId = this.ksqlINSERT(q);  // Кладем интервал в БД
            p.getPMapIntervals().add(new PInterval(iId, pi.getInputValLeft(), pi.getIBallVal()));
            // Первый интервал положили выше, т.к. там 2 значения. Теперь остальные
            for (int i = 0; i < intervals.size(); i++) {
                pi = intervals.get(i);  // Снова с начала, но теперь правые поля
                q = "INSERT INTO PUBLIC.PUBLIC.PRIZ_INTERVAL (PRIZNAK, VAL, BALLS) VALUES(" +
                        p.getTmpId() + "," + pi.getInputVal() + "," + pi.getIBallVal() + ");";
                iId = this.ksqlINSERT(q);  // Кладем интервал в БД
                p.getPMapIntervals().add(new PInterval(iId, pi.getInputVal(), pi.getIBallVal()));
            }
            priznakiMap.put(p.getTmpId(), p);  // Заменяем значение в мапе
        }
    }

    // Удаление признака из мапы и в БД
    public int deletePriznak(PriznakPR p) {
        String q = "DELETE FROM PUBLIC.PUBLIC.PRIZNAKI WHERE ID=" + p.getPid() + ";";
//        System.out.println("q " + q);
        int newId = this.ksqlDELETE(q);  // Удаляем признак в БД
        if (newId >=0) {  // Удачно удалилось
            priznakiMap.del(p.getPid());    // Удаляем признак из мапы
            obsPR.remove(p);
            // Удаляем интервалы из БД
            q = "DELETE FROM PUBLIC.PUBLIC.PRIZ_INTERVAL WHERE PRIZNAK=" + p.getPid() + ";";
            int newId2 = this.ksqlDELETE(q);  // Удаляем интервалы в БД
            for(int i=0;i<obsEQ.size();i++) {  // Убираем удаленный элемент из EQ
                if (obsEQ.get(i).getEQid() == p.getPid()) {
                    obsEQ.remove(i);
                    break;
                }
            }
        }
        return newId;
    }

    public PMapItem getPriznak(Long id) {  // Берем признак из мапы
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
        this.pMapTmp.setTmpId(id);
        this.pMapTmp.setDataValid(priznakiMap.get(id).getDataValid());
        this.pMapTmp.getPMapIntervals().clear();  // Очищаем и копируем
        for (PInterval pi: priznakiMap.get(id).getPMapIntervals()) {
            this.pMapTmp.getPMapIntervals().add(new PInterval(pi.getId(), pi.getVal(), pi.getBall()));
        }
//        this.pMapTmp = priznakiMap.get(id);
        return this.pMapTmp;
    }

    // Мапа всех признаков и их интервалов
    public class PriznakiMap {
        private HashMap<Long, PMapItem> dataMap;  // Массив признаков
        // name, data_status, pInterval - массив интервалов

        public PriznakiMap() {
            dataMap = new HashMap<>();  // Массив признаков
        }

        public void del(Long id) { // Удаляем строку
            dataMap.remove(id);
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
            tmpId = NOT_IN_PMAP;  // Такого нет в мапе. Например при создании признака, id ще нет
            this.name = name;
            this.dataValid = DATA_EMPTY;  // Сначала ничего не введено
            pMapIntervals = new ArrayList<>();
        }

        // Формирует список интервалов по началу, концу и кол-ву шагов и добавляет в данный PMapItem
        public void fastIntervalFill(Double from, Double to, int count) {
            Double step, val;
            val = from;
            step = (to - from) / (count);
            this.pMapIntervals.clear();   // Очищаем от прежних безобразий
 //  System.out.println(step);
            for(int i=0;i<=count;i++) { // Кол-во интервалов +1, Отдельная строка чтобы указать max
 //               System.out.println("--" + i + " val " + val);
                if (i == count) { val = to; } // Обходим ошибку округления, чтобы последний диапазон кончался на "to"
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
            obsPI = FXCollections.observableArrayList(pr);
            return obsPI;
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
            // Баллы хранятся в строке интервала, описывающего правую границу. Значение из первой строки интервалов игнорируется.
            PInterval pi;
            Double prevVal = minVal();
            for (int i=1;i<getPMapIntervals().size();i++) {  // Со второй строки
                pi = getPMapIntervals().get(i);
                if (inputVal >= prevVal && inputVal < pi.val) {
                    return pi.ball;
                } else {
                    prevVal = pi.val;
                }
            }
            return null; // Выход за диапазон. Тут не может быть, но увы
        }
    }

    //***********
    public class PIntervalPR extends HBox {  // Строка для таблицы (List) интервалов для ввода/правки
        private TextField iInputVal, iInputValLeft, iBallVal;
        private ImageView IV; // Кнопка "Удалить"

        public Double getInputValLeft() {
            if (iInputValLeft.getText().compareTo("") == 0) { return 0.0; }
            return Double.valueOf(iInputValLeft.getText());
        }

        public Double getInputVal() {
            if (iInputVal.getText().compareTo("") == 0) { return 0.0; }
            return Double.valueOf(iInputVal.getText());
        }

        public Integer getIBallVal() {
            if (iBallVal.getText().compareTo("") == 0) { return 0; }
            return Integer.valueOf(iBallVal.getText());
        }

        public void setLabMin(String str) {
            Label lll;
            lll = (Label) this.getChildren().get(1);
            lll.setText(str);
        }

        public PIntervalPR(PInterval pi, PInterval pi2, boolean isFirst) {
            Label ll = new Label(String.valueOf(pi2.getId()));
            ll.setPrefWidth(25);
            ll.setAlignment(Pos.CENTER_RIGHT);
      //****      ll.setVisible(false);
            this.getChildren().add(0, ll);

            // Первое поле ввода
            if (isFirst) {
                iInputValLeft = new TextField(String.valueOf(pi.getVal()));
 //               iInputVal.setTextFormatter(iInputValFormatter);

                iInputValLeft.setPrefWidth(50);
                iInputValLeft.setAlignment(Pos.CENTER_RIGHT);
                this.getChildren().add(1, iInputValLeft);
            } else { // Не первый интервал  - не даем вводить
                ll = new Label(String.valueOf(pi.getVal()));
                ll.setId("iLabMin");
                ll.setPrefWidth(50);
                ll.setAlignment(Pos.CENTER_RIGHT);
                this.getChildren().add(1, ll);
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

/*            if (pi.getBall() > 0) {
                iBallVal = new TextField(String.valueOf(pi.getBall()));
            } else {
                iBallVal = new TextField("");
            }

 */
            iBallVal = new TextField(String.valueOf(pi2.getBall()));
// Форматтер для ввода только чисел в TextField iBallVal
            UnaryOperator<TextFormatter.Change> iBallValFilter = change -> {
                String text = change.getText();
                if (text.compareTo(",") == 0) { text = "."; change.setText(".");}
                if (text.matches("[0-9-]*")) {  // integer
                    return change;
                }
                return null;
            };
            TextFormatter<String> iBallValFormatter = new TextFormatter<>(iBallValFilter);
            iBallVal.setTextFormatter(iBallValFormatter);

            iBallVal.setPrefWidth(50);
            iBallVal.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(iBallVal);

            if (!isFirst) {  // Первый интервал не удаляем. Признак без интервалов не имеет смысла
                this.IV = new ImageView(EQ_IMG_DELETE);
                this.IV.setFitWidth(20);
                this.IV.setFitHeight(20);
                PIntervalPR hb = this;
                IV.setOnMouseClicked(event -> {  // Удаление признака
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "\"Интервал\" будет удален без возможности восстановления");
                    alert.setTitle("Удаление данных");
                    alert.setHeaderText("Подтвердите удаление интервала");
                    alert.getButtonTypes().clear();
                    alert.getButtonTypes().addAll(new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE),
                            new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && (result.get().getButtonData().name() == "OK_DONE")) {
                        // Удаляем в pMapTmp. В мапе и БД не нужно. Там обновится при сохранении признака
                        pMapTmp.getPMapIntervals().remove(pi2);
    //                    obsPI.
                        int i = obsPI.indexOf(hb);  // Исправить интервалы вокруг удаленного
                        Double d = obsPI.get(i - 1).getInputVal();
                        obsPI.remove(hb);  // И обновляем список для ListView
                        if (i < obsPI.size()) {  // У последнего не нужно
                            obsPI.get(i).setLabMin(d.toString());
                        }
                    }
                });
                this.getChildren().add(IV);
            }

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
                    if (text.matches("[0-9.-]*")) {
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
                if (text.matches("[0-9.-]*")) {
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
            IV.setOnMouseClicked(event -> {  // Удаление интервала
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
                    if (text.matches("[0-9.-]*")) {
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
        public Long getEQid() {
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
            ll.setPrefWidth(350);
            ll.setMaxWidth(350);
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
                alert.setHeaderText("Подтвердите удаление признака \""+priznakiMap.getName(pid)+"\"");
                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE),
                        new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && (result.get().getButtonData().name() == "OK_DONE")) {
                    System.out.println("delete 2 " + this.pid);
                    deletePriznak(this);  // Удаляем в БД и мапе
                }
            });
            this.getChildren().add(IV);

            this.setSpacing(5);
            this.setAlignment(Pos.CENTER_LEFT);
//            this.setPadding(new Insets(0, 5, 0, 0));

            /*            this.setOnMouseClicked(event -> {
                System.out.println("lll " + this.getPid() + event);
                Controller.prOpenEditPane(this.getPid());
            });
*/
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
        obsEQ = FXCollections.observableArrayList(pr);
        return obsEQ;
    }

    // Возвращает список в виде PR для модуля Признаки
    public ObservableList<PriznakPR> getListPR() {
        List<PriznakPR> pr = new ArrayList<>();
        for (Map.Entry priznak: priznakiMap.dataMap.entrySet()) {
            pr.add(new PriznakPR((Long) priznak.getKey()));
        }
        obsPR = FXCollections.observableArrayList(pr);
        return obsPR;
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
                    " ORDER BY VAL;");
            if (rs != null) {
                try {
                    while (rs.next()) {
                        if (priznakiMap.get(rs.getLong("PRIZNAK")) != null) {  // висят интервалы без признаков
                            priznakiMap.get(rs.getLong("PRIZNAK")).pMapIntervals
                                    .add(new PInterval(rs.getLong("id"),
                                            rs.getDouble("VAL"),
                                            rs.getInt("BALLS")));
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }

    // Сохранение расчета в БД
    public long saveСalculation(String name, String design, String power, String power_val,
                                String power_val_style, int balls, int alg) {
        String q = "INSERT INTO PUBLIC.PUBLIC.ARCHIVE (NAME, DESIGN, POWER, POWER_VAL," +
                    " POWER_VAL_STYLE, BALLS, ALG) VALUES('" + name + "', '" +
                    design + "', '" + power + "', '" + power_val + "', '" + power_val_style + "', " +
                    balls  + ", " + alg + ");";
        System.out.println(q);
        Long newId = this.ksqlINSERT(q);  // Кладем расчет в БД
        if (newId >= 0) { // Пишем в БД признаки
            // Значения берем из list, который был на экране
            for (PriznakEQ p : obsEQ) {
                if (priznakiMap.get(p.getEQid()).getDataValid() == DATA_OK) { // Только по валидным признакам
                    q = "INSERT INTO PUBLIC.PUBLIC.ARCHIVE_PR (ARC_ID, NAME, VAL) VALUES(" +
                            newId + ", '" + priznakiMap.get(p.getEQid()).getName() + "'," + p.iInputVal.getText() + ");";
                    System.out.println(q);
                    this.ksqlINSERT(q);  // Кладем признаки в БД
                }
            }
        }
        return newId;
    }

    public Integer calcBalls2(boolean needMessage) {  // Подсчет кол-ва баллов для 2*2*2
        // needMessage , чтобы вызывать из calcBalls4 не повторяя сообщения об ошибках, т.к. расчет на тех же данных и ошибки те-же
        // Баллы хранятся в строке интервала, описывающего правую границу. Значение из первой строки интервалов игнорируется.
        PMapItem pm;
        int balls = 0;
        boolean isDataInput = false; // Есть признаки с введенными значениями - их посчитали
        for (Map.Entry priznak: priznakiMap.dataMap.entrySet()) {
            pm = (PMapItem) priznak.getValue();
            // с DATA_EMPTY -  введены интервалы - пропускаем
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
        Integer balls2 = calcBalls2(true);
        Long d;
        if (balls2 != null) {  // (AI5*-0,896+5,04)+AI5
            d = Math.round(Double.valueOf(balls2)*(-0.896)+5.04+Double.valueOf(balls2));
            return  d.intValue();
        }
        return null;
    }
}
