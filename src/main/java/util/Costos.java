package util;

import java.util.EnumMap;
import java.util.Map;

import enums.TipusRecurs;

/*
 * Costos de construcció i límits del joc (enunciat: "Costos de construcció" i "Límits per jugador").
 * Així no hi ha "números màgics" escampats pel codi.
 */
public class Costos {

    public static final Map<TipusRecurs, Integer> CARRETERA = cost(TipusRecurs.FUSTA, 1, TipusRecurs.MAO, 1);
    public static final Map<TipusRecurs, Integer> POBLAT = cost(TipusRecurs.FUSTA, 1, TipusRecurs.MAO, 1,
            TipusRecurs.LLANA, 1, TipusRecurs.BLAT, 1);
    public static final Map<TipusRecurs, Integer> CIUTAT = cost(TipusRecurs.BLAT, 2, TipusRecurs.MINERAL, 3);
    public static final Map<TipusRecurs, Integer> CARTA = cost(TipusRecurs.BLAT, 1, TipusRecurs.LLANA, 1,
            TipusRecurs.MINERAL, 1);

    public static final int MAX_CARRETERES = 15;
    public static final int MAX_POBLATS = 5;
    public static final int MAX_CIUTATS = 4;
    public static final int COMERC_BANCA = 4;

    private Costos() {
    }

    private static Map<TipusRecurs, Integer> cost(Object... parelles) {
        Map<TipusRecurs, Integer> m = new EnumMap<>(TipusRecurs.class);
        for (int i = 0; i < parelles.length; i += 2) {
            m.put((TipusRecurs) parelles[i], (Integer) parelles[i + 1]);
        }
        return m;
    }
}
