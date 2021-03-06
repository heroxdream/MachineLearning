package ztest;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

/**
 * Created by hanxuan on 9/10/15.
 */
public class Test {

    public static void main(String[] args) throws Exception{
//        System.out.println("hello");
//        Clock c = Clock.systemUTC();
//        System.out.println();
//        List<String> name = Arrays.asList("baa", "1", "2s", "a");
//        Collections.sort(name, (String a, String b) -> b.length() > a.length() ? 1: -1);
//        System.out.println(name);


//        int[][] a = new int[2][3];
//        System.out.println(a.length);
//        System.out.println(a[0].length);



//        List<String> l = Arrays.asList("1", "2");

//      System.out.print(new String[]{"1", "2", "3"}[-1]);
//        String a = "a        b";
//        System.out.println(Arrays.toString(a.split("\\s+")));


//        System.out.println(Arrays.toString(IntStream.range(0, 10).toArray()));

        Logger log = LogManager.getLogger(Test.class);
//        ExecutorService service = Executors.newFixedThreadPool(1);
//        CountDownLatch countDownLatch = new CountDownLatch(10);
////        AtomicInteger counter = new AtomicInteger(0);
//        for (int i = 0; i < 10 ; i++) {
//            service.submit(
//                    () -> {
//                        try {
//                            log.info("sleep2");
//                            TimeUnit.SECONDS.sleep(2);
//                        } catch (Throwable t) {
//
//                        }
//                        countDownLatch.countDown();
//                    });
//        }
//
//        try {
//            countDownLatch.await();
//            Thread.sleep(1000);
//        }catch (Throwable t) {
//            System.out.println(t);
//        }
//        service.shutdown();
//
//
//
//        log.info("after shutdown");
//
//        log.info("still");
//
//        return;

//


//        double[][] m0 = new double[][] {
//                {1, 96,    26,    26,    55},
//                {1, 55,    82,    62,    92},
//                {1, 14,    25,    48,    29},
//                {1, 15,    93,    36,    76},
//                {1, 26,    35,    84,    76},
//                {1, 85,    20,    59,    39}
//
//        };

//        RealMatrix p = new Array2DRowRealMatrix(m0, false);
//
//        p = p.transpose().multiply(p);
//
//        RealMatrix pInverse = new LUDecomposition(p).getSolver().getInverse();
//
//        log.info(Arrays.deepToString(pInverse.getData()));


//        double[][] m1 = ;
//
//        log.info(m1);
//
//        m1[0][1] = 0;
//        log.info(Arrays.deepToString(m0));



//        double[] a = {0.16472572793853585, 0.16473288429423177, 0.16364715196495644, 0.16387661017209518, 0.1623304921994822, 0.006160342174793988, 0.0024250452754503546, 0.1614497432746957};
//        int[] idx = RandomUtils.getIndexes(a.length);
//        SortIntDoubleUtils.sort(idx, a);
//        System.out.println(Arrays.toString(a));
//        System.out.println(Arrays.toString(idx));


//        String file1 = "/Users/hanxuan/Dropbox/neu/fall15/machine learning/data/digits.DataPointSet.txt";
//        String file2 = "/Users/hanxuan/Dropbox/neu/fall15/machine learning/data/digits.y.txt";
//
//        List<String> DataPointSet = FileUtils.readLines(file1);
//        List<String> y = FileUtils.readLines(file2);
//
//        List<String> l = new ArrayList<>();
//
//        for (int i = 0; i < DataPointSet.size(); i++) {
//            double yi = Double.parseDouble(y.get(i));
//            if (yi == 10) yi = 0;
//            l.add(DataPointSet.get(i) + "\t" + yi);
//        }
//
//        String output = "/Users/hanxuan/Dropbox/neu/fall15/machine learning/data/digits.Xy.txt";
//        FileUtils.writeLines(output, l);

//        HashSet<Integer> set = new HashSet<>();
//        set.add(1);
//        set.add(2);

//        HashSet<Integer> set = new HashSet<>(Arrays.asList(1));
//        for (int e : set) System.out.println(e);


        double[] x1 = new double[]{0, 0, 0, 0, 0};
        double[] x2 = new double[]{6, 7, 8, 1, 2, 3, 4, 5};
        double[] x3 = new double[]{2, 4, 6, 8, 10};
        double[] x4 = new double[]{0, 0, 0, 0, 0};

//        Percentile percentile = new Percentile();
//        System.out.println(percentile.evaluate(x2, 100));
//        Arrays.stream(x2).summaryStatistics();
//        NormalDistribution[] nds = new NormalDistribution[3];
//        for (int i = 0; i < 3; i++) {
//            System.out.println(nds[i]);
//        }

//        TDoubleArrayList l = new TDoubleArrayList(x2);
//        l.shuffle(new Random());
//        System.out.println(Arrays.toString(l.toArray()));
//        IntPredicate posIndicator = i -> testSet.getLabel(i) - POS == 0;
//        long positiveCount = Arrays.stream(label).filter(posIndicator).summaryStatistics().getCount();

//        Arrays.setAll(x2, i -> x2[i] / 10);

//        log.info(Arrays.toString(x2));

//        double [][] cov = new double[57][57];
//        for (int i = 0; i < 57; i++) {
//            cov[i][i] = 1;
//        }
//
//        double[] mu = RandomUtils.randomSumOneArray(57);
//
//        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(mu, cov);
//
//        double[] x = RandomUtils.randomOneOneArray(57);
//
//        log.info("density {}", distribution.density(x));


//        Stack<Integer> stack = new Stack<>();
//        stack.push(0);
//        stack.push(1);
//        stack.push(2);
//        stack.add(3);
//        log.info(stack.get(0));
//
//        log.info(stack.peek());

//        System.out.println(Math.log(10) / Math.log(2));
//        String file = "";
//        String file = "/Users/hanxuan/Dropbox/neu/fall15/machine learning/data/letter-recognition.reformat.data";
//        BufferedReader reader = new BufferedReader(new FileReader(file), 1024 * 1024 * 64);
//        String line;
//        HashSet<String> set = new HashSet<>();
//        while ((line = reader.readLine()) != null){
//            String[] es = line.trim().split(",");
//            set.add(es[es.length - 1]);
//        }
//        reader.close();
//
//        String[] classes = set.toArray(new String[0]);
//        Arrays.sort(classes);
//
//        HashMap<String, Integer> map = new HashMap<>();
//        for (int i = 0; i < classes.length; i++) {
//            map.put(classes[i], i);
//        }
//
//        reader = new BufferedReader(new FileReader(file), 1024 * 1024 * 64);
//        String newFile = "/Users/hanxuan/Dropbox/neu/fall15/machine learning/data/letter-recognition.reformat3.data";
//        BufferedFileWriter writer = new BufferedFileWriter(newFile);
//        String line2;
//        while ((line2 = reader.readLine()) != null){
//            String[] es = line2.trim().split(",");
//            StringBuilder builder = new StringBuilder();
//            for (int i = 0; i < es.length - 1; i++) {
//                builder.append(es[i]);
//                builder.append(",");
//            }
//            int classCount = map.get(es[es.length - 1]);
//            writer.writeLine(builder.toString() + classCount);
//        }
//        reader.close();
//        writer.close();

    }
}
