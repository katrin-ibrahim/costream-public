/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.streams;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.storm.Config;
import org.apache.storm.Constants;
import org.apache.storm.annotation.InterfaceStability;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.shade.com.google.common.collect.ArrayListMultimap;
import org.apache.storm.shade.com.google.common.collect.HashBasedTable;
import org.apache.storm.shade.com.google.common.collect.Multimap;
import org.apache.storm.shade.com.google.common.collect.Table;
import org.apache.storm.shade.org.jgrapht.graph.DefaultDirectedGraph;
import org.apache.storm.shade.org.jgrapht.traverse.TopologicalOrderIterator;
import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.streams.operations.IdentityFunction;
import org.apache.storm.streams.operations.mappers.PairValueMapper;
import org.apache.storm.streams.operations.mappers.TupleValueMapper;
import org.apache.storm.streams.processors.BaseProcessor;
import org.apache.storm.streams.processors.MapProcessor;
import org.apache.storm.streams.processors.Processor;
import org.apache.storm.streams.processors.StateQueryProcessor;
import org.apache.storm.streams.processors.StatefulProcessor;
import org.apache.storm.streams.processors.UpdateStateByKeyProcessor;
import org.apache.storm.streams.windowing.Window;
import org.apache.storm.topology.BoltDeclarer;
import org.apache.storm.topology.IBasicBolt;
import org.apache.storm.topology.IComponent;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Tuple;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.tuple.Triple;
/**
 * A builder for constructing a {@link StormTopology} via storm streams api (DSL).
 */
@InterfaceStability.Unstable
public class StreamBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(StreamBuilder.class);
    private static final Logger logger = LoggerFactory.getLogger("grouping");
    private final DefaultDirectedGraph<Node, Edge> graph;
    private final Table<Node, String, GroupingInfo> nodeGroupingInfo = HashBasedTable.create();
    private final Map<Node, WindowNode> windowInfo = new HashMap<>();
    private final List<ProcessorNode> curGroup = new ArrayList<>();
    private final Map<StreamBolt, BoltDeclarer> streamBolts = new HashMap<>();
    private int statefulProcessorCount = 0;
    private String timestampFieldName = null;
    private final ArrayList<Triple<String,String,String>> logBolts = new ArrayList<>();
    private final Config conf;
    private MongoClient mongoClient;

    /**
     * Creates a new {@link StreamBuilder}.
     */
    public StreamBuilder() {
        this(new Config());
    }

    public StreamBuilder(Config conf) {
        this.conf = conf;
        graph = new DefaultDirectedGraph<>(new StreamsEdgeFactory());
        boolean localExecution = conf.get("storm.cluster.mode").equals("local");
        if (!localExecution) {
            MongoCredential cred = MongoCredential.createCredential("dsps_mongo", "dsps", "s3cure!".toCharArray());
            mongoClient = new MongoClient(
                    new ServerAddress((String) conf.get("mongo.address"), (int) conf.get("mongo.port")),
                    Collections.singletonList(cred));
        }
    }

    /**
     * Creates a new {@link Stream} of tuples from the given {@link IRichSpout}.
     *
     * @param spout the spout
     * @return the new stream
     */
    public Stream<Tuple> newStream(IRichSpout spout) {
        return newStream(spout, 1, new HashMap<>());
    }


    public Stream<Tuple> newStream(IRichSpout spout, HashMap<String, Object> description) {
        return newStream(spout, 1, description);
    }

    public Stream<Tuple> newStream(IRichSpout spout, int parallelism) {
        return newStream(spout, parallelism, new HashMap<>());
    }

    /**
     * Creates a new {@link Stream} of tuples from the given {@link IRichSpout} with the given parallelism.
     *
     * @param spout       the spout
     * @param parallelism the parallelism of the stream
     * @return the new stream
     */
    public Stream<Tuple> newStream(IRichSpout spout, int parallelism, HashMap<String, Object> description) {
        SpoutNode spoutNode = new SpoutNode(spout, description);
        String spoutId = UniqueIdGen.getInstance().getUniqueSpoutId();
        spoutNode.setComponentId(spoutId);
        spoutNode.setParallelism(parallelism);
        graph.addVertex(spoutNode);
        return new Stream<>(this, spoutNode);
    }

    /**
     * Creates a new {@link Stream} of values from the given {@link IRichSpout} by extracting field(s) from tuples via the supplied {@link
     * TupleValueMapper}.
     *
     * @param spout       the spout
     * @param valueMapper the value mapper
     * @param <T>         the type of values in the resultant stream
     * @return the new stream
     */
    public <T> Stream<T> newStream(IRichSpout spout, TupleValueMapper<T> valueMapper) {
        return newStream(spout).map(valueMapper, new HashMap<>());
    }

    public <T> Stream<T> newStream(IRichSpout spout, TupleValueMapper<T> valueMapper, HashMap<String, Object> description) {
        return newStream(spout).map(valueMapper, description);
    }

    /**
     * Creates a new {@link Stream} of values from the given {@link IRichSpout} by extracting field(s) from tuples via the supplied {@link
     * TupleValueMapper} with the given parallelism.
     *
     * @param spout       the spout
     * @param valueMapper the value mapper
     * @param parallelism the parallelism of the stream
     * @param <T>         the type of values in the resultant stream
     * @return the new stream
     */
    public <T> Stream<T> newStream(IRichSpout spout, TupleValueMapper<T> valueMapper, int parallelism) {
        return newStream(spout, parallelism).map(valueMapper, new HashMap<>());
    }

    /**
     * Creates a new {@link PairStream} of key-value pairs from the given {@link IRichSpout} by extracting key and value from tuples via the
     * supplied {@link PairValueMapper}.
     *
     * @param spout           the spout
     * @param pairValueMapper the pair value mapper
     * @param <K>             the key type
     * @param <V>             the value type
     * @return the new stream of key-value pairs
     */
    public <K, V> PairStream<K, V> newStream(IRichSpout spout, PairValueMapper<K, V> pairValueMapper) {
        return newStream(spout).mapToPair(pairValueMapper);
    }

    /**
     * Creates a new {@link PairStream} of key-value pairs from the given {@link IRichSpout} by extracting key and value from tuples via the
     * supplied {@link PairValueMapper} and with the given value of parallelism.
     *
     * @param spout           the spout
     * @param pairValueMapper the pair value mapper
     * @param parallelism     the parallelism of the stream
     * @param <K>             the key type
     * @param <V>             the value type
     * @return the new stream of key-value pairs
     */
    public <K, V> PairStream<K, V> newStream(IRichSpout spout, PairValueMapper<K, V> pairValueMapper, int parallelism) {
        return newStream(spout, parallelism).mapToPair(pairValueMapper);
    }


    /**
     * Builds a new {@link StormTopology} for the computation expressed via the stream api.
     *
     * @return the storm topology
     */
    public StormTopology build() {
        nodeGroupingInfo.clear();
        windowInfo.clear();
        curGroup.clear();
        TopologicalOrderIterator<Node, Edge> iterator = new TopologicalOrderIterator<>(graph, queue());
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node instanceof SpoutNode) {
                addSpout(topologyBuilder, (SpoutNode) node);
            } else if (node instanceof ProcessorNode) {
                handleProcessorNode((ProcessorNode) node, topologyBuilder);
            } else if (node instanceof PartitionNode) {
                updateNodeGroupingInfo((PartitionNode) node);
                processCurGroup(topologyBuilder);
            } else if (node instanceof WindowNode) {
                updateWindowInfo((WindowNode) node);
                processCurGroup(topologyBuilder);
            } else if (node instanceof SinkNode) {
                processCurGroup(topologyBuilder);
                addSink(topologyBuilder, (SinkNode) node);
            }
        }
        processCurGroup(topologyBuilder);
        mayBeAddTsField();
        logGroupings();
        return topologyBuilder.createTopology();
    }

    // added
    private void logGroupings() {
        boolean localExecution = conf.get("storm.cluster.mode").equals("local");
        if (!localExecution) {
            MongoDatabase db = mongoClient.getDatabase((String) conf.get("mongo.database"));
            MongoCollection<Document> collection = db.getCollection((String) conf.get("mongo.collection.grouping"));
            for (Triple<String, String,String> entry : logBolts) {
                Document json = new Document();
                json.put("operator", entry.getLeft());
                json.put("component", entry.getMiddle());
                json.put("parallelism", entry.getRight());
                collection.insertOne(json);
            }
            mongoClient.close();
        } else {
            for (Triple<String, String, String> entry : logBolts) {
                JSONObject json = new JSONObject();
                json.put("operator", entry.getLeft());
                json.put("component", entry.getMiddle());
                json.put("parallelism", entry.getRight());
                logger.info(json.toJSONString());
            }
        }
        logBolts.clear();
    }


    Node addNode(Node parent, Node child) {
        return addNode(parent, child, parent.getOutputStreams().iterator().next(), parent.getParallelism());
    }

    Node addNode(Node parent, Node child, int parallelism) {
        return addNode(parent, child, parent.getOutputStreams().iterator().next(), parallelism);
    }

    Node addNode(Node parent, Node child, String parentStreamId) {
        return addNode(parent, child, parentStreamId, parent.getParallelism());
    }

    Node addNode(Node parent, Node child, String parentStreamId, int parallelism) {
        graph.addVertex(child);
        graph.addEdge(parent, child);
        child.setParallelism(parallelism);
        if (parent instanceof WindowNode || parent instanceof PartitionNode) {
            child.addParentStream(parentNode(parent), parentStreamId);
        } else {
            child.addParentStream(parent, parentStreamId);
        }
        if (!(child instanceof PartitionNode)) {
            if (child.getGroupingInfo() != null) {
                if (!child.getGroupingInfo().equals(parent.getGroupingInfo())) {
                    throw new IllegalStateException("Trying to assign grouping info for node"
                            + " with current grouping info: "
                            + child.getGroupingInfo()
                            + " to: "
                            + parent.getGroupingInfo()
                            + " Node: "
                            + child);
                }
            } else {
                child.setGroupingInfo(parent.getGroupingInfo());
            }
        }
        if (!(child instanceof WindowNode) && !child.isWindowed()) {
            child.setWindowed(parent.isWindowed());
        }
        return child;
    }

    // insert child in-between parent and its current child nodes
    Node insert(Node parent, Node child) {
        Node newChild = addNode(parent, child);
        for (Edge edge : graph.outgoingEdgesOf(parent)) {
            Node oldChild = edge.getTarget();
            graph.removeEdge(parent, oldChild);
            oldChild.removeParentStreams(parent);
            addNode(newChild, oldChild);
        }
        return newChild;
    }

    private PriorityQueue<Node> queue() {
        // min-heap
        return new PriorityQueue<>(new Comparator<Node>() {
            /*
             * Nodes in the descending order of priority.
             * ProcessorNode has higher priority than partition and window nodes
             * so that the topological order iterator will group as many processor nodes together as possible.
             * UpdateStateByKeyProcessor has a higher priority than StateQueryProcessor so that StateQueryProcessor
             * can be mapped to the same StatefulBolt that UpdateStateByKeyProcessor is part of.
             */
            Map<Class<?>, Integer> map = new HashMap<>();

            {
                map.put(SpoutNode.class, 0);
                map.put(UpdateStateByKeyProcessor.class, 1);
                map.put(ProcessorNode.class, 2);
                map.put(PartitionNode.class, 3);
                map.put(WindowNode.class, 4);
                map.put(StateQueryProcessor.class, 5);
                map.put(SinkNode.class, 6);
            }

            @Override
            public int compare(Node n1, Node n2) {
                return getPriority(n1) - getPriority(n2);
            }

            private int getPriority(Node node) {
                Integer priority;
                // check if processor has specific priority first
                if (node instanceof ProcessorNode) {
                    Processor processor = ((ProcessorNode) node).getProcessor();
                    priority = map.get(processor.getClass());
                    if (priority != null) {
                        return priority;
                    }
                }
                priority = map.get(node.getClass());
                if (priority != null) {
                    return priority;
                }
                return Integer.MAX_VALUE;
            }
        });
    }

    private void handleProcessorNode(ProcessorNode processorNode, TopologyBuilder topologyBuilder) {
        if (processorNode.getProcessor() instanceof StatefulProcessor) {
            statefulProcessorCount++;
            Set<ProcessorNode> initialNodes = initialProcessors(
                    curGroup.isEmpty() ? Collections.singletonList(processorNode) : curGroup);
            Set<Window<?, ?>> windows = getWindowParams(initialNodes);
            // if we get more than one stateful operation, we need to process the
            // current group so that we have one stateful operation per stateful bolt
            if (statefulProcessorCount > 1 || !windows.isEmpty()) {
                if (!curGroup.isEmpty()) {
                    processCurGroup(topologyBuilder);
                } else if (!windows.isEmpty()) {
                    // a stateful processor immediately follows a window specification
                    splitStatefulProcessor(processorNode, topologyBuilder);
                }
                statefulProcessorCount = 1;
            }
        }
        curGroup.add(processorNode);
    }

    /*
     * force create a windowed bolt with identity nodes so that we don't
     * have a stateful processor inside a windowed bolt.
     */
    private void splitStatefulProcessor(ProcessorNode processorNode, TopologyBuilder topologyBuilder) {
        for (Node parent : StreamUtil.<Node>getParents(graph, processorNode)) {
            ProcessorNode identity =
                    new ProcessorNode(new MapProcessor<>(new IdentityFunction<>()),
                            UniqueIdGen.getInstance().getUniqueStreamId(),
                            parent.getOutputFields());
            addNode(parent, identity);
            graph.removeEdge(parent, processorNode);
            processorNode.removeParentStreams(parent);
            addNode(identity, processorNode);
            curGroup.add(identity);
        }
        processCurGroup(topologyBuilder);
    }

    private void mayBeAddTsField() {
        if (timestampFieldName != null) {
            for (StreamBolt streamBolt : streamBolts.keySet()) {
                streamBolt.setTimestampField(timestampFieldName);
            }
        }
    }

    private void updateNodeGroupingInfo(PartitionNode partitionNode) {
        if (partitionNode.getGroupingInfo() != null) {
            for (Node parent : parentNodes(partitionNode)) {
                for (String parentStream : partitionNode.getParentStreams(parent)) {
                    nodeGroupingInfo.put(parent, parentStream, partitionNode.getGroupingInfo());
                }
            }
        }
    }

    private void updateWindowInfo(WindowNode windowNode) {
        for (Node parent : parentNodes(windowNode)) {
            windowInfo.put(parent, windowNode);
        }
        String tsField = windowNode.getWindowParams().getTimestampField();
        if (tsField != null) {
            if (timestampFieldName != null && !tsField.equals(timestampFieldName)) {
                throw new IllegalArgumentException("Cannot set different timestamp field names");
            }
            timestampFieldName = tsField;
        }
    }

    Node parentNode(Node curNode) {
        Set<Node> parentNode = parentNodes(curNode);
        if (parentNode.size() > 1) {
            throw new IllegalArgumentException("Node " + curNode + " has more than one parent node.");
        }
        if (parentNode.isEmpty()) {
            throw new IllegalArgumentException("Node " + curNode + " has no parent.");
        }
        return parentNode.iterator().next();
    }

    private Set<Node> parentNodes(Node curNode) {
        Set<Node> nodes = new HashSet<>();
        for (Node parent : StreamUtil.<Node>getParents(graph, curNode)) {
            if (parent instanceof ProcessorNode || parent instanceof SpoutNode) {
                nodes.add(parent);
            } else {
                nodes.addAll(parentNodes(parent));
            }
        }
        return nodes;
    }

    private Collection<List<ProcessorNode>> parallelismGroups(List<ProcessorNode> processorNodes) {
        Collection<List<ProcessorNode>> res = processorNodes.stream().collect(Collectors.groupingBy(Node::getParallelism)).values();
        return res;
    }

    private void processCurGroup(TopologyBuilder topologyBuilder) {
        if (!curGroup.isEmpty()) {
            parallelismGroups(curGroup).forEach(g -> doProcessCurGroup(topologyBuilder, g));
            curGroup.clear();
        }
    }


    private void doProcessCurGroup(TopologyBuilder topologyBuilder, List<ProcessorNode> group) {
        String boltId = UniqueIdGen.getInstance().getUniqueBoltId();

        for (ProcessorNode processorNode : group) {
            processorNode.setComponentId(boltId);
            processorNode.setWindowedParentStreams(getWindowedParentStreams(processorNode));
        }
        final Set<ProcessorNode> initialProcessors = initialProcessors(group);

        Set<Window<?, ?>> windowParams = getWindowParams(initialProcessors);
        if (windowParams.isEmpty()) {
            if (hasStatefulProcessor(group)) {
                addStatefulBolt(topologyBuilder, boltId, initialProcessors, group);
            } else {
                addBolt(topologyBuilder, boltId, initialProcessors, group);
            }

        } else if (windowParams.size() == 1) {
            addWindowedBolt(topologyBuilder, boltId, initialProcessors, windowParams.iterator().next(), group);

        } else {
            throw new IllegalStateException("More than one window config for current group " + group);
        }

    }

    private boolean hasStatefulProcessor(List<ProcessorNode> processorNodes) {
        for (ProcessorNode node : processorNodes) {
            if (node.getProcessor() instanceof StatefulProcessor) {
                return true;
            }
        }
        return false;
    }

    // todo - verify
    private int getParallelism(List<ProcessorNode> group) {
        Set<Integer> parallelisms = group.stream().map(Node::getParallelism).collect(Collectors.toSet());

        if (parallelisms.size() > 1) {
            int maxParallelism = parallelisms.stream().max(Integer::compare).orElse(1);
            group.forEach(node -> node.setParallelism(maxParallelism));
            return maxParallelism;
        }

        return parallelisms.isEmpty() ? 1 : parallelisms.iterator().next();
    }

    private Set<Window<?, ?>> getWindowParams(Set<ProcessorNode> initialProcessors) {
        Set<WindowNode> windowNodes = new HashSet<>();
        Set<Node> parents;
        for (ProcessorNode processorNode : initialProcessors) {
            parents = parentNodes(processorNode);
            for (Node node : parents) {
                if (windowInfo.containsKey(node)) {
                    windowNodes.add(windowInfo.get(node));
                }
            }
        }

        return windowNodes.stream().map(WindowNode::getWindowParams).collect(Collectors.toSet());
    }

    private void addSpout(TopologyBuilder topologyBuilder, SpoutNode spout) {
        // adding id and group

        logBolts.add(Triple.of((String)spout.getDescription().get("id"), spout.getComponentId(), String.valueOf(spout.getParallelism())));
        topologyBuilder.setSpout(spout.getComponentId(), spout.getSpout(), spout.getParallelism());
    }

    private void addSink(TopologyBuilder topologyBuilder, SinkNode sinkNode) {
        IComponent bolt = sinkNode.getBolt();
        BoltDeclarer boltDeclarer;
        logBolts.add(Triple.of((String) sinkNode.getDescription().get("id"), sinkNode.getComponentId(), String.valueOf(sinkNode.getParallelism())));

        if (bolt instanceof IRichBolt) {
            boltDeclarer = topologyBuilder.setBolt(sinkNode.getComponentId(), (IRichBolt) bolt, sinkNode.getParallelism());
        } else if (bolt instanceof IBasicBolt) {
            boltDeclarer = topologyBuilder.setBolt(sinkNode.getComponentId(), (IBasicBolt) bolt, sinkNode.getParallelism());
        } else {
            throw new IllegalArgumentException("Expect IRichBolt or IBasicBolt in addBolt");
        }
        for (Node parent : parentNodes(sinkNode)) {
            for (String stream : sinkNode.getParentStreams(parent)) {
                declareGrouping(boltDeclarer, parent, stream, nodeGroupingInfo.get(parent, stream));
            }
        }
    }

    // todo - log grouping
    private void logBoltGrouping(Iterable<ProcessorNode> nodes) {
        Set<Pair<String,String>> nodeIdsParallelism = new HashSet<>();
        Set<String> componentIds = new HashSet<>();
        for (ProcessorNode node : nodes) {
            BaseProcessor<?> p = (BaseProcessor<?>) node.getProcessor();
            // verifying if parallelism is set
            HashMap<String, Object> desc = p.getDescription();
            if (p.getDescription() != null && p.getDescription().get("id") != null) {
                nodeIdsParallelism.add(Pair.of((String) p.getDescription().get("id"), String.valueOf(node.getParallelism())));
            }
            if (node.componentId != null) {
                componentIds.add(node.componentId);
            }
        }
        if (componentIds.size() != 1) {
            throw new RuntimeException("Multiple or no component IDs found within a component: " + componentIds);
        }
        String componentId = componentIds.iterator().next();

        if (nodeIdsParallelism.size() == 0) {
            throw new RuntimeException("No Node IDs found within all processor nodes of component: " + componentId);
        }

        for (Pair<String,String>  entry: nodeIdsParallelism) {
            logBolts.add(Triple.of(entry.getFirst(), componentId, entry.getSecond()));
        }

    }

    private StreamBolt addBolt(TopologyBuilder topologyBuilder,
                               String boltId,
                               Set<ProcessorNode> initialProcessors,
                               List<ProcessorNode> group) {
        logBoltGrouping(group);
        ProcessorBolt bolt = new ProcessorBolt(boltId, graph, group);
        BoltDeclarer boltDeclarer = topologyBuilder.setBolt(boltId, bolt, getParallelism(group));
        bolt.setStreamToInitialProcessors(wireBolt(group, boltDeclarer, initialProcessors));
        streamBolts.put(bolt, boltDeclarer);
        return bolt;
    }

    private StreamBolt addStatefulBolt(TopologyBuilder topologyBuilder,
                                       String boltId,
                                       Set<ProcessorNode> initialProcessors,
                                       List<ProcessorNode> group) {
        logBoltGrouping(group);
        StateQueryProcessor<?, ?> stateQueryProcessor = getStateQueryProcessor(group);
        StatefulProcessorBolt<?, ?> bolt;
        if (stateQueryProcessor == null) {
            bolt = new StatefulProcessorBolt<>(boltId, graph, group);
            BoltDeclarer boltDeclarer = topologyBuilder.setBolt(boltId, bolt, getParallelism(group));
            bolt.setStreamToInitialProcessors(wireBolt(group, boltDeclarer, initialProcessors));
            streamBolts.put(bolt, boltDeclarer);
        } else {
            // state query is added to the existing stateful bolt
            ProcessorNode updateStateNode = stateQueryProcessor.getStreamState().getUpdateStateNode();
            bolt = findStatefulProcessorBolt(updateStateNode);
            for (ProcessorNode node : group) {
                node.setComponentId(bolt.getId());
            }
            bolt.addNodes(group);
            bolt.addStreamToInitialProcessors(wireBolt(bolt.getNodes(), streamBolts.get(bolt), initialProcessors));
        }
        return bolt;
    }

    private StateQueryProcessor<?, ?> getStateQueryProcessor(List<ProcessorNode> group) {
        for (ProcessorNode node : group) {
            if (node.getProcessor() instanceof StateQueryProcessor) {
                return (StateQueryProcessor<?, ?>) node.getProcessor();
            }
        }
        return null;
    }

    private StreamBolt addWindowedBolt(TopologyBuilder topologyBuilder,
                                       String boltId,
                                       Set<ProcessorNode> initialProcessors,
                                       Window<?, ?> windowParam,
                                       List<ProcessorNode> group) {
        logBoltGrouping(group);
        WindowedProcessorBolt bolt = new WindowedProcessorBolt(boltId, graph, group, windowParam);
        BoltDeclarer boltDeclarer = topologyBuilder.setBolt(boltId, bolt, getParallelism(group));
        bolt.setStreamToInitialProcessors(wireBolt(group, boltDeclarer, initialProcessors));
        streamBolts.put(bolt, boltDeclarer);
        return bolt;
    }

    private StatefulProcessorBolt<?, ?> findStatefulProcessorBolt(ProcessorNode updateStateNode) {
        for (StreamBolt bolt : streamBolts.keySet()) {
            if (bolt instanceof StatefulProcessorBolt) {
                StatefulProcessorBolt<?, ?> statefulProcessorBolt = (StatefulProcessorBolt) bolt;
                if (statefulProcessorBolt.getNodes().contains(updateStateNode)) {
                    return statefulProcessorBolt;
                }
            }
        }
        throw new IllegalArgumentException("Could not find Stateful bolt for node " + updateStateNode);
    }

    private Set<String> getWindowedParentStreams(ProcessorNode processorNode) {
        Set<String> res = new HashSet<>();
        for (Node parent : parentNodes(processorNode)) {
            if (parent instanceof ProcessorNode && parent.isWindowed()) {
                res.addAll(parent.getOutputStreams());
            }
        }
        return res;
    }

    private Multimap<String, ProcessorNode> wireBolt(List<ProcessorNode> group,
                                                     BoltDeclarer boltDeclarer,
                                                     Set<ProcessorNode> initialProcessors) {
        LOG.debug("Wiring bolt with boltDeclarer {}, group {}, initialProcessors {}, nodeGroupingInfo {}",
                boltDeclarer, group, initialProcessors, nodeGroupingInfo);
        Multimap<String, ProcessorNode> streamToInitialProcessor = ArrayListMultimap.create();
        Set<ProcessorNode> curSet = new HashSet<>(group);
        for (ProcessorNode curNode : initialProcessors) {
            for (Node parent : parentNodes(curNode)) {
                if (curSet.contains(parent)) {
                    LOG.debug("Parent {} of curNode {} is in group {}", parent, curNode, group);
                } else {
                    for (String stream : curNode.getParentStreams(parent)) {
                        declareGrouping(boltDeclarer, parent, stream, nodeGroupingInfo.get(parent, stream));
                        // put global stream id for spouts
                        if (parent.getComponentId().startsWith("spout")) {
                            stream = parent.getComponentId() + stream;
                        } else {
                            // subscribe to parent's punctuation stream
                            String punctuationStream = StreamUtil.getPunctuationStream(stream);
                            declareGrouping(boltDeclarer, parent, punctuationStream, GroupingInfo.all());
                        }
                        streamToInitialProcessor.put(stream, curNode);
                    }
                }
            }
        }
        return streamToInitialProcessor;
    }

    private void declareGrouping(BoltDeclarer boltDeclarer, Node parent, String streamId, GroupingInfo grouping) {
        if (grouping == null) {
            boltDeclarer.shuffleGrouping(parent.getComponentId(), streamId);
        } else {
            grouping.declareGrouping(boltDeclarer, parent.getComponentId(), streamId, grouping.getFields());
        }
    }

    private Set<ProcessorNode> initialProcessors(List<ProcessorNode> group) {
        Set<ProcessorNode> nodes = new HashSet<>();
        Set<ProcessorNode> curSet = new HashSet<>(group);
        for (ProcessorNode node : group) {
            for (Node parent : parentNodes(node)) {
                if (!(parent instanceof ProcessorNode) || !curSet.contains(parent)) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }

    public DefaultDirectedGraph<Node, Edge> getGraph() {
        return graph;
    }
}
