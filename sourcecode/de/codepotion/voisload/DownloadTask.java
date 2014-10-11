package de.codepotion.voisload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public class DownloadTask extends Task<Void> {
	private final String url, localPath;
	private final StringProperty titel = new SimpleStringProperty();
	private final HttpClient myClient;

	private final int bufferSize = 1024;

	public DownloadTask(String url, String localPath, String _titel, HttpClient _client) {
		this.url = url;
		this.localPath = localPath;
		titel.setValue(_titel);
		myClient = _client;
		this.updateMessage("Waiting...");
	}

	public DownloadTask(String url, String localPath, String _titel) {
		this(url, localPath, _titel, HttpClients.createDefault());
	}

	public String getTitel() {
		return titel.getValue();
	}

	@Override
	protected Void call() throws Exception {

		this.updateMessage("Waiting...");
		this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
		try {
			HttpGet getRequest = new HttpGet(url);
			HttpResponse response = myClient.execute(getRequest);
			long fileSize = response.getEntity().getContentLength();
			InputStream remoteContentStream = response.getEntity().getContent();
			File localFile = new File(localPath, Helper.filenameTranslate(titel.getValue()) + "("
					+ LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ")" + ".mp4");
			OutputStream localFileStream = new FileOutputStream(localFile);
			byte[] buffer = new byte[bufferSize];

			int sizeOfChunk;
			int amountComplete = 0;
			this.updateMessage("Running...");
			while ((sizeOfChunk = remoteContentStream.read(buffer)) != -1) {
				localFileStream.write(buffer, 0, sizeOfChunk);
				amountComplete += sizeOfChunk;
				this.updateProgress(amountComplete, fileSize);
			}
			remoteContentStream.close();
			localFileStream.close();
			this.updateMessage("Done (" + Helper.humanReadableByteCount(fileSize, true) + ")");
			this.updateProgress(1, 1);
		} catch (Exception e) {

		}
		return null;
	}
}
