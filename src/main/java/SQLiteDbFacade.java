import Macro.Macro;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.util.List;
import java.util.Properties;

public class SQLiteDbFacade implements DbFacade {

    private static SessionFactory sessionFactory = null;
    private static ServiceRegistry serviceRegistry = null;

    public SQLiteDbFacade() {
        configureSessionFactory();
    }


    private static  SessionFactory configureSessionFactory() {
        Configuration hibConfig = new Configuration();
        hibConfig.configure();

        Properties properties = hibConfig.getProperties();

        serviceRegistry = new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();
        sessionFactory = hibConfig.buildSessionFactory(serviceRegistry);

        return sessionFactory;
    }

    @Override
    public Macro loadMacro(String macroName) {

        Session session = null;
        Macro loadedMacro = null;

        try {

            session = sessionFactory.openSession();
            // Fetching saved data
            List<Macro> results = session.createQuery("from Macro.Macro where name = '" + macroName + "'" ).list();
            if (results.size() != 0) {
                loadedMacro = results.get(0);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally{
            if(session != null) {
                session.close();
            }
        }

        return loadedMacro;
    }

    @Override
    public boolean saveMacro(Macro userMacro) {

        Session session = null;
        Transaction tx = null;

        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();


            session.save(userMacro);

            // Committing the change in the database.
            session.flush();
            tx.commit();

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();

            // Rolling back the changes to make the data consistent in case of any failure
            // in between multiple database write operations.
            tx.rollback();
            return false;
        } finally{
            if(session != null) {
                session.close();
            }
        }
    }
}
