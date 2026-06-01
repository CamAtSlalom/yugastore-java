---
applyTo: "**/*Test*.java,**/*IT.java,**/*.test.*,**/*.spec.*,**/__tests__/**"
---

# Testing Standards (Yugastore)

**Tool:** GitHub Copilot · **Source of truth:** `.github/rules/rules.md` (testing-standards, JUnit5/Jest tool rules)

- **Frameworks**: JUnit 5 (Java/Spring) and Jest via `react-scripts` (React UI). Validate
  and uphold the existing setup — do not replace frameworks or add a second persistence
  stack just for tests.
- **Testing pyramid**: Unit 60-75% · Component/API/Integration 20-30% · E2E/UI 5-10%.
  Push tests as low as possible.
- **Coverage**: ≥ 80% new code; critical paths (login/auth, cart, checkout) ≥ 90% branch.
- **Determinism**: atomic, independent, deterministic. No timing reliance, no pre-seeded
  YugabyteDB rows. Clean state before each test; embedded/containerized DB for integration.
  Flakiness target < 1% — fix flaky tests, never ignore.
- **Diagnostics**: failures must surface a clear root cause; P1/P2 bugs reproducible in 1 attempt.

## JUnit 5 specifics
- `@Nested` to group, `@DisplayName` on class + method, Arrange-Act-Assert, one logical
  assertion (use `assertAll` for related checks).
- Naming: `*Test.java` (unit), `*IntegrationTest.java`/`*IT.java` (integration); mirror
  `src/main/java` package layout under `src/test/java`.
- Prefer AssertJ fluent assertions; exceptions via `assertThatThrownBy(...)`.
- Mockito: `@ExtendWith(MockitoExtension.class)`, `@Mock` + `@InjectMocks`; never mock DTOs.
- **Prefer Spring slices over full `@SpringBootTest`**: `@WebMvcTest` (controllers),
  `@DataJpaTest` (YSQL/JPA repos), `@MockBean` to replace context beans. Reserve
  `@SpringBootTest` for true integration.
- Speed: unit < 5s, Spring integration < 30s, full unit suite < 5 min. `@Tag("slow")` to exclude.
- Run: `./mvnw -pl <module> test`.

## Jest specifics (`react-ui/frontend`)
- `describe`/`it` names that read as sentences; AAA; one concept per test.
- `@testing-library/react` (not Enzyme); query by role → label → text → testId (last resort).
  Prefer `userEvent` over `fireEvent`. Test behavior, not implementation.
- Mock boundaries (APIs, storage), not internals. `await` all async; `waitFor()` for async DOM.
- Review snapshot diffs in PRs — never blind-update.
