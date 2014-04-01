
package org.javasimon.report;

import org.javasimon.CounterSample;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.StopwatchSample;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class CsvReporterTest {

	private CsvReporter reporter;
	private Manager manager;
	private TimeSource timeSource;

	private static final String COUNTERS_FILE = "countersFile.csv";
	private static final String STOPWATCHES_FILE = "stopwatchesFile.csv";
	private static final char SEPARATOR = ';';
	private static final String COUNTERS_FIELDS = "time;name;total;min;max;minTimestamp;maxTimestamp;incrementSum;decrementSum";
	private static final String STOPWATCH_FIELDS = "time;name;total;min;max;minTimestamp;maxTimestamp;active;maxActive;maxActiveTimestamp;last;mean;stdDev;variance;varianceN";

	@BeforeMethod
	public void beforeMethod() throws Exception {
		rmFiles();

		timeSource = mock(TimeSource.class);
		manager = mock(Manager.class);
		reporter = CsvReporter.forManager(manager)
				.countersFile(COUNTERS_FILE)
				.stopwatchesFile(STOPWATCHES_FILE)
				.timeSource(timeSource)
				.separator(SEPARATOR);
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		rmFiles();
	}

	private void rmFiles() throws IOException {
		rm(COUNTERS_FILE);
		rm(STOPWATCHES_FILE);
		rm(CsvReporter.DEFAULT_COUNTERS_FILE);
		rm(CsvReporter.DEFAULT_STOPWATCHES_FILE);
	}

	private void rm(String filePath) throws IOException {
		File file = new File(filePath);
		if (file.exists() && !file.delete()) {
			throw new IOException("Failed to delete file" + filePath);
		}
	}

	@Test
	public void testCreateForManager() {
		Assert.assertEquals(reporter.getManager(), manager);
	}

	@Test
	public void testCreateForDefaultManager() {
		Manager defaultManager = SimonManager.manager();
		CsvReporter reporter = CsvReporter.forDefaultManager();
		Assert.assertEquals(reporter.getManager(), defaultManager);
	}

	@Test
	public void testGetCountersFile() {
		Assert.assertEquals(reporter.getCountersFile(), COUNTERS_FILE);
	}

	@Test
	public void testGetDefaultCountersFile() {
		Assert.assertEquals(CsvReporter.forDefaultManager().getCountersFile(), CsvReporter.DEFAULT_COUNTERS_FILE);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetNullCountersFile() {
		reporter.countersFile(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetEmptyCountersFile() {
		reporter.countersFile("");
	}

	@Test
	public void testGetStopwatchesFile() {
		Assert.assertEquals(reporter.getStopwatchesFile(), STOPWATCHES_FILE);
	}

	@Test
	public void testGetDefaultStopwatchesFile() {
		Assert.assertEquals(reporter.forDefaultManager().getStopwatchesFile(), CsvReporter.DEFAULT_STOPWATCHES_FILE);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetNullStopwatchesFile() {
		reporter.stopwatchesFile(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetEmptyStopwatchesFile() {
		reporter.stopwatchesFile("");
	}

	@Test
	public void testGetTimeSource() {
		Assert.assertEquals(reporter.getTimeSource(), timeSource);
	}

	@Test
	public void testGetDefaultTimeSource() {
		Assert.assertTrue(CsvReporter.forDefaultManager().getTimeSource() instanceof SystemTimeSource);
	}

	@Test
	public void testGetSeparator() {
		Assert.assertEquals(reporter.getSeparator(), SEPARATOR);
	}

	@Test
	public void testGetDefaultSeparator() {
		Assert.assertEquals(CsvReporter.forDefaultManager().getSeparator(), CsvReporter.DEFAULT_SEPARATOR);
	}

	@Test
	public void testGetIsAppend() {
		reporter.append();
		Assert.assertTrue(reporter.isAppendFile());
	}

	@Test
	public void testGetDefaultIsAppend() {
		Assert.assertFalse(CsvReporter.forDefaultManager().isAppendFile());
	}

	@Test
	public void testSetAppendFile() {
		reporter.setAppendFile(true);
		Assert.assertTrue(reporter.isAppendFile());
	}

	@Test
	public void testCountersFileWasCreated() {
		reporter.onStart();
		reporter.onStop();
		Assert.assertTrue(fileExists(COUNTERS_FILE));
	}

	@Test
	 public void testCountersHeaderWasWritten() throws Exception {
		reporter.onStart();
		reporter.onStop();
		Assert.assertEquals(fileToString(COUNTERS_FILE),
				csv(COUNTERS_FIELDS));
	}

	private String csv(String... lines) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line);
			sb.append(String.format("%n"));
		}

		return sb.toString();
	}

	@Test
	public void testCounterSampleWasWritten() throws Exception {
		reporter.onStart();

		CounterSample counterSample = new CounterSample();
		counterSample.setName("counter.name");
		counterSample.setCounter(1);
		counterSample.setMin(0);
		counterSample.setMax(3);
		counterSample.setMinTimestamp(100);
		counterSample.setMaxTimestamp(200);
		counterSample.setIncrementSum(3);
		counterSample.setDecrementSum(2);

		when(timeSource.getTime()).thenReturn(1000L);
		reporter.report(Collections.EMPTY_LIST, Arrays.asList(counterSample));

		reporter.onStop();
		Assert.assertEquals(fileToString(COUNTERS_FILE),
				csv(COUNTERS_FIELDS,
				"1000;\"counter.name\";1;0;3;100;200;3;2"));
	}

	@Test
	public void testStopwatchesHeaderWasWritten() throws Exception {
		reporter.onStart();
		reporter.onStop();
		Assert.assertEquals(fileToString(STOPWATCHES_FILE),
				csv(STOPWATCH_FIELDS));
	}

	@Test
	public void testStopwatchSampleWritten() throws Exception {
		StopwatchSample sample = new StopwatchSample();
		sample.setName("stopwatch.name");
		sample.setTotal(100);
		sample.setMin(2);
		sample.setMax(10);
		sample.setMinTimestamp(200);
		sample.setMaxTimestamp(300);
		sample.setActive(3);
		sample.setMaxActive(10);
		sample.setMaxActiveTimestamp(400);
		sample.setLast(5);
		sample.setMean(8.0);
		sample.setStandardDeviation(4.0);
		sample.setVariance(2.0);
		sample.setVarianceN(2.0);

		reporter.onStart();
		when(timeSource.getTime()).thenReturn(1000L);
		reporter.report(Arrays.asList(sample), Collections.EMPTY_LIST);

		reporter.onStop();
		Assert.assertEquals(fileToString(STOPWATCHES_FILE),
				csv(STOPWATCH_FIELDS,
					"1000;\"stopwatch.name\";100;2;10;200;300;3;10;400;5;8.0;4.0;2.0;2.0"));
	}

	@Test
	public void testAppendToCountersFile() throws Exception {
		createFileWithContent(COUNTERS_FILE, COUNTERS_FIELDS);
		reporter.append();

		reporter.onStart();
		reporter.onStop();
		Assert.assertEquals(fileToString(COUNTERS_FILE),
				csv(COUNTERS_FIELDS));
	}

	@Test
	public void testAppendToStopwatchesFile() throws Exception {
		createFileWithContent(STOPWATCHES_FILE, STOPWATCH_FIELDS);
		reporter.append();

		reporter.onStart();
		reporter.onStop();
		Assert.assertEquals(fileToString(STOPWATCHES_FILE),
				csv(STOPWATCH_FIELDS));
	}

	@Test
	public void testAppendToCountersFileWhenFileDoesNotExists() throws Exception {
		reporter.append();

		reporter.onStart();
		reporter.onStop();
		Assert.assertEquals(fileToString(COUNTERS_FILE),
				csv(COUNTERS_FIELDS));
	}

	@Test
	public void testAppendToStopwatchesFileWhenFileDoesNotExists() throws Exception {
		reporter.append();

		reporter.onStart();
		reporter.onStop();
		Assert.assertEquals(fileToString(STOPWATCHES_FILE),
				csv(STOPWATCH_FIELDS));
	}

	private void createFileWithContent(String fileName, String content) throws IOException {
		PrintWriter writer = null;
		try {
			 writer = new PrintWriter(new FileWriter(fileName));
			 writer.println(content);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private String fileToString(String filePath) throws IOException {
		StringBuilder content = new StringBuilder();

		Reader reader = null;

		try {
			reader = new BufferedReader(new FileReader(filePath));

			char[] buffer = new char[1000];
			int read;
			while ((read = reader.read(buffer)) > 0) {
				content.append(buffer, 0, read);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return content.toString();
	}

	private boolean fileExists(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}
}