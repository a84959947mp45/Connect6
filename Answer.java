import java.util.*;
import java.lang.*;
import java.io.*;

//一種用來映射的類別(不一定會用到，先保留)
//記錄某個節點之所有資訊
class Answer
{
	int oldPrice=0;
	Move move1;
	Move move2;
	
	Move debugMove;
	private int SIZE=19;
	
	Price price;
	int[][] board;
	
	//該盤面所有可以走的路
	ArrayList<Move>moves;
	
	//自己可以產生的攻擊
	ArrayList<TMove>sliver_bullet=new ArrayList<TMove>();
	ArrayList<Move>golden_bullet=new ArrayList<Move>();
	
	//對方可用的防禦
	ArrayList<Move>shield=new ArrayList<Move>();
	//對方可能選擇的防禦
	AegisMove Aegis;
	
	//上一層
	Answer father;
	//-----節點資訊----------
	int myColor;//該誰下
	//-----------------------
	MySolver solver;
	
	Galanthus gop;
	Answer(MySolver my,int color)
	{
		
		solver=my;
		gop = my.gop;
		
		myColor = color;
		
		price = new Price();
		Update_Real();
		
		//move1 = my.move1;
		//move2 = my.move2;
	}
	Answer(Answer ff,int myColor)
	{
		father=ff;
		if(ff.father!=null&&(ff.move2==null||ff.move1==null))
			System.out.println("Warning!!父節點設置錯誤!");
		solver=father.solver;
		//從父點取得
		price = new Price(father.price);
		
		this.myColor = myColor;
		
		gop=father.gop;
	}
	Answer(Answer ff,int myColor,Move m)
	{
		this(ff,myColor);
		Update_One_Board(m);
	}
	Answer(Answer ff,int myColor,Move m1,Move m2)
	{
		this(ff,myColor);
		Update_Two_Board(m1,m2);
	}
	void Update_Real()
	{
		board = solver.GetCurrentState();
		moves = solver.GetAvailableCurrentMoves();
	}
	void Update_One_Board(Move m)
	{
		move1 = m;
		this.board = solver.GetNextState(father.board,move1,myColor);
		moves = solver.GetAvailableMoves(board,move1,father.moves);
		move2=null;
	}
	void Update_Two_Board(Move m1,Move m2)
	{
		move1=m1;
		int[][] temp_board=solver.GetNextState(father.board,move1,myColor);
		ArrayList<Move>temp_moves = solver.GetAvailableMoves(temp_board,move1,father.moves);
		move2=m2;
		this.board = solver.GetNextState(temp_board,this.move2,this.myColor);
		this.moves = solver.GetAvailableMoves(this.board,this.move2,temp_moves);
	}
	public boolean Equal_Line(Move m1,Move m2)
	{
		for(int i=0;i<4;i++)
		{
			Move mm1=GetStart(m1,i);
			Move mm2=GetStart(m2,i);
			if(mm1.row==mm2.row&&mm1.col==mm2.col)
				return true;
		}
		return false;
	}
	
	
	//取得這個座標的起點 
	public Move GetStart(Move m,int ch)
	{
		int i=m.row;
		int j=m.col;
		if(ch==0)//左方 ||最左 
			return new Move(i,1);
		else if(ch==1)//左上方 ||最左或最上 
		{
			if(i<j)
				return new Move(1,(j-i)+1);
			else
				return new Move((i-j)+1,1);
		}
		else if(ch==2)//上方
			return new Move(1,j);
		else//右上方
		{
			int x = SIZE+1-j;
			if(i<x)//5,12 -> 1,16 
				return new Move(1,(SIZE+1)-((x-i)+1));
			else//18,2 ->17,3->16,4->1,19 18,4->17,5->16,6->15,7->3,19
				return new Move((i-x)+1,19);
		}
	}
	void Update_All_Price()
	{
		for(int i=1;i<=SIZE;i++)
			for(int j=1;j<=SIZE;j++)
				Update_Price(new Move(i,j));		
	}
	void Update_Price(Move m1,Move m2)
	{
		Update_Price(m1);
		Update_Price(m2);
	}
	//每次最多只會跑19*4次
	void Update_Price(Move m)
	{
		if(solver.SPECIAL)
		{
			solver.OST.println(m.print());
			PrintBoard();
			PrintMoves();
		}
		int temp = this.GetPrice();
		int value=0;
		//四個方向
		for(int i=0;i<4;i++)
		{
			//取得方向的起點
			Move mm = GetStart(m,i);
			int r=i,x=mm.row,y=mm.col;
			//取得一列價值
			int v=gop.Calculate(this,mm,i);
			value+=v-this.Get(r,x,y);
			//將一列之價值儲存
			this.price.Set(r,x,y,v);
			PrintOneDirectionPrice(i,r,x,y);
		}
		PrintSliver();
		PrintFourDirectionPrice(m,temp,value);
	}
	public void GoldenAdd(Move m)
	{
		if(solver.SPECIAL)
			solver.OST.println("\ndd:"+debugMove.print()+m.print());
		int used=0;
		for(int i=0;i<golden_bullet.size();i++)
			if(m.Equal(golden_bullet.get(i)))
			{
				used=1;
				break;
			}
		
		if(used==0)
			golden_bullet.add(m);
		ShieldAdd(m);
	}
	public void ShieldAdd(Move m)
	{
		int used=0;
		for(int i=0;i<shield.size();i++)
			if(m.Equal(shield.get(i)))
			{
				used=1;
				break;
			}
		if(used==0)
			shield.add(m);
	}
	public void SliverAdd(Move m1,Move m2)
	{
		
		if(m2==null)
			GoldenAdd(m1);
		else
		{
			if(solver.SPECIAL)
				solver.OST.println("\ndd:"+debugMove.print()+m1.print()+m2.print());
			Move temp;
			
			if(m1.row==m2.row)
			{
				if(m1.col>m2.col)
				{
					temp=m1;
					m1=m2;
					m2=temp;
				}
			}
			else if(m1.row>m2.row)
			{
				temp=m1;
				m1=m2;
				m2=temp;
			}
				
			TMove attacker = new TMove(m1,m2);
			int used=0;
			for(int i=0;i<sliver_bullet.size();i++)
			{
				if(attacker.Equal(sliver_bullet.get(i)))
				{
					used=1;
					break;
				}
			}
			if(used==0)
				sliver_bullet.add(attacker);
			ShieldAdd(m1);
			ShieldAdd(m2);
		}
	}
	int Get(int r,int x,int y)
	{
		return price.Get(r,x,y);
	}
	int GetPrice()
	{
		return price.GetPrice();
	}
	//Debug顯示用
	String PrintMove()
	{
		return "("+move1.print()+","+move2.print()+")";
	}
	public void PrintBoard()
	{
		if(solver.SPECIAL)
		{
			solver.OST.println("Board:-----------------------------------------");
			for(int i=1;i<=19;i++)
			{
				for(int j=1;j<=19;j++)
				{
					if(board[i][j]==1)
						solver.OST.printf("O ");
					else if(board[i][j]==2)
						solver.OST.printf("X ");
					else
						solver.OST.printf(". ");
				}
				solver.OST.println();
			}
		}
	}
	public void PrintMoves()
	{
		if(solver.SPECIAL)
		{
			solver.OST.println("Moves:------------------------------------------");
			for(int i=1;i<=19;i++)
			{
				for(int j=1;j<=19;j++)
				{
					int has=0;
					for(int k=0;k<moves.size();k++)
					{
						Move m=moves.get(k);
						if(m.row==i&&m.col==j)
						{
							has=1;
							break;
						}
					}
					if(has==1)
						solver.OST.printf("O ");
					else
						solver.OST.printf(". ");
				}
				solver.OST.println();
			}
		}
	}
	void PrintOneDirectionPrice(int i,int r,int x,int y)
	{
		if(solver.SPECIAL)
			if(father!=null)
				solver.OST.printf("--%d:%d-%d=%d\n",i,this.Get(r,x,y),this.father.Get(r,x,y),this.Get(r,x,y)-this.father.Get(r,x,y));
			else
				solver.OST.printf("--i=%d:v=%d\n",i,this.Get(r,x,y));
	}
	void PrintFourDirectionPrice(Move m,int temp,int value)
	{
		if(solver.SPECIAL)
			if(father!=null)
				solver.OST.printf("!%s %d+%d=%d\n",m.print(),value,this.father.GetPrice(),this.GetPrice());
			else
				solver.OST.printf("!%s %d+%d=%d\n",m.print(),temp,value,this.GetPrice());
	}
	public void PrintSliver()
	{
		if(solver.SPECIAL)
		{
			solver.OST.println(sliver_bullet.size()+"-------攻擊型------------");
			for(int i=0;i<sliver_bullet.size();i++)
				solver.OST.printf("[%s %s],",sliver_bullet.get(i).move1.print(),sliver_bullet.get(i).move2.print());
			solver.OST.println();
			for(int i=0;i<golden_bullet.size();i++)
				solver.OST.printf("[%s],",golden_bullet.get(i).print());
			solver.OST.println();
			solver.OST.println("-------------------------防禦型-----------");
			for(int i=0;i<shield.size();i++)
				solver.OST.printf("[%s],",shield.get(i).print());
			solver.OST.println();
			solver.OST.println("-------------------------");
			
		}
	}
	public int GetMyColor()
	{
		return myColor;
	}
	public int GetOpponentColor()
	{
		return myColor%2+1;
	}
	public void R()
	{
		//solver.OST.println("壓縮前"+moves.size());
		for(int i=1;i<=SIZE;i++)
			for(int j=1;j<=SIZE;j++)
				gop.USED[i][j]=0;
		for(int i=0;i<moves.size();i++)
		{
			Move m = moves.get(i);
			if(gop.USED[m.row][m.col]==1||board[m.row][m.col]==1||board[m.row][m.col]==2)
			{
				moves.remove(i);
				i-=1;
			}
			else
				gop.USED[m.row][m.col]=1;
		}
		//solver.OST.println("壓縮後"+moves.size());
	}
	void SetFinalPrice(int v)//使用此函數後Answer便不可以再使用Update Price
	{
		oldPrice=price.GetPrice();
		price.SetPrice(v);
	}
	
}