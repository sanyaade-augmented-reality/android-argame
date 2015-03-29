# Code Style Guidelines #
## Whitespace ##
  * Indention: 2 spaces (no tabs), 4 spaces for line continuations
  * Line Length: 100 characters
  * Braces: Same line starting, new line ending, space before opening brace
  * Operators: Space before and after
  * Parenthesis: No spacing inside

## Comments ##
  * All functions must be prefaced with comments.
  * All classes must be prefaced with javadocs.
  * No @author tags in javadocs

## Autoformatters ##
Download the below xml file and save it to your local disk. Import this formatter in eclipse with Window --> Preferences --> Java --> Code Style --> Formatter and choose "Import..." and find this file on disk. Now, when you select code in eclipse, Ctrl + Shift + F will auto format it. Avoid auto-formatting entire classes if you're touching small parts, since it makes the code review unnecessarily large.
  * Eclipse: http://omnidroid.googlecode.com/svn/tools/Eclipse_Formatter-ITP_Conventions.xml


## Other Conventions ##
  * For anything not specified above, follow Sun's Java code convention document: http://java.sun.com/docs/codeconv/html/CodeConvTOC.doc.html


## Best Practices for Android Development ##
  * Designing for Performance http://developer.android.com/guide/practices/design/performance.html
  * User Interface Guidelines http://developer.android.com/guide/practices/ui_guidelines/index.html
  * Designing for Responsiveness http://developer.android.com/guide/practices/design/responsiveness.html
  * Designing for Seamlessness http://developer.android.com/guide/practices/design/seamlessness.html