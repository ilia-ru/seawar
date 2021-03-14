package seawar;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URI;
import java.nio.file.Paths;

public class Controller {
    Field fieldLeft;  // Левое поле для игры
    Field fieldRight;  // Правое поле для игры

    @FXML
    public Label txt1;
    public Label iKill;
    public Label iShipsTotalL;
    public Label iShipsTotalR;
    public Button bStart;
    public Button bEndGame;
    public Button alloc;
    public Spinner<Integer> cells1;
    public Spinner<Integer> iFieldSize;
    public Slider cells2;
    public Slider cells3;
    public Slider cells4;
    public AnchorPane anchSettings;
    public AnchorPane anchGame;
    public ImageView imgNotField;
    public ImageView imgLocator;
    public TextField cell2L;
    public TextField cell3L;
    public TextField cell4L;
    public CheckBox needShowL;
    public CheckBox needShowR;
    public CheckBox needShowLGame;
    public CheckBox needShowRGame;

    public Controller() {
    }

    @FXML private void initialize () {
        // Левое поле
        fieldLeft = new Field(10);  // СОздаем поле
        fieldLeft.shipsPatternsAll = new ShipsPatternsAll();  // создаем шаблоны корабликов
        // Оформляем поле для игры
        fieldLeft.setLayoutY(200);
        fieldLeft.setLayoutX(40);
        fieldLeft.setGridLinesVisible(true);
        fieldLeft.setNeedShow(needShowL.isSelected());

        // Событие по клику мышью на поле
        fieldLeft.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (fieldLeft.isMouseTransparent()) {  // Пока думает правое поле, левое закрыто
                    event.consume();
                } else {
                new Thread(() -> {
                        int shipId;
                        Field.Coords2Random.Coord2Random c;

                        int i = (int) (event.getX() * fieldLeft.fieldSize / fieldLeft.getWidth());
                        int j = (int) (event.getY() * fieldLeft.fieldSize / fieldLeft.getWidth());
                        Platform.runLater(() -> iKill.setText(""));
                        if (fieldLeft.getState(i, j) == fieldLeft.cellShip) {  // Проверяем попали?
                            fieldLeft.setState(i, j, fieldLeft.cellHit);
                            // проверям ранен/убит
                            shipId = fieldLeft.getShipId(i, j);
                            //fieldLeft.decShipsCount(shipId);
                            if (fieldLeft.decShipsCount(shipId) > 0) {  // Ранен
                                Platform.runLater(() -> iKill.setText("Ранен"));
                            } else {  // Убит
                                Platform.runLater(() -> iKill.setText("Убит"));
                                fieldLeft.shipsTotal--;
                                Platform.runLater(() -> iShipsTotalL.setText(String.valueOf(fieldLeft.shipsTotal)));
                                if (fieldLeft.shipsTotal <= 0) {
                                    Platform.runLater(() -> iKill.setText("Конец игры - Выиграл человек"));
                                }
                            }
                        } else {
                            fieldLeft.setMouseTransparent(true);  // Не даем кликнуть на левом поле пока комп думает и бьет
                            if (fieldLeft.getState(i, j) == fieldLeft.cellEmpty) {
                                fieldLeft.setState(i, j, fieldLeft.cellAway);

                                // Ответный удар - случайные координаты
//                        i = (int) Math.floor(Math.random() * fieldRight.fieldSize); //
//                        j = (int) Math.floor(Math.random() * fieldRight.fieldSize); //
                                boolean repeat = true; // Повторяем удар компьютера, если попадание
                                while (repeat) {  // Цикл, чтобы если попал, то продолжать бить
                                    c = fieldRight.coords2Random.getCoordRandom();
                                    i = c.x;
                                    j = c.y;
                                    //   System.out.println("11111");

                                    Platform.runLater(() -> iKill.setText(""));
                                    if (fieldRight.getState(i, j) == fieldRight.cellShip) {
                                        int finalI = i;
                                        int finalJ = j;

                                        Platform.runLater(() -> fieldRight.setState(finalI, finalJ, fieldRight.cellShipDemo));
                                        // проверям ранен/убит
                                        try {
                                            Thread.sleep(1000); // Пауза
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        Platform.runLater(() -> fieldRight.setState(finalI, finalJ, fieldRight.cellHit));
                                        // проверям ранен/убит
                                        try {
                                            Thread.sleep(600); // Пауза
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        shipId = fieldRight.getShipId(i, j);
                                        if (fieldRight.decShipsCount(shipId) > 0) {  // Ранен   ????? правильно ли???
                                            Platform.runLater(() -> iKill.setText("Ранен"));
                                        } else {  // Убит
                                            Platform.runLater(() -> iKill.setText("Убит"));
                                            fieldRight.shipsTotal--;
                                            Platform.runLater(() -> iShipsTotalR.setText(String.valueOf(fieldRight.shipsTotal)));
                                            if (fieldRight.shipsTotal <= 0) {
                                                Platform.runLater(() -> iKill.setText("Конец игры - Выиграл компьютер"));
                                                repeat = false;
                                            }
                                        }
                                    } else {  // Мимо
                                        if (fieldRight.getState(i, j) == fieldRight.cellEmpty) {
                                            int finalI = i;
                                            int finalJ = j;
                                            Platform.runLater(() -> fieldRight.setState(finalI, finalJ, fieldRight.cellAway));
                                        }
                                        repeat = false;
                                    }
                                }
                            }
                            fieldLeft.setMouseTransparent(false);
                        }
                }).start();
                //    System.out.println("222");
                }

            }  // MouseEvent
        }));
        // *** Левое поле

        // Правое поле
        fieldRight = new Field(10);  // СОздаем поле
        fieldRight.shipsPatternsAll = new ShipsPatternsAll();  // создаем шаблоны корабликов
        // Оформляем поле для игры
        fieldRight.setLayoutY(200);
        fieldRight.setLayoutX(440);
        fieldRight.setGridLinesVisible(true);
        fieldRight.setNeedShow(needShowR.isSelected());

        // Событие по клику мышью на поле
        fieldRight.setOnMouseClicked((new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                int i = (int) (event.getX() * fieldRight.fieldSize/ fieldRight.getWidth());
                int j = (int) (event.getY() * fieldRight.fieldSize/ fieldRight.getWidth());
                if(fieldRight.getState(i,j) == fieldRight.cellShip) {
                    fieldRight.setState(i,j, fieldRight.cellHit);
                } else {
                    if(fieldRight.getState(i,j) == fieldRight.cellEmpty)
                    { fieldRight.setState(i,j, fieldRight.cellAway); }
                }
            }
        }));
        // *** Правое поле

        // Настраиваем элементы интерфейса
        anchGame.setVisible(false);  // скрываем панель игры. Если забыли в интерфейсе убрать visible

        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, fieldLeft.shipsPatternsAll.getShipCount(0));
        cells1.setValueFactory(valueFactory);
        // cells1.getEditor().setText(String.valueOf(shipsPatternsAll.getShipCount(0)));
        cells2.setValue(Double.parseDouble(String.valueOf(fieldLeft.shipsPatternsAll.getShipCount(1))));
        cell2L.setText(String.valueOf(fieldLeft.shipsPatternsAll.getShipCount(1)));
        cells2.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, //
                                Number oldValue, Number newValue) {
                cell2L.setText(String.valueOf(newValue.intValue()));
                fieldLeft.shipsPatternsAll.setShipCount(1, newValue.intValue());
                fieldRight.shipsPatternsAll.setShipCount(1, newValue.intValue());
            }
        });
        cells3.setValue(Double.parseDouble(String.valueOf(fieldLeft.shipsPatternsAll.getShipCount(2))));
        cell3L.setText(String.valueOf(fieldLeft.shipsPatternsAll.getShipCount(2)));
        cells3.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, //
                                Number oldValue, Number newValue) {
                cell3L.setText(String.valueOf(newValue.intValue()));
                fieldLeft.shipsPatternsAll.setShipCount(2, newValue.intValue());
                fieldRight.shipsPatternsAll.setShipCount(2, newValue.intValue());
            }
        });
        cells4.setValue(Double.parseDouble(String.valueOf(fieldLeft.shipsPatternsAll.getShipCount(3))));
        cell4L.setText(String.valueOf(fieldLeft.shipsPatternsAll.getShipCount(3)));
        cells4.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, //
                                Number oldValue, Number newValue) {
                cell4L.setText(String.valueOf(newValue.intValue()));
                fieldLeft.shipsPatternsAll.setShipCount(3, newValue.intValue());
                fieldRight.shipsPatternsAll.setShipCount(3, newValue.intValue());
            }
        });
        SpinnerValueFactory<Integer> iFieldSizeDefault = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(7, 20, 10);
        iFieldSize.setValueFactory(iFieldSizeDefault);

        //  URI path = Paths.get("locator.gif").toAbsolutePath().toUri();

        // Надпись "Невозможно разместить корабли"
        URI localUrl = Paths.get("image\\notplaced.png").toAbsolutePath().toUri();
        //      Image img = new Image(localUrl.toString());
        imgNotField.setImage(new Image(localUrl.toString()));

        // Изображение экрана локатора
        localUrl = Paths.get("image\\locator.gif").toAbsolutePath().toUri();
        Image img = new Image(localUrl.toString());
        imgLocator.setImage(new Image(localUrl.toString()));

    }



    public void bAllocAction(ActionEvent actionEvent) {
        bStartAction(actionEvent);

    }

    // Конец игры, переход к настройкам
    public void bEndGameAction(ActionEvent actionEvent) {
        anchGame.setVisible(false);
    }

    public void bStartAction(ActionEvent actionEvent) {
        // Label labelTitle = new Label("Enter your user name and password!");
        // Размещаем кораблики на полях
        anchGame.getChildren().remove(fieldLeft);
        anchGame.getChildren().remove(fieldRight);
        if((fieldLeft.shipsAllocate()) && (fieldRight.shipsAllocate())) {
            imgNotField.setVisible(false);
            anchGame.getChildren().add(fieldLeft);
            iShipsTotalL.setText(String.valueOf(fieldLeft.shipsTotal));

            anchGame.getChildren().add(fieldRight);
            iShipsTotalR.setText(String.valueOf(fieldRight.shipsTotal));

            anchGame.setVisible(true);

        }
        else {  // Не разместились кораблики
            anchGame.setVisible(false);
            imgNotField.setVisible(true);
        }


/*        deck1.add(imgViewHide9, 0, 0);
        deck1.add(imgView2, 2, 0);
        deck1.add(labelTitle, 1, 0);

*/


        // System.out.println("Кнопка " + actionEvent.toString());
        //       txt1.setStyle(" -fx-background-color: yellow;");
    }

    public void cell4OnChange(ActionEvent actionEvent) {
        cell4L.setText(actionEvent.toString());

    }


    public void needShowLAction(ActionEvent actionEvent) {
        fieldLeft.setNeedShow(needShowL.isSelected());
        needShowLGame.setSelected(needShowL.isSelected()); // На обеих панелях одинаково
        fieldLeft.needShowExecute();
    }

    public void needShowRAction(ActionEvent actionEvent) {
        fieldRight.setNeedShow(needShowR.isSelected());
        needShowRGame.setSelected(needShowR.isSelected()); // На обеих панелях одинаково
        fieldRight.needShowExecute();
    }

    public void needShowLGameAction(ActionEvent actionEvent) {  //  С панели Game
        fieldLeft.setNeedShow(needShowLGame.isSelected());
        needShowL.setSelected(needShowLGame.isSelected()); // На обеих панелях одинаково
        fieldLeft.needShowExecute();
    }

    public void needShowRGameAction(ActionEvent actionEvent) {  //  С панели Game
        fieldRight.setNeedShow(needShowRGame.isSelected());
        needShowR.setSelected(needShowRGame.isSelected()); // На обеих панелях одинаково
        fieldRight.needShowExecute();
    }


}
