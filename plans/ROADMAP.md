# Agent Judge Roadmap

## Vision

Agent Judge provides a comprehensive, **agent-agnostic** framework for evaluating LLM outputs through deterministic, process-based, and LLM-powered judges with a flexible jury voting system.

## Current Status: 0.1.0-SNAPSHOT (Phase 0-1 Complete)

### Module Structure (Renamed)

| Module | Status | Description |
|--------|--------|-------------|
| `agent-judge-core` | DONE | Core Judge API - zero external dependencies |
| `agent-judge-exec` | DONE | Command execution judges (sandbox-based) |
| `agent-judge-llm` | DONE | LLM-powered semantic evaluation (single-call, stateless) |
| `agent-judge-advisor` | Pending Move | Moving to spring-ai-agents |
| `agent-judge-agent` | Pending Move | Moving to spring-ai-agents |
| `agent-judge-bom` | DONE | BOM for dependency management |

### Key Design Decision

See [AGENT-JUDGING-BOUNDARY.md](learnings/AGENT-JUDGING-BOUNDARY.md) for full rationale.

**Summary**: Agent-based judging belongs in `spring-ai-agents`, not here. `AgentJudge` is "an agent whose job is judging", not "a judge that uses agents".

---

## Phase 0: Module Renaming - COMPLETE

**Objective**: Rename all modules from `spring-ai-judge-*` to `agent-judge-*` to reflect the agent-agnostic nature of the framework.

### Entry Criteria

- [x] Local project builds successfully (`./mvnw clean verify`)
- [x] All existing tests pass
- [x] Backup created (tarball of current state)

### Tasks

#### 0.1 Rename Module Directories

- [x] Rename `spring-ai-judge-core` → `agent-judge-core`
- [x] Rename `spring-ai-judge-exec` → `agent-judge-exec`
- [x] Rename `spring-ai-judge-llm` → `agent-judge-llm`
- [x] Rename `spring-ai-judge-bom` → `agent-judge-bom`
- [x] Rename `spring-ai-judge-advisor` → `agent-judge-advisor` (temporary, will be moved later)
- [x] Rename `spring-ai-judge-agent` → `agent-judge-agent` (temporary, will be moved later)

#### 0.2 Update Parent POM

- [x] Change `<artifactId>` from `spring-ai-judge-parent` to `agent-judge-parent`
- [x] Update `<name>` and `<description>` to remove "Spring AI" references
- [x] Update `<url>` to `https://github.com/spring-ai-community/agent-judge`
- [x] Update SCM URLs to reference `agent-judge`
- [x] Update `<modules>` section with new directory names
- [x] Update `<properties>` version variable from `spring-ai-judge.version` to `agent-judge.version`
- [x] Update all internal module references in `<dependencyManagement>`

#### 0.3 Update Each Module POM

For each module (`core`, `exec`, `llm`, `bom`, `advisor`, `agent`):

- [x] Update `<parent>` artifactId to `agent-judge-parent`
- [x] Update `<artifactId>` to `agent-judge-*`
- [x] Update `<name>` to remove "Spring AI" prefix
- [x] Update `<description>` to remove "Spring AI" references
- [x] Update any inter-module `<dependency>` references

#### 0.4 Update BOM

- [x] Update all managed dependency artifactIds to `agent-judge-*`

#### 0.5 Verify Build

- [x] Run `./mvnw clean compile` - must pass
- [x] Run `./mvnw clean test` - all unit tests must pass
- [x] Run `./mvnw clean verify` - full build must pass

### Test Requirements

- [x] All existing unit tests continue to pass (no new tests needed for renaming)
- [x] Verify no hardcoded "spring-ai-judge" strings remain in Java code

### Exit Criteria

- [x] All modules renamed to `agent-judge-*`
- [x] `./mvnw clean verify` passes
- [x] All tests pass
- [x] No references to `spring-ai-judge` in any pom.xml files
- [x] Project can be installed to local repo: `./mvnw clean install`

### Completion Summary

**Commit:** `845e19b` - Initial commit: Agent Judge framework

**Learnings:**
1. **Backup before major refactoring** - Created tarball backup before starting
2. **Verify build at each step** - Ran `./mvnw clean verify` after all changes
3. **Use git rename detection** - `git add` with both old deletions and new additions preserves file history

---

## Phase 1: Sandbox Integration - COMPLETE (Dependency Update)

**Objective**: Update `agent-judge-exec` to use `agent-sandbox-core` from the newly created `agent-sandbox` project, enabling pluggable sandbox implementations.

### Entry Criteria

- [x] Phase 0 complete (all modules renamed)
- [x] `agent-sandbox` project available (GitHub: spring-ai-community/agent-sandbox)
- [x] `agent-sandbox-core` installed to local Maven repo or available via snapshot repo

### Tasks

#### 1.1 Update Sandbox Dependency - COMPLETE

- [x] In parent POM `<dependencyManagement>`, change:
  - From: `spring-ai-sandbox-core`
  - To: `agent-sandbox-core`
- [x] Verify `agent-judge-exec` pom.xml references the correct artifact

#### 1.2-1.5 Deferred Tasks (Future Enhancement)

The following tasks are deferred for a future enhancement. The current sandbox abstraction is already well-designed:

- [ ] Audit `agent-judge-exec` for direct `LocalSandbox` instantiation (1.2)
- [ ] Update `CommandJudge` to accept `Sandbox` via constructor injection (1.3)
- [ ] Add integration tests with DockerSandbox (1.4)
- [ ] Update documentation with pluggable sandbox configuration (1.5)

### Exit Criteria (Partially Complete)

- [x] `agent-judge-exec` depends on `agent-sandbox-core` (not `spring-ai-sandbox-core`)
- [ ] All sandbox implementations are pluggable via constructor injection (deferred)
- [x] All unit tests pass
- [x] `./mvnw clean verify` passes

### Completion Summary

**Commits:**
- `e93e6b0` (agent-sandbox) - Rename artifacts from spring-ai-sandbox to agent-sandbox
- `3170b67` (agent-judge) - Update sandbox dependency to agent-sandbox-core

**Learnings:**
1. **Agent-sandbox is technology-agnostic** - No Spring imports, only org URLs/license headers reference "spring-ai-community"
2. **Agent-judge-exec only needs dependency update** - The sandbox abstraction is already well-designed
3. **Clean target directories** - Old test reports in `target/` can cause confusion during grep searches

**Note:** Tasks 1.2-1.5 (pluggable sandbox injection, integration tests) are deferred for a future enhancement. The existing sandbox abstraction provides sufficient flexibility for current use cases.

---

## Phase 2: Move Agent Code to spring-ai-agents - READY TO START

**Objective**: Relocate `AgentJudge` and advisor code to `spring-ai-agents` where it belongs architecturally.

### Entry Criteria

- [x] Phase 1 complete (sandbox integration done)
- [ ] `spring-ai-agents` project cloned locally
- [ ] Understanding of `spring-ai-agents` module structure and conventions
- [ ] `agent-judge-core` installed to local repo (for spring-ai-agents to depend on)

### Pre-Entry Exploration (Based on Phase 0-1 Learnings)

Before starting implementation, review these key files to understand what needs to move:

**In agent-judge-agent module:**
- `AgentJudge.java` - Implements `Judge` from agent-judge-core
- `JudgeAgentClient.java` - Bridge interface for agent integration (to be replaced with direct AgentClient usage)
- `JudgeAgentResponse.java` - Response wrapper

**In agent-judge-advisor module:**
- `JudgeAdvisor.java` - Implements `AgentCallAdvisor` from spring-ai-agent-client
- `JuryAdvisor.java` - Jury-based advisor implementation

**Architecture Insights:**
1. `JudgeAgentClient` is a bridge interface - designed to be implemented by users, not a hard dependency
2. `JudgeAdvisor`/`JuryAdvisor` implement `AgentCallAdvisor` - they plug into the agent execution pipeline
3. Both modules depend only on `agent-judge-core` - clean separation of concerns
4. `spring-ai-agents` exists at `~/community/spring-ai-agents` - ready for Phase 2

### Tasks

#### 2.1 Prepare spring-ai-agents

- [ ] Clone/update `spring-ai-agents` repository
- [ ] Review existing module structure
- [ ] Identify where new judge module should be placed
- [ ] Add `agent-judge-core` as a dependency in spring-ai-agents parent POM

#### 2.2 Create Agent Judge Module in spring-ai-agents

- [ ] Create `spring-ai-agent-judge` module directory
- [ ] Create pom.xml with dependencies:
  - `agent-judge-core`
  - `spring-ai-agent-core` (or equivalent AgentClient module)
- [ ] Set up package structure: `org.springaicommunity.ai.agent.judge`

#### 2.3 Move AgentJudge Implementation

- [ ] Copy `AgentJudge` class from `agent-judge-agent`
- [ ] Update package declaration
- [ ] Update imports to use `AgentClient` directly (no adapters)
- [ ] Ensure `AgentJudge implements Judge` from `agent-judge-core`
- [ ] Remove any adapter/bridge code - use AgentClient directly

#### 2.4 Move Advisor Code

- [ ] Copy advisor classes from `agent-judge-advisor`
- [ ] Update package declarations
- [ ] Update imports
- [ ] Integrate with spring-ai-agents advisor patterns

#### 2.5 Add Tests in spring-ai-agents

- [ ] Unit tests for `AgentJudge` with mocked `AgentClient`
- [ ] Unit tests for advisor integration
- [ ] Integration tests with real agent execution (if applicable)

#### 2.6 Verify Integration

- [ ] Build spring-ai-agents: `./mvnw clean verify`
- [ ] All new tests pass
- [ ] Existing spring-ai-agents tests still pass
- [ ] No circular dependencies between projects

### Test Requirements

- [ ] Unit tests: AgentJudge with mocked AgentClient
- [ ] Unit tests: Advisor classes
- [ ] Integration tests: End-to-end agent judging workflow
- [ ] Minimum 80% line coverage for new module

### Exit Criteria

- [ ] `spring-ai-agent-judge` module exists in spring-ai-agents
- [ ] `AgentJudge` implements `Judge` from `agent-judge-core`
- [ ] `AgentJudge` uses `AgentClient` directly (no adapters)
- [ ] All advisor code relocated and functional
- [ ] All tests pass in spring-ai-agents
- [ ] spring-ai-agents builds successfully
- [ ] Code committed and pushed to spring-ai-agents repository

---

## Phase 3: Remove Agent Modules from agent-judge

**Objective**: Clean up agent-judge by removing the now-relocated agent and advisor modules.

### Entry Criteria

- [ ] Phase 2 complete and verified
- [ ] spring-ai-agents with new judge module is committed and pushed
- [ ] Confirmation that spring-ai-agents tests pass in CI

### Tasks

#### 3.1 Remove Module Directories

- [ ] Delete `agent-judge-agent` directory
- [ ] Delete `agent-judge-advisor` directory

#### 3.2 Update Parent POM

- [ ] Remove `agent-judge-agent` from `<modules>`
- [ ] Remove `agent-judge-advisor` from `<modules>`
- [ ] Remove managed dependencies for deleted modules from `<dependencyManagement>`

#### 3.3 Update BOM

- [ ] Remove `agent-judge-agent` from managed dependencies
- [ ] Remove `agent-judge-advisor` from managed dependencies

#### 3.4 Clean Up Any References

- [ ] Search for any remaining references to deleted modules
- [ ] Remove any bridge interfaces (`JudgeAgentClient`, `JudgeAgentResponse`)
- [ ] Remove any adapter classes

#### 3.5 Final Verification

- [ ] Run `./mvnw clean verify`
- [ ] Verify final module set: `core`, `exec`, `llm`, `bom`
- [ ] Run `./mvnw clean install`

### Test Requirements

- [ ] All remaining tests pass
- [ ] No test classes reference deleted modules
- [ ] Coverage thresholds maintained for remaining modules

### Exit Criteria

- [ ] Only 4 modules remain: `agent-judge-core`, `agent-judge-exec`, `agent-judge-llm`, `agent-judge-bom`
- [ ] No references to agent or advisor modules anywhere
- [ ] No adapter or bridge interfaces remain
- [ ] `./mvnw clean verify` passes
- [ ] Ready for GitHub repository creation and initial push

---

## Target Architecture (Post-Refactoring)

```
agent-judge (agent-agnostic)
├── agent-judge-core      # Judge, Judgment, Score, Jury APIs
├── agent-judge-exec      # Sandbox-based execution judges
├── agent-judge-llm       # ChatClient-based judges (single-call)
└── agent-judge-bom       # Dependency management

spring-ai-agents (depends on agent-judge-core)
├── spring-ai-agent-core  # AgentClient, tools, runtime
├── spring-ai-agent-judge # AgentJudge implements Judge
└── ...other modules

Dependencies:
  agent-judge-exec → agent-sandbox-core (Sandbox interface)
  spring-ai-agent-judge → agent-judge-core (Judge interface)
  spring-ai-agent-judge → spring-ai-agent-core (AgentClient)
```

---

## Getting Started (Post-Refactoring)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springaicommunity</groupId>
            <artifactId>agent-judge-bom</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Core judge functionality -->
    <dependency>
        <groupId>org.springaicommunity</groupId>
        <artifactId>agent-judge-core</artifactId>
    </dependency>

    <!-- Add exec or llm modules as needed -->
</dependencies>
```

---

## GitHub Repository Status

| Project | Repository | Status |
|---------|------------|--------|
| agent-sandbox | `spring-ai-community/agent-sandbox` | Pushed to GitHub |
| agent-judge | `spring-ai-community/agent-judge` | Local only (ready after Phase 3) |
| spring-ai-agents | `spring-ai-community/spring-ai-agents` | Existing (Phase 2 target) |

---

## Future Considerations

- Auto-configuration for Spring Boot
- Additional voting strategies (weighted, threshold-based)
- Integration with more evaluation frameworks
- E2B sandbox support in agent-judge-exec
- Documentation site (Antora)
- Publishing to Maven Central
