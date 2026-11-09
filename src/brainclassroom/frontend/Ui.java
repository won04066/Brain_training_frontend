package brainclassroom.frontend;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class Ui {
    static final Color BACKGROUND = new Color(247, 248, 244);
    static final Color GREEN = new Color(41, 103, 78);
    static final Color INK = new Color(38, 54, 48);

    private Ui() {
    }

    static JLabel label(String text, int size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.DIALOG, bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(INK);
        return label;
    }

    static JLabel paragraph(String text, int width) {
        return label("<html><div style='width:" + width + "px'>" + escape(text) + "</div></html>", 16, false);
    }

    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static JPanel panel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    static JButton button(String name, String text, Runnable action) {
        JButton button = new JButton(text);
        button.setName(name);
        button.setFont(new Font(Font.DIALOG, Font.BOLD, 17));
        button.setMargin(new java.awt.Insets(12, 24, 12, 24));
        button.addActionListener(event -> action.run());
        return button;
    }

    static void padding(JPanel panel, int amount) {
        panel.setBorder(BorderFactory.createEmptyBorder(amount, amount, amount, amount));
    }
}
