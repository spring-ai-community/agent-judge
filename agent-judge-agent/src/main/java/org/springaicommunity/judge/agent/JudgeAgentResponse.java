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

/**
 * Response from agent execution for judge evaluation.
 *
 * @param result the agent's output/result text
 * @param successful whether the agent completed successfully
 * @author Mark Pollack
 * @since 0.1.0
 * @see JudgeAgentClient
 */
public record JudgeAgentResponse(String result, boolean successful) {

	/**
	 * Create a successful agent response.
	 * @param result the agent's output
	 * @return successful response
	 */
	public static JudgeAgentResponse success(String result) {
		return new JudgeAgentResponse(result, true);
	}

	/**
	 * Create a failed agent response.
	 * @param result the agent's output or error message
	 * @return failed response
	 */
	public static JudgeAgentResponse failure(String result) {
		return new JudgeAgentResponse(result, false);
	}

}
