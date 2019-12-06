package astminer.parse.ktpsi


internal object TypeLabels {
    internal const val METHOD_DECLARATION = "FUN"
    //    TODO("consider other types")
    internal const val METHOD_IDENTIFIER = "IDENTIFIER"
    internal const val METHOD_TYPE = "TYPE_REFERENCE"
    internal const val CLASS_DECLARATION = "CLASS"
    internal const val CLASS_IDENTIFIER = "IDENTIFIER"
    internal const val PARAMETERS_LIST = "VALUE_PARAMETER_LIST"
    internal const val PARAMETER = "VALUE_PARAMETER"
    internal const val PARAMETER_IDENTIFIER = "IDENTIFIER"
    internal const val PARAMETER_TYPE = "TYPE_REFERENCE"
    internal const val WHITE_SPACE = "WHITE_SPACE"
    internal const val K_DOC = "KDoc"
    internal const val EMPTY_LIST = "<empty list>"
    internal const val PSI_ERROR_ELEMENT = "PsiErrorElement"
}
