package labex1;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AirFryerApp extends Application {

    String[] menu = {"ON", "OFF", "TIMER", "TEMP", "FRY"};

    int menuPosition = 0;
    int fryTime = 1;
    int fryTemp = 80;
    int secondsLeft = 0;
    int currentHeat = 25;

    boolean cordConnected = false;
    boolean basketConnected = false;
    boolean machineOn = false;
    boolean timerSaved = false;
    boolean tempSaved = false;
    boolean isFrying = false;
    boolean settingValue = false;
    boolean flashState = false;

    Label firstLine = new Label();
    Label secondLine = new Label();
    Label fan = new Label("Fan: OFF");
    Label heater = new Label("Heater: OFF");

    VBox displayBox = new VBox();

    Timeline timer;
    Timeline flashTimer;

    @Override
    public void start(Stage stage) {

        Label title = new Label("AIR FRYER");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");

        createDisplay();

        Button minus = new Button("-");
        Button select = new Button("PWR/SEL");
        Button plus = new Button("+");

        minus.setPrefSize(80, 50);
        select.setPrefSize(100, 50);
        plus.setPrefSize(80, 50);

        ToggleButton cord = new ToggleButton("Power Cord: OUT");
        ToggleButton basket = new ToggleButton("Basket: OUT");

        cord.setPrefWidth(160);
        basket.setPrefWidth(160);

        cord.setOnAction(e -> {

            cordConnected = cord.isSelected();

            if (cordConnected) {
                cord.setText("Power Cord: IN");
            } else {
                cord.setText("Power Cord: OUT");

                if (machineOn) {
                    powerOff();
                }
            }
        });

        basket.setOnAction(e -> {

            basketConnected = basket.isSelected();

            if (basketConnected) {
                basket.setText("Basket: IN");
            } else {
                basket.setText("Basket: OUT");

                if (machineOn) {
                    powerOff();
                }
            }
        });

        minus.setOnAction(e -> buttonPressed(-1));
        plus.setOnAction(e -> buttonPressed(1));
        select.setOnAction(e -> handleSelection());

        HBox controlButtons = new HBox(10, minus, select, plus);
        controlButtons.setAlignment(Pos.CENTER);

        HBox safetyControls = new HBox(10, cord, basket);
        safetyControls.setAlignment(Pos.CENTER);

        HBox deviceStatus = new HBox(30, fan, heater);
        deviceStatus.setAlignment(Pos.CENTER);

        VBox mainLayout = new VBox(
                20,
                title,
                displayBox,
                controlButtons,
                safetyControls,
                deviceStatus
        );

        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(30));

        Scene scene = new Scene(mainLayout, 450, 400);

        stage.setTitle("Air Fryer Simulator");
        stage.setScene(scene);
        stage.show();

        displayOption();
    }

    public void createDisplay() {

        firstLine.setMaxWidth(Double.MAX_VALUE);
        secondLine.setMaxWidth(Double.MAX_VALUE);

        firstLine.setAlignment(Pos.CENTER_LEFT);
        secondLine.setAlignment(Pos.CENTER);

        firstLine.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        secondLine.setStyle(
                "-fx-font-size: 27px;" +
                "-fx-font-weight: bold;"
        );

        displayBox.setPrefSize(300, 100);
        displayBox.setPadding(new Insets(10));
        displayBox.getChildren().addAll(firstLine, secondLine);

        setDisplayColor("yellow");
    }

    public void buttonPressed(int direction) {

        if (isFrying) {
            changeMenu(direction);
            return;
        }

        if (settingValue) {
            changeValue(direction);
            return;
        }

        changeMenu(direction);
    }

    public void changeMenu(int direction) {

        menuPosition += direction;

        if (menuPosition >= menu.length) {
            menuPosition = 0;
        }

        if (menuPosition < 0) {
            menuPosition = menu.length - 1;
        }

        if (menu[menuPosition].equals("FRY") && !canFry()) {
            changeMenu(direction);
            return;
        }

        displayOption();
    }

    public void displayOption() {

        String current = menu[menuPosition];

        firstLine.setText(current);

        if (isFrying) {
            secondLine.setText(secondsLeft + " sec");
            return;
        }

        if (current.equals("ON")) {

            if (machineOn) {
                secondLine.setText("ON");
            } else {
                secondLine.setText("OFF");
            }
        }

        if (current.equals("OFF")) {
            secondLine.setText("OFF");
        }

        if (current.equals("TIMER")) {
            secondLine.setText(fryTime + " sec");
        }

        if (current.equals("TEMP")) {
            secondLine.setText(fryTemp + " °C");
        }

        if (current.equals("FRY")) {
            secondLine.setText("FRY");
        }
    }

    public void handleSelection() {

        String selected = menu[menuPosition];

        if (isFrying) {

            if (selected.equals("OFF")) {
                powerOff();
            }

            return;
        }

        if (settingValue) {

            if (selected.equals("TIMER")) {
                timerSaved = true;
                secondLine.setText(fryTime + " sec SET");
            }

            if (selected.equals("TEMP")) {
                tempSaved = true;
                secondLine.setText(fryTemp + " °C SET");
            }

            settingValue = false;
            checkFryOption();

            return;
        }

        if (selected.equals("ON")) {
            powerOn();
        }

        if (selected.equals("OFF")) {
            powerOff();
        }

        if (selected.equals("TIMER")) {

            if (machineOn) {
                settingValue = true;
                secondLine.setText(fryTime + " sec");
            }
        }

        if (selected.equals("TEMP")) {

            if (machineOn) {
                settingValue = true;
                secondLine.setText(fryTemp + " °C");
            }
        }

        if (selected.equals("FRY")) {
            startFrying();
        }
    }

    public void changeValue(int direction) {

        String current = menu[menuPosition];

        if (current.equals("TIMER")) {

            fryTime += direction;

            if (fryTime < 1) {
                fryTime = 1;
            }

            if (fryTime > 60) {
                fryTime = 60;
            }

            secondLine.setText(fryTime + " sec");
        }

        if (current.equals("TEMP")) {

            fryTemp += direction * 5;

            if (fryTemp < 80) {
                fryTemp = 80;
            }

            if (fryTemp > 200) {
                fryTemp = 200;
            }

            secondLine.setText(fryTemp + " °C");
        }
    }

    public void powerOn() {

        if (!cordConnected || !basketConnected) {
            secondLine.setText("CHECK CORD/BASKET");
            return;
        }

        machineOn = true;

        firstLine.setText("ON");
        secondLine.setText("ON");

        setDisplayColor("red");
    }

    public void checkFryOption() {

        if (canFry()) {

            menuPosition = 4;

            firstLine.setText("FRY");
            secondLine.setText("FRY");
        }
    }

    public boolean canFry() {
        return machineOn && timerSaved && tempSaved;
    }

    public void startFrying() {

        if (!canFry()) {
            return;
        }

        isFrying = true;
        settingValue = false;

        secondsLeft = fryTime;
        currentHeat = 25;

        firstLine.setText("FRY");
        secondLine.setText(secondsLeft + " sec");

        fan.setText("Fan: SPINNING");
        heater.setText("Heater: " + currentHeat + " °C");

        if (secondsLeft <= 5) {
            startFlashing();
        }

        timer = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        e -> updateFrying()
                )
        );

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    public void updateFrying() {

        secondsLeft--;

        if (currentHeat < fryTemp) {

            currentHeat += 10;

            if (currentHeat > fryTemp) {
                currentHeat = fryTemp;
            }

            heater.setText("Heater: " + currentHeat + " °C");
        }

        secondLine.setText(secondsLeft + " sec");

        if (secondsLeft <= 5 && secondsLeft > 0 && flashTimer == null) {
            startFlashing();
        }

        if (secondsLeft <= 0) {
            powerOff();
        }
    }

    public void startFlashing() {

        if (flashTimer != null) {
            return;
        }

        flashTimer = new Timeline(
                new KeyFrame(
                        Duration.seconds(0.5),
                        e -> flashDisplay()
                )
        );

        flashTimer.setCycleCount(Timeline.INDEFINITE);
        flashTimer.play();
    }

    public void flashDisplay() {

        if (flashState) {
            setDisplayColor("red");
        } else {
            setDisplayColor("blue");
        }

        flashState = !flashState;
    }

    public void powerOff() {

        machineOn = false;
        isFrying = false;
        settingValue = false;

        timerSaved = false;
        tempSaved = false;
        flashState = false;

        stopAnimations();

        fryTime = 1;
        fryTemp = 80;
        currentHeat = 25;

        menuPosition = 1;

        firstLine.setText("OFF");
        secondLine.setText("OFF");

        fan.setText("Fan: OFF");
        heater.setText("Heater: OFF");

        setDisplayColor("yellow");
    }

    public void stopAnimations() {

        if (timer != null) {
            timer.stop();
            timer = null;
        }

        if (flashTimer != null) {
            flashTimer.stop();
            flashTimer = null;
        }
    }

    public void setDisplayColor(String color) {

        displayBox.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-border-color: black;" +
                "-fx-border-width: 3;"
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
