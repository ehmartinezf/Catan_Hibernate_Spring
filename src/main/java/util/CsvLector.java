package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvLector {

    private static final String CARPETA = "dades/";

    private CsvLector() {}

    public static List<String[]> llegir(String nomFitxer) {
        InputStream is = CsvLector.class.getClassLoader().getResourceAsStream(CARPETA + nomFitxer);
        if (is == null) {
            throw new IllegalStateException("No s'ha trobat el fitxer " + CARPETA + nomFitxer);
        }
        List<String[]> files = new ArrayList<>();
        try (BufferedReader br =
                new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            br.readLine();
            String linia;
            while ((linia = br.readLine()) != null) {
                if (!linia.isBlank()) {
                    String[] camps = linia.split(",", -1);
                    for (int i = 0; i < camps.length; i++) {
                        camps[i] = camps[i].trim();
                    }
                    files.add(camps);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error llegint el fitxer " + nomFitxer, e);
        }
        return files;
    }
}
