import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Planner {
	static Vector state;
	static Vector operators;
	static Random rand;
	static Vector plan;
	static String hold;
	static HashMap<String, ArrayList<String>> colors = new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> blockform = new HashMap<String, ArrayList<String>>();
	static ArrayList<String> doplan = new ArrayList<String>();
	static Hashtable re = new Hashtable();
	static ArrayList<String> name = new ArrayList<String>();
	static ArrayList<ArrayList<String>> namelist = new ArrayList<ArrayList<String>>();
	static int mTimetag = 0;
	static ConflictResolution cr;
	public static void main(String argv[]) {
		(new Planner()).start2("state.txt","test.txt");
		//initGoalListfile("test.txt");
	}

	Planner() {
		rand = new Random();
	}

	public static Vector plandoing(String plan1, Vector now) {
		Vector re = new Vector();
		Pattern pat1 = Pattern.compile("Place (.+) on (.+)");
		Matcher mat1 = pat1.matcher(plan1);
		Pattern pat2 = Pattern.compile("remove (.+) from on top (.+)");
		Matcher mat2 = pat2.matcher(plan1);
		Pattern pat3 = Pattern.compile("pick up (.+) from the table");
		Matcher mat3 = pat3.matcher(plan1);
		Pattern pat4 = Pattern.compile("put (.+) down on the table");
		Matcher mat4 = pat4.matcher(plan1);
		if (mat1.find()) {//Place x on y
			String x = mat1.group(1);
			String y = mat1.group(2);
			String check = "clear " + y;
			String check1 = "holding " + x;
			for (int j = 0; j < now.size(); j++) {//delete
				String a = (String) now.get(j);
				boolean flag = true;
				if (a.equals(check)) {
					flag = false;
				}
				if (a.equals(check1)) {
					flag = false;
				}
				if (flag) {
					re.addElement(a);
				}
			}
			hold = "";
			for (int i = 0; i < namelist.size(); i++) {
				ArrayList<String> memo = namelist.get(i);
				if (memo.contains(y)) {
					memo.add(x);
				}
				namelist.remove(i);
				namelist.add(i, memo);
			}//add
			re.addElement(x + " on " + y);
			re.addElement("clear " + x);
			re.addElement("handEmpty");
		} else if (mat2.find()) {//remove x from on top y
			String x = mat2.group(1);
			String y = mat2.group(2);
			String check = x + " on " + y;
			String check1 = "clear " + x;
			String check2 = "handEmpty";
			for (int j = 0; j < now.size(); j++) {//delete
				String a = (String) now.get(j);
				boolean flag = true;
				if (a.equals(check)) {
					flag = false;
				}
				if (a.equals(check1)) {
					flag = false;
				}
				if (a.equals(check2)) {
					flag = false;
				}
				if (flag) {
					re.addElement(a);
				}
			}
			hold = y;
			for (int i = 0; i < namelist.size(); i++) {
				ArrayList<String> memo = namelist.get(i);
				if (memo.contains(y)) {
					for (int j = 0; j < memo.size(); j++) {
						String s = memo.get(j);
						if (s.equals(x)) {
							memo.remove(j);
						}
					}
				}
				namelist.remove(i);
				namelist.add(i, memo);
			}//add
			re.addElement("clear " + y);
			re.addElement("holding " + x);
		} else if (mat3.find()) {//pick up x from the table
			String x = mat3.group(1);
			String check = "ontable " + x;
			String check1 = "clear " + x;
			String check2 = "handEmpty";
			for (int j = 0; j < now.size(); j++) {//delete
				String a = (String) now.get(j);
				boolean flag = true;
				if (a.equals(check)) {
					flag = false;
				}
				if (a.equals(check1)) {
					flag = false;
				}
				if (a.equals(check2)) {
					flag = false;
				}
				if (flag) {
					re.addElement(a);
				}
			}
			hold = x;
			for (int i = 0; i < namelist.size(); i++) {
				ArrayList<String> memo = namelist.get(i);
				if (memo.contains(x)) {
					memo.remove(0);
				}
				namelist.remove(i);
				namelist.add(i, memo);
			}//add
			re.addElement("holding " + x);
		} else if (mat4.find()) {//put x down on the table
			String x = mat4.group(1);
			String check = "holding " + x;
			for (int j = 0; j < now.size(); j++) {//delete
				String a = (String) now.get(j);
				boolean flag = true;
				if (a.equals(check)) {
					flag = false;
				}
				if (flag) {
					re.addElement(a);
				}
			}
			hold = "";
			for (int i = 0; i < namelist.size(); i++) {
				ArrayList<String> memo = namelist.get(i);
				if (memo.size() == 0) {
					memo.add(x);
					namelist.remove(i);
					namelist.add(i, memo);
					break;
				}
			}//add
			re.addElement("ontable " + x);
			re.addElement("clear " + x);
			re.addElement("handEmpty");
		}
		state.clear();
		for (int j = 0; j < re.size(); j++) {
			state.addElement(re.get(j));
		}
		return re;
	}

	public static ArrayList getblock(Vector initialState) {
		name.clear();
		hold = "";
		namelist.clear();
		for (int i = 0; i < initialState.size(); i++) {
			String state = (String) initialState.elementAt(i);
			Pattern pat1 = Pattern.compile("ontable (.+)");
			Matcher mat1 = pat1.matcher(state);
			Pattern pat2 = Pattern.compile("holding (.+)");
			Matcher mat2 = pat2.matcher(state);
			if (mat1.find()) {
				String name1 = mat1.group(1);
				if (!name.contains(name1)) {
					name.add(name1);
					ArrayList<String> n = new ArrayList<String>();
					n.add(name1);
					namelist.add(n);

				}
			} else if (mat2.find()) {
				String name1 = mat2.group(1);
				hold = name1;
				if (!name.contains(name1)) {
					name.add(name1);
				}
			}
		}

		boolean check = true;
		while (check) {
			check = false;
			for (int i = 0; i < initialState.size(); i++) {
				String state = (String) initialState.elementAt(i);
				Pattern pat1 = Pattern.compile("(.+) on (.+)");
				Matcher mat1 = pat1.matcher(state);
				if (mat1.find()) {
					String name1 = mat1.group(1);
					String name2 = mat1.group(2);
					if (!name.contains(name1)) {
						for (int j = 0; j < namelist.size(); j++) {
							ArrayList<String> a = namelist.get(j);
							if (a.contains(name2) && !a.contains(name1)) {
								namelist.remove(j);
								check = true;
								name.add(name1);
								a.add(name1);
								namelist.add(j, a);
							}
						}
					}
				}
			}
		}
		for (int i = namelist.size(); i < name.size(); i++) {
			ArrayList<String> a = new ArrayList<String>();
			namelist.add(a);
		}
		return namelist;

	}

	public static void start2(String infile, String goalfile) {
		rand = new Random();
		initOperators();
		Vector goalList = initGoalListfile(goalfile);
		Vector initialState = initInitialStatefile(infile);
		cr = new ConflictResolution(operators);
		Hashtable theBinding = new Hashtable();
		plan = new Vector();
		planning(goalList, initialState, theBinding);

		System.out.println("***** This is a plan! *****");
		// コピー
		re.clear();
		for (Enumeration e = theBinding.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String value = (String) theBinding.get(key);
			re.put(key, value);
		}
		doplan.clear();
		for (int i = 0; i < plan.size(); i++) {
			Operator op = (Operator) plan.elementAt(i);
			System.out.println((op.instantiate(theBinding)).name);
			doplan.add((op.instantiate(theBinding)).name);
		}
	}

	public static void restart(String goalfile, Vector list) {
		rand = new Random();
		initOperators();
		Vector goalList = initGoalListfile(goalfile);
		Vector initialState = list;
		re.clear();
		cr = new ConflictResolution(operators);
		Hashtable theBinding = new Hashtable();
		plan = new Vector();
		planning(goalList, initialState, theBinding);
		System.out.println("***** This is a plan! *****");
		// コピー
		re.clear();
		for (Enumeration e = theBinding.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String value = (String) theBinding.get(key);
			re.put(key, value);
		}
		doplan.clear();
		for (int i = 0; i < plan.size(); i++) {
			Operator op = (Operator) plan.elementAt(i);
			System.out.println((op.instantiate(theBinding)).name);
			doplan.add((op.instantiate(theBinding)).name);
		}

	}

	public static boolean checkOrder(Vector nowState, String op) {
		Pattern pat1 = Pattern.compile("Place (.+) on (.+)");
		Matcher mat1 = pat1.matcher(op);
		Pattern pat2 = Pattern.compile("remove (.+) from on top (.+)");
		Matcher mat2 = pat2.matcher(op);
		Pattern pat3 = Pattern.compile("pick up (.+) from the table");
		Matcher mat3 = pat3.matcher(op);
		Pattern pat4 = Pattern.compile("put (.+) down on the table");
		Matcher mat4 = pat4.matcher(op);
		if (mat1.find()) {
			String x = mat1.group(1);
			String y = mat1.group(2);
			String check1 = "clear " + y;
			String check2 = "holding " + x;
			boolean flag1 = false;
			boolean flag2 = false;
			for (int j = 0; j < nowState.size(); j++) {
				String a = (String) nowState.get(j);
				if (a.equals(check1)) {
					flag1 = true;
				}
				if (a.equals(check2)) {
					flag2 = true;
				}
			}
			return flag1 && flag2;
		} else if (mat2.find()) {
			String x = mat2.group(1);
			String y = mat2.group(2);
			String check1 = x + " on " + y;
			String check2 = "clear " + x;
			String check3 = "handEmpty";
			boolean flag1 = false;
			boolean flag2 = false;
			boolean flag3 = false;
			for (int j = 0; j < nowState.size(); j++) {
				String a = (String) nowState.get(j);
				if (a.equals(check1)) {
					flag1 = true;
				}
				if (a.equals(check2)) {
					flag2 = true;
				}
				if (a.equals(check3)) {
					flag3 = true;
				}
			}
			return flag1 && flag2 && flag3;
		} else if (mat3.find()) {
			String x = mat3.group(1);
			String check1 = "ontable " + x;
			String check2 = "clear " + x;
			String check3 = "handEmpty";
			boolean flag1 = false;
			boolean flag2 = false;
			boolean flag3 = false;
			for (int j = 0; j < nowState.size(); j++) {
				String a = (String) nowState.get(j);
				if (a.equals(check1)) {
					flag1 = true;
				}
				if (a.equals(check2)) {
					flag2 = true;
				}
				if (a.equals(check3)) {
					flag3 = true;
				}
			}
			return flag1 && flag2 && flag3;
		} else if (mat4.find()) {
			String x = mat4.group(1);
			String check1 = "holding " + x;
			boolean flag1 = false;
			for (int j = 0; j < nowState.size(); j++) {
				String a = (String) nowState.get(j);
				if (a.equals(check1)) {
					flag1 = true;
				}
			}
			return flag1;
		}
		return false;
	}

	/////////////////////////////////////////////////////////////

	//新たに追加したメソッド

	//競合解消

	/////////////////////////////////////////////////////////////

	public static void ConfRes0(Vector<Operator> operators, String thegoal, Hashtable theBinding) {
		for (int i = 0; i < operators.size(); i++) {
			Hashtable fbind = new Hashtable();
			for (String add : operators.elementAt(i).addList) {
				if ((new Unifier()).unify(add, thegoal, theBinding)) {
					Operator op = operators.elementAt(i);
					operators.removeElementAt(i);
					operators.add(0, op);
					//	              System.out.println(fbind);
					//	              break;

				}
			}
		}
	}

	public static Vector rename(Vector a, Hashtable b) {
		Vector re = new Vector();
		for (int i = 0; i < a.size(); i++) {
			String op = (String) a.elementAt(i);
			String memo = Operator.instantiateString(op, b);
			re.add(memo);
		}
		return re;
	}

	public static ArrayList<String> changeInitialState(
			ArrayList<String> initialState, String order) {
		if (order.startsWith("add")) {
			order = order.substring(4); // orderから"add"を削除
			// System.out.println(order);
			if (!initialState.contains(order)) {
				initialState.add(order);
			}
		} else if (order.startsWith("append")) {
			order = order.substring(7); // orderから"append"を削除
			// System.out.println(order);
			if (!initialState.contains(order)) {
				initialState.add(order);
			}
		} else if (order.startsWith("delete")) {
			order = order.substring(7); // orderから"delete"を削除
			if (initialState.contains(order)) {
				// System.out.println(order);
				initialState.remove(initialState.indexOf(order));
			}
		} else if (order.startsWith("remove")) {
			order = order.substring(7); // orderから"remove"を削除
			if (initialState.contains(order)) {
				// System.out.println(order);
				initialState.remove(initialState.indexOf(order));
			}
		}
		System.out.println(initialState);
		return initialState;
	}

	public static ArrayList<String> changeGoalList(ArrayList<String> goalList,
			String order) {
		if (order.startsWith("add")) {
			order = order.substring(4); // orderから"add"を削除
			// System.out.println(order);
			if (!goalList.contains(order)) {
				goalList.add(order);
			}
		} else if (order.startsWith("append")) {
			order = order.substring(7); // orderから"append"を削除
			// System.out.println(order);
			if (!goalList.contains(order)) {
				goalList.add(order);
			}
		} else if (order.startsWith("delete")) {
			order = order.substring(7); // orderから"delete"を削除
			if (goalList.contains(order)) {
				// System.out.println(order);
				goalList.remove(goalList.indexOf(order));
			}
		} else if (order.startsWith("remove")) {
			order = order.substring(7); // orderから"remove"を削除
			if (goalList.contains(order)) {
				// System.out.println(order);
				goalList.remove(goalList.indexOf(order));
			}
		}
		System.out.println(goalList);
		return goalList;
	}

	public void start() {
		initOperators();
		Vector goalList = initGoalListfile("goal.txt");
		Vector initialState = initInitialStatefile("state.txt");
		System.out.println("aaaaa" + initialState);
		initialState = changeinitalState(initialState);
		System.out.println("aaaaa" + initialState);
		System.out.println("aaaaa" + blockform);
		state = initInitialStatefile("state.txt");
		cr = new ConflictResolution(operators);
		Hashtable theBinding = new Hashtable();
		plan = new Vector();
		planning(goalList, initialState, theBinding);

		System.out.println("***** This is a plan! *****");
		for (int i = 0; i < plan.size(); i++) {
			Operator op = (Operator) plan.elementAt(i);
			System.out.println((op.instantiate(theBinding)).name);
			doplan.add((op.instantiate(theBinding)).name);
		}
	}

	static int count = 0;

	private static boolean planning(Vector theGoalList,  Vector<WorkingMemory> theCurrentState,
			Hashtable theBinding) {
		count++;
		if (count > 10) {
			System.out.println("ERROR OCCURED");
			System.exit(0);
		}
		System.out.println("*** GOALS ***" + theGoalList);
		if (theGoalList.size() == 1) {
			String aGoal = (String) theGoalList.elementAt(0);
			if (planningAGoal(aGoal, theCurrentState, theBinding, 0) != -1) {
				return true;
			} else {
				return false;
			}
		} else {
			String aGoal = (String) theGoalList.elementAt(0);
			int cPoint = 0; // 現在のゴールリスト内の探索場所
			while (cPoint < operators.size()) {
				// System.out.println("cPoint:"+cPoint);
				// Store original binding
				Hashtable orgBinding = new Hashtable(); // theBindingのコピーを生成
				for (Enumeration e = theBinding.keys(); e.hasMoreElements();) {
					String key = (String) e.nextElement();
					String value = (String) theBinding.get(key);
					orgBinding.put(key, value);
				}
				Vector<WorkingMemory> orgState = new Vector(); // CurrentStateのコピー
				for (int i = 0; i < theCurrentState.size(); i++) {
					orgState.addElement(theCurrentState.elementAt(i));
				}

				int tmpPoint = planningAGoal(aGoal, theCurrentState,
						theBinding, cPoint); // 失敗したら-1
				// System.out.println("tmpPoint: "+tmpPoint);

				if (tmpPoint != -1) {
					theGoalList.removeElementAt(0);
					System.out.print("theCurrentState:");
					System.out.println(theCurrentState);
					if (planning(theGoalList, theCurrentState, theBinding)) {
						// System.out.println("Success !");
						return true;
					} else {
						cPoint = tmpPoint;
						// System.out.println("Fail::"+cPoint);
						theGoalList.insertElementAt(aGoal, 0);

						theBinding.clear();
						for (Enumeration e = orgBinding.keys(); e
								.hasMoreElements();) {
							String key = (String) e.nextElement();
							String value = (String) orgBinding.get(key);
							theBinding.put(key, value);
						}
						theCurrentState.removeAllElements();
						for (int i = 0; i < orgState.size(); i++) {
							theCurrentState.addElement(orgState.elementAt(i));
						}
					}
				} else {
					theBinding.clear();
					for (Enumeration e = orgBinding.keys(); e.hasMoreElements();) {
						String key = (String) e.nextElement();
						String value = (String) orgBinding.get(key);
						theBinding.put(key, value);
					}
					theCurrentState.removeAllElements();
					for (int i = 0; i < orgState.size(); i++) {
						theCurrentState.addElement(orgState.elementAt(i));
					}
					return false;
				}
				// System.out.println("test:");
			}
			return false;
		}
	}

	private static int planningAGoal(String theGoal, Vector<WorkingMemory> theCurrentState,
			Hashtable theBinding, int cPoint) {
		System.out.println("**" + theGoal);
		int size = theCurrentState.size();
		// System.out.println("StateSize:"+size);
		for (int i = 0; i < size; i++) {
			String aState = theCurrentState.elementAt(i).getmState(); // 1つずつマッチング
			if ((new Unifier()).unify(theGoal, aState, theBinding)) {
				return 0; // 調査中のゴールが現在の状態にあれば成功
			}
		}
		// なければルールを使って分解する
		  int randInt = Math.abs(rand.nextInt()) % operators.size();
		//  Operator op = (Operator)operators.elementAt(randInt);
		//  operators.removeElementAt(randInt);
		//  operators.addElement(op);
		     cr.initState(theCurrentState,theBinding,theGoal,uniqueNum);
		     Vector<Operator> LEXOp = cr.Lex();
		for (int i = cPoint; i < operators.size(); i++) { // オペレーター全てを回す
			Operator anOperator = rename((Operator) operators.elementAt(i));
			// 現在のCurrent state, Binding, planをbackup
			Hashtable orgBinding = new Hashtable();
			for (Enumeration e = theBinding.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				String value = (String) theBinding.get(key);
				orgBinding.put(key, value);
			}
			Vector<WorkingMemory> orgState = new Vector();
			for (int j = 0; j < theCurrentState.size(); j++) {
				orgState.addElement(theCurrentState.elementAt(j));
			}
			Vector orgPlan = new Vector();
			for (int j = 0; j < plan.size(); j++) {
				orgPlan.addElement(plan.elementAt(j));
			}

			Vector addList = (Vector) anOperator.getAddList(); // オペレーターのADDリストを入手
			for (int j = 0; j < addList.size(); j++) {
				if ((new Unifier()).unify(theGoal,
						(String) addList.elementAt(j), theBinding)) { // 今のオペレータと今のゴールが一致したら
					// System.out.print("Addlistelementat:");
					// System.out.println(addList.elementAt(j));
					// System.out.println("Cdlistelementat:");
					// System.out.println(anOperator);
					Operator newOperator = anOperator.instantiate(theBinding);
					// System.out.println("NewAddlistelementat:");
					// System.out.println(newOperator);
					Vector newGoals = (Vector) newOperator.getIfList();
					System.out.print("Newname:");
					System.out.println(newOperator.name);
					if (planning(newGoals, theCurrentState, theBinding)) { // &checkA(newOperator.name)
						System.out.print("Ewname:");
						System.out.println(newOperator.name);
						newOperator = newOperator.instantiate(theBinding);
						plan.addElement(newOperator);
						theCurrentState = newOperator
								.applyState(theCurrentState,theBinding,++mTimetag);
						return i + 1;
					} else {
						// 失敗したら元に戻す．
						theBinding.clear();
						for (Enumeration e = orgBinding.keys(); e
								.hasMoreElements();) {
							String key = (String) e.nextElement();
							String value = (String) orgBinding.get(key);
							theBinding.put(key, value);
						}
						theCurrentState.removeAllElements();
						for (int k = 0; k < orgState.size(); k++) {
							theCurrentState.addElement((WorkingMemory) orgState.elementAt(k));
						}
						plan.removeAllElements();
						for (int k = 0; k < orgPlan.size(); k++) {
							plan.addElement(orgPlan.elementAt(k));
						}
					}
				}
			}
		}
		return -1;
	}

	static int uniqueNum = 0;

	private static Operator rename(Operator theOperator) {
		Operator newOperator = theOperator.getRenamedOperator(uniqueNum);
		uniqueNum = uniqueNum + 1;
		return newOperator;
	}

	private Vector initGoalList() {
		Vector goalList = new Vector();
		goalList.addElement("B on C");
		goalList.addElement("A on B");
		return goalList;
	}

	public static Vector chengegoal(Vector goal) {
		Vector goalList = new Vector();
		HashMap<String, Integer> name = new HashMap<String, Integer>();
		HashMap<String, String> blocks = new HashMap<String, String>();
		String holding="";
		for (int j = 0; j < goal.size(); j++) {
			String a = (String) goal.get(j);
			Pattern pat1 = Pattern.compile("holding (.+)");
			Matcher mat1 = pat1.matcher(a);
			Pattern pat2 = Pattern.compile("(.+) on (.+)");
			Matcher mat2 = pat2.matcher(a);
			if (mat1.find()) {
				holding=a;
			}
			if (mat2.find()) {
				String x = mat2.group(1);
				String y = mat2.group(2);
				blocks.put(y, x);
				if (name.containsKey(x)) {
					int num = name.get(x);
					num++;
					name.put(x, num);
				}
				else {
					name.put(x, 1);
				}
				if (name.containsKey(y)) {
					int num = name.get(y);
					num++;
					name.put(y, num);
				}
				else {
					name.put(y, 1);
				}
			}
			else{
				goalList.addElement(a);
			
			}
		}
		System.out.println(name);
		System.out.println(goal);
		while (true) {
			boolean flag = true;
			for (int j = 0; j < goal.size(); j++) {
				String a = (String) goal.get(j);
				Pattern pat2 = Pattern.compile("(.+) on (.+)");
				Matcher mat2 = pat2.matcher(a);
				if (mat2.find()) {
					String x = mat2.group(1);
					String y = mat2.group(2);
					int s = name.get(y);
					if (s == 1) {
						flag = false;
						goalList.addElement(a);
						int num = name.get(y);
						num--;
						name.put(y, num);
						num = name.get(x);
						num--;
						name.put(x, num);
						System.out.println(name);
						if (num == 1) {
							while (true) {
								if (blocks.containsKey(x)) {
									num=name.get(x);
									num--;
									name.put(x,num);
									String b = blocks.get(x);
									num=name.get(b);
									num--;
									name.put(b,num);
									String plus=b+" on " + x;
									goalList.addElement(plus);
									System.out.println(name);
									x=b;
								}
								else {
									break;
								}
							}
						}
					}

				}
			}
			if (flag) {
				break;
			}
		}
		if(!holding.equals("")){
			goalList.addElement(holding);
		}
		System.out.println("result" +goalList);
		return goalList;
	}

	public static Vector initGoalListfile(String fileName) {
		Vector goalList = new Vector();
		try { // ファイル読み込みに失敗した時の例外処理のためのtry-catch構文

			// 文字コードを指定してBufferedReaderオブジェクトを作る
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), "UTF-8"));
			// 変数lineに1行ずつ読み込むfor文
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {
				goalList.addElement(line);
			}

		} catch (IOException e) {
			e.printStackTrace(); // 例外が発生した所までのスタックトレースを表示
		}
		goalList=chengegoal(goalList);
		return goalList;

	}

	public static Vector changeinitalState(Vector now) {
		Vector initialState = new Vector();
		for (int j = 0; j < now.size(); j++) {
			String a = (String) now.get(j);
			Pattern pat1 = Pattern.compile("(.+)'s color is (.+)");
			Matcher mat1 = pat1.matcher(a);
			Pattern pat2 = Pattern.compile("(.+) is (.+)");
			Matcher mat2 = pat2.matcher(a);
			if (mat1.find()) {
				String x = mat1.group(1);
				String color = mat1.group(2);
				if (colors.containsKey(color)) {
					ArrayList<String> plus = colors.get(color);
					plus.add(x);
					colors.put(color, plus);
				}
				else {
					ArrayList<String> plus = new ArrayList<String>();
					plus.add(x);
					colors.put(color, plus);
				}
			}
			else if (mat2.find()) {
				String x = mat2.group(1);
				String block = mat2.group(2);
				if (blockform.containsKey(block)) {
					ArrayList<String> plus = blockform.get(block);
					plus.add(x);
					blockform.put(block, plus);
				}
				else {
					ArrayList<String> plus = new ArrayList<String>();
					plus.add(x);
					blockform.put(block, plus);
				}
			}
			else {
				initialState.addElement(a);
			}
		}
		return initialState;

	}

	private Vector initInitialState() {
		Vector initialState = new Vector();
		initialState.addElement("clear A");
		initialState.addElement("clear B");
		initialState.addElement("clear C");
		initialState.addElement("ontable A");
		initialState.addElement("ontable B");
		initialState.addElement("ontable C");
		initialState.addElement("handEmpty");
		return initialState;
	}

	public static Vector initInitialStatefile(String fileName) {
		Vector<WorkingMemory> initialState = new Vector();
	
		try { // ファイル読み込みに失敗した時の例外処理のためのtry-catch構文

			// 文字コードを指定してBufferedReaderオブジェクトを作る
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), "UTF-8"));
			// 変数lineに1行ずつ読み込むfor文
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {
				initialState.addElement(new WorkingMemory(line,mTimetag));
			
			}

		} catch (IOException e) {
			e.printStackTrace(); // 例外が発生した所までのスタックトレースを表示
		}
		return initialState;
	}

	private static void initOperators() {
		operators = new Vector();

		// OPERATOR 1
		// / NAME
		String name1 = new String("Place ?x on ?y");
		// / IF
		Vector ifList1 = new Vector();
		ifList1.addElement(new String("clear ?y"));
		ifList1.addElement(new String("holding ?x"));
		// / ADD-LIST
		Vector addList1 = new Vector();
		addList1.addElement(new String("?x on ?y"));
		addList1.addElement(new String("clear ?x"));
		addList1.addElement(new String("handEmpty"));
		// / DELETE-LIST
		Vector deleteList1 = new Vector();
		deleteList1.addElement(new String("clear ?y"));
		deleteList1.addElement(new String("holding ?x"));
		Operator operator1 = new Operator(name1, ifList1, addList1, deleteList1);
		operators.addElement(operator1);

		// OPERATOR 2
		// / NAME
		String name2 = new String("remove ?x from on top ?y");
		// / IF
		Vector ifList2 = new Vector();
		ifList2.addElement(new String("?x on ?y"));
		ifList2.addElement(new String("clear ?x"));
		ifList2.addElement(new String("handEmpty"));
		// / ADD-LIST
		Vector addList2 = new Vector();
		addList2.addElement(new String("clear ?y"));
		addList2.addElement(new String("holding ?x"));
		// / DELETE-LIST
		Vector deleteList2 = new Vector();
		deleteList2.addElement(new String("?x on ?y"));
		deleteList2.addElement(new String("clear ?x"));
		deleteList2.addElement(new String("handEmpty"));
		Operator operator2 = new Operator(name2, ifList2, addList2, deleteList2);
		operators.addElement(operator2);
		// OPERATOR 3
		// / NAME
		String name3 = new String("pick up ?x from the table");
		// / IF
		Vector ifList3 = new Vector();
		ifList3.addElement(new String("ontable ?x"));
		ifList3.addElement(new String("clear ?x"));
		ifList3.addElement(new String("handEmpty"));
		// / ADD-LIST
		Vector addList3 = new Vector();
		addList3.addElement(new String("holding ?x"));
		// / DELETE-LIST
		Vector deleteList3 = new Vector();
		deleteList3.addElement(new String("ontable ?x"));
		deleteList3.addElement(new String("clear ?x"));
		deleteList3.addElement(new String("handEmpty"));
		Operator operator3 = new Operator(name3, ifList3, addList3, deleteList3);
		operators.addElement(operator3);

		// OPERATOR 4
		// / NAME
		String name4 = new String("put ?x down on the table");
		// / IF
		Vector ifList4 = new Vector();
		ifList4.addElement(new String("holding ?x"));
		// / ADD-LIST
		Vector addList4 = new Vector();
		addList4.addElement(new String("ontable ?x"));
		addList4.addElement(new String("clear ?x"));
		addList4.addElement(new String("handEmpty"));
		// / DELETE-LIST
		Vector deleteList4 = new Vector();
		deleteList4.addElement(new String("holding ?x"));
		Operator operator4 = new Operator(name4, ifList4, addList4, deleteList4);
		operators.addElement(operator4);

	}
}

class Operator {
	String name;
	Vector<String> ifList;
	Vector<String> addList;
	Vector<String> deleteList;

	Operator(String theName, Vector theIfList, Vector theAddList,
			Vector theDeleteList) {
		name = theName;
		ifList = theIfList;
		addList = theAddList;
		deleteList = theDeleteList;
	}

	public Vector getAddList() {
		return addList;
	}

	public Vector getDeleteList() {
		return deleteList;
	}

	public Vector getIfList() {
		return ifList;
	}

	public String toString() {
		String result = "NAME: " + name + "\n" + "IF :" + ifList + "\n"
				+ "ADD:" + addList + "\n" + "DELETE:" + deleteList;
		return result;
	}

    public Vector applyState(Vector<WorkingMemory> theState,Hashtable binding,int num){
        Hashtable b = (Hashtable) binding.clone();
	for(int i = 0 ; i < addList.size() ; i++){
	    theState.addElement(new WorkingMemory(instantiateString(addList.elementAt(i),b),num));
	}
	Vector<WorkingMemory> nState = new Vector<>();
        for(int j = 0;j < deleteList.size();j++){
            String str = deleteList.get(j);
            for(WorkingMemory state: theState) {
                if ((new Unifier()).unify(str, state.getmState(),b)){
                    System.out.println(str+"<<<<<<<<<<<");
                    nState.addElement(state);
                }
            }
        }
        for(int i = 0;i<nState.size();i++){
            theState.removeElement(nState.get(i));
        }
	return theState;
    }
	public Operator getRenamedOperator(int uniqueNum) {
		Vector vars = new Vector();
		// IfListの変数を集める
		for (int i = 0; i < ifList.size(); i++) {
			String anIf = (String) ifList.elementAt(i);
			vars = getVars(anIf, vars);
		}
		// addListの変数を集める
		for (int i = 0; i < addList.size(); i++) {
			String anAdd = (String) addList.elementAt(i);
			vars = getVars(anAdd, vars);
		}
		// deleteListの変数を集める
		for (int i = 0; i < deleteList.size(); i++) {
			String aDelete = (String) deleteList.elementAt(i);
			vars = getVars(aDelete, vars);
		}
		Hashtable renamedVarsTable = makeRenamedVarsTable(vars, uniqueNum);

		// 新しいIfListを作る
		Vector newIfList = new Vector();
		for (int i = 0; i < ifList.size(); i++) {
			String newAnIf = renameVars((String) ifList.elementAt(i),
					renamedVarsTable);
			newIfList.addElement(newAnIf);
		}
		// 新しいaddListを作る
		Vector newAddList = new Vector();
		for (int i = 0; i < addList.size(); i++) {
			String newAnAdd = renameVars((String) addList.elementAt(i),
					renamedVarsTable);
			newAddList.addElement(newAnAdd);
		}
		// 新しいdeleteListを作る
		Vector newDeleteList = new Vector();
		for (int i = 0; i < deleteList.size(); i++) {
			String newADelete = renameVars((String) deleteList.elementAt(i),
					renamedVarsTable);
			newDeleteList.addElement(newADelete);
		}
		// 新しいnameを作る
		String newName = renameVars(name, renamedVarsTable);

		return new Operator(newName, newIfList, newAddList, newDeleteList);
	}

	private Vector getVars(String thePattern, Vector vars) {
		StringTokenizer st = new StringTokenizer(thePattern);
		for (int i = 0; i < st.countTokens();) {
			String tmp = st.nextToken();
			if (var(tmp)) {
				vars.addElement(tmp);
			}
		}
		return vars;
	}

	private Hashtable makeRenamedVarsTable(Vector vars, int uniqueNum) {
		Hashtable result = new Hashtable();
		for (int i = 0; i < vars.size(); i++) {
			String newVar = (String) vars.elementAt(i) + uniqueNum;
			result.put((String) vars.elementAt(i), newVar);
		}
		return result;
	}

	private String renameVars(String thePattern, Hashtable renamedVarsTable) {
		String result = new String();
		StringTokenizer st = new StringTokenizer(thePattern);
		for (int i = 0; i < st.countTokens();) {
			String tmp = st.nextToken();
			if (var(tmp)) {
				result = result + " " + (String) renamedVarsTable.get(tmp);
			} else {
				result = result + " " + tmp;
			}
		}
		return result.trim();
	}

	public Operator instantiate(Hashtable theBinding) {
		// name を具体化
		String newName = instantiateString(name, theBinding);
		// ifList を具体化
		Vector newIfList = new Vector();
		for (int i = 0; i < ifList.size(); i++) {
			String newIf = instantiateString((String) ifList.elementAt(i),
					theBinding);
			newIfList.addElement(newIf);
		}
		// addList を具体化
		Vector newAddList = new Vector();
		for (int i = 0; i < addList.size(); i++) {
			String newAdd = instantiateString((String) addList.elementAt(i),
					theBinding);
			newAddList.addElement(newAdd);
		}
		// deleteListを具体化
		Vector newDeleteList = new Vector();
		for (int i = 0; i < deleteList.size(); i++) {
			String newDelete = instantiateString(
					(String) deleteList.elementAt(i), theBinding);
			newDeleteList.addElement(newDelete);
		}
		return new Operator(newName, newIfList, newAddList, newDeleteList);
	}

	public static String instantiateString(String thePattern,
			Hashtable theBinding) {
		String result = new String();
		StringTokenizer st = new StringTokenizer(thePattern);
		for (int i = 0; i < st.countTokens();) {
			String tmp = st.nextToken();
			if (var(tmp)) {
				String newString = (String) theBinding.get(tmp);
				if (newString == null) {
					result = result + " " + tmp;
				} else {
					result = result + " " + newString;
				}
			} else {
				result = result + " " + tmp;
			}
		}
		return result.trim();
	}

	private static boolean var(String str1) {
		// 先頭が ? なら変数
		return str1.startsWith("?");
	}
}

class Unifier {
	StringTokenizer st1;
	String buffer1[];
	StringTokenizer st2;
	String buffer2[];
	Hashtable vars;

	Unifier() {
		// vars = new Hashtable();
	}

	public boolean unify(String string1, String string2, Hashtable theBindings) {
		Hashtable orgBindings = new Hashtable();
		for (Enumeration e = theBindings.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String value = (String) theBindings.get(key);
			orgBindings.put(key, value);
		}
		this.vars = theBindings;
		if (unify(string1, string2)) {
			return true;
		} else {
			// 失敗したら元に戻す．
			theBindings.clear();
			for (Enumeration e = orgBindings.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				String value = (String) orgBindings.get(key);
				theBindings.put(key, value);
			}
			return false;
		}
	}

	public boolean unify(String string1, String string2) {
		// 同じなら成功
		if (string1.equals(string2))
			return true;

		// 各々トークンに分ける
		st1 = new StringTokenizer(string1);
		st2 = new StringTokenizer(string2);

		// 数が異なったら失敗
		if (st1.countTokens() != st2.countTokens())
			return false;

		// 定数同士
		int length = st1.countTokens();
		buffer1 = new String[length];
		buffer2 = new String[length];
		for (int i = 0; i < length; i++) {
			buffer1[i] = st1.nextToken();
			buffer2[i] = st2.nextToken();
		}

		// 初期値としてバインディングが与えられていたら
		if (this.vars.size() != 0) {
			for (Enumeration keys = vars.keys(); keys.hasMoreElements();) {
				String key = (String) keys.nextElement();
				String value = (String) vars.get(key);
				replaceBuffer(key, value);
			}
		}

		for (int i = 0; i < length; i++) {
			if (!tokenMatching(buffer1[i], buffer2[i])) {
				return false;
			}
		}

		return true;
	}

	boolean tokenMatching(String token1, String token2) {
		if (token1.equals(token2))
			return true;
		if (var(token1) && !var(token2))
			return varMatching(token1, token2);
		if (!var(token1) && var(token2))
			return varMatching(token2, token1);
		if (var(token1) && var(token2))
			return varMatching(token1, token2);
		return false;
	}

	boolean varMatching(String vartoken, String token) {
		if (vars.containsKey(vartoken)) {
			if (token.equals(vars.get(vartoken))) {
				return true;
			} else {
				return false;
			}
		} else {
			replaceBuffer(vartoken, token);
			if (vars.contains(vartoken)) {
				replaceBindings(vartoken, token);
			}
			vars.put(vartoken, token);
		}
		return true;
	}

	void replaceBuffer(String preString, String postString) {
		for (int i = 0; i < buffer1.length; i++) {
			if (preString.equals(buffer1[i])) {
				buffer1[i] = postString;
			}
			if (preString.equals(buffer2[i])) {
				buffer2[i] = postString;
			}
		}
	}

	void replaceBindings(String preString, String postString) {
		Enumeration keys;
		for (keys = vars.keys(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			if (preString.equals(vars.get(key))) {
				vars.put(key, postString);
			}
		}
	}

	boolean var(String str1) {
		// 先頭が ? なら変数
		return str1.startsWith("?");
	}

}
