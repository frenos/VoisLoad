package de.codepotion.voisload;

import java.io.File;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MainApp extends Application {

	private Stage primaryStage;
	private ObservableList<Beitrag> beitraege = FXCollections.observableArrayList();

	public MainApp() {
	}

	/**
	 * Getter fuer Beitrags-XML
	 * 
	 * @return Xml-Datei mit Beitraegen
	 */
	public File getBeitragXmlPath() {
		String file = "beitraege.xml";
		return new File(file);
	}

	/**
	 * Getter fuer die BeitragsListe
	 * 
	 * @return Liste aller Beitraege
	 */
	public ObservableList<Beitrag> getBeitraege() {
		return beitraege;
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("VoisLoad!");
		try {
			Parent root = FXMLLoader.load(getClass().getResource("maingui.fxml"));
			Scene scene = new Scene(root);

			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Getter fuer die primaryStage
	 * 
	 * @return primaryStage der Anwendung
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}
}