package airports.view;

import airports.model.Airport;
import airports.model.AirportSystem;

import java.awt.*;

public class MapPanel extends Panel {

    private static final Color PINK_BACKGROUND = new Color(255, 170, 205);
    private static final Color EM_AIR = new Color(170, 20, 90);
    private static final Color BLUE_SKY = new Color(190, 225, 255);

    private final AirportSystem airportSystem;
    private final Panel checkBoxesPanel;
    private final AirportMapCanvas mapCanvas;

    public MapPanel(AirportSystem airportSystem) {
        this.airportSystem = airportSystem;

        setLayout(new BorderLayout());
        setBackground(PINK_BACKGROUND);

        add(createSpacer(25, 1), BorderLayout.WEST);
        add(createSpacer(25, 1), BorderLayout.EAST);
        add(createSpacer(1, 20), BorderLayout.NORTH);
        add(createSpacer(1, 20), BorderLayout.SOUTH);

        Panel contentPanel = new Panel(new BorderLayout(20, 0));
        contentPanel.setBackground(PINK_BACKGROUND);
        add(contentPanel, BorderLayout.CENTER);

        mapCanvas = new AirportMapCanvas(airportSystem);
        mapCanvas.setBackground(BLUE_SKY);
        contentPanel.add(mapCanvas, BorderLayout.CENTER);

        Panel rightPanel = new Panel(new BorderLayout(0, 10));
        rightPanel.setBackground(PINK_BACKGROUND);
        rightPanel.setPreferredSize(new Dimension(300, 400));

        Label titleLabel = new Label("Prikazani aerodromi", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(EM_AIR);
        titleLabel.setBackground(PINK_BACKGROUND);

        checkBoxesPanel = new Panel(new GridLayout(0, 1, 0, 5));
        checkBoxesPanel.setBackground(PINK_BACKGROUND);

        ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        scrollPane.add(checkBoxesPanel);

        rightPanel.add(titleLabel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(rightPanel, BorderLayout.EAST);

        refreshAirports();
    }

    public void refreshAirports() {
        checkBoxesPanel.removeAll();

        for (Airport airport : airportSystem.getAirports()) {
            Panel rowPanel = new Panel(new BorderLayout(5, 0));
            rowPanel.setBackground(PINK_BACKGROUND);

            Checkbox checkbox = new Checkbox(airport.getCode() + " - " + airport.getName() + " ("
                    + airport.getX() + "," + airport.getY() + ")", airport.isVisible());

            checkbox.setBackground(PINK_BACKGROUND);
            checkbox.addItemListener(event -> {
                airport.setVisible(checkbox.getState());
                mapCanvas.repaint();
            });

            rowPanel.add(checkbox, BorderLayout.CENTER);
            checkBoxesPanel.add(rowPanel);
        }

        checkBoxesPanel.validate();
        checkBoxesPanel.repaint();
        mapCanvas.repaint();
    }

    public void activateMap() {
        mapCanvas.activateBlinking();
    }

    public void deactivateMap() {
        mapCanvas.deactivateBlinking();
    }

    public void stopMapTimer() {
        mapCanvas.stopBlinkingTimer();
    }

    public boolean isAirportSelected() {
        return mapCanvas.isAirportSelected();
    }

    private Panel createSpacer(int width, int height) {
        Panel spacer = new Panel();
        spacer.setPreferredSize(new Dimension(width, height));
        spacer.setBackground(PINK_BACKGROUND);
        return spacer;
    }
}
