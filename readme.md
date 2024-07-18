# pope books

A app to read all books of HH Pope Shenouda III. Install the app from [Google Play](https://play.google.com/store/apps/details?id=com.softwarepharaoh.popebooks).

Mainly there are 2 old codebases:

1. Java programming language + plain text files as database
2. Java programming language + SQLite db

But this new codebase will be written in Kotlin with UI in Jetpack Compose + SQLite db with more features.

## My vision for the app

I want this app to be the goto app for reading all books written by or about HH Pope Shenouda III.

In brief, the goals of the app are:

- all books of Pope Shenounda III
- the app size must be as small as possible to enable more people to keep the app on their phones
- the app must support new and old versions of Android OS as possible

Current state of the app according to the vision:

- only 18 books were added
- the app is around 10 MB which is great
- the app is available for smartphones with Android 6.0 (2015) up to Android 14 (2023, the latest version)

## Why opensource ?

I created the app to be available to all people who need it, all the time, everywhere. I'd like to create a developer community to maintain the app indefinitely.

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
- Next Version
  - rewrite the app in Kotlin with Jetpack Compose
  - 0 poems added
  - search in all books
  - search in a certain book

