package threeD;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.stage.Stage;

public class StreamExplorer  extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
//	  List<Integer> model = new ArrayList<Integer>();  //FXCollections.observableIntegerArray();
//System.out.println(
// model.stream().parallel()
// 	.map(i -> 100 + 2 * i)
// 	.sum()
//);
 List<String> myList =
		    Arrays.asList(
		    		"a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", 
		    		"b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", 
		    		"c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8"
		    		);

		myList
		    .stream()
		    .filter(s -> s.startsWith("c"))
		    .map(String::toUpperCase)
		    .sorted()			// Comparator<String, String> cmp { (String a, String b) -> {  a.substring(1).compareTo(b.substring(1)); } } 
		    .forEach(System.out::println);

		
//		List<Integer> list = Arrays.asList(1,2,3,4,5,6,7);
		List<Integer> list = Arrays.asList(2,4,4,4,5,5,7,9);
		int sum = list.stream()
				.map(x -> x*x)
				.reduce((x,y) -> x + y)
				.get();
		System.out.println(sum);
		
		int sum2 = list.stream()
				.mapToInt(x -> x*x)
				.sum();  
		System.out.println(sum2);
		
		
		double mean = list.stream().mapToDouble(x -> x).sum() / list.size();
		System.out.println("Mean: " + mean);
		double variance = list.stream().mapToDouble(x -> ((mean-x) * (mean-x))).sum()  / list.size();
		System.out.println("variance: " + variance);
		double stdDev = Math.sqrt(variance);
		System.out.println("stdDev: " + stdDev);
		double cv = 100 * stdDev / mean;
		System.out.println("cd: " + cv + "%");

//		}
//	IntStream.range(0,96)
//	.mapToObj(i -> wellNotation96(i))			can I use functions outside this unit?
//	.map(i -> padTo3(i))
//	.forEach(System.out::println);
//		TODO be able to reference external statics in lambdas
    }
}
