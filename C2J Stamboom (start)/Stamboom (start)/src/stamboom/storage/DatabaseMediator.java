/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import stamboom.domain.Administratie;
import stamboom.domain.Geslacht;
import stamboom.domain.Gezin;
import stamboom.domain.Persoon;

public class DatabaseMediator implements IStorageMediator {

    private Properties props;
    private Connection conn;

    @Override
    public Administratie load() throws IOException 
    {
        
        Administratie admin = new Administratie();
        
        try
        {
            initConnection();
            
            String query = "select * from PERSOON order by NR asc";
            Statement st = conn.createStatement();
            ResultSet resultset = st.executeQuery(query);
            while(resultset.next())
            {
                int nr = resultset.getInt("NR");
                String vnamen = resultset.getString("VOORNAMEN");
                String[] voornamen = vnamen.split("\\s+");
                String achternaam = resultset.getString("ACHTERNAAM");
                String tussenvoegsels = resultset.getString("ACHTERNAAM");
                java.sql.Date geboortedatum = resultset.getDate("GEBOORTEDATUM");
                String geboorteplaats = resultset.getString("GEBOORTEPLAATS");
                String geslachtStr = resultset.getString("GESLACHT");
                Geslacht geslacht;
                
                switch (geslachtStr) 
                {
                    case "MAN":
                        geslacht = Geslacht.MAN;
                        break;
                    case "VROUW":
                        geslacht = Geslacht.VROUW;
                        break;
                    default:
                        geslacht = Geslacht.MAN;
                }

                Calendar d = Calendar.getInstance();
                d.setTime(geboortedatum);
                admin.addPersoon(geslacht, voornamen, achternaam, tussenvoegsels, d, geboorteplaats, null);
            }
            
            query = "select * from GEZIN order by NR asc";
            st = conn.createStatement();
            resultset = st.executeQuery(query);
            
            while(resultset.next())
            {
                int o1 = resultset.getInt("OUDER1");
                int o2 = resultset.getInt("OUDER2");
                
                Persoon ouder1 = admin.getPersoon(o1);
                Persoon ouder2 = admin.getPersoon(o2);
                
                boolean huwelijk=false;
                java.sql.Date huwelijksdatum = resultset.getDate("HUWELIJKSDATUM"); 
                if(!resultset.wasNull())
                    huwelijk = true;

                boolean scheiding=false;
                java.sql.Date scheidingsdatum = resultset.getDate("SCHEIDINGSDATUM");
                if(!resultset.wasNull())
                    scheiding = true;
                
                Gezin g = admin.addOngehuwdGezin(ouder1, ouder2);
                
                if(huwelijk)
                {
                    Calendar hdatum = Calendar.getInstance();
                    hdatum.setTime(huwelijksdatum); 
                    admin.setHuwelijk(g, hdatum);
                }
                if(scheiding)
                {
                    Calendar sdatum = Calendar.getInstance();
                    sdatum.setTime(scheidingsdatum);
                    admin.setScheiding(g, sdatum);
                }
            }
            
            query = "select * from PERSOON where OUDERLIJKGEZIN is not null";
            st = conn.createStatement();
            resultset = st.executeQuery(query);
            
            while(resultset.next())
            {
                int nr = resultset.getInt("NR");
                int gezinnr = resultset.getInt("OUDERLIJKGEZIN");
                Persoon kind = admin.getPersoon(nr);
                Gezin ouders = admin.getGezin(gezinnr);
                admin.setOuders(kind, ouders);
            }            
        }
        catch (SQLException ex) 
        {
            Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return admin;
    }
    
    public void save(Administratie admin) throws IOException {
        try {
            
            initConnection();
            
            //Clear the whole database
            String query = "DELETE FROM PERSOON";
            Statement st = conn.createStatement();
            st.execute(query);
            query = "DELETE FROM GEZIN";
            st.execute(query);
            
            
            ArrayList<PreparedStatement> queries = new ArrayList<>();
            String preparedQuery = "INSERT INTO PERSOON(NR, VOORNAMEN, TUSSENVOEGSEL, ACHTERNAAM, GEBOORTEDATUM, GEBOORTEPLAATS, OUDERLIJKGEZIN, GESLACHT) values (?,?,?,?,?,?,?,?)";
            
            //Fill list of preparedstatements with all the Persons
            for (Persoon p : admin.getPersonen())
            {
                PreparedStatement ps = conn.prepareStatement(preparedQuery);
                ps.setInt(1, p.getNr());
                ps.setString(2, p.getVoornamen());
                if(p.getTussenvoegsel() == null){
                    ps.setString(3, "");
                }
                else{
                    ps.setString(3, p.getTussenvoegsel());
                }
                ps.setString(4, p.getAchternaam());
                
                java.sql.Date date = convertJavaDateToSqlDate(p.getGebDat().getTime()); //>_>
                ps.setDate(5, date);

                ps.setString(6, p.getGebPlaats());
                if(p.getOuderlijkGezin() != null){
                    ps.setInt(7, p.getOuderlijkGezin().getNr());     
                }
                else{
                    ps.setNull(7, java.sql.Types.INTEGER);
                }
                if(p.getGeslacht() != null){
                    ps.setString(8, p.getGeslacht().toString());
                }
                
            }
            
            preparedQuery= "INSERT INTO GEZIN(NR, OUDER1, OUDER2, HUWELIJKSDATUM, SCHEIDINGSDATUM) values (?,?,?,?,?)";
            for(Gezin g : admin.getGezinnen())
            {
                PreparedStatement ps = conn.prepareStatement(preparedQuery);
                ps.setInt(1, g.getNr());
                ps.setInt(2, g.getOuder1().getNr());
                if(g.getOuder2() != null)
                    ps.setInt(3, g.getOuder2().getNr());
                else
                    ps.setNull(3, java.sql.Types.INTEGER);
                if(g.getHuwelijksdatum() != null)
                {
                    java.sql.Date date = convertJavaDateToSqlDate(g.getHuwelijksdatum().getTime());
                    ps.setDate(4, date);
                }
                else
                    ps.setNull(4, java.sql.Types.DATE);
                if(g.getScheidingsdatum() != null)
                {
                    java.sql.Date date = convertJavaDateToSqlDate(g.getScheidingsdatum().getTime());
                    ps.setDate(5, date);
                }
                else
                    ps.setNull(5, java.sql.Types.DATE);
                queries.add(ps);
                    
            }
            
            //Execute the prepared statements to save all the Persons
            for(PreparedStatement ps: queries)
            {
                try {
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    conn.commit();
                }
            }
        }catch (SQLException ex) {
                    Logger.getLogger(DatabaseMediator.class.getName()).log(Level.SEVERE, null, ex);
                }
    }
    
    public java.sql.Date convertJavaDateToSqlDate(java.util.Date date) 
    {
        return new java.sql.Date(date.getTime());
    }

    /**
     * Laadt de instellingen, in de vorm van een Properties bestand, en controleert
     * of deze in de correcte vorm is, en er verbinding gemaakt kan worden met
     * de database.
     * @param props
     * @return
     */
    @Override
    public final boolean configure(Properties props) {
        this.props = props;
        if (!isCorrectlyConfigured()) {
            System.err.println("props mist een of meer keys");
            return false;
        }

        try {
            initConnection();
            return true;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            this.props = null;
            return false;
        } finally {
            closeConnection();
        }
    }

    @Override
    public Properties config() {
        return props;
    }

    @Override
    public boolean isCorrectlyConfigured() {
        if (props == null) {
            return false;
        }
        if (!props.containsKey("driver")) {
            return false;
        }
        if (!props.containsKey("url")) {
            return false;
        }
        if (!props.containsKey("username")) {
            return false;
        }
        if (!props.containsKey("password")) {
            return false;
        }
        return true;
    }

    private void initConnection() throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        conn = DriverManager.getConnection("jdbc:oracle:thin:dbi317913/Dw8832OSpE@//fhictora01.fhict.local:1521/fhictora");
    }

    private void closeConnection() {
        try {
            conn.close();
            conn = null;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
