package Generador;

import java.util.Random;

public class Cadena {

    private static final String caracteresMini  = "abcdefghijklmnopqrstuvwxyz";
    private static final String caracteresMayus = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String simbols         = "@#&$?()[]*+-/<>_";
    private static final String numbers         = "0123456789";

    public String generarContrasenia(int longitud,
                                     boolean usarMini,
                                     boolean usarMayus,
                                     boolean usarSimbolos,
                                     boolean usarNumeros) {

        StringBuilder pool = new StringBuilder();
        if (usarMini)     pool.append(caracteresMini);
        if (usarMayus)    pool.append(caracteresMayus);
        if (usarSimbolos) pool.append(simbols);
        if (usarNumeros)  pool.append(numbers);

        // Si no marcó nada, usar todo
        if (pool.length() == 0) {
            pool.append(caracteresMini).append(caracteresMayus)
                .append(simbols).append(numbers);
        }

        String fuente = pool.toString();
        Random random = new Random();
        char[] contrasenia = new char[longitud];
        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(fuente.length());
            contrasenia[i] = fuente.charAt(index);
        }
        return new String(contrasenia);
    }
}
