package de.codepotion.voisload;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Hilfsklasse um einige einfache Methoden ohne Instanzieren zu nutzen.
 **/
public class Helper {

	private static Map<String, String> TranslationTable = new HashMap<String, String>();
	private static boolean isPopulated = false;

	// wird vor jedem Translate gerufen um die Tabelle zu fuellen
	private static void populateTranslationTable() {
		if (!isPopulated) {
			// umlaute
			TranslationTable.put("&auml;", "ä");
			TranslationTable.put("&Auml;", "Ä");
			TranslationTable.put("&ouml;", "ö");
			TranslationTable.put("&Ouml;", "ö");
			TranslationTable.put("&uuml;", "ü");
			TranslationTable.put("&Uuml;", "Ü");
			TranslationTable.put("&szlig;", "ß");

			// zeichen
			TranslationTable.put("&quot;", "");
			TranslationTable.put("&amp;", "&");
			TranslationTable.put("&#039;", "'");
			TranslationTable.put("&acute;", "´");

		}

	}

	/**
	 * Demaskiert Zeichen aus HTML-Quellcode wieder in die korrekten Zeichen.
	 * Beispielsweise '&auml;' wieder in ä.
	 * 
	 * @param input
	 *            HTML-Source in dem demaskiert werden soll
	 * @return HTML-Source mit demaskierten Zeichen
	 */
	public static String htmlTranslate(String input) {
		populateTranslationTable();

		for (Entry<String, String> translation : TranslationTable.entrySet()) {
			input = input.replace(translation.getKey(), translation.getValue());
		}
		return input;

	}

	/**
	 * Nicht erlaubte Zeichen fuer Dateinamen (Windows) werden aus dem String
	 * gefiltert und durch '_' ersetzt.
	 * 
	 * @param input
	 *            String in dem gefiltert werden soll
	 * @return input mit ersetzen Zeichen
	 **/
	public static String filenameTranslate(String input) {
		input = input.replaceAll("[%\\.\"\\*/:<>\\?\\\\\\|\\+,\\.;=\\[\\]]", "_");
		return input;
	}

	/**
	 * convert to easy reading String, from
	 * http://stackoverflow.com/questions/3758606
	 * /how-to-convert-byte-size-into-human-readable-format-in-java
	 * 
	 * @param bytes
	 * @param si
	 * 
	 * @return
	 **/
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
