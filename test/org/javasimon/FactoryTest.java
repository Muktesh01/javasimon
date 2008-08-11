package org.javasimon;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;

/**
 * FactoryTest.
 *
 * @author <a href="mailto:virgo47@gmail.com">Richard "Virgo" Richter</a>
 * @created Aug 8, 2008
 */
public final class FactoryTest {
	private static final int FRESH_FACTORY_SIMON_LIST_SIZE = 1;
	private static final int SIMON_COUNT_AFTER_COUNTER_ADDED = 5;

	private static final String ORG_JAVASIMON_TEST = "org.javasimon.test";
	private static final String ORG_JAVASIMON_TEST_COUNTER = "org.javasimon.test.counter";

	@BeforeMethod
	public void resetAndEnable() {
		SimonFactory.reset();
		SimonFactory.enable();
	}

	@Test
	public void testSimonCreation() {
		Assert.assertEquals(SimonFactory.simonNames().size(), FRESH_FACTORY_SIMON_LIST_SIZE);
		SimonFactory.getCounter(ORG_JAVASIMON_TEST_COUNTER).increment();
		Assert.assertEquals(SimonFactory.simonNames().size(), SIMON_COUNT_AFTER_COUNTER_ADDED);

		Assert.assertTrue(SimonFactory.getSimon(ORG_JAVASIMON_TEST) instanceof UnknownSimon);
		Assert.assertEquals(SimonFactory.getSimon(ORG_JAVASIMON_TEST).getChildren().size(), 1);
		SimonFactory.getCounter(ORG_JAVASIMON_TEST);
		Assert.assertTrue(SimonFactory.getSimon(ORG_JAVASIMON_TEST) instanceof Counter);
		Assert.assertEquals(SimonFactory.getSimon(ORG_JAVASIMON_TEST).getChildren().size(), 1);
	}

	@Test
	public void testDisabledSimons() {
		SimonFactory.getRootSimon().setState(SimonState.DISABLED, true);
		Assert.assertFalse(SimonFactory.getRootSimon().isEnabled());
		Assert.assertFalse(SimonFactory.getCounter(ORG_JAVASIMON_TEST_COUNTER).isEnabled());

		SimonFactory.getCounter(ORG_JAVASIMON_TEST_COUNTER).setState(SimonState.ENABLED, false);
		Assert.assertTrue(SimonFactory.getCounter(ORG_JAVASIMON_TEST_COUNTER).isEnabled());
		Assert.assertFalse(SimonFactory.getCounter(ORG_JAVASIMON_TEST).isEnabled());

		SimonFactory.getCounter(ORG_JAVASIMON_TEST_COUNTER).setState(SimonState.INHERIT, false);
		Assert.assertFalse(SimonFactory.getCounter(ORG_JAVASIMON_TEST_COUNTER).isEnabled());
		Assert.assertFalse(SimonFactory.getCounter(ORG_JAVASIMON_TEST).isEnabled());

		SimonFactory.getCounter(ORG_JAVASIMON_TEST_COUNTER).setState(SimonState.DISABLED, false);
		Assert.assertEquals(SimonFactory.getRootSimon().getName(), SimonFactory.ROOT_SIMON_NAME);

		SimonFactory.disable();
		Assert.assertNull(SimonFactory.getSimon(ORG_JAVASIMON_TEST));
		Assert.assertTrue(SimonFactory.getRootSimon() instanceof NullSimon);
		Assert.assertNull(SimonFactory.getRootSimon().getName());
	}

	@Test
	public void testStatePropagation() {
		Assert.assertTrue(SimonFactory.getStopwatch("org.javasimon.inherit.sw1").isEnabled());
		Assert.assertTrue(SimonFactory.getStopwatch("org.javasimon.enabled.sw1").isEnabled());
		Assert.assertTrue(SimonFactory.getStopwatch("org.javasimon.disabled.sw1").isEnabled());

		SimonFactory.getStopwatch("org.javasimon.enabled").setState(SimonState.ENABLED, true);
		SimonFactory.getStopwatch("org.javasimon.disabled").setState(SimonState.DISABLED, true);
		Assert.assertTrue(SimonFactory.getStopwatch("org.javasimon.inherit.sw1").isEnabled());
		Assert.assertTrue(SimonFactory.getStopwatch("org.javasimon.enabled.sw1").isEnabled());
		Assert.assertFalse(SimonFactory.getStopwatch("org.javasimon.disabled.sw1").isEnabled());

		SimonFactory.getRootSimon().setState(SimonState.DISABLED, false);
		Assert.assertFalse(SimonFactory.getStopwatch("org.javasimon.inherit.sw1").isEnabled());
		Assert.assertTrue(SimonFactory.getStopwatch("org.javasimon.enabled.sw1").isEnabled());
		Assert.assertFalse(SimonFactory.getStopwatch("org.javasimon.disabled.sw1").isEnabled());

		SimonFactory.getRootSimon().setState(SimonState.ENABLED, true);
		Assert.assertTrue(SimonFactory.getStopwatch("org.javasimon.inherit.sw1").isEnabled());
		Assert.assertTrue(SimonFactory.getStopwatch("org.javasimon.enabled.sw1").isEnabled());
		Assert.assertTrue(SimonFactory.getStopwatch("org.javasimon.disabled.sw1").isEnabled());
	}

	@Test
	public void testGeneratedNames() {
		Assert.assertEquals(SimonFactory.generateName("-stopwatch", true), getClass().getName() + ".testGeneratedNames-stopwatch");

		SimonFactory.disable();
		Assert.assertNull(SimonFactory.generateName("-stopwatch", true));
	}
}
