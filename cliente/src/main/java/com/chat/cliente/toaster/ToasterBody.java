package com.chat.cliente.toaster;



import javax.swing.*;

import com.chat.cliente.utils.UIUtils;

import java.awt.*;

class ToasterBody extends JPanel {
    private static final int TOAST_PADDING = 15;
    private final int toastWidth;
    private final String message;
    private final Color bgColor;
    private volatile boolean stopDisplaying;
    private int heightOfToast, stringPosX, stringPosY, yPos;
    private final JPanel panelToToastOn;

    public ToasterBody(JPanel panelToToastOn, String message, Color bgColor, int yPos) {
        this.panelToToastOn = panelToToastOn;
        this.message = message;
        this.yPos = yPos;
        this.bgColor = bgColor;

        FontMetrics metrics = getFontMetrics(UIUtils.FONT_GENERAL_UI);
        int stringWidth = metrics.stringWidth(this.message);

        toastWidth = stringWidth + (TOAST_PADDING * 2);
        heightOfToast = metrics.getHeight() + TOAST_PADDING;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setOpaque(false);
        setBounds((panelToToastOn.getWidth() - toastWidth) / 2, -heightOfToast, toastWidth, heightOfToast);

        stringPosX = (toastWidth - stringWidth) / 2;
        stringPosY = (heightOfToast - metrics.getHeight()) / 2 + metrics.getAscent();

        animateToPosition(yPos);
    }

    private void animateToPosition(int targetY) {
        new Thread(() -> {
            synchronized (this) {
                while (getBounds().y != targetY) {
                    int step = Math.max(Math.abs(targetY - getBounds().y) / 10, 1);
                    if (getBounds().y < targetY) {
                        setBounds((panelToToastOn.getWidth() - toastWidth) / 2, getBounds().y + step, toastWidth, heightOfToast);
                    } else {
                        setBounds((panelToToastOn.getWidth() - toastWidth) / 2, getBounds().y - step, toastWidth, heightOfToast);
                    }
                    repaint();
                    try {
                        Thread.sleep(5);
                    } catch (Exception ignored) {
                    }
                }
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = UIUtils.get2dGraphics(g);
        super.paintComponent(g2);

        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIUtils.ROUNDNESS, UIUtils.ROUNDNESS);

        g2.setFont(UIUtils.FONT_GENERAL_UI);
        g2.setColor(Color.white);
        g2.drawString(message, stringPosX, stringPosY);
    }

    public int getHeightOfToast() {
        return heightOfToast;
    }

    public synchronized boolean getStopDisplaying() {
        return stopDisplaying;
    }

    public synchronized void setStopDisplaying(boolean stopDisplaying) {
        this.stopDisplaying = stopDisplaying;
    }

    public void setyPos(int yPos) {
        this.yPos = yPos;
        animateToPosition(yPos);
    }

    public int getyPos() {
        return yPos;
    }
}
