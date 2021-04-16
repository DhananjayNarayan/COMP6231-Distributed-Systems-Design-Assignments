package WordCountAssignment;

import java.util.StringTokenizer;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;




public class WordCount {

	//The Mapper Class. Tokenization used. 
    public static class Mapping extends
            Mapper<Object, Text, Text, IntWritable> {

        private final IntWritable initialOne = new IntWritable(1); //value
        private final Text word = new Text();

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            StringTokenizer tokenItr = new StringTokenizer(value.toString());
            while (tokenItr.hasMoreTokens()) {
                word.set(tokenItr.nextToken().toLowerCase());
                
                //If the word is not in the list of StopWords, we map the word. 
                if(!StopWordList.listOfStopWords.contains(word.toString())) {
                    context.write(word, initialOne); // Initializing the word key with initial value of one. 
                }
            }
        }
    }
    
    // The class for Reducer
    public static class ReduceStage extends
            Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable count = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            int sum = 0;

            for (IntWritable val : values) {
                sum += val.get();
            }

            count.set(sum);
            context.write(key, count); // Writing to the file- the word and its count
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(Mapping.class);
        job.setCombinerClass(ReduceStage.class);
        job.setReducerClass(ReduceStage.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
} 

