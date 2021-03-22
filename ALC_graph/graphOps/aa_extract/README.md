
# The Sparx/EA Graph

For all the work below presuem the current working directory is...

```bash
pushd aa_extract
```

## Make TinkerGraph

Extract TinkerGraph from JSON output of WebGME

```
./gradlew :aa_extract:run_webgme_tg
```


### Load the Graph into Gremlin 

#### Install the TinkerPop3 REPL

Install the "Gremlin Console" as [described here](http://tinkerpop.apache.org/downloads.html).


#### Start the REPL

Once loaded the gremlin repl may be started (depending on where installed).

```bash
env CWD=`pwd` /opt/apache/tinkerpop/latest/bin/gremlin.sh
```

Load the graphson file...

```bash
 cwd = System.getenv("CWD")
 ifs = new File(cwd, 'foo.graphson')
 graph = TinkerGraph.open()
 g = graph.traversal()
 g.io(ifs.path).with(IO.reader, 'graphson').read().iterate()
```

## Graph Queries

The purpose is to be able to navigate the graph with

http://tinkerpop.apache.org/docs/current/tutorials/the-gremlin-console/

### Get a named node as an example

tcsv = g.V().has('object', 'name', 'Foo').next()

### What is its abstraction level?  [Conceptual, Logical, Platform, UoP, FaceData]

g.V(foo).repeat(out('package')).times(4).emit().path().by('name')

Get package level objects.

levels = g.V().has('object', 'object_type', 'Package').has('stereotype', within('ConceptualModel','LogicalModel','PlatformModel')).out('package')

g.V(tcsv).repeat(__.out('package')).until(within(levels)).emit().path().by('name')