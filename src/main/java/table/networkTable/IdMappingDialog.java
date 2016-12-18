package table.networkTable;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

enum SPECIES
{
	Human,
    Mouse,
    Rat,
    Frog,
    Zebra_fish,
    Fruit_fly,
    Mosquito,
    Worm,
    Arabidopsis_thaliana,
    Yeast,
    Escherichia_coli,
    Tuberculosis
};

enum DATABASE
{
	ENSEMBL,
	EMBL,
	Entrez_Gene,
	Gene_ID,
	GO,
	GenBank,
	Illumina,
	InterPro,
	MGI,
	PDB,
	RefSeq,
	UniGene,
	UNIPROT,
	UCSC_Genome_Browser
};


public class IdMappingDialog extends Dialog implements Initializable {

//	@FXML private Button okButton;
	@FXML private TextField targetName;
	@FXML private CheckBox forceSingle;
	@FXML private ComboBox<String> sourceChoices;
	@FXML private ComboBox<String> targetChoices;
	@FXML private ComboBox<String> speciesChoices;
	
	String targName = "";
	static String saveSpecies = null;
	static String saveSrc = null;
	static String saveTarg = null;
	static boolean saveForceSingle = false;
	
	public IdMappingDialog(String srcColumnName) {
		super();
		setTitle("ID Mapping");
		targName = "Mapped " + srcColumnName;

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("idmapper.fxml"));
		fxmlLoader.setController(this);
		getDialogPane().setPrefWidth(500 );
		getDialogPane().setPrefHeight(275);
		ButtonType okButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

		try {
			getDialogPane().getChildren().add((Parent) fxmlLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@FXML
	void onOkButtonAction(ActionEvent event) {
		close();
	}

	public String getName() {
		return targetName.getText();
	}

	public void doOK(ActionEvent ev) {
		System.out.println("doOK");
		
		targName = targetName.getText();
//		getDialogPane().
		saveSpecies = speciesChoices.getSelectionModel().getSelectedItem();
		saveSrc = sourceChoices.getSelectionModel().getSelectedItem();
		saveTarg = targetChoices.getSelectionModel().getSelectedItem();
		saveForceSingle = forceSingle.isSelected();
		close();

	}

	public void doCancel(ActionEvent ev) {
		System.out.println("doCancel");
		close();

	}

	/*
	 * Called when FXML file is load()ed (via FXMLLoader.load()). It will
	 * execute before the form is shown.
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		ObservableList<String> srcList = FXCollections.observableArrayList();
		ObservableList<String> targList = FXCollections.observableArrayList();
		ObservableList<String> speciesList = FXCollections.observableArrayList();
//		String[] databases = DATABASE.values();
//		for (String db : databases)
//		{
//			srcList.add(db);
//			targList.add(db);
//		}
		
		for (SPECIES s : SPECIES.values())
		{
			speciesList.add(s.toString());
		}
//		for (String s : getSpeciesList())
//			speciesList.add(s);
		speciesChoices.setItems(speciesList);
//		speciesChoices.onInputMethodTextChangedProperty(e -> {});
		sourceChoices.setItems(srcList);
		targetChoices.setItems(targList);
		
		if (saveSpecies == null) saveSpecies =  speciesList.get(0);
		speciesChoices.getSelectionModel().select(saveSpecies);
		if (saveSrc == null) saveSrc = "Entrez_Gene";
		sourceChoices.getSelectionModel().select(saveSrc);
		if (saveTarg == null) saveTarg = "Ensembl";
		targetChoices.getSelectionModel().select(saveTarg);
		targetName.setText(targName);
		
	}

}