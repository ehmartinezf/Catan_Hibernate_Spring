package dao;

import entities.Partida;
import java.util.List;

public interface IPartidaDao extends IGenericDao<Partida, Integer> {

    int iniciarPartida(List<String> nomsJugadors);

    int iniciarPartida(List<String> nomsJugadors, int puntsPerGuanyar);
}
