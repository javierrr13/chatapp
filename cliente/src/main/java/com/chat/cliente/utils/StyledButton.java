//package com.chat.cliente.utils;
//
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//
//public class StyledButton extends JLabel {
//    private Color backgroundColor;
//    private Color hoverColor;
//    private Runnable action;
//
//    public StyledButton(String text, Color backgroundColor, Color hoverColor, Runnable action) {
//        super(text, SwingConstants.CENTER);
//        this.backgroundColor = backgroundColor;
//        this.hoverColor = hoverColor;
//        this.action = action;
//
//        setOpaque(true);
//        setBackground(backgroundColor);
//        setForeground(Color.WHITE);
//        setFont(UIUtils.FONT_GENERAL_UI);
//        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//
//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                if (action != null) {
//                    action.run();
//                }
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                setBackground(hoverColor);
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                setBackground(backgroundColor);
//            }
//        });
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        Graphics2D g2 = UIUtils.get2dGraphics(g);
//        g2.setColor(getBackground());
//        g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIUtils.ROUNDNESS, UIUtils.ROUNDNESS);
//
//        g2.setFont(getFont());
//        g2.setColor(getForeground());
//
//        FontMetrics metrics = g2.getFontMetrics();
//        int x = (getWidth() - metrics.stringWidth(getText())) / 2;
//        int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
//        g2.drawString(getText(), x, y);
//    }
//
//    @Override
//    public void setBackground(Color bg) {
//        this.backgroundColor = bg;
//        super.setBackground(bg);
//    }
//}
