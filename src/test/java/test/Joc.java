package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entities.Carretera;
import entities.CartaDesenvolupament;
import entities.Ciutat;
import entities.Hexagon;
import entities.Jugador;
import entities.Partida;
import entities.Poblat;
import entities.RecursJugador;
import entities.Vertex;

import enums.EstatPartida;
import enums.TipusCarta;
import enums.TipusRecurs;
import enums.TipusTerreny;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/*
 * Utilitats dels tests: munten escenaris "a mà" (saltant-se les regles) i consulten l'estat de la BD.
 * Fer-ho així permet que cada test provi UNA sola cosa sense dependre de l'atzar dels daus ni del tauler.
 * Totes les operacions són una transacció pròpia (igual que els DAO), per això no es poden aniuar.
 */
final class Joc {

    private Joc() {}

    // ===================================== transaccions =====================================
    static <T> T tx(Function<Session, T> accio) {
        Session s = SessionManager.getSessionFactory().getCurrentSession();
        try {
            s.beginTransaction();
            T resultat = accio.apply(s);
            s.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            if (s.getTransaction().isActive()) {
                s.getTransaction().rollback();
            }
            throw e;
        }
    }

    static void txv(Consumer<Session> accio) {
        tx(
                s -> {
                    accio.accept(s);
                    return null;
                });
    }

    static <T> List<T> llista(Class<T> tipus, String hql, Object... parelles) {
        return tx(
                s -> {
                    Query<T> q = s.createQuery(hql, tipus);
                    for (int i = 0; i < parelles.length; i += 2) {
                        q.setParameter((String) parelles[i], parelles[i + 1]);
                    }
                    return q.getResultList();
                });
    }

    static <T> T un(Class<T> tipus, String hql, Object... parelles) {
        return llista(tipus, hql, parelles).get(0);
    }

    static long comptar(String hql, Object... parelles) {
        return un(Long.class, hql, parelles);
    }

    // ===================================== comprovacions =====================================
    /** El resultat NO ha de ser un error. */
    static void ok(String resultat) {
        assertFalse(
                resultat.startsWith(Missatges.ERROR),
                "S'esperava èxit però ha tornat: " + resultat);
    }

    /** El resultat ha de ser EXACTAMENT "ERROR: " + missatge. */
    static void ko(String resultat, String missatge) {
        assertEquals(Missatges.ERROR + missatge, resultat);
    }

    /** El resultat ha de començar per "ERROR: " + missatge (alguns errors hi afegeixen detalls). */
    static void koInici(String resultat, String missatge) {
        assertTrue(
                resultat.startsWith(Missatges.ERROR + missatge),
                "S'esperava un error que comencés per '"
                        + Missatges.ERROR
                        + missatge
                        + "' però ha tornat: "
                        + resultat);
    }

    // ===================================== crear partides =====================================
    static int nova(PartidaDao dao, int puntsPerGuanyar, String... noms) {
        int id = dao.iniciarPartida(List.of(noms), puntsPerGuanyar);
        assertTrue(id > 0, "La partida s'hauria d'haver creat");
        return id;
    }

    /** Partida de 2 jugadors (Anna i Bernat) a PREPARACIO. */
    static int nova2(PartidaDao dao) {
        return nova(dao, 10, "Anna", "Bernat");
    }

    /**
     * Partida de 2 jugadors ja EN_CURS: l'actiu és el jugador d'ordre 1 i ha tirat els daus.
     * Retorna l'id de la partida.
     */
    static int enCursAmbDausTirats(PartidaDao dao, int puntsPerGuanyar) {
        int id = nova(dao, puntsPerGuanyar, "Anna", "Bernat");
        enCurs(id, jugadors(id).get(0), 1, true);
        return id;
    }

    // ===================================== estat de la partida
    // =====================================
    static int actiu(int idPartida) {
        return un(
                Integer.class,
                "select p.jugadorActiu.id from Partida p where p.id = :p",
                "p",
                idPartida);
    }

    /** Ids dels jugadors ordenats per ordre de torn. */
    static List<Integer> jugadors(int idPartida) {
        return llista(
                Integer.class,
                "select j.id from Jugador j where j.partida.id = :p order by j.ordreTorn",
                "p",
                idPartida);
    }

    static int altre(int idPartida, int idJugador) {
        for (int id : jugadors(idPartida)) {
            if (id != idJugador) {
                return id;
            }
        }
        throw new IllegalStateException("No hi ha cap altre jugador");
    }

    static String nom(int idJugador) {
        return un(String.class, "select j.nom from Jugador j where j.id = :j", "j", idJugador);
    }

    static <T> T partida(int idPartida, Function<Partida, T> lector) {
        return tx(s -> lector.apply(s.get(Partida.class, idPartida)));
    }

    static EstatPartida estat(int idPartida) {
        return partida(idPartida, Partida::getEstat);
    }

    static int ronda(int idPartida) {
        return partida(idPartida, Partida::getRonda);
    }

    static boolean dausTirats(int idPartida) {
        return partida(idPartida, Partida::isDausTirats);
    }

    static boolean lladrePendent(int idPartida) {
        return partida(idPartida, Partida::isLladrePendent);
    }

    static int ultimaTirada(int idPartida) {
        return partida(idPartida, Partida::getUltimaTirada);
    }

    static int puntsPerGuanyar(int idPartida) {
        return partida(idPartida, Partida::getPuntsPerGuanyar);
    }

    static Integer idLladre(int idPartida) {
        return partida(
                idPartida, p -> p.getHexagonLladre() == null ? null : p.getHexagonLladre().getId());
    }

    static Integer idGuanyador(int idPartida) {
        return partida(idPartida, p -> p.getGuanyador() == null ? null : p.getGuanyador().getId());
    }

    // ===================================== forçar l'estat =====================================
    static void enCurs(int idPartida, int idActiu, int ronda, boolean dausTirats) {
        txv(
                s -> {
                    Partida p = s.get(Partida.class, idPartida);
                    p.setEstat(EstatPartida.EN_CURS);
                    p.setRonda(ronda);
                    p.setJugadorActiu(s.get(Jugador.class, idActiu));
                    p.setDausTirats(dausTirats);
                    p.setLladrePendent(false);
                });
    }

    static void setEstat(int idPartida, EstatPartida estat) {
        txv(s -> s.get(Partida.class, idPartida).setEstat(estat));
    }

    static void setActiu(int idPartida, int idJugador) {
        txv(s -> s.get(Partida.class, idPartida).setJugadorActiu(s.get(Jugador.class, idJugador)));
    }

    static void setDaus(int idPartida, boolean tirats) {
        txv(s -> s.get(Partida.class, idPartida).setDausTirats(tirats));
    }

    static void setLladrePendent(int idPartida, boolean pendent) {
        txv(s -> s.get(Partida.class, idPartida).setLladrePendent(pendent));
    }

    static void setLladre(int idPartida, int idHexagon) {
        txv(s -> s.get(Partida.class, idPartida).setHexagonLladre(s.get(Hexagon.class, idHexagon)));
    }

    static void setCavallers(int idJugador, int cavallers) {
        txv(s -> s.get(Jugador.class, idJugador).setCavallersJugats(cavallers));
    }

    static int cavallers(int idJugador) {
        return tx(s -> s.get(Jugador.class, idJugador).getCavallersJugats());
    }

    // ===================================== recursos =====================================
    static int recurs(int idJugador, TipusRecurs tipus) {
        return un(
                Integer.class,
                "select r.quantitat from RecursJugador r where r.jugador.id = :j and r.tipusRecurs"
                        + " = :t",
                "j",
                idJugador,
                "t",
                tipus);
    }

    static int total(int idJugador) {
        Long suma =
                un(
                        Long.class,
                        "select sum(r.quantitat) from RecursJugador r where r.jugador.id = :j",
                        "j",
                        idJugador);
        return suma == null ? 0 : suma.intValue();
    }

    static void setRecurs(int idJugador, TipusRecurs tipus, int quantitat) {
        txv(
                s -> {
                    RecursJugador r =
                            s.createQuery(
                                            "from RecursJugador r where r.jugador.id = :j and"
                                                    + " r.tipusRecurs = :t",
                                            RecursJugador.class)
                                    .setParameter("j", idJugador)
                                    .setParameter("t", tipus)
                                    .getSingleResult();
                    r.setQuantitat(quantitat);
                });
    }

    /** Posa la mateixa quantitat de TOTS els recursos. */
    static void donarTot(int idJugador, int quantitat) {
        for (TipusRecurs t : TipusRecurs.values()) {
            setRecurs(idJugador, t, quantitat);
        }
    }

    /** Posa exactament aquests recursos (la resta, a 0). */
    static void setRecursos(int idJugador, int fusta, int mao, int llana, int blat, int mineral) {
        setRecurs(idJugador, TipusRecurs.FUSTA, fusta);
        setRecurs(idJugador, TipusRecurs.MAO, mao);
        setRecurs(idJugador, TipusRecurs.LLANA, llana);
        setRecurs(idJugador, TipusRecurs.BLAT, blat);
        setRecurs(idJugador, TipusRecurs.MINERAL, mineral);
    }

    // ===================================== construccions "a mà"
    // =====================================
    static void poblat(int idJugador, int idVertex) {
        txv(
                s -> {
                    Poblat p = new Poblat();
                    p.setPropietari(s.get(Jugador.class, idJugador));
                    p.setVertex(s.get(Vertex.class, idVertex));
                    s.persist(p);
                });
    }

    static void ciutat(int idJugador, int idVertex) {
        txv(
                s -> {
                    Ciutat c = new Ciutat();
                    c.setPropietari(s.get(Jugador.class, idJugador));
                    c.setVertex(s.get(Vertex.class, idVertex));
                    s.persist(c);
                });
    }

    static void carretera(int idJugador, int idAresta) {
        txv(
                s -> {
                    Carretera c = new Carretera();
                    c.setPropietari(s.get(Jugador.class, idJugador));
                    c.setAresta(s.get(entities.Aresta.class, idAresta));
                    s.persist(c);
                });
    }

    static long poblats(int idJugador) {
        return comptar("select count(p) from Poblat p where p.propietari.id = :j", "j", idJugador);
    }

    static long ciutats(int idJugador) {
        return comptar("select count(c) from Ciutat c where c.propietari.id = :j", "j", idJugador);
    }

    static long carreteres(int idJugador) {
        return comptar(
                "select count(c) from Carretera c where c.propietari.id = :j", "j", idJugador);
    }

    // ===================================== topologia del tauler
    // =====================================
    /** Ids dels vèrtexs d'una partida ordenats pel seu codi (1..54). */
    static List<Integer> vertexs(int idPartida) {
        return llista(
                Integer.class,
                "select v.id from Vertex v where v.partida.id = :p order by v.codi",
                "p",
                idPartida);
    }

    static List<Integer> veins(int idVertex) {
        List<Integer> veins =
                new ArrayList<>(
                        llista(
                                Integer.class,
                                "select a.vertexB.id from Aresta a where a.vertexA.id = :v",
                                "v",
                                idVertex));
        veins.addAll(
                llista(
                        Integer.class,
                        "select a.vertexA.id from Aresta a where a.vertexB.id = :v",
                        "v",
                        idVertex));
        Collections.sort(veins);
        return veins;
    }

    static int arestaEntre(int idVertexA, int idVertexB) {
        return un(
                Integer.class,
                "select a.id from Aresta a where (a.vertexA.id = :x and a.vertexB.id = :y) or"
                        + " (a.vertexA.id = :y and a.vertexB.id = :x)",
                "x",
                idVertexA,
                "y",
                idVertexB);
    }

    /** Una aresta sense carretera que toca el vèrtex. */
    static int arestaLliure(int idVertex) {
        return llista(
                        Integer.class,
                        "select a.id from Aresta a where (a.vertexA.id = :v or a.vertexB.id = :v)"
                            + " and not exists (select c.id from Carretera c where c.aresta = a)"
                            + " order by a.codi",
                        "v",
                        idVertex)
                .get(0);
    }

    /**
     * Camí simple de "llargada" vèrtexs consecutius (cada un veí del següent), començant pel vèrtex
     * de codi 1.
     */
    static List<Integer> cami(int idPartida, int llargada) {
        List<Integer> cami = new ArrayList<>();
        cami.add(vertexs(idPartida).get(0));
        if (!estendre(cami, llargada)) {
            throw new IllegalStateException("No hi ha cap camí de " + llargada + " vèrtexs");
        }
        return cami;
    }

    private static boolean estendre(List<Integer> cami, int llargada) {
        if (cami.size() == llargada) {
            return true;
        }
        for (int v : veins(cami.get(cami.size() - 1))) {
            if (!cami.contains(v)) {
                cami.add(v);
                if (estendre(cami, llargada)) {
                    return true;
                }
                cami.remove(cami.size() - 1);
            }
        }
        return false;
    }

    /**
     * "n" vèrtexs que no són veïns entre ells (es poden fer poblats a tots sense incomplir la regla
     * de la distància).
     */
    static List<Integer> independents(int idPartida, int n) {
        List<Integer> triats = new ArrayList<>();
        Set<Integer> prohibits = new HashSet<>();
        for (int v : vertexs(idPartida)) {
            if (prohibits.contains(v)) {
                continue;
            }
            triats.add(v);
            prohibits.add(v);
            prohibits.addAll(veins(v));
            if (triats.size() == n) {
                return triats;
            }
        }
        throw new IllegalStateException("No hi ha " + n + " vèrtexs independents");
    }

    /** "n" arestes connectades entre elles (recorregut en amplada) a partir d'un vèrtex. */
    static List<Integer> arestesConnexes(int inici, int n) {
        List<Integer> arestes = new ArrayList<>();
        Deque<Integer> cua = new ArrayDeque<>();
        Set<Integer> vistos = new HashSet<>();
        cua.add(inici);
        vistos.add(inici);
        while (!cua.isEmpty()) {
            int v = cua.poll();
            for (int w : veins(v)) {
                int a = arestaEntre(v, w);
                if (!arestes.contains(a)) {
                    arestes.add(a);
                    if (arestes.size() == n) {
                        return arestes;
                    }
                }
                if (vistos.add(w)) {
                    cua.add(w);
                }
            }
        }
        throw new IllegalStateException("No hi ha " + n + " arestes connectades");
    }

    // ===================================== hexàgons =====================================
    static int hexagonDesert(int idPartida) {
        return un(
                Integer.class,
                "select h.id from Hexagon h where h.partida.id = :p and h.terreny = :t",
                "p",
                idPartida,
                "t",
                TipusTerreny.DESERT);
    }

    /** Un hexàgon amb aquest número de fitxa (n'hi ha 2 de 6 i 2 de 8, etc.). */
    static int hexagonAmbNumero(int idPartida, int numero) {
        return llista(
                        Integer.class,
                        "select h.id from Hexagon h where h.partida.id = :p and h.numero = :n order"
                                + " by h.codi",
                        "p",
                        idPartida,
                        "n",
                        numero)
                .get(0);
    }

    /** Un hexàgon que no és el del lladre actual. */
    static int hexagonDiferentDelLladre(int idPartida) {
        return llista(
                        Integer.class,
                        "select h.id from Hexagon h where h.partida.id = :p and h.id <> :l order by"
                                + " h.codi",
                        "p",
                        idPartida,
                        "l",
                        idLladre(idPartida))
                .get(0);
    }

    static List<Integer> vertexsDeHexagon(int idHexagon) {
        return llista(
                Integer.class,
                "select hv.vertex.id from HexagonVertex hv where hv.hexagon.id = :h order by"
                        + " hv.posicio",
                "h",
                idHexagon);
    }

    static List<Integer> hexagonsDeVertex(int idVertex) {
        return llista(
                Integer.class,
                "select hv.hexagon.id from HexagonVertex hv where hv.vertex.id = :v",
                "v",
                idVertex);
    }

    // ===================================== cartes =====================================
    /**
     * Treu una carta d'aquest tipus de la pila i la posa a la mà del jugador. Retorna l'id de la
     * carta.
     */
    static int donarCarta(int idJugador, TipusCarta tipus, int rondaCompra) {
        return tx(
                s -> {
                    Jugador j = s.get(Jugador.class, idJugador);
                    CartaDesenvolupament c =
                            s.createQuery(
                                            "select c from CartaDesenvolupament c where"
                                                    + " c.partida.id = :p and c.jugador is null and"
                                                    + " c.tipus = :t",
                                            CartaDesenvolupament.class)
                                    .setParameter("p", j.getPartida().getId())
                                    .setParameter("t", tipus)
                                    .setMaxResults(1)
                                    .getSingleResult();
                    c.setJugador(j);
                    c.setRondaCompra(rondaCompra);
                    return c.getId();
                });
    }

    static void setUsada(int idCarta, boolean usada) {
        txv(s -> s.get(CartaDesenvolupament.class, idCarta).setUsada(usada));
    }

    static boolean usada(int idCarta) {
        return tx(s -> s.get(CartaDesenvolupament.class, idCarta).isUsada());
    }

    static long cartesALaPila(int idPartida) {
        return comptar(
                "select count(c) from CartaDesenvolupament c where c.partida.id = :p and c.jugador"
                        + " is null",
                "p",
                idPartida);
    }

    static long cartesDelJugador(int idJugador) {
        return comptar(
                "select count(c) from CartaDesenvolupament c where c.jugador.id = :j",
                "j",
                idJugador);
    }

    /** Deixa a la pila NOMÉS cartes del tipus indicat (així la carta que es compri és coneguda). */
    static void pilaNomesDe(int idPartida, TipusCarta tipus) {
        txv(
                s ->
                        s.createMutationQuery(
                                        "delete from CartaDesenvolupament c where c.partida.id = :p"
                                                + " and c.jugador is null and c.tipus <> :t")
                                .setParameter("p", idPartida)
                                .setParameter("t", tipus)
                                .executeUpdate());
    }

    static void buidarPila(int idPartida) {
        txv(
                s ->
                        s.createMutationQuery(
                                        "delete from CartaDesenvolupament c where c.partida.id = :p"
                                                + " and c.jugador is null")
                                .setParameter("p", idPartida)
                                .executeUpdate());
    }

    // ===================================== fase de PREPARACIO
    // =====================================
    /**
     * Juga el torn de l'actiu a PREPARACIO (poblat + carretera + passar torn) i retorna l'id del
     * jugador que ha jugat.
     */
    static int jugarTornPreparacio(JugadorDao jd, int idPartida) {
        int id = actiu(idPartida);
        List<Vertex> lliures = jd.vertexsDisponibles(id);
        assertFalse(lliures.isEmpty(), "Hauria d'haver-hi vèrtexs disponibles");
        int vertex = lliures.get(0).getId();
        ok(jd.construirPoblat(id, vertex));
        ok(jd.construirCarretera(id, arestaLliure(vertex)));
        ok(jd.passarTorn(id));
        return id;
    }

    /**
     * Juga tota la PREPARACIO: en acabar la partida és EN_CURS, ronda 1, i l'actiu és el jugador
     * d'ordre 1.
     */
    static void preparar(JugadorDao jd, int idPartida) {
        int torns = 0;
        while (estat(idPartida) == EstatPartida.PREPARACIO) {
            jugarTornPreparacio(jd, idPartida);
            assertTrue(++torns <= 20, "La PREPARACIO no acaba mai");
        }
    }
}
