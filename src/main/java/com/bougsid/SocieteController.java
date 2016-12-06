package com.bougsid;

import com.bougsid.entities.Ordre;
import com.bougsid.entities.Societe;
import com.bougsid.metier.ISocieteMetier;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Map;

public class SocieteController {
    @FXML
    private TextField searchTextField;

    @FXML
    private TableView<Societe> societeTable;

    @FXML
    private TableColumn<Societe, String> codeColumn;

    @FXML
    private TableColumn<Societe, String> nameColumn;

    @FXML
    private TableView<Ordre> ordreTable;

    @FXML
    private TableColumn<Ordre, Number> actionsCountColumn;

    @FXML
    private TableColumn<Ordre, Number> actionPriceColumn;

    @FXML
    private TableColumn<Ordre, String> dateColumn;

    @FXML
    private Label totalAchat;

    @FXML
    private Label totalVente;

    @FXML
    private Label AVGAchat;

    @FXML
    private Label AVGVente;

    @FXML
    private Label estimation;

    private ISocieteMetier societeMetier;
    private ObservableList<Societe> societes;
    private static final String RMI_SERVICE_URL = "rmi://localhost:1999/societeRmiService";

    @FXML
    private void initialize() throws RemoteException, NotBoundException, MalformedURLException {
        //Set cell factory for SocieteTable
        this.setSocieteTableCellFactory();
        //Get Societe List
        this.getSocieteList();
        //Set Cell Factory fo OrdreTable
        this.setOrdreTableCellFactory();
        //Add listner to row clicked
        this.societeTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.updateOrdresAndInfos(newValue);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void setOrdreTableCellFactory() {
        this.actionsCountColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getActionsCount()));
        this.actionPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getActionPrice()));
        this.dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
    }

    private void setSocieteTableCellFactory() {
        this.codeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode()));
        this.nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
    }

    private void getSocieteList() throws RemoteException, NotBoundException, MalformedURLException {
        this.societeMetier = (ISocieteMetier) Naming.lookup(RMI_SERVICE_URL);
        this.societes = FXCollections.observableArrayList(societeMetier.findAll(0, 20).getContent());
        this.societeTable.setItems(this.societes);
    }

    private void updateOrdresAndInfos(Societe societe) throws RemoteException {
        if (societe == null) return;
        this.ordreTable.setItems(FXCollections.observableList(this.societeMetier.getOrdresPage(societe.getCode(), 0, 5).getContent()));
        Map<String, Double> infos = this.societeMetier.getAllInfos(societe.getCode());
        DecimalFormat df = new DecimalFormat("#.##");
        this.totalAchat.setText(String.valueOf(df.format(infos.get("totalAchat"))));
        this.totalVente.setText(String.valueOf(df.format(infos.get("totalVente"))));
        this.AVGAchat.setText(String.valueOf(df.format(infos.get("AVGAchat"))));
        this.AVGVente.setText(String.valueOf(df.format(infos.get("AVGVente"))));
        this.estimation.setText(String.valueOf(df.format(infos.get("estimation"))));
    }

    @FXML
    void search() throws RemoteException {
        String code = this.searchTextField.getText();
        this.societeTable.getSelectionModel().clearSelection();
        this.updateOrdresAndInfos(this.societeMetier.findSocieteByCode(code));
    }
}
