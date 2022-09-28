package rs.ac.bg.etf;

import java.io.FileWriter;
import java.io.IOException;

import rs.ac.bg.etf.automaton.Automaton;
import rs.ac.bg.etf.parser.CisPenn2011.CISPENN2011_Parser;
import rs.ac.bg.etf.parser.Parser;
import rs.ac.bg.etf.predictor.Instruction;
import rs.ac.bg.etf.predictor.Predictor;
import rs.ac.bg.etf.predictor.tage.TAGE;
import rs.ac.bg.etf.predictor.twoLevel.TwoLevel;
import rs.ac.bg.etf.predictor.yags.YAGS;
import rs.ac.bg.etf.stats.Statistics;

public class Test {

	static String get_automaton_type(Automaton.AutomatonType type){
		switch (type) {
            case ONEBIT: return "ONEBIT";
            case TWOBITS_TYPE1: return "TWOBITS_TYPE1";
            case TWOBITS_TYPE2: return "TWOBITS_TYPE2";
            case TWOBITS_TYPE3: return "TWOBITS_TYPE3";
            case TWOBITS_TYPE4: return "TWOBITS_TYPE4";
        }
		return "";
	}

	public static void main(String[] args) {
		String pathToTrace = args.length>0?args[0]: "/home/veljk/AOR2/branch_prediction/traces/neofetch.trace.gz";
		FileWriter myWriter;
		try {
			myWriter = new FileWriter("statistika");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}


		int bhr_sizes[] = {4,10,20,22};
		int addres_sizes[] = {4,10,20,22};
		Automaton.AutomatonType automaton_types[] = {
			Automaton.AutomatonType.ONEBIT, 
			Automaton.AutomatonType.TWOBITS_TYPE1,
			Automaton.AutomatonType.TWOBITS_TYPE2,
			Automaton.AutomatonType.TWOBITS_TYPE3,
			Automaton.AutomatonType.TWOBITS_TYPE4
		};

		for(int bhr_size : bhr_sizes){
			for(int addres_size : addres_sizes){
				for(Automaton.AutomatonType automaton_type : automaton_types){
					Parser parcer = new CISPENN2011_Parser(pathToTrace);
					Statistics stats = new Statistics();	

					Predictor predictor = new YAGS(bhr_size, addres_size, addres_size ,automaton_type);

					Instruction ins;
					int all = 0;
					while ((ins = parcer.getNext()) != null) {
						all++;
						if(!ins.isBranch())
							continue;
						boolean prediction = predictor.predict(ins);
						if (prediction != ins.isTaken())
							stats.incNumOfMisses();
						else
							stats.incNumOfHits();
						stats.incNumOfCondBranches();

						predictor.update(ins);
					}
					try {
						String write_str = stats.getNumOfHits() + "\t" + stats.getNumOfMisses() + "\t" + stats.getNumOfCondBranches() + "\t" + ((double) stats.getNumOfHits() / stats.getNumOfCondBranches() * 100) + "\t" + get_automaton_type(automaton_type) + "\t" + bhr_size + "\t" + addres_size + "\n";
						myWriter.write(write_str);
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		try {
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
