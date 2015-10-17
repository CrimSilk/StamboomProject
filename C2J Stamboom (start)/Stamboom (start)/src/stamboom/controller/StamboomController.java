/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.controller;

import java.io.File;
import java.io.IOException;
import stamboom.domain.Administratie;
import stamboom.storage.IStorageMediator;
import stamboom.storage.SerializationMediator;

//TGV:
import java.util.Properties;
import stamboom.storage.DatabaseMediator;
import stamboom.storage.DatabaseMediator;

public class StamboomController {

    private Administratie admin;
    private IStorageMediator storageMediator;
    private DatabaseMediator dbMediator;

    /**
     * creatie van stamboomcontroller met lege administratie en onbekend
     * opslagmedium
     */
    public StamboomController() {
        admin = new Administratie();
        storageMediator = new SerializationMediator();
        dbMediator = new DatabaseMediator();
        
    }

    public Administratie getAdministratie() {
        return admin;
    }

    /**
     * administratie wordt leeggemaakt (geen personen en geen gezinnen)
     */
    public void clearAdministratie() {
        admin = new Administratie();
    }

    /**
     * administratie wordt in geserialiseerd bestand opgeslagen
     *
     * @param bestand
     * @throws IOException
     */
    public void serialize(File bestand) throws IOException {
        Properties properties = new Properties(); 
        properties.setProperty("file", bestand.getAbsolutePath());
        storageMediator.configure(properties);
        storageMediator.save(admin);
    }

    /**
     * administratie wordt vanuit geserialiseerd bestand gevuld
     *
     * @param bestand
     * @throws IOException
     */
    public void deserialize(File bestand) throws IOException {
        Properties properties = new Properties(); //Zie Serialize methode
        properties.setProperty("file", bestand.getAbsolutePath());
        storageMediator.configure(properties);
        admin = storageMediator.load();
    }
    
    /**
     * administratie wordt vanuit standaarddatabase opgehaald
     *
     * @throws IOException
     */
    public void loadFromDatabase() throws IOException 
    {
        admin = dbMediator.load();
    }

    /**
     * administratie wordt in standaarddatabase bewaard
     *
     * @throws IOException
     */
    public void saveToDatabase() throws IOException {
        dbMediator.save(admin);
        //todo opgave 4
    }

}
