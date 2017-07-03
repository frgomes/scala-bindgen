package scala.scalanative
package bindings


object clang {

  import scalanative.native._

  type Data = Ptr[Byte]
  type CXIndex = Ptr[Byte]
  type CXCursor = Ptr[Byte]
  type CXType = Ptr[Byte]
  type CXTranslationUnit = Ptr[Byte]
  type CXUnsavedFile = Ptr[Byte]
  type Visitor = CFunctionPtr3[CXCursor, CXCursor, Data, UInt]

  type CXTranslationUnit_Flags = UInt
  val CXTranslationUnit_SkipFunctionBodies: CXTranslationUnit_Flags = 0x40.toUInt

  type CXCursorKind = UInt
  val CXCursor_UnionDecl       : CXCursorKind =  3.toUInt
  val CXCursor_EnumDecl        : CXCursorKind =  5.toUInt
  val CXCursor_EnumConstantDecl: CXCursorKind =  7.toUInt
  val CXCursor_FunctionDecl    : CXCursorKind =  8.toUInt
  val CXCursor_VarDecl         : CXCursorKind =  9.toUInt
  val CXCursor_TypedefDecl     : CXCursorKind = 20.toUInt

  type CXChildVisitResult = UInt
  val CXChildVisit_Break   : CXChildVisitResult = 0.toUInt
  val CXChildVisit_Continue: CXChildVisitResult = 1.toUInt
  val CXChildVisit_Recurse : CXChildVisitResult = 2.toUInt

  //TODO type CXTypeKind = UInt
  //TODO val CXType_Invalid            : CXTypeKind =
  //TODO val CXType_Unexposed          : CXTypeKind =
  //TODO val CXType_Void               : CXTypeKind =
  //TODO val CXType_Bool               : CXTypeKind =
  //TODO val CXType_Char_U             : CXTypeKind =
  //TODO val CXType_UChar              : CXTypeKind =
  //TODO val CXType_Char16             : CXTypeKind =
  //TODO val CXType_Char32             : CXTypeKind =
  //TODO val CXType_UShort             : CXTypeKind =
  //TODO val CXType_UInt               : CXTypeKind =
  //TODO val CXType_ULong              : CXTypeKind =
  //TODO val CXType_ULongLong          : CXTypeKind =
  //TODO val CXType_UInt128            : CXTypeKind =
  //TODO val CXType_Char_S             : CXTypeKind =
  //TODO val CXType_SChar              : CXTypeKind =
  //TODO val CXType_WChar              : CXTypeKind =
  //TODO val CXType_Short              : CXTypeKind =
  //TODO val CXType_Int                : CXTypeKind =
  //TODO val CXType_Long               : CXTypeKind =
  //TODO val CXType_LongLong           : CXTypeKind =
  //TODO val CXType_Int128             : CXTypeKind =
  //TODO val CXType_Float              : CXTypeKind =
  //TODO val CXType_Double             : CXTypeKind =
  //TODO val CXType_LongDouble         : CXTypeKind =
  //TODO val CXType_NullPtr            : CXTypeKind =
  //TODO val CXType_Overload           : CXTypeKind =
  //TODO val CXType_Dependent          : CXTypeKind =
  //TODO val CXType_ObjCId             : CXTypeKind =
  //TODO val CXType_ObjCClass          : CXTypeKind =
  //TODO val CXType_ObjCSel            : CXTypeKind =
  //TODO val CXType_Complex            : CXTypeKind =
  //TODO val CXType_Pointer            : CXTypeKind =
  //TODO val CXType_BlockPointer       : CXTypeKind =
  //TODO val CXType_LValueReference    : CXTypeKind =
  //TODO val CXType_RValueReference    : CXTypeKind =
  //TODO val CXType_Record             : CXTypeKind =
  //TODO val CXType_Enum               : CXTypeKind =
  //TODO val CXType_Typedef            : CXTypeKind =
  //TODO val CXType_ObjCInterface      : CXTypeKind =
  //TODO val CXType_ObjCObjectPointer  : CXTypeKind =
  //TODO val CXType_FunctionNoProto    : CXTypeKind =
  //TODO val CXType_FunctionProto      : CXTypeKind =
  //TODO val CXType_ConstantArray      : CXTypeKind =
  //TODO val CXType_Vector             : CXTypeKind =
  //TODO val CXType_IncompleteArray    : CXTypeKind =
  //TODO val CXType_VariableArray      : CXTypeKind =
  //TODO val CXType_DependentSizedArray: CXTypeKind =
  //TODO val CXType_MemberPointer      : CXTypeKind =


  @extern
  @link("clang")
  object api {

    @name("clang_Cursor_getNumArguments")               def Cursor_getNumArguments(`*cursor`: CXCursor): CInt = extern
    @name("clang_Cursor_getArgument")                   def Cursor_getArgument(cursor: CXCursor, i: CInt): CXCursor = extern
   
    @name("clang_getCursorKind")                        def getCursorKind(`*cursor`: CXCursor): CXCursorKind = extern
    @name("clang_getEnumConstantDeclValue")             def getEnumConstantDeclValue(`*cursor`: CXCursor): CLongLong = extern
    @name("clang_getCursorType")                        def getCursorType(`*cursor`: CXCursor): CXType = extern
    @name("clang_getResultType")                        def getResultType(`*tpe`: CXType): CXType = extern
    @name("clang_getEnumDeclIntegerType")               def getEnumDeclIntegerType(`*cursor`: CXCursor): CXType = extern
    @name("clang_getTypedefDeclUnderlyingType")         def getTypedefDeclUnderlyingType(`*cursor`: CXCursor): CXType = extern
    @name("clang_getTranslationUnitCursor")             def getTranslationUnitCursor(`unit`: CXTranslationUnit): CXCursor = extern
    @name("clang_getCursorKindSpelling")                def getCursorKindSpelling(`kind`: CXCursorKind): CString = extern
    @name("clang_getCursorSpelling")                    def getCursorSpelling(`*cursor`: CXCursor): CString = extern
    @name("clang_getTypeSpelling")                      def getTypeSpelling(`*tpe`: CXType): CString = extern;
   
    @name("clang_visitChildren")
    def visitChildren(parent: CXCursor, visitor: Visitor, data: Data): UInt = extern
   
    @name("clang_createIndex")
    def createIndex(excludeDeclarationsFromPCH: CInt, displayDiagnostics: CInt): CXIndex = extern
   
    @name("clang_disposeIndex")
    def disposeIndex(index: CXIndex): Unit = extern
   
    @name("clang_parseTranslationUnit")
    def parseTranslationUnit(
      index: CXIndex,
      fileName: CString,
      argv: Ptr[CString], argc: CInt,
      unsavedFiles: CXUnsavedFile, numUnsavedFiles: CInt,
      options: CXTranslationUnit_Flags): CXTranslationUnit = extern
   
    @name("clang_disposeTranslationUnit")
    def disposeTranslationUnit(unit: CXTranslationUnit): Unit = extern
  }
}
