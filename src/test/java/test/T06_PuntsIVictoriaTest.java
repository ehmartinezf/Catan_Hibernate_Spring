package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import enums.EstatPartida;
import enums.TipusCarta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/*
 * PRIORITAT 6: punts de victòria (poblat 1, ciutat 2, carta PUNT_VICTORIA 1, exèrcit més gran 2)
 * i final de la partida quan algú arriba als punts necessaris.
 */
class T06_PuntsIVictoriaTest extends BaseDbTest {

    private int partida;
    private int a;
    private int b;

    private void escenari(int punts) {
        partida = Joc.enCursAmbDausTirats(partidaDao, punts);
        a = Joc.actiu(partida);
        b = Joc.altre(partida, a);
    }

    // =============================== càlcul de punts ===============================
    @Test
    @DisplayName("Punts = poblats + 2 x ciutats + cartes de punt de victòria")
    void calculDePunts() {
        escenari(20);
        List<Integer> v = Joc.independents(partida, 4);
        Joc.poblat(a, v.get(0));
        Joc.poblat(a, v.get(1));
        Joc.ciutat(a, v.get(2));
        Joc.donarCarta(a, TipusCarta.PUNT_VICTORIA, 0);

        assertEquals(2 + 2 + 1, jugadorDao.calcularPunts(a));
        assertEquals(0, jugadorDao.calcularPunts(b));
    }

    @Test
    @DisplayName("Les carreteres i les altres cartes no donen punts")
    void carreteresICartesNoPuntuen() {
        escenari(20);
        Joc.carretera(a, Joc.arestaLliure(Joc.vertexs(partida).get(0)));
        Joc.donarCarta(a, TipusCarta.CAVALLER, 0);
        Joc.donarCarta(a, TipusCarta.MONOPOLI, 0);

        assertEquals(0, jugadorDao.calcularPunts(a));
    }

    @Test
    @DisplayName("Exèrcit més gran: +2 punts amb 3 o més cavallers jugats i més que cap altre")
    void exercitMesGran() {
        escenari(20);

        Joc.setCavallers(a, 2);
        assertEquals(0, jugadorDao.calcularPunts(a), "Amb 2 cavallers no n'hi ha prou");

        Joc.setCavallers(a, 3);
        assertEquals(2, jugadorDao.calcularPunts(a));
        assertEquals(0, jugadorDao.calcularPunts(b));
    }

    @Test
    @DisplayName("Exèrcit més gran: amb empat ningú no el té; es perd si l'altre el supera")
    void exercitMesGranEmpatIRellevo() {
        escenari(20);
        Joc.setCavallers(a, 3);
        Joc.setCavallers(b, 3);
        assertEquals(0, jugadorDao.calcularPunts(a));
        assertEquals(0, jugadorDao.calcularPunts(b));

        Joc.setCavallers(b, 4);
        assertEquals(0, jugadorDao.calcularPunts(a));
        assertEquals(2, jugadorDao.calcularPunts(b));
    }

    // =============================== victòria ===============================
    @Test
    @DisplayName(
            "En construir un poblat que fa arribar als punts necessaris, la partida acaba i guanya"
                    + " aquest jugador")
    void victoriaPerPoblat() {
        escenari(3);
        List<Integer> cami = Joc.cami(partida, 3);
        Joc.poblat(a, cami.get(0));
        Joc.donarCarta(a, TipusCarta.PUNT_VICTORIA, 0); // 2 punts
        Joc.carretera(a, Joc.arestaEntre(cami.get(0), cami.get(1)));
        Joc.carretera(a, Joc.arestaEntre(cami.get(1), cami.get(2)));
        Joc.donarTot(a, 5);

        Joc.ok(jugadorDao.construirPoblat(a, cami.get(2)));

        assertEquals(EstatPartida.FINALITZADA, Joc.estat(partida));
        assertEquals(a, Joc.idGuanyador(partida));
    }

    @Test
    @DisplayName("Victòria en millorar un poblat a ciutat")
    void victoriaPerCiutat() {
        escenari(3);
        Joc.poblat(a, Joc.vertexs(partida).get(0));
        Joc.donarCarta(a, TipusCarta.PUNT_VICTORIA, 0); // 1 + 1 = 2 punts
        Joc.setRecursos(a, 0, 0, 0, 2, 3);

        Joc.ok(jugadorDao.millorarACiutat(a, Joc.vertexs(partida).get(0))); // 2 + 1 = 3 punts

        assertEquals(EstatPartida.FINALITZADA, Joc.estat(partida));
        assertEquals(a, Joc.idGuanyador(partida));
    }

    @Test
    @DisplayName("Victòria en comprar una carta de punt de victòria")
    void victoriaPerCarta() {
        escenari(3);
        List<Integer> v = Joc.independents(partida, 2);
        Joc.poblat(a, v.get(0));
        Joc.poblat(a, v.get(1));
        Joc.pilaNomesDe(partida, TipusCarta.PUNT_VICTORIA);
        Joc.setRecursos(a, 0, 0, 1, 1, 1);

        String r = jugadorDao.comprarCarta(a);

        assertEquals("Has comprat una carta: PUNT_VICTORIA", r);
        assertEquals(EstatPartida.FINALITZADA, Joc.estat(partida));
        assertEquals(a, Joc.idGuanyador(partida));
    }

    @Test
    @DisplayName("Si no s'arriba als punts necessaris la partida continua EN_CURS sense guanyador")
    void senseVictoria() {
        escenari(10);
        List<Integer> v = Joc.independents(partida, 2);
        Joc.poblat(a, v.get(0));
        Joc.poblat(a, v.get(1));

        partidaDao.comprovarVictoria(partida);

        assertEquals(EstatPartida.EN_CURS, Joc.estat(partida));
        assertNull(Joc.idGuanyador(partida));
    }

    @Test
    @DisplayName("PartidaDao.comprovarVictoria finalitza la partida si algú ja té els punts")
    void comprovarVictoriaDirecte() {
        escenari(3);
        for (int v : Joc.independents(partida, 3)) {
            Joc.poblat(b, v);
        }

        partidaDao.comprovarVictoria(partida);

        assertEquals(EstatPartida.FINALITZADA, Joc.estat(partida));
        assertEquals(b, Joc.idGuanyador(partida));
    }

    @Test
    @DisplayName("Un cop finalitzada, el guanyador no canvia")
    void guanyadorEsMantenint() {
        escenari(3);
        for (int v : Joc.independents(partida, 3)) {
            Joc.poblat(a, v);
        }
        partidaDao.comprovarVictoria(partida);
        for (int v : Joc.independents(partida, 6).subList(3, 6)) {
            Joc.poblat(b, v);
        }

        partidaDao.comprovarVictoria(partida);

        assertEquals(a, Joc.idGuanyador(partida));
    }

    @Test
    @DisplayName("A PREPARACIO no es pot guanyar encara que es tinguin punts")
    void noEsGuanyaEnPreparacio() {
        escenari(3);
        Joc.setEstat(partida, EstatPartida.PREPARACIO);
        for (int v : Joc.independents(partida, 3)) {
            Joc.poblat(a, v);
        }

        partidaDao.comprovarVictoria(partida);

        assertEquals(EstatPartida.PREPARACIO, Joc.estat(partida));
        assertNull(Joc.idGuanyador(partida));
    }

    @Test
    @DisplayName("Comprovar la victòria d'una partida inexistent llança excepció")
    void comprovarVictoriaInexistent() {
        assertThrows(RuntimeException.class, () -> partidaDao.comprovarVictoria(99999));
    }
}
