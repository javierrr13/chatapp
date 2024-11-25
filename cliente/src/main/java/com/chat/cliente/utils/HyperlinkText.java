package com.chat.cliente.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.chat.cliente.utils.UIUtils.*;

public class HyperlinkText extends JLabel {

    public HyperlinkText(String hyperlinkText, int xPos, int yPos, Runnable hyperlinkAction) {
        super(hyperlinkText);
        setForeground(COLOR_OUTLINE); // Utilizar constantes de UIUtils
        setFont(FONT_FORGOT_PASSWORD); // Utilizar constantes de UIUtils
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Añadir comportamiento del mouse
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hyperlinkAction.run(); // Ejecutar acción proporcionada
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setForeground(COLOR_OUTLINE.darker()); // Cambiar color al pasar el mouse
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setForeground(COLOR_OUTLINE); // Restaurar color al salir
            }
        });

        // Ajustar tamaño y posición del componente
        Dimension prefSize = getPreferredSize();
        setBounds(xPos, yPos, prefSize.width, prefSize.height);
    }
}
