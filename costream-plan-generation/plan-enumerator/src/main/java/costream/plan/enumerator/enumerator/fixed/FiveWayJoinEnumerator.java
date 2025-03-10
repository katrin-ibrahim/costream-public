package costream.plan.enumerator.enumerator.fixed;

import costream.plan.enumerator.CombinationSpace;
import costream.plan.enumerator.enumerator.AbstractEnumerator;
import costream.plan.executor.main.Constants;
import costream.plan.executor.operators.WindowedJoinOperator;
import costream.plan.executor.operators.map.MapPairOperator;
import costream.plan.executor.operators.window.WindowOperator;
import costream.plan.executor.utils.Triple;
import org.apache.storm.streams.Pair;
import org.apache.storm.streams.windowing.TumblingWindows;
import org.apache.storm.topology.base.BaseWindowedBolt;

import java.util.HashMap;

public class FiveWayJoinEnumerator extends AbstractEnumerator {

    public FiveWayJoinEnumerator(CombinationSpace space) {
        super(space);
        this.mode = Constants.QueryType.FIXED.FIVE_WAY_JOIN;
    }

    @Override
    public void buildSingleQuery(int eventRate, int windowLength, int tupleWidth) {
        // first stream
        createFixedStream(new Triple<>(tupleWidth, tupleWidth, tupleWidth), eventRate);
        String head0 = curGraphHead.getId();

        // second stream
        createFixedStream(new Triple<>(tupleWidth, tupleWidth, tupleWidth), eventRate);
        String head1 = curGraphHead.getId();

        // third stream
        createFixedStream(new Triple<>(tupleWidth, tupleWidth, tupleWidth), eventRate);
        String head2 = curGraphHead.getId();

        // fourth stream
        createFixedStream(new Triple<>(tupleWidth, tupleWidth, tupleWidth), eventRate);
        String head3 = curGraphHead.getId();

        // fifth stream
        getLinearStreamWithFilter(eventRate, tupleWidth);
        String head4 = curGraphHead.getId();

        // map
        MapPairOperator mapToPairOperator = new MapPairOperator(
                input -> Pair.of(input.getTupleValue(Integer.class, 0), input), Integer.class);

        // window
        BaseWindowedBolt.Duration window = BaseWindowedBolt.Duration.of(windowLength * 1000);
        WindowOperator windowOperator = new WindowOperator("tumblingWindow", "duration", TumblingWindows.of(window), window.value, null);

        // join
        HashMap<String, Object> windowedJoinDescription = new HashMap<>();
        windowedJoinDescription.put(Constants.Features.joinKeyClass.name(), mapToPairOperator.getDescription().get(Constants.Features.mapKey.name()));
        windowedJoinDescription.putAll(windowOperator.getDescription());

        // create join operator
        WindowedJoinOperator windowedJoin1 = new WindowedJoinOperator(getOperatorIndex(true), windowedJoinDescription);
        WindowedJoinOperator windowedJoin2 = new WindowedJoinOperator(getOperatorIndex(true), windowedJoinDescription);
        WindowedJoinOperator windowedJoin3 = new WindowedJoinOperator(getOperatorIndex(true), windowedJoinDescription);
        WindowedJoinOperator windowedJoin4 = new WindowedJoinOperator(getOperatorIndex(true), windowedJoinDescription);

        // build graph
        addQueryVertex(windowedJoin1);
        currentGraph.addEdge(nextEdgeIndex(), currentGraph.getNode(head0), currentGraph.getNode(windowedJoin1.getId()), true);
        currentGraph.addEdge(nextEdgeIndex(), currentGraph.getNode(head1), currentGraph.getNode(windowedJoin1.getId()), true);
        curGraphHead = windowedJoin1;

        addQueryVertex(windowedJoin2);
        currentGraph.addEdge(nextEdgeIndex(), currentGraph.getNode(windowedJoin1.getId()), currentGraph.getNode(windowedJoin2.getId()), true);
        currentGraph.addEdge(nextEdgeIndex(), currentGraph.getNode(head2), currentGraph.getNode(windowedJoin2.getId()), true);
        curGraphHead = windowedJoin2;

        addQueryVertex(windowedJoin3);
        currentGraph.addEdge(nextEdgeIndex(), currentGraph.getNode(windowedJoin2.getId()), currentGraph.getNode(windowedJoin3.getId()), true);
        currentGraph.addEdge(nextEdgeIndex(), currentGraph.getNode(head3), currentGraph.getNode(windowedJoin3.getId()), true);
        curGraphHead = windowedJoin3;

        addQueryVertex(windowedJoin4);
        currentGraph.addEdge(nextEdgeIndex(), currentGraph.getNode(windowedJoin3.getId()), currentGraph.getNode(windowedJoin4.getId()), true);
        currentGraph.addEdge(nextEdgeIndex(), currentGraph.getNode(head4), currentGraph.getNode(windowedJoin4.getId()), true);
        curGraphHead = windowedJoin4;
    }
}