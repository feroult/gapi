package com.github.feroult.gapi.spreadsheet;

import com.github.feroult.gapi.SpreadsheetAPI;
import com.google.api.services.sheets.v4.Sheets;
import com.google.gdata.client.spreadsheet.SpreadsheetService;

public final class SpreadsheetAPIFactory {

	private SpreadsheetAPIFactory() {
		throw new RuntimeException("Should not be instanciated");
	}

	public static SpreadsheetAPI create(Sheets sheetsService) {
		return new SpreadsheetAPIImpl(sheetsService);
	}
}
