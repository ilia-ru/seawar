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
import java.util.List;

public class Priznaki extends KSQL {

    TableView<PriznakEQ> table = null;; // Таблица для показа на экране
    ListView<PriznakEQ> listView = null;; // Таблица для показа на экране
    private ObservableList<PriznakEQ> oPriznaki = null; // Список студентов
    Image EQ_IMG_OK = new Image("add.png");
    Image EQ_IMG_ERROR = new Image("del.png");
    public final double EMPTY_DOUBLE_VALUE = 10^12; // Для обозначения пустых значений в полях ввода признаков

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
    }

    public TableView<PriznakEQ> getTableView() {  // заполняет table данными из списка и возвращает ее
//        table.setItems(this.getListSQL(""));
        table.setItems(this.oPriznaki);
/*        table.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {  // редактирование данных студента
                if(event.getButton().name().equals("PRIMARY"))  // по левой кнопке мыши
                {
                    System.out.println("getClass "+event.getTarget().getClass().getName());
                }
            }
        }));

 */
        return table;
    }

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

    public ObservableList<PriznakEQ> createListFromSQL(String where) { // Создает из БД и возвращает список студентов
        ResultSet rs = this.ksqlSELECT("SELECT ID, NAME, MINVAL, MAXVAL FROM PUBLIC.PUBLIC.PR;"+" "+where); // where - фильтры
//        ObservableList<Student> oStudents = null;
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
