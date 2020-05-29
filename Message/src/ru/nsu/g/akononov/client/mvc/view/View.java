package ru.nsu.g.akononov.client.mvc.view;

import ru.nsu.g.akononov.client.mvc.controller.Controller;
import ru.nsu.g.akononov.client.mvc.model.Observer;
import ru.nsu.g.akononov.client.mvc.model.OutputType;
import ru.nsu.g.akononov.client.mvc.model.TcpClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;

public class View implements Observer {
    private final Controller controller;

    private final JFrame appFrame = new JFrame();
    private final JTextPane messages = new JTextPane();

    private final JTextField textToSend = new JTextField("");

    private final JButton usersButton = new JButton("Users");
    private final JButton exitButton = new JButton("Leave");

    private final JPanel bottomPanel = new JPanel(new BorderLayout());
    private final JPanel mainPanel = new JPanel(new BorderLayout());


    public View(TcpClient model, Controller controller) {
        this.controller = controller;
        model.registerObserver(this);
        EventQueue.invokeLater(this::setSwingSettings);
    }

    private void addLogPanel() {
        MutableAttributeSet set = new javax.swing.text.SimpleAttributeSet(messages.getParagraphAttributes());
        StyleConstants.setLineSpacing(set, (float)+0.5);
        messages.setParagraphAttributes(set, false);

        messages.setText("Enter your name:\n");
        messages.setEditable(false);

        JScrollPane jScrollPane = new JScrollPane(messages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(jScrollPane, BorderLayout.CENTER);
    }

    private void addMessageField() {
        textToSend.setEditable(true);
        textToSend.addActionListener(e -> {
            String textMessage = textToSend.getText();
            if (textMessage != null) {
                controller.sendMessage(textMessage);
            }
            textToSend.setText(null);
        });

        bottomPanel.add(textToSend, BorderLayout.CENTER);
    }

    private void addButtons() {
        exitButton.addActionListener(e -> {
            controller.sendMessage("/exit");
        });

        usersButton.addActionListener(e -> {
            controller.sendMessage("/users");
        });

        bottomPanel.add(usersButton, BorderLayout.WEST);
        bottomPanel.add(exitButton, BorderLayout.EAST);
    }

    private void setBorders(int borderSize) {
        bottomPanel.setBorder(new EmptyBorder(borderSize, borderSize, borderSize, borderSize));
        mainPanel.setBorder(new EmptyBorder(borderSize, borderSize, borderSize, borderSize));
    }

    private synchronized void setSwingSettings() {
        appFrame.setTitle("Client");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        appFrame.setSize(screenSize.width / 4, screenSize.height / 2);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setLocationRelativeTo(null);

        addLogPanel();
        addMessageField();
        addButtons();

        setBorders(screenSize.height / 150);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        appFrame.getContentPane().add(mainPanel);
        appFrame.setVisible(true);
    }

    @Override
    public synchronized void updateView(OutputType type, String text) {

        StyledDocument doc = messages.getStyledDocument();
        Style style = messages.addStyle("", null);

        switch (type) {
            case ERROR: {
                StyleConstants.setForeground(style, Color.RED);
                break;
            }
            case SHARED: {
                StyleConstants.setForeground(style, Color.BLACK);
                break;
            }
            case SYSTEM: {
                StyleConstants.setForeground(style, Color.BLUE);
                break;
            }
        }

        SwingUtilities.invokeLater(() -> {
            try {
                doc.insertString(doc.getLength(), text + "\n", style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
}
