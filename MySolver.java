import java.util.*;
import java.lang.*;
import java.io.*;

class MySolver extends Solver
{
	boolean SPECIAL;
	int opponentColor;
	int myColor;
	PrintStream OST;
	Galanthus gop;
	Answer root;
	int inf=0x3f3f3f3f;
	int count=0;
	boolean ICanWinDebug=false;
	ArrayList<Answer>leaf;
	long time1;
	int isMe=1;
	
	int TIME=15;
	
	int[] bean={600,600,600,1200,1200,1200,1200,1200,1200};
	public Answer Go(Answer root)
	{
		time1=System.currentTimeMillis();
		Answer finalAns=MinMax(root,-inf,inf,1);
		
		MaxPropComparator ss = new MaxPropComparator();
		ss.me=isMe;
		Collections.sort(leaf,ss);
		
		long use_time=TIME*1000-(System.currentTimeMillis()-time1);
		System.out.println("首次搜尋:"+use_time+"C:"+count);
		//System.out.println("深度搜尋");
		Answer child=finalAns;
		for(int i=0;i<leaf.size();i++)//第三層搜尋
		{
			long time2=System.currentTimeMillis();
			if(time2-time1>=TIME*1000)
				break;
			Answer next3Ans=MinMax(leaf.get(i),-inf,inf,4);
			if(next3Ans.GetPrice()*isMe>child.GetPrice()*isMe)
			{
				Dep(next3Ans);
				Dep(next3Ans.father.father);
				OST.println("-------------------------");
				finalAns=next3Ans.father.father.father;
				child=next3Ans;
			}
		}
		use_time=1000*TIME-(System.currentTimeMillis()-time1);
		System.out.println("深度搜尋花費:"+use_time+"C:"+count);
		
		return finalAns;
	}
	public String play()
	{
		leaf = new ArrayList<Answer>();
		//取得自己的顏色
		myColor = GetMarkTake();
		opponentColor=myColor%2+1;
		//DEBUG過程
		try
		{
			OST = new PrintStream(new FileOutputStream("DEBUG.txt"));
			gop = new Galanthus(this);
			//root，應該站對方的立場，估計對方的price以及攻擊
			root = new Answer(this,opponentColor);
			root.Update_All_Price();
			root.Aegis = IWillLose(root);
		}
		catch (FileNotFoundException ex)  
		{
			// insert code to run when exception occurs
		}
		//VS();
		if(root.GetPrice()<=-99999)
			return "lost";
		Answer finalAns=Go(root);
		Dep(finalAns);
		Mark(finalAns.move1);
		Mark(finalAns.move2);
		root = new Answer(this,myColor);
		root.Update_All_Price();
		
		if(root.GetPrice()>=99999)
		{
			System.out.println(finalAns.PrintMove());
			return "win";
		}
		//time1=System.currentTimeMillis();
		//Answer nextAns=MinMax(finalAns,-inf,inf,1);
		//Dep(nextAns);
		//test1();
		//testIcanWin2();
		//deepTest();
		//testIWillLose();
		return finalAns.PrintMove();
	}
	//兩個電腦PK用
	void VS()
	{
		//Mark(new Move(8,10),myColor);
		//Mark(new Move(9,11),myColor);
		//Mark(new Move(10,12),myColor);
		//Mark(new Move(9,12),myColor);
		//Mark(new Move(11,12),myColor);
		int c=1;
		while(true)
		{
			isMe=-1;
			System.out.printf("第%d手\n",c);
			c+=1;
			Answer root = new Answer(this,myColor);
			leaf = new ArrayList<Answer>();
			root.Update_All_Price();
			root.Aegis = IWillLose(root);
			//root.move1=new Move(2,3);
						//root.move2=new Move(4,3);
			//Dep(root);
			if(root.GetPrice()<=-99999)
				break;

			Answer finalAns=Go(root);
			Dep(finalAns);
			Mark(finalAns.move1,opponentColor);
			Mark(finalAns.move2,opponentColor);
			PrintBoard(GetCurrentState());
			count=0;
			isMe=1;
			root = new Answer(this,opponentColor);
			leaf = new ArrayList<Answer>();
			root.Update_All_Price();
			root.Aegis = IWillLose(root);
			
			if(root.GetPrice()>=99999)
				break;
			time1=System.currentTimeMillis();
			finalAns=Go(root);
			Dep(finalAns);
			Mark(finalAns.move1,myColor);
			Mark(finalAns.move2,myColor);
			PrintBoard(GetCurrentState());
			count=0;
		}
		
	}
	Answer MinMax(Answer parent,int a,int b,int deep)
	{
		
		long time2=System.currentTimeMillis();
		count+=1;
		if(count%5000==0)
			System.out.println(count);
		int winColor = parent.GetOpponentColor();//取得顏色
		int me=gop.MinOrMax(this.myColor,winColor);
		
		//這行可以走的步數
		ArrayList<TMove>next=new ArrayList<TMove>();
		parent.R();//Update

		//------------------可否勝利-----------------------------
		Answer ans=ICanWin(parent);//確認是否勝利
		
		if(ans!=null)//找到勝利
		{
			if(ans.GetPrice()*me<499999)
				System.out.println("Warning!權重計算錯誤!"+ans.GetPrice());
			return ans;
		}
		//--------------------------------------------------
		
		//----------有必下點--------------------------------------
		if(parent.Aegis.Aegis!=null&&parent.Aegis.Aegis.size()>0)//防禦
		{
			for(int i=0;i<parent.Aegis.Aegis.size();i++)
			{
				TMove def = parent.Aegis.Aegis.get(i);
				if(def.move2!=null)
					next.add(def);
				else
				{
					for(int j=0;j<parent.moves.size();j++)
						if(!def.move1.Equal(parent.moves.get(j)))
							next.add(new TMove(def.move1,parent.moves.get(j)));
				}
			}
		}//--------------------------------------------------
		else if(parent.Aegis.count!=0)
			System.out.println("Warning 此點會輸不須再搜尋!");
		else//沒有破著可以完全攻擊
		{
			for(int i=0;i<parent.moves.size();i++)
				for(int j=i+1;j<parent.moves.size();j++)
					next.add(new TMove(parent.moves.get(i),parent.moves.get(j)));
		}
		
		//-----------------搜尋------------------------
		Answer MyAns=null ;
		
		
		for(int i=0;i<next.size();i++)
		{
			Move m1 = next.get(i).move1;
			Move m2=next.get(i).move2;
			ans = new Answer(parent,parent.GetOpponentColor(),m1,m2);
			ans.Update_Price(m1,m2);
			ans.Aegis = IWillLose(ans);
			if(ans.Aegis.count==3)//-------表示可以在下一回贏-------------
			{
				if(deep==1)
				{
					System.out.println("我好像要贏了唷?");
				}
				Answer nextOp=ICanWin(ans);
				if(nextOp==null)//表示對手無法在下一回贏
				{
					ans.SetFinalPrice(me*999999);
					return ans;
				}
			}//-----------深度搜尋----------------------
			if(parent.Aegis.count==0&&(ans.GetPrice()-parent.GetPrice())*me<=bean[deep]*me||time2-time1>=TIME*1000)
			{
				
			}
			else if(deep!=3&&deep<=6)
			{
				Answer DeepAns=MinMax(ans,a,b,deep+1);
				
				ans.SetFinalPrice(DeepAns.GetPrice());//更新權重為葉子點
				//OST.println(ans.PrintMove()+ans.GetPrice()+":"+ans.oldPrice);
				if(i==0&&deep==1)
				{
					//Up(ans);
					//Dep(ans);
				}
			}
			else if(deep==3)
				leaf.add(ans);
			if(MyAns==null||MyAns.GetPrice()*me<ans.GetPrice()*me)//MyAns還沒被指派s 或是更佳的解
			{
				MyAns=ans;
				if(me==1)//MAX
				{
					a=Math.max(a,ans.GetPrice());
					if(ans.GetPrice()>=b)//表示此點的最大值會比b還大，那樣上一層便會選b而不會選此點
						return ans;
				}
				else//MIN
				{
					b=Math.min(b,ans.GetPrice());
					if(ans.GetPrice()<=a)//表示此點的最大值會比a還小，那樣上一層便會選a而不會選此點
						return ans;
				}
			}
		}
		return MyAns;
	}
	//防禦敵人的攻擊
	AegisMove IWillLose(Answer parent)
	{
		AegisMove Def=new AegisMove();
		
		ArrayList<TMove>Aegis=new ArrayList<TMove>();
		ArrayList<Move>Not_Aegis=new ArrayList<Move>();
		
		int atk=parent.sliver_bullet.size()+parent.golden_bullet.size();
		
		
		for(int i=0;i<parent.shield.size();i++)//找一點 
		{
			int def=0;
			Move d = parent.shield.get(i);
			for(int j=0;j<parent.sliver_bullet.size();j++)
				if(d.Equal(parent.sliver_bullet.get(j).move1)||d.Equal(parent.sliver_bullet.get(j).move2))
					def++;
			for(int j=0;j<parent.golden_bullet.size();j++)
				if(d.Equal(parent.golden_bullet.get(j)))
					def++;
			if(def==atk)
			{
				Aegis.add(new TMove(d,null));
				Def.count=1;
			}
			else
				Not_Aegis.add(d);
			
		}
		
		for(int i=0;i<Not_Aegis.size();i++)
			for(int j=i+1;j<Not_Aegis.size();j++)
			{
				int def=0;
				Move d1 = Not_Aegis.get(i);
				Move d2 = Not_Aegis.get(j);
				for(int k=0;k<parent.sliver_bullet.size();k++)
				{
					Move a1=parent.sliver_bullet.get(k).move1;
					Move a2=parent.sliver_bullet.get(k).move2;
					if(d1.Equal(a1)||d1.Equal(a2)||d2.Equal(a1)||d2.Equal(a2))
						def++;
				}
				for(int k=0;k<parent.golden_bullet.size();k++)
					if(d1.Equal(parent.golden_bullet.get(k))||d2.Equal(parent.golden_bullet.get(k)))
						def++;
				if(def==atk)
				{
					if(Def.count!=1)
						Def.count=2;
					Aegis.add(new TMove(d1,d2));
				}
				//SPECIAL=true;
				//if(SPECIAL)
				//	OST.println(d1.print()+d2.print()+def+"??");
				//SPECIAL=false;
			}
		if(ICanWinDebug)
		{
			OST.println(Aegis.size()+atk+"" +parent.shield.size()+""+parent.golden_bullet.size());
			for(int i=0;i<Aegis.size();i++)
				OST.println(Aegis.get(i).move1.print()+Aegis.get(i).move2.print());
		}
		Def.Aegis=Aegis;
		if(Aegis.size()>0)
			return Def;
		else if(atk!=0)//要輸掉了
		{
			//System.out.println("好像要輸掉了");
			Def.count=3;
			//return Def;
			if(parent.sliver_bullet.size()!=0)
				Def.Aegis=parent.sliver_bullet;
			else
				Def.Aegis.add(new TMove(parent.golden_bullet.get(0),null));
			return Def;
		}
		else
		{
			Def.count=0;
			return Def;
		}
	}
	Answer ICanWin(Answer parent)
	{
		ICanWinDebug=false;
		int winColor = parent.GetOpponentColor();//取得顏色
		int me=gop.MinOrMax(this.myColor,winColor);
		if(ICanWinDebug)
			OST.println("me:"+me+"color:"+winColor);
		
		if(parent.father!=null)//第二層以上 Min->Max->Min -----------------------------------------
		{
			
			Answer gaf = parent.father;
			if(SPECIAL)
				gaf.PrintSliver();
			for(int i=0;i<gaf.sliver_bullet.size();i++)
			{
				TMove win =  gaf.sliver_bullet.get(i);
				Move m1 = win.move1;
				Move m2 = win.move2;
				//表示gaf的攻擊沒有在parent被防禦
				if(gop.IsEmpty(parent.board[m1.row][m1.col])&&gop.IsEmpty(parent.board[m2.row][m2.col]))
				{
					Answer winAns = new Answer(parent,winColor,m1,m2);
					winAns.Update_Price(m1,m2);
					if(winAns.GetPrice()*me>99999)//找到勝利走法
						return winAns;
					else
						System.out.println("Warning!權重計算錯誤!");
				}
			}
			
			for(int i=0;i<gaf.golden_bullet.size();i++)
			{
				Move m = gaf.golden_bullet.get(i);
				if(gop.IsEmpty(parent.board[m.row][m.col]))
				{
					Answer winAns = new Answer(parent,winColor,m);
					winAns.Update_Price(m);
					
					if(ICanWinDebug)
						OST.println(winAns.PrintMove());
					
					if(winAns.GetPrice()*me>99999)//找到勝利走法
						return winAns;
					else
						System.out.println("Warning!權重計算錯誤!");
				}
			}
		}//---------------------------------------------------------------------------------------------------
		else//第一層
		{
						
			parent.R();
			ArrayList<Move>moves = parent.moves;
			for(int i=0;i<moves.size();i++)
				for(int j=i+1;j<moves.size();j++)
				{
					
					Move m1=moves.get(i);
					Move m2=moves.get(j);
					if(!parent.Equal_Line(m1,m2))//不在同條線不考慮
						continue;
					
					Answer winAns = new Answer(parent,winColor,m1,m2);
					winAns.Update_Price(m1,m2);
						
					if(ICanWinDebug)
						OST.println(winAns.PrintMove()+winAns.GetPrice());
					
					if(winAns.GetPrice()*me>99999)//找到勝利走法
					{
						//System.out.println("~~~");
						if(ICanWinDebug)
						{
							gop.Debug_On();
							SPECIAL=true;
							OST.println(winAns.PrintMove());
							winAns.PrintBoard();
							
							//
							gop.Debug_Off();
							SPECIAL=false;
						}
						return winAns;
					}
				}
			//return parent;
		}
		return null;
	}
	void testIcanWin()
	{
		Mark(new Move(3,3),myColor);
		Mark(new Move(6,6),myColor);
		Mark(new Move(5,5),myColor);
		Mark(new Move(4,4),myColor);
		root.Update_Real();
		root.Update_All_Price();
		
		Answer Win = ICanWin(root);
		//root.Update_Price(new Move(4,4));
		
		//Answer deep1=new Answer(root,myColor,
		gop.Debug_On();
		SPECIAL=true;
		
		OST.println(Win.PrintMove());
		Win.PrintBoard();
		System.out.println(Win.GetPrice());
		System.out.println("-------------------------------------");
		gop.Debug_Off();
		SPECIAL=false;
	}
	void testIcanWin2()
	{
		Mark(new Move(3,3),opponentColor);
		Mark(new Move(6,6),opponentColor);
		Mark(new Move(5,5),opponentColor);
		Mark(new Move(4,4),opponentColor);
		root.Update_Real();
		root.Update_All_Price();
		
		//Answer Win = ICanWin(root);
		
		
		root.Update_Price(new Move(4,4));
		root.R();
		for(int i=0;i<root.moves.size();i++)
			for(int j=i+1;j<root.moves.size();j++)
			{
				Move m1=root.moves.get(i);
				Move m2=root.moves.get(j);
				Answer deep1=new Answer(root,myColor,m1,m2);
				deep1.Update_Price(m1,m2);
				
				Answer Win = ICanWin(deep1);
				if(Win==null)
				{
					gop.Debug_On();
					SPECIAL=true;
					OST.println(deep1.PrintMove());
					deep1.PrintBoard();
					deep1.PrintMoves();
					ICanWin(deep1);
					//System.out.println(deep.GetPrice());
					//System.out.println("-------------------------------------");
					gop.Debug_Off();
					SPECIAL=false;
				}
			}
		//Answer deep2=new Answer(root,myColor,new Move(1,1),new Move(2,2));
		
		
		
	}
	void testIWillLose()//2層測試
	{
		Mark(new Move(1,1),myColor);
		Mark(new Move(2,2),opponentColor);
		Mark(new Move(6,6),opponentColor);
		Mark(new Move(5,5),opponentColor);
		Mark(new Move(4,4),opponentColor);
		Mark(new Move(3,3),opponentColor);
		Mark(new Move(3,5),opponentColor);
		Mark(new Move(2,6),opponentColor);
		Mark(new Move(1,7),opponentColor);
		
		root.Update_Real();
		root.Update_All_Price();
		
		//Answer Win = ICanWin(root);
		root.Update_Price(new Move(4,4));
		root.R();
		
		ArrayList<TMove>m=IWillLose(root).Aegis;
		OST.println(m.size());
		for(int i=0;i<m.size();i++)
		{
			Move m1=m.get(i).move1;
			Move m2=m.get(i).move2;
			Answer deep1;
			if(m2!=null)
			{
				OST.println(m1.print()+m2.print());
				deep1=new Answer(root,myColor,m1,m2);
				deep1.Update_Price(m1,m2);
				Answer Win = ICanWin(deep1);
				if(Win==null)
				{
					gop.Debug_On();
					SPECIAL=true;
					OST.println(deep1.PrintMove());
					deep1.PrintBoard();
					deep1.PrintMoves();
					ICanWin(deep1);
					//System.out.println(deep.GetPrice());
					//System.out.println("-------------------------------------");
					gop.Debug_Off();
					SPECIAL=false;
				}
				else
				{
					//ERROR
				}
			}
			else
			{
				for(int j=0;j<root.moves.size();j++)
				{	
					m2=root.moves.get(j);
					deep1=new Answer(root,myColor,m1,m2);
					deep1.Update_Price(m1,m2);
					Answer Win = ICanWin(deep1);
					if(Win!=null)
					{
						gop.Debug_On();
						SPECIAL=true;
						OST.println(deep1.PrintMove());
						deep1.PrintBoard();
						deep1.PrintMoves();
						ICanWin(deep1);
						//System.out.println(deep.GetPrice());
						//System.out.println("-------------------------------------");
						gop.Debug_Off();
						SPECIAL=false;
					}
					else
					{
						//ERROR
					}
				}
				deep1=new Answer(root,myColor,m1,root.moves.get(0));
				deep1.Update_Price(m1,root.moves.get(0));
				Answer Win = ICanWin(deep1);
				if(Win==null)
					{
						gop.Debug_On();
						SPECIAL=true;
						OST.println(deep1.PrintMove());
						deep1.PrintBoard();
						deep1.PrintMoves();
						ICanWin(deep1);
						//System.out.println(deep.GetPrice());
						//System.out.println("-------------------------------------");
						gop.Debug_Off();
						SPECIAL=false;
					}
			}		
		}
		//Answer deep2=new Answer(root,myColor,new Move(1,1),new Move(2,2));	
	}
	void deepTest()
	{
		//Mark(new Move(4,6),myColor);
		//Mark(new Move(5,3),myColor);
		Mark(new Move(10,10),0);
		Mark(new Move(5,8),opponentColor);
		
		Mark(new Move(5,5),myColor);
		Mark(new Move(5,6),myColor);
		Mark(new Move(5,7),myColor);
		Mark(new Move(6,8),myColor);
		Mark(new Move(7,9),myColor);
		Mark(new Move(9,11),opponentColor);
		root.Update_Real();
		root.Update_All_Price();
		
		//Answer Win = ICanWin(root);
		root.Update_Price(new Move(4,4));
		root.R();
		
		Dep(root);
		
		
		Answer deep=null;
		int da=0;
		for(int i=0;i<root.moves.size();i++)
			for(int j=i+1;j<root.moves.size();j++)
			{
				Move mm1=root.moves.get(i);
				Move mm2=root.moves.get(j);
				Answer t= new Answer(root,root.GetOpponentColor(),mm1,mm2);
				t.Update_Price(mm1,mm2);
				if(deep==null)
					deep=t;
				{
					int ga=IWillLose(t).count;
					if(ga==3)
					{
						deep=t;
						da=ga;
						
					}
					else if(deep.GetPrice()<t.GetPrice()&&da!=3)
						deep=t;
				}
			}
		//deep.Update_Price(mm1,mm2);
		deep.R();
		
		Dep(deep);
		Answer deep1=root;
		
		ArrayList<TMove>m=IWillLose(deep).Aegis;
		for(int i=0;i<m.size();i++)
		{
			Move m1=m.get(i).move1;
			Move m2=m.get(i).move2;
			if(m2!=null)
			{
				OST.println(m1.print()+m2.print());
				deep1=new Answer(deep,deep.GetOpponentColor(),m1,m2);
				deep1.Update_Price(m1,m2);
				deep1.R();
				Dep(deep1);
			}
			else
			{
				OST.print(m1.print()+"~~~");
			}
		}
		deep=null;
		da=0;
		for(int i=0;i<deep1.moves.size();i++)
			for(int j=i+1;j<deep1.moves.size();j++)
			{
				Move mm1=deep1.moves.get(i);
				Move mm2=deep1.moves.get(j);
				Answer t= new Answer(deep1,deep1.GetOpponentColor(),mm1,mm2);
				t.Update_Price(mm1,mm2);
				
				if(deep==null)
					deep=t;
				else 
				{
					int ga=IWillLose(t).count;
					if(ga==3)
					{
						deep=t;
						da=ga;
					}
					else if(deep.GetPrice()<t.GetPrice()&&da!=3)
						deep=t;
				}
			}
		Dep(deep);
		//ICanWin(deep1);
		//System.out.println(deep.GetPrice());
		//System.out.println("-------------------------------------");
		
	}
	void Up(Answer ans)
	{
		gop.Debug_On();
		SPECIAL=true;
		
		ans.Update_Price(ans.move1,ans.move2);
		
		gop.Debug_Off();
		SPECIAL=false;
	}
	void Dep(Answer ans)
	{
		gop.Debug_On();
		SPECIAL=true;
		
		OST.print(count);
		OST.println(ans.PrintMove());
			ans.PrintBoard();
			ans.PrintMoves();
			ans.PrintSliver();
		OST.println(ans.father.GetPrice()+"->"+ans.oldPrice+"->"+ans.GetPrice());
		gop.Debug_Off();
		
		SPECIAL=false;
	}
	void test1()
	{
		Mark(new Move(10,10),2);
		Mark(new Move(10,11),2);
		//Mark(new Move(10,12),1);
		Mark(new Move(10,14),2);
		Mark(new Move(10,13),2);
		//Mark(new Move(8,11),1);
		//Mark(new Move(9,12),1);
		
		root.Update_All_Price();
		
		//--------------test----------------------
		
		
		
		
		Answer next=new Answer(root,myColor,new Move(9,13));
		next.Update_Price(new Move(9,13));
		
		Answer next2=new Answer(root,myColor,new Move(1,2));
		next2.Update_Price(new Move(1,2));
		gop.Debug_On();
		SPECIAL=true;
		root.Update_Price(new Move(10,13));
		root.PrintBoard();
		root.PrintSliver();
		gop.Debug_Off();
		SPECIAL=false;
	}
	void test2()
	{
		Mark(new Move(18,1),1);
		Mark(new Move(17,2),1);
		Mark(new Move(15,4),1);
		Mark(new Move(14,5),1);
		Mark(new Move(13,6),1);
		
		Mark(new Move(10,1),1);
		Mark(new Move(10,2),1);
		Mark(new Move(10,3),1);
		Mark(new Move(10,4),1);
		Mark(new Move(10,8),1);
		Mark(new Move(10,9),1);
		Mark(new Move(10,10),1);
		Mark(new Move(10,11),1);
		Mark(new Move(10,12),2);
		Mark(new Move(10,13),1);
		Mark(new Move(10,14),1);
		Mark(new Move(10,17),1);
		Mark(new Move(10,18),1);
		Mark(new Move(10,19),1);
		//..OO.OO.O.OX..OO.OO
		Mark(new Move(7,3),1);
		Mark(new Move(7,4),1);
		Mark(new Move(7,6),1);
		Mark(new Move(7,7),1);
		Mark(new Move(7,9),1);
		Mark(new Move(7,11),1);
		Mark(new Move(7,12),2);
		Mark(new Move(7,15),1);
		Mark(new Move(7,16),1);
		Mark(new Move(7,18),1);
		//.OOOO..OOOX.OOOOO.->
		Mark(new Move(5,2),1);
		Mark(new Move(5,3),1);
		Mark(new Move(5,4),1);
		Mark(new Move(5,5),1);
		Mark(new Move(5,8),1);
		Mark(new Move(5,9),1);
		Mark(new Move(5,10),1);
		Mark(new Move(5,11),2);
		Mark(new Move(5,13),1);
		Mark(new Move(5,14),1);
		Mark(new Move(5,15),1);
		Mark(new Move(5,16),1);
		


		root.Update_All_Price();
		
		//--------------test----------------------
		
		gop.Debug_On();
		SPECIAL=true;
		root.Update_Price(new Move(10,5));
		
		Answer next=new Answer(root,myColor,new Move(7,19));
		next.Update_Price(new Move(7,19));
		
		Answer next2=new Answer(root,myColor,new Move(5,17),new Move(1,1));
		next2.Update_Price(new Move(5,17));
		next2.Update_Price(new Move(1,1));
		
		gop.Debug_Off();
		SPECIAL=false;
		
		ArrayList<Answer>xxx=new ArrayList<Answer>();
		long time1 = System.currentTimeMillis();
		for(int i=0;i<=1000000;i++)
		{
			if(i%100000==0)
				System.out.println(i);
			Answer next3=new Answer(next2,myColor,new Move(2,17));
			next3.Update_Price(new Move(2,17));
			//xxx.add(next3);
		}
		long time2 = System.currentTimeMillis();
		System.out.println("doSomething()花了：" + (time2-time1)/1000 + "秒");
	}
}