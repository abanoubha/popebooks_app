# Android app of Pope Shenouda's books

A app to read all books of HH Pope Shenouda III in Arabic. Install the app from [Google Play](https://play.google.com/store/apps/details?id=com.churchservants.popebooks).

Mainly there are 2 old codebases:

1. Java programming language + plain text files as database
2. Java programming language + SQLite db

But this new codebase (3rd) is written in Kotlin with UI in Jetpack Compose + SQLite db with more features.

If you are wondering why I migrated the storage from plain text files to a local SQLite db, read my reasoning on [this post](https://abanoubhanna.com/posts/text-files-vs-sqlite-android/).

## My vision for the app

I want this app to be the goto app for reading all books written by or about HH Pope Shenouda III.

In brief, the goals of the app are:

- all books of Pope Shenounda III
- the app size must be as small as possible to enable more people to keep the app on their phones
- the app must support new and old versions of Android OS as possible

Current state of the app according to the vision:

- all books were added
- the app is around 40 MB which is great as it has all books
- the app is available for smartphones with Android 5.0 (2014) up to Android 15 (2024, the latest version)

## Roadmap | timeline of release versions with tasks

- older app releases
  - use plain text files as a database for each chapter of each app (I will explain this in a blog post and/or video)
  - touch gestures to navigate pages of the books
  - written in Java with available Android/Java APIs/ABIs
- v2.0.0
  - 18 books added
  - remember last opened page (from old codebase)
  - migrate from plain text files to SQlite db (from old codebase)
  - still written in Java with XML Android Layouts
  - support Android 6.0 up to Android 14 (SDK 34)
- v2.0.1
  - upgrade AGP
  - upgrade Kotlin
  - upgrade kotlin coroutines
- v2.0.2
  - fix an issue with R8 minification called "missing classes". I wrote a blog about this incident.
- v3.0.0 (1)
  - app source code is now open source
  - rewrite the app in Kotlin with Jetpack Compose
  - touch gesture to swipe between pages of a book
  - remember the last open page
  - search in the content of all books
  - search in the content of a specific book
- v3.1.0 (2)
  - ui: use theme colors in search input and results views/widgets
  - run SQL functions in IO coroutines for better performance
  - show loading indicator while running the SQL search process
  - sanitize user input in search
- Next Version
  - 0 poems added
  - book: "كتاب القرآن والمسيحية"

