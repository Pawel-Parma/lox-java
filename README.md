# Jlox

Lox interpreter written in Java following the book [Crafting Interpreters](http://www.craftinginterpreters.com/).

## Branches:
- ### [*main*](https://github.com/Pawel-Parma/lox-java)

If you are in the main branch you are looking at the implementation derived from the book.  
It has some solved challenges and my own modifications.

<details>
 <summary>Changes:</summary>
  <ul>
   <li>
    const - declaration for immutable data. Examples:
    <br>

    // Values
    const x = 1;
    x = 2; // Error at 'x': Cannot reassign a constant.
    
    // Classes
    class A {
        init(b) { this.b = 1; }
    }

    const a = A(1);
    a.b = 2; // Error at 'b': Cannot modify a field of a constant object.
    
    // But this is allowed:
    var a_mut = a;
    a.b = 2; // No error

   </li>
  </ul>

  <ul>
   <li>
    break - statement for breaking out of loops. Example:
    
    for (var i = 0; i < 10; i = i + 1) {
        if (i == 5) break;
        print i;
    }

    Output:
    // 0
    // 1
    // 2
    // 3
    // 4

   </li>
  </ul>

  <ul>
   <li>
    continue - statement for skipping the rest of the loop body. Example: 
    
    for (var i = 0; i < 10; i = i + 1) {
        if (i % 2 == 0) continue;
        print i;
    }

    Output:
    // 1
    // 3
    // 5
    // 7
    // 9

   </li>
  </ul>

  <ul>
   <li>
    Escape sequences in strings - [ \n, \r, \t, \b, \', \", \\ ]. Note " ' " still works, no need for " \' ".
    Examples:    
    
    // New line
    print "Hello\nWorld"; // Hello
                          // World
                          // 
    
    // Tab
    print "Hello\tWorld"; // World  World

    // Backspace
    print "Hello\b World"; // Hell World

   </li>
  </ul>

  <ul>
   <li>
    String and number concatenation using the '+' operator.

    // string + number
    print "123" + 4; // 1234 (string)

    // number + string
    print 4 + "123"; // 4123 (string)

   </li>
  </ul>

  <ul>
   <li>
    def keyword before method declaration is now required.

    // Before
    class A {
        init() { print "A"; }
    }

    // Now
    class A {
        def init() { print "A"; }
    }

   </li>
  </ul>

  <ul>
   <li>
    lambda expressions - anonymous functions. Examples:

    // Single line
    const add = lambda(a, b) { return a + b; };
    print add(1, 2); // 3

    // Multi line
    const add = lambda(a, b) {
        var c = a + b;
        return c;
    };
    print add(3, 2); // 3

   </li>
  </ul>

  <ul>
   <li>
    imports - importing other files. Examples:

    // file1.lox
    import "file2";  // Hello from file2
    import "file3" as math;    

    fun foo() {
        print "Hello from foo of file1";
    }
    
    foo();  // Hello from foo of file1
    file2.foo();  // Hello from foo of file2

    print math.pow(2, -math.PI);  // 0.0625

    // file2.lox
    fun foo() {
        print "Hello from foo of file2";
    }

    print "Hello from file2";

    // file3.lox
    fun pow(val, exp) {
        if (exp < 0) {
            return 1 / pow(val, -exp);
        }
        var result = 1;
        for (var i = 0; i < exp; i = i + 1) {
            result = result * val;
        }
        return result;
    }

    const PI = 3.14;

   </li>
  </ul>

  <ul>
   <li>
    modulo operator. Examples:

    print 4 % 2; // 0
    print 5 % 2; // 1

   </li>
  </ul>
</details>

- ### [*jlox*](https://github.com/Pawel-Parma/lox-java/tree/jlox)

If you are in the jlox branch you are looking at the implementation following the book.
