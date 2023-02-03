# file-date-sorter
Sorts files in the CWD by putting them in folders based on creation date.

file-date-sorter is written for Java 12. It will count files made earlier than 4 am local time as the day before. It will find any already existing folders for a date in thee current directory if the folder name starts with the date in yyyy-mm-dd format, anything can come after the date in the folder name. This won't sort folders, executables or unknown file types (Java returns `null` when geetting the MIME type).

I mostly made this because I have a lot of photos that I've taken and need to edit, but they weren't organized at all. It was a mess lol.
# Usage
1. Put the jar file in the directoy of files you want to sort.
2. Run the jar file.
