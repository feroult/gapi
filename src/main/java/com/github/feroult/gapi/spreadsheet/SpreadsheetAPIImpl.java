package com.github.feroult.gapi.spreadsheet;

import com.github.feroult.gapi.BatchOptions;
import com.github.feroult.gapi.SpreadsheetAPI;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class SpreadsheetAPIImpl implements SpreadsheetAPI {

//	private static final int MAX_BATCH_ROWS = 500;

	private Sheets sheetsService;
	private Spreadsheet spreadsheet;
	private Sheet worksheet;

	SpreadsheetAPIImpl(Sheets sheetsService) {
//		this.spreadsheetService.setConnectTimeout(4 * 60 * 1000);
		this.sheetsService = sheetsService;
	}

	@Override
	public SpreadsheetAPI key(String key) {
		if (spreadsheet != null && spreadsheet.getSpreadsheetId().equals(key)) {
			return this;
		}

		try {
			spreadsheet = sheetsService.spreadsheets().get(key).execute();
			resetWorksheet();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return this;
	}

	private void resetWorksheet() {
		worksheet = null;
	}

	@Override
	public void setValue(int i, int j, String value) {
		List<List<Object>> values = Collections.singletonList(
				Collections.singletonList(value)
		);

		ValueRange body = new ValueRange().setValues(values);

		String range = getRange(i, j);

		try {
			sheetsService.spreadsheets().values()
					.update(spreadsheet.getSpreadsheetId(), range, body)
					.setValueInputOption("USER_ENTERED")
					.setIncludeValuesInResponse(true)
					.execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getValue(int i, int j) {
		String range = getRange(i, j);

		ValueRange response;
		try {
			response = sheetsService.spreadsheets().values()
					.get(spreadsheet.getSpreadsheetId(), range)
					.execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<List<Object>> values = response.getValues();
		return values.get(0).get(0).toString();
	}

	private String getRange(int row, int col) {
		return worksheet.getProperties().getTitle() + "!" + (char)(64 + col) + row;
	}

	@Override
	public SpreadsheetAPI worksheet(String title) {
		try {
			worksheet = getWorksheetByTitle(title);

			if (worksheet == null) {
				worksheet = createWorksheet(title);
			}

			return this;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasWorksheet(String title) {
		try {
			worksheet = getWorksheetByTitle(title);

			return worksheet != null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private Sheet createWorksheet(String title) throws IOException {
		AddSheetRequest addSheetRequest = new AddSheetRequest()
			.setProperties(
				new SheetProperties()
					.setTitle(title)
					.setGridProperties(
						new GridProperties()
							.setColumnCount(10)
							.setRowCount(10)
					)
			);

		List<Request> requests = new ArrayList<>();
		requests.add(new Request().setAddSheet(addSheetRequest));

		BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
		sheetsService.spreadsheets().batchUpdate(spreadsheet.getSpreadsheetId(), body).execute();
		return getWorksheetByTitle(title);
	}

	private Sheet getWorksheetByTitle(String title) throws IOException {
		Sheets.Spreadsheets.Get request = sheetsService.spreadsheets().get(spreadsheet.getSpreadsheetId());
		Spreadsheet response = request.execute();
		List<Sheet> filteredSheets = response.getSheets().stream()
				.filter(sheet -> sheet.getProperties().getTitle().equals(title)).collect(Collectors.toList());

		if (filteredSheets.isEmpty()) {
			return null;
		}
		return filteredSheets.get(0);
	}

	@Override
	public void batch(SpreadsheetBatch batch, BatchOptions... options) {
		try {
			adjustWorksheetDimensions(batch, options);

			ValueRange body = new ValueRange()
					.setValues(batchToList(batch));

			sheetsService.spreadsheets().values()
					.update(spreadsheet.getSpreadsheetId(), worksheet.getProperties().getTitle(), body)
					.setValueInputOption("RAW")
					.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<List<Object>> batchToList(SpreadsheetBatch batch) {
		List<List<Object>> ret = new ArrayList<>();

		for (int i = 1; i <= batch.rows(); i++) {
			List<Object> row = new ArrayList<>();
			for (int j = 1; j <= batch.cols(); j++) {
				row.add(batch.getValue(i, j));
			}
			ret.add(row);
		}

		return ret;
	}

	private void adjustWorksheetDimensions(SpreadsheetBatch updateSet, BatchOptions... options) throws IOException {
		int rowCount = worksheet.getProperties().getGridProperties().getRowCount();
		int columnCount = worksheet.getProperties().getGridProperties().getColumnCount();

		if (columnCount == updateSet.cols() && rowCount == updateSet.rows()) {
			return;
		}

		if (columnCount < updateSet.cols()) {
			columnCount = updateSet.cols();
		}

		if (columnCount > updateSet.cols() && BatchOptions.SHRINK.on(options)) {
			columnCount = updateSet.cols();
		}

		if (rowCount < updateSet.rows()) {
			rowCount = updateSet.rows();
		}

		if (rowCount > updateSet.rows() && BatchOptions.SHRINK.on(options)) {
			rowCount = updateSet.rows();
		}

		UpdateSheetPropertiesRequest updateSheetPropertiesRequest = new UpdateSheetPropertiesRequest()
			.setFields("*")
			.setProperties(new SheetProperties()
				.setTitle(worksheet.getProperties().getTitle())
				.setSheetId(worksheet.getProperties().getSheetId())
				.setGridProperties(new GridProperties()
					.setRowCount(rowCount)
					.setColumnCount(columnCount)
				)
			);

		List<Request> requests = new ArrayList<>();
		requests.add(new Request().setUpdateSheetProperties(updateSheetPropertiesRequest));

		BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
		sheetsService.spreadsheets().batchUpdate(spreadsheet.getSpreadsheetId(), body).execute();
	}

	@Override
	public List<Map<String, String>> asMap() {
		String range = worksheet.getProperties().getTitle();

		ValueRange response;
		try {
			response = sheetsService.spreadsheets().values()
					.get(spreadsheet.getSpreadsheetId(), range)
					.execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<List<Object>> body = response.getValues();
		List<Object> header = body.get(0);
		body.remove(0);
		return listToMap(header, body);
	}

	private List<Map<String, String>> listToMap(List<Object> header, List<List<Object>> body) {
		List<Map<String, String>> ret = new ArrayList<>();

		if (body.isEmpty()) {
			for (Object item : header) {
				Map<String, String> row = new HashMap<>();
				row.put(item.toString(), "");

				ret.add(row);
			}
		} else {
			for (List<Object> objects : body) {
				Map<String, String> row = new HashMap<>();
				for (int j = 0; j < header.size(); j++) {
					String value = "";
					if (objects.size() - 1 >= j) {
						value = objects.get(j).toString();
					}
					row.put(header.get(j).toString(), value);
				}
				ret.add(row);
			}
		}
		return ret;
	}
}
