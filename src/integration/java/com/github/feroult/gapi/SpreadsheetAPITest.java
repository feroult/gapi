package com.github.feroult.gapi;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Ignore
public class SpreadsheetAPITest {

	private final String[][] peopleTable = new String[][]{{"id", "name", "age"}, {"1", "John", "10"}, {"2", "Anne", "15"}};
	private final String[][] projecTable = new String[][]{{"Project ID", "Project Name"}, {"123", "Project 1"}, {"321", "Project 2"}};

	private GoogleAPI google;

	@Before
	public void before() {
		google = new GoogleAPI();
	}

	@Test
	public void testChangeSpreadsheet() {
		String key = google.drive().createSpreadsheet();
		google.spreadsheet(key).worksheet("first worksheet").setValue(1, 1, "xpto");
		assertEquals("xpto", google.spreadsheet(key).worksheet("first worksheet").getValue(1, 1));
		google.drive().delete(key);
	}

	@Test
	public void testSpreadsheetBatchUpdate() {
		String key = google.drive().createSpreadsheet();
		google.spreadsheet(key).worksheet("xpto").batch(new MockTableBatch(peopleTable));
		assertEquals("John", google.spreadsheet(key).worksheet("xpto").getValue(2, 2));

		google.drive().delete(key);
	}

	@Test
	public void testSpreadsheetBatchShrinkOption() {

		String[][] twoRowsTable = new String[][]{{"name", "age"}, {"john", "30"}};
		String[][] threeRowsTable = new String[][]{{"name", "age"}, {"john", "30"}, {"anne", "40"}};

		String key = google.drive().createSpreadsheet();

		try {
			google.spreadsheet(key).worksheet("xpto").batch(new MockTableBatch(threeRowsTable));
			assertEquals(2, google.spreadsheet(key).worksheet("xpto").asMap().size());

			google.spreadsheet(key).worksheet("xpto").batch(new MockTableBatch(twoRowsTable));
			assertEquals(2, google.spreadsheet(key).worksheet("xpto").asMap().size());

			google.spreadsheet(key).worksheet("xpto").batch(new MockTableBatch(twoRowsTable), BatchOptions.SHRINK);
			assertEquals(1, google.spreadsheet(key).worksheet("xpto").asMap().size());

		} finally {
			google.drive().delete(key);
		}

	}

	@Test
	public void testLoadSpreadsheetHash() {
		String key = google.drive().createSpreadsheet();

		try {
			google.spreadsheet(key).worksheet("xpto").batch(new MockTableBatch(peopleTable));

			List<Map<String, String>> records = google.spreadsheet(key).worksheet("xpto").asMap();

			Map<String, String> record = records.get(0);
			assertEquals("1", record.get("id"));
			assertEquals("John", record.get("name"));
			assertEquals("10", record.get("age"));

		} finally {
			google.drive().delete(key);
		}
	}

	@Test
	public void testNormalizeKeysAsMap() {
		String key = google.drive().createSpreadsheet();

		try {
			google.spreadsheet(key).worksheet("xpto").batch(new MockTableBatch(projecTable));

			List<Map<String, String>> records = google.spreadsheet(key).worksheet("xpto").asMap();

			Map<String, String> record = records.get(0);
			assertNull(record.get("Project ID"));
			assertNull(record.get("Project Name"));
			assertEquals("123", record.get("projectid"));
			assertEquals("Project 1", record.get("projectname"));

		} finally {
			google.drive().delete(key);
		}
	}

	@Test
	public void testSpreadsheetBatchByChunks() {
		String key = google.drive().createSpreadsheet();

		try {
			google.spreadsheet(key).worksheet("xpto").batch(new MockTableBatch(createBigPeopleTable()));
		} finally {
			google.drive().delete(key);
		}
	}

	private String[][] createBigPeopleTable() {
		int maxPeople = 1000;

		String[][] peopleTable = new String[maxPeople + 1][3];

		peopleTable[0][0] = "id";
		peopleTable[0][1] = "name";
		peopleTable[0][2] = "age";

		for (int i = 1; i <= maxPeople; i++) {
			peopleTable[i][0] = String.valueOf(i);
			peopleTable[i][1] = "John " + i;
			peopleTable[i][2] = String.valueOf(20 + i);
		}

		return peopleTable;
	}
}
