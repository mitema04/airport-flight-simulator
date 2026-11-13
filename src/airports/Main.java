package airports;

import airports.view.MainFrame;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // objasnjenje: ovaj EventQueue koristi da bismo ovaj posao prebacili u tok kontrole awt niti
        // to ce da bude izrazito znacajno da bi svim grafickim stvarima upravljala awt nit da ne bi
        // dolazilo do nekih zbunjivanja
        EventQueue.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
