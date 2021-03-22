package edu.vanderbilt.isis.dmsd.aa.bijector;

import edu.vanderbilt.isis.dmsd.aa.GraphBijector;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.GraphWriter;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TinkerGraphBijector extends AbstractGraphBijector implements GraphBijector {

    final static Logger log = LoggerFactory.getLogger(TinkerGraphBijector.class);

    TinkerGraph graph;

    public TinkerGraphBijector(final Map<String, String> options) {
        super(options);
    };

    /**
     * http://tinkerpop.apache.org/javadocs/3.2.2/full/org/apache/tinkerpop/gremlin/tinkergraph/structure/TinkerGraph.html
     *
     * @return
     */
    @Override
    public Boolean init() {
        log.info("initialize graph");
        // Configuration config = new ConfigurationBuilder().;
        this.graph = TinkerGraph.open();

        return true;
    }

    /**
     *   -  encoding : IO.graphml or IO.graphson
     */
    @Override
    public Boolean persist() {

        GraphTraversalSource g = graph.traversal();
        String basePath = super.base_path().toString();

        g.io(String.format("%s.%s", basePath, IO.graphml))
                .with(IO.writer, IO.graphml).write().iterate();

        g.io(String.format("%s.%s", basePath, IO.graphson))
                .with(IO.writer, IO.graphson).write().iterate();

        GraphWriter writer = GraphSONWriter.build()
                .wrapAdjacencyList(true).create();
        g.io(String.format("%s.%s", basePath, "json"))
                .with(IO.writer, writer).write().iterate();
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
    public Boolean createVertexIndex(String property, String label) {
        this.graph.createIndex(property, Vertex.class);
        return true;
    }


    @Override
    public Graph get() {
        return this.graph;
    }

}
