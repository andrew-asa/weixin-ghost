package com.asa.ghost.weixin.spider;

import com.asa.base.ui.jfxsupport.GUIState;
import com.asa.base.ui.jfxsupport.debugger.DebuggerController;
import com.asa.base.utils.io.ClassPathResource;
import com.asa.ghost.weixin.spider.view.MainView;
import com.asa.ghost.weixin.spider.view.SplashScreenCustom;
import com.asa.base.ui.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.imageio.ImageIO;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author andrew_asa
 * @date 2021/3/26.
 */
@SpringBootApplication(scanBasePackages = {
        "com.asa.ghost.weixin.spider",
        "com.asa.base.ui"}
)
public class WeixinSpider extends AbstractJavaFxApplicationSupport {

    public static void main(String[] args) {

        launch(WeixinSpider.class, MainView.class, new SplashScreenCustom(), args);
    }

    public void start(final Stage stage) throws Exception {

        super.start(stage);
        setOnClose(stage);
    }

    public void beforeInitialView(final Stage stage, final ConfigurableApplicationContext ctx) {

        super.beforeInitialView(stage, ctx);
        setSystemTray(stage, ctx);
    }

    private void setOnClose(Stage stage) {

        stage.setOnCloseRequest(event -> {
            onClose();
        });
    }

    private void onClose() {

        System.exit(0);
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
        }
    }

    java.awt.SystemTray tray;

    java.awt.TrayIcon trayIcon;

    private void setSystemTray(final Stage stage, final ConfigurableApplicationContext ctx) {

        try {
            java.awt.Toolkit.getDefaultToolkit();
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                return;
            }
            tray = java.awt.SystemTray.getSystemTray();
            java.awt.Image image = ImageIO.read(new ClassPathResource("com/asa/ghost/weixin/spider/img/Jarvis.png").getInputStream());
            trayIcon = new java.awt.TrayIcon(image);
            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));
            java.awt.MenuItem openItem = new java.awt.MenuItem("主页");
            openItem.addActionListener(event -> Platform.runLater(this::showStage));
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);
            java.awt.MenuItem exitItem = new java.awt.MenuItem("退出");
            exitItem.addActionListener(event -> {
                onClose();
            });
            java.awt.MenuItem debugger = ctx.getBean(DebuggerController.class).createDebuggerMenu("调试","退出调试");
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            popup.addSeparator();
            popup.add(debugger);
            trayIcon.setPopupMenu(popup);
            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

    private void showStage() {

        if (GUIState.getStage() != null) {
            GUIState.getStage().show();
            GUIState.getStage().toFront();
        }
    }
}
