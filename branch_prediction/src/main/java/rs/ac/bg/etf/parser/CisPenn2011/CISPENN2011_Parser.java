package rs.ac.bg.etf.parser.CisPenn2011;

import rs.ac.bg.etf.parser.Parser;
import rs.ac.bg.etf.predictor.Instruction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//https://www.cis.upenn.edu/~milom/cis501-Fall12/traces/trace-format.html
public class CISPENN2011_Parser implements Parser {

	private int numOfInstruction;
	private int numOfBr;
	private int numOfCndBr;
	private int numOfBackwardCndBr;
	private int numOfTaken;

	public int getNumOfTaken() {
		return numOfTaken;
	}

	public int getNumOfBackwardCndBr() {
		return numOfBackwardCndBr;
	}

	public int getNumOfInstruction() {
		return numOfInstruction;
	}

	public int getNumOfBr() {
		return numOfBr;
	}

	public int getNumOfCndBr() {
		return numOfCndBr;
	}

	private BufferedReader r;

	public CISPENN2011_Parser(String pathToTrace) {
		try {
			r = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(new FileInputStream(pathToTrace)), "US-ASCII"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Instruction getNext() {
		try {
			String line = r.readLine();
			if (line == null)
				return null;
			String[] tokens = line.split("\t");

			long pc = Long.parseLong(tokens[0].substring(2), 16);
			boolean outcome = Integer.parseInt(tokens[1])==1;
			boolean cond = Integer.parseInt(tokens[2])==1;

			String jmp_adr = tokens[7].replace(" ", "").substring(2, 14);

			long targetAddressTakenBranch = Long.parseLong(jmp_adr, 16);
			boolean isBackward = pc > targetAddressTakenBranch;

			return new CISPENN2011_Instruction(pc, outcome, true, cond, isBackward);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public void fillStats() {
		long pc;

		boolean cond;
		boolean outcome;
		long targetAddressTakenBranch;
		boolean isBackward;
		
		try {
			String line = " ";
			while ((line = r.readLine()) != null) {

				if(line == ""){
					continue;
				}

				String[] tokens = line.split("\t");

				pc = Long.parseLong(tokens[0].substring(2), 16);
				outcome = Integer.parseInt(tokens[1])==1;
				cond = Integer.parseInt(tokens[2])==1;

				String jmp_adr = tokens[7].replace(" ", "").substring(2, 14);

				targetAddressTakenBranch = Long.parseLong(jmp_adr, 16);
				isBackward = pc > targetAddressTakenBranch;

				if(targetAddressTakenBranch != 0)
					numOfBr++;
				if(cond)
					numOfCndBr++;
				if(cond && outcome)
					numOfTaken++;
				if(targetAddressTakenBranch != 0 && isBackward)
					numOfBackwardCndBr++;
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
