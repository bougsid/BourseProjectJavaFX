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
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.springframework.data.domain.Page;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Map;

public class SocieteController {
    private static final int MAX_SOCIETE_ITEMS = 4;
    private static final int MAX_ORDRE_ITEMS = 10;
    @FXML
    private TextField searchTextField;

    @FXML
    private TableView<Societe> societeTable;

    @FXML
    private Pagination pagination;

    @FXML
    private Pagination ordrePagination;

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
    private Societe selectedSociete;
    private static final String RMI_SERVICE_URL = "rmi://localhost:1999/societeRmiService";

    @FXML
    private void initialize() throws RemoteException, NotBoundException, MalformedURLException {
        this.societeMetier = (ISocieteMetier) Naming.lookup(RMI_SERVICE_URL);
        this.configurePagination();
        //Set cell factory for SocieteTable
        this.setSocieteTableCellFactory();
        //Get Societe List
        this.getSocieteList(0);
        //Pagination factory
        this.configurePaginationPageFactory();
        //Set Cell Factory fo OrdreTable
        this.setOrdreTableCellFactory();
        //Add listner to row clicked
        this.societeTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.selectedSociete = newValue;
                this.updateOrdresAndInfos(0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void configurePaginationPageFactory(){
        //Configure pagination
        this.pagination.setPageFactory(param -> {
            this.getSocieteList(param);
            return new BorderPane(societeTable);
        });
        this.ordrePagination.setPageFactory(param -> {
            this.updateOrdre(param);
            return new BorderPane(ordreTable);
        });
    }
    private void setOrdreTableCellFactory() {
        this.actionsCountColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getActionsCount()));
        this.actionPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getActionPrice()));
        this.dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
        this.ordreTable.setRowFactory(tv -> new TableRow<Ordre>() {
            @Override
            public void updateItem(Ordre item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setStyle("");
                } else if (item.getType().equals("Achat")) {
                    setStyle("-fx-background-color: green;");
                } else {
                    setStyle("-fx-background-color: red;");
                }
            }
        });
    }

    private void setSocieteTableCellFactory() {
        this.codeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCode()));
        this.nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
    }

    private void getSocieteList(int page) {
        System.out.println("Page = " + page);
        try {
            this.societes = FXCollections.observableArrayList(this.societeMetier.findAll(page, MAX_SOCIETE_ITEMS).getContent());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.societeTable.setItems(this.societes);
        this.societeTable.getSelectionModel().select(0);
        this.selectedSociete = this.societeTable.getSelectionModel().getSelectedItem();
    }

    private void configurePagination() throws RemoteException {
        this.pagination.setPageCount(this.societeMetier.findAll(0, MAX_SOCIETE_ITEMS).getTotalPages());
    }

    private void updateOrdresAndInfos(int page) throws RemoteException {
        if (selectedSociete == null) return;
        this.updateOrdre(page);
        Map<String, Double> infos = this.societeMetier.getAllInfos(selectedSociete.getCode());
        DecimalFormat df = new DecimalFormat("#.##");
        this.totalAchat.setText(String.valueOf(df.format(infos.get("totalAchat"))));
        this.totalVente.setText(String.valueOf(df.format(infos.get("totalVente"))));
        this.AVGAchat.setText(String.valueOf(df.format(infos.get("AVGAchat"))));
        this.AVGVente.setText(String.valueOf(df.format(infos.get("AVGVente"))));
        this.estimation.setText(String.valueOf(df.format(infos.get("estimation"))));
    }

    private void updateOrdre(int page) {
        Page<Ordre> ordrePage = null;
        try {
            ordrePage = this.societeMetier.getOrdresPage(selectedSociete.getCode(), page, MAX_ORDRE_ITEMS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        int totalPages = ordrePage.getTotalPages();
        if (page == 0)
            this.configureOrdrePagination(totalPages);
        this.ordreTable.setItems(FXCollections.observableList(ordrePage.getContent()));
    }

    private void configureOrdrePagination(int totalPages) {
        this.ordrePagination.setPageCount(totalPages);
        this.ordrePagination.setCurrentPageIndex(0);
    }

    @FXML
    void search() throws RemoteException {
        String code = this.searchTextField.getText();
        this.societeTable.getSelectionModel().clearSelection();
        this.selectedSociete = this.societeMetier.findSocieteByCode(code);
        this.updateOrdresAndInfos(0);
    }
}
