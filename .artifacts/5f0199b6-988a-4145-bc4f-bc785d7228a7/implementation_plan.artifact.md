# Fix Unresolved Reference 'mockito' in JvmEnvironmentTest.kt

The user is experiencing a build error where the `mockito` reference is unresolved in `JvmEnvironmentTest.kt`. This file is currently located in the `src/androidTest` directory (Instrumentation Tests), but its dependencies are only declared for the `test` configuration (Unit Tests). Furthermore, the test is explicitly designed to verify behavior in a "standard JVM environment," which corresponds to the `src/test` directory.

## User Review Required

> [!IMPORTANT]
> I am proposing to move `JvmEnvironmentTest.kt` from `src/androidTest` to `src/test`. This aligns the test's location with its purpose (testing non-Android environment detection) and resolves the dependency issue, as Mockito is already configured for unit tests.

## Proposed Changes

### cushy-storage module

#### [MODIFY] [JvmEnvironmentTest.kt](file:///Users/sargius/Documents/N5_Projects/N2_UDEEDIT/Projects/N02_cUD_1_3_CushyStorage/CushyStorage/cushy-storage/src/androidTest/java/pro/udeedit/devtools/cushystorage/JvmEnvironmentTest.kt) -> [NEW] [JvmEnvironmentTest.kt](file:///Users/sargius/Documents/N5_Projects/N2_UDEEDIT/Projects/N02_cUD_1_3_CushyStorage/CushyStorage/cushy-storage/src/test/java/pro/udeedit/devtools/cushystorage/JvmEnvironmentTest.kt)
- Move the file to the unit test source set.
- Update the test logic to stub `Application.applicationContext` and `Context.packageName` to avoid NullPointerExceptions when running on a mock context in the JVM.

#### [MODIFY] [build.gradle.kts](file:///Users/sargius/Documents/N5_Projects/N2_UDEEDIT/Projects/N02_cUD_1_3_CushyStorage/CushyStorage/cushy-storage/build.gradle.kts)
- (Optional but recommended) Add `mockito-kotlin` to `androidTestImplementation` as well, in case future instrumentation tests require it.

## Verification Plan

### Automated Tests
- Run `./gradlew :cushy-storage:testDebugUnitTest` to ensure the moved test passes on the JVM.
- Run `./gradlew :cushy-storage:compileDebugAndroidTestKotlin` to ensure the instrumentation tests still compile (now without the problematic file).

### Manual Verification
- Verify in the IDE that the `mockito` import is now resolved in the new location.
