package com.github.feroult.gapi;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.feroult.gapi.spreadsheet.SpreadsheetBatch;

public interface SpreadsheetAPI {

	public static List<String> SCOPES = Arrays.asList("https://spreadsheets.google.com/feeds");

	public SpreadsheetAPI key(String key);

	public void setValue(int i, int j, String value);

	public String getValue(int i, int j);

	public SpreadsheetAPI worksheet(String title);

	public boolean hasWorksheet(String title);

	public void batch(SpreadsheetBatch batch, BatchOptions... options);

	public List<Map<String, String>> asMap();

}