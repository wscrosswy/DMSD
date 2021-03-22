package edu.vanderbilt.isis.dmsd.aa;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface GraphBijector {

     enum Opt  {
         OUT_DIR("out_dir"),
         TARGET("target" ),
         PG_DBASE("pg_dbname"),
         EXPORT_BASE("export_base");

         public final String key;
         Opt(String key) { this.key = key; }

    }


    enum Target {
        ODB("odb" ),
        TG("tg"),
        NEO("neo");

        public final String key;
        Target(String key) { this.key = key; }

        boolean equal(String otherKey) {
            return otherKey.equals(this.key);
        }

        static Target get(String otherKey) {
            for ( Target tgt : Target.values()) {
                if (otherKey.equals(tgt.key)) {
                    return tgt;
                }
            }
            return null;
        }
    }

    Logger log = LoggerFactory.getLogger(GraphBijector.class);

    Boolean init();
    Boolean persist();
    Boolean complete();
    Graph get();
    Boolean createVertexIndex(String property, String label);

}
