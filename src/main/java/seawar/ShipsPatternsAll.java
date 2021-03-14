package seawar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

// Шаблоны всех вариантов всех кораблей для размещения на поле.
public class ShipsPatternsAll {
    private ArrayList<ShipPatterns> patterns = new ArrayList<ShipPatterns>(); // Массив шаблонов всех корабликов
    private int shipsTotal = 0;  // Общее кол-во кораблей на поле (сумма количеств каждого вида из ships.txt)

    public int getShipsTotal() {
        return shipsTotal;
    }

    public int incShipsTotal() {
        return ++shipsTotal;
    }

    public int decShipsTotal() {
        return --shipsTotal;
    }


    // Возвращает все варианты шаблонов i-го корабля
    public ShipPatterns getShipPatterns(int shipType) { // shipType-й элемент массива шаблонов - список шаблонов одного корабля
        return patterns.get(shipType);
    }

    // Возвращает кол-во типов кораблей
    public int getShipTypesCount() { return this.patterns.size(); }

    public class ShipPatterns {  // Все варианты шаблонов одного кораблика
        int shipCount = 0; // Кол-во таких кораблей на поле. Изначально из ships.txt
        private ArrayList<ShipPattern> patterns;  // Массив шаблонов одного кораблика
        private ArrayList<Integer> random; // Вспомогательный массив, чтобы случайно перебрать все шаблоны одного корабля

        public ShipPatterns(int shipCount) { // формируем шаблон
            this.shipCount = shipCount;
            patterns = new ArrayList<ShipPattern>();
            random = new ArrayList<Integer>();
        }

        // При размещении корабля на поле берем его шаблоны в случайном порядке для разнообразия размещения кораблей.
        // Для этого при очередном вызове getPatternRandom, случайным числом выбираем номер шаблона по массиву random.
        // В random хранятся номера шаблонов. И удаляем этот номер из random, чтобы в следующий раз уже его не взять.
        // Когда массив random пуст, возвращается null.
        // Массив random нужно создавать каждый раз перед новым перебором шаблонов.
        // (Например при переходе к следующему кораблю.) Для этого initPatternRandom()
        public ShipPattern getPatternRandom() { // Случайный шаблон из шаблонов одного корабля
            int iRandom, i;
            if (this.random.size() <= 0) { return null; } // Нету свежих шаблонов
            iRandom = (int) Math.floor(Math.random() * random.size());
            i = this.random.get(iRandom);  // Выбираем из оставшихся в random
            this.random.remove(iRandom);  // удаляем использованный элемент
//            System.out.println("iRandom= " + iRandom);
            return patterns.get(i);
        }

        public void initPatternRandom() { // Восстанавливаем массив random чтобы снова выбирать из всех шаблонов
            int i;
            this.random.clear(); // Если не все удалили в прошлый проход - чистим.
            for(i=0;i<this.patterns.size();i++) {
                this.random.add(i);
            }
        }

        public ShipPattern getShipPattern(int patternNum) { // patternNum-й элемент массива шаблонов - один из шаблонов одного корабля
            return patterns.get(patternNum);
        }

        public int getShipCount() {  // кол-во таких кораблей на поле
            return shipCount;
        }

        public void setShipCount(int shipCount) {  // Устанавливаем кол-во таких кораблей на поле
            this.shipCount = shipCount;
        }

        public void add(ShipPattern pattern) { // добавляем очередной шаблон этого кораблика
            this.patterns.add(pattern);
 //           this.random.add(i);
        }
    } // ShipPatterns

    public class ShipPattern {  // Один вариант шаблона кораблика

        private byte centerX, centerY;
        private byte[][] pattern;

        public ShipPattern(byte x, byte y, String p) { // формируем шаблон
            this.centerX = x;
            this.centerY = y;
            pattern = new byte[5][5];
            int j = 0; // индекс строки в pattern
            int k = 0; // индекс столбца в pattern
            for (int i = 0; i < p.length(); i++) { // Разбираем строковое представление из CFG в байтовый массив
                switch (p.charAt(i)) {
                    case '8': {
                        pattern[j][k] = 8;
                        k++;
                        break;
                    }
                    case '9': {
                        pattern[j][k] = 9;
                        k++;
                        break;
                    }
                    case '#': { // новая строка
                        for (; k < 5; k++) {
                            pattern[j][k] = 0;
                        } // обнуляем шаблон, где нет инфы о корабле
                        j++;
                        k = 0;
                        break;
                    }
                }

            }
            j++;
            for (; j < 5; j++)
                for (k = 0; k < 5; k++) {
                    pattern[j][k] = 0;
                } // обнуляем шаблон, где нет инфы о корабле

        }

        public byte getPattern(int x, int y) {  // Возвращает ячейку шаблона с указанными координатами
            return pattern[y][x];  // В массиве первая координата - строка - транспонируем
        }

        public byte getCenterX() {
            return centerX;
        }
        public byte getCenterY() {
            return centerY;
        }

    }// ShipPattern

//    private ArrayList<ArrayList<ShipPattern>> patterns = new ArrayList<>(); // Массив шаблонов одного кораблика

    public ShipsPatternsAll() { // формируем шаблоны кораблей по файлу ships.txt
        String cfgLine; // временная строка - читаем из файла
        String patternLine;  // Строка, куда собираем очередной шаблон: 888#898#888
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean state = false;  // статус в разборе входного потока. True - были в звездочке
        byte x = 0, y = 0;  // центр очередного шаблона
        int i;  // номер корабля в массиве шаблонов
        byte sc;  // временная перем. для кол-ва кораблей этого типа из ships.txt

//        System.out.println("Начинаем игру 22222");

        try {
            FileReader cfg = new FileReader("ships.txt");
            BufferedReader cfgBR = new BufferedReader(cfg);

            cfgLine = null;
            patternLine = "";
            i = -1;
            while ((cfgLine = cfgBR.readLine()) != null) {
                //               System.out.println(cfgLine);
                switch (cfgLine.charAt(0)) {
                    case ';': {
                        break;
                    }
                    case '#': {
                        if (state) {  //Добавляем шаблон
                            state = false;
                            patterns.get(i).add(new ShipPattern(x, y, patternLine)); // добавляем очередной шаблон
                            patternLine = "";
                        }
                      //  System.out.println(cfgLine);
                        // Добавляем новый тип корабля
                        sc = (byte) Character.getNumericValue(cfgLine.charAt(3));
                        patterns.add(new ShipPatterns(sc));
                        shipsTotal += sc;  // Считаем общее кол-во кораблей на поле
                        i++;
                        break;
                    }
                    case '*': {
                        if (state) {  //Добавляем шаблон
                            state = false;
                            patterns.get(i).add(new ShipPattern(x, y, patternLine)); // добавляем очередной шаблон
                            patternLine = "";
                        }
                        x = (byte) Character.getNumericValue(cfgLine.charAt(1));
                        y = (byte) Character.getNumericValue(cfgLine.charAt(3));
                        state = true;
                        break;
                    }
                    default: {
                        if (patternLine.length() != 0) {
                            patternLine = patternLine.concat("#");
                        }
                        patternLine = patternLine.concat(cfgLine);
                        break;
                    }

                }

            }
            if (state) {  //Добавляем шаблон по концу файла
                state = false;
                patterns.get(i).add(new ShipPattern(x, y, patternLine)); // добавляем очередной шаблон
            }

            cfgBR.close();
        } catch (IOException e) {
            System.out.println("Нет файла ships.txt");
            //e.printStackTrace();
        }

    }

    public int getShipCount(int ship) {  // кол-во таких кораблей на поле
        return patterns.get(ship).getShipCount();

    }

    public void setShipCount(int ship, int count) {  // кол-во таких кораблей на поле
        patterns.get(ship).setShipCount(count);

    }

}
