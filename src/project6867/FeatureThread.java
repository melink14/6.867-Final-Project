package project6867;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.distance.PearsonCorrelationCoefficient;
import net.sf.javaml.featureselection.subset.GreedyBackwardElimination;
import net.sf.javaml.featureselection.subset.GreedyForwardSelection;

public class FeatureThread implements Runnable {

	private Dataset data;
	private double n;
	private int featureSize;
	private Direction direction;
	private DateFormat dateFormat;
	enum Direction{FORWARD, BACKWARD};
	
	public FeatureThread(Dataset d, int featureSize, double retention, Direction dir){
		this.data = d.copy();
		this.n = retention;
		this.featureSize = featureSize;
		this.direction = dir;
		dateFormat = new SimpleDateFormat("HH:mm:ss");
	}
	
	@Override
	public void run() {
		System.out.println(this.toString() + ":Started at " + dateFormat.format(new Date()));
		Set<Integer> selected;
		if(this.direction == Direction.FORWARD){
			GreedyForwardSelection selector = new GreedyForwardSelection((int)(this.featureSize*this.n), new PearsonCorrelationCoefficient());
			selector.build(data);
			selected = selector.selectedAttributes();
		}else{
			GreedyBackwardElimination selector = new GreedyBackwardElimination((int)(this.featureSize*this.n), new PearsonCorrelationCoefficient());
			selector.build(data);
			selected = selector.selectedAttributes();
		}
		System.out.println(this.toString() + ":Writing output at " + dateFormat.format(new Date()));
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(this.toString() + "_" + data.size() + "mixed.data"));
			for(Integer i : selected){
				out.write(i.toString() + ", ");
			}
			out.flush();
			out.close();
			System.out.println(this.toString() + ":Finished successfully at " + dateFormat.format(new Date()));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public String toString(){
		return (this.direction == Direction.FORWARD ? "forward" : "backward") + "@" + this.n;
	}
}
