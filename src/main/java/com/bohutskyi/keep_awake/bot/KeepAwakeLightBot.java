package com.bohutskyi.keep_awake.bot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class KeepAwakeLightBot {

    private static boolean keepAwake = false;
    private static boolean moveMouse = false;
    private static Robot robot;

    public static void main(String[] args) {
        // Create UI components
        JFrame frame = new JFrame("Prevent Sleep Bot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLayout(new FlowLayout());

        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JCheckBox moveMouseCheckBox = new JCheckBox("Move Mouse");
        JLabel statusLabel = new JLabel("Status: Stopped");

        // Add components to frame
        frame.add(startButton);
        frame.add(stopButton);
        frame.add(moveMouseCheckBox);
        frame.add(statusLabel);

        // Set default button states
        stopButton.setEnabled(false);

        // Action listeners for buttons
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keepAwake = true;
                moveMouse = moveMouseCheckBox.isSelected();
                statusLabel.setText("Status: Running");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                
                new Thread(() -> {
                    try {
                        robot = new Robot();
                        while (keepAwake) {
                            if (moveMouse) {
                                // Move mouse in the same position (x: 100, y: 100)
                                robot.mouseMove(100, 100);
                            }
                            // Simulate a small delay
                            Thread.sleep(5000); // 5 seconds interval
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keepAwake = false;
                statusLabel.setText("Status: Stopped");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        // Display the frame
        frame.setVisible(true);
    }
}
