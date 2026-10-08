package Generador;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.util.List;

public class Ventana extends JFrame {

    // ── Componentes ──────────────────────────────────────────────────────────
    private JTextField  txtContrasenia;
    private JSlider     sliderLongitud;
    private JLabel      lblLongitud;
    private JCheckBox   chkMini, chkMayus, chkSimbolos, chkNumeros;
    private JButton     btnGenerar, btnCopiar, btnLimpiar, btnGuardar, btnEliminar;
    private JTable      tabla;
    private DefaultTableModel modeloTabla;
    private JLabel      lblRuta;

    // ── Lógica ───────────────────────────────────────────────────────────────
    private final Cadena        cadena  = new Cadena();
    private final GestorArchivo gestor  = new GestorArchivo();

    // ── Colores ──────────────────────────────────────────────────────────────
    private static final Color COLOR_FONDO     = new Color(30, 30, 40);
    private static final Color COLOR_PANEL     = new Color(40, 42, 58);
    private static final Color COLOR_ACENTO    = new Color(94, 129, 244);
    private static final Color COLOR_ACENTO2   = new Color(67, 210, 170);
    private static final Color COLOR_TEXTO     = new Color(220, 220, 235);
    private static final Color COLOR_SUBTEXTO  = new Color(140, 145, 175);
    private static final Color COLOR_PELIGRO   = new Color(235, 87, 87);
    private static final Color COLOR_INPUT     = new Color(22, 23, 33);

    public Ventana() {
        setTitle("Password Generator Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        try {
            // Cargar icono en multiples tamanios para que se vea bien en todos los contextos
            java.util.List<java.awt.Image> icons = new java.util.ArrayList<>();
            for (String res : new String[]{"/img/logo.png"}) {
                java.net.URL url = getClass().getResource(res);
                if (url != null) {
                    java.awt.Image img = new ImageIcon(url).getImage();
                    icons.add(img);
                    // Agregar versiones escaladas
                    icons.add(img.getScaledInstance(128, 128, java.awt.Image.SCALE_SMOOTH));
                    icons.add(img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH));
                    icons.add(img.getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH));
                    icons.add(img.getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
                }
            }
            if (!icons.isEmpty()) setIconImages(icons);
        } catch (Exception ignored) {}

        initComponents();
        cargarTabla();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(COLOR_FONDO);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setContentPane(root);

        root.add(crearPanelGenerador(), BorderLayout.NORTH);
        root.add(crearPanelGuardadas(),  BorderLayout.CENTER);
        root.add(crearPanelRuta(),        BorderLayout.SOUTH);
    }

    // ── Panel superior: generador ─────────────────────────────────────────────
    private JPanel crearPanelGenerador() {
        JPanel p = panelRedondeado("🔐  Generador de Contraseñas");
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        // ── Contraseña generada
        txtContrasenia = new JTextField(28);
        txtContrasenia.setEditable(false);
        estilizarCampo(txtContrasenia);
        txtContrasenia.setFont(new Font("Monospaced", Font.BOLD, 16));
        txtContrasenia.setForeground(COLOR_ACENTO2);

        g.gridx = 0; g.gridy = 0; g.gridwidth = 4; g.weightx = 1.0;
        p.add(txtContrasenia, g);

        // ── Slider longitud
        sliderLongitud = new JSlider(4, 40, 12);
        sliderLongitud.setBackground(COLOR_PANEL);
        sliderLongitud.setForeground(COLOR_TEXTO);
        sliderLongitud.setMajorTickSpacing(4);
        sliderLongitud.setPaintTicks(true);
        sliderLongitud.setMinorTickSpacing(1);
        sliderLongitud.setSnapToTicks(false);

        lblLongitud = labelEstilo("Longitud: 12", COLOR_SUBTEXTO);

        sliderLongitud.addChangeListener(e ->
            lblLongitud.setText("Longitud: " + sliderLongitud.getValue()));

        g.gridx = 0; g.gridy = 1; g.gridwidth = 3;
        p.add(sliderLongitud, g);
        g.gridx = 3; g.gridwidth = 1; g.weightx = 0;
        p.add(lblLongitud, g);

        // ── Checkboxes
        chkMini     = checkEstilo("abc", true);
        chkMayus    = checkEstilo("ABC", true);
        chkSimbolos = checkEstilo("@#&", true);
        chkNumeros  = checkEstilo("123", true);

        JPanel pChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pChecks.setBackground(COLOR_PANEL);
        pChecks.add(labelEstilo("Incluir:", COLOR_SUBTEXTO));
        pChecks.add(chkMini); pChecks.add(chkMayus);
        pChecks.add(chkSimbolos); pChecks.add(chkNumeros);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 4; g.weightx = 1.0;
        p.add(pChecks, g);

        // ── Botones
        btnGenerar = boton("⚡ Generar",  COLOR_ACENTO);
        btnCopiar  = boton("📋 Copiar",   COLOR_ACENTO);
        btnLimpiar = boton("🗑 Limpiar",  new Color(70, 75, 100));
        btnGuardar = boton("💾 Guardar",  COLOR_ACENTO2);

        btnGenerar.addActionListener(e -> generar());
        btnCopiar .addActionListener(e -> copiar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());

        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pBotones.setBackground(COLOR_PANEL);
        pBotones.add(btnGenerar); pBotones.add(btnCopiar);
        pBotones.add(btnLimpiar); pBotones.add(btnGuardar);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 4;
        p.add(pBotones, g);

        return p;
    }

    // ── Panel central: contraseñas guardadas ──────────────────────────────────
    private JPanel crearPanelGuardadas() {
        JPanel p = panelRedondeado("🗂  Contraseñas Guardadas");
        p.setLayout(new BorderLayout(0, 8));

        modeloTabla = new DefaultTableModel(new String[]{"Etiqueta", "Contraseña"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modeloTabla);
        tabla.setBackground(COLOR_INPUT);
        tabla.setForeground(COLOR_TEXTO);
        tabla.setSelectionBackground(COLOR_ACENTO);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setRowHeight(28);
        tabla.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tabla.getTableHeader().setBackground(COLOR_PANEL);
        tabla.getTableHeader().setForeground(COLOR_SUBTEXTO);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabla.setGridColor(new Color(50, 53, 70));
        tabla.setShowGrid(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));

        // Columna contraseña oculta con asteriscos
        tabla.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String v = val == null ? "" : val.toString();
                setText("•".repeat(v.length()));
                setBackground(sel ? COLOR_ACENTO : COLOR_INPUT);
                setForeground(sel ? Color.WHITE : COLOR_SUBTEXTO);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(560, 180));
        scroll.getViewport().setBackground(COLOR_INPUT);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(55, 58, 80)));

        // Botones de tabla
        btnEliminar = boton("🗑 Eliminar seleccionada", COLOR_PELIGRO);
        JButton btnCopiarGuardada = boton("📋 Copiar contraseña", new Color(70, 75, 100));
        JButton btnMostrar = boton("👁 Mostrar/Ocultar", new Color(70, 75, 100));

        btnEliminar.addActionListener(e -> eliminarSeleccionada());
        btnCopiarGuardada.addActionListener(e -> copiarSeleccionada());

        // Toggle mostrar/ocultar contraseñas
        final boolean[] mostrar = {false};
        btnMostrar.addActionListener(e -> {
            mostrar[0] = !mostrar[0];
            if (mostrar[0]) {
                tabla.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object val,
                            boolean sel, boolean foc, int row, int col) {
                        super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                        setBackground(sel ? COLOR_ACENTO : COLOR_INPUT);
                        setForeground(sel ? Color.WHITE : COLOR_ACENTO2);
                        setFont(new Font("Monospaced", Font.PLAIN, 13));
                        return this;
                    }
                });
            } else {
                tabla.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object val,
                            boolean sel, boolean foc, int row, int col) {
                        super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                        String v = val == null ? "" : val.toString();
                        setText("•".repeat(v.length()));
                        setBackground(sel ? COLOR_ACENTO : COLOR_INPUT);
                        setForeground(sel ? Color.WHITE : COLOR_SUBTEXTO);
                        return this;
                    }
                });
            }
            tabla.repaint();
        });

        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pBotones.setBackground(COLOR_PANEL);
        pBotones.add(btnCopiarGuardada);
        pBotones.add(btnMostrar);
        pBotones.add(btnEliminar);

        p.add(scroll, BorderLayout.CENTER);
        p.add(pBotones, BorderLayout.SOUTH);

        return p;
    }

    // ── Panel inferior: ruta del archivo ─────────────────────────────────────
    private JPanel crearPanelRuta() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        p.setBackground(COLOR_FONDO);
        lblRuta = labelEstilo("📁 " + gestor.getRutaArchivo(), new Color(80, 85, 115));
        lblRuta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(lblRuta);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Acciones
    // ─────────────────────────────────────────────────────────────────────────

    private void generar() {
        String pass = cadena.generarContrasenia(
            sliderLongitud.getValue(),
            chkMini.isSelected(), chkMayus.isSelected(),
            chkSimbolos.isSelected(), chkNumeros.isSelected()
        );
        txtContrasenia.setText(pass);
    }

    private void copiar() {
        String pass = txtContrasenia.getText();
        if (pass.isEmpty()) { mostrarInfo("Primero generá una contraseña."); return; }
        Toolkit.getDefaultToolkit().getSystemClipboard()
               .setContents(new StringSelection(pass), null);
        mostrarInfo("¡Contraseña copiada al portapapeles!");
    }

    private void limpiar() {
        txtContrasenia.setText("");
    }

    private void guardar() {
        String pass = txtContrasenia.getText();
        if (pass.isEmpty()) { mostrarInfo("Primero generá una contraseña."); return; }

        String etiqueta = JOptionPane.showInputDialog(this,
            "¿Cuál es el nombre de esta contraseña?\n(Ej: Gmail, Netflix, Banco…)",
            "Guardar contraseña", JOptionPane.QUESTION_MESSAGE);

        if (etiqueta == null || etiqueta.trim().isEmpty()) return;

        try {
            gestor.guardar(etiqueta.trim(), pass);
            cargarTabla();
            mostrarInfo("✅ Contraseña guardada como \"" + etiqueta.trim() + "\"");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    private void eliminarSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { mostrarInfo("Seleccioná una fila primero."); return; }

        String etiqueta = (String) modeloTabla.getValueAt(fila, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar la contraseña de \"" + etiqueta + "\"?",
            "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            gestor.eliminar(fila);
            cargarTabla();
        } catch (Exception ex) {
            mostrarError("Error al eliminar: " + ex.getMessage());
        }
    }

    private void copiarSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { mostrarInfo("Seleccioná una fila primero."); return; }
        String pass = (String) modeloTabla.getValueAt(fila, 1);
        Toolkit.getDefaultToolkit().getSystemClipboard()
               .setContents(new StringSelection(pass), null);
        mostrarInfo("✅ Contraseña copiada al portapapeles.");
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<String[]> lista = gestor.cargarTodas();
            for (String[] entrada : lista) {
                modeloTabla.addRow(entrada);
            }
        } catch (Exception ex) {
            mostrarError("Error al cargar contraseñas: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de UI
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel panelRedondeado(String titulo) {
        JPanel p = new JPanel();
        p.setBackground(COLOR_PANEL);
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 65, 90), 1),
            titulo,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13),
            COLOR_ACENTO
        );
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(8, 0, 8, 0),
            BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(8, 10, 10, 10))
        ));
        return p;
    }

    private void estilizarCampo(JTextField tf) {
        tf.setBackground(COLOR_INPUT);
        tf.setForeground(COLOR_TEXTO);
        tf.setCaretColor(COLOR_ACENTO);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 65, 90)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private JLabel labelEstilo(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setForeground(color);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private JCheckBox checkEstilo(String texto, boolean sel) {
        JCheckBox c = new JCheckBox(texto, sel);
        c.setBackground(COLOR_PANEL);
        c.setForeground(COLOR_TEXTO);
        c.setFont(new Font("Monospaced", Font.BOLD, 12));
        c.setFocusPainted(false);
        return c;
    }

    private JButton boton(String texto, Color colorFondo) {
        JButton b = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? colorFondo.darker() :
                            getModel().isRollover() ? colorFondo.brighter() : colorFondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, 32));
        return b;
    }

    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        EventQueue.invokeLater(Ventana::new);
    }
}
