package com.teafactory.gui;

import com.teafactory.core.TeaFactory;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Современный GUI для Tea Factory Simulator
 * Использует чистый дизайн с GridBagLayout для идеального выравнивания
 */
public class FactoryGUI extends JFrame {
    // Ссылка на фабрику
    private TeaFactory factory;

    // Компоненты для отображения фазы
    private JLabel phaseNumberLabel;
    private JLabel phaseNameLabel;

    // Компоненты для буферов
    private JLabel rawBufferSizeLabel;
    private JProgressBar rawBufferProgress;

    private JLabel midBufferSizeLabel;
    private JProgressBar midBufferProgress;

    private JLabel readyBufferSizeLabel;
    private JProgressBar readyBufferProgress;

    // Лог и кнопки
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;

    // Таймер для обновления GUI
    private Timer updateTimer;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    // Цветовая схема (темная тема + акцент)
    private static final Color BG_DARK = new Color(30, 30, 35);
    private static final Color BG_PANEL = new Color(40, 42, 48);
    private static final Color ACCENT = new Color(100, 200, 255);
    private static final Color TEXT_PRIMARY = new Color(230, 230, 230);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 170);
    private static final Color SUCCESS = new Color(80, 200, 120);
    private static final Color DANGER = new Color(255, 100, 100);
    private static final Color WARNING = new Color(255, 180, 80);

    public FactoryGUI() {
        super("Tea Factory Simulator");
        initializeGUI();
        createFactory();
        setupUpdateTimer();
    }

    /**
     * Инициализация GUI
     */
    private void initializeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_DARK);

        // Создаем все панели
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * Верхняя панель: Название + текущая фаза
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(BG_PANEL);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, ACCENT),
                new EmptyBorder(15, 20, 15, 20)
        ));

        // Название приложения
        JLabel titleLabel = new JLabel("TEA FACTORY SIMULATOR");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ACCENT);

        // Панель текущей фазы
        JPanel phasePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        phasePanel.setOpaque(false);

        JLabel phasePrefix = new JLabel("Current Phase:");
        phasePrefix.setFont(new Font("Arial", Font.PLAIN, 14));
        phasePrefix.setForeground(TEXT_SECONDARY);

        phaseNumberLabel = new JLabel("0");
        phaseNumberLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        phaseNumberLabel.setForeground(ACCENT);

        phaseNameLabel = new JLabel("NOT STARTED");
        phaseNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        phaseNameLabel.setForeground(TEXT_PRIMARY);

        phasePanel.add(phasePrefix);
        phasePanel.add(phaseNumberLabel);
        phasePanel.add(new JLabel(" – "));
        phasePanel.add(phaseNameLabel);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(phasePanel, BorderLayout.EAST);

        return header;
    }

    /**
     * Центральная панель: 3 буфера в сетке
     */
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_DARK);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 10, 10, 10);

        // RAW BUFFER
        gbc.gridx = 0;
        gbc.gridy = 0;
        BufferComponents rawComponents = createBufferPanel("RAW BUFFER", 5);
        rawBufferSizeLabel = rawComponents.sizeLabel;
        rawBufferProgress = rawComponents.progressBar;
        center.add(rawComponents.panel, gbc);

        // MID BUFFER
        gbc.gridy = 1;
        BufferComponents midComponents = createBufferPanel("MID BUFFER", 3);
        midBufferSizeLabel = midComponents.sizeLabel;
        midBufferProgress = midComponents.progressBar;
        center.add(midComponents.panel, gbc);

        // READY BUFFER
        gbc.gridy = 2;
        BufferComponents readyComponents = createBufferPanel("READY BUFFER", 4);
        readyBufferSizeLabel = readyComponents.sizeLabel;
        readyBufferProgress = readyComponents.progressBar;
        center.add(readyComponents.panel, gbc);

        return center;
    }

    /**
     * Вспомогательный класс для хранения компонентов буфера
     */
    private static class BufferComponents {
        JPanel panel;
        JLabel sizeLabel;
        JProgressBar progressBar;

        BufferComponents(JPanel panel, JLabel sizeLabel, JProgressBar progressBar) {
            this.panel = panel;
            this.sizeLabel = sizeLabel;
            this.progressBar = progressBar;
        }
    }

    /**
     * Создание панели буфера
     */
    private BufferComponents createBufferPanel(String name, int capacity) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new CompoundBorder(
                new LineBorder(ACCENT.darker(), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Заголовок + размер
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel nameLabel = new JLabel(name + " (capacity " + capacity + ")");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);

        JLabel sizeLabel = new JLabel("0 / " + capacity);
        sizeLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        sizeLabel.setForeground(ACCENT);

        headerPanel.add(nameLabel, BorderLayout.WEST);
        headerPanel.add(sizeLabel, BorderLayout.EAST);

        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(10));

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, capacity);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.BOLD, 12));
        progressBar.setForeground(ACCENT);
        progressBar.setBackground(BG_DARK);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 30));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        panel.add(progressBar);

        return new BufferComponents(panel, sizeLabel, progressBar);
    }

    /**
     * Правая панель: кнопки управления
     */
    private JPanel createRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(BG_DARK);
        right.setBorder(new EmptyBorder(20, 10, 20, 20));
        right.setPreferredSize(new Dimension(150, 0));

        // START button
        startButton = createStyledButton("START", SUCCESS);
        startButton.addActionListener(e -> startFactory());

        // STOP button
        stopButton = createStyledButton("STOP", DANGER);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopFactory());

        // CLEAR button
        JButton clearButton = createStyledButton("CLEAR", WARNING);
        clearButton.addActionListener(e -> clearLog());

        right.add(startButton);
        right.add(Box.createVerticalStrut(15));
        right.add(stopButton);
        right.add(Box.createVerticalStrut(30));
        right.add(clearButton);
        right.add(Box.createVerticalGlue());

        return right;
    }

    /**
     * Создание стилизованной кнопки
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(120, 40));
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover эффект
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(color.brighter());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    /**
     * Нижняя панель: лог событий
     */
    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(0, 5));
        bottom.setBackground(BG_DARK);
        bottom.setBorder(new CompoundBorder(
                new MatteBorder(2, 0, 0, 0, ACCENT),
                new EmptyBorder(15, 20, 20, 20)
        ));
        bottom.setPreferredSize(new Dimension(0, 200));

        // Заголовок лога
        JLabel logTitle = new JLabel("Event Log");
        logTitle.setFont(new Font("Arial", Font.BOLD, 14));
        logTitle.setForeground(TEXT_PRIMARY);

        // Текстовая область
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(BG_PANEL);
        logArea.setForeground(new Color(150, 255, 150));
        logArea.setCaretColor(ACCENT);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new LineBorder(ACCENT.darker(), 1));
        scrollPane.setBackground(BG_PANEL);

        bottom.add(logTitle, BorderLayout.NORTH);
        bottom.add(scrollPane, BorderLayout.CENTER);

        return bottom;
    }

    // ================== ЛОГИКА УПРАВЛЕНИЯ ==================

    /**
     * Создание фабрики
     */
    private void createFactory() {
        factory = new TeaFactory(this::appendLog);
    }

    /**
     * Настройка таймера обновления
     */
    private void setupUpdateTimer() {
        updateTimer = new Timer(200, e -> updateDisplay());
    }

    /**
     * Запуск фабрики
     */
    private void startFactory() {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        factory.start();
        updateTimer.start();
        appendLog("Factory simulation started");
    }

    /**
     * Остановка фабрики
     */
    private void stopFactory() {
        stopButton.setEnabled(false);
        factory.stop();
        updateTimer.stop();
        appendLog("Factory simulation stopped");
        startButton.setEnabled(true);
    }

    /**
     * Очистка лога
     */
    private void clearLog() {
        logArea.setText("");
    }

    /**
     * Обновление всех компонентов GUI
     */
    private void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            int phase = factory.getCurrentPhase();
            String phaseName = factory.getCurrentPhaseName();
            updatePhase(phase, phaseName);

            updateRawBuffer(factory.getRawBuffer().size(), factory.getRawBuffer().getCapacity());
            updateMidBuffer(factory.getMidBuffer().size(), factory.getMidBuffer().getCapacity());
            updateReadyBuffer(factory.getReadyBuffer().size(), factory.getReadyBuffer().getCapacity());
        });
    }

    // ================== PUBLIC API ДЛЯ ОБНОВЛЕНИЯ GUI ==================

    /**
     * Обновление текущей фазы
     */
    public void updatePhase(int phase, String phaseName) {
        phaseNumberLabel.setText(String.valueOf(phase));
        phaseNameLabel.setText(phaseName);
    }

    /**
     * Обновление RAW буфера
     */
    public void updateRawBuffer(int current, int max) {
        rawBufferSizeLabel.setText(current + " / " + max);
        rawBufferProgress.setValue(current);
        rawBufferProgress.setString(current + " / " + max);
    }

    /**
     * Обновление MID буфера
     */
    public void updateMidBuffer(int current, int max) {
        midBufferSizeLabel.setText(current + " / " + max);
        midBufferProgress.setValue(current);
        midBufferProgress.setString(current + " / " + max);
    }

    /**
     * Обновление READY буфера
     */
    public void updateReadyBuffer(int current, int max) {
        readyBufferSizeLabel.setText(current + " / " + max);
        readyBufferProgress.setValue(current);
        readyBufferProgress.setString(current + " / " + max);
    }

    /**
     * Добавление сообщения в лог
     */
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}