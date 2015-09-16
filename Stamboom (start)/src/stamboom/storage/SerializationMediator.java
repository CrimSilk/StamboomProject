/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.storage;

import java.io.*;
//import java.io.File;
//import java.io.IOException;
import java.util.Properties;
import stamboom.domain.Administratie;

public class SerializationMediator implements IStorageMediator {

    /**
     * bevat de bestandslocatie. Properties is een subclasse van HashTable, een
     * alternatief voor een List. Het verschil is dat een List een volgorde heeft,
     * en een HashTable een key/value index die wordt opgevraagd niet op basis van
     * positie, maar op key.
     */
    private Properties props;

    /**
     * creation of a non configured serialization mediator
     */
    public SerializationMediator() {
        props = null;
    }

    @Override
    public Administratie load() throws IOException 
    {
        if (!isCorrectlyConfigured()) 
        {
            throw new RuntimeException("Serialization mediator isn't initialized correctly.");
        }
        
        Administratie admin = null; //Maak een lege administratie aan
        try
        {
            FileInputStream fisIn = new FileInputStream(props.getProperty("file")); //Open de fis met het path zoals opgegeven in de properties
            ObjectInputStream oisIn = new ObjectInputStream(fisIn); //Opent de ois
            admin = (Administratie) oisIn.readObject(); //Het ingelezen object wordt gecast naar admin
            oisIn.close(); //sluiten van beide streams
            fisIn.close();
        }
        catch(IOException i) //Als er iets misgaat met het bestand tijdens het streamen
        {
            System.out.println("Probleem met het lezen van het bestand! 1");
            i.printStackTrace();
            return null;
        }
        catch(ClassNotFoundException c) //Als de klasse niet gevonden kan worden
        {
            System.out.println("Administratie klasse niet gevonden!");
            return null;
        }
        // todo opgave 2 X
        return admin;
    }

    @Override
    public void save(Administratie admin) throws IOException {
        if (!isCorrectlyConfigured()) {
            System.out.println(props.getProperty("file"));
            throw new RuntimeException("Serialization mediator isn't initialized correctly.");
        }
        
        try
        {
            FileOutputStream fosFile = new FileOutputStream(props.getProperty("file"));
            ObjectOutputStream oosOut= new ObjectOutputStream(fosFile);
            oosOut.writeObject(admin);
            oosOut.close();
            fosFile.close();
        }
        catch (IOException i)
        {
            System.out.println("Probleem met het lezen van het bestand! 2");
            i.printStackTrace();
            return;
        }
        // todo opgave 2 X
  
    }

    /**
     * Laadt de instellingen, in de vorm van een Properties bestand, en controleert
     * of deze in de juiste vorm is.
     * @param props
     * @return
     */
    @Override
    public boolean configure(Properties props) {
        this.props = props;
        return isCorrectlyConfigured();
    }

    @Override
    public Properties config() {
        return props;
    }

    /**
     * Controleert of er een geldig Key/Value paar bestaat in de Properties.
     * De bedoeling is dat er een Key "file" is, en de Value van die Key 
     * een String representatie van een FilePath is (eg. C:\\Users\Username\test.txt).
     * 
     * @return true if config() contains at least a key "file" and the
     * corresponding value is formatted like a file path
     */
    @Override
    public boolean isCorrectlyConfigured() {
        if (props == null) {
            return false;
        }
        return props.containsKey("file") 
                && props.getProperty("file").contains(File.separator);
    }
}
