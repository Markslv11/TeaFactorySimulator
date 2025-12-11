package com.teafactory.gui;

import com.teafactory.core.TeaFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FactoryGUI extends JFrame {
    private TeaFactory factory;
    private JLabel phaseLabel;
    private JLabel rawBufferLabel;
    private JLabel midBufferLabel;
    private JLabel readyBufferLabel;
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private AnimatedProgressBar rawProgress;
    private AnimatedProgressBar midProgress;
    private AnimatedProgressBar readyProgress;
    private Timer updateTimer;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private static final Color DARK_BG = new Color(20, 25, 35);
    private static final Color PANEL_BG = new Color(30, 35, 45);
    private static final Color ACCENT_BLUE = new Color(52, 152, 219);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color ACCENT_ORANGE = new Color(230, 126, 34);
    private static final Color ACCENT_RED = new Color(231, 76, 60);
    private static final Color TEXT_COLOR = new Color(236, 240, 241);

    // Класс-контейнер для компонентов буфера
    private static class BufferPanelComponents {
        JPanel panel;
        JLabel statusLabel;

        BufferPanelComponents(JPanel panel, JLabel statusLabel) {
            this.panel = panel;
            this.statusLabel = statusLabel;
        }
    }

    public FactoryGUI() {
        super("Tea Factory Simulator - Gaming Edition");
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        initializeComponents();
        createFactory();
        setupTimer();
        startAnimations();
    }

    private void initializeComponents() {
        setSize(1000, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, DARK_BG, 0, getHeight(), new Color(15, 20, 30));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setOpaque(false);

        JPanel titleBar = createTitleBar();
        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();
        JPanel controlPanel = createControlPanel();

        mainPanel.add(titleBar, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    private JPanel createTitleBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 40));

        JLabel title = new JLabel("TEA FACTORY SIMULATOR");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(ACCENT_BLUE);
        title.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setOpaque(false);

        JButton closeButton = createCircleButton(ACCENT_RED);
        closeButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(closeButton);

        panel.add(title, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JButton createCircleButton(Color color) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color btnColor = color;
                if (getModel().isPressed()) {
                    btnColor = color.darker();
                } else if (getModel().isRollover()) {
                    btnColor = color.brighter();
                }

                g2d.setColor(btnColor);
                g2d.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "X";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(text, x, y);
            }
        };
        button.setPreferredSize(new Dimension(30, 30));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, ACCENT_BLUE, getWidth(), 0, ACCENT_BLUE.darker());
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() / 2.0, 20, 20));
            }
        };
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 80));

        JLabel icon = new JLabel("⚙");
        icon.setFont(new Font("Arial", Font.BOLD, 40));
        icon.setForeground(Color.WHITE);

        phaseLabel = new JLabel("Not Started");
        phaseLabel.setFont(new Font("Arial", Font.BOLD, 28));
        phaseLabel.setForeground(Color.YELLOW);

        panel.add(icon);
        panel.add(phaseLabel);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 15, 15));
        panel.setOpaque(false);

        // RAW BUFFER
        BufferPanelComponents rawComponents = createGamingBufferPanel("RAW BUFFER", ACCENT_GREEN, "🌿");
        rawBufferLabel = rawComponents.statusLabel;
        rawProgress = new AnimatedProgressBar(5, ACCENT_GREEN);
        rawComponents.panel.add(rawProgress);

        // MID BUFFER
        BufferPanelComponents midComponents = createGamingBufferPanel("MID BUFFER", ACCENT_ORANGE, "⚗");
        midBufferLabel = midComponents.statusLabel;
        midProgress = new AnimatedProgressBar(3, ACCENT_ORANGE);
        midComponents.panel.add(midProgress);

        // READY BUFFER
        BufferPanelComponents readyComponents = createGamingBufferPanel("READY BUFFER", ACCENT_BLUE, "📦");
        readyBufferLabel = readyComponents.statusLabel;
        readyProgress = new AnimatedProgressBar(4, ACCENT_BLUE);
        readyComponents.panel.add(readyProgress);

        panel.add(rawComponents.panel);
        panel.add(midComponents.panel);
        panel.add(readyComponents.panel);

        return panel;
    }

    private BufferPanelComponents createGamingBufferPanel(String title, Color color, String icon) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(PANEL_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));

                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 15, 15));
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(color);

        JLabel statusLabel = new JLabel("0/0");
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        statusLabel.setForeground(TEXT_COLOR);

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(statusLabel);

        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(10));

        return new BufferPanelComponents(panel, statusLabel);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(PANEL_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(0, 200));

        JLabel titleLabel = new JLabel("📊 EVENT LOG");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(ACCENT_BLUE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(DARK_BG);
        logArea.setForeground(ACCENT_GREEN);
        logArea.setCaretColor(ACCENT_GREEN);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 15, 0, 0));

        startButton = createGamingButton("START", ACCENT_GREEN);
        startButton.addActionListener(e -> startFactory());

        stopButton = createGamingButton("STOP", ACCENT_RED);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopFactory());

        JButton clearButton = createGamingButton("CLEAR", ACCENT_ORANGE);
        clearButton.addActionListener(e -> logArea.setText(""));

        panel.add(startButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(stopButton);
        panel.add(Box.createVerticalStrut(30));
        panel.add(clearButton);

        return panel;
    }

    private JButton createGamingButton(String text, Color color) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color btnColor = color;
                if (!isEnabled()) {
                    btnColor = new Color(100, 100, 100);
                } else if (getModel().isPressed()) {
                    btnColor = color.darker();
                } else if (getModel().isRollover()) {
                    btnColor = color.brighter();
                }

                GradientPaint gradient = new GradientPaint(0, 0, btnColor, 0, getHeight(), btnColor.darker());
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() / 2.0, 10, 10));

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(text, x, y);
            }
        };
        button.setPreferredSize(new Dimension(120, 50));
        button.setMaximumSize(new Dimension(120, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void createFactory() {
        factory = new TeaFactory(this::log);
    }

    private void setupTimer() {
        updateTimer = new Timer(200, e -> updateDisplay());
    }

    private void startAnimations() {
        Timer animationTimer = new Timer(50, e -> {
            if (rawProgress != null) rawProgress.tick();
            if (midProgress != null) midProgress.tick();
            if (readyProgress != null) readyProgress.tick();
        });
        animationTimer.start();
    }

    private void startFactory() {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        factory.start();
        updateTimer.start();
        log(">>> FACTORY STARTED <<<");
    }

    private void stopFactory() {
        stopButton.setEnabled(false);
        factory.stop();
        updateTimer.stop();
        log(">>> FACTORY STOPPED <<<");
        startButton.setEnabled(true);
    }

    private void updateDisplay() {
        int phase = factory.getCurrentPhase();
        String phaseName = factory.getCurrentPhaseName();
        phaseLabel.setText("Phase " + phase + " - " + phaseName);

        updateBufferDisplay(rawBufferLabel, rawProgress, factory.getRawBuffer());
        updateBufferDisplay(midBufferLabel, midProgress, factory.getMidBuffer());
        updateBufferDisplay(readyBufferLabel, readyProgress, factory.getReadyBuffer());
    }

    private void updateBufferDisplay(JLabel label, AnimatedProgressBar progress, com.teafactory.buffer.TeaBuffer buffer) {
        int size = buffer.size();
        int capacity = buffer.getCapacity();

        label.setText(size + "/" + capacity);
        progress.setValue(size);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    static class AnimatedProgressBar extends JPanel {
        private int value;
        private final int maximum;
        private final Color color;
        private int animationFrame;

        public AnimatedProgressBar(int maximum, Color color) {
            this.maximum = maximum;
            this.color = color;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 30));
        }

        public void setValue(int value) {
            this.value = value;
            repaint();
        }

        public void tick() {
            animationFrame++;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2d.setColor(new Color(50, 50, 50));
            g2d.fill(new RoundRectangle2D.Double(0, 0, width, height, 15, 15));

            if (value > 0) {
                int filledWidth = (int) ((double) value / maximum * width);

                GradientPaint gradient = new GradientPaint(0, 0, color, filledWidth, 0, color.darker());
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, filledWidth, height, 15, 15));

                int offset = animationFrame % 40;
                g2d.setColor(new Color(255, 255, 255, 30));
                for (int i = -40; i < filledWidth; i += 40) {
                    g2d.fillRect(i + offset, 0, 20, height);
                }
            }

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String text = (int)((double)value / maximum * 100) + "%";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (width - fm.stringWidth(text)) / 2;
            int y = (height + fm.getAscent()) / 2 - 2;
            g2d.drawString(text, x, y);
        }
    }
}