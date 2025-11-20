module com.example.learningfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.ensiasd.genki to javafx.fxml;
    exports com.ensiasd.genki;
}