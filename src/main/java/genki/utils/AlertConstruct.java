package genki.utils;

import javafx.scene.control.Alert;

public class AlertConstruct {

    public static void alertConstructor(String title, String header, String content, Alert.AlertType type) {
        switch (type) {
            case ERROR:
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle(title);
                errorAlert.setHeaderText(header);
                errorAlert.setContentText(content);
                errorAlert.showAndWait();
                break;
            case INFORMATION:
                Alert informationAlert = new Alert(Alert.AlertType.INFORMATION);
                informationAlert.setTitle(title);
                informationAlert.setHeaderText(header);
                informationAlert.setContentText(content);
                informationAlert.showAndWait();
                break;
            case WARNING:
                Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                warningAlert.setTitle(title);
                warningAlert.setHeaderText(header);
                warningAlert.setContentText(content);
                warningAlert.showAndWait();

        }
    }
}
