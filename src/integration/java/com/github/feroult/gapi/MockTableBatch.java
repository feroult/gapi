package com.github.feroult.gapi;

import com.github.feroult.gapi.spreadsheet.SpreadsheetBatch;

public class MockTableBatch implements SpreadsheetBatch {

	private String[][] table;

	public MockTableBatch(String[][] table) {
		this.table = table;
	}

	@Override
	public int rows() {
		return table.length;
	}

	@Override
	public String getValue(int row, int column) {
		return table[row - 1][column - 1];
	}

	@Override
	public int cols() {
		return table[0].length;
	}
}
