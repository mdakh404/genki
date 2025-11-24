module genki {
    requires javafx.controls;
    requires javafx.fxml;


    opens genki to javafx.fxml;
    exports genki;
}