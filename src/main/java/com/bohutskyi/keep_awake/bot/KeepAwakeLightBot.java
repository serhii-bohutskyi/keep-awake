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
        JCheckBox moveMouseCheckBox = new JCheckBox("Move Mouse", true);
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
                                // Get the current mouse position
                                Point currentMousePosition = MouseInfo.getPointerInfo().getLocation();
                                int x = (int) currentMousePosition.getX();
                                int y = (int) currentMousePosition.getY();

                                // Move the mouse by 1 pixel and back to its original position immediately
                                robot.mouseMove(x + 1, y); // Move by 1 pixel
                                Thread.sleep(10); // Short delay (10 milliseconds)
                                robot.mouseMove(x, y); // Move back to original position
                            }
                            // Simulate a small delay between movements
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
