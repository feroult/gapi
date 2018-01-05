package com.github.feroult.gapi;

import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.gtt.DocumentEntry;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;

public class DocsAPI {

	private DocsService docsService;
	private DocumentEntry doc;

	public DocsAPI(DocsService docsService) {
		this.docsService = docsService;
	}

	public DocsAPI key(String key) {
		try {
			String spreadsheetURL = "https://spreadsheets.google.com/feeds/spreadsheets/" + key;
			doc = docsService.getEntry(new URL(spreadsheetURL), DocumentEntry.class);
			return this;
		} catch (ResourceNotFoundException e) {
			return null;
		} catch (IOException | ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		return doc.getTitle().getPlainText();
	}

	public String getText() {
		return doc.getPlainTextContent();
	}
}
