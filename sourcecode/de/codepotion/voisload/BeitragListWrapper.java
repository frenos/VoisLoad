package de.codepotion.voisload;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapperklasse um die Beitraege ueber JAXB direkt in xml persistent zu
 * speichern.
 * 
 * @author frenos
 *
 */
@XmlRootElement(name = "beitraege")
public class BeitragListWrapper {

	private List<Beitrag> beitraege = new ArrayList<Beitrag>();

	@XmlElement(name = "beitrag")
	public List<Beitrag> getBeitraege() {
		return beitraege;
	}

	public void setBeitraege(List<Beitrag> beitraege) {
		this.beitraege = beitraege;
	}

}
