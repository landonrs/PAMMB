/* PAMM: Personal Assistant Macro Maker.
 * Copyright (C) 2019 Landon Shumway.
 * https://github.com/landonrs/PAMMB
 *
 * PAMM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PAMM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package db;

import macro.Macro;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.util.List;
import java.util.Properties;

public class SQLiteDbFacade {

    private static SessionFactory sessionFactory = null;
    private static ServiceRegistry serviceRegistry = null;
    private static SQLiteDbFacade instance = null;

    private SQLiteDbFacade() {
        configureSessionFactory();
    }

    public static SQLiteDbFacade getInstance() {
        if(instance == null) {
            instance = new SQLiteDbFacade();
        }

        return instance;
    }


    private static  SessionFactory configureSessionFactory() {
        Configuration hibConfig = new Configuration();
        hibConfig.configure();

        Properties properties = hibConfig.getProperties();

        serviceRegistry = new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();
        sessionFactory = hibConfig.buildSessionFactory(serviceRegistry);

        return sessionFactory;
    }

    public static List getMacroNames(){

        Session session = null;
        List macroNames = null;

        try {

            session = sessionFactory.openSession();
            // get all macro names
            macroNames = session.createCriteria(Macro.class)
                    .setProjection(Projections.property("name")).list();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally{
            if(session != null) {
                session.close();
            }
        }

        return macroNames;
    }

    public void deleteMacro(String macroName) {

        Session session = null;
        Transaction tx = null;
        Macro deletedMacro = getInstance().loadMacro(macroName);

        try {

            session = sessionFactory.openSession();
            tx = session.beginTransaction();

            // delete selected macro
            session.delete(deletedMacro);

            // Committing the change in the database.
            session.flush();
            tx.commit();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally{
            if(session != null) {
                session.close();
            }
        }
    }

    public Macro loadMacro(String macroName) {

        Session session = null;
        Macro loadedMacro = null;

        try {

            session = sessionFactory.openSession();
            // Fetching saved data
            loadedMacro = (Macro) session.get(Macro.class, macroName);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally{
            if(session != null) {
                session.close();
            }
        }

        return loadedMacro;
    }

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

    /**
     * Verifies that a user entered macro name does not already exist in DB
     * @param macroName - the name entered by the user for the newly created macro
     * @return true if macroName does not already exist in db
     */
    public boolean uniqueMacroName(String macroName) {
        Macro userMacro = loadMacro(macroName);
        if(userMacro != null) {
            return false;
        }

        return true;
    }
}
