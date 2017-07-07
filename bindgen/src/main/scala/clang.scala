package scala.scalanative
package bindings

object clang {
  import scalanative.native._

  type Data              = Ptr[Byte]
  type CXIndex           = Ptr[Byte]
  type CXCursor          = Ptr[Byte]
  type CXType            = Ptr[Byte]
  type CXTranslationUnit = Ptr[Byte]
  type CXUnsavedFile     = Ptr[Byte]
  type Visitor           = CFunctionPtr3[CXCursor, CXCursor, Data, UInt]

  type CXCursorKind            = UInt
  type CXTypeKind              = UInt
  type CXTranslationUnit_Flags = UInt
  type CXChildVisitResult      = UInt

  @extern
  @link("clang")
  object api {
    @name("bindgen_clang_CXCursor_StructDecl")
    def CXCursor_StructDecl(): CXCursorKind = extern

    @name("bindgen_clang_CXCursor_UnionDecl")
    def CXCursor_UnionDecl(): CXCursorKind = extern

    @name("bindgen_clang_CXCursor_EnumDecl")
    def CXCursor_EnumDecl(): CXCursorKind = extern

    @name("bindgen_clang_CXCursor_EnumConstantDecl")
    def CXCursor_EnumConstantDecl(): CXCursorKind = extern

    @name("bindgen_clang_CXCursor_FunctionDecl")
    def CXCursor_FunctionDecl(): CXCursorKind = extern;

    @name("bindgen_clang_CXCursor_VarDecl")
    def CXCursor_VarDecl(): CXCursorKind = extern

    @name("bindgen_clang_CXCursor_TypedefDecl")
    def CXCursor_TypedefDecl(): CXCursorKind = extern

    @name("bindgen_clang_CXTranslationUnit_None")
    def CXTranslationUnit_None(): CXTranslationUnit_Flags = extern

    @name("bindgen_clang_CXTranslationUnit_SkipFunctionBodies")
    def CXTranslationUnit_SkipFunctionBodies(): CXTranslationUnit_Flags =
      extern

    @name("bindgen_clang_CXChildVisit_Break")
    def CXChildVisit_Break(): CXChildVisitResult = extern

    @name("bindgen_clang_CXChildVisit_Continue")
    def CXChildVisit_Continue(): CXChildVisitResult = extern

    @name("bindgen_clang_CXChildVisit_Recurse")
    def CXChildVisit_Recurse(): CXChildVisitResult = extern

    @name("bindgen_clang_CXType_Invalid")
    def CXType_Invalid(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Unexposed")
    def CXType_Unexposed(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Void")
    def CXType_Void(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Bool")
    def CXType_Bool(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Char_U")
    def CXType_Char_U(): CXTypeKind = extern

    @name("bindgen_clang_CXType_UChar")
    def CXType_UChar(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Char16")
    def CXType_Char16(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Char32")
    def CXType_Char32(): CXTypeKind = extern

    @name("bindgen_clang_CXType_UShort")
    def CXType_UShort(): CXTypeKind = extern

    @name("bindgen_clang_CXType_UInt")
    def CXType_UInt(): CXTypeKind = extern

    @name("bindgen_clang_CXType_ULong")
    def CXType_ULong(): CXTypeKind = extern

    @name("bindgen_clang_CXType_ULongLong")
    def CXType_ULongLong(): CXTypeKind = extern

    @name("bindgen_clang_CXType_UInt128")
    def CXType_UInt128(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Char_S")
    def CXType_Char_S(): CXTypeKind = extern

    @name("bindgen_clang_CXType_SChar")
    def CXType_SChar(): CXTypeKind = extern

    @name("bindgen_clang_CXType_WChar")
    def CXType_WChar(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Short")
    def CXType_Short(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Int")
    def CXType_Int(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Long")
    def CXType_Long(): CXTypeKind = extern

    @name("bindgen_clang_CXType_LongLong")
    def CXType_LongLong(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Int128")
    def CXType_Int128(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Float")
    def CXType_Float(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Double")
    def CXType_Double(): CXTypeKind = extern

    @name("bindgen_clang_CXType_LongDouble")
    def CXType_LongDouble(): CXTypeKind = extern

    @name("bindgen_clang_CXType_NullPtr")
    def CXType_NullPtr(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Overload")
    def CXType_Overload(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Dependent")
    def CXType_Dependent(): CXTypeKind = extern

    @name("bindgen_clang_CXType_ObjCId")
    def CXType_ObjCId(): CXTypeKind = extern

    @name("bindgen_clang_CXType_ObjCClass")
    def CXType_ObjCClass(): CXTypeKind = extern

    @name("bindgen_clang_CXType_ObjCSel")
    def CXType_ObjCSel(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Complex")
    def CXType_Complex(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Pointer")
    def CXType_Pointer(): CXTypeKind = extern

    @name("bindgen_clang_CXType_BlockPointer")
    def CXType_BlockPointer(): CXTypeKind = extern

    @name("bindgen_clang_CXType_LValueReference")
    def CXType_LValueReference(): CXTypeKind = extern

    @name("bindgen_clang_CXType_RValueReference")
    def CXType_RValueReference(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Record")
    def CXType_Record(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Enum")
    def CXType_Enum(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Typedef")
    def CXType_Typedef(): CXTypeKind = extern

    @name("bindgen_clang_CXType_ObjCInterface")
    def CXType_ObjCInterface(): CXTypeKind = extern

    @name("bindgen_clang_CXType_ObjCObjectPointer")
    def CXType_ObjCObjectPointer(): CXTypeKind = extern

    @name("bindgen_clang_CXType_FunctionNoProto")
    def CXType_FunctionNoProto(): CXTypeKind = extern

    @name("bindgen_clang_CXType_FunctionProto")
    def CXType_FunctionProto(): CXTypeKind = extern

    @name("bindgen_clang_CXType_ConstantArray")
    def CXType_ConstantArray(): CXTypeKind = extern

    @name("bindgen_clang_CXType_Vector")
    def CXType_Vector(): CXTypeKind = extern

    @name("bindgen_clang_CXType_IncompleteArray")
    def CXType_IncompleteArray(): CXTypeKind = extern

    @name("bindgen_clang_CXType_VariableArray")
    def CXType_VariableArray(): CXTypeKind = extern

    @name("bindgen_clang_CXType_DependentSizedArray")
    def CXType_DependentSizedArray(): CXTypeKind = extern

    @name("bindgen_clang_CXType_MemberPointer")
    def CXType_MemberPointer(): CXTypeKind = extern;

    @name("bindgen_clang_getCursorKind")
    def getCursorKind(cursor: CXCursor): CXCursorKind = extern

    @name("bindgen_clang_Cursor_getNumArguments")
    def Cursor_getNumArguments(cursor: CXCursor): CInt = extern

    @name("bindgen_clang_getEnumConstantDeclValue")
    def getEnumConstantDeclValue(cursor: CXCursor): CLongLong = extern

    @name("bindgen_clang_getCursorType")
    def getCursorType(cursor: CXCursor): CXType = extern

    @name("bindgen_clang_getResultType")
    def getResultType(tpe: CXType): CXType = extern

    @name("bindgen_clang_getEnumDeclIntegerType")
    def getEnumDeclIntegerType(cursor: CXCursor): CXType = extern

    @name("bindgen_clang_getTypedefDeclUnderlyingType")
    def getTypedefDeclUnderlyingType(cursor: CXCursor): CXType = extern

    @name("bindgen_clang_getTranslationUnitCursor")
    def getTranslationUnitCursor(unit: CXTranslationUnit): CXCursor = extern

    @name("bindgen_clang_getCursorKindSpelling")
    def getCursorKindSpelling(kind: CXCursorKind): CString = extern

    @name("bindgen_clang_getCursorSpelling")
    def getCursorSpelling(cursor: CXCursor): CString = extern

    @name("bindgen_clang_getTypeSpelling")
    def getTypeSpelling(tpe: CXType): CString = extern;

    @name("bindgen_clang_Cursor_getArgument")
    def Cursor_getArgument(cursor: CXCursor, i: CInt): CXCursor = extern

    @name("bindgen_clang_visitChildren")
    def visitChildren(parent: CXCursor, visitor: Visitor, data: Data): UInt =
      extern

    @name("clang_createIndex")
    def createIndex(excludeDeclarationsFromPCH: CInt,
                    displayDiagnostics: CInt): CXIndex = extern

    @name("clang_disposeIndex")
    def disposeIndex(index: CXIndex): Unit = extern

    @name("clang_parseTranslationUnit")
    def parseTranslationUnit(index: CXIndex,
                             fileName: CString,
                             argv: Ptr[CString],
                             argc: CInt,
                             unsavedFiles: CXUnsavedFile,
                             numUnsavedFiles: CInt,
                             options: UInt): CXTranslationUnit = extern

    @name("clang_disposeTranslationUnit")
    def disposeTranslationUnit(unit: CXTranslationUnit): Unit = extern
  }
}
