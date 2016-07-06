package io.skymind.echidna.api.sequence;

import org.canova.api.io.data.IntWritable;
import org.canova.api.io.data.LongWritable;
import org.canova.api.writable.Writable;
import io.skymind.echidna.api.ReduceOp;
import io.skymind.echidna.api.Transform;
import io.skymind.echidna.api.reduce.Reducer;
import io.skymind.echidna.api.schema.Schema;
import io.skymind.echidna.api.schema.SequenceSchema;
import io.skymind.echidna.api.sequence.window.ReduceSequenceByWindowTransform;
import io.skymind.echidna.api.sequence.window.TimeWindowFunction;
import io.skymind.echidna.api.sequence.window.WindowFunction;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alex on 16/04/2016.
 */
public class TestReduceSequenceByWindowFunction {

    @Test
    public void testReduceSequenceByWindowFunction(){
        //Time windowing: 1 second (1000 milliseconds) window

        //Create some data.
        List<List<Writable>> sequence = new ArrayList<>();
        //First window:
        sequence.add(Arrays.asList((Writable)new LongWritable(1451606400000L), new IntWritable(0)));
        sequence.add(Arrays.asList((Writable)new LongWritable(1451606400000L + 100L), new IntWritable(1)));
        sequence.add(Arrays.asList((Writable)new LongWritable(1451606400000L + 200L), new IntWritable(2)));
        //Second window:
        sequence.add(Arrays.asList((Writable)new LongWritable(1451606400000L + 1000L), new IntWritable(3)));
        //Third window: empty
        //Fourth window:
        sequence.add(Arrays.asList((Writable)new LongWritable(1451606400000L + 3000L), new IntWritable(4)));
        sequence.add(Arrays.asList((Writable)new LongWritable(1451606400000L + 3100L), new IntWritable(5)));

        Schema schema = new SequenceSchema.Builder()
                .addColumnTime("timecolumn", DateTimeZone.UTC)
                .addColumnInteger("intcolumn")
                .build();

        WindowFunction wf = new TimeWindowFunction("timecolumn",1, TimeUnit.SECONDS);
        wf.setInputSchema(schema);


        //Now: reduce by summing...
        Reducer reducer = new Reducer.Builder(ReduceOp.Sum)
                .takeFirstColumns("timecolumn")
                .build();

        Transform transform = new ReduceSequenceByWindowTransform(reducer,wf);
        transform.setInputSchema(schema);

        List<List<Writable>> postApply = transform.mapSequence(sequence);
        assertEquals(4,postApply.size());


        List<Writable> exp0 = Arrays.asList((Writable)new LongWritable(1451606400000L),new LongWritable(0+1+2));
        assertEquals(exp0, postApply.get(0));

        List<Writable> exp1 = Arrays.asList((Writable)new LongWritable(1451606400000L + 1000L),new LongWritable(3));
        assertEquals(exp1, postApply.get(1));

        List<Writable> exp2 = Arrays.asList((Writable)new LongWritable(0L), new LongWritable(0));
        assertEquals(exp2, postApply.get(2));

        List<Writable> exp3 = Arrays.asList((Writable)new LongWritable(1451606400000L + 3000L),new LongWritable(9));
        assertEquals(exp3, postApply.get(3));
    }

}