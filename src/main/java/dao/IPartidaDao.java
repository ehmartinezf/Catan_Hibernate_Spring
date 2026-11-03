package dao;

import entities.Partida;
import java.util.List;

public interface IPartidaDao extends IGenericDao<Partida, Integer> {

    int iniciarPartida(List<String> nomsJugadors);
    
    String mostrarTauler(int idPartida);
    
    String mostrarPartida(int idPartida);
    
    void comprovarVictoria(int idPartida);
}
