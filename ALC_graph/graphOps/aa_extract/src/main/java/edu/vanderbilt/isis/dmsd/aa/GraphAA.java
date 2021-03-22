
package edu.vanderbilt.isis.dmsd.aa;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import groovy.json.StringEscapeUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
// import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
// import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.tinkerpop.gremlin.process.traversal.P.eq;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.addV;
import java.util.UUID;


public class GraphAA {
    final static Logger log = LoggerFactory.getLogger(GraphAA.class);

    File aa_file;
    final Map<String, String> options;

    final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param database
     * @return
     */
    public Boolean openFile(String database) {
        String fname = "input/"+database;
        this.aa_file = new File(fname);
        log.warn("using file: {}", fname);
        if (!this.aa_file.exists()) {
            log.warn("could not find file: {}", database);
            return false;
        }


        try {

        } catch (Exception ex) {
            log.warn("could not find file: {}", database);
            return false;
        }
        return true;
    }

    static String VERTEX_TO_STRING(Iterator<VertexProperty<Vertex>> it) {
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            VertexProperty<Vertex> vp = it.next();
            sb.append("[").append(vp.key()).append("]: {").append(vp.value()).append("}, ");
        }
        return sb.toString();
    }

    /**
     * https://github.com/FasterXML/jackson-databind
     *
     * @param gts
     * @param element
     */
    private void loadElement(GraphTraversalSource gts, JsonNode element) {
        log.info("processing {}", element);

        element.elements();
        for (Iterator<JsonNode> childIx = element.elements(); childIx.hasNext(); ) {
            JsonNode child = childIx.next();
            if (child instanceof Map) {
                loadElement(gts, child);
                continue;
            }
            if (child instanceof List) {
                loadElement(gts, child);
                continue;
            }
            if (child.isInt()) {
                gts.addV();
                continue;
            }
            if (child.isDouble()) {
                gts.addV();
                continue;
            }
            if (child.isTextual()) {
                gts.addV();
                continue;
            }
            log.warn("bad json element {}", child);
        }
    }

    private void loadMap(GraphTraversalSource gts, Map<String, Object> mmap) {
        GraphTraversal<Vertex, Vertex> gt;
        log.info("processing {}", mmap);

        Iterator<Map.Entry<String, Object>> itr = mmap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Object> entry = itr.next();
            String myk = entry.getKey();
            Object myo = entry.getValue();
            String metatype;
            Map<String, Object> props = (Map<String, Object>) myo;
            metatype = (String) props.get("MetaType");
            gt = gts.addV(metatype);
            gt.property("modelKey", myk);

            Iterator<Map.Entry<String, Object>> propitr = props.entrySet().iterator();
            while (propitr.hasNext()) {
                Map.Entry<String, Object> prop = propitr.next();
                String propk = prop.getKey();
                Object propo = prop.getValue();
                if (propk == "Attributes") {
                    Map<String, Object> attrs = (Map<String, Object>) propo;
                    Iterator<Map.Entry<String, Object>> attritr = attrs.entrySet().iterator();
                    while (attritr.hasNext()) {
                        Map.Entry<String, Object> attr = attritr.next();
                        String attrk = attr.getKey().replaceAll(" ", "_");
                        Object attro = attr.getValue();
                        if (attrk != "Definition") {
                            gt.property("attr_" + attrk, attro);
                            if(attro == "SafetyCase")
                                System.out.println("SafetyCase attro =="+ attro);
                        }
                    }
                    UUID rguid = UUID.randomUUID();
                    // was causing a duplicate key in the graph export
                    //gt.property("instance_guid", rguid.toString());
                }
                else if (propk == "References") {
                    Map<String, Object> reffs = (Map<String, Object>) propo;
                    Iterator<Map.Entry<String, Object>> reffsitr = reffs.entrySet().iterator();
                    while (reffsitr.hasNext()) {
                        Map.Entry<String, Object> attr = reffsitr.next();
                        String attrk = attr.getKey().replaceAll(" ", "_");
                        Object attro = attr.getValue();
                        if (attrk != "Definition")
                            gt.property("ref_" + attrk, attro);
                    }
                    UUID rguid = UUID.randomUUID();
                    gt.property("instance_guid", rguid.toString());
                } else  {
                    gt.property(propk, propo);
                }

                //System.out.println("Key = " + myk);
            }
            Vertex tag = gt.next();
            log.debug(VERTEX_TO_STRING(tag.properties()));

        }

        log.info("Establishing implements edges");
        gts
                .V().has("ref_implements").as("src")
                .V().has("modelKey").as("dest")
                .where("src", eq("dest")).by("ref_implements").by("modelKey")
                .addE("implements").from("src").to("dest")
                .iterate();
        log.info("done");

        log.info("Establishing validIn edges");
        gts
                .V().has("ref_validIn").as("src")
                .V().has("modelKey").as("dest")
                .where("src", eq("dest")).by("ref_validIn").by("modelKey")
                .addE("validIn").from("src").to("dest")
                .iterate();
        log.info("done");

        log.info("Establishing Ref edges");
        gts
                .V().has("ref_Ref").as("src")
                .V().has("modelKey").as("dest")
                .where("src", eq("dest")).by("ref_Ref").by("modelKey")
                .addE("Ref").from("src").to("dest")
                .iterate();
        log.info("done");


        log.info("Establishing uses edges");
        gts
                .V().has("ref_uses").as("src")
                .V().has("modelKey").as("dest")
                .where("src", eq("dest")).by("ref_uses").by("modelKey")
                .addE("implements").from("src").to("dest")
                .iterate();
        log.info("done");

        log.info("Establishing Parent/Child via has_parent links");
        gts
                .V().has("ParentPath").as("child")
                .V().has("modelKey").as("parent")
                .where("child", eq("parent")).by("ParentPath").by("modelKey")
                .addE("has_parent").from("child").to("parent")
                .iterate();
        log.info("done");

        log.info("connecting source objects to connector");
        gts
                .V().has("SrcPath").as("connector")
                .V().has("modelKey").as("source")
                .where("connector", eq("source")).by("SrcPath").by("modelKey")
                .addE("starts").from("source").to("connector")
                .iterate();

        log.info("connecting connector to dest objects");
        gts
                .V().has("DstPath").as("connector")
                .V().has("modelKey").as("dest")
                .where("connector", eq("dest")).by("DstPath").by("modelKey")
                .addE("ends").from("connector").to("dest")
                .iterate();

        log.info("Establish" +
                "" +
                "ing BasePath via base_class links");
        gts
                .V().has("BasePath").as("child")
                .V().has("modelKey").as("parent")
                .where("child", eq("parent")).by("BasePath").by("modelKey")
                .addE("parent_class").from("child").to("parent")
                .iterate();
        log.info("done");

/*
        log.info("Establishing Parent/Child via has_parent links");
        gts
                .V().hasLabel("node").has("ParentPath").as("child")
                .V().hasLabel("node").has("modelKey").as("parent")
                .where("child", eq("parent")).by("ParentPath").by("modelKey")
                .addE("has_parent").from("child").to("parent")
                .iterate();

 */
        log.info("done");

/*
        log.info("assigning objects to their parent");
        gts
                .V().hasLabel("Object").has("object_id").as("parent")
                .V().hasLabel("Object").has("parentid").as("child")
                .where("child", eq("parent")).by("parentid").by("object_id")
                .addE("has_parent").from("child").to("parent")
                .iterate();
*/
        log.info("Establishing specializes edges");
        gts
                .V().has("ref_specializes").as("src")
                .V().has("modelKey").as("dest")
                .where("src", eq("dest")).by("ref_specializes").by("modelKey")
                .addE("specializes").from("src").to("dest")
                .iterate();
        log.info("done");
    }



    /**
     * Get the tables:
     * - t_object :
     *
     * http://tinkerpop.apache.org/javadocs/3.2.2/full/org/apache/tinkerpop/gremlin/process/traversal/dsl/graph/GraphTraversalSource.html
     *
     * @return
     */
    public Graph load(final GraphBijector gb) throws Exception {

        gb.createVertexIndex("package_id", "Package");

        log.info("load graph");
        Graph graph = gb.get();
        if ( graph == null ) {
            throw new Exception("graph not created");
        }
        GraphTraversalSource gts = graph.traversal();

        try {
            Map<String,Object> mymap = mapper.readValue(this.aa_file,Map.class);
            JsonNode root = mapper.readTree(this.aa_file);
            loadMap(gts,  mymap );
            log.warn("got root");
        } catch (IOException ex) {
            //GraphAA.printSQLException(ex);
            log.warn("cannot get root in json file");
        }


        log.info("linking packages in a hierarchy");
        gts
                .V().hasLabel("Package").has("package_id").as("parent")
                .V().hasLabel("Package").has("parent_id").as("child")
                .where("parent", eq("child")).by("package_id").by("parent_id")
                .addE("in_package").from("child").to("parent")
                .iterate();

        return graph;
    }


    public GraphAA(final Map<String, String> options) {
        this.options = options;
        this.openFile(this.options.get("pg_dbname"));
    }

}
