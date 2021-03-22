
package edu.vanderbilt.isis.dmsd.aa;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.vanderbilt.isis.dmsd.aa.bijector.Neo4JBijector;
import edu.vanderbilt.isis.dmsd.aa.bijector.OrientBijector;
import edu.vanderbilt.isis.dmsd.aa.bijector.TinkerGraphBijector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class CodeGen {
  final static Logger log = LoggerFactory.getLogger(CodeGen.class);

  /**
   * http://jcommander.org/
   */

  @Parameter(names = "--help", help = true)
  private boolean help;

  public static class PathConverter implements IStringConverter<Path> {
    @Override
    public Path convert(String value) {
      List<String> path = Arrays.asList(value.split("/"));
      String first = path.remove(0);
      String[] rest = path.toArray(new String[path.size()]);
      return Paths.get(first, rest);
    }
  }
  @Parameter(names = {"--output-dir", "--outdir"}, description = "output directory", converter = PathConverter.class)
  private Path outdir = Paths.get("output");

  @Parameter(names = {"--target"}, description = "target database type : tg, odb, neo")
  private String target = "tg";

  @Parameter(names = { "--pg-db", "--postgresql-db", "--pg-dbase" }, description = "source postgesql database")
  private String pg_database = "";


  @Parameter(names = { "--orient-url", "-ou" }, description = "orientDB remote database url, blank for local")
  private String orient_url = "";

  @Parameter(names = {"--orient-user"}, description = "orientDB user name")
  private String orient_user = "root";

  @Parameter(names = "--orient-pass", description = "Connection password", password = true)
  private String orient_pass = "orientdb";

  @Parameter(names = "--orient-file-name", description = "orientdb export file name")
  private String orient_file_name = "orientdb";

  @Parameter(names = {"--export-basename", "--export"},
          description = "the base name for the export files, default: sparx")
  private String export_base = "sparx";



  public static void main(String[] argv) {

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    StatusPrinter.print(lc);

    CodeGen main = new CodeGen();
    JCommander jc = JCommander.newBuilder()
            .addObject(main)
            .build();

    jc.parse(argv);
    main.run(jc);

  }


  public void run(JCommander jc) {

    if (help) {
      jc.usage();
      return;
    }

    log.info("target database {}", target );
    final Map<String,String> options = new HashMap<String,String>();

    options.put(GraphBijector.Opt.OUT_DIR.key, outdir.toString());
    options.put(GraphBijector.Opt.TARGET.key, target);
    options.put(GraphBijector.Opt.PG_DBASE.key, pg_database);
    options.put(GraphBijector.Opt.EXPORT_BASE.key, export_base);

    GraphBijector gb = null;

    switch (GraphBijector.Target.get(target)) {
      case ODB:
          options.put(OrientBijector.Orient.URL.key, orient_url );
          options.put(OrientBijector.Orient.USER.key, orient_user);
          options.put(OrientBijector.Orient.PASS.key, orient_pass);
          options.put(OrientBijector.Orient.FILE_NAME.key, orient_file_name);

          gb = new OrientBijector(options);
         break;

      case TG:
        gb = new TinkerGraphBijector(options);
        break;

      case NEO:
        gb = new Neo4JBijector(options);

      default:
        log.error("No known database target selected : {}", target);
        jc.usage();
    }

    gb.init();

    GraphAA gs = new GraphAA(options);
    try {
      gs.load(gb);
    } catch (Exception ex) {
      log.error("no valid graph supplied {}", ex.getLocalizedMessage());
    }

    gb.persist();
    gb.complete();
  }

}