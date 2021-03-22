package edu.vanderbilt.isis.dmsd.aa.bijector;

import edu.vanderbilt.isis.dmsd.aa.GraphBijector;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public abstract class AbstractGraphBijector implements GraphBijector {

    public final Map<String, String> options;

    public AbstractGraphBijector(final Map<String, String> options) {
        this.options = options;
    };

    public Path base_path() {
            return Paths.get(options.get(Opt.OUT_DIR.key),
                    String.format("%s-%s-%s",
                    options.get(Opt.EXPORT_BASE.key),
                    options.get(Opt.TARGET.key),
                    options.get(Opt.PG_DBASE.key)));
    }
}
