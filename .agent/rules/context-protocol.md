# Context Protocol & Phase Lifecycle Rules

All project context lives in `/context` as markdown files. Chat is temporary; files are memory.
Work flows strictly through phases, with each phase reading upstream files and updating its own target file.

---

## 🔄 Phase Pipeline (Reads ➔ Writes)

1. **Design**: `@Alex` reads `context/00-idea.md` ➔ writes `context/architecture.md`
2. **Technical Design**: `@Elena` and `@Leo` read `context/architecture.md` ➔ write `context/implementation.md`:
   - `@Elena` owns the **"Messaging & AI"** section.
   - `@Leo` owns the **"Data & Cache"** section.
   - `@Alex` reviews the overall integration.
3. **Per Service**: `@Alex`, `@Elena`, and `@Leo` read `context/architecture.md`, `context/implementation.md`, and `context/services/<service-name>.md` ➔ update `context/services/<service-name>.md`, then write code.
4. **Deployment**: `@Maya` reads `context/architecture.md` and all `context/services/*.md` ➔ writes `context/deployment.md`

---

## 📜 Core Rules

1. **Upstream Briefing**: At the start of any task, read the upstream files for the current phase and summarise them in 5 lines before continuing.
2. **Section Ownership & Change Requests**: Each persona edits only its own file or designated section. If you encounter a conflict or gap in an upstream file, record it under `"Change requests"` in your own file and state which persona must update the upstream file.
3. **Decision Tracking with IDs**: Record all decisions with clear IDs (e.g., `ARCH-001`, `IMPL-004`, `SVC-order-002`). Reference upstream decisions by ID instead of copying their text.
4. **Link, Don't Duplicate**: Keep service files concise (under ~150 lines). Link to shared architectural or implementation decisions.
5. **Session Wrap-Up / Persona Hand-Off**: Before ending a session or switching persona, update your context file (`Decisions`, `Open questions`, `Next steps`, `Status`) and show the diff.
6. **Plan Approval Gate**: Do NOT write application code until the service file's `"Plan"` section is explicitly marked `Approved: yes` by the user.
7. **No Skill Dumping**: Skills contain generic domain know-how. Context files must hold only project-specific decisions—never copy raw skill guides into context files.
8. **Chat-to-File Persistence**: Whenever a decision is agreed upon in chat, immediately write it into the corresponding context file before proceeding.
