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
    const - declaration for immutable data 
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
    
    // But this is allowed
    var a_mut = a;
    a.b = 2; // No error

   </li>
  </ul>

  <ul>
   <li>
    break - statement for breaking out of loops
   </li>
  </ul>

  <ul>
   <li>
    continue - statement for skipping the rest of the loop body
   </li>
  </ul>
</details>

- ### [*jlox*](https://github.com/Pawel-Parma/lox-java/tree/jlox)

If you are in the jlox branch you are looking at the implementation following the book.
