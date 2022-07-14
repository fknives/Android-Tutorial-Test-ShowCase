# About contributing into the project and structure
These are notes to myself what the project has currently and how to do verifications.

## Pull Request

Each pull request must be go though the automated CI checks, which are:
- Static Code Analysis (detekt, ktlint, lint)
- JVM Tests (unit tests + Robolectric Tests)
- Android Tests on Emulators

This is to ensure the project is still working after every merge.

## Verifications of Screenshot Pulling

A Manual Action has been created to verify the ScreenshotRule with the Gradle script is able to pull screenshots from the Device.
This can be run in Actions/Verify Screenshots can be created and pulled/Run Workflow

This should be tested on newer Emulators time to time.

## Libraries
Libraries are util classes for Testing.
This can be released into packages by simply creating a Release from the Project. That will trigger an Action which publishes to GitHub Packages.

## Modifications to Tests

Each modifications to tests should also modify the corresponding section if applicable.
This to ensure whoever wants to learn, won't get stuck by inconsistency between the code and description.
