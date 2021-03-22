// Initialize graph
cwd = System.getenv("CWD")
ifs = new File(cwd + '/server/data', 'sparx-tg-JSON_Output_ROOT-1608060036328.json.graphson')
graph = TinkerGraph.open()
g = graph.traversal()
g.io(ifs.path).with(IO.reader, 'graphson').read().iterate()

// Language subgraph
language = g.V().has('attr_name','Language').repeat(__.in('has_parent')).emit().toList()
meta = { e -> e.property('MetaType').value() }
// *** TED: could be something interesting here ***

// Traversal Functions
pathsBetween = { a,b -> g.V().is(within(a)).repeat(both('parent_class').simplePath().is(not(within(language)))).until(is(within(b))).path() }
endpointsOf = { paths -> paths.collect{ [it.first(), it.last()] }.flatten() }
childrenOf = { a -> g.V(a).in('has_parent').toList() }
orphansWithRespectTo = { a,b -> childrenOf(a) - endpointsOf(pathsBetween(childrenOf(nhs),childrenOf(us))) }

// Multiplicity-related Functions
multiplicity = { c ->
    assert meta(c) in ['Composition', 'Participant']
    [lower: c.property('attr_lowerBound').value(), upper: c.property('attr_upperBound').value()]
}
cardinalityGT = { c1, c2 -> ( c1 == -1? Integer.MAX_VALUE : c1 ) > (c2 == -1? Integer.MAX_VALUE : c2) }
multiplicitiesOverlap = { m1, m2 -> cardinalityGT(m1.upper, m2.lower)}

// Example elements
nhs = g.V().hasLabel('Entity').has('attr_name', 'CareConnect-Patient-1').next()
us = g.V().hasLabel('Entity').has('attr_name', 'us-core-patient').next()

// Example traversal
// Check attribute presence
nhs_us_paths = pathsBetween(childrenOf(nhs),childrenOf(us)).toList()
nhs_orphans = orphansWithRespectTo(nhs,us).toList()
us_orphans = orphansWithRespectTo(us,nhs).toList()

// Compare linked attribute multiplicities
nhs_us_paths.each{ p ->
    m1 = multiplicity(endpointsOf(p)[0])
    m2 = multiplicity(endpointsOf(p)[1])

    print m1
    print m2
}
