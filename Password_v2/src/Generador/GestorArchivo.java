package Generador;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Guarda y carga contraseñas cifradas con AES-256-GCM.
 * Archivo: ~/PasswordApp/passwords.dat
 *
 * Formato de cada línea (Base64):
 *   salt:iv:etiqueta_cifrada:contrasenia_cifrada
 */
public class GestorArchivo {

    private static final String CARPETA    = System.getProperty("user.home") + File.separator + "PasswordApp";
    private static final String ARCHIVO    = CARPETA + File.separator + "passwords.dat";

    private static final String ALGORITMO  = "AES/GCM/NoPadding";
    private static final int    TAG_BITS   = 128;
    private static final int    IV_BYTES   = 12;
    private static final int    SALT_BYTES = 16;
    private static final int    ITER       = 65536;
    private static final int    KEY_BITS   = 256;

    // Clave maestra de la app
    private static final String MASTER_KEY = "";

    public GestorArchivo() {
        new File(CARPETA).mkdirs();
    }

    // ── API pública ──────────────────────────────────────────────────────────

    public void guardar(String etiqueta, String contrasenia) throws Exception {
        byte[]    salt = generarBytes(SALT_BYTES);
        byte[]    iv   = generarBytes(IV_BYTES);
        SecretKey key  = derivarClave(MASTER_KEY, salt);

        String linea = toB64(salt) + ":" + toB64(iv) + ":"
                     + cifrar(etiqueta, key, iv) + ":"
                     + cifrar(contrasenia, key, iv);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO, true))) {
            bw.write(linea);
            bw.newLine();
        }
    }

    public List<String[]> cargarTodas() throws Exception {
        List<String[]> lista = new ArrayList<>();
        File f = new File(ARCHIVO);
        if (!f.exists()) return lista;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] p = linea.split(":", 4);
                if (p.length != 4) continue;

                byte[]    salt = fromB64(p[0]);
                byte[]    iv   = fromB64(p[1]);
                SecretKey key  = derivarClave(MASTER_KEY, salt);

                lista.add(new String[]{
                    descifrar(p[2], key, iv),
                    descifrar(p[3], key, iv)
                });
            }
        }
        return lista;
    }

    public void eliminar(int indice) throws Exception {
        List<String[]> todas = cargarTodas();
        if (indice < 0 || indice >= todas.size()) return;
        todas.remove(indice);
        reescribir(todas);
    }

    public String getRutaArchivo() { return ARCHIVO; }

    // ── Cifrado AES-256-GCM ──────────────────────────────────────────────────

    private String cifrar(String texto, SecretKey key, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance(ALGORITMO);
        c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        return toB64(c.doFinal(texto.getBytes(StandardCharsets.UTF_8)));
    }

    private String descifrar(String b64, SecretKey key, byte[] iv) throws Exception {
        Cipher c = Cipher.getInstance(ALGORITMO);
        c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        return new String(c.doFinal(fromB64(b64)), StandardCharsets.UTF_8);
    }

    // ── Derivación de clave PBKDF2 ───────────────────────────────────────────

    private SecretKey derivarClave(String password, byte[] salt) throws Exception {
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] kb = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, ITER, KEY_BITS)).getEncoded();
        return new SecretKeySpec(kb, "AES");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void reescribir(List<String[]> lista) throws Exception {
        new PrintWriter(new FileWriter(ARCHIVO, false)).close(); // vaciar
        for (String[] e : lista) guardar(e[0], e[1]);
    }

    private byte[] generarBytes(int n) {
        byte[] b = new byte[n]; new SecureRandom().nextBytes(b); return b;
    }

    private String toB64(byte[] d)  { return Base64.getEncoder().encodeToString(d); }
    private byte[] fromB64(String s){ return Base64.getDecoder().decode(s); }
}
