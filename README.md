# Mingpao Scrapping
Scrapping electronic Ming Pao daily. Subscription account is required.
# Build
`./gradlew build`

The built jar file is placed under `build/libs` directory (`*-standalone.jar`)

# Execution

`java -jar <jar-file>` for help

```
Usage: <main class> [options]
  Options:
  \* -date
      Date of paper to be extracted (yyyy-mm-dd)
  \* -dir
      Directory for output
  \* -username
      Username to login
  \* -password
      Password to login
    -start
      Starting page to be extracted
```

One JPG is generated for each page at output directory, and one PDF file is generated there.

