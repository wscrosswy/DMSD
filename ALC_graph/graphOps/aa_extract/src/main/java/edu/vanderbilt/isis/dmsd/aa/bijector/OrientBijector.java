package edu.vanderbilt.isis.dmsd.aa.bijector;


import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.tool.ODatabaseExport;
import edu.vanderbilt.isis.dmsd.aa.GraphBijector;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;



public class OrientBijector extends AbstractGraphBijector implements GraphBijector {

    final static Logger log = LoggerFactory.getLogger(OrientBijector.class);

    public enum Orient  {
        URL("orient-url" ),
        USER("orient-user"),
        PASS("orient-pass"),
        FILE_NAME("orient-file-name");

        public final String key;
        Orient(String key) { this.key = key; }
    }


    public OrientBijector(final Map<String, String> options) {
        super(options);
    };

    OrientGraph graph = null;
    Configuration config = new BaseConfiguration();;

    /**
     * http://orientdb.com/docs/3.0.x/tinkerpop3/OrientDB-TinkerPop3.html
     *
     * @return
     */
    @Override
    public Boolean init() {
        log.info("initialize graph");

        if (this.options == null || this.options.isEmpty() ) {
            log.info("in memory orientdb: null options");
            this.graph =  OrientGraph.open();
            return true;
        }
        String inMemoryQ = this.options.get(Orient.URL.key);
        if (inMemoryQ == null || inMemoryQ.isEmpty()) {
            log.info("in memory orientdb: requested");
            this.graph =  OrientGraph.open();
            return true;
        }

        log.info("config orientdb: {}", this.options);
        this.config.setProperty("orient-url", options.get(Orient.URL.key));
        this.config.setProperty("orient-user", options.get(Orient.USER.key));
        this.config.setProperty("orient-pass", options.get(Orient.PASS.key));

        try {
            this.graph = OrientGraph.open(this.config);
            log.info("database open: {}", graph);
        } catch (com.orientechnologies.orient.core.exception.ODatabaseException ex) {
            log.warn("could not open graph database: {}", ex.getLocalizedMessage());
        }
        return true;
    }


    /**
     *
     *   -  encoding : IO.graphml or IO.graphson
     */
    @Override
    public Boolean persist() {
        GraphTraversalSource g = graph.traversal();
        Path basePath = super.base_path();

        log.info("writting graphml");
        g.io(String.format("%s.%s", basePath.toString(), IO.graphml))
                .with(IO.writer, IO.graphml)
                .write().iterate();

        log.info("writing export json");
        OCommandOutputListener listener = new OCommandOutputListener() {
            @Override
            public void onMessage(String msg) {
                log.info("export {}", msg);
            }
        };
        try {

            ODatabaseDocument db = this.graph.getRawDatabase();
            if (db instanceof ODatabaseDocumentInternal) {
                log.info("export database as json");
                ODatabaseDocumentInternal dbi = (ODatabaseDocumentInternal) db;
                String ef = String.format("%s-%s-%s.export",
                        options.get(Opt.EXPORT_BASE.key),
                        options.get(Opt.TARGET.key),
                        options.get(Opt.PG_DBASE.key));
                ODatabaseExport export = new ODatabaseExport(dbi, ef, listener);
                export.exportDatabase();
                export.close();
            } else {
                log.warn("not a internal database ");
                // ODatabaseRecordThreadLocal.INSTANCE.get();
            }

        } catch (IOException ex) {

        }

        return true;
    }

    @Override
    public Boolean complete() {
        try {
            graph.close();
        } catch (Exception ex) {
            log.warn("datbase does not close: {}", ex.getLocalizedMessage());
            return false;
        }
        return true;
    }

    @Override
    public Graph get() {
        return this.graph;
    }


    @Override
    public Boolean createVertexIndex(String property, String label) {
        // OrientVertexType account = this.graph.createVertexType(label);
        this.graph.createVertexIndex(property, label, this.config);
        return true;
    }



}
