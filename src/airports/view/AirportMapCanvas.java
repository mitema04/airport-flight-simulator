package airports.view;

import airports.model.Airport;
import airports.model.AirportSystem;
import airports.timer.BlinkingTimer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
public class AirportMapCanvas extends Canvas {

    private final AirportSystem airportSystem;
    private static final Color BLUE_SKY = new Color(190, 225, 255);

    private static final int MAP_MARGIN = 30;
    private static final int AIRPORT_SIZE = 10;
    private final BlinkingTimer blinkingTimer;

    // ovo nam je zbog onog timera i toga svega volatile
    private volatile Airport selectedAirport;

    private boolean selectedAirportRed = true;

    public boolean isAirportSelected() {
        return selectedAirport != null;
    }

    public Airport getSelectedAirport() {
        return selectedAirport;
    }

    public AirportMapCanvas(AirportSystem airportSystem) {
        this.airportSystem = airportSystem;
        setBackground(BLUE_SKY);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                handleAirportClick(event.getX(), event.getY());
            }
        });
        blinkingTimer = new BlinkingTimer(this);
        blinkingTimer.start();
    }

    private void handleAirportClick(int mouseX, int mouseY) {
        for (Airport airport : airportSystem.getAirports()) {
            if (!airport.isVisible())
                continue;

            int airportX = getScreenX(airport);
            int airportY = getScreenY(airport);
            int halfSize = AIRPORT_SIZE / 2;

            boolean clickedInsideAirport =
                    mouseX >= airportX - halfSize
                            && mouseX <= airportX + halfSize
                            && mouseY >= airportY - halfSize
                            && mouseY <= airportY + halfSize;

            if (clickedInsideAirport) {
                if (selectedAirport == airport) {
                    selectedAirport = null;
                    selectedAirportRed = true;  // da bi za sledeci aerodrom vec bilo spremno kad ga kliknemo
                } else {
                    selectedAirport = airport;
                    selectedAirportRed = true;
                }
                repaint();
                return;
            }
        }
    }

    // ove dve metode zapravo vracaju koordinate sa platna
    private int getScreenX(Airport airport) {
        return MAP_MARGIN + (int) Math.round((airport.getX() + 180) / 360.0 * (getWidth() - 2 * MAP_MARGIN));
    }

    private int getScreenY(Airport airport) {
        return MAP_MARGIN + (int) Math.round((90 - airport.getY()) / 180.0 * (getHeight() - 2 * MAP_MARGIN));
    }

    public void changeBlinkingState() {
        if (selectedAirport == null)
            return;
        selectedAirportRed = !selectedAirportRed;
        repaint();
    }

    public void activateBlinking() {
        blinkingTimer.setActive(true);
    }

    public void deactivateBlinking() {
        blinkingTimer.setActive(false);
        selectedAirportRed = true;
        selectedAirport = null;
        repaint();
    }

    public void stopBlinkingTimer() {
        blinkingTimer.stopTimer();
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.setFont(new Font("Arial", Font.BOLD, 12));
        for (Airport airport : airportSystem.getAirports()) {
            if (!airport.isVisible())
                continue;
            int screenX = getScreenX(airport);
            int screenY = getScreenY(airport);
            if (airport == selectedAirport && selectedAirportRed) {
                graphics.setColor(Color.RED);
            } else {
                graphics.setColor(Color.GRAY);
            }
            graphics.fillRect(screenX - AIRPORT_SIZE / 2, screenY - AIRPORT_SIZE / 2, AIRPORT_SIZE, AIRPORT_SIZE);
            graphics.setColor(Color.BLACK);
            graphics.drawString(airport.getCode(), screenX + 2, screenY - 7);
        }
    }
}
