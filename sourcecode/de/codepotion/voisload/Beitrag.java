package de.codepotion.voisload;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Datenklasse um die Informationen zu einem Beitrag gekapselt zu sammeln.
 * 
 * @author frenos
 *
 */
public class Beitrag {
	private final StringProperty titel;
	private final SimpleIntegerProperty nummer;
	private final StringProperty url;
	private final StringProperty kategorie;

	public Beitrag() {
		this(0, null, null, null);
	}

	public Beitrag(int _nummer, String _titel, String _url, String _kategorie) {
		this.nummer = new SimpleIntegerProperty(_nummer);
		this.titel = new SimpleStringProperty(_titel);
		this.url = new SimpleStringProperty(_url);
		this.kategorie = new SimpleStringProperty(_kategorie);
	}

	public int getNummer() {
		return nummer.get();
	}

	public void setNummer(int nummer) {
		this.nummer.set(nummer);
	}

	public IntegerProperty getNummerProp() {
		return nummer;
	}

	public String getTitel() {
		return titel.get();
	}

	public void setTitel(String title) {
		this.titel.set(title);
	}

	public StringProperty getTitelProp() {
		return titel;
	}

	public String getUrl() {
		return url.get();
	}

	public void setUrl(String url) {
		this.url.set(url);
	}

	public StringProperty getUrlProp() {
		return url;
	}

	public String getKategorie() {
		return kategorie.get();
	}

	public void setKategorie(String kategorie) {
		this.kategorie.set(kategorie);
	}

	public StringProperty getKategorieProp() {
		return kategorie;
	}
}
