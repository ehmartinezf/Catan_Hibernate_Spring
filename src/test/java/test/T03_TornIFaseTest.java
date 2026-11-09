package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import enums.EstatPartida;
import enums.TipusRecurs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * PRIORITAT 3: control de torns i fases a EN_CURS.
 * Totes les accions han de comprovar: partida no finalitzada -> és el teu torn -> daus tirats -> lladre no pendent.
 * (Els ids 1 que es passen a les accions són irrellevants: el torn es valida ABANS de mirar l'objecte.)
 */
class T03_TornIFaseTest extends BaseDbTest {

    private int partida;
    private int actiu;
    private int altre;

    /** Partida de 2 jugadors EN_CURS, ronda 1, daus SENSE tirar. */
    private void escenari(boolean dausTirats) {
        partida = Joc.nova2(partidaDao);
        actiu = Joc.jugadors(partida).get(0);
        altre = Joc.jugadors(partida).get(1);
        Joc.enCurs(partida, actiu, 1, dausTirats);
    }

    // --------------------------------- tirar daus ---------------------------------
    @Test
    @DisplayName("Sense tirar els daus no es pot fer cap acció del torn")
    void senseTirarDaus() {
        escenari(false);

        Joc.ko(jugadorDao.construirCarretera(actiu, 1), Missatges.CAL_TIRAR_DAUS);
        Joc.ko(jugadorDao.construirPoblat(actiu, 1), Missatges.CAL_TIRAR_DAUS);
        Joc.ko(jugadorDao.millorarACiutat(actiu, 1), Missatges.CAL_TIRAR_DAUS);
        Joc.ko(
                jugadorDao.comerciarBanca(actiu, TipusRecurs.FUSTA, TipusRecurs.MAO),
                Missatges.CAL_TIRAR_DAUS);
        Joc.ko(jugadorDao.comprarCarta(actiu), Missatges.CAL_TIRAR_DAUS);
        Joc.ko(jugadorDao.jugarCarta(actiu, 1, null), Missatges.CAL_TIRAR_DAUS);
        Joc.ko(jugadorDao.passarTorn(actiu), Missatges.CAL_TIRAR_DAUS);
        assertEquals(actiu, Joc.actiu(partida));
    }

    @Test
    @DisplayName("Els daus es tiren una sola vegada per torn i la tirada queda desada (2..12)")
    void tirarDausUnaVegada() {
        escenari(false);

        String r = partidaDao.tirarDaus(partida);

        Matcher m = Pattern.compile("Daus: (\\d) \\+ (\\d) = (\\d+)\\n").matcher(r);
        assertTrue(m.find(), "Format inesperat: " + r);
        int d1 = Integer.parseInt(m.group(1));
        int d2 = Integer.parseInt(m.group(2));
        int suma = Integer.parseInt(m.group(3));
        assertTrue(d1 >= 1 && d1 <= 6 && d2 >= 1 && d2 <= 6, r);
        assertEquals(d1 + d2, suma);
        assertEquals(suma, Joc.ultimaTirada(partida));
        assertTrue(Joc.dausTirats(partida));

        assertEquals(Missatges.DAUS_JA_TIRATS, partidaDao.tirarDaus(partida));
    }

    @Test
    @DisplayName("Tirar els daus d'una partida que no existeix")
    void tirarDausPartidaInexistent() {
        assertEquals(Missatges.PARTIDA_NO_EXISTEIX, partidaDao.tirarDaus(99999));
    }

    // --------------------------------- és el teu torn ---------------------------------
    @Test
    @DisplayName("Un jugador que no és l'actiu no pot fer cap acció")
    void noEsElTeuTorn() {
        escenari(true);

        Joc.ko(jugadorDao.construirCarretera(altre, 1), Missatges.NO_ES_EL_TEU_TORN);
        Joc.ko(jugadorDao.construirPoblat(altre, 1), Missatges.NO_ES_EL_TEU_TORN);
        Joc.ko(jugadorDao.millorarACiutat(altre, 1), Missatges.NO_ES_EL_TEU_TORN);
        Joc.ko(
                jugadorDao.comerciarBanca(altre, TipusRecurs.FUSTA, TipusRecurs.MAO),
                Missatges.NO_ES_EL_TEU_TORN);
        Joc.ko(jugadorDao.comprarCarta(altre), Missatges.NO_ES_EL_TEU_TORN);
        Joc.ko(jugadorDao.jugarCarta(altre, 1, null), Missatges.NO_ES_EL_TEU_TORN);
        Joc.ko(jugadorDao.moureLladre(altre, 1, null), Missatges.NO_ES_EL_TEU_TORN);
        Joc.ko(jugadorDao.passarTorn(altre), Missatges.NO_ES_EL_TEU_TORN);
        assertEquals(actiu, Joc.actiu(partida));
    }

    @Test
    @DisplayName("Un jugador que no existeix rep l'error corresponent")
    void jugadorInexistent() {
        escenari(true);

        Joc.ko(jugadorDao.construirCarretera(99999, 1), Missatges.JUGADOR_NO_EXISTEIX);
        Joc.ko(jugadorDao.construirPoblat(99999, 1), Missatges.JUGADOR_NO_EXISTEIX);
        Joc.ko(jugadorDao.millorarACiutat(99999, 1), Missatges.JUGADOR_NO_EXISTEIX);
        Joc.ko(
                jugadorDao.comerciarBanca(99999, TipusRecurs.FUSTA, TipusRecurs.MAO),
                Missatges.JUGADOR_NO_EXISTEIX);
        Joc.ko(jugadorDao.comprarCarta(99999), Missatges.JUGADOR_NO_EXISTEIX);
        Joc.ko(jugadorDao.jugarCarta(99999, 1, null), Missatges.JUGADOR_NO_EXISTEIX);
        Joc.ko(jugadorDao.moureLladre(99999, 1, null), Missatges.JUGADOR_NO_EXISTEIX);
        Joc.ko(jugadorDao.passarTorn(99999), Missatges.JUGADOR_NO_EXISTEIX);
        assertEquals(0, jugadorDao.calcularPunts(99999));
        assertTrue(jugadorDao.vertexsDisponibles(99999).isEmpty());
    }

    // --------------------------------- passar torn ---------------------------------
    @Test
    @DisplayName(
            "Passar torn: canvia el jugador actiu, es poden tornar a tirar els daus i la ronda no"
                    + " canvia")
    void passarTorn() {
        escenari(true);

        String r = jugadorDao.passarTorn(actiu);

        assertEquals("Torn passat. Ara juga " + Joc.nom(altre), r);
        assertEquals(altre, Joc.actiu(partida));
        assertFalse(Joc.dausTirats(partida));
        assertEquals(1, Joc.ronda(partida));
        assertEquals(EstatPartida.EN_CURS, Joc.estat(partida));
    }

    @Test
    @DisplayName("Quan l'últim jugador passa torn, torna al primer i la ronda augmenta")
    void novaRonda() {
        escenari(true);
        Joc.ok(jugadorDao.passarTorn(actiu));
        Joc.setDaus(partida, true);

        Joc.ok(jugadorDao.passarTorn(altre));

        assertEquals(actiu, Joc.actiu(partida));
        assertEquals(2, Joc.ronda(partida));
        assertFalse(Joc.dausTirats(partida));
    }

    @Test
    @DisplayName("Amb 3 jugadors el torn gira 1 -> 2 -> 3 -> 1 i la ronda puja en tornar al 1")
    void girTresJugadors() {
        partida = Joc.nova(partidaDao, 10, "Anna", "Bernat", "Carla");
        List<Integer> ordre = Joc.jugadors(partida);
        Joc.enCurs(partida, ordre.get(0), 1, true);

        for (int i = 0; i < 3; i++) {
            int actual = ordre.get(i);
            Joc.setDaus(partida, true);
            Joc.ok(jugadorDao.passarTorn(actual));
            assertEquals(ordre.get((i + 1) % 3), Joc.actiu(partida));
            assertEquals(i == 2 ? 2 : 1, Joc.ronda(partida), "Ronda després del torn " + (i + 1));
        }
    }

    // --------------------------------- lladre pendent ---------------------------------
    @Test
    @DisplayName(
            "Amb el lladre pendent cal moure'l abans de fer qualsevol altra cosa (ni passar torn)")
    void lladrePendentBloquejaLesAccions() {
        escenari(true);
        Joc.setLladrePendent(partida, true);

        Joc.ko(jugadorDao.construirCarretera(actiu, 1), Missatges.LLADRE_PENDENT);
        Joc.ko(jugadorDao.construirPoblat(actiu, 1), Missatges.LLADRE_PENDENT);
        Joc.ko(jugadorDao.millorarACiutat(actiu, 1), Missatges.LLADRE_PENDENT);
        Joc.ko(
                jugadorDao.comerciarBanca(actiu, TipusRecurs.FUSTA, TipusRecurs.MAO),
                Missatges.LLADRE_PENDENT);
        Joc.ko(jugadorDao.comprarCarta(actiu), Missatges.LLADRE_PENDENT);
        Joc.ko(jugadorDao.jugarCarta(actiu, 1, null), Missatges.LLADRE_PENDENT);
        Joc.ko(jugadorDao.passarTorn(actiu), Missatges.LLADRE_PENDENT);
        assertEquals(actiu, Joc.actiu(partida));
    }

    // --------------------------------- partida finalitzada ---------------------------------
    @Test
    @DisplayName("Una partida FINALITZADA no admet cap acció")
    void partidaFinalitzada() {
        escenari(true);
        Joc.setEstat(partida, EstatPartida.FINALITZADA);

        Joc.ko(jugadorDao.construirCarretera(actiu, 1), Missatges.PARTIDA_FINALITZADA);
        Joc.ko(jugadorDao.construirPoblat(actiu, 1), Missatges.PARTIDA_FINALITZADA);
        Joc.ko(jugadorDao.millorarACiutat(actiu, 1), Missatges.PARTIDA_FINALITZADA);
        Joc.ko(
                jugadorDao.comerciarBanca(actiu, TipusRecurs.FUSTA, TipusRecurs.MAO),
                Missatges.PARTIDA_FINALITZADA);
        Joc.ko(jugadorDao.comprarCarta(actiu), Missatges.PARTIDA_FINALITZADA);
        Joc.ko(jugadorDao.jugarCarta(actiu, 1, null), Missatges.PARTIDA_FINALITZADA);
        Joc.ko(jugadorDao.moureLladre(actiu, 1, null), Missatges.PARTIDA_FINALITZADA);
        Joc.ko(jugadorDao.passarTorn(actiu), Missatges.PARTIDA_FINALITZADA);
        assertEquals(Missatges.PARTIDA_FINALITZADA, partidaDao.tirarDaus(partida));
    }
}
