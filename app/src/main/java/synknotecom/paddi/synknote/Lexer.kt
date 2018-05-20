package synknotecom.paddi.synknote

/**
* Created by PaddiM8 on 2018-03-13.
*/

fun lexMarkdown(input: String): List<List<Int>> {
    val findings: MutableList<MutableList<Int>> = mutableListOf(
            mutableListOf(), // **
            mutableListOf(), // __
            mutableListOf(), // *
            mutableListOf() // _
    )

    var i = 0
    while (i < input.length) {
        val char = input[i]
        if (char.isModifier()) {
            var charAfter = ' '
            if (i < input.length - 1)
                charAfter = input[i + 1]

            if (char == charAfter) { // Two or more
                val modifierIndex = modifierToIndex(char.toString() + char.toString())
                findings[modifierIndex].add(i)
                i++
            } else { // Just one
                val modifierIndex = modifierToIndex(char.toString())
                findings[modifierIndex].add(i)
            }
        }

        i++
    }

    return findings
}

//fun Char.isModifier(): Boolean {
    //return arrayOf('*', '_').any { this == it }
//}

fun Char.isModifier(): Boolean =
    this == '*' || this == '_'

fun modifierToIndex(modifier: String): Int {
    return when (modifier) {
        "**"  -> 0
        "__"  -> 1
        "*"   -> 2
        "_"   -> 3
        else  -> 0
    }
}