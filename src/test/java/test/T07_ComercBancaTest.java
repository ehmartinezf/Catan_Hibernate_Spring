package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import enums.TipusRecurs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * PRIORITAT 7: comerç amb la banca (4 recursos iguals per 1 d'un altre tipus).
 */
class T07_ComercBancaTest extends BaseDbTest {

    private int partida;
    private int a;
    private int b;

    private void escenari() {
        partida = Joc.enCursAmbDausTirats(partidaDao, 10);
        a = Joc.actiu(partida);
        b = Joc.altre(partida, a);
    }

    @Test
    @DisplayName("Canvia 4 recursos d'un tipus per 1 d'un altre")
    void canvia4Per1() {
        escenari();
        Joc.setRecursos(a, 4, 0, 0, 0, 0);

        String r = jugadorDao.comerciarBanca(a, TipusRecurs.FUSTA, TipusRecurs.MINERAL);

        assertTrue(r.startsWith("Comerç fet"), r);
        assertEquals(0, Joc.recurs(a, TipusRecurs.FUSTA));
        assertEquals(1, Joc.recurs(a, TipusRecurs.MINERAL));
        assertEquals(1, Joc.total(a));
    }

    @Test
    @DisplayName("Amb més de 4 recursos només se'n gasten 4")
    void nomesGastaQuatre() {
        escenari();
        Joc.setRecursos(a, 0, 9, 0, 2, 0);

        Joc.ok(jugadorDao.comerciarBanca(a, TipusRecurs.MAO, TipusRecurs.BLAT));

        assertEquals(5, Joc.recurs(a, TipusRecurs.MAO));
        assertEquals(3, Joc.recurs(a, TipusRecurs.BLAT));
    }

    @Test
    @DisplayName("Amb menys de 4 recursos del tipus no es pot comerciar")
    void recursosInsuficients() {
        escenari();
        Joc.setRecursos(a, 3, 10, 10, 10, 10);

        Joc.koInici(
                jugadorDao.comerciarBanca(a, TipusRecurs.FUSTA, TipusRecurs.MINERAL),
                Missatges.SENSE_RECURSOS);

        assertEquals(3, Joc.recurs(a, TipusRecurs.FUSTA));
        assertEquals(10, Joc.recurs(a, TipusRecurs.MINERAL));
    }

    @Test
    @DisplayName("No es pot donar i rebre el mateix tipus")
    void mateixTipus() {
        escenari();
        Joc.donarTot(a, 10);

        Joc.ko(
                jugadorDao.comerciarBanca(a, TipusRecurs.LLANA, TipusRecurs.LLANA),
                Missatges.COMERC_IGUALS);
        assertEquals(50, Joc.total(a));
    }

    @Test
    @DisplayName("Cal indicar els dos tipus de recurs")
    void tipusNuls() {
        escenari();
        Joc.donarTot(a, 10);

        Joc.ko(jugadorDao.comerciarBanca(a, null, TipusRecurs.MAO), Missatges.CAL_DOS_TIPUS_RECURS);
        Joc.ko(
                jugadorDao.comerciarBanca(a, TipusRecurs.FUSTA, null),
                Missatges.CAL_DOS_TIPUS_RECURS);
        Joc.ko(jugadorDao.comerciarBanca(a, null, null), Missatges.CAL_DOS_TIPUS_RECURS);
        assertEquals(50, Joc.total(a));
    }

    @Test
    @DisplayName("El comerç no afecta els recursos de l'altre jugador")
    void noTocaAltresJugadors() {
        escenari();
        Joc.donarTot(a, 4);
        Joc.donarTot(b, 4);

        Joc.ok(jugadorDao.comerciarBanca(a, TipusRecurs.FUSTA, TipusRecurs.MAO));

        assertEquals(20, Joc.total(b));
    }
}
