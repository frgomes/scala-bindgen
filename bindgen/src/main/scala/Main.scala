package demo


object Main {

  private val USAGE = s"
    |Generate C bindings for Scala Native.
    |Usage:
    |  bindgen [options] <file> [-- <clang-args>...]
    |  bindgen (-h | --help)
    |Options:
    |  <clang-args>                 Options passed directly to clang.
    |  -h, --help                   Display this help message.
    |  --link=<library>             Link to a dynamic library, can be provided multiple times.
    |                               <library> is in the format `[kind=]lib`, where `kind` is
    |                               one of `static`, `dynamic` or `framework`.
    |  --output=<output>            Write bindings to <output> (- is stdout).
    |                               [default: -]
    |  --match=<name>               Only output bindings for definitions from files
    |                               whose name contains <name>
    |                               If multiple -match options are provided, files
    |                               matching any rule are bound to.
    |  --builtins                   Output bindings for builtin definitions
    |                               (for example __builtin_va_list)
    |  --emit-clang-ast             Output the ast (for debugging purposes)
    |  --override-enum-type=<type>  Override enum type, type name could be
    |                                 uchar
    |                                 schar
    |                                 ushort
    |                                 sshort
    |                                 uint
    |                                 sint
    |                                 ulong
    |                                 slong
    |                                 ulonglong
    |                                 slonglong
    |  --use-core                  Use `core` as a base crate for `Option` and such.
    |                              See also `--ctypes-prefix`.
    |  --ctypes-prefix=<prefix>    Use this prefix for all the types in the generated
    |                              code.
    |                              [default: std::os::raw]
    |  --remove-prefix=<prefix>    Prefix to remove from all the symbols, like
    |                              `libfoo_`. The removal is case-insensitive.
    |  --no-derive-debug           Disable `derive(Debug)` for all generated types.
    |  --no-rust-enums             Convert C enums to Rust constants instead of enums.
    |  --dont-convert-floats       Disables the convertion of C `float` and `double`
    |                              to Rust `f32` and `f64`.
    |".stripMargin


  def main(args: Array[String]): Unit = {

  }

  def args_to_opts(args: Args): Builder {
    var builder = Builder::new(args.arg_file);
    builder.emit_ast(args.flag_emit_clang_ast)
           .ctypes_prefix(args.flag_ctypes_prefix
                              .split("::")
                              .map(String::from)
                              .collect::<Vec<_>>())
           .use_core(args.flag_use_core)
           .derive_debug(!args.flag_no_derive_debug)
           .rust_enums(!args.flag_no_rust_enums)
           .override_enum_ty(args.flag_override_enum_type);
    for arg in args.arg_clang_args {
        builder.clang_arg(arg);
    }
    if let Some(s) = args.flag_match {
        builder.match_pat(s);
    }
    if let Some(s) = args.flag_remove_prefix {
        builder.remove_prefix(s);
    }
    if args.flag_builtins {
        builder.builtins();
    }
    if args.flag_dont_convert_floats {
        builder.dont_convert_floats();
    }
    if let Some(link) = args.flag_link {
        var parts = link.split('=');
        let (lib, kind) = match (parts.next(), parts.next()) {
            (Some(lib), None) => (lib, LinkType::Dynamic),
            (Some(kind), Some(lib)) => {
                (lib,
                 match kind {
                    "static" => LinkType::Static,
                    "dynamic" => LinkType::Dynamic,
                    "framework" => LinkType::Framework,
                    _ => {
                        println!("Link type unknown: {}", kind);
                        exit(1);
                    }
                })
            }
            _ => {
                println!("Wrong link format: {}", link);
                exit(1);
            }
        };
        builder.link(lib, kind);
    }
    builder
}

}



object Builder {
    /// Returns a new builder for the C header to parse.
    pub fn new<T: Into<String>>(header: T) -> Builder<'a> {
        let mut builder = Builder {
            logger: None,
            options: Default::default(),
        };
        builder.clang_arg(header);
        builder
    }
}
class Builder {
  val options: BindgenOptions
  val logger: Option[Logger]

  /// Add a pattern to filter which file to generate a binding for.
  def match_pat<T: Into<String>>(&mut self, arg: T) -> &mut Self {
    self.options.match_pat.push(arg.into());
    self
  }

    /// Add a clang CLI argument.
    def clang_arg<T: Into<String>>(&mut self, arg: T) -> &mut Self {
        self.options.clang_args.push(arg.into());
        self
    }

    /// Add a library to link.
    def link<T: Into<String>>(&mut self, library: T, link_type: LinkType) -> &mut Self {
        self.options.links.push((library.into(), link_type));
        self
    }

    /// Force bindgen to exit if a type is not recognized.
    def forbid_unknown_types(&mut self) -> &mut Self {
        self.options.fail_on_unknown_type = true;
        self
    }

    /// Control if we should use the c builtins like `__va_list`.
    def builtins(&mut self) -> &mut Self {
        self.options.builtins = true;
        self
    }

    /// Control if the generated structs will derive Debug.
    def derive_debug(&mut self, derive_debug: bool) -> &mut Self {
        self.options.derive_debug = derive_debug;
        self
    }

    /// Control if bindgen should convert the C enums to rust enums or rust constants.
    def rust_enums(&mut self, value: bool) -> &mut Self {
        self.options.rust_enums = value;
        self
    }

    /// Set the logger to use.
    def log(&mut self, logger: &'a Logger) -> &mut Self {
        self.logger = Some(logger);
        self
    }

    /// Overrides the type used to represent a C enum.
    def override_enum_ty<T: Into<String>>(&mut self, ty: T) -> &mut Self {
        self.options.override_enum_ty = ty.into();
        self
    }

    /// Set the prefix to remove from all the symbols, like `libfoo_`.
    def remove_prefix<T: Into<String>>(&mut self, ty: T) -> &mut Self {
        self.options.remove_prefix = ty.into();
        self
    }

    /// Controls if bindgen should also print the parsed AST (for debug).
    def emit_ast(&mut self, value: bool) -> &mut Self {
        self.options.emit_ast = value;
        self
    }

    /// Defines if we should use `std` or `core` for `Option` and such.
    def use_core(&mut self, value: bool) -> &mut Self {
        self.options.use_core = value;
        self
    }

    /// Sets the prefix to use for c_void and others.
    def ctypes_prefix<T: Into<Vec<String>>>(&mut self, prefix: T) -> &mut Self {
        self.options.ctypes_prefix = prefix.into();
        self
    }

    /// Defines if we should convert float and double to f32 and f64.
    ///
    /// The format is [not defined](https://en.wikipedia.org/wiki/C_data_types#Basic_types),
    /// but is the same as in rust in all the supported platforms.
    def dont_convert_floats(&mut self) -> &mut Self {
        self.options.convert_floats = false;
        self
    }

    /// Generate the binding using the options previously set.
    def generate(&self) -> Result<Bindings, ()> {
        Bindings::generate(&self.options, self.logger, None)
    }
}








pub struct BindgenOptions {
    pub match_pat: Vec<String>,
    pub builtins: bool,
    pub rust_enums: bool,
    pub links: Vec<(String, LinkType)>,
    pub emit_ast: bool,
    pub fail_on_unknown_type: bool,
    pub override_enum_ty: String,
    pub clang_args: Vec<String>,
    pub derive_debug: bool,
    /// The prefix to use for the c types like c_void.
    ///
    /// Default: ["std", "os", "raw"]
    pub ctypes_prefix: Vec<String>,
    /// Defines if we should use `std` or `core` for `Option` and such.
    pub use_core: bool,
    /// Prefix to remove from all the symbols, like `libfoo_`.
    pub remove_prefix: String,
    /// See `Builder::convert_floats`.
    pub convert_floats: bool,
}

impl Default for BindgenOptions {
    fn default() -> BindgenOptions {
        let clang = Clang::find(None).expect("No clang found, is it installed?");
        let mut args = Vec::new();
        for dir in clang.c_search_paths {
            args.push("-idirafter".to_owned());
            args.push(dir.to_str().unwrap().to_owned());
        }
        BindgenOptions {
            match_pat: Vec::new(),
            builtins: false,
            rust_enums: true,
            links: Vec::new(),
            emit_ast: false,
            fail_on_unknown_type: true,
            override_enum_ty: "".to_owned(),
            clang_args: args,
            derive_debug: true,
            ctypes_prefix: vec!["std".into(), "os".into(), "raw".into()],
            use_core: false,
            remove_prefix: String::new(),
            convert_floats: true,
        }
    }
}

/// Type of the link to the library which binding is generating.
#[derive(Debug, Copy, Clone, PartialEq, Eq, PartialOrd, Ord)]
pub enum LinkType {
    /// Do a static link to the library.
    Static,
    /// Do a dynamic link to the library.
    Dynamic,
    /// Link to a MacOS Framework.
    Framework,
}

/// Trait used internaly to log things with context like the C file line number.
pub trait Logger: std::fmt::Debug {
    /// Defaults to `error!()`.
    fn error(&self, msg: &str) {
        error!("{}", msg);
    }

    /// Defaults to `warn!()`.
    fn warn(&self, msg: &str) {
        warn!("{}", msg);
    }
}

/// Contains the generated code.
#[derive(Clone)]
pub struct Bindings {
    module: ast::Mod,
    attributes: Vec<ast::Attribute>,
}

