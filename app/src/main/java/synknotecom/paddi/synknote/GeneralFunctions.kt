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

fun stripFileExtension(fileName: String) : String {
    val endPosition = fileName.lastIndexOf(".")

    if (endPosition < 0) // If there are no dots in the name
        return fileName
    return fileName.substring(0, endPosition)
}

fun getFileExtension(fileName: String) : String {
    val startPosition = fileName.lastIndexOf(".")
    val endPosition = fileName.length - 1

    if (startPosition < 0)
        return fileName
    return fileName.substring(startPosition, endPosition)
}