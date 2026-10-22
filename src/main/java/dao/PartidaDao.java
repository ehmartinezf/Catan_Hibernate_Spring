package dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import entities.Assentament;
import entities.Carretera;
import entities.Ciutat;
import entities.Hexagon;
import entities.HexagonVertex;
import entities.Jugador;
import entities.Partida;
import entities.Poblat;
import enums.EstatPartida;
import enums.TipusRecurs;
import util.Inicialitzador;

/*
 * DAO "fort" del bloc partida + tauler. Hereta el CRUD de GenericDao<Partida, Integer>.
 *
 * PATRÓ de cada operació (teoria patroDAO, apartat 4):
 *   - el mètode PÚBLIC obre la transacció, crida el privat, fa commit i, si hi ha excepció, rollback;
 *   - el mètode PRIVAT fa la feina REBENT la sessió (no obre cap transacció pròpia).
 */
public class PartidaDao extends GenericDao<Partida, Integer> implements IPartidaDao {

    private final Random random = new Random();

    // ============================ INICIAR PARTIDA ============================

    @Override
    public int iniciarPartida(List<String> nomsJugadors) {
        return iniciarPartida(nomsJugadors, Inicialitzador.PUNTS_PER_DEFECTE);
    }

    // Versió amb els punts per guanyar configurables (enunciat: "10 per defecte, configurable a la partida")
    @Override
    public int iniciarPartida(List<String> nomsJugadors, int puntsPerGuanyar) {
        // Validacions d'entrada ABANS d'obrir cap transacció
        if (nomsJugadors == null || nomsJugadors.size() < 2 || nomsJugadors.size() > 4) {
            System.out.println("Hi ha d'haver entre 2 i 4 jugadors");
            return -1;
        }
        for (String nom : nomsJugadors) {
            if (nom == null || nom.isBlank()) {
                System.out.println("Cap jugador pot tenir el nom buit");
                return -1;
            }
        }
        if (puntsPerGuanyar < 3) {
            System.out.println("Els punts per guanyar han de ser com a mínim 3");
            return -1;
        }

        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            int id = iniciarPartida(session, nomsJugadors, puntsPerGuanyar);
            session.getTransaction().commit();
            return id;
        } catch (RuntimeException e) {
            handleException(session, e);
            return -1;
        }
    }

    private int iniciarPartida(Session session, List<String> noms, int puntsPerGuanyar) {
        // Tot el que fa Inicialitzador (tauler des de CSV, jugadors, pila) va dins la MATEIXA transacció:
        // si falla un CSV es fa rollback i no queda un tauler a mig crear.
        Partida partida = Inicialitzador.crearPartida(session, noms, puntsPerGuanyar);
        return partida.getId();
    }

    // ============================ MOSTRAR TAULER ============================

    @Override
    public String mostrarTauler(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = mostrarTauler(session, idPartida);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String mostrarTauler(Session session, int idPartida) {
        Partida p = session.find(Partida.class, idPartida);
        if (p == null) return Missatges.PARTIDA_NO_EXISTEIX;

        // Consulta 1: tots els vèrtexs de cada hexàgon, ordenats (JOIN FETCH: res de N+1)
        List<HexagonVertex> files = session.createNamedQuery("HexagonVertex.perPartida", HexagonVertex.class)
                .setParameter("idPartida", idPartida)
                .getResultList();

        // Consulta 2: tots els assentaments de la partida amb el propietari
        Map<Integer, Assentament> perVertex = new java.util.HashMap<>();
        for (Assentament a : session.createNamedQuery("Assentament.perPartida", Assentament.class)
                .setParameter("idPartida", idPartida).getResultList()) {
            perVertex.put(a.getVertex().getId(), a);
        }

        StringBuilder sb = new StringBuilder("=== TAULER de la partida " + idPartida + " ===\n");
        Hexagon actual = null;
        for (HexagonVertex hv : files) {
            Hexagon h = hv.getHexagon();
            if (!h.equals(actual)) { // canvi d'hexàgon: capçalera
                actual = h;
                boolean lladre = p.getLladre() != null && p.getLladre().getId().equals(h.getId());
                sb.append("Hexàgon ").append(h.getCodi()).append(": ").append(h.getTerreny())
                        .append(h.getNumero() != null ? " (número " + h.getNumero() + ")" : "")
                        .append(lladre ? " [LLADRE]" : "").append("\n");
            }
            Assentament a = perVertex.get(hv.getVertex().getId());
            if (a != null) { // només els vèrtexs construïts (les carreteres no cal mostrar-les)
                sb.append("   vèrtex ").append(hv.getVertex().getCodi()).append(" (pos ").append(hv.getPosicio())
                        .append("): ").append(Hibernate.getClass(a).getSimpleName()).append(" de ")
                        .append(a.getPropietari().getNom()).append("\n");
            }
        }
        return sb.toString();
    }

    // ============================ MOSTRAR PARTIDA ============================

    @Override
    public String mostrarPartida(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = mostrarPartida(session, idPartida);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String mostrarPartida(Session session, int idPartida) {
        Partida p = session.find(Partida.class, idPartida);
        if (p == null) return Missatges.PARTIDA_NO_EXISTEIX;

        StringBuilder sb = new StringBuilder("=== PARTIDA " + idPartida + " ===\n");
        sb.append("Estat: ").append(p.getEstat()).append(" | Ronda: ").append(p.getRonda())
                .append(" | Jugador actiu: ").append(p.getActiu() != null ? p.getActiu().getNom() : "-")
                .append(" | Daus tirats: ").append(p.isDausTirats())
                .append(" | Lladre pendent: ").append(p.isLladrePendent()).append("\n");
        if (p.getGuanyador() != null) {
            sb.append("GUANYADOR: ").append(p.getGuanyador().getNom()).append("\n");
        }
        List<Jugador> jugadors = session.createNamedQuery("Jugador.perPartida", Jugador.class)
                .setParameter("idPartida", idPartida).getResultList();
        for (Jugador j : jugadors) {
            // Els tipus de recurs són secrets: només es mostra el TOTAL
            sb.append(String.format("  %d. %s (%s): %d punts, %d poblats, %d ciutats, %d carreteres, %d recursos%n",
                    j.getOrdreTorn(), j.getNom(), j.getColor(), ReglesJoc.calcularPunts(session, j),
                    ReglesJoc.comptar(session, j.getId(), Poblat.class),
                    ReglesJoc.comptar(session, j.getId(), Ciutat.class),
                    ReglesJoc.comptar(session, j.getId(), Carretera.class), j.getTotalRecursos()));
        }
        return sb.toString();
    }

    // ============================ TIRAR DAUS ============================

    @Override
    public String tirarDaus(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            String resultat = tirarDaus(session, idPartida);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String tirarDaus(Session session, int idPartida) {
        Partida p = session.find(Partida.class, idPartida);
        if (p == null) return Missatges.PARTIDA_NO_EXISTEIX;
        if (p.getEstat() == EstatPartida.FINALITZADA) return Missatges.PARTIDA_FINALITZADA;
        if (p.getEstat() != EstatPartida.EN_CURS) return Missatges.NOMES_EN_CURS;
        if (p.isDausTirats()) return Missatges.DAUS_JA_TIRATS;

        int d1 = random.nextInt(6) + 1;
        int d2 = random.nextInt(6) + 1;
        int suma = d1 + d2;
        p.setUltimaTirada(suma);
        p.setDausTirats(true);

        StringBuilder sb = new StringBuilder("Daus: " + d1 + " + " + d2 + " = " + suma + "\n");
        if (suma == 7) {
            // PART AVANÇADA: sense producció, descart de la meitat i lladre pendent
            sb.append(descartar(session, p));
            p.setLladrePendent(true);
            sb.append("Cal moure el lladre (Jugador.MoureLladre)\n");
        } else {
            Map<String, Map<TipusRecurs, Integer>> resum = ReglesJoc.produir(session, p, suma);
            if (resum.isEmpty()) {
                sb.append("Ningú rep recursos\n");
            }
            for (Map.Entry<String, Map<TipusRecurs, Integer>> e : resum.entrySet()) {
                sb.append(e.getKey()).append(" rep ").append(e.getValue()).append("\n");
            }
        }
        return sb.toString();
    }

    // Tots els jugadors amb més de 7 recursos en descarten la meitat (arrodonint cap avall), triats a l'atzar
    private String descartar(Session session, Partida p) {
        StringBuilder sb = new StringBuilder();
        List<Jugador> jugadors = session.createNamedQuery("Jugador.perPartida", Jugador.class)
                .setParameter("idPartida", p.getId()).getResultList();
        for (Jugador j : jugadors) {
            int total = j.getTotalRecursos();
            if (total > 7) {
                int descartar = total / 2;
                List<TipusRecurs> bossa = new ArrayList<>();
                for (TipusRecurs t : TipusRecurs.values()) {
                    for (int i = 0; i < j.getQuantitat(t); i++) bossa.add(t);
                }
                Collections.shuffle(bossa);
                for (int i = 0; i < descartar; i++) {
                    j.treureRecurs(bossa.get(i), 1);
                }
                sb.append(j.getNom()).append(" descarta ").append(descartar).append(" recursos\n");
            }
        }
        return sb.toString();
    }

    // ============================ COMPROVAR VICTÒRIA ============================

    @Override
    public String comprovarVictoria(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida p = session.find(Partida.class, idPartida);
            String resultat = p == null ? Missatges.PARTIDA_NO_EXISTEIX : comprovarVictoria(session, p);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Missatges.ERROR_BD;
        }
    }

    private String comprovarVictoria(Session session, Partida p) {
        if (p.getEstat() == EstatPartida.FINALITZADA) {
            return "La partida ja està finalitzada. Guanyador: " + p.getGuanyador().getNom();
        }
        String missatge = ReglesJoc.comprovarVictoria(session, p);
        return missatge != null ? missatge : "Encara no hi ha guanyador";
    }

    // ============================ EXÈRCIT MÉS GRAN ============================

    @Override
    public Jugador exercitMesGran(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida p = session.find(Partida.class, idPartida);
            Jugador gran = p == null ? null : ReglesJoc.exercitMesGran(session, p);
            session.getTransaction().commit();
            return gran;
        } catch (RuntimeException e) {
            handleException(session, e);
            return null;
        }
    }
}
