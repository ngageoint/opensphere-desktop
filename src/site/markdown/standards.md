## Coding Standards

The application makes use of a set of formatting standards and naming conventions, as well as various tools to enforce good coding practices (PMD, Checkstyle, FindBugs, etc.). Code formatter templates for Eclipse are provided within the Core application. Import these files directly into Eclipse to make use of the Project Standards. Contributions to the application baseline may be either rejected or re-formatted to match application standards.

### Eclipse Imports for Code Style

Import the editor template: 

1. Window->Preferences, Java->Editor->Templates
2. Click import and select core/eclipse/templates.xml

Import the Java code template:

1. Window->Preferences, Java->Code Style->Code Templates
2. Click import and select core/eclipse/codetemplates.xml

### Coding Format

The OpenSphere Desktop code format is provided as a pre-configured template for use with Eclipse. To import the template into Eclipse:
 
1. Window->Preferences, Java->Code Style->Formatter
2. Click import and select core/eclipse/formatter.xml

#### Format Summary

The OpenSphere Style Guide is outlined in detail below.  A brief summary is provided here.

* Tab Policy: Spaces only, indent each level by 4 spaces
* Braces: Opening and closing curly braces must always be on a new line at the level of the declaring block. Closing braces must be on their own line (e.g.: the 'else' keyword must not be on the same line as the closing brace from the preceding if block. Array Initializers may be declared on a single line if desired. Lambda bodies must be declared on the next line, and indented one level. 
* Whitespace: A single space should follow every comma, closing parenthesis, operator (assignment, addition, subtraction, multiplication, division, lamda arrow, etc.), and after the ellipses in vararg parameters. A single space should precede every operator, opening parenthesis of a control block ('if', 'for', 'while' 'do while', 'synchronized', 'try-with-resources', 'catch', 'assert', 'return', and 'throw'). No whitespace should ever precede a comma, colon, semi-colon, unary operator, postfix operator, or the opening parenthesis of a method declaration.  No whitespace should ever follow a unary operator, prefix operator, closing parenthesis of a cast.
* Blank Lines: A single blank line should always follow the following: package declaration, the last import declaration, each import group (as defined by the second-level package name, such as 'java.util'), each declared member field in a class, each closing brace. Multiple blank lines should be collapsed to a single blank line.
* New lines: A new line should be used in the following circumstances: An empty class body (anonymous or declared), an empty method body or other block, after labels, at the end of every file, and after each annotation (class, method, or local variables), before control statements following blocks, such as an 'else' in an 'if' statement, a 'catch' and / or 'finally' in a 'try' statement, a 'while' in a 'do' statement. 'else if' should be on the same line (no newline between 'else' and 'if'). 
* Line Wrapping: The width of a single line should not exceed 130 characters. Wrapped lines should be indented two spaces. 
* Comments: Every member field, class, enum, or other documentable item MUST be documented using JavaDoc style comments, REGARDLESS OF VISIBILITY, including ALL PRIVATE MEMBERS. HTML may be used to provide formatting in JavaDoc comments. Single-line JavaDoc may be collapsed such that the opening mark ('/**'), comment, and closing mark ('*/') are all on the same line. Every parameter must be documented, including type arguments. Every return statement and exception condition must be documented. Overriding / Implemented members should use the '{@inheritDoc}' on the subclass, and provide a '@see' tag to the declared method.  

### Checkstyle Configuration

The configuration of the Checkstyle utility is provided in three files, within the root project. These files are used during automated builds to validate the format and structure of the code within the baseline. The files are src/main/resources/checkstyle-config.xml, src/main/resources/checkstyle-suppressions.xml and src/main/resources/checkstyle-todo-config.xml. Developers are encouraged to install the Checkstyle plugin into Eclipse, and import these configuration files into the plugin's configuration for realtime feedback on formatting issues before checkin occurs.

#### Configuring the Eclipse Checkstyle Plugin

To configure the Eclipse Checkstyle Plugin, open the Eclipse preferences through the Window->Preferences menu, and select "Checkstyle" in the left side of the window. In the configuration screen, Check the following boxes: 

* Warn before losing configured file sets
* Include rule names in violation messages
* Include module id (if available) in violation messages
* Limit Checkstyle markers per resource to '100'
* Run Checkstyle in background on full builds.

To add the Checkstyle configuration to the Plugin, click 'New' in the 'Global Check Configurations' section. In the resulting popup, change the 'Type' field to 'Project Relative Configuration', in the 'Name' field, enter 'OpenSphere Checks'. Click 'Browse' and navigate to 'src/main/resources/checkstyle-config.xml', and click 'OK'. Click 'OK' to save the configuration changes.

### PMD Configuration

The configuration of the PMD utility is provided in the root project. Application-specific rules are defined in the core/pmd-config.xml file, which may be used with the Eclipse PMD plugin. 

### FindBugs Configuration

FindBugs is enforced at build time through the continuous build architecture. Conformity may be verfied using the Eclipse FindBugs plugin. 

# OpenSphere[^1] Java Style Guide
## 1 Introduction

This document serves as the complete definition of OpenSphere's coding standards for source code in the Java™ Programming Language. A Java source file is described as being in OpenSphere Style if and only if it adheres to the rules herein.

Like other programming style guides, the issues covered span not only aesthetic issues of formatting, but other types of conventions or coding standards as well. However, this document focuses primarily on the hard-and-fast rules that we follow universally, and avoids giving advice that isn't clearly enforceable (whether by human or tool).

Finally, though the use of the Eclipse IDE is certainly not required, a pre-configured implementation of this guide is provided for use in the Eclipse IDE, and is located at /eclipse/open-sphere-formatter.xml.
## 1.1 Terminology notes
In this document, unless otherwise clarified:

1. The term class is used inclusively to mean an "ordinary" class, enum class, interface or annotation type (`@interface`).
2. The term member (of a class) is used inclusively to mean a nested class, field, method, or constructor; that is, all top-level contents of a class except initializers and comments.
3. The term comment always refers to implementation comments. We do not use the phrase "documentation comments", instead using the common term "Javadoc."

Other "terminology notes" will appear occasionally throughout the document.
## 1.2 Guide notes
Example code in this document is **non-normative**. That is, while the examples are in OpenSphere Style, they may not illustrate the only stylish way to represent the code. Optional formatting choices made in examples should not be enforced as rules.
# 2 Source file basics
## 2.1 File name
The source file name consists of the case-sensitive name of the top-level class it contains (of which there is _exactly one_), plus the `.java` extension.
## 2.2 File encoding: UTF-8
Source files are encoded in **UTF-8**.
## 2.3 Special characters
### 2.3.1 Whitespace characters
Aside from the line terminator sequence, the ASCII horizontal space character (0x20) is the only whitespace character that appears anywhere in a source file. This implies that:
1. All other whitespace characters in string and character literals are escaped.
2. Tab characters are not used for indentation.

### 2.3.2 Special escape sequences
For any character that has a _special escape sequence_ (`\b`, `\t`, `\n`, `\f`, `\r`, `\"`, `\'` and `\\`), that sequence is used rather than the corresponding octal (e.g. `\012`) or Unicode (e.g. `\u000a`) escape.
### 2.3.3 Non-ASCII characters

For the remaining non-ASCII characters, either the actual Unicode character (e.g. `∞`) or the equivalent Unicode escape (e.g. 
 `\u221e`) is used. The choice depends only on which makes the code **easier to read and understand**, although Unicode escapes outside string literals and comments are strongly discouraged.

> Tip: In the Unicode escape case, and occasionally even when actual Unicode characters are used, an explanatory comment can be very helpful.

Examples:

<table>
<tr><th>Example</th><th>Discussion</th></tr>
<tr><td><code>String unitAbbrev = "μs";</code></td><td>Best: perfectly clear even without a comment.</td></tr>
<tr><td><code>String unitAbbrev = "\u03bcs"; // "μs"</code></td><td>Allowed, but there's no reason to do this.</td></tr>
<tr><td><code>String unitAbbrev = "\u03bcs"; // Greek letter mu, "s"</code></td><td>Allowed, but awkward and prone to mistakes.</td></tr>
<tr><td><code>String unitAbbrev = "\u03bcs";</code></td><td>Poor: the reader has no idea what this is.</td></tr>
<tr><td><code>return '\ufeff' + content; // byte order mark</code></td><td>Good: use escapes for non-printable characters, and comment if necessary.</td></tr>
</table>

> Tip: Never make your code less readable simply out of fear that some programs might not handle non-ASCII characters properly. If that should happen, those programs are broken and they must be fixed.

# 3 Source file structure
A source file consists of, in order:
1. License or copyright information, if present
2. Package statement
3. Import statements
4. Exactly one top-level class

**Exactly one blank line** separates each section that is present.
## 3.1 License or copyright information, if present
If license or copyright information belongs in a file, it belongs here.
## 3.2 Package statement
The package statement is **not line-wrapped**. The column limit (Section 4.4, *Column limit: 130*) does not apply to package statements.
## 3.3 Import statements
### 3.3.1 No wildcard imports
Wildcard imports, static or otherwise, are not used.
### 3.3.2 No line-wrapping
Import statements are **not line-wrapped**. The column limit (Section 4.4, *Column limit: 130*) does not apply to import statements.
### 3.3.3 Ordering and spacing
Imports are ordered as follows:
1. All static imports in a single block.
2. Non-static imports in blocks by top-level domain name. The order of the blocks should be as follows: java, javafx, javax, org, com, then all others.

If there are both static and non-static imports, a single blank line separates the two blocks.

Within each block the imported names appear in ASCII sort order. (**Note:** this is not the same as the import statements being in ASCII sort order, since '.' sorts before ';'.)
### 3.3.4 No static import for classes

Static import is not used for static nested classes. They are imported with normal imports.
## 3.4 Class declaration
### 3.4.1 Exactly one top-level class declaration
Each top-level class resides in a source file of its own.
### 3.4.2 Ordering of class contents
The order you choose for the members and initializers of your class can have a great effect on learnability. As a guideline, classes should be ordered as follows: constants, fields, initializers, static factory methods, constructors, followed by all other methods (favoring public methods at the top through private methods at the bottom). However, there's no single correct recipe for how to do it; different classes may order their contents in different ways.

What is important is that each class uses **some logical order**, which its maintainer could explain if asked. For example, new methods are not just habitually added to the end of the class, as that would yield "chronological by date added" ordering, which is not a logical ordering.
#### 3.4.2.1 Overloads: never split
When a class has multiple constructors, or multiple methods with the same name, these appear sequentially, with no other code in between (not even private members).
# 4 Formatting
**Terminology Note:** block-like construct refers to the body of a class, method or constructor. Note that, by Section 4.8.3.1 on __array initializers__, any array initializer may optionally be treated as if it were a block-like construct.
## 4.1 Braces
### 4.1.1 Braces are used where optional
Braces are used with `if`, `else`, `for`, `do` and `while` statements, even when the body is empty or contains only a single statement.
### 4.1.2 Nonempty blocks: Allman style
Braces follow the [Allman] (https://en.wikipedia.org/wiki/Indent_style#Allman_style) style for nonempty blocks and block-like constructs:
1. Line break before the opening brace.
2. Line break after the opening brace.
3. Line break before the closing brace.
4. Line break after the closing brace.

Examples:

    return new MyClass() 
    {
        @Override 
        public void method() 
        {
            if (condition()) 
            {
                try 
                {
                    something();
                } 
                catch (ProblemException e) 
                {
                    recover();
                }
            } 
            else if (otherCondition()) 
            {
                somethingElse();
            } 
            else 
            {
                lastThing();
            }
        }
    };

A few exceptions for enum classes are given in Section 4.8.1, Enum classes.

## 4.2 Block indentation: +4 spaces
Each time a new block or block-like construct is opened, the indent increases by four spaces. When the block ends, the indent returns to the previous indent level. The indent level applies to both code and comments throughout the block. (See the example in Section 4.1.2, Nonempty blocks: Allman Style.)
## 4.3 One statement per line
Each statement is followed by a line break.
## 4.4 Column limit: 130
Java code has a column limit of 130 characters. Except as noted below, any line that would exceed this limit must be line-wrapped, as explained in Section 4.5, Line-wrapping.
Exceptions:
1. Lines where obeying the column limit is not possible (for example, a long URL in Javadoc, or a long JSNI method reference).
2. package and import statements (see Sections 3.2 Package statement and 3.3 Import statements).
3. Command lines in a comment that may be cut-and-pasted into a shell.

## 4.5 Line-wrapping
**Terminology Note:** When code that might otherwise legally occupy a single line is divided into multiple lines, this activity is called line-wrapping.

There is no comprehensive, deterministic formula showing exactly how to line-wrap in every situation. Very often there are several valid ways to line-wrap the same piece of code.

> Note: While the typical reason for line-wrapping is to avoid overflowing the column limit, even code that would in fact fit within the column limit may be line-wrapped at the author's discretion.

> Tip: Extracting a method or local variable may solve the problem without the need to line-wrap.

### 4.5.1 Where to break

The prime directive of line-wrapping is: prefer to break at a **higher syntactic level**. Also:

1. When a line is broken at a non-assignment operator the break comes before the symbol.
    * This also applies to the following "operator-like" symbols:
        * the dot separator (`.`)
        * the two colons of a method reference (`::`)
        * an ampersand in a type bound (`<T extends Foo & Bar>`)
        * a pipe in a catch block (`catch (FooException | BarException e)`).
2. When a line is broken at an assignment operator the break typically comes after the symbol, but either way is acceptable.
    * This also applies to the "assignment-operator-like" colon in an enhanced `for` ("foreach") statement.
3. A method or constructor name stays attached to the open parenthesis (`(`) that follows it.
4. A comma (`,`) stays attached to the token that precedes it.

> Note: The primary goal for line wrapping is to have clear code, not necessarily code that fits in the smallest number of lines.

### 4.5.2 Indent continuation lines at least +4 spaces
When line-wrapping, each line after the first (each continuation line) is indented at least +4 from the original line.

When there are multiple continuation lines, indentation may be varied beyond +4 as desired. In general, two continuation lines use the same indentation level if and only if they begin with syntactically parallel elements.

Section 4.6.3 on Horizontal alignment addresses the discouraged practice of using a variable number of spaces to align certain tokens with previous lines.
## 4.6 Whitespace
### 4.6.1 Vertical Whitespace
A single blank line appears:
1. Between consecutive members or initializers of a class: fields, constructors, methods, nested classes, static initializers, and instance initializers.
2. Between statements, as needed to organize the code into logical subsections.
3. As required by other sections of this document (such as Section 3, Source file structure, and Section 3.3, Import statements).

_Multiple_ consecutive blank lines are not permitted.
### 4.6.2 Horizontal whitespace
Beyond where required by the language or other style rules, and apart from literals, comments and Javadoc, a single ASCII space also appears in the following places only.

1. Separating any reserved word, such as if, for or catch, from an open parenthesis (`(`) that follows it on that line
2. Before any open curly brace (`{`), with two exceptions:
    * `@SomeAnnotation({ a, b })` (no space is used)
    * `String[][] x = {{"foo"}};` (no space is required between `{{`, by item 6 below)
3. On both sides of any binary or ternary operator. This also applies to the following "operator-like" symbols:
    * the ampersand in a conjunctive type bound: `<T extends Foo & Bar>`
    * the pipe for a catch block that handles multiple exceptions: `catch (FooException | BarException e)`
    * the colon (`:`) in an enhanced `for` ("foreach") statement
    * the arrow in a lambda expression: (String str) -> str.length()
    but not
    * the two colons (`::`) of a method reference, which is written like `Object::toString`
    * the dot separator (`.`), which is written like `object.toString()`
4. After `,:;` unless at the end of a line
5. Between the type and variable of a declaration: `List<String> list`
6. Just inside both braces of an array initializer
        `new int[] { 5, 6 }` is valid

This rule is never interpreted as requiring or forbidding additional space at the start of a line; it addresses only interior space. Space at the end of a line should be removed.

### 4.6.3 Horizontal alignment: not permitted
**Terminology Note:** Horizontal alignment is the practice of adding a variable number of additional spaces in your code with the goal of making certain tokens appear directly below certain other tokens on previous lines.

This practice is **not permitted** by OpenSphere Style.

Here is an example of alignment:

    private int   x;      // this is aligned
    private Color color;

> Tip: Alignment can aid readability, but it creates problems for future maintenance. Consider a future change that needs to touch just one line. This change may leave the formerly-pleasing formatting mangled, and that is allowed. More often it prompts the coder (perhaps you) to adjust whitespace on nearby lines as well, possibly triggering a cascading series of reformattings. That one-line change now has a "blast radius." This can at worst result in pointless busywork, but at best it still corrupts version history information, slows down reviewers and exacerbates merge conflicts.

## 4.7 Grouping parentheses: recommended
Optional grouping parentheses are omitted only when author and reviewer agree that there is no reasonable chance the code will be misinterpreted without them, nor would they have made the code easier to read. It is _not_ reasonable to assume that every reader has the entire Java operator precedence table memorized.
## 4.8 Specific constructs

### 4.8.1 Enum classes

Here is an example of enum formatting:

    private enum Answer 
    {
        /** Yes. */
        YES 
        {
            @Override 
            public String toString() 
            {
                return "yes";
            }
        },
        
        /** No. */
        NO,
        
        /** Maybe. */
        MAYBE;
    }

Since enum classes _are classes_, all other rules for formatting classes apply.
### 4.8.2 Variable declarations
#### 4.8.2.1 One variable per declaration
Every variable declaration (field or local) declares only one variable: declarations such as `int a, b;` are not used.
#### 4.8.2.2 Declared when needed
Local variables are **not** habitually declared at the start of their containing block or block-like construct. Instead, local variables are declared close to the point they are first used (within reason), to minimize their scope. Local variable declarations typically have initializers, or are initialized immediately after declaration.
### 4.8.3 Arrays
#### 4.8.3.1 Array initializers
Any array initializer is formatted as follows:
`new int[] { 0, 1, 2, 3 }`
#### 4.8.3.2 No C-style array declarations
The square brackets form a part of the _type_, not the variable: `String[] args`, not `String args[]`.
#### 4.8.4 Switch statements
**Terminology Note:** Inside the braces of a switch block are one or more statement groups. Each statement group consists of one or more switch labels (either `case FOO`: or `default:`), followed by one or more statements.
#### 4.8.4.1 Indentation
As with any other block, the contents of a switch block are indented +4.

After a switch label, there is a line break, and the indentation level is increased +4, exactly as if a block were being opened. The following switch label returns to the previous indentation level, as if a block had been closed.
#### 4.8.4.2 Fall-through: commented
Within a switch block, each statement group either terminates abruptly (with a `break`, `continue`, `return` or thrown exception), or is marked with a comment to indicate that execution will or might continue into the next statement group. Any comment that communicates the idea of fall-through is sufficient (typically `// fall through`). This special comment is not required in the last statement group of the switch block. Example:

    switch (input) 
    {
        case 1:
        case 2:
            prepareOneOrTwo();
            // fall through
        case 3:
            handleOneTwoOrThree();
            break;
        default:
            handleLargeNumber(input);
    }

Notice that no comment is needed after `case 1:`, only at the end of the statement group.
#### 4.8.4.3 The `default` case is present
Each switch statement includes a `default` statement group, even if it contains no code.
#### 4.8.5 Annotations
Annotations applying to a class, method or constructor appear immediately after the documentation block, and each annotation is listed on a line of its own (that is, one annotation per line). These line breaks do not constitute line-wrapping (Section 4.5, Line-wrapping), so the indentation level is not increased. Example:

    @Override
    @Nullable
    public String getNameIfPresent() 
    { 
        ...
    }

### 4.8.6 Comments
This section addresses _implementation comments_. Javadoc is addressed separately in Section 7, Javadoc.

Any line break may be preceded by arbitrary whitespace followed by an implementation comment. Such a comment renders the line non-blank.
#### 4.8.6.1 Block comment style
Block comments are indented at the same level as the surrounding code. They may be in `/* ... */` style or `// ...` style. For multi-line `/* ... */` comments, subsequent lines must start with `*` aligned with the `*` on the previous line.

    /*
     * This is          // And so           /* Or you can
     * okay.            // is this.          * even do this. */
     */

Comments are not enclosed in boxes drawn with asterisks or other characters.

> Tip: When writing multi-line comments, use the `/* ... */` style if you want automatic code formatters to re-wrap the lines when necessary (paragraph-style). Most formatters don't re-wrap lines in `// ...` style comment blocks.

#### 4.8.6.2 Trailing comments
Trailing comments are **never** permitted.
### 4.8.7 Modifiers
Class and member modifiers, when present, appear in the order recommended by the Java Language Specification:
`public protected private abstract default static final transient volatile synchronized native strictfp`
### 4.8.8 Numeric Literals
`long`-valued integer literals use an uppercase `L` suffix, never lowercase (to avoid confusion with the digit `1`). For example, `3000000000L` rather than `3000000000l`.
# 5 Naming
## 5.1 Rules common to all identifiers
Identifiers use only ASCII letters and digits, and, in a small number of cases noted below, underscores. Thus each valid identifier name is matched by the regular expression `\w+`.

In OpenSphere Style, special prefixes may be used, but are discoraged. If used, only the following prefixes are permitted:
<table>
<tr><th>Prefix</th><th>Permitted location</th></tr>
<tr><td><code>my</code></td><td>private non-static Member Variables</td></tr>
<tr><td><code>our</code></td><td>class-level static non-final fields</td></tr>
<tr><td><code>s</code></td><td>class-level static non-final fields</td></tr>
<tr><td><code>p</code></td><td>method parameters</td></tr>
</table>
No other prefixes or suffixes are permitted under any circumstances.

## 5.2 Rules by identifier type
### 5.2.1 Package names
Package names are all lowercase, with consecutive words simply concatenated together (no underscores). For example, `com.example.deepspace`, not `com.example.deepSpace` or `com.example.deep_space`.
### 5.2.2 Class names
Class names are written in [UpperCamelCase] (https://google.github.io/styleguide/javaguide.html#s5.3-camel-case).

Class names are typically nouns or noun phrases. For example, `Character` or `ImmutableList`. Interface names may also be nouns or noun phrases (for example, `List`), but may sometimes be adjectives or adjective phrases instead (for example, `Readable`).

There are no specific rules or even well-established conventions for naming annotation types.

Test classes are named starting with the name of the class they are testing, and ending with `Test`. For example, `HashTest` or `HashIntegrationTest`.
### 5.2.3 Method names
Method names are written in [lowerCamelCase] (https://google.github.io/styleguide/javaguide.html#s5.3-camel-case).

Method names are typically verbs or verb phrases. For example, `sendMessage` or `stop`.

Underscores may appear in `JUnit` _test_ method names to separate logical components of the name. One typical pattern is `test<MethodUnderTest>_<state>`, for example `testPop_emptyStack`. There is no One Correct Way to name test methods.
### 5.2.4 Constant names
Constant names use `CONSTANT_CASE`: all uppercase letters, with words separated by underscores. But what is a constant, exactly?

Constants are static final fields whose contents are deeply immutable and whose methods have no detectable side effects. This includes primitives, Strings, immutable types, and immutable collections of immutable types. If any of the instance's observable state can change, it is not a constant. Merely *intending* to never mutate the object is not enough. Examples:

    // Constants
    static final int NUMBER = 5;
    static final ImmutableList<String> NAMES = ImmutableList.of("Ed", "Ann");
    static final ImmutableMap<String, Integer> AGES = ImmutableMap.of("Ed", 35, "Ann", 32);
    static final Joiner COMMA_JOINER = Joiner.on(','); // because Joiner is immutable
    static final SomeMutableType[] EMPTY_ARRAY = {};
    enum SomeEnum { ENUM_CONSTANT }
    
    // Not constants
    static String nonFinal = "non-final";
    final String nonStatic = "non-static";
    static final Set<String> mutableCollection = new HashSet<String>();
    static final ImmutableSet<SomeMutableType> mutableElements = ImmutableSet.of(mutable);
    static final ImmutableMap<String, SomeMutableType> mutableValues =
        ImmutableMap.of("Ed", mutableInstance, "Ann", mutableInstance2);
    static final Logger logger = Logger.getLogger(MyClass.getName());
    static final String[] nonEmptyArray = {"these", "can", "change"};

These names are typically nouns or noun phrases.
### 5.2.5 Non-constant field names
Non-constant field names (static or otherwise) are written in [lowerCamelCase] (https://google.github.io/styleguide/javaguide.html#s5.3-camel-case).

These names are typically nouns or noun phrases. For example, `computedValues` or `index`.
### 5.2.6 Parameter names
Parameter names are written in [lowerCamelCase] (https://google.github.io/styleguide/javaguide.html#s5.3-camel-case).

One-character parameter names in public methods are not permitted, and ***must*** be avoided.
### 5.2.7 Local variable names
Local variable names are written in [lowerCamelCase] (https://google.github.io/styleguide/javaguide.html#s5.3-camel-case).

Even when final and immutable, local variables are not considered to be constants, and should not be styled as constants.

One-character local variable names are not permitted ***must*** be avoided.
### 5.2.8 Type variable names
Each type variable is named in one of two styles:
* A single capital letter, optionally followed by a single numeral (such as `E`, `T`, `X`, `T2`)
* A name in the form used for classes (see Section 5.2.2, Class names), followed by the capital letter `T` (examples: `RequestT`, `FooBarT`).
### 5.3 Camel case: defined
Sometimes there is more than one reasonable way to convert an English phrase into camel case, such as when acronyms or unusual constructs like "IPv6" or "iOS" are present. To improve predictability, OpenSphere Style specifies the following (nearly) deterministic scheme.

Beginning with the prose form of the name:

1. Convert the phrase to plain ASCII and remove any apostrophes. For example, "Müller's algorithm" might become "Muellers algorithm".
2. Divide this result into words, splitting on spaces and any remaining punctuation (typically hyphens).
    * _Recommended:_ if any word already has a conventional camel-case appearance in common usage, split this into its constituent parts (e.g., "AdWords" becomes "ad words"). Note that a word such as "iOS" is not really in camel case per se; it defies any convention, so this recommendation does not apply.
3. Now lowercase everything (including acronyms), then uppercase only the first character of:
    * ... each word, to yield upper camel case, or
    * ... each word except the first, to yield lower camel case
4. Finally, join all the words into a single identifier.

Note that the casing of the original words is almost entirely disregarded. Examples:
<table>
<tr><th>Prose form</th><th>Correct</th><th>Incorrect</th></tr>
<tr><td>"XML HTTP request"</td><td>XmlHttpRequest</td><td>XMLHTTPRequest</td></tr>
<tr><td>"new customer ID"</td><td>newCustomerId</td><td>newCustomerID</td></tr>
<tr><td>"inner stopwatch"</td><td>innerStopwatch</td><td>innerStopWatch</td></tr>
<tr><td>"supports IPv6 on iOS?"</td><td>supportsIpv6OnIos</td><td>supportsIPv6OnIOS</td></tr>
<tr><td>"YouTube importer"</td><td>YouTubeImporter, YoutubeImporter*</td><td></td></tr>
</table>

*Acceptable, but not recommended.

> Note: Some words are ambiguously hyphenated in the English language: for example "nonempty" and "non-empty" are both correct, so the method names checkNonempty and checkNonEmpty are likewise both correct.

# 6 Programming Practices
## 6.1 `@Override`: always used
A method is marked with the `@Override` annotation whenever it is legal. This includes a class method overriding a superclass method, a class method implementing an interface method, and an interface method respecifying a superinterface method.

**Exception:** `@Override` may be omitted when the parent method is `@Deprecated`.
## 6.2 Caught exceptions: not ignored
Except as noted below, it is very rarely correct to do nothing in response to a caught exception. (Typical responses are to log it, or if it is considered "impossible", rethrow it as an `AssertionError`.)

When it truly is appropriate to take no action whatsoever in a catch block, the reason this is justified is explained in a comment.

    try 
    {
        int i = Integer.parseInt(response);
        return handleNumericResponse(i);
    } 
    catch (NumberFormatException ok) 
    {
        // it's not numeric; that's fine, just continue
    }
    return handleTextResponse(response);

**Exception:** In tests, a caught exception may be ignored without comment if its name is or begins with `expected`. The following is a very common idiom for ensuring that the code under test does throw an exception of the expected type, so a comment is unnecessary here.

    try 
    {
        emptyStack.pop();
        fail();
    } 
    catch (NoSuchElementException expected) 
    {
    }

## 6.3 Static members: qualified using class
When a reference to a static class member must be qualified, it is qualified with that class's name, not with a reference or expression of that class's type.

    Foo aFoo = ...;
    Foo.aStaticMethod(); // good
    aFoo.aStaticMethod(); // bad
    somethingThatYieldsAFoo().aStaticMethod(); // very bad

## 6.4 Finalizers: not used
It is ***extremely rare*** to override `Object.finalize`.

> Tip: Don't do it. If you absolutely must, first read and understand Effective Java Item 7, "Avoid Finalizers," very carefully, and then don't do it.

# 7 Javadoc
## 7.1 Formatting
### 7.1.1 General form
The basic formatting of Javadoc blocks is as seen in this example:

    /**
     * Multiple lines of Javadoc text are written here,
     * wrapped normally...
     */
    public int method(String p1) 
    { 
        ... 
    }

... or in this single-line example:

    /** An especially short bit of Javadoc. */

The basic form is always acceptable. The single-line form may be substituted when there are no at-clauses present, and the entirety of the Javadoc block (including comment markers) can fit on a single line.

### 7.1.2 Paragraphs
One blank line—that is, a line containing only the aligned leading asterisk (`*`)—appears between paragraphs, and before the group of "at-clauses" if present. Each paragraph but the first has &lt;p&gt; immediately before the first word, with no space after.

### 7.1.3 At-clauses
Any of the standard "at-clauses" that are used appear in the order `@param`, `@return`, `@throws`, `@deprecated`, and these four types never appear with an empty description. When an at-clause doesn't fit on a single line, continuation lines are indented four (or more) spaces from the position of the `@`. 

#### 7.1.3.1 Required At-clauses
Every parameter in a method signature must have an associated `@param` tag.

If a method has a return type other than `void`, the `@return` tag must be present.

Every declared thrown exception must have an associated `@throws` tag (even if the declared exception extends `RuntimeException`.
### 7.2 The summary fragment
Each Javadoc block begins with a brief **summary fragment**. This fragment is very important: it is the only part of the text that appears in certain contexts such as class and method indexes.

This is a fragment—a noun phrase or verb phrase, not a complete sentence. It does not begin with `A {@code Foo} is a..`., or `This method returns...`, nor does it form a complete imperative sentence like `Save the record.`. However, the fragment is capitalized and punctuated as if it were a complete sentence.

> Tip: A common mistake is to write simple Javadoc in the form `/** @return the customer ID */`. This is incorrect, and should be changed to `/** Returns the customer ID. */`.

## 7.3 Where Javadoc is used
Javadoc is present for every class (`public`, `private`, or `otherwise`), and every member of such a class (`private`, `protected`, package and `public`), with a few exceptions /notes below.

Additional Javadoc content may also be present, as explained in Section 7.3.4, Non-required Javadoc.
### 7.3.2 Note: overrides
Javadoc is always present on a method that overrides a supertype method, but the content must use the `{@inheritDoc}` before optionally declaring behavior that differs from the parent declaration. For Example:

    /**
     * {@inheritDoc}
     * &lt;p&gt;Does some other stuff.&lt;/p&gt;
     */
    public void foo()
    {
    }


### 7.3.4 Non-required Javadoc
Other classes and members have Javadoc as needed or desired.

Whenever an implementation comment would be used to define the overall purpose or behavior of a class or member, that comment is written as Javadoc instead (using `/**`).

Non-required Javadoc is not strictly required to follow the formatting rules of Sections 7.1.2, 7.1.3, and 7.2, though it is of course recommended.

[^1]: Thanks to https://google.github.io/styleguide/javaguide.html for providing a base for this document.

