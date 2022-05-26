package com.xdavide9.jnotepad;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.xdavide9.jnotepad.configuration.Configuration;
import com.xdavide9.jnotepad.configuration.ConfigurationSerializer;
import com.xdavide9.jnotepad.gui.Gui;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Objects;

@Slf4j
public class JNotepad {

    private ConfigurationSerializer configurationSerializer;
    private Configuration configuration;
    private Gui gui;

    private static String pathToOpen;

    public static final String APP_NAME = "BetterNotePad";
    public static final String DEFAULT_FONT_FAMILY = "Segoe UI";
    public static final String INITIAL_FILE_NAME = "Untitled";

    public static void main(String[] args) {
        pathToOpen = retrievePath(args);
        customizeLaf();
        SwingUtilities.invokeLater(JNotepad::new);
    }

    private static String retrievePath(String[] args) {
        try {
            return args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            log.info("No file to open provided to the arguments");
        }

        return null;
    }

    private static void customizeLaf() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf() {
                //removing the content of this method because it was responsible for producing an annoying beep sound
                @Override
                public void provideErrorFeedback(Component component) {}
            });
        } catch (UnsupportedLookAndFeelException e) {
            log.error("Could not set up Look and Feel", e);
        }

        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("defaultFont", new Font(JNotepad.DEFAULT_FONT_FAMILY, Font.PLAIN, 14));
        log.info("Successfully set up and customized Look and Feel");
    }

    private JNotepad() {
        createGui();
        if (Objects.nonNull(pathToOpen))
            open(pathToOpen);
        saveConfiguration();
    }

    private void createGui() {
        configurationSerializer = new ConfigurationSerializer();
        configuration = configurationSerializer.deserialize();

        if (Objects.isNull(configuration))
            configuration = new Configuration(JNotepad.INITIAL_FILE_NAME,
                    0, 0, 970, 600,
                    new Font(JNotepad.DEFAULT_FONT_FAMILY, Font.PLAIN, 22),
                    true);

        gui = new Gui(JNotepad.INITIAL_FILE_NAME,
                configuration.getX(),
                configuration.getY(),
                configuration.getWidth(),
                configuration.getHeight(),
                configuration.getFont(),
                configuration.isLineWrap());

        log.info("Successfully created Gui with configuration = {}", configuration);
    }

    private void open(String path) {
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while((line = bufferedReader.readLine()) != null)
                gui.getTextArea().append(line + "\n");

            fileReader.close();
            bufferedReader.close();

            StringBuilder builder = new StringBuilder(path);
            String fileName = builder.substring(builder.lastIndexOf("\\") + 1);

            gui.getFrame().setTitle(fileName);
            gui.getFileService().setPath(pathToOpen);

            log.info("Successfully opened file provided from the arguments");
        } catch (Exception e) {
            log.error("Could not open file provided from the arguments", e);
        }
    }

    private void saveConfiguration() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Rectangle rect = gui.getFrame().getBounds();
            configuration.setX(rect.x);
            configuration.setY(rect.y);
            configuration.setWidth(rect.width);
            configuration.setHeight(rect.height);
            configuration.setFont(gui.getTextArea().getFont());
            configuration.setLineWrap(gui.getTextArea().getLineWrap());
            configurationSerializer.serialize(configuration);
            log.info("Successfully saved configuration");
        }));
    }
}
