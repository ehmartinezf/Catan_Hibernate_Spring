package dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.hibernate.Session;

import entities.Aresta;
import entities.CartaDesenvolupament;
import entities.Carretera;
import entities.Ciutat;
import entities.Hexagon;
import entities.HexagonVertex;
import entities.Jugador;
import entities.Partida;
import entities.Poblat;
import entities.Vertex;
import enums.EstatPartida;
import enums.TipusCarta;
import enums.TipusRecurs;
import enums.TipusTerreny;
import util.Costos;

/*
 * DAO de les accions d'un jugador. Mateix patró que PartidaDao:
 * mètode públic = transacció; mètode privat = feina, rebent la sessió.
 * Les comprovacions es fan ABANS de modificar res (regla de la teoria): així, si una regla falla,
 * no s'ha tocat cap dada i no cal desfer res.
 */
public class JugadorDao extends GenericDao<Jugador, Integer> implements IJugadorDao {

    private final Random random = new Random();

    // ============================ MOSTRAR RECURSOS ============================

    @Override
    public String mostrarRecursos(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = mostrarRecursos(session, idJugador);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String mostrarRecursos(Session session, int idJugador) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Missatges.JUGADOR_NO_EXISTEIX;
        StringBuilder sb = new StringBuilder("Recursos de " + j.getNom() + ": ");
        for (TipusRecurs t : TipusRecurs.values()) {
            sb.append(t).append("=").append(j.getQuantitat(t)).append(" ");
        }
        return sb.toString().trim();
    }

    // ============================ CONSTRUIR CARRETERA ============================

    @Override
    public String construirCarretera(int idJugador, int idAresta) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = construirCarretera(session, idJugador, idAresta);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String construirCarretera(Session session, int idJugador, int idAresta) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Missatges.JUGADOR_NO_EXISTEIX;
        String error = ReglesJoc.validarAccio(j, true);
        if (error != null) return error;

        Aresta a = session.find(Aresta.class, idAresta);
        if (a == null) return Missatges.ARESTA_NO_EXISTEIX;
        error = ReglesJoc.comprovarCarretera(session, j, a);
        if (error != null) return error;

        // Tot comprovat: ara sí, modifiquem
        if (j.getPartida().getEstat() != EstatPartida.PREPARACIO) {
            j.pagar(Costos.CARRETERA); // entitat gestionada: dirty checking, no cal merge
        }
        session.persist(new Carretera(j, a));
        return Missatges.CARRETERA_CONSTRUIDA;
    }

    // ============================ CONSTRUIR POBLAT ============================

    @Override
    public String construirPoblat(int idJugador, int idVertex) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = construirPoblat(session, idJugador, idVertex);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String construirPoblat(Session session, int idJugador, int idVertex) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Missatges.JUGADOR_NO_EXISTEIX;
        String error = ReglesJoc.validarAccio(j, true);
        if (error != null) return error;

        Vertex v = session.find(Vertex.class, idVertex);
        if (v == null) return Missatges.VERTEX_NO_EXISTEIX;
        error = ReglesJoc.comprovarPoblat(session, j, v);
        if (error != null) return error;

        Partida p = j.getPartida();
        boolean preparacio = p.getEstat() == EstatPartida.PREPARACIO;
        long poblatsAbans = ReglesJoc.comptar(session, j.getId(), Poblat.class);

        if (!preparacio) {
            j.pagar(Costos.POBLAT);
        }
        session.persist(new Poblat(j, v));

        // Segon poblat de la PREPARACIO: recursos inicials (1 per cada hexàgon adjacent que no sigui desert)
        if (preparacio && poblatsAbans == 1) {
            for (HexagonVertex hv : session.createNamedQuery("HexagonVertex.perVertex", HexagonVertex.class)
                    .setParameter("idVertex", v.getId()).getResultList()) {
                if (hv.getHexagon().getTerreny() != TipusTerreny.DESERT) {
                    j.afegirRecurs(hv.getHexagon().getTerreny().getRecurs(), 1);
                }
            }
        }
        String victoria = ReglesJoc.comprovarVictoria(session, p);
        return Missatges.POBLAT_CONSTRUIT + (victoria != null ? "\n" + victoria : "");
    }

    // ============================ MILLORAR A CIUTAT ============================

    @Override
    public String millorarACiutat(int idJugador, int idVertex) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = millorarACiutat(session, idJugador, idVertex);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String millorarACiutat(Session session, int idJugador, int idVertex) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Missatges.JUGADOR_NO_EXISTEIX;
        String error = ReglesJoc.validarAccio(j, false);
        if (error != null) return error;

        Vertex v = session.find(Vertex.class, idVertex);
        if (v == null) return Missatges.VERTEX_NO_EXISTEIX;
        if (!v.getPartida().getId().equals(j.getPartida().getId())) return Missatges.ALTRA_PARTIDA;

        List<Poblat> poblats = session.createNamedQuery("Poblat.delJugadorAlVertex", Poblat.class)
                .setParameter("idJugador", j.getId())
                .setParameter("idVertex", v.getId())
                .getResultList();
        if (poblats.isEmpty()) return Missatges.NO_HI_HA_POBLAT;
        error = ReglesJoc.comprovarCiutat(session, j);
        if (error != null) return error;

        j.pagar(Costos.CIUTAT);
        session.remove(poblats.get(0));
        // IMPORTANT: Hibernate fa els INSERT abans dels DELETE al flush. Com que assentament.vertex_id és UNIQUE,
        // cal esborrar el poblat a la BD ABANS d'inserir la ciutat: per això el flush() explícit.
        session.flush();
        session.persist(new Ciutat(j, v));

        String victoria = ReglesJoc.comprovarVictoria(session, j.getPartida());
        return Missatges.CIUTAT_CONSTRUIDA + (victoria != null ? "\n" + victoria : "");
    }

    // ============================ COMERCIAR AMB LA BANCA ============================

    @Override
    public String comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = comerciarBanca(session, idJugador, dona, rep);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String comerciarBanca(Session session, int idJugador, TipusRecurs dona, TipusRecurs rep) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Missatges.JUGADOR_NO_EXISTEIX;
        String error = ReglesJoc.validarAccio(j, false);
        if (error != null) return error;
        if (dona == null || rep == null || dona == rep) return Missatges.COMERC_IGUALS;
        if (j.getQuantitat(dona) < Costos.COMERC_BANCA) return Missatges.SENSE_RECURSOS;

        j.treureRecurs(dona, Costos.COMERC_BANCA);
        j.afegirRecurs(rep, 1);
        return Missatges.COMERC_FET + " (" + dona + " -> " + rep + ")";
    }

    // ============================ CALCULAR PUNTS ============================

    @Override
    public int calcularPunts(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.find(Jugador.class, idJugador);
            int punts = j == null ? -1 : ReglesJoc.calcularPunts(session, j);
            session.getTransaction().commit();
            return punts;
        } catch (RuntimeException e) {
            handleException(session, e);
            return -1;
        }
    }

    // ============================ PASSAR TORN ============================

    @Override
    public String passarTorn(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = passarTorn(session, idJugador);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String passarTorn(Session session, int idJugador) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Missatges.JUGADOR_NO_EXISTEIX;
        Partida p = j.getPartida();
        if (p.getEstat() == EstatPartida.FINALITZADA) return Missatges.PARTIDA_FINALITZADA;
        if (p.getActiu() == null || !p.getActiu().getId().equals(j.getId())) return Missatges.NO_ES_EL_TEU_TORN;

        List<Jugador> jugadors = session.createNamedQuery("Jugador.perPartida", Jugador.class)
                .setParameter("idPartida", p.getId()).getResultList();
        int n = jugadors.size();
        int ordre = j.getOrdreTorn();

        if (p.getEstat() == EstatPartida.PREPARACIO) {
            // En la ronda r el jugador ha de tenir r poblats i r carreteres
            long poblats = ReglesJoc.comptar(session, j.getId(), Poblat.class);
            long carreteres = ReglesJoc.comptar(session, j.getId(), Carretera.class);
            if (poblats < p.getRonda() || carreteres < p.getRonda()) return Missatges.PREP_CAL_COMPLETAR;

            if (p.getRonda() == 1) {
                if (ordre < n) {
                    p.setActiu(jugadors.get(ordre)); // següent jugador (la llista és 0-based)
                } else {
                    p.setRonda(2); // l'últim juga dos cops seguits: l'ordre de serp (1,2,3,4,4,3,2,1)
                }
                return Missatges.TORN_PASSAT;
            }
            if (ordre > 1) {
                p.setActiu(jugadors.get(ordre - 2)); // ronda 2: el jugador anterior
                return Missatges.TORN_PASSAT;
            }
            // Ha acabat el jugador d'ordre 1 a la ronda 2: tots haurien de tenir 2 poblats i 2 carreteres
            for (Jugador altre : jugadors) {
                if (ReglesJoc.comptar(session, altre.getId(), Poblat.class) < 2
                        || ReglesJoc.comptar(session, altre.getId(), Carretera.class) < 2) {
                    return Missatges.PREP_CAL_COMPLETAR;
                }
            }
            p.setEstat(EstatPartida.EN_CURS);
            p.setRonda(1);
            p.setActiu(jugadors.get(0));
            p.setDausTirats(false);
            return Missatges.PREPARACIO_ACABADA;
        }

        // EN_CURS: cal haver tirat els daus i no tenir el lladre pendent
        if (!p.isDausTirats()) return Missatges.CAL_TIRAR_DAUS;
        if (p.isLladrePendent()) return Missatges.LLADRE_PENDENT;

        if (ordre == n) {
            p.setRonda(p.getRonda() + 1); // la ronda s'incrementa quan el torn torna al jugador d'ordre 1
        }
        p.setActiu(jugadors.get(ordre % n)); // el següent (o el primer si ordre == n)
        p.setDausTirats(false);
        return Missatges.TORN_PASSAT;
    }

    // ============================ MOURE LLADRE (avançat) ============================

    @Override
    public String moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = moureLladre(session, idJugador, idHexagon, idJugadorVictima);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String moureLladre(Session session, int idJugador, int idHexagon, Integer idVictima) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Missatges.JUGADOR_NO_EXISTEIX;
        Partida p = j.getPartida();
        if (p.getEstat() == EstatPartida.FINALITZADA) return Missatges.PARTIDA_FINALITZADA;
        if (p.getActiu() == null || !p.getActiu().getId().equals(j.getId())) return Missatges.NO_ES_EL_TEU_TORN;
        if (!p.isLladrePendent()) return Missatges.LLADRE_NO_PENDENT;

        Hexagon h = session.find(Hexagon.class, idHexagon);
        if (h == null) return Missatges.HEXAGON_NO_EXISTEIX;
        if (!h.getPartida().getId().equals(p.getId())) return Missatges.ALTRA_PARTIDA;
        if (p.getLladre() != null && p.getLladre().getId().equals(h.getId())) return Missatges.LLADRE_MATEIX_HEXAGON;

        // Possibles víctimes: altres jugadors amb un assentament en aquest hexàgon i amb recursos
        List<Jugador> candidats = new ArrayList<>();
        for (Jugador c : session.createNamedQuery("Jugador.ambAssentamentAlHexagon", Jugador.class)
                .setParameter("idHexagon", h.getId()).getResultList()) {
            if (!c.getId().equals(j.getId()) && c.getTotalRecursos() > 0) {
                candidats.add(c);
            }
        }
        Jugador victima = null;
        if (idVictima != null) {
            for (Jugador c : candidats) {
                if (c.getId().equals(idVictima)) victima = c;
            }
            if (victima == null) return Missatges.VICTIMA_INVALIDA;
        } else if (!candidats.isEmpty()) {
            return Missatges.VICTIMA_OBLIGATORIA;
        }

        // Tot validat: movem el lladre i, si cal, robem 1 recurs aleatori
        p.setLladre(h);
        p.setLladrePendent(false);
        if (victima == null) {
            return "Lladre mogut a l'hexàgon " + h.getCodi() + ". No es roba res";
        }
        List<TipusRecurs> bossa = new ArrayList<>();
        for (TipusRecurs t : TipusRecurs.values()) {
            for (int i = 0; i < victima.getQuantitat(t); i++) bossa.add(t);
        }
        TipusRecurs robat = bossa.get(random.nextInt(bossa.size()));
        victima.treureRecurs(robat, 1);
        j.afegirRecurs(robat, 1);
        return "Lladre mogut a l'hexàgon " + h.getCodi() + ". " + j.getNom() + " roba 1 " + robat + " a " + victima.getNom();
    }

    // ============================ COMPRAR CARTA (avançat) ============================

    @Override
    public String comprarCarta(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = comprarCarta(session, idJugador);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String comprarCarta(Session session, int idJugador) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Missatges.JUGADOR_NO_EXISTEIX;
        String error = ReglesJoc.validarAccio(j, false);
        if (error != null) return error;
        if (!j.potPagar(Costos.CARTA)) return Missatges.SENSE_RECURSOS;

        Partida p = j.getPartida();
        List<CartaDesenvolupament> pila = session.createNamedQuery("Carta.pila", CartaDesenvolupament.class)
                .setParameter("idPartida", p.getId()).getResultList();
        if (pila.isEmpty()) return Missatges.PILA_BUIDA;

        CartaDesenvolupament carta = pila.get(random.nextInt(pila.size())); // s'agafa a l'atzar
        j.pagar(Costos.CARTA);
        carta.setJugador(j); // surt de la pila (jugador = null) i passa a la mà del jugador
        carta.setRondaCompra(p.getRonda());

        String victoria = ReglesJoc.comprovarVictoria(session, p); // una carta PUNT_VICTORIA pot donar la victòria
        return "Has comprat una carta: " + carta.getTipus() + (victoria != null ? "\n" + victoria : "");
    }

    // ============================ JUGAR CARTA (avançat) ============================

    @Override
    public String jugarCarta(int idJugador, int idCarta, TipusRecurs recurs) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = jugarCarta(session, idJugador, idCarta, recurs);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String jugarCarta(Session session, int idJugador, int idCarta, TipusRecurs recurs) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Missatges.JUGADOR_NO_EXISTEIX;
        String error = ReglesJoc.validarAccio(j, false);
        if (error != null) return error;

        CartaDesenvolupament c = session.find(CartaDesenvolupament.class, idCarta);
        if (c == null) return Missatges.CARTA_NO_EXISTEIX;
        if (c.getJugador() == null || !c.getJugador().getId().equals(j.getId())) return Missatges.CARTA_NO_ES_TEVA;
        if (c.isUsada()) return Missatges.CARTA_JA_USADA;
        Partida p = j.getPartida();
        if (c.getRondaCompra() != null && c.getRondaCompra() >= p.getRonda()) return Missatges.CARTA_MATEIXA_RONDA;
        if (c.getTipus() == TipusCarta.PUNT_VICTORIA) return Missatges.CARTA_PUNT_VICTORIA;
        if (c.getTipus() == TipusCarta.MONOPOLI && recurs == null) return Missatges.CARTA_MONOPOLI_RECURS;

        c.setUsada(true);
        if (c.getTipus() == TipusCarta.CAVALLER) {
            j.setCavallersJugats(j.getCavallersJugats() + 1);
            p.setLladrePendent(true); // ara el jugador ha de cridar moureLladre (sense descart)
            String victoria = ReglesJoc.comprovarVictoria(session, p); // pot haver conquerit l'exèrcit més gran
            return "Cavaller jugat: cal moure el lladre" + (victoria != null ? "\n" + victoria : "");
        }

        // MONOPOLI: tots els altres jugadors donen tots els recursos del tipus indicat
        int total = 0;
        List<Jugador> jugadors = session.createNamedQuery("Jugador.perPartida", Jugador.class)
                .setParameter("idPartida", p.getId()).getResultList();
        for (Jugador altre : jugadors) {
            if (!altre.getId().equals(j.getId())) {
                total += altre.treureRecurs(recurs, altre.getQuantitat(recurs));
            }
        }
        j.afegirRecurs(recurs, total);
        return "Monopoli de " + recurs + ": has rebut " + total + " recursos";
    }

    // ============================ VÈRTEXS DISPONIBLES (avançat) ============================

    @Override
    public List<Vertex> vertexsDisponibles(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            List<Vertex> vertexs = vertexsDisponibles(session, idJugador);
            session.getTransaction().commit();
            return vertexs;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Collections.emptyList();
        }
    }

    private List<Vertex> vertexsDisponibles(Session session, int idJugador) {
        Jugador j = session.find(Jugador.class, idJugador);
        if (j == null) return Collections.emptyList();
        Partida p = j.getPartida();
        // Tot es resol amb JPQL (consultes amb NOT EXISTS), sense recórrer els 54 vèrtexs a Java
        if (p.getEstat() == EstatPartida.PREPARACIO) {
            return session.createNamedQuery("Vertex.disponiblesPreparacio", Vertex.class)
                    .setParameter("idPartida", p.getId())
                    .getResultList();
        }
        return session.createNamedQuery("Vertex.disponibles", Vertex.class)
                .setParameter("idPartida", p.getId())
                .setParameter("idJugador", j.getId())
                .getResultList();
    }
}
