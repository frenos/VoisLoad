package de.codepotion.voisload;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

public class Vois {
	private static int aktuellsterBeitrag;
	private static int maxSchrittweite = 5;
	private static String downloadPath;
	private static ObservableList<Beitrag> alleBeitraege = FXCollections.observableArrayList();
	private static File dataFile = new File("beitraege.xml");
	private ExecutorService executor;
	private HttpClient httpClient;

	// root
	@FXML
	private Accordion accordionView;
	@FXML
	private TitledPane beitragPane;
	@FXML
	private TitledPane downloadsPane;
	// main tab
	@FXML
	private TableView<Beitrag> myTable;
	@FXML
	private TableColumn<Beitrag, String> titel;
	@FXML
	private TableColumn<Beitrag, Number> beitragNummer;
	@FXML
	private TableColumn<Beitrag, String> kategorie;
	@FXML
	private Button updateButton;
	@FXML
	private ProgressIndicator updateProgress;

	// Settings tab
	@FXML
	private TextField settings_beitrag;
	@FXML
	private Button settings_setbeitrag;
	@FXML
	private TextField settings_dlFolder;
	// Downloads tab
	@FXML
	TableView<DownloadTask> downloadsTable;
	@FXML
	TableColumn<DownloadTask, String> titelCol;
	@FXML
	TableColumn<DownloadTask, String> statusCol;
	@FXML
	TableColumn<DownloadTask, Double> progressCol;

	public Vois() {
		Preferences prefs = Preferences.userNodeForPackage(Vois.class);
		aktuellsterBeitrag = prefs.getInt("aktuellsterBeitrag", 0);
		downloadPath = prefs.get("downloadPath", null);
		/*
		 * Wenn kein Downloadordner auf dem System konfiguriert wurde zeige
		 * Directorychooser
		 */
		if (downloadPath == null) {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("DownloadOrdner wählen:");
			downloadPath = chooser.showDialog(null).getPath();
			prefs.put("downloadPath", downloadPath);
		}
		try {
			dataFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		executor = Executors.newFixedThreadPool(1, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(false);
				return t;
			}
		});
		httpClient = HttpClients.createDefault();
	}

	@FXML
	private void initialize() {
		/* Zellen an die jeweilige Property der Kapselklasse Beitrag binden */
		titel.setCellValueFactory(cellData -> cellData.getValue().getTitelProp());
		beitragNummer.setCellValueFactory(cellData -> cellData.getValue().getNummerProp());
		kategorie.setCellValueFactory(cellData -> cellData.getValue().getKategorieProp());
		/* Beitraege einlesen von XML (wenn vorhanden) */
		loadBeitragDataFromFile(dataFile);

		myTable.setItems(Vois.alleBeitraege);
		myTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		settings_beitrag.setText(String.valueOf(aktuellsterBeitrag));
		settings_dlFolder.setText(downloadPath);

		titelCol.setCellValueFactory(new PropertyValueFactory<DownloadTask, String>("Titel"));
		statusCol.setCellValueFactory(new PropertyValueFactory<DownloadTask, String>("message"));
		progressCol.setCellValueFactory(new PropertyValueFactory<DownloadTask, Double>("progress"));
		progressCol.setCellFactory(ProgressBarTableCell.<DownloadTask> forTableColumn());

		accordionView.setExpandedPane(beitragPane);
	}

	public ObservableList<Beitrag> getBeitragList() {
		return alleBeitraege;
	}

	public void toggleUpdateProgress() {
		updateProgress.setVisible(!updateProgress.isVisible());
	}

	@FXML
	public void saveAktuellsterBeitrag() {
		Preferences prefs = Preferences.userNodeForPackage(Vois.class);
		prefs.putInt("aktuellsterBeitrag", Integer.valueOf(settings_beitrag.getText()));
	}

	@FXML
	public void chooseDownloadPath() {
		Preferences prefs = Preferences.userNodeForPackage(Vois.class);
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("DownloadOrdner wählen:");
		downloadPath = chooser.showDialog(null).getPath();
		settings_dlFolder.setText(downloadPath);
		prefs.put("downloadPath", downloadPath);
	}

	@FXML
	void startDownloads() {
		ObservableList<Beitrag> selectedItems = myTable.getSelectionModel().getSelectedItems();
		for (Beitrag beitrag : selectedItems) {
			String filename = String.valueOf(beitrag.getNummer()) + " - " + beitrag.getTitel();
			downloadsTable.getItems().add(new DownloadTask(beitrag.getUrl(), downloadPath, filename, httpClient));
		}

		for (DownloadTask task : downloadsTable.getItems()) {
			executor.execute(task);
		}
		downloadsPane.setExpanded(true);
	}

	@FXML
	public void updateBeitraege() {
		Timer BackgroundServ = new Timer();
		BackgroundServ.schedule(new TimerTask() {

			@Override
			public void run() {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						toggleUpdateProgress();
					}
				});
				Vois.getNewBeitraege(aktuellsterBeitrag + 1);
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						toggleUpdateProgress();
					}
				});
			}
		}, 0);
	}

	public static void getNewBeitraege(int startNummer) {
		String baseUrl = "http://www.vois.tv/?vid=";
		int failcount = 0;
		int beitragNummer = startNummer;
		while (failcount < maxSchrittweite) {
			try {
				Content myContent = Request.Get(baseUrl + beitragNummer).execute().returnContent();
				if (!myContent.asString().startsWith("<br />")) {
					failcount = 0;
					Pattern p = Pattern.compile("OverlayPlayer.getInstance\\(\\).openPlayer\\(\\{(.*)\\},",
							Pattern.DOTALL);
					Matcher m = p.matcher(myContent.asString());
					/*
					 * Für ältere Videos wird dem Videoplayer ein Objekt im
					 * JSON-Format übergeben. Wenn es existiert wird die
					 * Information ausgelesen und das JSON geparst.
					 */
					if (m.find()) {
						String description = m.group(1);
						description = Helper.htmlTranslate(description);
						JSONObject beitrag = new JSONObject("{" + description + "}");
						Beitrag neuBeitrag = new Beitrag(beitrag.getInt("id"), beitrag.getString("title"),
								"http://vois.vor-ort-in-steinfurt.de/cms/output/flv/pass_video.php?vid="
										+ beitrag.getInt("id") + "&type=CONTENT&format=10",
								beitrag.getString("channel_name"));
						if (!alleBeitraege.contains(neuBeitrag))
							alleBeitraege.add(neuBeitrag);
					}
					/*
					 * Für neuere Videos gibt es nur den Workaround die
					 * Informationen über den Seitentitel zu bekommen. Format
					 * ist dabei: <basetitel> | <Kategorie> |<Titel d. Beitrag>
					 */
					else {
						p = Pattern.compile("<title>(.*)</title>");
						m = p.matcher(myContent.asString());
						if (m.find()) {
							String data[] = m.group(1).split("\\|");
							data[2] = Helper.htmlTranslate(data[2]);
							Beitrag neuBeitrag = new Beitrag(beitragNummer, data[2].trim(),
									"http://vois.vor-ort-in-steinfurt.de/cms/output/flv/pass_video.php?vid="
											+ beitragNummer + "&type=CONTENT&format=10", data[1].trim());

							if (!alleBeitraege.contains(neuBeitrag))
								alleBeitraege.add(neuBeitrag);
						}
					}
					aktuellsterBeitrag = beitragNummer;
				} else {
					failcount++;
				}
				beitragNummer++;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		/* Die neuen Beiträge in der XML persistent speichern. */
		saveBeitragDataToFile(dataFile);
		Preferences prefs = Preferences.userNodeForPackage(Vois.class);
		prefs.putInt("aktuellsterBeitrag", aktuellsterBeitrag);
		/*
		 * Schrittweite verdoppeln. Es sind immer wieder Beitragsnummern nicht
		 * belegt. Default wird maximal 5 Beiträge weit gesucht. Hiermit kann
		 * duch wiederholtes Klicken immer weiter gesucht werden.
		 */
		maxSchrittweite *= 2;
	}

	/**
	 * Lädt Beiträge von der angegebenen Datei und füllt Beitragsliste.
	 * 
	 * @param file
	 *            Datei von der gelesen werden soll
	 */
	public static void loadBeitragDataFromFile(File file) {
		try {
			JAXBContext context = JAXBContext.newInstance(BeitragListWrapper.class);
			Unmarshaller um = context.createUnmarshaller();

			BeitragListWrapper wrapper = (BeitragListWrapper) um.unmarshal(file);
			alleBeitraege.clear();
			alleBeitraege.addAll(wrapper.getBeitraege());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Speichert die Beitraege mittels JAXB auf die angegebene Datei.
	 * 
	 * @param file
	 *            Datei in die geschrieben werden soll
	 */
	public static void saveBeitragDataToFile(File file) {
		try {
			JAXBContext context = JAXBContext.newInstance(BeitragListWrapper.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			BeitragListWrapper wrapper = new BeitragListWrapper();
			wrapper.setBeitraege(alleBeitraege);

			m.marshal(wrapper, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
