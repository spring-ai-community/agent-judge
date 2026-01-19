# Design Decision: Agent-based Judging Lives in `spring-ai-agents`, Not `spring-ai-judge`

**Decision Date**: 2026-01-16

## Decision

`spring-ai-judge` will remain a **standalone, agent-agnostic judgment library**. It will provide core judgment abstractions and non-agent implementations (deterministic, process-based, and simple LLM-based via `ChatClient`).

All **agent-based judging** will be removed from `spring-ai-judge` and re-homed in `spring-ai-agents`, where it will be implemented as an *agent whose purpose is judging*, not as a judge that happens to use agents.

## Rationale

The introduction of `JudgeAgentClient` and adapter layers during extraction revealed a boundary mismatch rather than a missing abstraction. Agent-based judging requires autonomy, iteration, tool use, and delegation—capabilities that fundamentally belong to the agent runtime. By contrast, `spring-ai-judge` is about evaluation semantics (criteria, verdicts, aggregation), not autonomous execution strategies.

Placing agent-based judging inside `spring-ai-judge` forced artificial bridge interfaces and adapters to avoid circular dependencies. These adapters are a design smell in a greenfield refactoring and signal that the responsibility was placed in the wrong module. Moving agent-based judging into `spring-ai-agents` eliminates the need for adapters, preserves clean dependency direction, and keeps `spring-ai-judge` genuinely reusable by consumers who do not use agents at all.

## Key Insight

`AgentJudge` is not "a judge implementation that uses agents"; it is **an agent whose job is judging**. It should therefore live with agents, implement the `Judge` interface from `spring-ai-judge-core`, and depend directly on `AgentClient`.

## Target Architecture (Post-Change)

```
spring-ai-judge (agent-agnostic)
├── spring-ai-judge-core
│   ├── Judge
│   ├── JudgmentContext
│   ├── Verdict / Score / Explanation
│   └── Aggregation APIs
├── spring-ai-judge-exec
│   └── ProcessRunner-based judges
├── spring-ai-judge-llm
│   └── ChatClient-based judges (single-call, stateless)
└── spring-ai-judge-bom

spring-ai-agents (depends on spring-ai-judge)
├── spring-ai-agent-core
│   └── AgentClient, tools, runtime
├── spring-ai-agent-judge
│   └── AgentJudge implements Judge (uses AgentClient directly)
└── spring-ai-agent-advisor
    └── Advisors that invoke judges during agent runs
```

## Implementation Plan

### Phase 1: Remove Misplaced Agent Abstractions (Mechanical)

1. **Delete module**
   - Remove `spring-ai-judge-agent`
   - Remove it from parent `pom.xml`, BOM, CI workflows

2. **Delete bridge abstractions**
   - Remove `JudgeAgentClient`
   - Remove `JudgeAgentResponse`
   - Delete all adapter-related code paths referencing them

3. **Remove adapters**
   - Delete `AgentClientAdapter`
   - Delete `AgentClientAdapterResponse`
   - Remove any indirect wiring that exists solely to support this bridge

> Outcome: `spring-ai-judge` has *zero* knowledge of agents.

### Phase 2: Re-home Agent-Based Judging (Structural)

4. **Create new module**
   - `spring-ai-agent-judge` in spring-ai-agents
   - Depends on `spring-ai-judge-core` and `spring-ai-agent-core`

5. **Move AgentJudge**
   - Relocate `AgentJudge` into this new module
   - `AgentJudge` implements `Judge`, accepts `AgentClient` directly

6. **Align responsibility**
   - Any logic that spawns agents, delegates to CLI tools, or iterates stays entirely inside `spring-ai-agents`

### Phase 3: Stabilize Public Contracts (Conceptual)

7. **Keep Judge SPI minimal**
   - `Judge` remains synchronous or async
   - Input: `JudgmentContext`, Output: `Judgment / Verdict`

8. **Clarify documentation**
   - `spring-ai-judge-llm` = single-call LLM judges
   - `spring-ai-agent-judge` = autonomous agent-based evaluation

## Resulting Benefits

- Clean, one-directional dependencies
- No adapters or bridge interfaces
- `spring-ai-judge` is reusable without agents
- Agent autonomy remains fully owned by `spring-ai-agents`
- Future evolution is simpler
