package com.ems.copilot.emscopilot;

import com.ems.copilot.emscopilot.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class EmsCopilotBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
