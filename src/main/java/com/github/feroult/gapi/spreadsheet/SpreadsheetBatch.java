package com.github.feroult.gapi.spreadsheet;

public interface SpreadsheetBatch {

	int rows();

	int cols();

	// 1-based indexes
	String getValue(int row, int column);

}
