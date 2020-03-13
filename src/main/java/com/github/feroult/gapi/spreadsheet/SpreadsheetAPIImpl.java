package com.github.feroult.gapi.spreadsheet;

import com.github.feroult.gapi.BatchOptions;
import com.github.feroult.gapi.SpreadsheetAPI;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.WorksheetQuery;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

class SpreadsheetAPIImpl implements SpreadsheetAPI {

	private static final int MAX_BATCH_ROWS = 500;

	private SpreadsheetService spreadsheetService;
	private SpreadsheetEntry spreadsheet;
	private WorksheetEntry worksheet;
	private Sheets sheetsServiceV4;
	private Spreadsheet spreadsheetV4;
	private Sheet worksheetV4;

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_YELLOW = "\u001B[33m";

	SpreadsheetAPIImpl(SpreadsheetService spreadsheetService, Sheets sheetsService) {
		this.spreadsheetService = spreadsheetService;
		this.spreadsheetService.setConnectTimeout(4 * 60 * 1000);
		this.sheetsServiceV4 = sheetsService;
	}

	@Override
	public SpreadsheetAPI key(String key) {
		if (spreadsheetV4 != null && spreadsheetV4.getSpreadsheetId().equals(key)) {
			return this;
		}

		try {
			String spreadsheetURL = "https://spreadsheets.google.com/feeds/spreadsheets/" + key;
			spreadsheet = spreadsheetService.getEntry(new URL(spreadsheetURL), SpreadsheetEntry.class);
			spreadsheetV4 = sheetsServiceV4.spreadsheets().get(key).execute();
			resetWorksheet();
		} catch (IOException | ServiceException e) {
			throw new RuntimeException(e);
		}

		return this;

//		if (spreadsheet != null && spreadsheet.getKey().equals(key)) {
//			return this;
//		}
//
//		try {
//			String spreadsheetURL = "https://spreadsheets.google.com/feeds/spreadsheets/" + key;
//			spreadsheet = spreadsheetService.getEntry(new URL(spreadsheetURL), SpreadsheetEntry.class);
//			resetWorksheet();
//		} catch (ResourceNotFoundException e) {
//			return null;
//		} catch (IOException | ServiceException e) {
//			throw new RuntimeException(e);
//		}
	}

	private void resetWorksheet() {
		worksheet = null;
		worksheetV4 = null;
	}

	@Override
	public void setValue(int i, int j, String value) {
//		try {
//			CellFeed cellFeed = spreadsheetService.getFeed(worksheet.getCellFeedUrl(), CellFeed.class);
//
//			CellEntry cellEntry = new CellEntry(i, j, value);
//			cellFeed.insert(cellEntry);
//		} catch (ServiceException | IOException e) {
//			throw new RuntimeException(e);
//		}

		List<List<Object>> values = Arrays.asList(
				Arrays.asList(value)
		);

		ValueRange body = new ValueRange()
				.setValues(values);

		String range = worksheetV4.getProperties().getTitle() + "!" + (char)(64 + j) + i;

		try {
			UpdateValuesResponse result = sheetsServiceV4.spreadsheets().values()
					.update(spreadsheetV4.getSpreadsheetId(), range, body)
					.setValueInputOption("USER_ENTERED")
					.setIncludeValuesInResponse(true)
					.execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getValue(int i, int j) {
//		try {
//			CellQuery query = new CellQuery(worksheet.getCellFeedUrl());
//
//			query.setMinimumRow(i);
//			query.setMaximumRow(i);
//			query.setMinimumCol(j);
//			query.setMaximumCol(j);
//			query.setMaxResults(1);
//
//			CellFeed cellFeed = spreadsheetService.getFeed(query, CellFeed.class);
//			List<CellEntry> entries = cellFeed.getEntries();
//			if (entries.isEmpty()) {
//				return null;
//			}
//			return entries.get(0).getCell().getValue();
//		} catch (ServiceException | IOException e) {
//			throw new RuntimeException(e);
//		}
		String range = worksheetV4.getProperties().getTitle() + "!" + (char)(64 + j) + i;

		ValueRange response;
		try {
			response = sheetsServiceV4.spreadsheets().values()
					.get(spreadsheetV4.getSpreadsheetId(), range)
					.execute();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<List<Object>> values = response.getValues();
		return values.get(0).get(0).toString();
	}

	@Override
	public SpreadsheetAPI worksheet(String title) {
		try {
			worksheet = getWorksheetByTitle(title);
			worksheetV4 = getWorksheetByTitleV4(title);

			// TODO: Fazer o método createWorkSheet retornar uma workSheet
			if (worksheetV4 == null) {
				createWorksheetV4(title);

				worksheetV4 = getWorksheetByTitleV4(title);
				worksheet = getWorksheetByTitle(title);
			}

			return this;
		} catch (ServiceException | IOException e) {
			throw new RuntimeException(e);
		}

//		try {
//			worksheet = getWorksheetByTitle(title);
//			worksheetName = lucasGetWorksheetByTitle(title);
//
//			if (worksheet == null) {
//				worksheet = createWorksheet(title);
//			}
//
//			if (worksheetName == null) {
//				// TODO: Fazer o método createWorkSheet retornar uma workSheet
//				lucasCreateWorksheet(title);
//				worksheetName = lucasGetWorksheetByTitle(title);
//			}
//
//			return this;
//		} catch (ServiceException | IOException e) {
//			throw new RuntimeException(e);
//		}
	}

	@Override
	public boolean hasWorksheet(String title) {
		try {
			worksheet = getWorksheetByTitle(title);
			worksheetV4 = getWorksheetByTitleV4(title);

			return worksheetV4 != null;
		} catch (ServiceException | IOException e) {
			throw new RuntimeException(e);
		}

//		try {
//			worksheet = getWorksheetByTitle(title);
//			worksheetName = lucasGetWorksheetByTitle(title);
//
//			return worksheetName != null;
//		} catch (ServiceException | IOException e) {
//			throw new RuntimeException(e);
//		}
	}

	// TODO: remover esse método quando não usar mais worksheet
	private WorksheetEntry createWorksheet(String title) throws IOException, ServiceException {
		WorksheetEntry worksheet = new WorksheetEntry();
		worksheet.setTitle(new PlainTextConstruct(title));
		worksheet.setColCount(10);
		worksheet.setRowCount(10);
		return spreadsheetService.insert(spreadsheet.getWorksheetFeedUrl(), worksheet);
	}


	private void createWorksheetV4(String title) throws IOException {
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
		sheetsServiceV4.spreadsheets().batchUpdate(spreadsheetV4.getSpreadsheetId(), body).execute();
	}

	// TODO: remover esse método quando não usar mais worksheet
	private WorksheetEntry getWorksheetByTitle(String title) throws IOException, ServiceException {
		WorksheetQuery query = new WorksheetQuery(spreadsheet.getWorksheetFeedUrl());

		query.setTitleExact(true);
		query.setTitleQuery(title);

		WorksheetFeed worksheetFeed = spreadsheetService.getFeed(query, WorksheetFeed.class);
		List<WorksheetEntry> worksheets = worksheetFeed.getEntries();

		if (worksheets.size() == 0) {
			return null;
		}

		return worksheets.get(0);
	}

	// TODO: corrigir esse método
	private Sheet getWorksheetByTitleV4(String title) throws IOException {
		Sheets.Spreadsheets.Get request = sheetsServiceV4.spreadsheets().get(spreadsheetV4.getSpreadsheetId());
		Spreadsheet response = request.execute();
		List<Sheet> filteredSheets = response.getSheets().stream()
				.filter(sheet -> sheet.getProperties().getTitle().equals(title)).collect(Collectors.toList());

		if (filteredSheets.isEmpty()) {
			return  null;
		}
		return filteredSheets.get(0);
	}

	@Override
	public void batch(SpreadsheetBatch batch, BatchOptions... options) {
		try {
			int sentRows = 0;

			adjustWorksheetDimensions(batch, options);

			while (sentRows < batch.rows()) {
				int batchRows = (sentRows + MAX_BATCH_ROWS > batch.rows()) ? batch.rows() - sentRows : MAX_BATCH_ROWS;

				CellFeed cellFeed = queryCellFeedForBatch(batch, sentRows, batchRows);
				CellFeed batchRequest = createBatchRequest(cellFeed, batch, sentRows, batchRows);
				CellFeed batchResponse = executeBatchRequest(cellFeed, batchRequest);

				checkBatchResponse(batchResponse);

				sentRows += batchRows;
			}
		} catch (ServiceException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private CellFeed executeBatchRequest(CellFeed cellFeed, CellFeed batchRequest) throws IOException, ServiceException {
		Link batchLink = cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);

		spreadsheetService.setHeader("If-Match", "*");
		CellFeed batchResponse = spreadsheetService.batch(new URL(batchLink.getHref()), batchRequest);
		spreadsheetService.setHeader("If-Match", null);
		return batchResponse;
	}

	private CellFeed createBatchRequest(CellFeed cellFeed, SpreadsheetBatch batch, int sentRows, int batchRows) {
		List<CellEntry> cellEntries = cellFeed.getEntries();

		CellFeed batchRequest = new CellFeed();

		int count = 0;

		for (int i = 1; i <= batchRows; i++) {
			for (int j = 1; j <= batch.cols(); j++) {
				BatchOperationType batchOperationType = BatchOperationType.UPDATE;
				CellEntry sourceEntry = cellEntries.get(count);
				CellEntry batchEntry = new CellEntry(sourceEntry);
				batchEntry.changeInputValueLocal(batch.getValue(sentRows + i, j));
				BatchUtils.setBatchId(batchEntry, String.valueOf(count));
				BatchUtils.setBatchOperationType(batchEntry, batchOperationType);
				batchRequest.getEntries().add(batchEntry);

				count++;
			}
		}

		return batchRequest;
	}

	private CellFeed queryCellFeedForBatch(SpreadsheetBatch batch, int sentRows, int batchRows) throws IOException, ServiceException {

		CellQuery query = new CellQuery(worksheet.getCellFeedUrl());

		query.setMinimumRow(sentRows + 1);
		query.setMaximumRow(sentRows + batchRows);
		query.setMinimumCol(1);
		query.setMaximumCol(batch.cols());

		query.setReturnEmpty(true);
		return spreadsheetService.getFeed(query, CellFeed.class);
	}

	private void adjustWorksheetDimensions(SpreadsheetBatch updateSet, BatchOptions... options) throws IOException, ServiceException {
		if (worksheet.getColCount() == updateSet.cols() && worksheet.getRowCount() == updateSet.rows()) {
			return;
		}

		if (worksheet.getColCount() < updateSet.cols()) {
			worksheet.setColCount(updateSet.cols());
		}

		if (worksheet.getColCount() > updateSet.cols() && BatchOptions.SHRINK.on(options)) {
			worksheet.setColCount(updateSet.cols());
		}

		if (worksheet.getRowCount() < updateSet.rows()) {
			worksheet.setRowCount(updateSet.rows());
		}

		if (worksheet.getRowCount() > updateSet.rows() && BatchOptions.SHRINK.on(options)) {
			worksheet.setRowCount(updateSet.rows());
		}

		worksheet.update();
	}

	private void checkBatchResponse(CellFeed batchResponse) {
		for (CellEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			if (!BatchUtils.isSuccess(entry)) {
				BatchStatus status = BatchUtils.getBatchStatus(entry);
				throw new RuntimeException(MessageFormat.format("update failed batchId={0}, reason={1}", batchId, status.getReason()));
			}
		}
	}

	@Override
	public List<Map<String, String>> asMap() {
		List<Map<String, String>> records = new ArrayList<>();

		try {
			ListFeed feed = spreadsheetService.getFeed(worksheet.getListFeedUrl(), ListFeed.class);

			for (ListEntry row : feed.getEntries()) {
				records.add(loadRecord(row));
			}

			return records;
		} catch (ServiceException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> loadRecord(ListEntry row) {
		Map<String, String> record = new HashMap<>();
		for (String tag : row.getCustomElements().getTags()) {
			record.put(tag, row.getCustomElements().getValue(tag));
		}
		return record;
	}
}
