from lark import Lark
import sys


# TODO add in "<sep>"
parser = Lark('''
    start : r
    ?r : c?
       | start_with
       | end_with
       | contain
       | concat
       | notcc
       | not
       | or
       | and
       | optional
       | star
       | repeat
       | repeat_at_least
       | repeat_range
    ?c : number_class
       | non_zero_number_class
       | letter_class
       | lowercase_class
       | uppercase_class
       | any_class
       | alphanumerical_class
       | number
       | letter
       | special_char
    k                     : INT
    number                : "<" DIGIT ">"
    letter                : "<" LETTER ">"
    number_class          : "<num>"
    non_zero_number_class : "<num1-9>"
    letter_class          : "<let>"
    lowercase_class       : "<low>"
    uppercase_class       : "<cap>"
    any_class             : "<any>"
    alphanumerical_class  : "<alphanum>"
    special_char          : "<" SCHAR ">" 
    SCHAR                 : "-" | "=" | "!" | "@" | "#" | "$" | "." | "%" | "^" | "&" | "*" | "(" | "\\"" | ")" | "_" | "+" | "\\\\" | "\'" | "<" | ">"
    start_with      : "startwith(" r ")"
    end_with        : "endwith(" r ")"
    contain         : "contain(" r ")"
    notcc           : "notcc(" r ")"
    not             : "not(" r ")"
    concat          : "concat(" r "," r ")"
    or              : "or(" r "," r ")"
    and             : "and(" r "," r ")"
    optional        : "optional(" r ")"
    star            : "star(" r ")" 
    repeat          : "repeat(" r "," k ")"
    repeat_at_least : "repeatatleast(" r "," k ")"
    repeat_range    : "repeatrange(" r "," k "," k ")"
    %import common.INT
    %import common.DIGIT
    %import common.LETTER
''')


def traverse(tree, ops):
    operation = tree.data

    if operation == "k":
        raise ValueError("k should never be an operation - something is wrong with the code - an earlier function should have just grabbed the value, not called `tree_to_regex` on a 'k' operation.")

    if operation == "number" or operation == "letter":
        ops.add("<" + str(tree.children[0]) + ">")
        return 

    if operation == "number_class":
        ops.add("<num>")
        return

    if operation == "non_zero_number_class":
        ops.add("<num1-9>")
        return

    if operation == "letter_class":
        ops.add("<let>")
        return

    if operation == "lowercase_class":
        ops.add("<low>")
        return

    if operation == "uppercase_class":
        ops.add("<cap>")
        return

    if operation == "any_class":
        ops.add("<any>")
        return

    if operation == "alphanumerical_class":
        ops.add("<alphanum>")
        return

    if operation == "special_char":
        op = str(tree.children[0])
        ops.add("<" + op + ">")
        return

    if operation == "start":
        # keep traversing
        traverse(tree.children[0], ops)
        return

    if operation == "notcc":
        ops.add("notcc")
        traverse(tree.children[0], ops)
        return

    if operation == "not":
        ops.add("not")
        traverse(tree.children[0], ops)
        return

    if operation == "start_with":
        ops.add("startwith")
        regex = traverse(tree.children[0], ops)
        return

    if operation == "end_with":
        ops.add("endwith")
        traverse(tree.children[0], ops)
        return

    if operation == "contain":
        ops.add("contain")
        traverse(tree.children[0], ops)
        return

    if operation == "concat":
        ops.add("concat")
        child1, child2 = tree.children
        traverse(child1, ops)
        traverse(child2, ops)
        return

    if operation == "or":
        ops.add("or")
        child1, child2 = tree.children
        traverse(child1, ops) 
        traverse(child2, ops)
        return

    if operation == "and":
        ops.add("and")
        child1, child2 = tree.children
        traverse(child1, ops)
        traverse(child2, ops)
        return

    if operation == "optional":
        ops.add("optional")
        traverse(tree.children[0], ops)
        return

    if operation == "star":
        ops.add("star")
        traverse(tree.children[0], ops)
        return

    if operation == "repeat":
        ops.add("repeat")
        traverse(tree.children[0], ops)
        return

    if operation == "repeat_at_least":
        ops.add("repeatatleast")
        traverse(tree.children[0], ops)
        return

    if operation == "repeat_range":
        ops.add("repeatatleast")
        traverse(tree.children[0], ops)
        return

    raise ValueError(f"Didn't know what to do for operation '{operation}'")

def extract_ops(dsl: str) -> str:
    '''Takes a string in the DSL and converts it to regex'''
    tree = parser.parse(dsl)
    ops = set()
    traverse(tree, ops)
    return ops


if __name__ == "__main__":
    # INPUT_STRING = "or(contain(<2>),<let>)"
    # INPUT_STRING = "concat(repeatatleast(<let>,1),<C>)"
    # INPUT_STRING = "endwith(endwith(<num1-9>))"

    print(extract_ops(sys.argv[1]))