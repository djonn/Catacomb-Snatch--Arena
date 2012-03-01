package com.mojang.mojam.gui;

import java.awt.event.KeyEvent;

import com.mojang.mojam.screen.*;


public class InfoMenu extends GuiMenu {

    // public static lol... ;)
    public static String ip = "";

    private int selectedItem = 0;
    private final int gameWidth;

    public InfoMenu(int gameWidth, int gameHeight) {
        super();
        this.gameWidth = gameWidth;

        addButton(new Button(TitleMenu.INFO_BACK_ID, 4, gameWidth - 128 - 20, gameHeight - 24 - 20));
    }
 
    public void render(Screen screen) {

        screen.clear(0);
//        screen.blit(Art.titles[1], 0, 10);
        screen.blit(Art.infomenu, 0, 0);

        super.render(screen);
    }

    @Override
    public void buttonPressed(Button button) {
    }

    public void keyPressed(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
            e.consume();
            buttons.get(selectedItem).postClick();
        }
    }

    public void keyReleased(KeyEvent arg0) {
    }

    public void keyTyped(KeyEvent arg0) {
    }

}
