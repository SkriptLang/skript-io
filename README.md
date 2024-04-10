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

[//]: # (Auto-generated documentation starts here.)

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

#### Size of Path

Since `1.0.0`

The size (in bytes) of a file by path. Non-files have a size of zero.

```sk
set {_size} to the size of ./test.txt
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
	set the text contents of the file to "line 1"
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

#### Content Type of Request

Since `1.0.0`

The data format a web request will use, such as "application/json" or "text/html".

When making a request you may have to use a specific content type (and format your data accordingly!)
When receiving a request, this should indicate the format of the incoming data.
Not all web requests will have data attached.

```sk
open a web request to http://localhost:3000:
	set the request's content-type to "application/json"
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

#### Header of Request

Since `1.0.0`

A key/value-based header in a request, such as "Content-Type" -> "text/html".

Request headers information about the client requesting the resource.
Response headers hold information about the response.

```sk
open a web request to http://localhost:3000:
	set the request's "Content-Encoding" header to "gzip"
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

### Events

#### Visit Website

Since `1.0.0`

Called when a website running from this server is visited.
This could be from a browser asking for a page or a web request.

While requests can be read and responded to in this event listener,
it is much safer to use the dedicated website section.

```sk
on website visit:
	set the status code to 200
```

### Effects

#### Close Website

Since `1.0.0`

Closes the website at a given path and/or port, or the current website.
The website will stop accepting connections.

Any currently-open tasks may continue to run in the background.

```sk
open a website for /landing/:
	transfer ./site/welcome.html to the response
	close the current website
```

#### Expect Response

Since `1.0.0`

Notifies a connection that you expect a response (and waits for it).

```sk
open a request to https://skriptlang.org:
	accept the response:
		broadcast the response's content
```

#### Send Web Request

Since `1.0.0`

Prepares an HTTP request to be sent to a website URL. This may have content written to it.

Once the request has been dispatched, the response can be read.

```sk
open a web request to https://skriptlang.org:
	set the request's method to "GET"
	await the response:
		broadcast the response's text content
```

#### Open Website

Since `1.0.0`

Opens a website at the provided path and port, defaulting to the root path `/` on port 80.
Whenever a request is received, the code inside the section will be run.

Responses to a request should start by sending a status code (e.g. 200 = OK) and then any data.

Website paths should end in a separator `/`, and will handle any requests to their directory.
A website on the root path `/` will accept any unhandled requests.

Multiple websites cannot be opened on the same port and path.
Multiple websites can be opened on *different* paths with the same port, such as `/foo/` and `/bar/`

```sk
open a website on port 12345:
	set the status code to 200	add "<body>" to the response	add "<h1>hello!!!</h1>" to the response	add "<p>there are %size of all players% players online</p>" to the response	add "</body>" to the response
```

## Common

### Expressions

#### Contents of Resource

Since `1.0.0`

The contents of (the stuff inside) a resource, such as an open file.
This uses a type parser (e.g. the number context of X will parse X as a number).

This will return nothing if the resource is unreadable.

```sk
open a website:
	broadcast the text content of the request's body
open file ./test.txt:
	broadcast the text contents of file
```

### Effects

#### Transfer

Since `1.0.0`

Safely copies data from one (readable) resource to another (writable) resource.
Useful for responding to a web request with a file (or copying one file into another).

```sk
transfer {input} to {output}
transfer ./test.html to the response
```

#### Change: Reverse Indexed Set

Since `1.0.0`

A special edition of the set changer that can maintain the indices of target data.

```sk
set yaml contents of file to {_options::*}
```

#### Encode

Since `1.0.0`

Used for converting data from one format to another.
This includes special list variable data structures.

```sk
encode {_raw text} as json to {_json::*}
decode {_config::*} from yaml to {_raw text}
```

#### Throw Error

Since `1.0.0`

Produces an error that terminates the current trigger, unless it is 'caught' by a try/catch section.

```sk
throw an error with message "oops!"
```

#### Catch Error

Since `1.0.0`

Obtains the error from the previous `try` section and stores it in a variable.
This can also be used as a section that will run only if an error occurred.

```sk
try:
	add "hello" to the file
catch {error}
```

#### Try (Section)

Since `1.0.0`

Attempts to run the code in the section. If any part of the code encounters an error,the section will exit immediately
and any remaining code will not be run.
This means that the script may continue in an unexpected state (i.e. some variables may be different from expected)and
so the `try` section should be used with caution.
Any kind of delay is prohibited within the try section.

```sk
try:
	add "hello" to the file
catch {error}
```

#### Change: Indexed Set

Since `1.0.0`

A special edition of the set changer that can maintain the indices of source data.

```sk
set {_options::*} to yaml contents of file
```
