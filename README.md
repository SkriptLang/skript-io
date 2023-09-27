skript-io
=====

Basic input/output support for files, writers and other channels in [Skript](https://skriptlang.org).

## Introduction

### The 'Path'

The basic unit of syntax in skript-io is the 'path' or uniform resource identifier.

Paths are literal directions to files `./test.txt`, directories `./plugins/`, absolute file
paths `/var/www/html/index.html`, website links `https://skriptlang.org` or other system resources.

### Object Safety

Resources (e.g. files) are almost never referenced directly via their object, instead using a path. This protects the
input/output operations behind a layer of security.

Even when a resource must be referenced directly (e.g. when editing a file) both the resource and its input/output pipes
are behind a safe controller.

### Resource Sections

When a resource is accessed (i.e. read from or written to) this requires us to access resources outside the Java Virtual
Machine, such as by asking the file system for a file.

As these 'i/o' operations are delicate and at risk of memory leaks, etc. they are always done inside special access
sections and never directly.

The resource is accessed by opening a section. Inside that section the resource is available for
editing/changing/reading/communicating, and when the section ends the resource will close and all data will be cleaned
up safely.

## File

### Conditions

#### File/Directory Exists

Since `1.0.0`

Checks whether the given path is a file that exists.

```sk
if file ./test.txt exists:
	delete file ./test.txt
```

### Expressions

#### Line of File

Since `1.0.0`

Reads an individual line of a file. Line indexing begins at 1. The value will be empty if the file ended or could not be
read.

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

### Effects

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

Opens a file at a path for reading and writing. If the file does not exist or is inaccessible, the section will not be
run.

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

## Web

### Expressions

#### Source of Request

Since `1.0.0`

The address a request was made from, in IP format.

```sk
open a website:
	broadcast the request's source
```

#### Incoming Request

Since `1.0.0`

The current request being made of your website.
This is typically a browser asking for a page.

```sk
open a website:
	set {_file} to path of request
	if file {_file} exists:
		set the status code to 200
		transfer {_file} to the response
	else:
		set the status code to 404
		add "Page not found." to the response
```

#### Current Website

Since `1.0.0`

The current website, in a website section.

```sk
open a website:
	close the current website
```

#### Status Code

Since `1.0.0`

The status code of a web request.
A `200` status is OK.

When receiving a response, this is the status of your previous request.

When responding to a request, this must be set before data can be transferred.

```sk
open a website:
	set the status code to 200 # OK
	transfer ./site/index.html to the response
```

#### Web Response

Since `1.0.0`

Your website's response to a request made to your site.
This resource can be written to (in order to send data back to the requester).

This can be used to send data to a client, e.g. sending a page to a browser when requested.

```sk
open a website:
	add {_greeting} to the response
	transfer ./site/index.html to the response
```

#### Path of Request

Since `1.0.0`

The (file) path a web request is asking for.
This is typically a browser asking for a page, e.g. `/something/page.html`.

Properly-formatted requests typically start with an absolute `/...` - be careful when serving content.

```sk
open a website:
	broadcast the request's path
```

#### Method of Request

Since `1.0.0`

The type of a web request, such as "GET" or "POST" or "PATCH".

Requests for data (e.g. asking for a webpage) typically use "GET".
Sending data (e.g. submitting a form, searching) typically uses "POST".

```sk
open a website:
	if method of request is "GET":
```

### Effects

#### Open Website

Since `1.0.0`

Opens a website at the provided path and port, defaulting to the root path `/` on port 80.
Whenever a request is received, the code inside the section will be run.

Website paths should end in a separator `/`, and will handle any requests to their directory.
A website on the root path `/` will accept any unhandled requests.

Multiple websites cannot be opened on the same port and path.
Multiple websites can be opened on *different* paths with the same port, such as `/foo/` and `/bar/`

```sk
open a website for /landing/:
	transfer ./site/welcome.html to the response
```

## Common

### Effects

#### Contents of Resource

Since `1.0.0`

The contents of (the text inside) a resource, such as an open file.
This will return nothing if the resource is unreadable.

```sk
open a website:
	broadcast the content of the request's body
open file ./test.txt:
	broadcast the contents of file
```

#### Transfer

Since `1.0.0`

Safely copies data from one (readable) resource to another (writable) resource.
Useful for responding to a web request with a file (or copying one file into another).

```sk
transfer {input} to {output}
transfer ./test.html to the response
```
