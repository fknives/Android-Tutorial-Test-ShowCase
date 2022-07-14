./gradlew clean
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.fnives.test.showcase.rule.ScreenshotTest
./gradlew app:hasScreenshots