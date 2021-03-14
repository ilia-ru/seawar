package seawar;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;


public class Field extends GridPane {
 //   File file = new File("d:\\_work\\ilia\\_java\\SeaWar\\image\\img.png");
//    private String localUrl;
    public ShipsPatternsAll shipsPatternsAll;  // шаблоны корабликов
    public int shipsTotal;  // Общее кол-во кораблей

    // Кол-во клеток в каждом корабле. Чтобы знать ранен, или убит.
    // Индекс в массиве - номер корабля по порядку.
    // В каждой клетке есть поле shipId.
    // При раскрытии клетки проверяем какого она корабля и count--
    private ArrayList<Integer> shipsCount = new ArrayList<Integer>();

    public int decShipsCount(int i) {  // Уменьшает кол-во таких корабликов на 1 и возвращает новое значение
        int k = this.getShipsCount(i)-1;
        this.shipsCount.set(i, k);
        return k;
    }

    public void setShipsCount(int i, int count) {
        this.shipsCount.set(i, count);
    }

    public Integer getShipsCount(int i) {
        return shipsCount.get(i);
    }

    private boolean needShow = false;  // true - показываем корабли на поле, false - скрываем

    boolean demoMode = false; // Демо режим - показываем размещение кораблей на поле с замедлением
 //   boolean demoMode = true; // Демо режим - показываем размещение кораблей на поле с замедлением
    public boolean isDemoMode() {
        return demoMode;
    }
    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }

    // Статусы cell и pattern
    byte cellShip = 9; // Есть кораблик в этой точке
    byte cellNearShip = 8; // Территория рядом с кораблем
    byte cellAway = 7;  // Мимо
    byte cellHit = 6;   // Попадание
    byte cellEmpty = 0; // Не занято
    byte cellOut = -1; // За краем поля
    byte cellShipDemo = 5; // В режиме демо - изображает попытку поставить кораблик в клетку

    Image imgShip, imgShipDemo, imgHit, imgMimo, imgNoShip; // картинки для поля

   // Для боя компьютеру нужно обходить те клетки, куда уже бил.
    // В этом массиве, при каждом ударе, убираем битую клетку.
    // Из остальных случайно выбираем куда бить
    Coords2Random coords2Random = new Coords2Random();
    public class Coords2Random {
       private ArrayList<Coord2Random> coords;

       public class Coord2Random {  // Пара координат
           int x, y;

           public Coord2Random(int x, int y) {
               this.x = x;
               this.y = y;
           }
       }

        public int getCoordSize() {  // Размер массива координат
            return this.coords.size();
        }

        public Coords2Random() {
           this.coords = new ArrayList<Coord2Random>();
       }

       public void init() {  // Заполняем массив парами координат дублируя поле в виде массива координат
           for (int i = 0; i < fieldSize; i++)
               for (int j = 0; j < fieldSize; j++) {
                   this.coords.add(new Coord2Random(i, j));
               }
       }

       public Coord2Random getCoordRandom() {
           int i = (int) Math.floor(Math.random() * getCoordSize());
           Coord2Random c = coords.get(i);
           coords.remove(i); // Удаляем - сюда уже били
           return c;
      }
   }


    public void setNeedShow(boolean needShow) {
        this.needShow = needShow;
    }

    // Проходим по полю и ставим картинку клетки в соотв с needShow
    public void needShowExecute() {
        for (int i = 0; i < this.fieldSize; i++)
            for (int j = 0; j < this.fieldSize; j++) {
                if (getState(i, j) == cellShip) {
                    FieldCell f;
                    f = (FieldCell) this.getChildren().get(i * this.fieldSize + j);
                    if (needShow) {
                        f.setImage(imgShip); // показываем кораблики
                    } else {
                        f.setImage(imgNoShip);  // Не показываем
                    }


                }
            }
    }

    int fieldSize = 10;
//    DeckCell[][] deckCells;  // Поле виде массива ячеек DeckCell

    public boolean shipsAllocate() { // Размещает корабли на поле
        this.fieldClear();

        // Начинаем с больших кораблей - потом добавить сортировку шаблонов после чтения ships.txt
        for(int shipType=shipsPatternsAll.getShipTypesCount()-1;shipType>=0;shipType--){  // По типам кораблей
            for(int shipNum=0;shipNum<shipsPatternsAll.getShipCount(shipType);shipNum++) {  // Столько кораблей этого типа
                if(!shipAllocate(shipType)) return false;
            }
        }
        // После расстановки, убираем на поле все кроме "9"
        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; j++) {
                if(getState(i,j) != cellShip) {
                    setState(i,j,cellEmpty);
                }
            }
        }
        return true;
    }

    // Размещает один кораблик на поле
    // Случайным числом берем клетку. Размещаем указанный вид корабля.
    // В случайном порядке по очереди выбираем вариант шаблона и пробуем разместить на поле
    public boolean shipAllocate(int shipType) {
        int x, y; // Координаты центра кораблика
        int i, j;
        boolean notPlace;
        byte stateSafe = 0;  // Здесь храним state текущей ячейки, чтобы потом восстановить, если не лег кораблик
        int ver;  // Несколько раз пробуем в разные случайные координаты
        int cellCount;  // Кол-во "9" в шаблоне
        int currentShip;
       // int patternNum;  // Номер шаблона
        ShipsPatternsAll.ShipPattern pattern;

 //       System.out.println("*************");

        //Несколько раз пробуем в разные случайные координаты
        for(ver=0;ver<15;ver++) {

            x = (int) Math.floor(Math.random() * this.fieldSize); // Случайные координаты центра кораблика на поле
            y = (int) Math.floor(Math.random() * this.fieldSize); // Случайные координаты центра кораблика на поле
            shipsPatternsAll.getShipPatterns(shipType).initPatternRandom();

            // Размещаем кораблик на поле. Случайным образом берем очередной шаблон из его вариантов.
            // Пробуем ставить. Если встал, все. Если нет, пробуем с другим шаблоном.
            // Когда шаблоны кончились - уходим.
            while ((pattern = shipsPatternsAll.getShipPatterns(shipType).getPatternRandom()) != null) {
                // Проходим по шаблону
                notPlace = false;
                for (i = 0; i < 5 && !notPlace; i++) {
                    for (j = 0; j < 5 && !notPlace; j++) {
                       /* if(demoMode && (pattern.getPattern(i, j) == cellShip)) {  // Ячейка - кораблик - демонстрируем
                            // Сохраняем текущий статус ячейки
                            stateSafe =  this.getState(i + x - pattern.getCenterX(), j + y - pattern.getCenterY());
                            this.setState(i + x - pattern.getCenterX(), j + y - pattern.getCenterY(), cellShipDemo);
                            try {
                                Thread.sleep(100); // Пауза
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }*/
                        if(!canPlaceShipCell(i + x - pattern.getCenterX(), j + y - pattern.getCenterY(),
                                pattern.getPattern(i, j))) {
                            // Вернули прежний статус текущей ячейке
                            /*if(demoMode) {
                                this.setState(i + x - pattern.getCenterX(), j + y - pattern.getCenterY(), cellShip);
                            }*/
                            notPlace = true; // Этот вариант шаблона сюда поставить нельзя
                        }
                    }
                }
                if(!notPlace) {
  //                  System.out.println("Влезло " + shipType + " x=" + x + " y=" + y+" ver="+ver);
                    cellCount = 0;
                    currentShip = shipsCount.size(); // Номер очередного кораблика, чтобы ставить в ячейки
                    for (i = 0; i < 5; i++) {
                        for (j = 0; j < 5; j++) {
                            // Ставим статус только в пустые ячейки и если нужно ставить не 0
                            // начала восстанавливаем
 //                           this.demo2State(i + x - pattern.getCenterX(), j + y - pattern.getCenterY());

//                            if ((this.getState(i + x - pattern.getCenterX(), j + y - pattern.getCenterY()) == cellEmpty)
  //                                  && (pattern.getPattern(i, j) != cellEmpty)) {
                            if (this.getState(i + x - pattern.getCenterX(), j + y - pattern.getCenterY()) == cellEmpty) {
                                /*ShipsPatternsAll.ShipPattern finalPattern = pattern;
                                int finalX = x;
                                int finalI = i;
                                int finalJ = j;
                                int finalY = y;
                                Platform.runLater(() ->
                                   this.setState(finalI + finalX - finalPattern.getCenterX(), finalJ + finalY - finalPattern.getCenterY(), finalPattern.getPattern(finalI, finalJ)));
                                */
                                if(pattern.getPattern(i, j) == cellShip) {
                                    cellCount++;  // Кол-во "9" в кораблике. Чтобы потом знать ранен/убит
                                    this.setShipId(i + x - pattern.getCenterX(), j + y - pattern.getCenterY(), currentShip);

                                }
                                this.setState(i + x - pattern.getCenterX(), j + y - pattern.getCenterY(), pattern.getPattern(i, j));
                            }
                        }
                    }
                    shipsCount.add(currentShip, cellCount);  // Столько "9" в этом кораблике
                    shipsTotal++;
                    return true;// Поместился кораблик
                } else {

         //           System.out.println("Не влезло " + shipType + " x=" + x + " y=" + y+" ver="+ver);
                }
            }
        }

        return false;// Не лег кораблик
    }

    class FieldCell extends ImageView {  // Ячейка
        private byte state;  // Статус ячейки - кораблик, ранен...

        // Статус ячейки для демо режима (покаызваем как кораблики становятся)+
        // Сюда переписываем значение из state, чтобы если кораблик не лег, восстановить
        // что было в state до попытки поставить корабль
        private byte stateDemo;
        private int shipId;  // Номер корабля, чтобы знать ранен или убит

        public void setState(byte state) {
            this.state = state;
        }
        public byte getState() {
            return state;
        }

        public void setStateDemo(byte state) {
            this.state = state;
        }
        public byte getStateDemo() {
            return state;
        }
        public void state2Demo() {
            stateDemo = state;
        } // Копируем state -> stateDemo
        public void demo2state() { // Копируем stateDemo -> state
            state = stateDemo;
            stateDemo = cellEmpty;  // сразу обнуляем, т.к. больше не понадобится
        } // Копируем state -> stateDemo

        public void setShipId(int shipId) {
            this.shipId = shipId;
        }
        public int getShipId() {
            return shipId;
        }

         public FieldCell() { // Инициализируем клетку
             state = cellEmpty;
             shipId = -1;  // Номер кораблика в этой клетке. Чтобы знать кого топят
         }
    }

    public boolean inRange(int x, int y) {  // Проверяем индексы. Если не в диапазоне поля, то return false
        if (x < 0 || x >= this.fieldSize || y < 0 || y >= this.fieldSize) { return false; }
        return true;
    }

    public byte getState(int x, int y){  // Статус клетки возвращаем
        FieldCell f;
        if (inRange(x,y)) {
            f = (FieldCell) this.getChildren().get(x*this.fieldSize+y);
            return f.getState();
        }
        else return cellOut;  // За краем поля
    }

    public boolean setState(int x, int y, byte state){  // Статус клетки устанавливаем
        FieldCell f;
        if (inRange(x,y)) {
            f = (FieldCell) this.getChildren().get(x*this.fieldSize+y);
            f.setState(state);
            if(state == cellShip) { // Ячейка с корабликом
                if (demoMode) {
                    f.setImage(imgShip); // В демо-режиме показываем кораблики
                }
                else {
                    if(needShow) {
                        f.setImage(imgShip); // В демо-режиме показываем кораблики
                    } else {
                        f.setImage(imgNoShip);  // В обычном рехиме - нет
                    }
                }
            }
            if(state == cellShipDemo) { f.setImage(imgShipDemo); }  // Показываем кораблик (демо-режим)
            if(state == cellAway) { f.setImage(imgMimo); }
            if(state == cellHit) { f.setImage(imgHit); }
            if(state == cellEmpty) { f.setImage(imgNoShip); }
          //  f.setImage(imgShip);

            return true;
        }
        else return false;  // За краем поля
    }

    public boolean state2Demo(int x, int y) { // Копируем state -> stateDemo
        FieldCell f;
        if (inRange(x,y)) {
            f = (FieldCell) this.getChildren().get(x*this.fieldSize+y);
            f.state2Demo();
            return true;
        }
        else return false;  // За краем поля
    }

    public boolean demo2State(int x, int y) { // Копируем stateDemo -> state
        FieldCell f;
        if (inRange(x,y)) {
            f = (FieldCell) this.getChildren().get(x*this.fieldSize+y);
            f.demo2state();
            return true;
        }
        else return false;  // За краем поля
    }

     public int getShipId(int x, int y){  // Номер корабля на поле этой клетки возвращаем
        FieldCell f;
        if (inRange(x,y)) {
            f = (FieldCell) this.getChildren().get(x*this.fieldSize+y);
            return f.getShipId();
        }
        else return cellOut;  // За краем поля
    }

    public boolean setShipId(int x, int y, int shipId){  // Статус клетки устанавливаем
        FieldCell f;
        if (inRange(x,y)) {
            f = (FieldCell) this.getChildren().get(x*this.fieldSize+y);
            f.setShipId(shipId);
            return true;
        }
        else return false;  // За краем поля
    }

/*    public FieldCell get(int x, int y){
        if (inRange(x,y)) { return (FieldCell) this.getChildren().get(x*10+y); }
        else return null;
    }

    public String getValue(int x, int y){
        if (inRange(x,y)) { return this.get(x,y).getText(); }
        else return null;
    }

    public boolean set(int x, int y, String value){  // Заносим в ячейку значение String
        if (inRange(x,y)) {
            this.get(x,y).setText(value); return true; }
        return false;
    }

    public boolean set(int x, int y, byte value){  // Заносим в ячейку значение byte
        if (inRange(x,y)) { this.get(x,y).setText(String.valueOf(value)); return true; }
        return false;
    }
*/
    public boolean canPlaceShipCell(int x, int y, byte value){  // Проверяем можно ли сюда поставить эту клетку кораблика
        if (inRange(x,y)) {
            if ((this.getState(x, y) + value) <= 16) {   // 8+0=8  8+8=16   8+9>16
                return true;
            }
        }
        else{  // За краем поля
            if (value == cellShip) { return false; } // Кораблик не влез
            else {  // 0 или 8 ушло за край - нормально
                return true;
            }
        }
        return false;
    }

    public Field(int fieldSize) { // Создаем поле размером deckSize*deckSize
        int i, j;
        this.fieldSize = fieldSize;

        // Картинки для ячеек
 /*       File file = new File("d:\\_work\\ilia\\_java\\SeaWar\\image\\ship.png");
        String localUrl = null;
        try {
            localUrl = file.toURI().toURL().toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        imgShip = new Image(localUrl);
*/

        imgShip = new Image("file:.\\image\\ship.png");
        imgShipDemo = new Image("file:.\\image\\ship_demo.gif");
        imgHit = new Image("file:.\\image\\ship_hit.png");
        imgMimo = new Image("file:.\\image\\mimo.png");
        imgNoShip = new Image("file:.\\image\\no_ship.png");

        for(i=0; i<this.fieldSize; i++){
            this.getColumnConstraints().add(new ColumnConstraints(30));
            this.getRowConstraints().add(new RowConstraints(30));
            //           this.addColumn(i,new Label(String.valueOf(i)));
            for(j=0; j<this.fieldSize; j++){
                //    deckCells[i][j] = new DeckCell();
//                this.add(new DeckCell(String.valueOf(i)+", "+String.valueOf(j)), i, j);
                this.add(new FieldCell(), i, j);
            }
            this.getColumnConstraints().get(i).setHalignment(HPos.CENTER);
            this.getColumnConstraints().get(i).setPrefWidth(30);
            this.getRowConstraints().get(i).setPrefHeight(30);
            this.getRowConstraints().get(i).setValignment(VPos.CENTER);
        }

    }

    public void fieldClear() {
        int i, j;
        for(i=0; i<this.fieldSize; i++) {
            for (j = 0; j < this.fieldSize; j++) {
                //this.set(i, j, "");
                this.setState(i, j, cellEmpty);
                this.setShipId(i, j, -1);
            }
        }
        this.shipsCount.clear();  // Очищаем кол-во кораблей на поле
        shipsTotal = 0;
        this.coords2Random.init();
    }
}
