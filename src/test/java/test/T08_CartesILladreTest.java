package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import enums.TipusCarta;
import enums.TipusRecurs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * PRIORITAT 8: part avançada. Cartes de desenvolupament (comprar/jugar) i lladre (moure i robar).
 * Escenari: partida EN_CURS a la ronda 2, daus tirats; "a" és l'actiu i "b" el rival.
 * Les cartes de la mà tenen rondaCompra = 1, així que ja es poden jugar.
 */
class T08_CartesILladreTest extends BaseDbTest {

    private int partida;
    private int a;
    private int b;

    private void escenari() {
        partida = Joc.nova2(partidaDao);
        a = Joc.jugadors(partida).get(0);
        b = Joc.jugadors(partida).get(1);
        Joc.enCurs(partida, a, 2, true);
    }

    // =============================== comprar carta ===============================
    @Test
    @DisplayName("Comprar una carta costa 1 llana + 1 blat + 1 mineral i la carta passa al jugador")
    void comprarCarta() {
        escenari();
        Joc.setRecursos(a, 2, 2, 1, 1, 1);

        String r = jugadorDao.comprarCarta(a);

        assertTrue(r.startsWith("Has comprat una carta: "), r);
        assertEquals(1, Joc.cartesDelJugador(a));
        assertEquals(20, Joc.cartesALaPila(partida));
        assertEquals(2, Joc.recurs(a, TipusRecurs.FUSTA));
        assertEquals(2, Joc.recurs(a, TipusRecurs.MAO));
        assertEquals(0, Joc.recurs(a, TipusRecurs.LLANA));
        assertEquals(0, Joc.recurs(a, TipusRecurs.BLAT));
        assertEquals(0, Joc.recurs(a, TipusRecurs.MINERAL));
        assertEquals(
                2,
                Joc.un(
                        Integer.class,
                        "select c.rondaCompra from CartaDesenvolupament c where c.jugador.id = :j",
                        "j",
                        a),
                "La carta guarda la ronda en què s'ha comprat");
    }

    @Test
    @DisplayName("Comprar una carta sense recursos suficients")
    void comprarCartaSenseRecursos() {
        escenari();
        Joc.setRecursos(a, 5, 5, 1, 1, 0);

        Joc.koInici(jugadorDao.comprarCarta(a), Missatges.SENSE_RECURSOS);

        assertEquals(0, Joc.cartesDelJugador(a));
        assertEquals(21, Joc.cartesALaPila(partida));
        assertEquals(1, Joc.recurs(a, TipusRecurs.LLANA));
    }

    @Test
    @DisplayName("Pila de cartes buida")
    void pilaBuida() {
        escenari();
        Joc.buidarPila(partida);
        Joc.donarTot(a, 5);

        Joc.ko(jugadorDao.comprarCarta(a), Missatges.PILA_BUIDA);
        assertEquals(25, Joc.total(a));
    }

    // =============================== jugar carta ===============================
    @Test
    @DisplayName("No es pot jugar una carta que no és a la teva mà (d'un altre o inexistent)")
    void cartaNoEsTeva() {
        escenari();
        int deLaltre = Joc.donarCarta(b, TipusCarta.CAVALLER, 1);

        Joc.ko(jugadorDao.jugarCarta(a, deLaltre, null), Missatges.CARTA_NO_ES_TEVA);
        Joc.ko(jugadorDao.jugarCarta(a, 99999, null), Missatges.CARTA_NO_ES_TEVA);
    }

    @Test
    @DisplayName("Una carta que ja s'ha usat no es pot tornar a jugar")
    void cartaJaUsada() {
        escenari();
        int carta = Joc.donarCarta(a, TipusCarta.CAVALLER, 1);
        Joc.setUsada(carta, true);

        Joc.ko(jugadorDao.jugarCarta(a, carta, null), Missatges.CARTA_JA_USADA);
    }

    @Test
    @DisplayName("No es pot jugar una carta a la mateixa ronda en què s'ha comprat")
    void cartaMateixaRonda() {
        escenari();
        int carta = Joc.donarCarta(a, TipusCarta.CAVALLER, 2);

        Joc.ko(jugadorDao.jugarCarta(a, carta, null), Missatges.CARTA_MATEIXA_RONDA);
        assertFalse(Joc.usada(carta));
        assertEquals(0, Joc.cavallers(a));
    }

    @Test
    @DisplayName("Les cartes de PUNT_VICTORIA no es juguen")
    void puntVictoriaNoEsJuga() {
        escenari();
        int carta = Joc.donarCarta(a, TipusCarta.PUNT_VICTORIA, 1);

        Joc.ko(jugadorDao.jugarCarta(a, carta, null), Missatges.CARTA_PUNT_VICTORIA);
        assertFalse(Joc.usada(carta));
    }

    @Test
    @DisplayName("Jugar un cavaller: suma un cavaller, queda usat i s'ha de moure el lladre")
    void jugarCavaller() {
        escenari();
        int carta = Joc.donarCarta(a, TipusCarta.CAVALLER, 1);

        Joc.ok(jugadorDao.jugarCarta(a, carta, null));

        assertEquals(1, Joc.cavallers(a));
        assertTrue(Joc.usada(carta));
        assertTrue(Joc.lladrePendent(partida));
    }

    @Test
    @DisplayName("Jugar el monopoli sense indicar el recurs")
    void monopoliSenseRecurs() {
        escenari();
        int carta = Joc.donarCarta(a, TipusCarta.MONOPOLI, 1);

        Joc.ko(jugadorDao.jugarCarta(a, carta, null), Missatges.CARTA_MONOPOLI_RECURS);
        assertFalse(Joc.usada(carta));
    }

    @Test
    @DisplayName(
            "[POSSIBLE BUG] Monopoli: el jugador rep tots els recursos d'aquell tipus dels altres"
                    + " jugadors")
    void monopoli() {
        escenari();
        int carta = Joc.donarCarta(a, TipusCarta.MONOPOLI, 1);
        Joc.setRecursos(a, 1, 0, 0, 0, 0);
        Joc.setRecursos(b, 3, 2, 0, 0, 0);

        String r = jugadorDao.jugarCarta(a, carta, TipusRecurs.FUSTA);

        // Ara mateix la consulta HQL de JugadorDao fa servir "r.tipus" i el camp es diu
        // "tipusRecurs": torna "ERROR intern: ..."
        assertEquals("Monopoli: has rebut 3 FUSTA", r);
        assertEquals(4, Joc.recurs(a, TipusRecurs.FUSTA));
        assertEquals(0, Joc.recurs(b, TipusRecurs.FUSTA));
        assertEquals(
                2, Joc.recurs(b, TipusRecurs.MAO), "Els altres recursos del rival no es toquen");
        assertTrue(Joc.usada(carta));
    }

    // =============================== moure el lladre ===============================
    private int hexagonDestinacio() {
        Joc.setLladrePendent(partida, true);
        return Joc.hexagonDiferentDelLladre(partida);
    }

    @Test
    @DisplayName("Moure el lladre a un hexàgon on no hi ha ningú (sense víctima)")
    void moureSenseVictima() {
        escenari();
        int destinacio = hexagonDestinacio();

        String r = jugadorDao.moureLladre(a, destinacio, null);

        assertTrue(r.startsWith("Lladre mogut"), r);
        assertEquals(destinacio, Joc.idLladre(partida));
        assertFalse(Joc.lladrePendent(partida));
    }

    @Test
    @DisplayName("Només es pot moure el lladre si hi ha un moviment pendent")
    void lladreNoPendent() {
        escenari();

        Joc.ko(
                jugadorDao.moureLladre(a, Joc.hexagonDiferentDelLladre(partida), null),
                Missatges.LLADRE_NO_PENDENT);
    }

    @Test
    @DisplayName("El lladre s'ha de moure a un hexàgon diferent de l'actual")
    void mateixHexagon() {
        escenari();
        hexagonDestinacio();

        Joc.ko(
                jugadorDao.moureLladre(a, Joc.idLladre(partida), null),
                Missatges.LLADRE_MATEIX_HEXAGON);
        assertTrue(Joc.lladrePendent(partida));
    }

    @Test
    @DisplayName("No es pot moure el lladre a un hexàgon d'una altra partida")
    void hexagonDAltraPartida() {
        escenari();
        hexagonDestinacio();
        int altra = Joc.nova2(partidaDao);

        Joc.ko(jugadorDao.moureLladre(a, Joc.hexagonDesert(altra), null), Missatges.ALTRA_PARTIDA);
    }

    @Test
    @DisplayName("Moure el lladre i robar 1 recurs a un rival amb assentament a l'hexàgon")
    void robarRecurs() {
        escenari();
        int destinacio = hexagonDestinacio();
        Joc.poblat(b, Joc.vertexsDeHexagon(destinacio).get(0));
        Joc.setRecursos(b, 3, 0, 0, 0, 0);
        Joc.setRecursos(a, 0, 0, 0, 0, 0);

        String r = jugadorDao.moureLladre(a, destinacio, b);

        assertEquals("Lladre mogut. Has robat 1 FUSTA a " + Joc.nom(b), r);
        assertEquals(2, Joc.recurs(b, TipusRecurs.FUSTA));
        assertEquals(1, Joc.recurs(a, TipusRecurs.FUSTA));
        assertEquals(destinacio, Joc.idLladre(partida));
        assertFalse(Joc.lladrePendent(partida));
    }

    @Test
    @DisplayName("Si hi ha un rival amb recursos a l'hexàgon cal indicar-lo com a víctima")
    void victimaObligatoria() {
        escenari();
        int destinacio = hexagonDestinacio();
        int lladreAbans = Joc.idLladre(partida);
        Joc.poblat(b, Joc.vertexsDeHexagon(destinacio).get(0));
        Joc.setRecursos(b, 1, 0, 0, 0, 0);

        Joc.ko(jugadorDao.moureLladre(a, destinacio, null), Missatges.VICTIMA_OBLIGATORIA);

        assertEquals(lladreAbans, Joc.idLladre(partida));
        assertTrue(Joc.lladrePendent(partida));
        assertEquals(1, Joc.total(b));
    }

    @Test
    @DisplayName("Un rival sense recursos no es pot robar, però tampoc cal indicar-lo")
    void victimaSenseRecursos() {
        escenari();
        int destinacio = hexagonDestinacio();
        Joc.poblat(b, Joc.vertexsDeHexagon(destinacio).get(0));
        Joc.setRecursos(b, 0, 0, 0, 0, 0);

        Joc.ko(jugadorDao.moureLladre(a, destinacio, b), Missatges.VICTIMA_INVALIDA);
        Joc.ok(jugadorDao.moureLladre(a, destinacio, null));
        assertEquals(destinacio, Joc.idLladre(partida));
    }

    @Test
    @DisplayName("La víctima ha de tenir un assentament a l'hexàgon i no pot ser el mateix jugador")
    void victimaInvalida() {
        escenari();
        int destinacio = hexagonDestinacio();
        Joc.setRecursos(b, 5, 5, 5, 5, 5);

        // b no té cap assentament a l'hexàgon de destinació
        Joc.ko(jugadorDao.moureLladre(a, destinacio, b), Missatges.VICTIMA_INVALIDA);

        // a es posa ell mateix com a víctima
        Joc.poblat(a, Joc.vertexsDeHexagon(destinacio).get(0));
        Joc.setRecursos(a, 5, 5, 5, 5, 5);
        Joc.ko(jugadorDao.moureLladre(a, destinacio, a), Missatges.VICTIMA_INVALIDA);
        assertTrue(Joc.lladrePendent(partida));
    }

    @Test
    @DisplayName("Després de moure el lladre el jugador ja pot continuar el torn")
    void desbloquejaElTorn() {
        escenari();
        int destinacio = hexagonDestinacio();
        Joc.ok(jugadorDao.moureLladre(a, destinacio, null));

        Joc.ok(jugadorDao.passarTorn(a));
        assertEquals(b, Joc.actiu(partida));
    }
}
