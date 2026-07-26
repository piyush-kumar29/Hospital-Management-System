package com.hospital.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StatCard extends JPanel {
    private final String title;
    private final String value;
    private final Color accentColor;

    public StatCard(String title, String value, Color accentColor) {
        this.title = title;
        this.value = value;
        this.accentColor = accentColor;
        
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setPreferredSize(new Dimension(200, 90));
        initUI();
    }

    private void initUI() {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIUtils.FONT_BOLD);
        titleLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g2.setColor(UIUtils.COLOR_CARD_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

        // Draw left accent bar
        g2.setColor(accentColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, 6, getHeight() - 1, 6, 6));

        // Border
        g2.setColor(new Color(226, 232, 240));
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

        g2.dispose();
        super.paintComponent(g);
    }
}
