package dao;

import entities.Jugador;
import entities.Vertex;
import enums.TipusRecurs;
import java.util.List;

public interface IJugadorDao extends IGenericDao<Jugador, Integer> {

    void mostrarRecursos(int idJugador);

    String construirCarretera(int idJugador, int idAresta);

    String construirPoblat(int idJugador, int idVertex);

    String millorarACiutat(int idJugador, int idVertex);

    String comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep);

    int calcularPunts(int idJugador);

    String passarTorn(int idJugador);

    String moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima);

    String comprarCarta(int idJugador);

    String jugarCarta(int idJugador, int idCarta, TipusRecurs recurs);

    List<Vertex> vertexsDisponibles(int idJugador);
    
}
