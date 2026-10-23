package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import entities.Aresta;
import entities.CartaDesenvolupament;
import entities.Hexagon;
import entities.Jugador;
import entities.Partida;
import entities.Vertex;
import enums.ColorJugador;
import enums.TipusCarta;
import enums.TipusTerreny;
import jakarta.persistence.EntityManager;

/*
 * Crea tot el necessari per iniciar una partida: el tauler (llegit dels CSV), els jugadors i la pila de cartes.
 * Treballa amb la sessió que li passen (Session és un EntityManager): NO obre cap transacció.
 * És la mateixa idea que "IniciarPartida -> creaJugadors, creaTauler..." de la teoria.
 */
public class Inicialitzador {

    public static final int PUNTS_PER_DEFECTE = 10;

    private Inicialitzador() {
    }

    public static Partida crearPartida(EntityManager em, List<String> noms, int puntsPerGuanyar) {
        Partida partida = new Partida(puntsPerGuanyar);
        em.persist(partida); // IDENTITY: l'INSERT es fa ja, i la partida ja té id
        creaTauler(em, partida);
        List<Jugador> jugadors = creaJugadors(em, partida, noms);
        creaPila(em, partida);
        partida.setJugadorActiu(jugadors.get(0)); // el jugador d'ordreTorn 1 comença
        return partida;
    }

    private static void creaTauler(EntityManager em, Partida partida) {
        // 1) Entitats SENSE dependències: vèrtexs (primer, perquè les arestes i els hexàgons hi apunten)
        Map<Integer, Vertex> vertexs = new LinkedHashMap<>();
        for (String[] fila : CsvLector.llegir("vertexs.csv")) {
            int codi = Integer.parseInt(fila[0]);
            Vertex v = new Vertex(codi);
            partida.afegirVertex(v);
            em.persist(v);
            vertexs.put(codi, v);
        }

        // 2) Arestes: cada una uneix dos vèrtexs que ja existeixen
        for (String[] fila : CsvLector.llegir("arestes.csv")) {
            Aresta a = new Aresta(Integer.parseInt(fila[0]),
                    obtenir(vertexs, Integer.parseInt(fila[1]), "arestes.csv"),
                    obtenir(vertexs, Integer.parseInt(fila[2]), "arestes.csv"));
            partida.afegirAresta(a);
            em.persist(a);
        }

        // 3) Hexàgons: terrenys i números aleatoris (19 terrenys, 18 números: el desert no en té)
        List<TipusTerreny> terrenys = new ArrayList<>();
        afegir(terrenys, TipusTerreny.BOSC, 4);
        afegir(terrenys, TipusTerreny.PASTURES, 4);
        afegir(terrenys, TipusTerreny.CAMPS, 4);
        afegir(terrenys, TipusTerreny.TURONS, 3);
        afegir(terrenys, TipusTerreny.MUNTANYES, 3);
        afegir(terrenys, TipusTerreny.DESERT, 1);
        Collections.shuffle(terrenys);

        List<Integer> numeros = new ArrayList<>(List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12));
        Collections.shuffle(numeros);

        List<String[]> filesHexagons = CsvLector.llegir("hexagons.csv");
        Map<Integer, Hexagon> hexagons = new LinkedHashMap<>();
        int i = 0;
        for (String[] fila : filesHexagons) {
            Hexagon h = new Hexagon(Integer.parseInt(fila[0]), Integer.parseInt(fila[1]), Integer.parseInt(fila[2]));
            TipusTerreny terreny = terrenys.get(i++);
            h.setTerreny(terreny);
            h.setNumero(terreny == TipusTerreny.DESERT ? null : numeros.remove(0));
            partida.afegirHexagon(h);
            hexagons.put(h.getCodi(), h);
        }

        // 4) Relació hexàgon-vèrtex (hexagon_vertex.csv): els sis vèrtexs de cada hexàgon amb la seva posició
        for (String[] fila : CsvLector.llegir("hexagon_vertex.csv")) {
            Hexagon h = obtenir(hexagons, Integer.parseInt(fila[0]), "hexagon_vertex.csv");
            Vertex v = obtenir(vertexs, Integer.parseInt(fila[1]), "hexagon_vertex.csv");
            h.afegirVertex(v, Integer.parseInt(fila[2]));
        }

        // 5) persist dels hexàgons: el cascade ALL desa també els seus HexagonVertex (els vèrtexs ja existeixen)
        for (Hexagon h : hexagons.values()) {
            em.persist(h);
            if (h.getTerreny() == TipusTerreny.DESERT) {
                partida.setLladre(h); // el lladre comença al desert
            }
        }
    }

    private static List<Jugador> creaJugadors(EntityManager em, Partida partida, List<String> noms) {
        List<String> ordre = new ArrayList<>(noms);
        Collections.shuffle(ordre); // l'ordre de torn és aleatori
        List<Jugador> jugadors = new ArrayList<>();
        for (int i = 0; i < ordre.size(); i++) {
            // el color s'assigna segons l'ordre de torn; el constructor crea els 5 RecursJugador a 0
            Jugador j = new Jugador(ordre.get(i).trim(), ColorJugador.values()[i], i + 1, partida);
            partida.afegirJugador(j);
            em.persist(j); // cascade ALL: també es desa l'inventari
            jugadors.add(j);
        }
        return jugadors;
    }

    private static void creaPila(EntityManager em, Partida partida) {
        List<CartaDesenvolupament> pila = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            pila.add(new CartaDesenvolupament(TipusCarta.CAVALLER));
        }
        for (int i = 0; i < 5; i++) {
            pila.add(new CartaDesenvolupament(TipusCarta.PUNT_VICTORIA));
        }
        for (int i = 0; i < 2; i++) {
            pila.add(new CartaDesenvolupament(TipusCarta.MONOPOLI));
        }
        for (CartaDesenvolupament c : pila) {
            partida.afegirCarta(c);
            em.persist(c);
        }
    }

    private static <T> void afegir(List<T> llista, T valor, int vegades) {
        for (int i = 0; i < vegades; i++) {
            llista.add(valor);
        }
    }

    // Si un CSV apunta a un codi que no existeix, diem QUIN fitxer i QUIN valor (en lloc d'un NullPointerException)
    private static <T> T obtenir(Map<Integer, T> mapa, int codi, String fitxer) {
        T valor = mapa.get(codi);
        if (valor == null) {
            throw new IllegalStateException(fitxer + ": no existeix el codi " + codi);
        }
        return valor;
    }
}
