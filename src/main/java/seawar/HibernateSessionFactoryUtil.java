package seawar;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.spi.ServiceException;

public class HibernateSessionFactoryUtil {
    private static SessionFactory sessionFactory;

    private HibernateSessionFactoryUtil() {}

    public static SessionFactory getSessionFactory() {
        Configuration configuration = null;
        StandardServiceRegistryBuilder builder = null;
        org.hibernate.service.ServiceRegistry r = null;
        if (sessionFactory == null) {
            try {
                configuration = new Configuration().configure();
//                configuration.addAnnotatedClass(User.class);
//                configuration.addAnnotatedClass(Auto.class);
                builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            } catch (IllegalStateException e) {
                //System.out.println("Исключение!" + e);
                System.out.println("Ошибка1. Таблица заблокирована другим приложением");
            } catch (Exception e) {
                //System.out.println("Исключение!" + e);
                System.out.println("Ошибка2. Таблица заблокирована другим приложением");
            }
            try {
                    r = builder.build();
            } catch (ServiceException e) {
                //System.out.println("Исключение!" + e);
                System.out.println("Ошибка11. Таблица заблокирована другим приложением");
            } catch (Exception e) {
                //System.out.println("Исключение!" + e);
                System.out.println("Ошибка12. Таблица заблокирована другим приложением");
            }
            try {
                    sessionFactory = configuration.buildSessionFactory(r);
            } catch (ServiceException e) {
                //System.out.println("Исключение!" + e);
                System.out.println("Ошибка111. Таблица заблокирована другим приложением");
            } catch (Exception e) {
                //System.out.println("Исключение!" + e);
                System.out.println("Ошибка112. Таблица заблокирована другим приложением");
            }
        }
        return sessionFactory;
    }
}