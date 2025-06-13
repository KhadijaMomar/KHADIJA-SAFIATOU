package gui;

import java.awt.Color;

import javax.swing.JComboBox; // <-- AJOUTE CETTE IMPORTATION
import javax.swing.JList; // <-- AJOUTE CETTE IMPORTATION
import java.awt.Component; 
import javax.swing.DefaultListCellRenderer; 
import java.awt.Font;
import java.awt.Cursor;
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;

/**
 * Classe utilitaire pour définir les styles visuels de l'application GUI.
 * Centralise les couleurs, les polices et les bordures pour un design cohérent et agréable.
 */
public class Style {
    // Palette de couleurs inspirée d'un thème professionnel et moderne
    public static final Color PRIMARY_BACKGROUND = new Color(34, 40, 49); // Gris foncé/bleu nuit
    public static final Color SECONDARY_BACKGROUND = new Color(57, 62, 70); // Gris plus clair
    public static final Color ACCENT_COLOR = new Color(0, 173, 181); // Bleu turquoise vibrant
    public static final Color TEXT_COLOR = new Color(238, 238, 238); // Blanc cassé
    public static final Color ERROR_COLOR = new Color(255, 87, 87); // Rouge vif pour les erreurs
    public static final Color SUCCESS_COLOR = new Color(100, 200, 100); // Vert pour les succès
    public static final Color BUTTON_HOVER_COLOR = new Color(0, 140, 145); // Teinte plus foncée pour le survol
    public static final Font FONT_TEXT = new Font("Arial", Font.PLAIN, 12);
    public static final Color ACCENT_COLOR_LIGHT = new Color(200, 200, 255);
    public static final Color PRIMARY_COLOR = new Color(0, 120, 215);

    // Polices
    public static final Font FONT_TITLE = new Font("Inter", Font.BOLD, 28);
    public static final Font FONT_SUBTITLE = new Font("Inter", Font.BOLD, 18);
    public static final Font FONT_LABEL = new Font("Inter", Font.PLAIN, 14);
    public static final Font FONT_BUTTON = new Font("Inter", Font.BOLD, 14);
    public static final Font FONT_COMPACT_BUTTON = new Font("Inter", Font.BOLD, 12); // Nouvelle police compacte
    public static final Font FONT_TEXTFIELD = new Font("Inter", Font.PLAIN, 14);

    // Bordures
    public static final Border PADDING_BORDER = new EmptyBorder(20, 20, 20, 20);
    public static final Border COMPONENT_PADDING = new EmptyBorder(10, 10, 10, 10);
    public static final Border ROUNDED_BORDER = BorderFactory.createLineBorder(ACCENT_COLOR, 1, true);
    public static final Border FOCUS_BORDER = BorderFactory.createLineBorder(ACCENT_COLOR.brighter(), 2, true);
    public static final Border COMPACT_BUTTON_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1, true),
        new EmptyBorder(5, 10, 5, 10) // Bordure compacte
    );

    /**
     * Applique un style standard à un bouton.
     * @param button Le bouton à styliser.
     */
    public static void styleButton(JButton button) {
        button.setBackground(ACCENT_COLOR);
        button.setForeground(PRIMARY_BACKGROUND);
        button.setFont(FONT_BUTTON);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1, true),
            new EmptyBorder(10, 20, 10, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
            }
        });
    }

    /**
     * Style pour boutons compacts (texte court et taille réduite)
     * @param button Le bouton compact à styliser
     */
    public static void styleCompactButton(JButton button) {
        button.setBackground(ACCENT_COLOR);
        button.setForeground(PRIMARY_BACKGROUND);
        button.setFont(FONT_COMPACT_BUTTON); // Police plus petite
        button.setBorder(COMPACT_BUTTON_BORDER); // Bordure compacte
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
            }
        });
    }

    /**
     * Applique un style standard à un champ de texte.
     * @param textField Le champ de texte à styliser.
     */
    public static void styleTextField(JTextField textField) {
        textField.setBackground(SECONDARY_BACKGROUND);
        textField.setForeground(TEXT_COLOR);
        textField.setFont(FONT_TEXTFIELD);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        textField.setCaretColor(TEXT_COLOR);
    }

    /**
     * Applique un style standard à un champ de mot de passe.
     * @param passwordField Le champ de mot de passe à styliser.
     */
    public static void stylePasswordField(JPasswordField passwordField) {
        passwordField.setBackground(SECONDARY_BACKGROUND);
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setFont(FONT_TEXTFIELD);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        passwordField.setCaretColor(TEXT_COLOR);
    }

    /**
     * Applique un style standard à un label.
     * @param label Le label à styliser.
     */
    public static void styleLabel(JLabel label) {
        label.setForeground(TEXT_COLOR);
        label.setFont(FONT_LABEL);
    }
    

 public static void styleComboBox(JComboBox<?> comboBox) {
     comboBox.setBackground(SECONDARY_BACKGROUND);
     comboBox.setForeground(TEXT_COLOR);
     comboBox.setFont(FONT_TEXTFIELD); // Utilise la même police que les textfields
     comboBox.setBorder(BorderFactory.createCompoundBorder(
         BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1, true),
         new EmptyBorder(5, 5, 5, 5) // Moins de padding pour les combobox
     ));
     // Personnaliser le rendu pour un meilleur aspect
     comboBox.setRenderer(new DefaultListCellRenderer() {
         private static final long serialVersionUID = 1L;
         @Override
         public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             label.setBackground(isSelected ? ACCENT_COLOR.darker() : SECONDARY_BACKGROUND);
             label.setForeground(TEXT_COLOR);
             label.setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding pour les items
             return label;
         }
     });
     
 }


/**
 * Crée un JLabel stylisé avec la police et la couleur de texte standard.
 * @param text Le texte du label.
 * @return Un JLabel stylisé.
 */
public static JLabel createStyledLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(FONT_TEXTFIELD); // Ou FONT_BUTTON si tu préfères
    label.setForeground(TEXT_COLOR);
    return label;
}

public static JButton createStyledButton(String string) {
	// TODO Auto-generated method stub
	return null;
}

}
