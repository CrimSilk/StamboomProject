/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stamboom.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import stamboom.controller.StamboomController;
import stamboom.domain.Administratie;
import stamboom.domain.Geslacht;
import stamboom.domain.Gezin;
import stamboom.domain.Persoon;
import stamboom.storage.IStorageMediator;
import stamboom.storage.DatabaseMediator;
import stamboom.storage.SerializationMediator;
import stamboom.util.StringUtilities;

/**
 *
 * @author frankpeeters
 */
public class StamboomFXController extends StamboomController implements Initializable {

    //MENUs en TABs
    @FXML MenuBar menuBar;
    @FXML MenuItem miNew;
    @FXML MenuItem miOpen;
    @FXML MenuItem miSave;
    @FXML CheckMenuItem cmDatabase;
    @FXML MenuItem miClose;
    @FXML Tab tabPersoon;
    @FXML Tab tabGezin;
    @FXML Tab tabPersoonInvoer;
    @FXML Tab tabGezinInvoer;

    //PERSOON
    @FXML ComboBox cbPersonen;
    @FXML TextField tfPersoonNr;
    @FXML TextField tfVoornamen;
    @FXML TextField tfTussenvoegsel;
    @FXML TextField tfAchternaam;
    @FXML TextField tfGeslacht;
    @FXML TextField tfGebDatum;
    @FXML TextField tfGebPlaats;
    @FXML ComboBox cbOuderlijkGezin;
    @FXML ListView lvAlsOuderBetrokkenBij;
    @FXML Button btStamboom;

    //INVOER GEZIN
    @FXML ComboBox cbOuder1Invoer;
    @FXML ComboBox cbOuder2Invoer;
    @FXML TextField tfHuwelijkInvoer;
    @FXML TextField tfScheidingInvoer;
    @FXML Button btOKGezinInvoer;
    @FXML Button btCancelGezinInvoer;
    
    //INVOER PERSOON
    @FXML TextField tfInvoerVoornamen;
    @FXML TextField tfInvoerTussenvoegsel;
    @FXML TextField tfInvoerAchternaam;
    @FXML TextField tfInvoerGeboortePlaats;
    @FXML ComboBox cbInvoerGeslacht;
    @FXML ComboBox cbInvoerOuderlijkGezin;
    @FXML TextField tfInvoerGeboorteDatum;
    @FXML Button btOKPersoonInvoer;
    @FXML Button btCancelPersoonInvoer;
    
    //GEZIN
    @FXML TextField tfGezinNummer;
    @FXML TextField tfOuder1;
    @FXML TextField tfOuder2;
    @FXML TextField tfGetrouwdOp;
    @FXML Button btGetrouwdOpOpslaan;
    @FXML TextField tfGescheidenOp;
    @FXML Button btGescheidenOpOpslaan;
    @FXML ListView lvKinderen; 
    @FXML ComboBox cbGezin;
    
    private boolean withDatabase;

    private IStorageMediator storageMediator;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initComboboxes();
        withDatabase = true;
        
        storageMediator = new SerializationMediator();
    }

    private void initComboboxes() {
        cbInvoerGeslacht.setItems(FXCollections.observableArrayList(Geslacht.values()));
        cbInvoerGeslacht.setValue(Geslacht.MAN);
        cbOuder1Invoer.setItems(getAdministratie().getPersonen());
        cbOuder2Invoer.setItems(getAdministratie().getPersonen());
        cbPersonen.setItems(getAdministratie().getPersonen());
        cbGezin.setItems(getAdministratie().getGezinnen());
        cbOuderlijkGezin.setItems(getAdministratie().getGezinnen());
        cbInvoerOuderlijkGezin.setItems(getAdministratie().getGezinnen());

    }

    public void selectPersoon(Event evt) {
        Persoon persoon = (Persoon) cbPersonen.getSelectionModel().getSelectedItem();
        showPersoon(persoon);
    }

    private void showPersoon(Persoon persoon) {
        if (persoon == null) {
            clearTabPersoon();
            System.out.println("Persoon = null");
        } else {
            tfPersoonNr.setText(persoon.getNr() + "");
            tfVoornamen.setText(persoon.getVoornamen());
            tfTussenvoegsel.setText(persoon.getTussenvoegsel());
            tfAchternaam.setText(persoon.getAchternaam());
            tfGeslacht.setText(persoon.getGeslacht().toString());
            tfGebDatum.setText(StringUtilities.datumString(persoon.getGebDat()));
            tfGebPlaats.setText(persoon.getGebPlaats());
            if (persoon.getOuderlijkGezin() != null) {
                cbOuderlijkGezin.getSelectionModel().select(persoon.getOuderlijkGezin());
            } else {
                cbOuderlijkGezin.getSelectionModel().clearSelection();
            }
            lvAlsOuderBetrokkenBij.setItems(persoon.getAlsOuderBetrokkenIn());
        }
    }

    public void setOuders(Event evt) {
        if (tfPersoonNr.getText().isEmpty()) {
            return;
        }
        Gezin ouderlijkGezin = (Gezin) cbOuderlijkGezin.getSelectionModel().getSelectedItem();
        if (ouderlijkGezin == null) {
            return;
        }

        int nr = Integer.parseInt(tfPersoonNr.getText());
        Persoon p = getAdministratie().getPersoon(nr);
        if(getAdministratie().setOuders(p, ouderlijkGezin)){
            showDialog("Success", ouderlijkGezin.toString()
                + " is nu het ouderlijk gezin van " + p.getNaam());
        }
        
    }

    public void selectGezin(Event evt) {
        Gezin gezin = (Gezin)cbGezin.getSelectionModel().getSelectedItem();
        if(gezin != null) {
            showGezin(gezin);
        }
    }

    private void showGezin(Gezin gezin) {
        clearTabGezin();
        if(gezin != null) {
            
            tfGezinNummer.setText(String.valueOf(gezin.getNr()));
            if(gezin.getOuder1() != null) {
                tfOuder1.setText(gezin.getOuder1().getNaam());
            }
            if(gezin.getOuder2() != null) {
                tfOuder2.setText(gezin.getOuder2().getNaam());
            }
            
            Calendar huwelijksDatum = gezin.getHuwelijksdatum();
            Calendar scheidingsDatum = gezin.getScheidingsdatum();
            
            if(huwelijksDatum != null) {
                tfGetrouwdOp.setText(StringUtilities.datumString(huwelijksDatum));
            }
            if(scheidingsDatum != null) {
                tfGescheidenOp.setText(StringUtilities.datumString(scheidingsDatum));
            }
            
            lvKinderen.setItems(FXCollections.observableArrayList(gezin.getKinderen()));
        }
    }

    public void setHuwdatum(Event evt) {
        Calendar cal;
        Gezin gezin;
        try {
            gezin = getAdministratie().getGezin(Integer.parseInt(tfGezinNummer.getText()));
            cal = StringUtilities.datum(tfGetrouwdOp.getText());
        }
        catch (IllegalArgumentException IAE) {
            showDialog("Incorrecte Invoer", "Ongeldige datum, controleer de datum");
            return;
        }
        
        if(gezin != null && cal != null) {
            if(getAdministratie().setHuwelijk(gezin, cal)) {
                showGezin(gezin);
                showDialog("Nieuwe datum toegevoed", "Nieuwe datum succesvol toegevoegd");
            }
            else {
                showDialog("Er is iets fout gegaan", "De nieuwe datum is niet toegevoegd");
            }
        }
        else {
            showDialog("Er is iets fout gegaan", "Gezin of datum was fout");
        }
    }

    public void setScheidingsdatum(Event evt) {
        Calendar cal;
        Gezin gezin;
        
        try {
            gezin = getAdministratie().getGezin(Integer.parseInt(tfGezinNummer.getText()));
            cal = StringUtilities.datum(tfGescheidenOp.getText());
        }
        catch (IllegalArgumentException IAE) {
            showDialog("Incorrecte Invoer", "Ongeldige datum, controleer de datum");
            return;
        }
        
        if(gezin != null && cal != null) {
            if(getAdministratie().setScheiding(gezin, cal)) {
                showGezin(gezin);
                showDialog("Nieuwe datum toegevoed", "Nieuwe datum succesvol toegevoegd");
            }
            else {
                showDialog("Er is iets fout gegaan", "De nieuwe datum is niet toegevoegd");
            }
        }
        else {
            showDialog("Er is iets fout gegaan", "Gezin of datum was fout");
        }
    }

    public void cancelPersoonInvoer(Event evt) {
        clearTabPersoonInvoer();
    }

    public void okPersoonInvoer(Event evt) {
        String voornamen = tfInvoerVoornamen.getText();
        String tussenvoegsel = tfInvoerTussenvoegsel.getText();
        String achternaam = tfInvoerAchternaam.getText();
        String geboortePlaats = tfInvoerGeboortePlaats.getText();
        Geslacht geslacht = (Geslacht)cbInvoerGeslacht.getSelectionModel().getSelectedItem();
        Gezin gezin = (Gezin)cbInvoerOuderlijkGezin.getSelectionModel().getSelectedItem();
        Calendar geboorteDatum = null;
        
        try {
            geboorteDatum = StringUtilities.datum(tfInvoerGeboorteDatum.getText());
        }
        catch (IllegalArgumentException IAE) {
            showDialog("Ongeldige datum", "Datum incorrect");
        }
        
        if(voornamen.isEmpty() || achternaam.isEmpty() || geboortePlaats.isEmpty() || geboorteDatum == null) {
            showDialog("Ongeldige gegevens", "Een van de velden is niet (goed) ingevuld");
        }
         
        String[] vnamen = voornamen.split("\\s+");
        
        Persoon persoon = getAdministratie().addPersoon(geslacht, vnamen, achternaam, tussenvoegsel, geboorteDatum, geboortePlaats, gezin);
        
        if(persoon != null) {
            showDialog("Succes", "De persoon is toegevoegd");
        }
        else {
            showDialog("Fout", "Er is iets fout gegaan");
        }
        
        clearTabPersoonInvoer();
        initComboboxes();
    }

    public void okGezinInvoer(Event evt) {
        Persoon ouder1 = (Persoon) cbOuder1Invoer.getSelectionModel().getSelectedItem();
        if (ouder1 == null) {
            showDialog("Warning", "eerste ouder is niet ingevoerd");
            return;
        }
        Persoon ouder2 = (Persoon) cbOuder2Invoer.getSelectionModel().getSelectedItem();
        Calendar huwdatum;
        try {
            huwdatum = StringUtilities.datum(tfHuwelijkInvoer.getText());
        } catch (IllegalArgumentException exc) {
            showDialog("Warning", "huwelijksdatum :" + exc.getMessage());
            return;
        }
        Gezin g;
        if (huwdatum != null) {
            g = getAdministratie().addHuwelijk(ouder1, ouder2, huwdatum);
            if (g == null) {
                showDialog("Warning", "Invoer huwelijk is niet geaccepteerd");
            } else {
                Calendar scheidingsdatum;
                try {
                    scheidingsdatum = StringUtilities.datum(tfScheidingInvoer.getText());
                    if(scheidingsdatum != null){
                        getAdministratie().setScheiding(g, scheidingsdatum);
                    }
                } catch (IllegalArgumentException exc) {
                    showDialog("Warning", "scheidingsdatum :" + exc.getMessage());
                }
            }
        } else {
            g = getAdministratie().addOngehuwdGezin(ouder1, ouder2);
            if (g == null) {
                showDialog("Warning", "Invoer ongehuwd gezin is niet geaccepteerd");
            }
        }

        clearTabGezinInvoer();
        initComboboxes();
    }

    public void cancelGezinInvoer(Event evt) {
        clearTabGezinInvoer();
    }

    
    public void showStamboom(Event evt) {
        if(!tfPersoonNr.getText().isEmpty()) {
            Persoon persoon = getAdministratie().getPersoon(Integer.parseInt(tfPersoonNr.getText()));
            
            TreeItem<String> tree = CreateTree(persoon, null);
            
            TreeView treeView = new TreeView(tree);

            Scene scene = new Scene(treeView, 500, 500);
            Stage stage = new Stage();
            stage.setTitle("Stamboom");
            stage.setScene(scene);

            stage.show();
        }
        
    }
    
    public TreeItem<String> CreateTree(Persoon persoon, TreeItem parentBranch) {
        TreeItem<String> branch = new TreeItem<>(persoon.getNaam());
        
        if(parentBranch != null) {
            parentBranch.getChildren().add(branch);
        }
        
        if(persoon.getOuderlijkGezin() == null)
        {
            return branch;
        }
        
        Persoon ouder1 = persoon.getOuderlijkGezin().getOuder1();
        Persoon ouder2 = persoon.getOuderlijkGezin().getOuder2();
        
        if(ouder1 != null) {
            CreateTree(ouder1, branch);
        }
        
        if(ouder2 != null) {
            CreateTree(ouder2, branch);
        }
        
        return branch;
    }
    
    
    public TreeItem<String> makeBranch(String title, TreeItem<String> parent) {
        TreeItem<String> branch = new TreeItem<String>();
        parent.getChildren().add(branch);
        
        return branch;
    }

    public void createEmptyStamboom(Event evt) {
        this.clearAdministratie();
        clearTabs();
        initComboboxes();
    }

    
    public void openStamboom(Event evt) throws IOException {
        this.loadFromDatabase();
        showDialog("MELDING:", "Stamboom ingeladen!");
        getAdministratie().setObservableLists();
        initComboboxes();
    }
    
    public void saveStamboom(Event evt) throws IOException {
        
        this.saveToDatabase();
        showDialog("MELDING:", "Stamboom opgeslagen!");
    }
    
    public void closeApplication(Event evt) throws IOException {
        saveStamboom(evt);
        getStage().close();
    }
   
    public void configureStorage(Event evt) {
        withDatabase = cmDatabase.isSelected();
    }
 
    public void selectTab(Event evt) {
        Object source = evt.getSource();
        if (source == tabPersoon) {
            clearTabPersoon();
        } else if (source == tabGezin) {
            clearTabGezin();
        } else if (source == tabPersoonInvoer) {
            clearTabPersoonInvoer();
        } else if (source == tabGezinInvoer) {
            clearTabGezinInvoer();
        }
    }

    private void clearTabs() {
        clearTabPersoon();
        clearTabPersoonInvoer();
        clearTabGezin();
        clearTabGezinInvoer();
    }

    
    private void clearTabPersoonInvoer() {
        tfInvoerVoornamen.clear();
        tfInvoerTussenvoegsel.clear();
        tfInvoerAchternaam.clear();
        cbInvoerGeslacht.getSelectionModel().clearSelection();
        tfInvoerGeboorteDatum.clear();
        tfInvoerGeboortePlaats.clear();
        cbInvoerOuderlijkGezin.getSelectionModel().clearSelection();
    }
    
    private void clearTabGezinInvoer() {
        cbOuder1Invoer.getSelectionModel().clearSelection();
        cbOuder2Invoer.getSelectionModel().clearSelection();
        tfHuwelijkInvoer.clear();
        tfScheidingInvoer.clear();
    }

    private void clearTabPersoon() {
        cbPersonen.getSelectionModel().clearSelection();
        tfPersoonNr.clear();
        tfVoornamen.clear();
        tfTussenvoegsel.clear();
        tfAchternaam.clear();
        tfGeslacht.clear();
        tfGebDatum.clear();
        tfGebPlaats.clear();
        cbOuderlijkGezin.getSelectionModel().clearSelection();
        lvAlsOuderBetrokkenBij.setItems(FXCollections.emptyObservableList());
    }
    
    private void clearTabGezin() {
        tfGezinNummer.clear();
        tfOuder1.clear();
        tfOuder2.clear();
        tfGetrouwdOp.clear();
        tfGescheidenOp.clear();
        lvKinderen.setItems(FXCollections.emptyObservableList());
    }

    private void showDialog(String type, String message) {
        Stage myDialog = new Dialog(getStage(), type, message);
        myDialog.show();
    }

    private Stage getStage() {
        return (Stage) menuBar.getScene().getWindow();
    }

}
