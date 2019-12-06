package astminer.parse.ktpsi


internal object TypeLabels {
    internal const val IDENTIFIER = "IDENTIFIER"
    internal const val METHOD_DECLARATION = "FUN"
    //    TODO("consider other types")
    internal const val METHOD_TYPE = "TYPE_REFERENCE"
    internal const val CLASS_DECLARATION = "CLASS"
    internal const val PARAMETERS_LIST = "VALUE_PARAMETER_LIST"
    internal const val PARAMETER = "VALUE_PARAMETER"
    internal const val PARAMETER_TYPE = "TYPE_REFERENCE"
    internal const val WHITE_SPACE = "WHITE_SPACE"
    internal const val K_DOC = "KDoc"
    internal const val EMPTY_LIST = "<empty list>"
    internal const val PSI_ERROR_ELEMENT = "PsiErrorElement"

    internal const val K_DOC_PREF = "KDOC"

    internal val TO_KEEP = setOf("IDENTIFIER", "INTEGER_LITERAL", "data", "private", "public", "protected", "internal",
            "class", "object", "interface", "companion", "null", "true", "false", "this", "lateinit", "super", "ANDAND",
            "REGULAR_STRING_PART", "override", "PLUS", "MINUS", "MUL", "DIV", "ELVIS", "EQEQ", "SAFE_ACCESS", "is",
            "CHARACTER_LITERAL", "FLOAT_CONSTANT", "infix", "inline", "field", "operator", "vararg", "inner", "tailrec",
            "set", "get", "out", "enum", "PLUSPLUS", "MINUSMINUS", "PLUSEQ", "MINUSEQ", "NOT_IS", "NOT_IN", "dynamic",
            "external", "final", "sealed", "in", "noinline", "abstract", "const", "crossinline", "dynamic", "reified",
            "open", "typealias", "RANGE", "OROR", "as", "AS_SAFE", "PERC", "MULTEQ", "DIVEQ", "EQEQEQ", "PERCEQ",
            "where", "actual", "expect", "suspend", "param", "receiver")
    internal val TO_REMOVE = setOf("LT", "AT", "GT", "GTEQ", "LTEQ", "EQ", "COMMA", "LBRACE", "RBRACE", "LPAR", "RPAR",
            "for", "if", "while", "by", "import", "try", "catch", "finally", "throw", "init", "ARROW", "return", "when", "DOT",
            "LBRACKET", "RBRACKET", "COLONCOLON", "fun", "else", "OPEN_QUOTE", "CLOSING_QUOTE", "COLON", "break",
            "ESCAPE_SEQUENCE", "QUEST", "package", "val", "var", "constructor", "EXCL", "EXCLEQ", "EXCLEQEQEQ",
            "EXCLEXCL", "DO", "continue", "do", "file", "annotation", "semicolon", "SHORT_TEMPLATE_ENTRY_START",
            "SHORT_TEMPLATE_ENTRY_END", "LONG_TEMPLATE_ENTRY_START", "LONG_TEMPLATE_ENTRY_END", "SEMICOLON")
    internal val LEAF_TYPES = setOf("IDENTIFIER", "CHARACTER_LITERAL", "FLOAT_CONSTANT", "REGULAR_STRING_PART",
            "INTEGER_LITERAL")
//    TODO("reconsider operators, template parts, val/var, object/interafce, modifiers")
}
