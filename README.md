skript-io
=====

Basic input/output support for files, writers and other channels in [Skript](https://skriptlang.org).

## Introduction

### The 'Path'

The basic unit of syntax in skript-io is the 'path' or uniform resource identifier.

Paths are literal directions to files `./test.txt`, directories `./plugins/`, absolute file paths `/var/www/html/index.html`, website links `https://skriptlang.org` or other system resources.

### Object Safety

Resources (e.g. files) are almost never referenced directly via their object, instead using a path. This protects the input/output operations behind a layer of security.

Even when a resource must be referenced directly (e.g. when editing a file) both the resource and its input/output pipes are behind a safe controller.

### Resource Sections

When a resource is accessed (i.e. read from or written to) this requires us to access resources outside the Java Virtual Machine, such as by asking the file system for a file.

As these 'i/o' operations are delicate and at risk of memory leaks, etc. they are always done inside special access sections and never directly.

The resource is accessed by opening a section. Inside that section the resource is available for editing/changing/reading/communicating, and when the section ends the resource will close and all data will be cleaned up safely.

## Features

### Files

Skript-io has basic support for most simple file actions, including creating, deleting, editing and moving files and directories.


#### File/Directory Exists

Since `1.0.0`

Checks whether the given path is a file that exists.

```sk
if file ./test.txt exists:
	delete file ./test.txt
```


#### Line of File

Since `1.0.0`

Reads an individual line of a file. Line indexing begins at 1. The value will be empty if the file ended or could not be read.

```sk
broadcast line 1 of the current file
```


#### Lines of File

Since `1.0.0`

The lines of a currently-open file as a list of text.

```sk
open file ./test.txt:
	loop the lines of file:
		broadcast loop-value
	set the lines of file to "hello" and "there"
```


#### Current File

Since `1.0.0`

The currently-open file inside a file reading/editing section.

```sk
create a new file ./test.txt:
	add "hello" to the file
```


#### Contents of File

Since `1.0.0`

The contents of (the text inside) a currently-open file. This will be blank if the file is empty or unreadable.

```sk
open file ./test.txt:
	broadcast the contents of file
```


#### Size of File

Since `1.0.0`

The size (in bytes) of the currently-open file.

```sk
open file ./test.txt:
	broadcast "the file-size is %size of file%"
```


#### Files in Directory

Since `1.0.0`

Returns a list of (file/folder) paths in the given directory.

```sk
loop the files in ./test/:
	delete the file at loop-value
```


#### Size of File Path

Since `1.0.0`

The size (in bytes) of a file by path. Non-files have a size of zero.

```sk
set {_size} to the file size of ./test.txt
```


#### Create File

Since `1.0.0`

Creates a new file at a path. If the file already exists or was successfully created, opens an editing section.

```sk
create file ./test.txt:
	add "hello" to the file
```


#### Delete File/Directory

Since `1.0.0`

Deletes the folder or file at the given path.

```sk
recursively delete folder ./test/
delete folder ./test/
delete the file at ./config.txt
```


#### Edit File

Since `1.0.0`

Opens a file at a path for reading and writing. If the file does not exist or is inaccessible, the section will not be run.

```sk
edit file ./test.txt:
	set the contents of the file to "line 1"
	add "line 2" to the lines of the file
```


#### Create Directory

Since `1.0.0`

Creates a new folder at the given path, if one does not exist.

```sk
create a new folder ./test/
```


#### Read File

Since `1.0.0`

Opens a file at a path only for reading.
The file cannot be written to.
If the file does not exist or is unreadable, the section will not be run.

```sk
read file ./test.txt:
	loop the lines of the file:
		broadcast loop-value
```


#### Rename File

Since `1.0.0`

Renames a file or directory. To rename a directory please use the 'move' effect.

```sk
rename file ./example/test.txt to "blob.txt"
```


#### Move File/Directory

Since `1.0.0`

Moves a file or folder to a target position, overwriting the previous file.
The source file or folder will replace whatever is at the destination path.
If the 'into' version is used, the source will be moved inside the target directory, rather than replacing it.


```sk
move file ./example/test.txt to ./config/test.txt
move file ./test.txt to ./blob.txt
move file ./test.txt into ./config/
move folder ./example/ to ./config/
move folder ./example/ into ./config/
```

