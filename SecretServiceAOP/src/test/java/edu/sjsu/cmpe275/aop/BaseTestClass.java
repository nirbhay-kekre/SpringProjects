package edu.sjsu.cmpe275.aop;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath:context.xml", })
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseTestClass {
	@Autowired
	SecretService secretService;
	
	@Autowired
	SecretStats secretStats;

	@Before
	public void setup() {
		secretStats.resetStatsAndSystem();
	}
}
