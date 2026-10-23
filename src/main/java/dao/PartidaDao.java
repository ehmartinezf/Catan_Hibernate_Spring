package dao;

import entities.Hexagon;
import entities.Partida;
import java.util.List;
import org.hibernate.Session;
import util.Inicialitzador;

public class PartidaDao extends GenericDao<Partida, Integer> implements IPartidaDao {

    @Override
    public int iniciarPartida(List<String> nomsJugadors, int puntsPerGuanyar) {

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

            int id = iniciarPartida(nomsJugadors, puntsPerGuanyar);
            session.getTransaction().commit();
            return id;

        } catch (RuntimeException e) {
            handleException(session, e);
            return -1;
        }
    }

    @Override
    public int iniciarPartida(List<String> nomsJugadors) {
        return iniciarPartida(nomsJugadors, Inicialitzador.PUNTS_PER_DEFECTE);
    }

    @Override
    public String mostrarTauler(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida partida = session.find(Partida.class, idPartida);
            
            List<Hexagon> tauler = session.createQuery("FROM hexagon", Hexagon.class).getResultList();
            for(Hexagon hexagon: tauler){
                System.out.println(hexagon);
            }
        } catch (Exception e) {
        }
    }

}
