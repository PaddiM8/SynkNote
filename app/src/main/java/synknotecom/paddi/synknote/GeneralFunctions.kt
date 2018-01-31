package synknotecom.paddi.synknote

/**
 * Created by paddi on 1/30/18.
 */

fun getFileExtensionFromType(type: String) : String {
    return if (type == "Markdown Document")
        ".md"
    else
        ".txt"
}