
package dao;

import org.hibernate.Session;

import entities.Jugador;
import entities.RecursJugador;
import util.HibernateUtil;

public class JugadorDAO {

    public void mostrarRecursos(int idJugador) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Jugador jugador = session.find(Jugador.class, idJugador);
            if (jugador == null) {
                System.out.println("No existe ningún jugador con ID " + idJugador);
                return;
            }
            System.out.println("Recursos de " + jugador.getNom() + ":");
            for (RecursJugador recurso : jugador.getRecursos()) {
                System.out.println(recurso.getTipusRecurs() + ": " + recurso.getQuantitat());
            }
        }
    }

    public void construirCarretera(int idJugador, int idAresta) {

        Transaction transacion = null;

        try (Session session =  HibernateUtil.getSessionFactory().openSession()) {
            transacion = session.beginTransaction();

            Jugador jugador = session.find(Jugador.class, idJugador);
            if (jugador == null) {
                throw new IllegalArgumentException("El jugador no existe.");
            }            
            Aresta aresta = session.find(Aresta.class, idAresta);

            if (aresta == null) {
                throw new IllegalArgumentException("La arista no existe.");
            }

            if (!aresta.getPartida().getId()
                    .equals(jugador.getPartida().getId())) {
                throw new IllegalArgumentException("La arista pertenece a otra partida.");
            }
  
            if (jugador.getPartida().getJugadorActiu() == null || !jugador.getPartida().getJugadorActiu().getId().equals(jugador.getId())) {
                throw new IllegalStateException( "No es el turno de este jugador.");
            }
            
            if (aresta.getCarretera() != null) {
                throw new IllegalStateException("Ya hay una carretera en esta arista.");
            }

            Carretera carretera = new Carretera();

            // Hay que asignar el propietario y la arista mediante
            // los setters de las entidades, una vez corregidas
            // las relaciones JPA.

            transacion.commit();

        } catch (RuntimeException e) {
            if (transacion != null && transacion.isActive()) {
                transacion.rollback();
            }
            throw e;
        }
    }
}



