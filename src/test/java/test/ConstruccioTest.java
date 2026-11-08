package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import enums.TipusRecurs;


class ConstruccioTest extends BaseDbTest {

    private int partida;
    private int a;
    private int b;
    private List<Integer> p;

    private void escenari() {
        partida = Joc.enCursAmbDausTirats(partidaDao, 10);
        a = Joc.actiu(partida);
        b = Joc.altre(partida, a);
        p = Joc.cami(partida, 5);
        Joc.poblat(a, p.get(0));
        Joc.carretera(a, Joc.arestaEntre(p.get(0), p.get(1)));
    }

    @Test
    @DisplayName("Carretera connectada: es construeix i costa 1 fusta + 1 maó")
    void carreteraConnectada() {
        escenari();
        Joc.setRecursos(a, 2, 2, 2, 2, 2);

        Joc.ok(jugadorDao.construirCarretera(a, Joc.arestaEntre(p.get(1), p.get(2))));

        assertEquals(2, Joc.carreteres(a));
        assertEquals(1, Joc.recurs(a, TipusRecurs.FUSTA));
        assertEquals(1, Joc.recurs(a, TipusRecurs.MAO));
        assertEquals(2, Joc.recurs(a, TipusRecurs.LLANA));
        assertEquals(2, Joc.recurs(a, TipusRecurs.BLAT));
        assertEquals(2, Joc.recurs(a, TipusRecurs.MINERAL));
    }

    @Test
    @DisplayName("Carretera sense recursos suficients: error i res no canvia")
    void carreteraSenseRecursos() {
        escenari();
        Joc.setRecursos(a, 1, 0, 5, 5, 5);

        Joc.koInici(jugadorDao.construirCarretera(a, Joc.arestaEntre(p.get(1), p.get(2))), Missatges.SENSE_RECURSOS);

        assertEquals(1, Joc.carreteres(a));
        assertEquals(1, Joc.recurs(a, TipusRecurs.FUSTA), "El rollback ha de deixar els recursos igual");
    }

    @Test
    @DisplayName("Carretera no connectada a cap construcció pròpia")
    void carreteraDesconnectada() {
        escenari();
        Joc.donarTot(a, 5);

        Joc.ko(jugadorDao.construirCarretera(a, Joc.arestaEntre(p.get(3), p.get(4))), Missatges.NO_CONNECTAT);
        assertEquals(25, Joc.total(a));
    }

    @Test
    @DisplayName("Carretera en una aresta ja ocupada")
    void carreteraOcupada() {
        escenari();
        Joc.donarTot(a, 5);

        Joc.ko(jugadorDao.construirCarretera(a, Joc.arestaEntre(p.get(0), p.get(1))), Missatges.ARESTA_OCUPADA);
    }

    @Test
    @DisplayName("Una aresta d'una altra partida es rebutja")
    void carreteraAltraPartida() {
        escenari();
        int altra = Joc.nova2(partidaDao);
        Joc.donarTot(a, 5);

        Joc.ko(jugadorDao.construirCarretera(a, Joc.arestaLliure(Joc.vertexs(altra).get(0))), Missatges.ALTRA_PARTIDA);
    }

    @Test
    @DisplayName("Límit de 15 carreteres per jugador")
    void limitDeCarreteres() {
        partida = Joc.enCursAmbDausTirats(partidaDao, 10);
        a = Joc.actiu(partida);
        List<Integer> arestes = Joc.arestesConnexes(Joc.vertexs(partida).get(0), 16);
        for (int i = 0; i < 15; i++) {
            Joc.carretera(a, arestes.get(i));
        }
        Joc.donarTot(a, 20);

        Joc.koInici(jugadorDao.construirCarretera(a, arestes.get(15)), Missatges.LIMIT_CARRETERES);

        assertEquals(15, Joc.carreteres(a));
    }

    @Test
    @DisplayName("Poblat connectat per carretera: es construeix i costa fusta, maó, llana i blat")
    void poblatConnectat() {
        escenari();
        Joc.carretera(a, Joc.arestaEntre(p.get(1), p.get(2)));
        Joc.setRecursos(a, 3, 3, 3, 3, 3);

        Joc.ok(jugadorDao.construirPoblat(a, p.get(2)));

        assertEquals(2, Joc.poblats(a));
        assertEquals(2, Joc.recurs(a, TipusRecurs.FUSTA));
        assertEquals(2, Joc.recurs(a, TipusRecurs.MAO));
        assertEquals(2, Joc.recurs(a, TipusRecurs.LLANA));
        assertEquals(2, Joc.recurs(a, TipusRecurs.BLAT));
        assertEquals(3, Joc.recurs(a, TipusRecurs.MINERAL), "El mineral no es gasta");
    }

    @Test
    @DisplayName("Poblat sense recursos suficients")
    void poblatSenseRecursos() {
        escenari();
        Joc.carretera(a, Joc.arestaEntre(p.get(1), p.get(2)));
        Joc.setRecursos(a, 1, 1, 1, 0, 5);

        Joc.koInici(jugadorDao.construirPoblat(a, p.get(2)), Missatges.SENSE_RECURSOS);

        assertEquals(1, Joc.poblats(a));
        assertEquals(1, Joc.recurs(a, TipusRecurs.FUSTA));
    }

    @Test
    @DisplayName("Poblat en un vèrtex on no arriba cap carretera pròpia")
    void poblatSenseCarretera() {
        escenari();
        Joc.donarTot(a, 5);

        Joc.ko(jugadorDao.construirPoblat(a, p.get(3)), Missatges.NO_CONNECTAT);
    }

    @Test
    @DisplayName("Regla de la distància: no es pot fer un poblat al costat d'un altre assentament")
    void poblatReglaDistancia() {
        escenari();
        Joc.carretera(a, Joc.arestaEntre(p.get(1), p.get(2)));
        Joc.poblat(b, p.get(2));
        Joc.donarTot(a, 5);

        Joc.ko(jugadorDao.construirPoblat(a, p.get(1)), Missatges.REGLA_DISTANCIA);
    }

    @Test
    @DisplayName("Poblat en un vèrtex ocupat")
    void poblatVertexOcupat() {
        escenari();
        Joc.donarTot(a, 5);

        Joc.ko(jugadorDao.construirPoblat(a, p.get(0)), Missatges.VERTEX_OCUPAT);
    }

    @Test
    @DisplayName("Poblat en un vèrtex d'una altra partida")
    void poblatAltraPartida() {
        escenari();
        int altra = Joc.nova2(partidaDao);
        Joc.donarTot(a, 5);

        Joc.ko(jugadorDao.construirPoblat(a, Joc.vertexs(altra).get(0)), Missatges.ALTRA_PARTIDA);
    }

    @Test
    @DisplayName("Límit de 5 poblats per jugador")
    void limitDePoblats() {
        partida = Joc.enCursAmbDausTirats(partidaDao, 10);
        a = Joc.actiu(partida);
        List<Integer> v = Joc.independents(partida, 6);
        for (int i = 0; i < 5; i++) {
            Joc.poblat(a, v.get(i));
        }
        Joc.carretera(a, Joc.arestaLliure(v.get(5)));
        Joc.donarTot(a, 5);

        Joc.koInici(jugadorDao.construirPoblat(a, v.get(5)), Missatges.LIMIT_POBLATS);

        assertEquals(5, Joc.poblats(a));
    }

    @Test
    @DisplayName("Millorar poblat a ciutat: costa 2 blat + 3 mineral i el poblat desapareix")
    void millorarACiutat() {
        escenari();
        Joc.setRecursos(a, 1, 1, 1, 2, 3);

        assertEquals("Poblat millorat a ciutat", jugadorDao.millorarACiutat(a, p.get(0)));

        assertEquals(0, Joc.poblats(a));
        assertEquals(1, Joc.ciutats(a));
        assertEquals(0, Joc.recurs(a, TipusRecurs.BLAT));
        assertEquals(0, Joc.recurs(a, TipusRecurs.MINERAL));
        assertEquals(1, Joc.recurs(a, TipusRecurs.FUSTA));
        assertEquals(2, jugadorDao.calcularPunts(a), "Una ciutat val 2 punts");
    }

    @Test
    @DisplayName("Millorar a ciutat sense recursos suficients")
    void millorarSenseRecursos() {
        escenari();
        Joc.setRecursos(a, 0, 0, 0, 1, 3);

        Joc.koInici(jugadorDao.millorarACiutat(a, p.get(0)), Missatges.SENSE_RECURSOS);

        assertEquals(1, Joc.poblats(a));
        assertEquals(0, Joc.ciutats(a));
        assertEquals(3, Joc.recurs(a, TipusRecurs.MINERAL));
    }

    @Test
    @DisplayName("Només es pot millorar un poblat propi (no un vèrtex buit ni un poblat d'un altre)")
    void millorarSensePoblatPropi() {
        escenari();
        Joc.poblat(b, p.get(3));
        Joc.setRecursos(a, 0, 0, 0, 5, 5);

        Joc.ko(jugadorDao.millorarACiutat(a, p.get(2)), Missatges.NO_HI_HA_POBLAT);
        Joc.ko(jugadorDao.millorarACiutat(a, p.get(3)), Missatges.NO_HI_HA_POBLAT);
    }

    @Test
    @DisplayName("Una ciutat no es pot tornar a millorar")
    void ciutatNoEsMillora() {
        escenari();
        Joc.setRecursos(a, 0, 0, 0, 10, 10);
        Joc.ok(jugadorDao.millorarACiutat(a, p.get(0)));

        Joc.ko(jugadorDao.millorarACiutat(a, p.get(0)), Missatges.NO_HI_HA_POBLAT);
    }

    @Test
    @DisplayName("Límit de 4 ciutats per jugador")
    void limitDeCiutats() {
        partida = Joc.enCursAmbDausTirats(partidaDao, 20);
        a = Joc.actiu(partida);
        List<Integer> v = Joc.independents(partida, 5);
        for (int i = 0; i < 4; i++) {
            Joc.ciutat(a, v.get(i));
        }
        Joc.poblat(a, v.get(4));
        Joc.setRecursos(a, 0, 0, 0, 5, 5);

        Joc.koInici(jugadorDao.millorarACiutat(a, v.get(4)), Missatges.LIMIT_CIUTATS);

        assertEquals(4, Joc.ciutats(a));
        assertEquals(1, Joc.poblats(a));
    }

    @Test
    @DisplayName("Els errors de construcció desfan tota la transacció (rollback)")
    void rollbackEnError() {
        escenari();
        Joc.setRecursos(a, 1, 1, 1, 1, 0);
        long abans = Joc.comptar("select count(c) from Construccio c");

        jugadorDao.construirPoblat(a, p.get(3)); // no connectat

        assertEquals(abans, Joc.comptar("select count(c) from Construccio c"));
        assertTrue(Joc.total(a) == 4);
    }
}
