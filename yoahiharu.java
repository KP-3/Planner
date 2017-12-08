	int count=0;
	private boolean planning(Vector theGoalList,
			Vector theCurrentState,
			Hashtable theBinding){
		count++;
		if(count>1000){
			System.out.println("ERROR OCCURED");	
			System.exit(0);
		}
		System.out.println("*** GOALS ***" + theGoalList);
		if(theGoalList.size() == 1){
			String aGoal = (String)theGoalList.elementAt(0);
			if(planningAGoal(aGoal,theCurrentState,theBinding,0) != -1){
				return true;
			} else {
				return false;
			}
		} else {
			String aGoal = (String)theGoalList.elementAt(0);
			int cPoint = 0; //現在のゴールリスト内の探索場所
			while(cPoint < operators.size()){
				//System.out.println("cPoint:"+cPoint);
				// Store original binding
				Hashtable orgBinding = new Hashtable(); //theBindingのコピーを生成
				for(Enumeration e = theBinding.keys() ; e.hasMoreElements();){
					String key = (String)e.nextElement();
					String value = (String)theBinding.get(key);
					orgBinding.put(key,value);
				}
				Vector orgState = new Vector(); //CurrentStateのコピー
				for(int i = 0; i < theCurrentState.size() ; i++){
					orgState.addElement(theCurrentState.elementAt(i));
				}

				int tmpPoint = planningAGoal(aGoal,theCurrentState,theBinding,cPoint); //失敗したら-1
				//				System.out.println("tmpPoint: "+tmpPoint);

				if(tmpPoint != -1){
					theGoalList.removeElementAt(0);
					System.out.print("theCurrentState:");
					System.out.println(theCurrentState);
					if(planning(theGoalList,theCurrentState,theBinding)){
						//System.out.println("Success !");
						return true;
					} else {
						cPoint = tmpPoint;
						//System.out.println("Fail::"+cPoint);
						theGoalList.insertElementAt(aGoal,0);

						theBinding.clear();
						for(Enumeration e=orgBinding.keys();e.hasMoreElements();){
							String key = (String)e.nextElement();
							String value = (String)orgBinding.get(key);
							theBinding.put(key,value);
						}
						theCurrentState.removeAllElements();
						for(int i = 0 ; i < orgState.size() ; i++){
							theCurrentState.addElement(orgState.elementAt(i));
						}
					}
				} else {
					theBinding.clear();
					for(Enumeration e=orgBinding.keys();e.hasMoreElements();){
						String key = (String)e.nextElement();
						String value = (String)orgBinding.get(key);
						theBinding.put(key,value);
					}
					theCurrentState.removeAllElements();
					for(int i = 0 ; i < orgState.size() ; i++){
						theCurrentState.addElement(orgState.elementAt(i));
					}
					return false;
				}
//				System.out.println("test:");	
			}
			return false;
		}
	}

	private int planningAGoal(String theGoal,Vector theCurrentState,
			Hashtable theBinding,int cPoint){
		System.out.println("**"+theGoal);
		int size = theCurrentState.size();
		//		System.out.println("StateSize:"+size);
		for(int i =  0; i < size ; i++){
			String aState = (String)theCurrentState.elementAt(i); //1つずつマッチング
			if((new Unifier()).unify(theGoal,aState,theBinding)){
				return 0;                                          //調査中のゴールが現在の状態にあれば成功
			}
		}
		//なければルールを使って分解する
		int randInt = Math.abs(rand.nextInt()) % (operators.size()-1);
		Operator op = (Operator)operators.elementAt(randInt);
		operators.removeElementAt(randInt);
		operators.add(2,op);

		for(int i = cPoint ; i < operators.size() ; i++){ //オペレーター全てを回す
			Operator anOperator = rename((Operator)operators.elementAt(i));
			// 現在のCurrent state, Binding, planをbackup
			Hashtable orgBinding = new Hashtable();
			for(Enumeration e = theBinding.keys() ; e.hasMoreElements();){
				String key = (String)e.nextElement();
				String value = (String)theBinding.get(key);
				orgBinding.put(key,value);
			}
			Vector orgState = new Vector();
			for(int j = 0; j < theCurrentState.size() ; j++){
				orgState.addElement(theCurrentState.elementAt(j));
			}
			Vector orgPlan = new Vector();
			for(int j = 0; j < plan.size() ; j++){
				orgPlan.addElement(plan.elementAt(j));
			}

			Vector addList = (Vector)anOperator.getAddList();   //オペレーターのADDリストを入手
			for(int j = 0 ; j < addList.size() ; j++){
				if((new Unifier()).unify(theGoal,               
						(String)addList.elementAt(j),
						theBinding)){                            //今のオペレータと今のゴールが一致したら
					//					System.out.print("Addlistelementat:");
					//					System.out.println(addList.elementAt(j));
					//					System.out.println("Cdlistelementat:");
					//					System.out.println(anOperator);
					Operator newOperator = anOperator.instantiate(theBinding);
					//					System.out.println("NewAddlistelementat:");
					//					System.out.println(newOperator);
					Vector newGoals = (Vector)newOperator.getIfList();
					System.out.print("Newname:");
					System.out.println(newOperator.name);
					if(planning(newGoals,theCurrentState,theBinding)){    //&checkA(newOperator.name)
						System.out.print("Ewname:");
						System.out.println(newOperator.name);
						plan.addElement(newOperator);
						theCurrentState =newOperator.applyState(theCurrentState);
						return i+1;
					} else {
						// 失敗したら元に戻す．
						theBinding.clear();
						for(Enumeration e=orgBinding.keys();e.hasMoreElements();){
							String key = (String)e.nextElement();
							String value = (String)orgBinding.get(key);
							theBinding.put(key,value);
						}
						theCurrentState.removeAllElements();
						for(int k = 0 ; k < orgState.size() ; k++){
							theCurrentState.addElement(orgState.elementAt(k));
						}
						plan.removeAllElements();
						for(int k = 0 ; k < orgPlan.size() ; k++){
							plan.addElement(orgPlan.elementAt(k));
						}
					}
				}		
			}
		}
		return -1;
	}

private Vector initGoalList(){
		Vector goalList = new Vector();

		goalList.addElement("C on B");
		goalList.addElement("D on C");
		goalList.addElement("handEmpty");
		goalList.addElement("B on A");
		//		goalList.addElement("holding E");

		Vector onGoalList = new Vector();
		Vector otherList = new Vector();
		Vector finalGoalList = new Vector();

		for(Object s : goalList)
		{
			String str = (String)s;
			String[] tmp = str.split(" ", 0);
			if(tmp.length == 3)
			{
				onGoalList.addElement(s);
			}
			else
			{
				otherList.addElement(s);
			}
		}
		boolean handEmpty =false;
		for (Object s : otherList) {
			if(Objects.equals("handEmpty",s))
			{
				handEmpty=true;
			}
		}
		if(handEmpty)
			for(Object s : otherList)
			{
				String str = (String)s;
				String[] tmp = str.split(" ", 0);
				if(Objects.equals("holding",tmp[0]))
				{
					System.out.println("ERROR OCCURED");	
					System.exit(0);
				}
			}
		//System.out.println(onGoalList);

		Map<String, Integer> m = new HashMap<String, Integer>();
		// Java7以降なら new HashMap<>() でOK

		for (Object s : onGoalList) {
			String str = (String)s;
			String[] tmp = str.split(" ", 0);
			for(String t : tmp){

				int v;
				if (m.containsKey(t)) {
					// Mapに登録済み
					v = m.get(t) + 1;
				} else {
					// Mapに未登録
					v = 1;
				}
				m.put((String)t, v);
			}
		}
		//		System.out.println(m);
		Vector oneList =new Vector();
		int onecount=0;
		for (String key : m.keySet()) 
		{
			//			System.out.println(m.get(key));
			if(m.get(key)==1)
			{
				oneList.add(key);
				onecount++;
			}
		}
		if(onecount>2)
		{
			System.out.println("ERROR OCCURED");	
			System.exit(0);
		}
		String first = "";
		String last = "";

		//		System.out.println(oneList);
		for (Object s : onGoalList) {
			String str = (String)s;
			String[] tmp = str.split(" ", 0);
			if(oneList.indexOf(tmp[2])>-1){
				finalGoalList.add(s);
				last = tmp[0];
				oneList.remove(oneList.indexOf(tmp[2]));
				first=(String)oneList.get(0);
			}

		}
		//		System.out.println("first="+first);
		//		System.out.println("last="+last);
		String x="";
		do{
			for(Object s : onGoalList)
			{
				String str = (String)s;
				String[] tmp = str.split(" ", 0);
				//			System.out.println(s);
				//			System.out.print(tmp[2]+',');
				//			System.out.println(last);

				if(Objects.equals(tmp[2],last)){
					finalGoalList.add(s);
					last = tmp[0];
					x = tmp[0];
					//	System.out.println("first="+first);
					//	System.out.println("last="+last);

				}

			}
		}while(!Objects.equals(first,x));

		goalList.clear();
		goalList.addAll(finalGoalList);
		goalList.addAll(otherList);

		return goalList;
	}

	private void initOperators(){
		operators = new Vector();

		// OPERATOR 1
		/// NAME
		String name1 = new String("Place ?x on ?y");
		/// IF
		Vector ifList1 = new Vector();
		ifList1.addElement(new String("clear ?y"));
		ifList1.addElement(new String("holding ?x"));
		/// ADD-LIST
		Vector addList1 = new Vector();
		addList1.addElement(new String("?x on ?y"));
		addList1.addElement(new String("clear ?x"));
		addList1.addElement(new String("handEmpty"));
		/// DELETE-LIST
		Vector deleteList1 = new Vector();
		deleteList1.addElement(new String("clear ?y"));
		deleteList1.addElement(new String("holding ?x"));
		Operator operator1 =
				new Operator(name1,ifList1,addList1,deleteList1);
		operators.addElement(operator1);

		// OPERATOR 2
		/// NAME
		String name2 = new String("remove ?x from on top ?y");
		/// IF
		Vector ifList2 = new Vector();
		ifList2.addElement(new String("?x on ?y"));
		ifList2.addElement(new String("clear ?x"));
		ifList2.addElement(new String("handEmpty"));
		/// ADD-LIST
		Vector addList2 = new Vector();
		addList2.addElement(new String("clear ?y"));
		addList2.addElement(new String("holding ?x"));
		/// DELETE-LIST
		Vector deleteList2 = new Vector();
		deleteList2.addElement(new String("?x on ?y"));
		deleteList2.addElement(new String("clear ?x"));
		deleteList2.addElement(new String("handEmpty"));
		Operator operator2 =
				new Operator(name2,ifList2,addList2,deleteList2);


		// OPERATOR 3
		/// NAME
		String name3 = new String("pick up ?x from the table");
		/// IF
		Vector ifList3 = new Vector();
		ifList3.addElement(new String("ontable ?x"));
		ifList3.addElement(new String("clear ?x"));
		ifList3.addElement(new String("handEmpty"));
		/// ADD-LIST
		Vector addList3 = new Vector();
		addList3.addElement(new String("holding ?x"));
		/// DELETE-LIST
		Vector deleteList3 = new Vector();
		deleteList3.addElement(new String("ontable ?x"));
		deleteList3.addElement(new String("clear ?x"));
		deleteList3.addElement(new String("handEmpty"));
		Operator operator3 =
				new Operator(name3,ifList3,addList3,deleteList3);
		operators.addElement(operator3);

		// OPERATOR 4
		/// NAME
		String name4 = new String("put ?x down on the table");
		/// IF
		Vector ifList4 = new Vector();
		ifList4.addElement(new String("holding ?x"));
		/// ADD-LIST
		Vector addList4 = new Vector();
		addList4.addElement(new String("ontable ?x"));
		addList4.addElement(new String("clear ?x"));
		addList4.addElement(new String("handEmpty"));
		/// DELETE-LIST
		Vector deleteList4 = new Vector();
		deleteList4.addElement(new String("holding ?x"));
		Operator operator4 =
				new Operator(name4,ifList4,addList4,deleteList4);
		operators.addElement(operator4);
		operators.addElement(operator2);
	}
