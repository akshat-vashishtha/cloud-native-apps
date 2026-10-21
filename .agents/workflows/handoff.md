# Handoff Workflow (/handoff)

Execute this workflow when completing a work session, switching personas, or passing work to the next phase.

---

## 📋 Steps to Execute

1. **Identify Current Context File**: Determine the active phase file (`architecture.md`, `implementation.md`, `services/<service-name>.md`, or `deployment.md`).
2. **Update Context File**:
   - Update metadata header (`Status`, `Owner`, `Updated`).
   - Append any new decisions with proper IDs (`ARCH-XXX`, `IMPL-XXX`, `SVC-xxx-XXX`).
   - Refresh `Open questions` and `Next steps`.
   - Add any `Change requests` targeting upstream documents if gaps were uncovered.
3. **Display the Diff**: Show the user the exact markdown diff/updates made to the context file.
4. **Handoff Summary**: State clearly:
   - Which **Persona** is next (e.g., `@Elena`, `@Leo`, `@Maya`).
   - Which **Context Files** the next persona needs to read and write.
   - The immediate next action required.
