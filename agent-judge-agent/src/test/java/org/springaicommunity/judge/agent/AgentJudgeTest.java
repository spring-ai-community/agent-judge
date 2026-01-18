/*
 * Copyright 2024 Spring AI Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springaicommunity.judge.agent;

import org.junit.jupiter.api.Test;
import org.springaicommunity.judge.JudgeMetadata;
import org.springaicommunity.judge.JudgeType;
import org.springaicommunity.judge.context.ExecutionStatus;
import org.springaicommunity.judge.context.JudgmentContext;
import org.springaicommunity.judge.result.Judgment;
import org.springaicommunity.judge.result.JudgmentStatus;
import org.springaicommunity.judge.score.BooleanScore;
import org.springaicommunity.judge.score.NumericalScore;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AgentJudge}.
 *
 * @author Mark Pollack
 * @since 0.1.0
 */
class AgentJudgeTest {

	@Test
	void shouldRequireAgentClient() {
		assertThatThrownBy(() -> AgentJudge.builder().criteria("Test criteria").build())
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("JudgeAgentClient is required");
	}

	@Test
	void shouldRequireCriteria() {
		JudgeAgentClient mockClient = mock(JudgeAgentClient.class);

		assertThatThrownBy(() -> AgentJudge.builder().agentClient(mockClient).build())
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Criteria is required");
	}

	@Test
	void shouldCreateJudgeWithMetadata() {
		JudgeAgentClient mockClient = mock(JudgeAgentClient.class);

		AgentJudge judge = AgentJudge.builder()
			.agentClient(mockClient)
			.name("TestAgent")
			.description("Test Description")
			.criteria("Test criteria")
			.build();

		JudgeMetadata metadata = judge.metadata();

		assertThat(metadata.name()).isEqualTo("TestAgent");
		assertThat(metadata.description()).isEqualTo("Test Description");
		assertThat(metadata.type()).isEqualTo(JudgeType.AGENT);
	}

	@Test
	void shouldCreateCodeReviewJudge() {
		JudgeAgentClient mockClient = mock(JudgeAgentClient.class);

		AgentJudge judge = AgentJudge.codeReview(mockClient);

		JudgeMetadata metadata = judge.metadata();
		assertThat(metadata.name()).isEqualTo("CodeReview");
		assertThat(metadata.description()).isEqualTo("Agent-powered code review");
	}

	@Test
	void shouldCreateSecurityAuditJudge() {
		JudgeAgentClient mockClient = mock(JudgeAgentClient.class);

		AgentJudge judge = AgentJudge.securityAudit(mockClient);

		JudgeMetadata metadata = judge.metadata();
		assertThat(metadata.name()).isEqualTo("SecurityAudit");
		assertThat(metadata.description()).isEqualTo("Agent-powered security audit");
	}

	@Test
	void shouldParsePassingJudgment() {
		JudgeAgentClient mockClient = mock(JudgeAgentClient.class);
		when(mockClient.execute(anyString(), any(Path.class))).thenReturn(new JudgeAgentResponse("""
				PASS: true
				SCORE: 8.5
				REASONING: The code is well-structured and follows best practices.
				""", true));

		AgentJudge judge = AgentJudge.builder().agentClient(mockClient).criteria("Evaluate code quality").build();

		JudgmentContext context = createContext();
		Judgment judgment = judge.judge(context);

		assertThat(judgment.status()).isEqualTo(JudgmentStatus.PASS);
		assertThat(judgment.score()).isInstanceOf(NumericalScore.class);
		assertThat(((NumericalScore) judgment.score()).value()).isEqualTo(8.5);
		assertThat(judgment.reasoning()).contains("well-structured");
	}

	@Test
	void shouldParseFailingJudgment() {
		JudgeAgentClient mockClient = mock(JudgeAgentClient.class);
		when(mockClient.execute(anyString(), any(Path.class))).thenReturn(new JudgeAgentResponse("""
				PASS: false
				REASONING: The code has critical bugs.
				""", true));

		AgentJudge judge = AgentJudge.builder().agentClient(mockClient).criteria("Evaluate code quality").build();

		Judgment judgment = judge.judge(createContext());

		assertThat(judgment.status()).isEqualTo(JudgmentStatus.FAIL);
		assertThat(judgment.score()).isInstanceOf(BooleanScore.class);
		assertThat(((BooleanScore) judgment.score()).value()).isFalse();
		assertThat(judgment.reasoning()).contains("critical bugs");
	}

	@Test
	void shouldHandleMissingPassInResponse() {
		JudgeAgentClient mockClient = mock(JudgeAgentClient.class);
		when(mockClient.execute(anyString(), any(Path.class)))
			.thenReturn(new JudgeAgentResponse("Some unclear response without structured format", true));

		AgentJudge judge = AgentJudge.builder().agentClient(mockClient).criteria("Evaluate code quality").build();

		Judgment judgment = judge.judge(createContext());

		// Should default to FAIL when PASS pattern not found
		assertThat(judgment.status()).isEqualTo(JudgmentStatus.FAIL);
	}

	@Test
	void shouldUseCustomGoalTemplate() {
		JudgeAgentClient mockClient = mock(JudgeAgentClient.class);
		when(mockClient.execute(anyString(), any(Path.class)))
			.thenReturn(new JudgeAgentResponse("PASS: true\nREASONING: Custom template worked", true));

		AgentJudge judge = AgentJudge.builder()
			.agentClient(mockClient)
			.criteria("Custom criteria")
			.goalTemplate("Custom template: {goal} - {criteria}")
			.build();

		Judgment judgment = judge.judge(createContext());

		assertThat(judgment.status()).isEqualTo(JudgmentStatus.PASS);
	}

	private JudgmentContext createContext() {
		return JudgmentContext.builder()
			.goal("Test goal")
			.workspace(Path.of("/tmp/test"))
			.agentOutput("Test output")
			.executionTime(Duration.ofSeconds(5))
			.startedAt(Instant.now())
			.status(ExecutionStatus.SUCCESS)
			.build();
	}

}
