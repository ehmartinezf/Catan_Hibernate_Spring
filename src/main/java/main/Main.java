package main;

import java.util.List;

import org.hibernate.Session;

import com.mysql.cj.xdevapi.SessionFactory;

import dao.JugadorDao;
import dao.PartidaDao;
import dao.SessionManager;
import entities.CartaDesenvolupament;
import entities.Jugador;
import entities.Vertex;
import enums.EstatPartida;
import enums.TipusCarta;
import enums.TipusRecurs;

public class Main {

    private static final PartidaDao partidaDao = new PartidaDao();
    private static final JugadorDao jugadorDao = new JugadorDao();

    public static void main(String[] args) {
    	Session session = SessionManager.getSessionFactory().getCurrentSession();
        try {
            escenariBase();
            escenariAvancat();
        } finally {
            SessionManager.tancar();
        }
    }

    private static void escenariBase() {
        titol("1. INICIAR PARTIDA");
        System.out.println("Amb 1 jugador (ha de fallar): " + partidaDao.iniciarPartida(List.of("Anna")));
        int idPartida = partidaDao.iniciarPartida(List.of("Anna", "Bernat", "Carla"), 4);
        System.out.println("Partida creada amb id " + idPartida);
        System.out.println(partidaDao.mostrarPartida(idPartida));

        titol("2. PREPARACIO: validacions i dos poblats + dues carreteres per jugador (ordre de serp)");
        int idActiu = actiu(idPartida);
        int idAltre = altreJugador(idPartida, idActiu);
        System.out.println("Tirar daus a PREPARACIO: " + partidaDao.tirarDaus(idPartida));
        System.out.println("Passar torn sense construir: " + jugadorDao.passarTorn(idActiu));
        System.out.println("Carretera abans del poblat: " + jugadorDao.construirCarretera(idActiu, 1));
        System.out.println("Poblat d'un jugador sense torn: " + jugadorDao.construirPoblat(idAltre, 1));
        System.out.println("Comerciar a PREPARACIO: "
                + jugadorDao.comerciarBanca(idActiu, TipusRecurs.FUSTA, TipusRecurs.MAO));
        prepararPartida(idPartida, true);
        System.out.println(partidaDao.mostrarPartida(idPartida));
        System.out.println(partidaDao.mostrarTauler(idPartida));

        titol("3. EN_CURS: validacions de torn");
        idActiu = actiu(idPartida);
        idAltre = altreJugador(idPartida, idActiu);
        System.out.println("Construir sense tirar daus: " + jugadorDao.construirCarretera(idActiu, 1));
        System.out.println("Passar torn sense tirar daus: " + jugadorDao.passarTorn(idActiu));
        System.out.println("Un jugador que no és l'actiu: " + jugadorDao.construirCarretera(idAltre, 1));

        titol("4. TIRAR DAUS (producció)");
        tirarDausIMoure(idPartida);
        System.out.println("Tirar de nou el mateix torn: " + partidaDao.tirarDaus(idPartida));

        titol("5. CONSTRUIR (donem recursos de sobra per provar)");
        idActiu = actiu(idPartida);
        donarRecursos(idActiu, 30);
        jugadorDao.mostrarRecursos(idActiu);
        int vertexPoblat = vertexAmbPoblat(idActiu);
        int idAresta = aresta(vertexPoblat);
        System.out.println("Carretera: " + jugadorDao.construirCarretera(idActiu, idAresta));
        System.out.println("Mateixa aresta un altre cop: " + jugadorDao.construirCarretera(idActiu, idAresta));
        System.out.println("Comerç amb la banca 4:1: "
                + jugadorDao.comerciarBanca(idActiu, TipusRecurs.FUSTA, TipusRecurs.MINERAL));
        System.out.println("Comerç amb el mateix recurs: "
                + jugadorDao.comerciarBanca(idActiu, TipusRecurs.FUSTA, TipusRecurs.FUSTA));
        List<Vertex> disponibles = jugadorDao.vertexsDisponibles(idActiu);
        for (int i = 0; i < 6 && disponibles.isEmpty(); i++) {
        	
            System.out.println("Allarguem una carretera: " + jugadorDao.construirCarretera(idActiu, aresteConnectada(idActiu)));
            disponibles = jugadorDao.vertexsDisponibles(idActiu);
        }
        System.out.println("Vèrtexs disponibles ara: " + disponibles.size());
        System.out.println("Poblat en un vèrtex ocupat: " + jugadorDao.construirPoblat(idActiu, vertexPoblat));
        System.out.println("Poblat nou: " + jugadorDao.construirPoblat(idActiu, disponibles.get(0).getId()));
        System.out.println("Punts de " + idActiu + ": " + jugadorDao.calcularPunts(idActiu));
        System.out.println("Millorar un vèrtex sense poblat: " + jugadorDao.millorarACiutat(idActiu, disponibles.get(0).getId() + 1));
        System.out.println("Millorar el poblat original a ciutat: " + jugadorDao.millorarACiutat(idActiu, vertexPoblat));
        System.out.println(partidaDao.mostrarPartida(idPartida));
        System.out.println("Comprovar victòria: ");
        partidaDao.comprovarVictoria(idPartida);

        titol("6. PARTIDA FINALITZADA: no admet cap acció");
        System.out.println(jugadorDao.passarTorn(actiu(idPartida)));
        System.out.println(partidaDao.tirarDaus(idPartida));
    }

    private static void escenariAvancat() {
        titol("7. PART AVANÇADA: cartes, lladre i exèrcit més gran (partida nova, 10 punts)");
        int idPartida = partidaDao.iniciarPartida(List.of("Dani", "Eva"));
        prepararPartida(idPartida, false);
        int idJug = actiu(idPartida);
        int idRival = altreJugador(idPartida, idJug);
        donarRecursos(idJug, 150);

        donarCarta(idJug, TipusCarta.CAVALLER);
        donarCarta(idJug, TipusCarta.CAVALLER);
        donarCarta(idJug, TipusCarta.CAVALLER);
        donarCarta(idJug, TipusCarta.MONOPOLI);
        donarCarta(idJug, TipusCarta.PUNT_VICTORIA);
        System.out.println("Carta comprada aquest torn, sense tirar daus: " + jugadorDao.comprarCarta(idJug));

        System.out.println("-- Tirem els daus");
        tirarDausIMoure(idPartida);
        System.out.println("Comprar carta (torn actiu, daus tirats): " + jugadorDao.comprarCarta(idJug).replace("\n", " | "));
        System.out.println("Jugar la carta que acabem de comprar: " + jugadorDao.jugarCarta(idJug,
                consulta("select c.id from CartaDesenvolupament c where c.jugador.id = :j and c.rondaCompra = 1", "j", idJug).get(0), null));

        int idCav1 = idCarta(idJug, TipusCarta.CAVALLER, 0);
        int idCav2 = idCarta(idJug, TipusCarta.CAVALLER, 1);
        int idCav3 = idCarta(idJug, TipusCarta.CAVALLER, 2);
        int idMono = idCarta(idJug, TipusCarta.MONOPOLI, 0);
        int idPV = idCarta(idJug, TipusCarta.PUNT_VICTORIA, 0);

        System.out.println("Jugar PUNT_VICTORIA: " + jugadorDao.jugarCarta(idJug, idPV, null));
        System.out.println("Jugar MONOPOLI sense indicar recurs: " + jugadorDao.jugarCarta(idJug, idMono, null));
        System.out.println("Jugar la carta d'un altre jugador: " + jugadorDao.jugarCarta(idRival, idCav1, null));

        int hexVictima = hexagonAmbVictima(idPartida, idJug);
        donarRecursos(idRival, 2);
        System.out.println("Jugar CAVALLER 1: " + jugadorDao.jugarCarta(idJug, idCav1, null));
        System.out.println("Passar torn amb el lladre pendent: " + jugadorDao.passarTorn(idJug));
        System.out.println("Tornar a jugar una carta amb el lladre pendent: " + jugadorDao.jugarCarta(idJug, idCav2, null));
        System.out.println("Moure el lladre a l'hexàgon actual: "
                + jugadorDao.moureLladre(idJug, lladre(idPartida), null));
        System.out.println("Moure el lladre amb una víctima invàlida: "
                + jugadorDao.moureLladre(idJug, hexVictima, idJug));
        System.out.println("Moure el lladre robant al rival: " + jugadorDao.moureLladre(idJug, hexVictima, idRival));
        System.out.println("Jugar CAVALLER 2: " + jugadorDao.jugarCarta(idJug, idCav2, null));
        System.out.println(jugadorDao.moureLladre(idJug, hexagonBuit(idPartida), null));
        System.out.println("Exèrcit més gran amb 2 cavallers: " + jugadorDao.exercitMesGran(idPartida));
        System.out.println("Jugar CAVALLER 3: " + jugadorDao.jugarCarta(idJug, idCav3, null));
        System.out.println(jugadorDao.moureLladre(idJug, hexVictima, idRival));
        System.out.println("Exèrcit més gran amb 3 cavallers: " + jugadorDao.exercitMesGran(idPartida));
        System.out.println("Punts de " + idJug + " (inclou +2 d'exèrcit i la carta PUNT_VICTORIA): " + jugadorDao.calcularPunts(idJug));

        donarRecursos(idRival, 3);
        System.out.println("Recursos del rival abans del monopoli: ");
        jugadorDao.mostrarRecursos(idRival);
        System.out.println("Jugar MONOPOLI de FUSTA: " + jugadorDao.jugarCarta(idJug, idMono, TipusRecurs.FUSTA));
        System.out.println("Recursos del rival després: ");
        jugadorDao.mostrarRecursos(idRival);
        System.out.println(partidaDao.mostrarPartida(idPartida));
        System.out.println("Vèrtexs disponibles (qualsevol): " + jugadorDao.vertexsDisponibles(idJug).size());
    }

    private static void titol(String text) {
        System.out.println("\n########## " + text + " ##########");
    }

    private static void prepararPartida(int idPartida, boolean imprimir) {
        while (partidaDao.get(idPartida).getEstat() == EstatPartida.PREPARACIO) {
            int idActiu = actiu(idPartida);
            List<Vertex> lliures = jugadorDao.vertexsDisponibles(idActiu);
            Vertex v = lliures.get((int) (Math.random() * lliures.size()));
            String r1 = jugadorDao.construirPoblat(idActiu, v.getId());
            String r2 = jugadorDao.construirCarretera(idActiu, aresta(v.getId()));
            String r3 = jugadorDao.passarTorn(idActiu);
            if (imprimir) {
                System.out.println("  jugador " + idActiu + ": poblat al vèrtex " + v.getCodi() + " (" + r1 + "), " + r2 + ", " + r3);
            }
        }
    }

    private static void tirarDausIMoure(int idPartida) {
        String tirada = partidaDao.tirarDaus(idPartida);
        System.out.println(tirada);
        if (tirada.contains("= 7")) {
            System.out.println("Passar torn amb el lladre pendent: " + jugadorDao.passarTorn(actiu(idPartida)));
            System.out.println(jugadorDao.moureLladre(actiu(idPartida), hexagonBuit(idPartida), null));
        }
    }

    private static int actiu(int idPartida) {
        return partidaDao.get(idPartida).getJugadorActiu().getId();
    }

    private static int altreJugador(int idPartida, int idActiu) {
        for (Jugador j : jugadorDao.list()) {
            if (!j.getId().equals(idActiu) && j.getPartida().getId() == idPartida) {
                return j.getId();
            }
        }
        return -1;
    }

    private static void donarRecursos(int idJugador, int quantitat) {
        Jugador j = jugadorDao.get(idJugador);
        for (TipusRecurs t : TipusRecurs.values()) {
            jugadorDao.afegirRecurs(j,t, quantitat);
        }
        jugadorDao.saveOrUpdate(j);
    }

    private static void donarCarta(int idJugador, TipusCarta tipus) {
        Session s = SessionManager.getSessionFactory().getCurrentSession();
        s.beginTransaction();
        Jugador j = s.find(Jugador.class, idJugador);
        CartaDesenvolupament c = s.createQuery("""
                select c from CartaDesenvolupament c
                where c.partida.id = :p and c.jugador is null and c.tipus = :t
                """, CartaDesenvolupament.class)
                .setParameter("p", j.getPartida().getId()).setParameter("t", tipus)
                .setMaxResults(1).getSingleResult();
        c.setJugador(j);
        c.setRondaCompra(0);
        s.getTransaction().commit();
    }

    private static int idCarta(int idJugador, TipusCarta tipus, int posicio) {
        Session s = SessionManager.getSessionFactory().getCurrentSession();
        s.beginTransaction();
        List<Integer> ids = s.createQuery("""
                select c.id from CartaDesenvolupament c
                where c.jugador.id = :j and c.tipus = :t and c.rondaCompra = 0 order by c.id
                """, Integer.class).setParameter("j", idJugador).setParameter("t", tipus).getResultList();
        s.getTransaction().commit();
        return ids.get(posicio);
    }

    private static List<Integer> consulta(String hql, String param, Object valor) {
        Session s = SessionManager.getSessionFactory().getCurrentSession();
        s.beginTransaction();
        List<Integer> r = s.createQuery(hql, Integer.class).setParameter(param, valor).getResultList();
        s.getTransaction().commit();
        return r;
    }

    private static int aresta(int idVertex) {
        return consulta("""
                select a.id from Aresta a
                where (a.vertexA.id = :v or a.vertexB.id = :v)
                  and not exists (select c.id from Carretera c where c.aresta.id = a.id)
                order by a.codi
                """, "v", idVertex).get(0);
    }

    private static int aresteConnectada(int idJugador) {
        return consulta("""
                select a.id from Aresta a
                where not exists (select c.id from Carretera c where c.aresta.id = a.id)
                  and exists (select c2.id from Carretera c2
                              where c2.propietari.id = :j
                                and (c2.aresta.vertexA.id in (a.vertexA.id, a.vertexB.id)
                                  or c2.aresta.vertexB.id in (a.vertexA.id, a.vertexB.id)))
                order by a.codi
                """, "j", idJugador).get(0);
    }

    private static int vertexAmbPoblat(int idJugador) {
        return consulta("select p.vertex.id from Poblat p where p.propietari.id = :j order by p.id", "j", idJugador).get(0);
    }

    private static int lladre(int idPartida) {
        return partidaDao.get(idPartida).getId();
    }

    private static int hexagonBuit(int idPartida) {
        int idLladre = lladre(idPartida);
        Session s = SessionManager.getSessionFactory().getCurrentSession();
        s.beginTransaction();
        Integer id = s.createQuery("""
                select h.id from Hexagon h
                where h.partida.id = :p and h.id <> :lladre
                  and not exists (select a.id from Assentament a
                                  where a.vertex.id in (select hv.vertex.id from HexagonVertex hv where hv.hexagon.id = h.id))
                order by h.codi
                """, Integer.class).setParameter("p", idPartida).setParameter("lladre", idLladre)
                .setMaxResults(1).getSingleResult();
        s.getTransaction().commit();
        return id;
    }

    private static int hexagonAmbVictima(int idPartida, int idJugador) {
        int idLladre = lladre(idPartida);
        Session s = SessionManager.getSessionFactory().getCurrentSession();
        s.beginTransaction();
        Integer id = s.createQuery("""
                select hv.hexagon.id from Assentament a, HexagonVertex hv
                where hv.vertex.id = a.vertex.id and a.propietari.id <> :j
                  and hv.hexagon.partida.id = :p and hv.hexagon.id <> :lladre
                order by hv.hexagon.codi
                """, Integer.class).setParameter("p", idPartida).setParameter("j", idJugador)
                .setParameter("lladre", idLladre).setMaxResults(1).getSingleResult();
        s.getTransaction().commit();
        return id;
    }
}
