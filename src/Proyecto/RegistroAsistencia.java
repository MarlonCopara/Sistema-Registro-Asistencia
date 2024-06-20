package Proyecto;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Erik
 */
public class RegistroAsistencia extends javax.swing.JInternalFrame implements Runnable {

    private byte[] huella = null;
    private String cedula;
    int hora, min, seg;
    Thread hilo;
    
    public DefaultTableModel horarios, registros;
    int horaEntradaMatutina = 0;
    int horaSalidaMatutina = 0;
    int horaEntradaVespertina = 0;
    int horaSalidaVespertina = 0;
    Conexion cc = new Conexion();
    Connection cn = cc.conexion();

    /**
     * Creates new form RegistroAsistencia
     */
    public RegistroAsistencia(String cedula) {
        initComponents();
        this.cedula = cedula;
        jlblFecha.setText(fecha());
        hilo = new Thread(this);
        hilo.start();
        jlblCedula.setText(this.cedula);
        String horario[] = {"Cedula", "Nombre", "Apellido", "Entrada", "Salida", "Jornada"};
        horarios = new DefaultTableModel(null, horario);
        jtblHorario.setModel(horarios);

        cargarHorario();
        cargarRegistro();

    }

    @Override
    public void run() {
        Thread current = Thread.currentThread();
        while (current == hilo) {
            hora();
            jlblReloj.setText(hora + ":" + min + ":" + seg);
            activarBoton();
        }
    }

    public String fecha() {
        Date fecha = new Date();
        SimpleDateFormat formatofecha = new SimpleDateFormat("YYYY/MM/dd");
        return formatofecha.format(fecha);
    }

    public void hora() {
        Calendar calendario = new GregorianCalendar();
        Date horaactual = new Date();
        calendario.setTime(horaactual);
        hora = calendario.get(Calendar.HOUR_OF_DAY);// > 9 ? "" + calendario.get(Calendar.HOUR_OF_DAY) : "0" + calendario.get(Calendar.HOUR_OF_DAY);
        min = calendario.get(Calendar.MINUTE);// > 9 ? "" + calendario.get(Calendar.MINUTE) : "0" + calendario.get(Calendar.MINUTE);
        seg = calendario.get(Calendar.SECOND);// > 9 ? "" + calendario.get(Calendar.SECOND) : "0" + calendario.get(Calendar.SECOND);
    }

    public void marcarAsistencia() {
        try {
            String fecha = new SimpleDateFormat("YYYY/MM/dd").format(Calendar.getInstance().getTime());
            DateTimeFormatter formatohora = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter formatohr = DateTimeFormatter.ofPattern("HH");
            LocalDateTime hora = LocalDateTime.now();
            String h = formatohora.format(hora);
            String aux = formatohr.format(hora);
            int hr = Integer.valueOf(aux);
            Conexion cc = new Conexion();
            Connection cn = cc.conexion();
            String sql = "";
            sql = "INSERT INTO registros (cedula,fecha,hora,jornada,tipo) values(?,?,?,?,?)";
            PreparedStatement psd = cn.prepareStatement(sql);
            psd.setString(1, this.cedula);
            psd.setString(2, fecha);
            psd.setString(3, h);
            if (hr <= horaSalidaMatutina || hr <= horaSalidaMatutina - 1) {
                psd.setString(4, "matutina");
            } else if (hr >= horaEntradaVespertina || hr >= horaEntradaVespertina - 1) {
                psd.setString(4, "vespertina");
            }
            if (hr == horaEntradaMatutina || hr == horaEntradaMatutina - 1 || hr == horaEntradaVespertina || hr == horaEntradaVespertina - 1) {
                psd.setString(5, "entrada");
            } else if (hr == horaSalidaMatutina || hr == horaSalidaVespertina || hr == horaSalidaMatutina - 1 || hr == horaSalidaVespertina - 1) {
                psd.setString(5, "salida");
            }
            int n = psd.executeUpdate();
            if (n > 0) {
                cargarRegistro();
                JOptionPane.showMessageDialog(this, "Ingreso con exito");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "ERROR: " + ex);
        }
    }

    public void marcarAsistenciaTarde() {

        try {
            String fecha = new SimpleDateFormat("dd/MM/YYYY").format(Calendar.getInstance().getTime());
            DateTimeFormatter formatohora = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime hora = LocalDateTime.now();
            String h = formatohora.format(hora);
            Conexion cc = new Conexion();
            Connection cn = cc.conexion();
            String sql = "";
            sql = "INSERT INTO asistenciasT (cedula, hora_entrada_tarde,hora_salida_tarde,fecha) values(?,?,?,?)";
            PreparedStatement psd = cn.prepareStatement(sql);
            psd.setString(1, this.cedula);
            psd.setString(2, h);
            psd.setString(3, "Campo vacio");
            psd.setString(4, fecha);
            int n = psd.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Ingreso con exito");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "ERROR: " + ex);
        }
    }

    private void cargarHorario() {

        try {
            Conexion cc = new Conexion();
            Connection cn = cc.conexion();
            String sql = "SELECT d.cedula, d.nombre, d.apellido, h.horaE, h.horaS, h.jornada FROM docentes as d, horarios as h WHERE d.cedula = '" + cedula + "' AND h.cedula = '" + cedula + "'";
            PreparedStatement ps = cn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String cedula = rs.getString("cedula");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String horaE = rs.getString("horaE");
                String horaS = rs.getString("horaS");
                String jornada = rs.getString("jornada");

                if (jornada.equals("matutina")) {
                    horaEntradaMatutina = Integer.valueOf(horaE);
                    horaSalidaMatutina = Integer.valueOf(horaS);
                }
                if (jornada.equals("vespertina")) {
                    horaEntradaVespertina = Integer.valueOf(horaE);
                    horaSalidaVespertina = Integer.valueOf(horaS);
                }
                String cargar[] = new String[6];
                cargar[0] = cedula;
                cargar[1] = nombre;
                cargar[2] = apellido;
                cargar[3] = horaE;
                cargar[4] = horaS;
                cargar[5] = jornada;
                horarios.addRow(cargar);
            }
        } catch (Exception e) {
            System.out.println("Error...." + e);
        }
    }

    private void cargarRegistro() {
        String registro[] = {"Cedula", "Nombre", "Apellido", "Hora Marcada", "Jornada", "Tipo"};
        registros = new DefaultTableModel(null, registro);
        jtblRegistro.setModel(registros);
        try {
            Conexion cc = new Conexion();
            Connection cn = cc.conexion();
            String sql = "SELECT d.cedula, d.nombre, d.apellido, r.hora, r.jornada, r.tipo FROM docentes as d, registros as r WHERE d.cedula = '" + cedula + "' AND r.cedula = '" + cedula + "'";
            PreparedStatement ps = cn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String cedula = rs.getString("cedula");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String hora = rs.getString("hora");
                String jornada = rs.getString("jornada");
                String tipo = rs.getString("tipo");
                String cargar[] = new String[6];
                cargar[0] = cedula;
                cargar[1] = nombre;
                cargar[2] = apellido;
                cargar[3] = hora;
                cargar[4] = jornada;
                cargar[5] = tipo;
                registros.addRow(cargar);
            }
        } catch (Exception e) {
            System.out.println("Error...." + e);
        }
    }

    private void activarBoton() {
        if (!controlAsistencia()) {
            if ((hora == horaEntradaMatutina && min <= 15) || (hora == horaEntradaMatutina - 1 && min >= 50)) {
                jbtnMarcarAsistencia.setEnabled(true);
                jbtnMarcarAsistencia.setText("Marcar Entrada Mañana");
            } else if ((hora == horaEntradaVespertina && min <= 15) || (hora == horaEntradaVespertina - 1 && min >= 50)) {
                jbtnMarcarAsistencia.setEnabled(true);
                jbtnMarcarAsistencia.setText("Marcar Entrada Tarde");
            } else if ((hora == horaSalidaMatutina && min <= 10) || (hora == horaSalidaMatutina - 1 && min >= 55)) {
                jbtnMarcarAsistencia.setEnabled(true);
                jbtnMarcarAsistencia.setText("Marcar Salida Mañana");
            } else if ((hora == horaSalidaVespertina && min <= 10) || (hora == horaSalidaVespertina - 1 && min >= 55)) {
                jbtnMarcarAsistencia.setEnabled(true);
                jbtnMarcarAsistencia.setText("Marcar Salida Tarde");
            } else {
                jbtnMarcarAsistencia.setEnabled(false);
                jbtnMarcarAsistencia.setText("MARCAR ASISTENCIA");
            }
        } else {
            jbtnMarcarAsistencia.setEnabled(false);
            jbtnMarcarAsistencia.setText("MARCAR ASISTENCIA");
        }
    }

    private boolean controlAsistencia() {
        String jornada = "", tipo = "";
        if (hora <= horaSalidaMatutina) {
            jornada = "matutina";
        } else if (hora >= horaEntradaVespertina) {
            jornada = "vespertina";
        }

        if (hora == horaEntradaMatutina || hora == horaEntradaVespertina || hora == horaEntradaMatutina - 1 || hora == horaEntradaVespertina - 1) {
            tipo = "entrada";
        } else if (hora == horaSalidaMatutina || hora == horaSalidaVespertina || hora == horaSalidaMatutina - 1 || hora == horaSalidaVespertina - 1) {
            tipo = "salida";
        }

        try {
            String sql = "SELECT * FROM registros WHERE cedula = '" + cedula + "' AND fecha = '" + fecha() + "' AND jornada = '" + jornada + "' AND tipo = '" + tipo + "'";
            PreparedStatement ps = cn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error...." + e);
        }
        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jlblReloj = new javax.swing.JLabel();
        jlblFecha = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jtblHorario = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jtblRegistro = new javax.swing.JTable();
        jbtnMarcarAsistencia = new javax.swing.JButton();
        jbtnCancelar = new javax.swing.JButton();
        jlblCedula = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 0, 0));

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("REGISTRO DE ASISTENCIA");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 684, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(34, 30, 30));
        jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jlblReloj.setFont(new java.awt.Font("Leelawadee", 1, 48)); // NOI18N
        jlblReloj.setForeground(new java.awt.Color(255, 255, 255));
        jlblReloj.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlblReloj.setText("10  :  30  :  59");

        jlblFecha.setFont(new java.awt.Font("Leelawadee UI", 1, 48)); // NOI18N
        jlblFecha.setForeground(new java.awt.Color(255, 255, 255));
        jlblFecha.setText("14/7/2022");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jlblFecha)
                .addGap(52, 52, 52)
                .addComponent(jlblReloj, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlblReloj, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                    .addComponent(jlblFecha))
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setBackground(new java.awt.Color(102, 0, 0));
        jLabel3.setFont(new java.awt.Font("Yu Gothic UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Jornadas Pendientes");
        jLabel3.setOpaque(true);

        jtblHorario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jtblHorario);

        jtblRegistro.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(jtblRegistro);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 694, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(139, Short.MAX_VALUE))
        );

        jbtnMarcarAsistencia.setBackground(java.awt.Color.green);
        jbtnMarcarAsistencia.setFont(new java.awt.Font("Yu Gothic UI", 0, 36)); // NOI18N
        jbtnMarcarAsistencia.setForeground(new java.awt.Color(255, 255, 255));
        jbtnMarcarAsistencia.setText("Registrar ");
        jbtnMarcarAsistencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnMarcarAsistenciaActionPerformed(evt);
            }
        });

        jbtnCancelar.setBackground(java.awt.Color.red);
        jbtnCancelar.setFont(new java.awt.Font("Yu Gothic UI", 0, 36)); // NOI18N
        jbtnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        jbtnCancelar.setText("Cancelar");
        jbtnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCancelarActionPerformed(evt);
            }
        });

        jlblCedula.setFont(new java.awt.Font("Leelawadee UI", 1, 18)); // NOI18N
        jlblCedula.setText("CEDULA");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jbtnMarcarAsistencia, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jbtnCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(320, 320, 320)
                .addComponent(jlblCedula)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlblCedula)
                .addGap(14, 14, 14)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbtnMarcarAsistencia, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbtnCancelar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jbtnMarcarAsistenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnMarcarAsistenciaActionPerformed
        marcarAsistencia();

    }//GEN-LAST:event_jbtnMarcarAsistenciaActionPerformed

    private void jbtnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnCancelarActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jbtnCancelarActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton jbtnCancelar;
    private javax.swing.JButton jbtnMarcarAsistencia;
    private javax.swing.JLabel jlblCedula;
    private javax.swing.JLabel jlblFecha;
    private javax.swing.JLabel jlblReloj;
    private javax.swing.JTable jtblHorario;
    private javax.swing.JTable jtblRegistro;
    // End of variables declaration//GEN-END:variables
}
