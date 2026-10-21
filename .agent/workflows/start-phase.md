# Start Phase Workflow (/start-phase)

Execute this workflow when initiating a new project phase or resuming work on an existing phase.

---

## 📋 Steps to Execute

1. **Ask for Phase**: Inquire which phase is being started if not specified (`Design`, `Technical Design`, `Per Service`, `Deployment`).
2. **Read Upstream Dependencies**:
   - For **Design**: Read `context/00-idea.md`.
   - For **Technical Design**: Read `context/architecture.md`.
   - For **Per Service**: Read `context/architecture.md`, `context/implementation.md`, and `context/services/<service-name>.md`.
   - For **Deployment**: Read `context/architecture.md` and all `context/services/*.md`.
3. **5-Line Upstream Summary**: Provide a concise 5-line summary of key findings, decisions, and constraints from the upstream files.
4. **Adopt Persona**: Adopt the assigned persona role (`@Alex`, `@Elena`, `@Leo`, or `@Maya`) and proceed with the phase tasks.
