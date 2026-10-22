package dao;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import entities.Assentament;
import entities.Aresta;
import entities.Carretera;
import entities.Construccio;
import entities.Hexagon;
import entities.Jugador;
import entities.Partida;
import entities.Poblat;
import entities.Ciutat;
import entities.Vertex;
import enums.EstatPartida;
import enums.TipusCarta;
import enums.TipusRecurs;
import jakarta.persistence.EntityManager;
import util.Costos;

/*
 * Regles del joc que comparteixen PartidaDao i JugadorDao.
 * Tots els mètodes REBEN la sessió (Session és un EntityManager) i NO obren cap transacció:
 * treballen dins de la transacció de qui els crida (patró de la teoria: "els mètodes privats reben la sessió").
 * Els mètodes comprovar... tornen null si tot és correcte, o el missatge d'error.
 */
public class ReglesJoc {

    private ReglesJoc() {
    }

    // ================= TORN I FASE =================

    /*
     * Comprovacions comunes a TOTES les accions d'un jugador (enunciat, "Considerations"):
     * partida no finalitzada, jugador actiu i, en EN_CURS, daus tirats i lladre no pendent.
     * permetPreparacio = true per a construir els dos primers poblats i carreteres.
     */
    public static String validarAccio(Jugador j, boolean permetPreparacio) {
        Partida p = j.getPartida();
        if (p.getEstat() == EstatPartida.FINALITZADA) return Missatges.PARTIDA_FINALITZADA;
        if (p.getActiu() == null || !p.getActiu().getId().equals(j.getId())) return Missatges.NO_ES_EL_TEU_TORN;
        if (p.getEstat() == EstatPartida.PREPARACIO) return permetPreparacio ? null : Missatges.NOMES_EN_CURS;
        if (!p.isDausTirats()) return Missatges.CAL_TIRAR_DAUS;
        if (p.isLladrePendent()) return Missatges.LLADRE_PENDENT;
        return null;
    }

    // ================= COMPTADORS (consultes a la BD) =================

    public static long comptar(EntityManager em, int idJugador, Class<? extends Construccio> tipus) {
        return em.createNamedQuery("Construccio.comptarPerTipus", Long.class)
                .setParameter("idJugador", idJugador)
                .setParameter("tipus", tipus)
                .getSingleResult();
    }

    private static long carreteresTocant(EntityManager em, int idJugador, int idVertex) {
        return em.createNamedQuery("Carretera.tocaVertex", Long.class)
                .setParameter("idJugador", idJugador)
                .setParameter("idVertex", idVertex)
                .getSingleResult();
    }

    // L'assentament (poblat o ciutat) d'aquest vèrtex és del jugador?
    private static boolean assentamentPropi(EntityManager em, int idJugador, int idVertex) {
        List<Assentament> as = em.createNamedQuery("Assentament.perVertex", Assentament.class)
                .setParameter("idVertex", idVertex)
                .getResultList();
        return !as.isEmpty() && as.get(0).getPropietari().getId().equals(idJugador);
    }

    // ================= PUNTS, EXÈRCIT I VICTÒRIA =================

    public static int calcularPunts(EntityManager em, Jugador j) {
        int punts = (int) comptar(em, j.getId(), Poblat.class) + 2 * (int) comptar(em, j.getId(), Ciutat.class);
        punts += em.createNamedQuery("Carta.comptarMa", Long.class)
                .setParameter("idJugador", j.getId())
                .setParameter("tipus", TipusCarta.PUNT_VICTORIA)
                .getSingleResult().intValue();
        Jugador gran = exercitMesGran(em, j.getPartida());
        if (gran != null && gran.getId().equals(j.getId())) {
            punts += 2;
        }
        return punts;
    }

    // Més cavallers jugats, amb un mínim de 3 i sense empat. Si no hi ha ningú, null.
    public static Jugador exercitMesGran(EntityManager em, Partida p) {
        List<Jugador> jugadors = em.createNamedQuery("Jugador.perPartida", Jugador.class)
                .setParameter("idPartida", p.getId())
                .getResultList();
        Jugador millor = null;
        int max = 0;
        boolean empat = false;
        for (Jugador j : jugadors) {
            if (j.getCavallersJugats() > max) {
                max = j.getCavallersJugats();
                millor = j;
                empat = false;
            } else if (j.getCavallersJugats() == max) {
                empat = true;
            }
        }
        return (millor != null && max >= 3 && !empat) ? millor : null;
    }

    // Si algú arriba als punts per guanyar: FINALITZADA + guanyador. Torna el missatge de victòria o null.
    public static String comprovarVictoria(EntityManager em, Partida p) {
        if (p.getEstat() == EstatPartida.FINALITZADA) return null;
        List<Jugador> jugadors = em.createNamedQuery("Jugador.perPartida", Jugador.class)
                .setParameter("idPartida", p.getId())
                .getResultList();
        Jugador guanyador = null;
        int maxPunts = 0;
        for (Jugador j : jugadors) {
            int punts = calcularPunts(em, j);
            if (punts >= p.getPuntsPerGuanyar() && punts > maxPunts) {
                maxPunts = punts;
                guanyador = j;
            }
        }
        if (guanyador == null) return null;
        p.setEstat(EstatPartida.FINALITZADA);
        p.setGuanyador(guanyador);
        return "La partida ha acabat! Guanyador: " + guanyador.getNom() + " amb " + maxPunts + " punts";
    }

    // ================= REGLES DE COL·LOCACIÓ =================

    public static String comprovarCarretera(EntityManager em, Jugador j, Aresta a) {
        Partida p = j.getPartida();
        if (!a.getPartida().getId().equals(p.getId())) return Missatges.ALTRA_PARTIDA;

        long ocupada = em.createNamedQuery("Carretera.perAresta", Long.class)
                .setParameter("idAresta", a.getId())
                .getSingleResult();
        if (ocupada > 0) return Missatges.ARESTA_OCUPADA;

        int idA = a.getVertexA().getId();
        int idB = a.getVertexB().getId();

        if (p.getEstat() == EstatPartida.PREPARACIO) {
            long poblats = comptar(em, j.getId(), Poblat.class);
            long carreteres = comptar(em, j.getId(), Carretera.class);
            if (poblats < p.getRonda()) return Missatges.PREP_POBLAT_PRIMER;
            if (carreteres >= p.getRonda()) return Missatges.PREP_CARRETERA_JA_COLOCADA;
            // ha de tocar un poblat propi que encara no té cap carretera pròpia al costat
            boolean toca = false;
            for (int idV : new int[] { idA, idB }) {
                if (assentamentPropi(em, j.getId(), idV) && carreteresTocant(em, j.getId(), idV) == 0) {
                    toca = true;
                }
            }
            return toca ? null : Missatges.PREP_CARRETERA_POBLAT;
        }

        // EN_CURS: connectada a un assentament propi o a una altra carretera pròpia (un poblat rival no talla)
        boolean connectada = assentamentPropi(em, j.getId(), idA) || assentamentPropi(em, j.getId(), idB)
                || carreteresTocant(em, j.getId(), idA) > 0 || carreteresTocant(em, j.getId(), idB) > 0;
        if (!connectada) return Missatges.NO_CONNECTAT;
        if (comptar(em, j.getId(), Carretera.class) >= Costos.MAX_CARRETERES) return Missatges.LIMIT_CARRETERES;
        if (!j.potPagar(Costos.CARRETERA)) return Missatges.SENSE_RECURSOS;
        return null;
    }

    public static String comprovarPoblat(EntityManager em, Jugador j, Vertex v) {
        Partida p = j.getPartida();
        if (!v.getPartida().getId().equals(p.getId())) return Missatges.ALTRA_PARTIDA;

        long ocupat = em.createNamedQuery("Assentament.perVertex", Assentament.class)
                .setParameter("idVertex", v.getId())
                .getResultList().size();
        if (ocupat > 0) return Missatges.VERTEX_OCUPAT;

        long veins = em.createNamedQuery("Assentament.veinsOcupats", Long.class)
                .setParameter("idVertex", v.getId())
                .getSingleResult();
        if (veins > 0) return Missatges.REGLA_DISTANCIA;

        long poblats = comptar(em, j.getId(), Poblat.class);
        if (poblats >= Costos.MAX_POBLATS) return Missatges.LIMIT_POBLATS;

        if (p.getEstat() == EstatPartida.PREPARACIO) {
            // en la ronda r es col·loca el poblat número r, i només després d'haver col·locat la carretera anterior
            if (poblats >= p.getRonda()) return Missatges.PREP_POBLAT_JA_COLOCAT;
            if (comptar(em, j.getId(), Carretera.class) < poblats) return Missatges.PREP_CARRETERA_PRIMER;
            return null; // a PREPARACIO ni connexió ni recursos
        }

        if (carreteresTocant(em, j.getId(), v.getId()) == 0) return Missatges.NO_CONNECTAT;
        if (!j.potPagar(Costos.POBLAT)) return Missatges.SENSE_RECURSOS;
        return null;
    }

    public static String comprovarCiutat(EntityManager em, Jugador j) {
        if (comptar(em, j.getId(), Ciutat.class) >= Costos.MAX_CIUTATS) return Missatges.LIMIT_CIUTATS;
        if (!j.potPagar(Costos.CIUTAT)) return Missatges.SENSE_RECURSOS;
        return null;
    }

    // ================= PRODUCCIÓ =================

    /*
     * Producció d'una tirada: per cada hexàgon amb aquest número (i sense lladre), cada poblat adjacent
     * dona 1 recurs al seu propietari i cada ciutat 2. Modifica els jugadors (entitats gestionades:
     * dirty checking) i torna un resum nom -> recursos rebuts.
     */
    public static Map<String, Map<TipusRecurs, Integer>> produir(EntityManager em, Partida p, int numero) {
        Map<String, Map<TipusRecurs, Integer>> resum = new TreeMap<>();
        List<Hexagon> hexagons = em.createNamedQuery("Hexagon.perNumero", Hexagon.class)
                .setParameter("idPartida", p.getId())
                .setParameter("numero", numero)
                .getResultList();
        for (Hexagon h : hexagons) {
            if (p.getLladre() != null && p.getLladre().getId().equals(h.getId())) {
                continue; // l'hexàgon amb el lladre no produeix
            }
            TipusRecurs recurs = h.getTerreny().getRecurs();
            List<Assentament> assentaments = em.createNamedQuery("Assentament.perHexagon", Assentament.class)
                    .setParameter("idHexagon", h.getId())
                    .getResultList();
            for (Assentament a : assentaments) {
                a.getPropietari().afegirRecurs(recurs, a.getProduccio());
                resum.computeIfAbsent(a.getPropietari().getNom(), k -> new TreeMap<>())
                        .merge(recurs, a.getProduccio(), Integer::sum);
            }
        }
        return resum;
    }
}
