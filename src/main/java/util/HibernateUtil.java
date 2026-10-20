package util;

import org.hibernate.cfg.Configuration;

import com.mysql.cj.xdevapi.SessionFactory;

public class HibernateUtil {

	private static final SessionFactory sessionFactory = crearSessionFactory();

	private static SessionFactory crearSessionFactory() {
		try {
			return (SessionFactory) new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("Error al crear SessionFactory: " + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static void cerrar() {
		if (sessionFactory != null && !((org.hibernate.SessionFactory) sessionFactory).isClosed()) {
			((org.hibernate.SessionFactory) sessionFactory).close();
		}
	}
}
