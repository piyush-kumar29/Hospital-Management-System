package com.hospital.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class UIUtils {
    // Theme Colors
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);       // #2563EB Vibrant Blue
    public static final Color COLOR_PRIMARY_DARK = new Color(29, 78, 216);  // #1D4ED8
    public static final Color COLOR_SECONDARY = new Color(71, 85, 105);     // #475569 Slate
    public static final Color COLOR_SIDEBAR = new Color(15, 23, 42);        // #0F172A Dark Slate
    public static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);  // #1E293B
    public static final Color COLOR_BG = new Color(248, 250, 252);          // #F8FAFC Light Slate BG
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    
    // Status Colors
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);     // Emerald
    public static final Color COLOR_WARNING = new Color(245, 158, 11);     // Amber
    public static final Color COLOR_DANGER = new Color(239, 68, 68);       // Red
    public static final Color COLOR_INFO = new Color(14, 165, 233);        // Sky Blue

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BOLD);
        button.setForeground(fg);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JTextField createStyledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JPasswordField createStyledPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_REGULAR);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_REGULAR);
        cb.setBackground(Color.WHITE);
        return cb;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.setColor(new Color(226, 232, 240));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(36);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_MAIN);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String str = value.toString().toUpperCase();
                label.setOpaque(true);
                label.setFont(FONT_BOLD);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                
                if (str.contains("REQUESTED") || str.contains("PENDING") || str.contains("UNPAID")) {
                    label.setBackground(new Color(254, 243, 199)); // Light Amber
                    label.setForeground(new Color(180, 83, 9));
                } else if (str.contains("CONFIRMED") || str.contains("PAID") || str.contains("COMPLETED") || str.contains("DISPENSED")) {
                    label.setBackground(new Color(209, 250, 229)); // Light Emerald
                    label.setForeground(new Color(4, 120, 87));
                } else if (str.contains("CANCELED") || str.contains("DISCHARGED")) {
                    label.setBackground(new Color(254, 226, 226)); // Light Red
                    label.setForeground(new Color(185, 28, 28));
                } else if (str.contains("OCCUPIED")) {
                    label.setBackground(new Color(224, 231, 255)); // Light Indigo
                    label.setForeground(new Color(67, 56, 202));
                } else {
                    label.setBackground(new Color(241, 245, 249));
                    label.setForeground(COLOR_TEXT_MAIN);
                }
            }
            return label;
        }
    }
}
