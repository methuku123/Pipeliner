/*

NAME : Methuku Preetham
ROLL : 16CS01045

*/

import java.util.*;
import java.io.*;

public class Pipeliner{

	static class node{
		String in;
		int writeReg;
		HashSet<Integer> readReg;
		boolean isMemReq;
		boolean isImmInv;
	}

	static class dNode{
		ArrayList<String> tDep;
		ArrayList<Integer> tReg;
		int inNum;
		dNode(int inNum, ArrayList<String> tDep, ArrayList<Integer> tReg){
			this.tDep = tDep;
			this.inNum = inNum;
			this.tReg = tReg;
		}
	}

	public static int hltFlag = 0;

	public static void main(String[] args){

		Pipeliner cs = new Pipeliner();

		Scanner scn = new Scanner(System.in);
		File file = new File("test1.txt");
		
		ArrayList<String> ins = new ArrayList<String>();
		int noi = 0;

		try{
			Scanner sc = new Scanner(file);
			while(sc.hasNextLine()){
				String st = sc.nextLine();
				if(!st.equals("NOP") && !st.equals("HLT")){
					ins.add(st);
					++noi;
				}
				if(st.equals("HLT")){
					hltFlag = 1;
				}
			}
		}catch(IOException ex){
			System.out.println("------------------------------------------------------");
			System.out.println("\033[1;31mFile is not found\033[0m");
			System.out.println("------------------------------------------------------");
			System.exit(0);
		}

		node[] arr = new node[noi];

		if(hltFlag == 1)
			ins.add("HLT");

		noi = cs.parseInstructions(ins, arr, noi);

		LinkedList<dNode>[] depArr = new LinkedList[noi];
		for(int i = 0; i < noi; i++){
			depArr[i] = new LinkedList<dNode>();
		}

		cs.fillDependencies(depArr, arr, noi);

		cs.printDependencies(depArr, arr, noi);

		HashSet<Integer>[] hashArr = new HashSet[noi];
		int[] inDegrees = new int[noi];
		Arrays.fill(inDegrees, 0);

		cs.constructGraph(depArr, inDegrees, hashArr, noi);

		ArrayList<Integer> optList = new ArrayList<Integer>();

		cs.startTracking(hashArr, inDegrees, optList, noi);

		System.out.println("\n\033[1;31mQuestion(2) -->\033[0m\n");
		System.out.println("Reordering of instructions(For reducing NOPs)");
		System.out.println("------------------------------------------------------");
		for(int j : optList){
			System.out.println(j);
		}
		if(hltFlag == 1)
			System.out.println(noi);
		System.out.println("------------------------------------------------------");

		System.out.println("With NOPs");
		System.out.println("------------------------------------------------------");
		ArrayList<String> withNOPs = new ArrayList<String>();
		cs.fillWithNOPs(withNOPs, ins, optList, depArr, arr, noi);
		System.out.println("------------------------------------------------------");

		cs.memoryDelays(withNOPs, ins);

	}

	public static void memoryDelays(ArrayList<String> withNOPs, ArrayList<String> ins){
		int count = 0;
		ArrayList<Integer> memList = new ArrayList<Integer>();
		HashSet<Integer> hslist = new HashSet<Integer>();

		for(int i = 0; i < withNOPs.size(); i++){

			if((withNOPs.get(i).charAt(0) == 'L') || (withNOPs.get(i).charAt(0) == 'S' && withNOPs.get(i).charAt(1) == 'T')){
				
				if(i+1 < withNOPs.size() && (withNOPs.get(i+1).equals("NOP") || withNOPs.get(i+1).equals("HLT")));
				else if(i+1 < withNOPs.size()){
					count+=2;
					if(!hslist.contains(i+1)){
						memList.add(i+1);
						hslist.add(i+1);
					}
				}
				int diff = withNOPs.size() - i;
				if(diff >= 4){
					count+=4;
					if(!hslist.contains(i+2)){
						memList.add(i+2);
						hslist.add(i+2);
					}
					if(!hslist.contains(i+3)){
						memList.add(i+3);
						hslist.add(i+3);
					}
				}
				else if(diff == 3){
					count+=2;
					if(!hslist.contains(i+2)){
						memList.add(i+2);
						hslist.add(i+2);
					}
				}
			}
		}
		System.out.println("\n\033[1;31mQuestion(3) -->\033[0m\n");
		System.out.println("Wastage of clock cycles due to memory delays");
		System.out.println("------------------------------------------------------");
		System.out.print(count+" --> At lines (");
		for(int j = 0; j < memList.size(); j++){
			if(j < memList.size()-1)
				System.out.print(memList.get(j)+", ");
			else
				System.out.println(memList.get(j)+")");
		}
		System.out.println("------------------------------------------------------");
	}

	public static void fillWithNOPs(ArrayList<String> withNOPs, ArrayList<String> ins, ArrayList<Integer> optList, LinkedList<dNode>[] depArr, node[] arr, int noi){

		int m = 0;
		withNOPs.add(ins.get(optList.get(m++)));

		for(int i = 1; i < noi; i++){
			for(int j = i-1; j >= 0 && j >= i-3; j--){
				int diff = i-j;
				int m1 = m-diff;

				if(withNOPs.get(m1).equals("NOP"))
					break;

				int flag = 0;
				int be = optList.get(j);
				
				LinkedList<dNode> ll = depArr[be];
				Iterator<dNode> it = ll.listIterator();
				ArrayList<String> typeDep = new ArrayList<String>();

				while(it.hasNext()){
					dNode temp = (dNode)it.next();
					if(temp.inNum == optList.get(i)){
						typeDep = temp.tDep;
						break;
					}
				}

				if(!arr[be].in.equals("STORE")){
					for(String k : typeDep){
						if(k.equals("RAW")){
							flag = 1;
							break;
						}
					}
				}

				if(flag == 1){
					if(diff == 1){
						withNOPs.add("NOP");
						withNOPs.add("NOP");
						withNOPs.add("NOP");
						m+=3;
					}
					else if(diff == 2){
						withNOPs.add("NOP");
						withNOPs.add("NOP");
						m+=2;
					}
					else if(diff == 3){
						withNOPs.add("NOP");
						m+=1;
					}
					break;
				}

			}
			withNOPs.add(ins.get(optList.get(i)));
			m+=1;
		}

		if(hltFlag == 1)
			withNOPs.add("HLT");

		for(String h : withNOPs){
			System.out.println(h);
		}
	}

	public static void constructGraph(LinkedList<dNode>[] depArr, int[] inDegrees, HashSet<Integer>[] hashArr, int noi){

		for(int i = 0; i < noi; i++){
		
			HashSet<Integer> hste = new HashSet<Integer>();
			LinkedList<dNode> lte = depArr[i];
			Iterator it = lte.listIterator();

			while(it.hasNext()){
				dNode te = (dNode)it.next();
				hste.add(te.inNum);
				inDegrees[te.inNum]++;
			}

			hashArr[i] = hste;
		}

	}

	public static void startTracking(HashSet<Integer>[] hashArr, int[] inDegrees, ArrayList<Integer> optList, int noi){

		ArrayList<Integer> clist = new ArrayList<Integer>();
		HashSet<Integer> hlist = new HashSet<Integer>();
		HashSet<Integer> finishList = new HashSet<Integer>();

		int m = 0;
		int prev = -1;

		while(finishList.size() < noi){

			for(int i = 0; i < noi; i++){
				if(!hlist.contains(i) && !finishList.contains(i) && inDegrees[i] == 0){
					clist.add(i);
					hlist.add(i);
				}
			}
			
			Iterator it = clist.iterator();
			int min_ins = -1;

			Collections.sort(clist);
			Collections.shuffle(clist, new Random(5));

			for(int p : clist){
				if(m != 0 && !hashArr[prev].contains(p)){
					min_ins = p;
					break;
				}
			}

			if(min_ins == -1){
				min_ins = (int)Collections.min(clist);
			}
			m++;

			clist.remove(new Integer(min_ins));
			finishList.add(min_ins);
			hlist.remove(min_ins);
			optList.add(min_ins);
			prev = min_ins;

			Iterator it1 = hashArr[min_ins].iterator();

			while(it1.hasNext()){
				int te1 = (int)it1.next();
				inDegrees[te1]--;
			}
		}
	}

	public static void printDependencies(LinkedList<dNode>[] depArr, node[] arr, int noi){

		System.out.println("------------------------------------------------------");
		System.out.println("\n\033[1;31mQuestion(1) -->\033[0m\n");
		System.out.println("Dependencies(RAW, WAW, WAR)");
		System.out.println("------------------------------------------------------");

		for(int i = 0; i < noi; i++){

			Iterator it = depArr[i].listIterator();
			System.out.println("ins("+i+") : ");

			while(it.hasNext()){

				dNode it1 = (dNode)it.next();
				System.out.printf("         %2d-->",it1.inNum);

				ArrayList<String> hsDep = it1.tDep;
				ArrayList<Integer> hsReg = it1.tReg;

				for(int k = 0; k < hsDep.size(); k++){

					if(k==0)
						System.out.print(" (");
					else
						System.out.print("               (");

					System.out.println(hsDep.get(k)+"- R"+hsReg.get(k)+")");
				}

				System.out.println();
			}

			if(depArr[i].size() == 0){
				System.out.println("          NONE\n");
			}
		}

		System.out.println("------------------------------------------------------");
	}

	public static void fillDependencies(LinkedList<dNode>[] depArr, node[] arr, int noi){

		for(int i = 0; i < noi; i++){

			node temp1 = arr[i];
			int write1 = temp1.writeReg;
			HashSet<Integer> hsTemp1 = temp1.readReg;

			for(int j = i+1; j < noi; j++){

				node temp2 = arr[j];
				int write2 = temp2.writeReg;
				HashSet<Integer> hsTemp2 = temp2.readReg;

				ArrayList<String> hsDep = new ArrayList<String>();
				ArrayList<Integer> hsReg = new ArrayList<Integer>();

				int ct = 0;
				if(hsTemp2.contains(write1)){
					hsDep.add("RAW");
					hsReg.add(write1);
					ct++;
				}
				if(hsTemp1.contains(write2)){
					hsDep.add("WAR");
					hsReg.add(write2);
					ct++;
				}
				if(write1 == write2){
					hsDep.add("WAW");
					hsReg.add(write1);
					ct++;
				}
				if(ct != 0)
					depArr[i].add(new dNode(j, hsDep, hsReg));
			}
		}
	} 

	public static int parseInstructions(ArrayList<String> ins, node[] arr, int noi){
		for(int i = 0; i < noi; i++){
			String str = (String)ins.get(i);
			String[] strTemp;
			strTemp = str.split(" ");
			int lORs = 0;

			if(strTemp[0].equals("STORE")){
				node temp = new node();
				temp.in = strTemp[0];
				temp.writeReg = 0;
				temp.isImmInv = true;
				temp.isMemReq = true;
		
				StringBuilder sb = new StringBuilder(strTemp[1]);
				sb.deleteCharAt(strTemp[1].length()-1);
				strTemp[1] = sb.toString();
			
				HashSet<Integer> arrTemp = new HashSet<Integer>();

				arrTemp.add(Integer.parseInt(strTemp[1].substring(1)));

				int sz = strTemp[2].length();
				int index2 = 0;
				for(int l = 0; l < sz; l++){
					if(strTemp[2].charAt(l) == 'R')
						break;
					index2++;
				}
				index2++;

				arrTemp.add(Integer.parseInt(strTemp[2].substring(index2, sz-1)));

				temp.readReg = arrTemp;
				arr[i] = temp;
				lORs = 1;
			}
			else if(strTemp[0].equals("LOAD")){
				node temp = new node();
				temp.in = strTemp[0];

				StringBuilder sb = new StringBuilder(strTemp[1]);
				sb.deleteCharAt(strTemp[1].length()-1);
				strTemp[1] = sb.toString();

				temp.writeReg = Integer.parseInt(strTemp[1].substring(1));
				temp.isImmInv = true;
				temp.isMemReq = true;

				HashSet<Integer> arrTemp = new HashSet<Integer>();
				int sz = strTemp[2].length();
				int index2 = 0;
				for(int l = 0; l < strTemp[2].length(); l++){
					if(strTemp[2].charAt(l) == 'R')
						break;
					index2++;
				}
				index2++;

				arrTemp.add(Integer.parseInt(strTemp[2].substring(index2, sz-1)));
				temp.readReg = arrTemp;
				arr[i] = temp;
				lORs = 1;
			}

			int insSize = strTemp.length;
			int flag = 0;
			if(strTemp[insSize-1].charAt(0) == '#'){
				flag = 1;
			}

			if(flag == 1 && lORs == 0 && !strTemp[0].equals("HLT")){
				node temp = new node();
				temp.in = strTemp[0];
				temp.writeReg = Integer.parseInt(strTemp[1].substring(1));
				temp.isImmInv = true;
				temp.isMemReq = false;
				HashSet<Integer> arrTemp = new HashSet<Integer>();
				arrTemp.add(Integer.parseInt(strTemp[2].substring(1)));
				temp.readReg = arrTemp;
				arr[i] = temp;
			}
			else if(!strTemp[0].equals("MOV") && flag == 0 && lORs == 0 && !strTemp[0].equals("HLT")){
				node temp = new node();
				temp.in = strTemp[0];
				temp.writeReg = Integer.parseInt(strTemp[1].substring(1));
				temp.isMemReq = false;
				temp.isImmInv = false;
				HashSet<Integer> arrTemp = new HashSet<Integer>();
				arrTemp.add(Integer.parseInt(strTemp[2].substring(1)));
				arrTemp.add(Integer.parseInt(strTemp[3].substring(1)));
				temp.readReg = arrTemp;
				arr[i] = temp;
			}
			else if(strTemp[0].equals("MOV")){
				node temp = new node();
				temp.in = strTemp[0];
				temp.writeReg = Integer.parseInt(strTemp[1].substring(1));
				temp.isMemReq = false;
				temp.isImmInv = false;
				HashSet<Integer> arrTemp = new HashSet<Integer>();
				arrTemp.add(Integer.parseInt(strTemp[2].substring(1)));
				temp.readReg = arrTemp;
				arr[i] = temp;
			}
		}
		return noi;
	}
}
