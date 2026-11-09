package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import enums.TipusRecurs;
import enums.TipusTerreny;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/*
 * PRIORITAT 5: tirar els daus i la producció de recursos.
 * Els daus són aleatoris dins del DAO, així que es tiren fins que surt el número que ens interessa
 * (i abans de cada tirada es posen els recursos a 0 perquè només comptin els de l'última).
 */
class T05_ProduccioTest extends BaseDbTest {

    private static final int MAX_INTENTS = 1000;

    private int partida;
    private int a;
    private int b;

    private void escenari() {
        partida = Joc.enCursAmbDausTirats(partidaDao, 10);
        a = Joc.actiu(partida);
        b = Joc.altre(partida, a);
        Joc.setDaus(partida, false);
    }

    /**
     * Tira fins que la suma és "objectiu" (o 7 si es demana). Retorna el text de la tirada.
     * Recursos a 0 abans de cada intent.
     */
    private String tirarFinsA(int objectiu) {
        for (int i = 0; i < MAX_INTENTS; i++) {
            Joc.donarTot(a, 0);
            Joc.donarTot(b, 0);
            Joc.setDaus(partida, false);
            Joc.setLladrePendent(partida, false);
            String r = partidaDao.tirarDaus(partida);
            if (Joc.ultimaTirada(partida) == objectiu) {
                return r;
            }
        }
        throw new AssertionError(
                "No ha sortit mai un " + objectiu + " en " + MAX_INTENTS + " tirades");
    }

    /**
     * Recursos que ha de rebre un assentament al vèrtex donat per una tirada, sense comptar el
     * lladre a "idLladre".
     */
    private Map<TipusRecurs, Integer> esperat(
            int vertex, int tirada, int idLladre, int multiplicador) {
        Map<TipusRecurs, Integer> m = new EnumMap<>(TipusRecurs.class);
        for (int h : Joc.hexagonsDeVertex(vertex)) {
            Integer numero =
                    Joc.un(Integer.class, "select h.numero from Hexagon h where h.id = :h", "h", h);
            if (numero != null && numero == tirada && h != idLladre) {
                TipusTerreny t =
                        Joc.un(
                                TipusTerreny.class,
                                "select h.terreny from Hexagon h where h.id = :h",
                                "h",
                                h);
                m.merge(t.getRecurs(), multiplicador, Integer::sum);
            }
        }
        return m;
    }

    private void comprovarRecursos(int jugador, Map<TipusRecurs, Integer> esperat) {
        for (TipusRecurs r : TipusRecurs.values()) {
            assertEquals(esperat.getOrDefault(r, 0), Joc.recurs(jugador, r), "Recurs " + r);
        }
    }

    // --------------------------------- producció ---------------------------------
    @Test
    @DisplayName("Un poblat rep 1 recurs per cada hexàgon veí amb el número tirat")
    void poblatRepUnRecurs() {
        escenari();
        int hexagon = Joc.hexagonAmbNumero(partida, 6);
        int vertex = Joc.vertexsDeHexagon(hexagon).get(0);
        Joc.poblat(a, vertex);

        String r = tirarFinsA(6);

        Map<TipusRecurs, Integer> esperat = esperat(vertex, 6, Joc.idLladre(partida), 1);
        assertFalse(esperat.isEmpty());
        comprovarRecursos(a, esperat);
        assertTrue(r.contains(Joc.nom(a) + " rep 1 "), r);
    }

    @Test
    @DisplayName("Una ciutat rep 2 recursos per hexàgon")
    void ciutatRepDos() {
        escenari();
        int hexagon = Joc.hexagonAmbNumero(partida, 8);
        int vertex = Joc.vertexsDeHexagon(hexagon).get(0);
        Joc.ciutat(a, vertex);

        String r = tirarFinsA(8);

        Map<TipusRecurs, Integer> esperat = esperat(vertex, 8, Joc.idLladre(partida), 2);
        assertFalse(esperat.isEmpty());
        comprovarRecursos(a, esperat);
        assertTrue(r.contains(Joc.nom(a) + " rep 2 "), r);
    }

    @Test
    @DisplayName("Tots els jugadors amb un assentament a l'hexàgon reben recursos")
    void repTothom() {
        escenari();
        int hexagon = Joc.hexagonAmbNumero(partida, 6);
        List<Integer> vertexs = Joc.vertexsDeHexagon(hexagon);
        int va = vertexs.get(0);
        int vb = vertexs.get(2); // posicions 0 i 2: no són veïns
        Joc.poblat(a, va);
        Joc.poblat(b, vb);

        tirarFinsA(6);

        comprovarRecursos(a, esperat(va, 6, Joc.idLladre(partida), 1));
        comprovarRecursos(b, esperat(vb, 6, Joc.idLladre(partida), 1));
        assertTrue(Joc.total(a) >= 1 && Joc.total(b) >= 1);
    }

    @Test
    @DisplayName("L'hexàgon amb el lladre no produeix")
    void lladreBloquejaLaProduccio() {
        escenari();
        int hexagon = Joc.hexagonAmbNumero(partida, 6);
        int vertex = Joc.vertexsDeHexagon(hexagon).get(0);
        Joc.poblat(a, vertex);
        Joc.setLladre(partida, hexagon);

        tirarFinsA(6);

        Map<TipusRecurs, Integer> esperat = esperat(vertex, 6, hexagon, 1);
        comprovarRecursos(a, esperat);
        int rebuts = esperat.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(rebuts, Joc.total(a), "L'hexàgon del lladre no ha de produir");
    }

    @Test
    @DisplayName("Un jugador sense assentaments als hexàgons del número tirat no rep res")
    void senseAssentamentsNoRepRes() {
        escenari();
        int hexagon = Joc.hexagonAmbNumero(partida, 6);
        int vertex = Joc.vertexsDeHexagon(hexagon).get(0);
        Joc.poblat(a, vertex);

        tirarFinsA(6);

        assertEquals(0, Joc.total(b));
    }

    @Test
    @DisplayName("Per a qualsevol tirada es rep exactament el que toca (30 tirades)")
    void produccioGeneral() {
        escenari();
        int hexagon = Joc.hexagonAmbNumero(partida, 8);
        int vertex = Joc.vertexsDeHexagon(hexagon).get(1);
        Joc.poblat(a, vertex);

        for (int i = 0; i < 30; i++) {
            Joc.donarTot(a, 0);
            Joc.setDaus(partida, false);
            Joc.setLladrePendent(partida, false);
            partidaDao.tirarDaus(partida);
            int tirada = Joc.ultimaTirada(partida);

            if (tirada == 7) {
                assertEquals(0, Joc.total(a));
            } else {
                comprovarRecursos(a, esperat(vertex, tirada, Joc.idLladre(partida), 1));
            }
        }
    }

    // --------------------------------- el 7 ---------------------------------
    @Test
    @DisplayName("Amb un 7 no hi ha producció")
    void suma7NoProdueix() {
        escenari();
        int vertex = Joc.vertexsDeHexagon(Joc.hexagonAmbNumero(partida, 6)).get(0);
        Joc.poblat(a, vertex);

        String r = tirarFinsA(7);

        assertTrue(r.contains("No hi ha producció."), r);
        assertEquals(0, Joc.total(a));
    }

    @Test
    @DisplayName(
            "[POSSIBLE BUG] Amb un 7 cal moure el lladre: la partida hauria de quedar amb"
                    + " lladrePendent = true")
    void suma7DeixaLladrePendent() {
        escenari();

        tirarFinsA(7);

        assertNotNull(Joc.idLladre(partida));
        assertTrue(
                Joc.lladrePendent(partida),
                "tirarDaus no marca lladrePendent amb un 7, així que moureLladre() mai es pot"
                        + " cridar després d'un 7");
    }
}
