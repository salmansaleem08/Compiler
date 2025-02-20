My Custom Programming Language - User Manual
Overview
This document provides a comprehensive guide to using the custom programming language. It includes syntax rules, keywords, operators, and special conventions necessary to write and execute programs in this language.
Syntax Rules
1.	No Uppercase Letters - All keywords and identifiers must be in lowercase.
2.	Semicolon as a Terminator - Every statement must end with a semicolon (;).
3.	Single-line Comments - Use // for single-line comments.
4.	Multi-line Comments - Enclosed within $$ symbols:
5.	$$
6.	This is a multi-line comment.
$$
Data Types and Variables
This language supports multiple data types:
Data Type	Keyword	Example
Boolean	bitz	bitz _global = true;
Integer	numz	numz _count = 10;
Float	floatz	floatz _decimal = 3.14159;
Character	charz	charz _letter = 'a';
Variable Declaration
Variables must be declared using their respective type keyword, followed by an identifier and an assigned value.
numz value = 5;
floatz result = 2.5;
bitz is_active = true;
charz letter = 'b';
Operators
This language supports the following arithmetic operations:
Operator	Operation	Example
+	Addition	result = value + valuee;
-	Subtraction	result = value - valuee;
*	Multiplication	result = value * valuee;
/	Division	result = value / valuee;
%	Modulus	result = value % valuee;
^	Exponentiation	result = value ^ valuee;
Example:
numz a = 5;
numz b = 2;
numz result;
result = a + b;
Printing Output
To print output, use print<< followed by the text or variable:
print<<HelloHi!;
Boolean Operations
Boolean values (true and false) are assigned using the bitz type. Example:
bitz flag = true;
flag = false;
Additional Notes
•	Identifiers must be written in lowercase.
•	No uppercase letters are allowed anywhere in the code.
•	Semicolon (``) is required at the end of every statement.
•	Ensure proper usage of variables before operations.
Example Program
$$
   Example Program
$$
numz a = 5;
numz b = 3;
numz result;
result = a ^ b; // Exponentiation
print<<result;
