package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import enums.EstatPartida;
import enums.TipusRecurs;
import enums.TipusTerreny;

class PreparacioTest extends BaseDbTest {

    private int partida;
    private int actiu;
    private int primerVertex;

    private void escenari() {
        partida = Joc.nova2(partidaDao);
        actiu = Joc.actiu(partida);
        primerVertex = Joc.vertexs(partida).get(0);
    }

    @Test
    @DisplayName("El poblat de PREPARACIO és gratuït")
    void poblatGratuit() {
        escenari();

        String r = jugadorDao.construirPoblat(actiu, primerVertex);

        assertTrue(r.startsWith("Poblat construit"), r);
        assertEquals(1, Joc.poblats(actiu));
        assertEquals(0, Joc.total(actiu), "No s'ha de descomptar res (el jugador no té recursos)");
    }

    @Test
    @DisplayName("Només es pot col·locar un poblat per ronda")
    void nomesUnPoblatPerRonda() {
        escenari();
        Joc.ok(jugadorDao.construirPoblat(actiu, primerVertex));
        int altre = jugadorDao.vertexsDisponibles(actiu).get(0).getId();

        Joc.ko(jugadorDao.construirPoblat(actiu, altre), Missatges.PREP_POBLAT_JA_COLOCAT);
        assertEquals(1, Joc.poblats(actiu));
    }

    @Test
    @DisplayName("Un vèrtex ocupat no es pot tornar a ocupar")
    void vertexOcupat() {
        escenari();
        prepararTornDelPrimer();
        int b = Joc.actiu(partida);

        Joc.ko(jugadorDao.construirPoblat(b, primerVertex), Missatges.VERTEX_OCUPAT);
    }

    @Test
    @DisplayName("Regla de la distància: no es pot construir a un vèrtex veí d'un assentament")
    void reglaDeLaDistancia() {
        escenari();
        prepararTornDelPrimer();
        int b = Joc.actiu(partida);
        int vei = Joc.veins(primerVertex).get(0);

        Joc.ko(jugadorDao.construirPoblat(b, vei), Missatges.REGLA_DISTANCIA);
        assertEquals(0, Joc.poblats(b));
    }

    @Test
    @DisplayName("Un vèrtex d'una altra partida es rebutja")
    void vertexDAltraPartida() {
        escenari();
        int altraPartida = Joc.nova2(partidaDao);
        int vertexForaneo = Joc.vertexs(altraPartida).get(0);

        Joc.ko(jugadorDao.construirPoblat(actiu, vertexForaneo), Missatges.ALTRA_PARTIDA);
    }

    @Test
    @DisplayName("No es pot col·locar la carretera abans del poblat")
    void carreteraAbansDelPoblat() {
        escenari();

        Joc.ko(jugadorDao.construirCarretera(actiu, Joc.arestaLliure(primerVertex)), Missatges.PREP_POBLAT_PRIMER);
        assertEquals(0, Joc.carreteres(actiu));
    }

    @Test
    @DisplayName("La carretera de PREPARACIO és gratuïta i ha de tocar el poblat")
    void carreteraGratuitaTocantElPoblat() {
        escenari();
        Joc.ok(jugadorDao.construirPoblat(actiu, primerVertex));

        Joc.ok(jugadorDao.construirCarretera(actiu, Joc.arestaLliure(primerVertex)));

        assertEquals(1, Joc.carreteres(actiu));
        assertEquals(0, Joc.total(actiu));
    }

    @Test
    @DisplayName("La carretera no pot estar lluny del poblat que s'acaba de col·locar")
    void carreteraLluny() {
        escenari();
        Joc.ok(jugadorDao.construirPoblat(actiu, primerVertex));
        int lluny = jugadorDao.vertexsDisponibles(actiu).get(0).getId();

        Joc.ko(jugadorDao.construirCarretera(actiu, Joc.arestaLliure(lluny)), Missatges.PREP_CARRETERA_POBLAT);
        assertEquals(0, Joc.carreteres(actiu));
    }

    @Test
    @DisplayName("Només es pot col·locar una carretera per ronda")
    void nomesUnaCarreteraPerRonda() {
        escenari();
        Joc.ok(jugadorDao.construirPoblat(actiu, primerVertex));
        Joc.ok(jugadorDao.construirCarretera(actiu, Joc.arestaLliure(primerVertex)));

        String r = jugadorDao.construirCarretera(actiu, Joc.arestaLliure(primerVertex));

        assertTrue(r.startsWith(Missatges.ERROR), r);
        assertEquals(1, Joc.carreteres(actiu));
    }

    @Test
    @DisplayName("Una aresta que ja té carretera no es pot tornar a ocupar")
    void arestaOcupada() {
        escenari();
        int vei = Joc.veins(primerVertex).get(0);
        int aresta = Joc.arestaEntre(primerVertex, vei);
        Joc.ok(jugadorDao.construirPoblat(actiu, primerVertex));
        Joc.ok(jugadorDao.construirCarretera(actiu, aresta));
        Joc.ok(jugadorDao.passarTorn(actiu));

        Joc.ko(jugadorDao.construirCarretera(Joc.actiu(partida), aresta), Missatges.ARESTA_OCUPADA);
    }

    @Test
    @DisplayName("Una aresta d'una altra partida es rebutja")
    void arestaDAltraPartida() {
        escenari();
        int altraPartida = Joc.nova2(partidaDao);
        int arestaForana = Joc.arestaLliure(Joc.vertexs(altraPartida).get(0));
        Joc.ok(jugadorDao.construirPoblat(actiu, primerVertex));

        Joc.ko(jugadorDao.construirCarretera(actiu, arestaForana), Missatges.ALTRA_PARTIDA);
    }

    @Test
    @DisplayName("No es pot passar torn sense haver col·locat poblat i carretera")
    void passarTornSenseCompletar() {
        escenari();
        Joc.ko(jugadorDao.passarTorn(actiu), Missatges.PREP_CAL_COMPLETAR);

        Joc.ok(jugadorDao.construirPoblat(actiu, primerVertex));
        Joc.ko(jugadorDao.passarTorn(actiu), Missatges.PREP_CAL_COMPLETAR);
        assertEquals(actiu, Joc.actiu(partida), "El torn no ha d'haver canviat");
    }

    @Test
    @DisplayName("En passar torn la partida segueix a PREPARACIO i el torn passa al següent jugador")
    void passarTornDonaElTornAlSeguent() {
        escenari();
        int segon = Joc.jugadors(partida).get(1);

        int jugat = Joc.jugarTornPreparacio(jugadorDao, partida);

        assertEquals(Joc.jugadors(partida).get(0), jugat);
        assertEquals(segon, Joc.actiu(partida));
        assertEquals(EstatPartida.PREPARACIO, Joc.estat(partida));
        assertEquals(1, Joc.ronda(partida));
        assertFalse(Joc.dausTirats(partida));
    }

    @ParameterizedTest(name = "{0} jugadors")
    @ValueSource(ints = {2, 3, 4})
    @DisplayName("Ordre de serp: 1,2,..,n,n,..,2,1 i després la partida passa a EN_CURS amb el jugador 1")
    void ordreDeSerp(int jugadors) {
        String[] noms = Arrays.copyOf(new String[]{"Anna", "Bernat", "Carla", "Dani"}, jugadors);
        int id = Joc.nova(partidaDao, 10, noms);
        List<Integer> ordre = Joc.jugadors(id);

        List<Integer> esperat = new ArrayList<>(ordre);
        List<Integer> invers = new ArrayList<>(ordre);
        Collections.reverse(invers);
        esperat.addAll(invers);

        List<Integer> jugats = new ArrayList<>();
        while (Joc.estat(id) == EstatPartida.PREPARACIO) {
            jugats.add(Joc.jugarTornPreparacio(jugadorDao, id));
            assertTrue(jugats.size() <= 2 * jugadors, "La PREPARACIO dura massa torns");
        }

        assertEquals(esperat, jugats);
        assertEquals(EstatPartida.EN_CURS, Joc.estat(id));
        assertEquals(1, Joc.ronda(id));
        assertEquals(ordre.get(0), Joc.actiu(id));
        assertFalse(Joc.dausTirats(id), "En començar EN_CURS cal tirar els daus");
    }

    @Test
    @DisplayName("En acabar la PREPARACIO cada jugador té 2 poblats, 2 carreteres, 0 ciutats i 2 punts")
    void resultatDeLaPreparacio() {
        escenari();
        Joc.preparar(jugadorDao, partida);

        for (int j : Joc.jugadors(partida)) {
            assertEquals(2, Joc.poblats(j));
            assertEquals(2, Joc.carreteres(j));
            assertEquals(0, Joc.ciutats(j));
            assertEquals(2, jugadorDao.calcularPunts(j));
        }
    }

    @Test
    @DisplayName("El segon poblat dóna 1 recurs de cada hexàgon veí (el desert no dóna res)")
    void recursosInicials() {
        escenari();
        Joc.preparar(jugadorDao, partida);

        for (int j : Joc.jugadors(partida)) {
            int segonVertex = Joc.llista(Integer.class,
                    "select p.vertex.id from Poblat p where p.propietari.id = :j order by p.id", "j", j).get(1);
            Map<TipusRecurs, Integer> esperat = new EnumMap<>(TipusRecurs.class);
            for (TipusTerreny t : Joc.llista(TipusTerreny.class,
                    "select hv.hexagon.terreny from HexagonVertex hv where hv.vertex.id = :v", "v", segonVertex)) {
                if (t.getRecurs() != null) {
                    esperat.merge(t.getRecurs(), 1, Integer::sum);
                }
            }

            for (TipusRecurs r : TipusRecurs.values()) {
                assertEquals(esperat.getOrDefault(r, 0), Joc.recurs(j, r), "Recurs " + r + " del jugador " + j);
            }
        }
    }

    @Test
    @DisplayName("A PREPARACIO no es poden tirar els daus")
    void tirarDausNoPermes() {
        escenari();
        assertEquals(Missatges.NOMES_EN_CURS, partidaDao.tirarDaus(partida));
    }

    @Test
    @DisplayName("A PREPARACIO no es pot comerciar, millorar a ciutat, comprar/jugar cartes ni moure el lladre")
    void accionsNoPermeses() {
        escenari();
        Joc.donarTot(actiu, 10);

        Joc.ko(jugadorDao.comerciarBanca(actiu, TipusRecurs.FUSTA, TipusRecurs.MAO), Missatges.NOMES_EN_CURS);
        Joc.ko(jugadorDao.millorarACiutat(actiu, primerVertex), Missatges.NOMES_EN_CURS);
        Joc.ko(jugadorDao.comprarCarta(actiu), Missatges.NOMES_EN_CURS);
        Joc.ko(jugadorDao.jugarCarta(actiu, 1, null), Missatges.NOMES_EN_CURS);
        Joc.ko(jugadorDao.moureLladre(actiu, Joc.hexagonDesert(partida), null), Missatges.NOMES_EN_CURS);
        assertEquals(50, Joc.total(actiu), "No s'ha d'haver descomptat cap recurs");
    }

    @Test
    @DisplayName("Un jugador que no té el torn no pot construir a PREPARACIO")
    void noEsElTeuTorn() {
        escenari();
        int altre = Joc.altre(partida, actiu);

        Joc.ko(jugadorDao.construirPoblat(altre, primerVertex), Missatges.NO_ES_EL_TEU_TORN);
        Joc.ko(jugadorDao.construirCarretera(altre, Joc.arestaLliure(primerVertex)), Missatges.NO_ES_EL_TEU_TORN);
        Joc.ko(jugadorDao.passarTorn(altre), Missatges.NO_ES_EL_TEU_TORN);
    }

    private void prepararTornDelPrimer() {
        Joc.ok(jugadorDao.construirPoblat(actiu, primerVertex));
        Joc.ok(jugadorDao.construirCarretera(actiu, Joc.arestaLliure(primerVertex)));
        Joc.ok(jugadorDao.passarTorn(actiu));
    }
}
