package com.github.feroult.gapi;

import com.github.feroult.gapi.spreadsheet.SpreadsheetBatch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface SpreadsheetAPI {

	List<String> SCOPES = Arrays.asList("https://spreadsheets.google.com/feeds");

	SpreadsheetAPI key(String key);

	void setValue(int i, int j, String value);

	String getValue(int i, int j);

	SpreadsheetAPI worksheet(String title);

	boolean hasWorksheet(String title);

	void batch(SpreadsheetBatch batch, BatchOptions... options);

	List<Map<String, String>> asMap();

}