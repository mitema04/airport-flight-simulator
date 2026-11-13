package airports.view;

import airports.model.AirportSystem;
import airports.timer.InactivityTimer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class MainFrame extends Frame {

    private final AirportSystem airportSystem;

    private static final Color PINK_BACKGROUND = new Color(255, 170, 205);
    private static final Color EM_AIR = new Color(170, 20, 90);

    private final CardLayout cardLayout;

    private final Panel contentPanel;
    private final MapPanel mapPanel;
    private final SimulationPanel simulationPanel;

    private Button dataButton;
    private Button mapButton;
    private Button simulationButton;

    private final Font navigationFont = new Font("Arial", Font.PLAIN, 12);
    private final Font activeNavigationFont = new Font("Arial", Font.BOLD, 12);

    // ove inactivity stvari se vezuju za prozor cele aplikacije, ni za da jedan panel posebno
    private final InactivityTimer inactivityTimer;
    private Dialog inactivityDialog;
    private Label inactivityLabel;

    private volatile boolean fileDialogOpen = false;

    public MainFrame() {
        airportSystem = new AirportSystem();
        setTitle("Airport Traffic Simulation");
        setSize(1000, 650);
        setLayout(new BorderLayout(10, 10));
        setBackground(PINK_BACKGROUND);

        Panel navigationPanel = createNavigationPanel();
        add(navigationPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new Panel(cardLayout);

        // na osnovu ovih constraints znamo tacno koju karticu pokazujemo tj otvaramo sta god
        DataPanel dataPanel = new DataPanel(this, airportSystem);
        contentPanel.add(dataPanel, "DATA");
        mapPanel = new MapPanel(airportSystem);
        contentPanel.add(mapPanel, "MAP");
        simulationPanel = new SimulationPanel(airportSystem);
        contentPanel.add(simulationPanel, "SIMULATION");

        add(contentPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                inactivityTimer.stopTimer();
                mapPanel.stopMapTimer();
                simulationPanel.stopSimulationTimer();
                dispose();
            }
        });

        setLocationRelativeTo(null);
        inactivityTimer = new InactivityTimer(this);
        inactivityTimer.start();
        startActivityTracking();
    }

    // dodaje dugmice i povezuje prikaz actionListenerima sa karticama cardPanel-a
    private Panel createNavigationPanel() {
        Panel navigationPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        navigationPanel.setBackground(PINK_BACKGROUND);

        dataButton = new Button("Podaci");
        mapButton = new Button("Mapa");
        simulationButton = new Button("Simulacija");

        dataButton.setForeground(EM_AIR);
        mapButton.setForeground(EM_AIR);
        simulationButton.setForeground(EM_AIR);

        dataButton.setPreferredSize(new Dimension(80, 20));
        mapButton.setPreferredSize(new Dimension(80, 20));
        simulationButton.setPreferredSize(new Dimension(80, 20));

        dataButton.addActionListener(event -> showPage("DATA", dataButton));
        mapButton.addActionListener(event -> showPage("MAP", mapButton));
        simulationButton.addActionListener(event -> showPage("SIMULATION", simulationButton));

        navigationPanel.add(dataButton);
        navigationPanel.add(mapButton);
        navigationPanel.add(simulationButton);
        // ovo je prvo sto je svakako selektovano
        markActiveButton(dataButton);
        return navigationPanel;
    }


    private void showPage(String pageName, Button activeButton) {
        if (pageName.equals("MAP")) {
            mapPanel.refreshAirports();
            mapPanel.activateMap();
        }
        else
            mapPanel.deactivateMap();

        cardLayout.show(contentPanel, pageName);
        markActiveButton(activeButton);
    }

    // ovo samo podebljava font aktivnom dugmetu
    private void markActiveButton(Button activeButton) {
        dataButton.setFont(navigationFont);
        mapButton.setFont(navigationFont);
        simulationButton.setFont(navigationFont);
        activeButton.setFont(activeNavigationFont);
    }

    private void startActivityTracking() {
        long eventMask = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK;
        // ovaj addAWTEventListener prima 2 argumenta, jedan koji je objekat koji implementira listener, a drugi
        // je na sta treba da se reaguje, ovo je super da ne bismo morali za svaku komponentu da dodajemo listenere
        // posebno
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (inactivityDialog == null) {
                inactivityTimer.resetTimer();
            }}, eventMask);
    }

    public void inactivityTimeChanged(int secondsLeft) {
        if (secondsLeft == 5)
            showInactivityDialog();

        if (secondsLeft > 0 && secondsLeft <= 5 && inactivityLabel != null)
            inactivityLabel.setText("Ako ne nastavite rad, program se zatvara za: " + secondsLeft);

        if (secondsLeft <= 0) {
            if (inactivityDialog != null) {
                inactivityDialog.dispose();
            }
            dispose();
            //System.exit(0);
        }
    }

    private void showInactivityDialog() {
        if (inactivityDialog != null)
            return;

        inactivityDialog = new Dialog(this, "Upozorenje", true);
        inactivityDialog.setSize(400, 160);
        inactivityDialog.setLayout(new BorderLayout(10, 10));
        inactivityDialog.setBackground(PINK_BACKGROUND);

        // ovo je ona konstantna labela sto se ne menja
        Label inactiveLabel = new Label("Neaktivni ste.", Label.CENTER);
        // ovo je ona labelica sto se menja
        inactivityLabel = new Label("Ako ne nastavite rad, program se zatvara za: 5", Label.CENTER);

        inactiveLabel.setBackground(PINK_BACKGROUND);
        inactivityLabel.setBackground(PINK_BACKGROUND);

        Panel messagePanel = new Panel(new GridLayout(2, 1));
        messagePanel.setBackground(PINK_BACKGROUND);

        messagePanel.add(inactiveLabel);
        messagePanel.add(inactivityLabel);

        // ovaj wrapper je dodat da se ne bi slepile stvari uz gornju ivicu
        Panel messageWrapper = new Panel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        messageWrapper.setBackground(PINK_BACKGROUND);
        messageWrapper.add(messagePanel);
        inactivityDialog.add(messageWrapper, BorderLayout.CENTER);

        Button continueButton = new Button("Nastavi rad");
        continueButton.addActionListener(event -> continueWorking());
        Panel buttonsPanel = new Panel(new FlowLayout());
        buttonsPanel.setBackground(PINK_BACKGROUND);
        buttonsPanel.add(continueButton);

        inactivityDialog.add(buttonsPanel, BorderLayout.SOUTH);

        inactivityDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                continueWorking();
            }
        });

        inactivityDialog.setLocationRelativeTo(this);
        inactivityDialog.setVisible(true);
    }

    // kada pritisnemo dugme za nastavak rada u dijalogu
    private void continueWorking() {
        inactivityTimer.resetTimer();
        if (inactivityDialog != null) {
            inactivityDialog.dispose();
            inactivityDialog = null;
            inactivityLabel = null;
        }
    }

    public boolean shouldPauseInactivityTimer() {
        return mapPanel.isAirportSelected() || simulationPanel.isSimulationRunning();
    }

    public void setFileDialogOpen(boolean fileDialogOpen) {
        this.fileDialogOpen = fileDialogOpen;
    }

    public boolean isFileDialogOpen() {
        return fileDialogOpen;
    }
}