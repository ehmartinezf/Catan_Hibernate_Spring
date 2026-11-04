package dao;

import entities.Aresta;
import entities.Assentament;
import entities.CartaDesenvolupament;
import entities.Carretera;
import entities.Ciutat;
import entities.Hexagon;
import entities.HexagonVertex;
import entities.Jugador;
import entities.Partida;
import entities.Poblat;
import entities.RecursJugador;
import entities.Vertex;
import enums.EstatPartida;
import enums.TipusCarta;
import enums.TipusRecurs;
import enums.TipusTerreny;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.hibernate.Session;

public class JugadorDao extends GenericDao<Jugador, Integer> implements IJugadorDao {

    private static final int MAX_CARRETERES = 15;
    private static final int MAX_POBLATS = 5;
    private static final int MAX_CIUTATS = 4;
    private static final Random RANDOM = new Random();

    @Override
    public void mostrarRecursos(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);

            if (j == null) {
                System.out.println(Missatges.JUGADOR_NO_EXISTEIX);
                session.getTransaction().rollback();
                return;
            }

            System.out.println("Recursos de " + j.getNom() + ":");
            List<RecursJugador> recursos = session.createNamedQuery("RecursJugador.perJugador", RecursJugador.class).setParameter("jugador", j).getResultList();

            for (int i = 0; i < recursos.size(); i++) {
                RecursJugador r = recursos.get(i);
                System.out.println("  " + r.getTipusRecurs() + ": " + r.getQuantitat());
            }
            session.getTransaction().commit();

        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    @Override
    public String construirCarretera(int idJugador, int idAresta) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            String error = comprovarTorn(j, true);
            if (error != null) {
                return cancelar(session, error);
            }
            Partida p = j.getPartida();
            boolean preparacio = p.getEstat() == EstatPartida.PREPARACIO;

            Aresta a = session.get(Aresta.class, idAresta);
            if (a == null || a.getPartida().getId() != p.getId()) {
                return cancelar(session, Missatges.ALTRA_PARTIDA);
            }

            int ocupada = session.createQuery("select count(c) from Carretera c where c.aresta = :a", Long.class).setParameter("a", a).uniqueResult().intValue();
            if (ocupada > 0) {
                return cancelar(session, Missatges.ARESTA_OCUPADA);
            }

            if (preparacio) {
                int carreteres = numCarreteres(session, j);
                int poblats = numPoblats(session, j);
                if (carreteres >= poblats) {
                    return cancelar(session, Missatges.PREP_POBLAT_PRIMER);
                }
                if (carreteres >= p.getRonda()) {
                    return cancelar(session, Missatges.PREP_CARRETERA_JA_COLOCADA);
                }

                boolean toca = false;
                Vertex[] extrems = {a.getVertexA(), a.getVertexB()};
                for (int i = 0; i < extrems.length; i++) {
                    Vertex v = extrems[i];
                    Assentament as = assentamentAl(session, v);
                    if (as != null && as.getPropietari().getId() == j.getId()
                            && carreteresPropiesAlVertex(session, j, v) == 0) {
                        toca = true;
                    }
                }
                if (!toca) {
                    return cancelar(session, Missatges.PREP_CARRETERA_POBLAT);
                }
            } else {
                int construccions = session.createQuery("select count(x) from Assentament x where x.propietari = :j and (x.vertex = :va or x.vertex = :vb)", Long.class).setParameter("j", j).setParameter("va", a.getVertexA()).setParameter("vb", a.getVertexB()).uniqueResult().intValue();
                int carreteres = carreteresPropiesAlVertex(session, j, a.getVertexA()) + carreteresPropiesAlVertex(session, j, a.getVertexB());
                if (construccions + carreteres == 0) {
                    return cancelar(session, Missatges.NO_CONNECTAT);
                }
                if (numCarreteres(session, j) >= MAX_CARRETERES) {
                    return cancelar(session, Missatges.LIMIT_CARRETERES + MAX_CARRETERES + " carreteres");
                }
                error = comprovarRecursos(session, j, 1, 1, 0, 0, 0);
                if (error != null) {
                    return cancelar(session, error);
                }
                descomptarRecursos(session, j, 1, 1, 0, 0, 0);
            }

            Carretera c = new Carretera();
            c.setPropietari(j);
            c.setAresta(a);
            session.persist(c);
            session.getTransaction().commit();
            return "Carretera construida";
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_INTERN + e.getMessage();
        }
    }

    @Override
    public String construirPoblat(int idJugador, int idVertex) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            String error = comprovarTorn(j, true);
            if (error != null) {
                return cancelar(session, error);
            }
            Partida p = j.getPartida();
            boolean preparacio = p.getEstat() == EstatPartida.PREPARACIO;

            Vertex v = session.get(Vertex.class, idVertex);
            if (v == null || v.getPartida().getId() != p.getId()) {
                return cancelar(session, Missatges.ALTRA_PARTIDA);
            }

            if (assentamentAl(session, v) != null) {
                return cancelar(session, Missatges.VERTEX_OCUPAT);
            }

            int veins = session.createQuery("select count(a) from Assentament a where a.vertex in (select ar.vertexB from Aresta ar where ar.vertexA = :v) or a.vertex in (select ar.vertexA from Aresta ar where ar.vertexB = :v)", Long.class).setParameter("v", v).uniqueResult().intValue();

            if (veins > 0) {
                return cancelar(session, Missatges.REGLA_DISTANCIA);
            }

            int poblats = numPoblats(session, j);
            if (preparacio) {
                if (poblats >= p.getRonda()) {
                    return cancelar(session, Missatges.PREP_POBLAT_JA_COLOCAT);
                }
                if (poblats > numCarreteres(session, j)) {
                    return cancelar(session, Missatges.PREP_CARRETERA_PRIMER);
                }
            } else {
                if (carreteresPropiesAlVertex(session, j, v) == 0) {
                    return cancelar(session, Missatges.NO_CONNECTAT);
                }
                if (poblats >= MAX_POBLATS) {
                    return cancelar(session, Missatges.LIMIT_POBLATS + MAX_POBLATS + " poblats");
                }
                error = comprovarRecursos(session, j, 1, 1, 1, 1, 0);
                if (error != null) {
                    return cancelar(session, error);
                }
                descomptarRecursos(session, j, 1, 1, 1, 1, 0);
            }

            Poblat poblat = new Poblat();
            poblat.setPropietari(j);
            poblat.setVertex(v);
            session.persist(poblat);

            String missatge = "Poblat construit.";

            if (preparacio && poblats + 1 == 2) {
                List<HexagonVertex> hvs = session.createQuery("select hv from HexagonVertex hv where hv.vertex = :v", HexagonVertex.class).setParameter("v", v).getResultList();
                missatge += " Recursos inicials:";
                for (int i = 0; i < hvs.size(); i++) {
                    HexagonVertex hv = hvs.get(i);
                    TipusRecurs t = recursDelTerreny(hv.getHexagon().getTerreny());
                    if (t != null) {
                        afegirRecurs(session, j, t, 1);
                        missatge += " 1 " + t;
                    }
                }
            }

            comprovarVictoria(session, p);
            session.getTransaction().commit();
            return missatge;

        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_INTERN + e.getMessage();
        }
    }

    @Override
    public String millorarACiutat(int idJugador, int idVertex) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            String error = comprovarTorn(j, true);
            if (error != null) {
                return cancelar(session, error);
            }
            Partida p = j.getPartida();
            if (p.getEstat() != EstatPartida.EN_CURS) {
                return cancelar(session, Missatges.NOMES_EN_CURS);
            }

            Vertex v = session.get(Vertex.class, idVertex);
            if (v == null || v.getPartida().getId() != p.getId()) {
                return cancelar(session, Missatges.ALTRA_PARTIDA);
            }

            Poblat poblat = session.createQuery("select x from Poblat x where x.vertex = :v and x.propietari = :j", Poblat.class).setParameter("v", v).setParameter("j", j).uniqueResult();
            if (poblat == null) {
                return cancelar(session, Missatges.NO_HI_HA_POBLAT);
            }

            if (numCiutats(session, j) >= MAX_CIUTATS) {
                return cancelar(session, Missatges.LIMIT_CIUTATS + MAX_CIUTATS + " ciutats");
            }
            error = comprovarRecursos(session, j, 0, 0, 0, 2, 3);
            if (error != null) {
                return cancelar(session, error);
            }
            descomptarRecursos(session, j, 0, 0, 0, 2, 3);

            session.remove(poblat);
            session.flush();
            Ciutat ciutat = new Ciutat();
            ciutat.setPropietari(j);
            ciutat.setVertex(v);
            session.persist(ciutat);

            comprovarVictoria(session, p);
            session.getTransaction().commit();
            return "Poblat millorat a ciutat";

        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_INTERN + e.getMessage();
        }
    }

    @Override
    public String comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            String error = comprovarTorn(j, true);
            if (error != null) {
                return cancelar(session, error);
            }
            if (j.getPartida().getEstat() != EstatPartida.EN_CURS) {
                return cancelar(session, Missatges.NOMES_EN_CURS);
            }
            if (dona == null || rep == null) {
                return cancelar(session, Missatges.CAL_DOS_TIPUS_RECURS);
            }
            if (dona == rep) {
                return cancelar(session, Missatges.COMERC_IGUALS);
            }

            RecursJugador rDona = recurs(session, j, dona);
            if (rDona.getQuantitat() < 4) {
                return cancelar(session, Missatges.SENSE_RECURSOS + "4 " + dona);
            }
            rDona.setQuantitat(rDona.getQuantitat() - 4);
            afegirRecurs(session, j, rep, 1);
            session.getTransaction().commit();
            return "Comerç fet: 4 " + dona + " per 1 " + rep;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_INTERN + e.getMessage();
        }
    }

    @Override
    public int calcularPunts(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            int punts = 0;
            if (j != null) {
                punts = punts(session, j);
            }
            session.getTransaction().commit();
            return punts;
        } catch (RuntimeException e) {
            handleException(session, e);
            return 0;
        }
    }

    @Override
    public String passarTorn(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            String error = comprovarTorn(j, true);
            if (error != null) {
                return cancelar(session, error);
            }
            Partida p = j.getPartida();

            List<Jugador> ordre = session.createQuery("select x from Jugador x where x.partida = :p order by x.ordreTorn", Jugador.class).setParameter("p", p).getResultList();
            int n = ordre.size();
            int pos = ordre.indexOf(j);

            if (p.getEstat() == EstatPartida.PREPARACIO) {
                if (numPoblats(session, j) < p.getRonda() || numCarreteres(session, j) < p.getRonda()) {
                    return cancelar(session, Missatges.PREP_CAL_COMPLETAR);
                }
                if (p.getRonda() == 1) {
                    if (pos < n - 1) {
                        p.setJugadorActiu(ordre.get(pos + 1));
                    } else {
                        p.setRonda(2);
                    }
                } else {
                    if (pos > 0) {
                        p.setJugadorActiu(ordre.get(pos - 1));
                    } else {
                        p.setEstat(EstatPartida.EN_CURS);
                        p.setRonda(1);
                        p.setJugadorActiu(ordre.get(0));
                    }
                }
            } else {
                if (pos < n - 1) {
                    p.setJugadorActiu(ordre.get(pos + 1));
                } else {
                    p.setJugadorActiu(ordre.get(0));
                    p.setRonda(p.getRonda() + 1);
                }
            }
            p.setDausTirats(false);

            session.getTransaction().commit();
            return "Torn passat. Ara juga " + p.getJugadorActiu().getNom();
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_INTERN + e.getMessage();
        }
    }

    @Override
    public String moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            String error = comprovarTorn(j, false);
            if (error != null) {
                return cancelar(session, error);
            }
            Partida p = j.getPartida();
            if (p.getEstat() != EstatPartida.EN_CURS) {
                return cancelar(session, Missatges.NOMES_EN_CURS);
            }
            if (!p.isLladrePendent()) {
                return cancelar(session, Missatges.LLADRE_NO_PENDENT);
            }

            Hexagon h = session.get(Hexagon.class, idHexagon);
            if (h == null || h.getPartida().getId() != p.getId()) {
                return cancelar(session, Missatges.ALTRA_PARTIDA);
            }
            if (p.getHexagonLladre() != null && p.getHexagonLladre().getId() == h.getId()) {
                return cancelar(session, Missatges.LLADRE_MATEIX_HEXAGON);
            }

            List<Jugador> candidats = session.createQuery("select distinct a.propietari from Assentament a, HexagonVertex hv where hv.vertex = a.vertex and hv.hexagon = :h and a.propietari <> :j", Jugador.class).setParameter("h", h).setParameter("j", j).getResultList();

            String missatge = "Lladre mogut.";
            if (idJugadorVictima != null) {
                Jugador victima = null;
                for (int i = 0; i < candidats.size(); i++) {
                    if (candidats.get(i).getId() == idJugadorVictima) {
                        victima = candidats.get(i);
                    }
                }
                if (victima == null) {
                    return cancelar(session, Missatges.VICTIMA_INVALIDA);
                }
                if (totalRecursos(session, victima) == 0) {
                    return cancelar(session, Missatges.VICTIMA_INVALIDA);
                }
                TipusRecurs robat = robarRecursAleatori(session, victima, j);
                missatge += " Has robat 1 " + robat + " a " + victima.getNom();
            } else {
                for (int i = 0; i < candidats.size(); i++) {
                    if (totalRecursos(session, candidats.get(i)) > 0) {
                        return cancelar(session, Missatges.VICTIMA_OBLIGATORIA);
                    }
                }
            }

            p.setHexagonLladre(h);
            p.setLladrePendent(false);

            session.getTransaction().commit();
            return missatge;

        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_INTERN + e.getMessage();
        }
    }

    @Override
    public String comprarCarta(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            String error = comprovarTorn(j, true);
            if (error != null) {
                return cancelar(session, error);
            }
            Partida p = j.getPartida();
            if (p.getEstat() != EstatPartida.EN_CURS) {
                return cancelar(session, Missatges.NOMES_EN_CURS);
            }

            List<CartaDesenvolupament> pila = session.createQuery("select c from CartaDesenvolupament c where c.partida = :p and c.jugador is null", CartaDesenvolupament.class).setParameter("p", p).getResultList();
            if (pila.isEmpty()) {
                return cancelar(session, Missatges.PILA_BUIDA);
            }

            error = comprovarRecursos(session, j, 0, 0, 1, 1, 1);
            if (error != null) {
                return cancelar(session, error);
            }
            descomptarRecursos(session, j, 0, 0, 1, 1, 1);

            CartaDesenvolupament carta = pila.get(RANDOM.nextInt(pila.size()));
            carta.setJugador(j);
            carta.setRondaCompra(p.getRonda());

            comprovarVictoria(session, p);
            session.getTransaction().commit();
            return "Has comprat una carta: " + carta.getTipus();
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_INTERN + e.getMessage();
        }
    }

    @Override
    public String jugarCarta(int idJugador, int idCarta, TipusRecurs recurs) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            String error = comprovarTorn(j, true);
            if (error != null) {
                return cancelar(session, error);
            }
            Partida p = j.getPartida();
            if (p.getEstat() != EstatPartida.EN_CURS) {
                return cancelar(session, Missatges.NOMES_EN_CURS);
            }

            CartaDesenvolupament carta = session.get(CartaDesenvolupament.class, idCarta);
            if (carta == null || carta.getJugador() == null || carta.getJugador().getId() != j.getId()) {
                return cancelar(session, Missatges.CARTA_NO_ES_TEVA);
            }
            if (carta.isUsada()) {
                return cancelar(session, Missatges.CARTA_JA_USADA);
            }
            if (carta.getRondaCompra() == null || carta.getRondaCompra() >= p.getRonda()) {
                return cancelar(session, Missatges.CARTA_MATEIXA_RONDA);
            }

            String missatge;
            if (carta.getTipus() == TipusCarta.CAVALLER) {
                j.setCavallersJugats(j.getCavallersJugats() + 1);
                p.setLladrePendent(true);
                missatge = "Cavaller jugat: ara has de moure el lladre";

            } else if (carta.getTipus() == TipusCarta.MONOPOLI) {
                if (recurs == null) {
                    return cancelar(session, Missatges.CARTA_MONOPOLI_RECURS);
                }
                List<RecursJugador> altres = session.createQuery("select r from RecursJugador r where r.jugador.partida = :p and r.jugador <> :j and r.tipus = :t", RecursJugador.class).setParameter("p", p).setParameter("j", j).setParameter("t", recurs).getResultList();

                int total = 0;
                for (int i = 0; i < altres.size(); i++) {
                    total += altres.get(i).getQuantitat();
                    altres.get(i).setQuantitat(0);
                }
                afegirRecurs(session, j, recurs, total);
                missatge = "Monopoli: has rebut " + total + " " + recurs;

            } else {
                return cancelar(session, Missatges.CARTA_PUNT_VICTORIA);
            }

            carta.setUsada(true);
            comprovarVictoria(session, p);
            session.getTransaction().commit();
            return missatge;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_INTERN + e.getMessage();
        }
    }

    @Override
    public List<Vertex> vertexsDisponibles(int idJugador) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Jugador j = session.get(Jugador.class, idJugador);
            if (j == null) {
                session.getTransaction().rollback();
                return new ArrayList<>();
            }
            Partida p = j.getPartida();
            boolean preparacio = p.getEstat() == EstatPartida.PREPARACIO;

            String hql = "select v from Vertex v where v.partida = :p and not exists (select a from Assentament a where a.vertex = v) and not exists (select a2 from Assentament a2, Aresta ar where (ar.vertexA = v and a2.vertex = ar.vertexB) or (ar.vertexB = v and a2.vertex = ar.vertexA)) ";

            List<Vertex> vertexs;
            if (preparacio) {
                hql += "order by v.codi";
                vertexs = session.createQuery(hql, Vertex.class).setParameter("p", p).getResultList();
            } else {
                hql += "and exists (select c from Carretera c where c.propietari = :j and (c.aresta.vertexA = v or c.aresta.vertexB = v)) order by v.codi";
                vertexs = session.createQuery(hql, Vertex.class).setParameter("p", p).setParameter("j", j).getResultList();
            }

            session.getTransaction().commit();
            return vertexs;

        } catch (RuntimeException e) {
            handleException(session, e);
            return new ArrayList<>();
        }
    }

    private String cancelar(Session session, String missatge) {
        session.getTransaction().rollback();
        return Missatges.ERROR + missatge;
    }

    private String comprovarTorn(Jugador j, boolean comprovarDausILladre) {
        if (j == null) {
            return Missatges.JUGADOR_NO_EXISTEIX;
        }
        Partida p = j.getPartida();
        if (p.getEstat() == EstatPartida.FINALITZADA) {
            return Missatges.PARTIDA_FINALITZADA;
        }
        if (p.getJugadorActiu() == null || p.getJugadorActiu().getId() != j.getId()) {
            return Missatges.NO_ES_EL_TEU_TORN;
        }
        if (comprovarDausILladre && p.getEstat() == EstatPartida.EN_CURS) {
            if (!p.isDausTirats()) {
                return Missatges.CAL_TIRAR_DAUS;
            }
            if (p.isLladrePendent()) {
                return Missatges.LLADRE_PENDENT;
            }
        }
        return null;
    }

    private int numPoblats(Session session, Jugador j) {
        return session.createQuery("select count(p) from Poblat p where p.propietari = :j", Long.class).setParameter("j", j).uniqueResult().intValue();
    }

    private int numCiutats(Session session, Jugador j) {
        return session.createQuery("select count(c) from Ciutat c where c.propietari = :j", Long.class).setParameter("j", j).uniqueResult().intValue();
    }

    private int numCarreteres(Session session, Jugador j) {
        return session.createQuery("select count(c) from Carretera c where c.propietari = :j", Long.class).setParameter("j", j).uniqueResult().intValue();
    }

    private Assentament assentamentAl(Session session, Vertex v) {
        return session.createQuery("select a from Assentament a where a.vertex = :v", Assentament.class).setParameter("v", v).uniqueResult();
    }

    private int carreteresPropiesAlVertex(Session session, Jugador j, Vertex v) {
        return session.createQuery("select count(c) from Carretera c where c.propietari = :j and (c.aresta.vertexA = :v or c.aresta.vertexB = :v)", Long.class).setParameter("j", j).setParameter("v", v).uniqueResult().intValue();
    }

    private RecursJugador recurs(Session session, Jugador j, TipusRecurs tipus) {
        return session.createNamedQuery("RecursJugador.perJugadorITipus", RecursJugador.class).setParameter("jugador", j).setParameter("tipus", tipus).getSingleResult();
    }

    void afegirRecurs(Session session, Jugador j, TipusRecurs tipus, int quantitat) {
        RecursJugador r = recurs(session, j, tipus);
        r.setQuantitat(r.getQuantitat() + quantitat);
    }

    private int totalRecursos(Session session, Jugador j) {
        List<RecursJugador> recursos = session.createNamedQuery("RecursJugador.perJugador", RecursJugador.class).setParameter("jugador", j).getResultList();
        int total = 0;
        for (int i = 0; i < recursos.size(); i++) {
            total += recursos.get(i).getQuantitat();
        }
        return total;
    }

    private String comprovarRecursos(Session session, Jugador j, int fusta, int mao, int llana, int blat, int mineral) {
        if (recurs(session, j, TipusRecurs.FUSTA).getQuantitat() < fusta) {
            return Missatges.SENSE_RECURSOS + fusta + " " + TipusRecurs.FUSTA;
        }
        if (recurs(session, j, TipusRecurs.MAO).getQuantitat() < mao) {
            return Missatges.SENSE_RECURSOS + mao + " " + TipusRecurs.MAO;
        }
        if (recurs(session, j, TipusRecurs.LLANA).getQuantitat() < llana) {
            return Missatges.SENSE_RECURSOS + llana + " " + TipusRecurs.LLANA;
        }
        if (recurs(session, j, TipusRecurs.BLAT).getQuantitat() < blat) {
            return Missatges.SENSE_RECURSOS + blat + " " + TipusRecurs.BLAT;
        }
        if (recurs(session, j, TipusRecurs.MINERAL).getQuantitat() < mineral) {
            return Missatges.SENSE_RECURSOS + mineral + " " + TipusRecurs.MINERAL;
        }
        return null;
    }

    private void descomptarRecursos(Session session, Jugador j, int fusta, int mao, int llana, int blat, int mineral) {
        afegirRecurs(session, j, TipusRecurs.FUSTA, -fusta);
        afegirRecurs(session, j, TipusRecurs.MAO, -mao);
        afegirRecurs(session, j, TipusRecurs.LLANA, -llana);
        afegirRecurs(session, j, TipusRecurs.BLAT, -blat);
        afegirRecurs(session, j, TipusRecurs.MINERAL, -mineral);
    }

    private TipusRecurs robarRecursAleatori(Session session, Jugador victima, Jugador lladre) {
        List<RecursJugador> recursos = session.createNamedQuery("RecursJugador.perJugador", RecursJugador.class).setParameter("jugador", victima).getResultList();

        List<TipusRecurs> bossa = new ArrayList<>();
        for (int i = 0; i < recursos.size(); i++) {
            for (int k = 0; k < recursos.get(i).getQuantitat(); k++) {
                bossa.add(recursos.get(i).getTipusRecurs());
            }
        }

        TipusRecurs triat = bossa.get(RANDOM.nextInt(bossa.size()));
        afegirRecurs(session, victima, triat, -1);
        afegirRecurs(session, lladre, triat, 1);
        return triat;
    }

    TipusRecurs recursDelTerreny(TipusTerreny terreny) {
        if (terreny == TipusTerreny.BOSC) {
            return TipusRecurs.FUSTA;
        } else if (terreny == TipusTerreny.TURONS) {
            return TipusRecurs.MAO;
        } else if (terreny == TipusTerreny.PASTURES) {
            return TipusRecurs.LLANA;
        } else if (terreny == TipusTerreny.CAMPS) {
            return TipusRecurs.BLAT;
        } else if (terreny == TipusTerreny.MUNTANYES) {
            return TipusRecurs.MINERAL;
        }
        return null;
    }

    int punts(Session session, Jugador j) {
        int poblats = numPoblats(session, j);
        int ciutats = numCiutats(session, j);
        int cartesPunt = session.createQuery("select count(c) from CartaDesenvolupament c where c.jugador = :j and c.tipus = :t", Long.class).setParameter("j", j).setParameter("t", TipusCarta.PUNT_VICTORIA).uniqueResult().intValue();

        int total = poblats + 2 * ciutats + cartesPunt;

        Jugador exercit = exercitMesGran(session, j.getPartida());
        if (exercit != null && exercit.getId() == j.getId()) {
            total += 2;
        }
        return total;
    }

    Jugador exercitMesGran(Session session, Partida p) {
        List<Jugador> jugadors = session.createQuery("select j from Jugador j where j.partida = :p order by j.cavallersJugats desc", Jugador.class).setParameter("p", p).getResultList();
        if (jugadors.isEmpty()) {
            return null;
        }
        Jugador primer = jugadors.get(0);
        if (primer.getCavallersJugats() < 3) {
            return null;
        }
        if (jugadors.size() > 1 && jugadors.get(1).getCavallersJugats() == primer.getCavallersJugats()) {
            return null;
        }
        return primer;
    }

    void comprovarVictoria(Session session, Partida p) {
        if (p.getEstat() != EstatPartida.EN_CURS) {
            return;
        }
        List<Jugador> jugadors = session.createQuery("select j from Jugador j where j.partida = :p order by j.ordreTorn", Jugador.class).setParameter("p", p).getResultList();

        for (int i = 0; i < jugadors.size(); i++) {
            Jugador j = jugadors.get(i);
            if (punts(session, j) >= p.getPuntsPerGuanyar()) {
                p.setEstat(EstatPartida.FINALITZADA);
                p.setGuanyador(j);
                return;
            }
        }
    }
}
