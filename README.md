javaComparator
==============
What is it?

Structural Comparison is to list down structural differences of 2 java files. 
             Generally comparison should be on raw files, i.e., java files should not be compiled. Using JDT plugin APIs, construct AST trees for java file and start comparing these AST trees against each other. AST tree are mainly compared for differences by 3 structural property types viz., package, imports and types. Difference object should be constructed based on the comparison.
