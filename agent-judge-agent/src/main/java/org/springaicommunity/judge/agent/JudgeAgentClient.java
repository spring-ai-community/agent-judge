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

import java.nio.file.Path;

/**
 * Bridge interface for agent client integration.
 *
 * <p>
 * This interface allows the Judge framework to use agent capabilities without hard
 * dependencies on specific agent implementations. Users of the Judge framework should
 * provide their own implementation that adapts their agent client to this interface.
 * </p>
 *
 * <p>
 * Example implementation with Spring AI Agents:
 * </p>
 *
 * <pre>{@code
 * public class AgentClientAdapter implements JudgeAgentClient {
 *     private final AgentClient agentClient;
 *
 *     public AgentClientAdapter(AgentClient agentClient) {
 *         this.agentClient = agentClient;
 *     }
 *
 *     &#64;Override
 *     public JudgeAgentResponse execute(String goal, Path workspace) {
 *         AgentClientResponse response = agentClient.goal(goal)
 *             .workingDirectory(workspace)
 *             .run();
 *         return new JudgeAgentResponse(response.getResult(), response.isSuccessful());
 *     }
 * }
 * }</pre>
 *
 * @author Mark Pollack
 * @since 0.1.0
 * @see AgentJudge
 */
@FunctionalInterface
public interface JudgeAgentClient {

	/**
	 * Execute an agent task with the given goal and workspace.
	 * @param goal the goal/task for the agent to accomplish
	 * @param workspace the working directory for agent execution
	 * @return the agent response containing result and success status
	 */
	JudgeAgentResponse execute(String goal, Path workspace);

}
