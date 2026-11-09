package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import enums.ColorJugador;
import enums.EstatPartida;
import enums.TipusCarta;
import enums.TipusRecurs;
import enums.TipusTerreny;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * PRIORITAT 1: sense una partida ben creada no funciona res més.
 * Comprova iniciarPartida: validacions d'entrada, estat inicial, tauler (19 hexàgons, 54 vèrtexs, 72 arestes),
 * jugadors (ordre aleatori, colors, inventari a 0) i pila de cartes.
 */
class T01_IniciarPartidaTest extends BaseDbTest {

    // --------------------------------- estat inicial ---------------------------------
    @Test
    @DisplayName("Una partida nova comença a PREPARACIO, ronda 1, 10 punts i sense daus tirats")
    void estatInicial() {
        int id = partidaDao.iniciarPartida(List.of("Anna", "Bernat"));

        assertTrue(id > 0);
        assertEquals(EstatPartida.PREPARACIO, Joc.estat(id));
        assertEquals(1, Joc.ronda(id));
        assertEquals(10, Joc.puntsPerGuanyar(id));
        assertFalse(Joc.dausTirats(id));
        assertFalse(Joc.lladrePendent(id));
        assertEquals(0, Joc.ultimaTirada(id));
        assertNull(Joc.idGuanyador(id));
    }

    @Test
    @DisplayName("Es poden indicar els punts per guanyar")
    void puntsPersonalitzats() {
        int id = partidaDao.iniciarPartida(List.of("Anna", "Bernat"), 4);
        assertEquals(4, Joc.puntsPerGuanyar(id));
    }

    @Test
    @DisplayName("Comença el jugador d'ordre de torn 1")
    void comencaElPrimerJugador() {
        int id = Joc.nova2(partidaDao);
        assertEquals(Joc.jugadors(id).get(0), Joc.actiu(id));
    }

    // --------------------------------- validacions ---------------------------------
    @Test
    @DisplayName("Amb menys de 2 o més de 4 jugadors no es crea la partida (-1)")
    void nombreDeJugadorsInvalid() {
        assertEquals(-1, partidaDao.iniciarPartida(List.of("Anna")));
        assertEquals(-1, partidaDao.iniciarPartida(List.of()));
        assertEquals(-1, partidaDao.iniciarPartida(List.of("A", "B", "C", "D", "E")));
        assertEquals(-1, partidaDao.iniciarPartida((List<String>) null));
    }

    @Test
    @DisplayName("Entre 2 i 4 jugadors és vàlid")
    void nombreDeJugadorsValid() {
        assertTrue(partidaDao.iniciarPartida(List.of("A", "B")) > 0);
        assertTrue(partidaDao.iniciarPartida(List.of("A", "B", "C")) > 0);
        assertTrue(partidaDao.iniciarPartida(List.of("A", "B", "C", "D")) > 0);
    }

    @Test
    @DisplayName("Cap jugador pot tenir el nom nul, buit o en blanc")
    void nomsInvalids() {
        assertEquals(-1, partidaDao.iniciarPartida(Arrays.asList("Anna", null)));
        assertEquals(-1, partidaDao.iniciarPartida(List.of("Anna", "")));
        assertEquals(-1, partidaDao.iniciarPartida(List.of("Anna", "   ")));
    }

    @Test
    @DisplayName("Els punts per guanyar han de ser com a mínim 3")
    void puntsPerGuanyarMinim() {
        assertEquals(-1, partidaDao.iniciarPartida(List.of("Anna", "Bernat"), 2));
        assertEquals(-1, partidaDao.iniciarPartida(List.of("Anna", "Bernat"), 0));
        assertEquals(-1, partidaDao.iniciarPartida(List.of("Anna", "Bernat"), -5));
        assertTrue(partidaDao.iniciarPartida(List.of("Anna", "Bernat"), 3) > 0);
    }

    @Test
    @DisplayName("Una partida rebutjada no deixa res a la base de dades")
    void partidaRebutjadaNoDesaRes() {
        partidaDao.iniciarPartida(List.of("Anna"));
        partidaDao.iniciarPartida(List.of("Anna", " "));

        assertTrue(partidaDao.list().isEmpty());
        assertEquals(0, Joc.comptar("select count(h) from Hexagon h"));
        assertEquals(0, Joc.comptar("select count(j) from Jugador j"));
    }

    // --------------------------------- tauler ---------------------------------
    @Test
    @DisplayName("El tauler té 19 hexàgons, 54 vèrtexs, 72 arestes i 114 relacions hexàgon-vèrtex")
    void midesDelTauler() {
        int id = Joc.nova2(partidaDao);

        assertEquals(
                19, Joc.comptar("select count(h) from Hexagon h where h.partida.id = :p", "p", id));
        assertEquals(
                54, Joc.comptar("select count(v) from Vertex v where v.partida.id = :p", "p", id));
        assertEquals(
                72, Joc.comptar("select count(a) from Aresta a where a.partida.id = :p", "p", id));
        assertEquals(
                114,
                Joc.comptar(
                        "select count(hv) from HexagonVertex hv where hv.hexagon.partida.id = :p",
                        "p",
                        id));
    }

    @Test
    @DisplayName(
            "Distribució de terrenys: 4 boscs, 4 pastures, 4 camps, 3 turons, 3 muntanyes i 1"
                    + " desert")
    void distribucioDeTerrenys() {
        int id = Joc.nova2(partidaDao);

        assertEquals(4, terreny(id, TipusTerreny.BOSC));
        assertEquals(4, terreny(id, TipusTerreny.PASTURES));
        assertEquals(4, terreny(id, TipusTerreny.CAMPS));
        assertEquals(3, terreny(id, TipusTerreny.TURONS));
        assertEquals(3, terreny(id, TipusTerreny.MUNTANYES));
        assertEquals(1, terreny(id, TipusTerreny.DESERT));
    }

    private long terreny(int idPartida, TipusTerreny t) {
        return Joc.comptar(
                "select count(h) from Hexagon h where h.partida.id = :p and h.terreny = :t",
                "p",
                idPartida,
                "t",
                t);
    }

    @Test
    @DisplayName(
            "Els 18 números són 2, 3x2, 4x2, 5x2, 6x2, 8x2, 9x2, 10x2, 11x2 i 12 (cap 7) i el"
                    + " desert no en té")
    void numerosDeFitxes() {
        int id = Joc.nova2(partidaDao);

        List<Integer> numeros =
                Joc.llista(
                        Integer.class,
                        "select h.numero from Hexagon h where h.partida.id = :p and h.numero is not"
                                + " null order by h.numero",
                        "p",
                        id);
        assertEquals(List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12), numeros);

        assertEquals(
                0,
                Joc.comptar(
                        "select count(h) from Hexagon h where h.partida.id = :p and h.terreny = :t"
                                + " and h.numero is not null",
                        "p",
                        id,
                        "t",
                        TipusTerreny.DESERT));
    }

    @Test
    @DisplayName("El lladre comença al desert")
    void lladreAlDesert() {
        int id = Joc.nova2(partidaDao);
        assertEquals(Joc.hexagonDesert(id), Joc.idLladre(id));
    }

    @Test
    @DisplayName("Cada hexàgon té exactament 6 vèrtexs a les posicions 0..5")
    void cadaHexagonTeSisVertexs() {
        int id = Joc.nova2(partidaDao);

        List<Long> perHexagon =
                Joc.llista(
                        Long.class,
                        "select count(hv) from HexagonVertex hv where hv.hexagon.partida.id = :p"
                                + " group by hv.hexagon.id",
                        "p",
                        id);
        assertEquals(19, perHexagon.size());
        for (long n : perHexagon) {
            assertEquals(6, n);
        }

        int hexagon = Joc.hexagonDesert(id);
        List<Integer> posicions =
                Joc.llista(
                        Integer.class,
                        "select hv.posicio from HexagonVertex hv where hv.hexagon.id = :h order by"
                                + " hv.posicio",
                        "h",
                        hexagon);
        assertEquals(List.of(0, 1, 2, 3, 4, 5), posicions);
    }

    @Test
    @DisplayName("Les arestes uneixen dos vèrtexs diferents de la mateixa partida")
    void arestesCoherents() {
        int id = Joc.nova2(partidaDao);

        assertEquals(
                0,
                Joc.comptar(
                        "select count(a) from Aresta a where a.partida.id = :p and a.vertexA ="
                                + " a.vertexB",
                        "p",
                        id));
        assertEquals(
                0,
                Joc.comptar(
                        "select count(a) from Aresta a where a.partida.id = :p "
                                + "and (a.vertexA.partida.id <> :p or a.vertexB.partida.id <> :p)",
                        "p",
                        id));
    }

    @Test
    @DisplayName("Cada vèrtex té 2 o 3 veïns")
    void graus() {
        int id = Joc.nova2(partidaDao);
        for (int v : Joc.vertexs(id)) {
            int veins = Joc.veins(v).size();
            assertTrue(veins == 2 || veins == 3, "El vèrtex " + v + " té " + veins + " veïns");
        }
    }

    @Test
    @DisplayName("Dues partides tenen taulers independents")
    void taulersIndependents() {
        int a = Joc.nova2(partidaDao);
        int b = Joc.nova2(partidaDao);

        assertNotEquals(a, b);
        assertEquals(54, Joc.vertexs(a).size());
        assertEquals(54, Joc.vertexs(b).size());
        List<Integer> junts = new ArrayList<>(Joc.vertexs(a));
        junts.retainAll(Joc.vertexs(b));
        assertTrue(junts.isEmpty(), "Cap vèrtex pot ser de les dues partides");
        assertEquals(2, partidaDao.list().size());
    }

    // --------------------------------- jugadors ---------------------------------
    @Test
    @DisplayName("L'ordre de torn és 1..n i l'assignació de colors segueix aquest ordre")
    void ordreIColors() {
        int id = Joc.nova(partidaDao, 10, "Anna", "Bernat", "Carla", "Dani");

        List<Integer> ordre =
                Joc.llista(
                        Integer.class,
                        "select j.ordreTorn from Jugador j where j.partida.id = :p order by"
                                + " j.ordreTorn",
                        "p",
                        id);
        assertEquals(List.of(1, 2, 3, 4), ordre);

        List<ColorJugador> colors =
                Joc.llista(
                        ColorJugador.class,
                        "select j.color from Jugador j where j.partida.id = :p order by"
                                + " j.ordreTorn",
                        "p",
                        id);
        assertEquals(
                List.of(
                        ColorJugador.VERMELL,
                        ColorJugador.BLAU,
                        ColorJugador.BLANC,
                        ColorJugador.TARONJA),
                colors);
    }

    @Test
    @DisplayName("Hi ha tants jugadors com noms, tots amb el seu nom (sense espais sobrants)")
    void noms() {
        int id = Joc.nova(partidaDao, 10, " Anna ", "Bernat", "Carla");

        List<String> noms =
                new ArrayList<>(
                        Joc.llista(
                                String.class,
                                "select j.nom from Jugador j where j.partida.id = :p",
                                "p",
                                id));
        Collections.sort(noms);
        assertEquals(List.of("Anna", "Bernat", "Carla"), noms);
    }

    @Test
    @DisplayName("Cada jugador comença amb els 5 recursos a 0")
    void inventariInicialA0() {
        int id = Joc.nova2(partidaDao);

        for (int j : Joc.jugadors(id)) {
            assertEquals(
                    5,
                    Joc.comptar(
                            "select count(r) from RecursJugador r where r.jugador.id = :j",
                            "j",
                            j));
            for (TipusRecurs t : TipusRecurs.values()) {
                assertEquals(0, Joc.recurs(j, t), "Recurs " + t + " del jugador " + j);
            }
        }
    }

    @Test
    @DisplayName("Cada jugador comença amb 0 punts, 0 construccions i 0 cartes")
    void jugadorsBuits() {
        int id = Joc.nova2(partidaDao);

        for (int j : Joc.jugadors(id)) {
            assertEquals(0, jugadorDao.calcularPunts(j));
            assertEquals(0, Joc.poblats(j));
            assertEquals(0, Joc.ciutats(j));
            assertEquals(0, Joc.carreteres(j));
            assertEquals(0, Joc.cartesDelJugador(j));
            assertEquals(0, Joc.cavallers(j));
        }
    }

    // --------------------------------- pila de cartes ---------------------------------
    @Test
    @DisplayName("La pila té 14 cavallers, 5 punts de victòria i 2 monopolis, sense propietari")
    void pilaDeCartes() {
        int id = Joc.nova2(partidaDao);

        assertEquals(21, Joc.cartesALaPila(id));
        assertEquals(14, cartes(id, TipusCarta.CAVALLER));
        assertEquals(5, cartes(id, TipusCarta.PUNT_VICTORIA));
        assertEquals(2, cartes(id, TipusCarta.MONOPOLI));
        assertEquals(
                0,
                Joc.comptar(
                        "select count(c) from CartaDesenvolupament c where c.partida.id = :p and"
                                + " c.usada = true",
                        "p",
                        id));
    }

    private long cartes(int idPartida, TipusCarta t) {
        return Joc.comptar(
                "select count(c) from CartaDesenvolupament c where c.partida.id = :p and c.tipus ="
                        + " :t",
                "p",
                idPartida,
                "t",
                t);
    }
}
