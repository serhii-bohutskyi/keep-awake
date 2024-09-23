package com.bohutskyi.keep_awake.bot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;

public class KeepAwakeSmartBot {

    // Static fields for UI components and behavior
    private static boolean keepAwake = false;
    private static Robot robot;
    private static int interval = 5;  // default interval in seconds
    private static long lastUserActionTime = System.currentTimeMillis();
    private static Timer botTimer = new Timer();
    private static TrayIcon trayIcon;

    // UI components
    private static JCheckBox moveMouseCheckBox;
    private static JCheckBox shiftKeyCheckBox;
    private static JCheckBox ctrlKeyCheckBox;
    private static JCheckBox leftClickCheckBox;
    private static JCheckBox rightClickCheckBox;
    private static JLabel statusLabel;
    private static JButton startButton;
    private static JButton stopButton;
    private static JSpinner intervalSpinner;
    private static JCheckBox fiveMinCheckBox;
    private static JCheckBox tenMinCheckBox;
    private static JCheckBox fifteenMinCheckBox;

    public static void main(String[] args) {
        try {
            robot = new Robot();
            // Disable JNativeHook logging to avoid cluttering the console
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        // Check if the system supports a system tray
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported on this system.");
            return;
        }

        // Setup UI
        JFrame frame = setupUI();

        // Register global mouse and keyboard listeners
        registerGlobalListeners();

        // Add to system tray
        configureSystemTray(frame);
    }

    // Method to set up the UI components
    private static JFrame setupUI() {
        JFrame frame = new JFrame("Smart Prevent Sleep Bot");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);  // Hide instead of exit
        frame.setSize(400, 400);
        frame.setLayout(new FlowLayout());

        // Define UI elements
        JLabel intervalLabel = new JLabel("Interval (seconds):");
        intervalSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        moveMouseCheckBox = new JCheckBox("Move Mouse");
        shiftKeyCheckBox = new JCheckBox("Press Shift");
        ctrlKeyCheckBox = new JCheckBox("Press Ctrl");
        leftClickCheckBox = new JCheckBox("Left Click");
        rightClickCheckBox = new JCheckBox("Right Click");

        JLabel minuteIntervalLabel = new JLabel("Or select interval in minutes:");
        fiveMinCheckBox = new JCheckBox("5 mins");
        tenMinCheckBox = new JCheckBox("10 mins");
        fifteenMinCheckBox = new JCheckBox("15 mins");

        statusLabel = new JLabel("Status: Stopped");
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");

        // Set default button states
        stopButton.setEnabled(false);

        // Action listener for Start button
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keepAwake = true;

                // Determine which interval to use
                if (fiveMinCheckBox.isSelected()) {
                    interval = 5 * 60;  // Convert minutes to seconds
                } else if (tenMinCheckBox.isSelected()) {
                    interval = 10 * 60;
                } else if (fifteenMinCheckBox.isSelected()) {
                    interval = 15 * 60;
                } else {
                    interval = (int) intervalSpinner.getValue();  // Use seconds if no minute checkbox is selected
                }

                statusLabel.setText("Status: Running");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                startBot();
            }
        });

        // Action listener for Stop button
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                keepAwake = false;
                statusLabel.setText("Status: Stopped");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                botTimer.cancel();
            }
        });

        // Add components to the frame
        frame.add(intervalLabel);
        frame.add(intervalSpinner);
        frame.add(minuteIntervalLabel);
        frame.add(fiveMinCheckBox);
        frame.add(tenMinCheckBox);
        frame.add(fifteenMinCheckBox);
        frame.add(moveMouseCheckBox);
        frame.add(shiftKeyCheckBox);
        frame.add(ctrlKeyCheckBox);
        frame.add(leftClickCheckBox);
        frame.add(rightClickCheckBox);
        frame.add(startButton);
        frame.add(stopButton);
        frame.add(statusLabel);

        // Add logic for enabling/disabling interval spinner when minute checkboxes are selected
        ItemListener minuteCheckboxListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (fiveMinCheckBox.isSelected() || tenMinCheckBox.isSelected() || fifteenMinCheckBox.isSelected()) {
                    intervalSpinner.setEnabled(false);  // Disable seconds spinner
                } else {
                    intervalSpinner.setEnabled(true);   // Enable seconds spinner
                }
            }
        };

        // Attach the same listener to all minute interval checkboxes
        fiveMinCheckBox.addItemListener(minuteCheckboxListener);
        tenMinCheckBox.addItemListener(minuteCheckboxListener);
        fifteenMinCheckBox.addItemListener(minuteCheckboxListener);

        // Display the frame
        frame.setVisible(true);

        // Hide to tray on close
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                frame.setVisible(false);  // Hide instead of exiting
            }
        });

        return frame;
    }

    // Method to configure the system tray
    private static void configureSystemTray(JFrame frame) {
        SystemTray tray = SystemTray.getSystemTray();
        Image trayImage = Toolkit.getDefaultToolkit().getImage("icon.png");  // Set your tray icon image here

        // Create popup menu for the tray icon
        PopupMenu popup = new PopupMenu();

        // Add "Open" menu item
        MenuItem openItem = new MenuItem("Open");
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(true);
                frame.setState(Frame.NORMAL);  // Restore window if minimized
            }
        });
        popup.add(openItem);

        // Add "Exit" menu item
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);  // Exit the application
            }
        });
        popup.add(exitItem);

        // Create the tray icon
        trayIcon = new TrayIcon(trayImage, "Smart Prevent Sleep Bot", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);  // Add the tray icon to the system tray
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        // Set tray icon tooltip
        trayIcon.setToolTip("Smart Prevent Sleep Bot is running in the background.");
    }

    // Method to register global mouse and keyboard listeners using JNativeHook
    private static void registerGlobalListeners() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }

        // Native key listener to detect key presses globally
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                resetTimer();  // Reset the timer on key press
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {}

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {}
        });

        // Native mouse listener to detect mouse clicks globally
        GlobalScreen.addNativeMouseListener(new NativeMouseListener() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent e) {
                resetTimer();  // Reset the timer on mouse click
            }

            @Override
            public void nativeMousePressed(NativeMouseEvent e) {}

            @Override
            public void nativeMouseReleased(NativeMouseEvent e) {}
        });

        // Native mouse motion listener to detect mouse movements globally
        GlobalScreen.addNativeMouseMotionListener(new NativeMouseMotionListener() {
            @Override
            public void nativeMouseMoved(NativeMouseEvent e) {
                resetTimer();  // Reset the timer on mouse movement
            }

            @Override
            public void nativeMouseDragged(NativeMouseEvent e) {
                resetTimer();  // Reset the timer on mouse drag
            }
        });
    }

    // Method to start the bot
    private static void startBot() {
        botTimer = new Timer();
        botTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastUserActionTime >= interval * 1000) {
                    performBotActions();  // Call to perform bot actions
                }
            }
        }, 0, 1000);  // Check every second
    }

    // Method to perform bot actions
    private static void performBotActions() {
        try {
            if (moveMouseCheckBox.isSelected()) {
                // Move the mouse slightly to prevent system from going idle
                Point currentLocation = MouseInfo.getPointerInfo().getLocation();
                int x = (int) currentLocation.getX();
                int y = (int) currentLocation.getY();

                // Move the mouse by 10 pixels in both directions and back
                robot.mouseMove(x + 10, y + 10);
                Thread.sleep(100); // short sleep to simulate natural movement
                robot.mouseMove(x, y);
                System.out.println("Mouse moved.");
            }

            if (shiftKeyCheckBox.isSelected()) {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyRelease(KeyEvent.VK_SHIFT);
                System.out.println("Shift key pressed.");
            }

            if (ctrlKeyCheckBox.isSelected()) {
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                System.out.println("Ctrl key pressed.");
            }

            if (leftClickCheckBox.isSelected()) {
                // Simulate a left mouse click
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);  // Left mouse button press
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);  // Left mouse button release
                System.out.println("Left mouse clicked.");
            }

            if (rightClickCheckBox.isSelected()) {
                // Simulate a right mouse click
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);  // Right mouse button press
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);  // Right mouse button release
                System.out.println("Right mouse clicked.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Method to reset the timer on user activity detection
    private static void resetTimer() {
        lastUserActionTime = System.currentTimeMillis();
        System.out.println("User activity detected, resetting timer...");
    }
}
