package gapi;

import static org.junit.Assert.assertEquals;
import gapi.utils.Setup;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class GoogleAPITest {

	private final String[][] peopleTable = new String[][] { { "id", "name", "age" }, { "1", "John", "10" },
			{ "2", "Anne", "15" } };

	private GoogleAPI google;

	@Before
	public void before() {
		setup();
		google = new GoogleAPI();
	}

	private void setup() {
		System.setProperty(Setup.GAPI_SERVICE_ACCOUNT_EMAIL, System.getenv("GAPI_SERVICE_ACCOUNT_EMAIL"));
		System.setProperty(Setup.GAPI_SERVICE_ACCOUNT_KEY, System.getenv("GAPI_SERVICE_ACCOUNT_KEY")); 
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
		
		String[][] peopleTable = new String[maxPeople+1][3];
		
		peopleTable[0][0] = "id";
		peopleTable[0][1] = "name";
		peopleTable[0][2] = "age";

		for(int i = 1; i <= maxPeople; i++) {
			peopleTable[i][0] = String.valueOf(i);
			peopleTable[i][1] = "John " + i;
			peopleTable[i][2] = String.valueOf(20+i);			
		}
				
		return peopleTable;
	}	
}
