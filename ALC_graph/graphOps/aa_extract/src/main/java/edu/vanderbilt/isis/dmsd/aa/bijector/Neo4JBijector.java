package edu.vanderbilt.isis.dmsd.aa.bijector;


import edu.vanderbilt.isis.dmsd.aa.GraphBijector;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.GraphWriter;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Neo4JBijector extends AbstractGraphBijector implements GraphBijector {

    final static Logger log = LoggerFactory.getLogger(Neo4JBijector.class);

    Graph graph;

    public Neo4JBijector(final Map<String, String> options) {
        super(options);
    };

    /**
     * http://tinkerpop.apache.org/javadocs/3.2.2/full/org/apache/tinkerpop/gremlin/tinkergraph/structure/TinkerGraph.html
     *
     * @return
     */
    public  Boolean init() {

        //Graph graph = Neo4jGraph.open(this.options.get("db_filename"));
        //GraphTraversalSource g = graph.traversal();
        this.graph = null;

        return true;
    }


    private void saveGraph(Graph graph, String fn, String encoding) {
        GraphTraversalSource g = graph.traversal();
        switch (encoding) {
            case IO.graphml:
                g.io(fn)
                        .with(IO.writer, encoding)
                        .write().iterate();
                break;

            case IO.graphson:
                g.io(fn)
                        .with(IO.writer, encoding)
                        .write().iterate();
                break;

            case "json":

                GraphWriter writer = GraphSONWriter.build()
                        .wrapAdjacencyList(true).create();

                g.io(fn)
                        .with(IO.writer, writer)
                        .write().iterate();
                break;


            default:

                g.io(fn)
                        .with(IO.writer, IO.graphson)
                        .write().iterate();
                break;

        }
    }

    /**
     *
     */
    @Override
    public Boolean persist() {

        String baseName = super.base_path().toString();

        this.saveGraph( this.graph,"sparx-neo.graphml", "graphml");
        this.saveGraph( this.graph, "sparx-neo.graphson", "graphson");
        this.saveGraph( this.graph, "sparx-neo.json", "json");

        return true;
    }



    @Override
    public Boolean complete() {
        try {
            this.graph.close();
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
        // this.graph.createIndex(property, Vertex.class);
        return true;
    }


}
