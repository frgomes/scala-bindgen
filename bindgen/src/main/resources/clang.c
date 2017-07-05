/**
 * Macro-based libclang wrapper.
 *
 * Required until Scala Native support passing structs as parameters:
 * https://github.com/scala-native/scala-native/issues/195
 */

#include <clang-c/Index.h>
#include <stdlib.h>
#include <string.h>

// An incomplete list of enums. Not all of these are currently used.
// Each enum value is exposed to Scala using a C method.
#define ENUM_INFO(_) \
  _(CXCursorKind, CXCursor_StructDecl) \
  _(CXCursorKind, CXCursor_UnionDecl) \
  _(CXCursorKind, CXCursor_EnumDecl) \
  _(CXCursorKind, CXCursor_EnumConstantDecl) \
  _(CXCursorKind, CXCursor_FunctionDecl) \
  _(CXCursorKind, CXCursor_VarDecl) \
  _(CXCursorKind, CXCursor_TypedefDecl) \
  _(CXTranslationUnit_Flags, CXTranslationUnit_None) \
  _(CXTranslationUnit_Flags, CXTranslationUnit_SkipFunctionBodies) \
  _(CXChildVisitResult, CXChildVisit_Break) \
  _(CXChildVisitResult, CXChildVisit_Continue) \
  _(CXChildVisitResult, CXChildVisit_Recurse) \
  _(CXTypeKind, CXType_Invalid) \
  _(CXTypeKind, CXType_Unexposed) \
  _(CXTypeKind, CXType_Void) \
  _(CXTypeKind, CXType_Bool) \
  _(CXTypeKind, CXType_Char_U) \
  _(CXTypeKind, CXType_UChar) \
  _(CXTypeKind, CXType_Char16) \
  _(CXTypeKind, CXType_Char32) \
  _(CXTypeKind, CXType_UShort) \
  _(CXTypeKind, CXType_UInt) \
  _(CXTypeKind, CXType_ULong) \
  _(CXTypeKind, CXType_ULongLong) \
  _(CXTypeKind, CXType_UInt128) \
  _(CXTypeKind, CXType_Char_S) \
  _(CXTypeKind, CXType_SChar) \
  _(CXTypeKind, CXType_WChar) \
  _(CXTypeKind, CXType_Short) \
  _(CXTypeKind, CXType_Int) \
  _(CXTypeKind, CXType_Long) \
  _(CXTypeKind, CXType_LongLong) \
  _(CXTypeKind, CXType_Int128) \
  _(CXTypeKind, CXType_Float) \
  _(CXTypeKind, CXType_Double) \
  _(CXTypeKind, CXType_LongDouble) \
  _(CXTypeKind, CXType_NullPtr) \
  _(CXTypeKind, CXType_Overload) \
  _(CXTypeKind, CXType_Dependent) \
  _(CXTypeKind, CXType_ObjCId) \
  _(CXTypeKind, CXType_ObjCClass) \
  _(CXTypeKind, CXType_ObjCSel) \
  _(CXTypeKind, CXType_Complex) \
  _(CXTypeKind, CXType_Pointer) \
  _(CXTypeKind, CXType_BlockPointer) \
  _(CXTypeKind, CXType_LValueReference) \
  _(CXTypeKind, CXType_RValueReference) \
  _(CXTypeKind, CXType_Record) \
  _(CXTypeKind, CXType_Enum) \
  _(CXTypeKind, CXType_Typedef) \
  _(CXTypeKind, CXType_ObjCInterface) \
  _(CXTypeKind, CXType_ObjCObjectPointer) \
  _(CXTypeKind, CXType_FunctionNoProto) \
  _(CXTypeKind, CXType_FunctionProto) \
  _(CXTypeKind, CXType_ConstantArray) \
  _(CXTypeKind, CXType_Vector) \
  _(CXTypeKind, CXType_IncompleteArray) \
  _(CXTypeKind, CXType_VariableArray) \
  _(CXTypeKind, CXType_DependentSizedArray) \
  _(CXTypeKind, CXType_MemberPointer)

// Wrappers for various getters.
// PRIMITIVE_ wraps methods returning primitive values
// COPY_ wraps methods returning structs by value.
// STRING_ wraps methods returning CXStrings.
#define GETTER_INFO(PRIMITIVE_, COPY_, STRING_) \
  PRIMITIVE_(CXCursorKind, getCursorKind, CXCursor, *cursor) \
  PRIMITIVE_(CInt, Cursor_getNumArguments, CXCursor, *cursor) \
  PRIMITIVE_(CLongLong, getEnumConstantDeclValue, CXCursor, *cursor) \
  COPY_(CXType, getCursorType, CXCursor, *cursor) \
  COPY_(CXType, getResultType, CXType, *tpe) \
  COPY_(CXType, getEnumDeclIntegerType, CXCursor, *cursor) \
  COPY_(CXType, getTypedefDeclUnderlyingType, CXCursor, *cursor) \
  COPY_(CXCursor, getTranslationUnitCursor, CXTranslationUnit, unit) \
  STRING_(getCursorKindSpelling, CXCursorKind, kind) \
  STRING_(getCursorSpelling, CXCursor, *cursor) \
  STRING_(getTypeSpelling, CXType, *tpe)

typedef enum CXCursorKind CXCursorKind;
typedef long long CLongLong;
typedef int CInt;

#define DEFINE_ENUM(tpe, id) enum tpe bindgen_clang_##id() { return id; };
ENUM_INFO(DEFINE_ENUM)

#define PRIMITIVE_GETTER(tpe, name, argType, arg) \
  tpe \
  bindgen_clang_##name(argType arg) \
  { \
    return clang_##name(arg); \
  }

#define COPY_GETTER(tpe, name, argType, arg) \
  tpe * \
  bindgen_clang_##name(argType arg) \
  { \
    tpe *copy = malloc(sizeof(tpe)); \
    *copy = clang_##name(arg); \
    return copy; \
  }

#define STRING_GETTER(name, argType, arg) \
  const char * \
  bindgen_clang_##name(argType arg) \
  { \
    CXString cxstring = clang_##name(arg); \
    const char *string = strdup(clang_getCString(cxstring)); \
    clang_disposeString(cxstring); \
    return string; \
  }

GETTER_INFO(PRIMITIVE_GETTER, COPY_GETTER, STRING_GETTER)

CXCursor *
bindgen_clang_Cursor_getArgument(CXCursor *cursor, int i)
{
  CXCursor *copy = malloc(sizeof(CXCursor));
  *copy = clang_Cursor_getArgument(*cursor, i);
  return copy;
}

/*
 * Wrappers for the visitor API
 */

typedef enum CXChildVisitResult (*bindgen_visitor)(CXCursor *cursor, CXCursor *parent, void *data);

struct bindgen_context {
  bindgen_visitor visitor;
  void *data;
};

static enum CXChildVisitResult
bindgen_clang_visit(CXCursor cursor, CXCursor parent, CXClientData data)
{
  struct bindgen_context *ctx = data;

  CXSourceLocation location = clang_getCursorLocation(cursor);
  if (!clang_Location_isFromMainFile(location))
    return CXChildVisit_Continue;

  return ctx->visitor(&cursor, &parent, ctx->data);
}

unsigned
bindgen_clang_visitChildren(CXCursor *parent, bindgen_visitor visitor,  void *data)
{
  struct bindgen_context ctx = { .visitor = visitor, .data = data };

  return clang_visitChildren(*parent, bindgen_clang_visit, &ctx);
}
