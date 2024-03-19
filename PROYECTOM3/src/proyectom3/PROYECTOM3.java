package proyectom3;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.toedter.calendar.JCalendar;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import java.sql.*;

public class PROYECTOM3 extends JFrame {

    private JLabel lblTipoCoche, lblCostoAuto, lblPrecioVenta, lblGastosMantenimiento, lblImpuestos, lblBeneficios;
    private JTextField txtCostoAuto, txtPrecioVenta, txtGastosMantenimiento, txtImpuestos;
    private JButton btnCalcular, btnVerBeneficios;
    private JCalendar calendarInicio, calendarFin;
    private Map<LocalDate, Double> beneficiosPorFecha;
    private DecimalFormat formatoDecimal;
    private JTextField txtTipoCoche;

    private static final String FILENAME = "beneficios.txt";

    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;

    public PROYECTOM3() {
        initComponents();
        cargarBeneficios();
        formatoDecimal = new DecimalFormat("#,###", new DecimalFormatSymbols(new Locale("es", "ES")));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/netbeans_ct";
            String usuario = "CarlosTarre";
            String contraseña = "1234";
            connection = DriverManager.getConnection(url, usuario, contraseña);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        setTitle("Calculadora de Beneficios del Usuario");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        lblTipoCoche = new JLabel("Tipo de coche:");
        lblCostoAuto = new JLabel("Costo del automóvil:");
        lblPrecioVenta = new JLabel("Precio de venta:");
        lblGastosMantenimiento = new JLabel("Gastos de mantenimiento:");
        lblImpuestos = new JLabel("Impuestos:");
        lblBeneficios = new JLabel("Beneficios:");

        txtTipoCoche = new JTextField(10);
        txtCostoAuto = new JTextField(10);
        txtPrecioVenta = new JTextField(10);
        txtGastosMantenimiento = new JTextField(10);
        txtImpuestos = new JTextField(10);

        btnCalcular = new JButton("Calcular");
        btnCalcular.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calcularBeneficios();
            }
        });

        btnVerBeneficios = new JButton("Ver Beneficios");
        btnVerBeneficios.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                verBeneficios();
            }
        });

        calendarInicio = new JCalendar();
        calendarFin = new JCalendar();

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblTipoCoche)
                        .addComponent(lblCostoAuto)
                        .addComponent(lblPrecioVenta)
                        .addComponent(lblGastosMantenimiento)
                        .addComponent(lblImpuestos)
                        .addComponent(lblBeneficios))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(txtTipoCoche)
                        .addComponent(txtCostoAuto)
                        .addComponent(txtPrecioVenta)
                        .addComponent(txtGastosMantenimiento)
                        .addComponent(txtImpuestos)
                        .addComponent(btnCalcular)
                        .addComponent(btnVerBeneficios)
                        .addComponent(calendarInicio)
                        .addComponent(calendarFin))
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblTipoCoche)
                        .addComponent(txtTipoCoche))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCostoAuto)
                        .addComponent(txtCostoAuto))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPrecioVenta)
                        .addComponent(txtPrecioVenta))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblGastosMantenimiento)
                        .addComponent(txtGastosMantenimiento))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblImpuestos)
                        .addComponent(txtImpuestos))
                .addComponent(btnCalcular)
                .addComponent(btnVerBeneficios)
                .addComponent(lblBeneficios)
                .addComponent(calendarInicio)
                .addComponent(calendarFin)
        );

        add(panel);
    }

   private void calcularBeneficios() {
    // Obtener la fecha actual
    LocalDate fechaActual = LocalDate.now();

    // Calcular beneficios
    double precioVenta = parsearNumero(txtPrecioVenta.getText());
    double costoAuto = parsearNumero(txtCostoAuto.getText());
    double gastosMantenimiento = parsearNumero(txtGastosMantenimiento.getText());
    double impuestos = parsearNumero(txtImpuestos.getText());

    // Calcular los beneficios
    double beneficios = precioVenta - costoAuto - gastosMantenimiento - impuestos;

    // Redondear a tres decimales
    beneficios = Math.round(beneficios * 1000.0) / 1000.0;

    // Mostrar los beneficios calculados
    lblBeneficios.setText("Beneficios: $" + String.format("%.3f", beneficios));

    // Verificar si ya hay beneficios acumulados para la fecha actual
    Double beneficiosAcumulados = beneficiosPorFecha.getOrDefault(fechaActual, 0.0);

    // Acumular los beneficios
    beneficiosAcumulados += beneficios;

    // Actualizar el mapa con los beneficios acumulados
    beneficiosPorFecha.put(fechaActual, beneficiosAcumulados);

    // Mostrar los beneficios totales hasta la fecha actual
    double beneficiosTotales = calcularBeneficiosTotales();
    lblBeneficios.setText("Beneficios totales: $" + formatoDecimal.format(beneficiosTotales));

    // Guardar los beneficios acumulados
    guardarBeneficios();
}

private double parsearNumero(String texto) {
    
    // Eliminar cualquier separador de miles y convertir el texto a un valor numérico
    texto = texto.replaceAll("\\.", "");
    return Double.parseDouble(texto);
}

    private double calcularBeneficiosTotales() {
        // Calcular los beneficios totales acumulados hasta la fecha actual
        double beneficiosTotales = 0;
        for (double beneficio : beneficiosPorFecha.values()) {
            beneficiosTotales += beneficio;
        }
        return beneficiosTotales;
    }

    private void verBeneficios() {
        // Obtener el rango de fechas seleccionado
        LocalDate fechaInicio = calendarInicio.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate fechaFin = calendarFin.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        // Calcular los beneficios dentro del rango de fechas
        double beneficiosEnRango = 0;
        for (Map.Entry<LocalDate, Double> entry : beneficiosPorFecha.entrySet()) {
            LocalDate fecha = entry.getKey();
            if (fecha.isEqual(fechaInicio) || fecha.isEqual(fechaFin) ||
                    (fecha.isAfter(fechaInicio) && fecha.isBefore(fechaFin))) {
                beneficiosEnRango += entry.getValue();
            }
        }

        // Mostrar los beneficios en el rango de fechas
        JOptionPane.showMessageDialog(this, "Beneficios desde " + fechaInicio + " hasta " + fechaFin + ": $" + formatoDecimal.format(beneficiosEnRango));
    }

    private void cargarBeneficios() {
        beneficiosPorFecha = new HashMap<>();
        File file = new File(FILENAME);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                LocalDate fecha = LocalDate.parse(parts[0]);
                double beneficio = Double.parseDouble(parts[1]);
                // Acumular los beneficios en lugar de sobrescribirlos
                beneficiosPorFecha.merge(fecha, beneficio, Double::sum);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void guardarBeneficios() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME))) {
            for (Map.Entry<LocalDate, Double> entry : beneficiosPorFecha.entrySet()) {
                LocalDate fecha = entry.getKey();
                double beneficio = entry.getValue();
                writer.write(fecha.toString() + "," + beneficio);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        // Cierre de la conexión al cerrar la ventana
        super.dispose();
        try {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PROYECTOM3().setVisible(true);
            }
        });
    }
}

