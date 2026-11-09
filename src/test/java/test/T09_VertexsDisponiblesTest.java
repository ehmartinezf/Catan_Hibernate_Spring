package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entities.Vertex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/*
 * PRIORITAT 9: vertexsDisponibles (on es pot construir un poblat ara mateix).
 */
class T09_VertexsDisponiblesTest extends BaseDbTest {

    private List<Integer> ids(List<Vertex> vertexs) {
        List<Integer> ids = new ArrayList<>();
        for (Vertex v : vertexs) {
            ids.add(v.getId());
        }
        return ids;
    }

    @Test
    @DisplayName(
            "A PREPARACIO, en començar, tots els 54 vèrtexs estan disponibles i ordenats per codi")
    void totsDisponibles() {
        int partida = Joc.nova2(partidaDao);

        List<Vertex> v = jugadorDao.vertexsDisponibles(Joc.actiu(partida));

        assertEquals(54, v.size());
        assertEquals(Joc.vertexs(partida), ids(v));
    }

    @Test
    @DisplayName("Un assentament descarta el seu vèrtex i els veïns (regla de la distància)")
    void descartaVeins() {
        int partida = Joc.nova2(partidaDao);
        int actiu = Joc.actiu(partida);
        int vertex = Joc.vertexs(partida).get(0);
        List<Integer> veins = Joc.veins(vertex);
        Joc.poblat(Joc.altre(partida, actiu), vertex);

        List<Integer> disponibles = ids(jugadorDao.vertexsDisponibles(actiu));

        assertEquals(54 - 1 - veins.size(), disponibles.size());
        assertTrue(!disponibles.contains(vertex));
        for (int w : veins) {
            assertTrue(!disponibles.contains(w), "El veí " + w + " no hauria d'estar disponible");
        }
    }

    @Test
    @DisplayName("A EN_CURS només surten els vèrtexs connectats per una carretera pròpia")
    void enCursNomesAmbCarretera() {
        int partida = Joc.enCursAmbDausTirats(partidaDao, 10);
        int a = Joc.actiu(partida);
        assertTrue(
                jugadorDao.vertexsDisponibles(a).isEmpty(),
                "Sense carreteres no hi ha cap vèrtex disponible");

        List<Integer> p = Joc.cami(partida, 3);
        Joc.poblat(a, p.get(0));
        Joc.carretera(a, Joc.arestaEntre(p.get(0), p.get(1)));
        Joc.carretera(a, Joc.arestaEntre(p.get(1), p.get(2)));

        // p0 està ocupat i p1 és veí de p0: només queda p2
        assertEquals(List.of(p.get(2)), ids(jugadorDao.vertexsDisponibles(a)));
        assertTrue(jugadorDao.vertexsDisponibles(Joc.altre(partida, a)).isEmpty());
    }

    @Test
    @DisplayName("Un jugador inexistent no té vèrtexs disponibles")
    void jugadorInexistent() {
        assertTrue(jugadorDao.vertexsDisponibles(99999).isEmpty());
    }
}
