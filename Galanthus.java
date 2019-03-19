import java.util.*;
import java.lang.*;
import java.io.*;

class Galanthus
{
	boolean f=false;
	int DEF=1;	
	
	PrintStream OST;
	//-----------------------------------
	private int SIZE=19;
	private int [][][] D=new int[4][SIZE+1][SIZE+1];
	private int [][][] OE1=new int[4][SIZE+1][SIZE+1];//
	private int [][][] OE2=new int[4][SIZE+1][SIZE+1];
	private int [][][] OCB=new int[4][SIZE+1][SIZE+1];
	
	private int [][][] XE1=new int[4][SIZE+1][SIZE+1];//
	private int [][][] XE2=new int[4][SIZE+1][SIZE+1];
	private int [][][] XCB=new int[4][SIZE+1][SIZE+1];
	
	public int [][] USED = new int[SIZE+1][SIZE+1];
	
	private int myColor;
	private int opponentColor;
	
	//每次最多只會跑19次
	//針對某一個方向做判斷
	Galanthus(MySolver solver)
	{
		this.myColor = solver.myColor;
		this.opponentColor=solver.opponentColor;
		this.OST=solver.OST;
	}
	public int check(int v)
	{
		String op="+";
		int c=1;
		String ss="我";
		int ex=0;
		if (v==0)
			return 0;
		if(v<0)//表示為對方
		{
			c=-1*DEF;
			ss="敵";
			v=-v;
			ex=2;
		}
		if(f)
		{
			OST.print(op);
			OST.print(c*v);
		}
		switch(v)
		{
			//發現一死 ex:..OX..
			case 2:
				if(f)
					OST.print(ss+"(一死)");
				return c*5-ex;
			case 3://發現一活 ex:..O..
				if(f)
					OST.print(ss+"(一活)");
				return c*15;
			case 5://發現兩死 ex:..OOX..
				if(f)
					OST.print(ss+"(兩死)");
				return c*100-ex;
			case 6://發現兩活 ex: ..OO..
				if(f)
					OST.print(ss+"(兩活)");
				return c*400;
			case 8://發現三死 ex: ..OOOX...
				if(f)
					OST.print(ss+"(三死)");
				return c*300-ex;
			case 9://發現三活 ex: ...OOO...
				if(f)
					OST.print(ss+"(三活)");
				return c*850;
			default:
				if(v>=16)//(16,17,18)發現能夠下六個 ex: .XOOOOOOX.
					return c*9999999;
				else if(v%3==1)//發現被擋 ex: XOOOOOX
				{
					if(f)
						OST.print(ss+"(被擋)");
					return 0;
				}
				else if(v%3==2)//(11、14)發現四死、五死 ex:..XOOOO...
				{
					if(f)
						OST.print(ss+"(一破)");
					return c*600;
				}
				else//(12,15)發現四活、五活 ex:..OOOOO..
				{
					if(f)
						OST.print(ss+"(兩破)");
					return c*1700;
				}
		}
	}
	int Before(int ch,int i,int j,int v)
	{
		if(Check_Out(i,j,ch,v))
			return 0;
		return D[ch][i-Ro(ch,v)][j-Co(ch,v)];
	}
	int Before(int ch,int i,int j,int v,int[][][] B)
	{
		if(Check_Out(i,j,ch,v))
			return 0;
		return B[ch][i-Ro(ch,v)][j-Co(ch,v)];
	}
	int Before(int ch,int i,int j,int v,int[][] B)
	{
		if(Check_Out(i,j,ch,v))
			return -1;
		return B[i-Ro(ch,v)][j-Co(ch,v)];
	}
	Move FindMove(int ch,int i,int j,int v)
	{
		return new Move(i-Ro(ch,v),j-Co(ch,v));
	}
	int Calculate(Answer ans,Move m,int ch)
	{
		//if(f)
		//	printPrice(OE2[ch],ch,"OE2");
		int price=0;
		int[][] board = ans.board;
		int i=m.row,j=m.col;
		int count=25;
		int me;
		if(f)
			OST.print(m.print());
		while(count-->0)//理論上不會跑超過19次，除非BUG
		{
			if(f)
				OST.printf("%s ",new Move(i,j).print());
			if(board[i][j]==myColor)//電腦的棋子
				me=1;
			else if(board[i][j]==opponentColor)//對方的
				me=-1;
			else//空點
				me=0;
			
			if(!Check_Start(new Move(i,j),ch))//非起點，有舊點
			{
				if(me==0)//此點為空
				{
					D[ch][i][j]=0;//帶0
					price+=check(Before(ch,i,j,-1));//確認舊點加總
				}
				else if(!Conflict(board[i+Ro(ch)][j+Co(ch)],board[i][j]))
					D[ch][i][j]=Before(ch,i,j,-1)+3*me;//頭活點，此點與上一點相同或空 OO or .O or XX or .X
				else//此點與上一點不同ex: XXO 對X來講尾死 對O頭死  
				{
					D[ch][i][j]=me*2;//重新計數 ， //新點的頭死點 XO.. or OXX..
					price+=check(Before(ch,i,j,-1)+me);//舊點的尾死點  OOX.  or XXO..
				}
			}
			else//無舊點(表示i,j為起點)
			{
				if(me==0)
					D[ch][i][j]=0;
				else//新點的頭死點
					D[ch][i][j]=me*2; //頭死點 |O.. or |XX..
			}
			Find_Kill(ans,ch,i,j);
			//到終點
			if(Check_End(new Move(i,j),ch))
			{
				if(f)
					OST.printf("%s enter final\n",new Move(i,j).print());
				price+=check(D[ch][i][j]-me);
				break;
			}
			
			i-=Ro(ch);
			j-=Co(ch);
		}
		if(f)
		{
			OST.printf("one_direct_value=%d\n",price);
			printPrice(D[ch],ch,"D");
			printPrice(OE1[ch],ch,"OE1");
			printPrice(OE2[ch],ch,"OE2");
			printPrice(OCB[ch],ch,"OCB");
		}
		count=25;
		i=m.row;
		j=m.col;
		//還原----
		while(count-->0)
		{

			D[ch][i][j]=0;
			OE1[ch][i][j]=OE2[ch][i][j]=OCB[ch][i][j]=0;
			XE1[ch][i][j]=XE2[ch][i][j]=XCB[ch][i][j]=0;
			
			if(Check_End(new Move(i,j),ch))
				break;
			i-=Ro(ch);
			j-=Co(ch);
			
		}
		return price;
	}
	public boolean IsEmpty(int v)
	{
		if(v!=2&&v!=1&&v>=0)
			return true;
		else
			return false;
	}
	public void Find_Kill(Answer ans,int ch,int i,int j)
	{
		//if(f)
			//OST.printf("%s ",new Move(i,j).print());
		int[][] B=ans.board;
		if(!Check_Start(new Move(i,j),ch))
		{
			if(IsEmpty(B[i][j]))//.
			{
				OE2[ch][i][j]=Before(ch,i,j,-1,OE1)+1;//.OOO.->
				//XE2[ch][i][j]=Before(ch,i,j,-1,XE1)+1;
				OE1[ch][i][j]=0;//.OOO. 
				//XE1[ch][i][j]=0;

				//連3格空點 表示對方無法在下一步讓此連線
				if(!Check_Out(i,j,ch,-2)&&IsEmpty(Before(ch,i,j,-1,B))&&IsEmpty(Before(ch,i,j,-2,B)))
				{
					OCB[ch][i][j]=0;
					//XCB[ch][i][j]=0;
				}
				else
				{
					OCB[ch][i][j]=Before(ch,i,j,-1,OCB);
					if(!Check_Out(i,j,ch,-6))//扣去前六個位置的值
						OCB[ch][i][j]-=Before(ch,i,j,-6,OCB);
					if(OCB[ch][i][j]<0)
						OCB[ch][i][j]=0;
						//System.out.printf("Warning 有bug!! OCB的值小於0! %d %d %d\n",ch,i,j);
					//XCB[ch][i][j]=Before(ch,i,j,-1,XCB);
				}
			}
			else if(B[i][j]==ans.myColor)//自己
			{
				OE2[ch][i][j]=Before(ch,i,j,-1,OE2)+1;
				//XE2[ch][i][j]=0;
				OE1[ch][i][j]=Before(ch,i,j,-1,OE1)+1;
				//XE1[ch][i][j]=0;
				OCB[ch][i][j]=Before(ch,i,j,-1,OCB)+1;
				//XCB[ch][i][j]=0;
			}				
			else//X
			{
				//XE2[ch][i][j]=Before(ch,i,j,-1,XE2)+1;
				OE2[ch][i][j]=0;
				//XE1[ch][i][j]=Before(ch,i,j,-1,XE1)+1;
				OE1[ch][i][j]=0;
				OCB[ch][i][j]=0;
				//XCB[ch][i][j]=Before(ch,i,j,-1,XCB)+1;
			}
		}
		else
		{
			OE1[ch][i][j]=OE2[ch][i][j]=OCB[ch][i][j]=0;
			//XE1[ch][i][j]=XE2[ch][i][j]=XCB[ch][i][j]=0;
			if(B[i][j]==ans.myColor)
				OE1[ch][i][j]=OE2[ch][i][j]=OCB[ch][i][j]=1;
			//else if(B[i][j]==2)
				//XE1[ch][i][j]=XE2[ch][i][j]=XCB[ch][i][j]=1;
		}
		Add_Kill(ans,ch,i,j);
	}
	int fff=1;
	public void Add_Kill(Answer ans,int ch,int i,int j)
	{
		ans.debugMove=new Move(i,j);
		int[][] B=ans.board;
			if(OE1[ch][i][j]==4)//至少存在四連
			{
				if(Before(ch,i,j,1,B)==ans.myColor)//至少存在五連
				{
					if(Before(ch,i,j,2,B)==ans.myColor)//存在六連
					{
						//不清楚
					}
					else//存在五連
					{
						if(IsEmpty(Before(ch,i,j,2,B)))
							ans.SliverAdd(FindMove(ch,i,j,2),null);//i+2
						if(IsEmpty(Before(ch,i,j,-4,B)))
							ans.SliverAdd(FindMove(ch,i,j,-4),null);//i-4
					}
				}
				else//存在四連
				{
					if(IsEmpty(Before(ch,i,j,1,B))&&IsEmpty(Before(ch,i,j,-4,B)))//.OOOO.
						ans.SliverAdd(FindMove(ch,i,j,1),FindMove(ch,i,j,-4));
					if(IsEmpty(Before(ch,i,j,1,B))&&IsEmpty(Before(ch,i,j,2,B)))//OOOO..
						ans.SliverAdd(FindMove(ch,i,j,1),FindMove(ch,i,j,2));
					if(IsEmpty(Before(ch,i,j,-4,B))&&IsEmpty(Before(ch,i,j,-5,B)))//..OOOO
						ans.SliverAdd(FindMove(ch,i,j,-4),FindMove(ch,i,j,-5));
				}
			}
			else if(OCB[ch][i][j]>=4&&OE1[ch][i][j]<OCB[ch][i][j])//中洞 //.OOO.O OOO.OO OO.OO. .OO.OOO.
			{
				if(OE2[ch][i][j]<=5&&OE2[ch][i][j]!=OE1[ch][i][j])//二洞
				{
					if(IsEmpty(Before(ch,i,j,-OE2[ch][i][j],B))&&IsEmpty(Before(ch,i,j,-OE1[ch][i][j],B)))
						ans.SliverAdd(FindMove(ch,i,j,-OE2[ch][i][j]),FindMove(ch,i,j,-OE1[ch][i][j]));
				}
				else
					if(IsEmpty(Before(ch,i,j,-OE1[ch][i][j],B)))
						ans.SliverAdd(FindMove(ch,i,j,-OE1[ch][i][j]),null);
			}
	}
	//確認每個不同的方向的位置與前後位置的關係 ex:ch=0，左向右，其串列為 D[i][n]->D[i][n-1]->D[i][n-2] row 不變 col 為-1 
	public int Ro(int ch,int v)//0 ->0 | 1 ->-1| 2 ->-1| 3 ->-1 
	{
		return ch>0 ?  -1*v: 0;
	}
	public int Co(int ch,int v)//0 ->-1 | 1 ->-1 | 2 ->0 | 3 ->1
	{
		return ch>0 ? (ch-2)*v:-1*v;
	}
	public int Ro(int ch)//0 ->0 | 1 ->-1| 2 ->-1| 3 ->-1 
	{
		return ch>0 ?  -1: 0;
	}
	public int Co(int ch)//0 ->-1 | 1 ->-1 | 2 ->0 | 3 ->1
	{
		return ch>0 ? (ch-2):-1;
	}
	public boolean Check_Out(int x,int y,int ch,int v)
	{
		x-=Ro(ch,v);
		y-=Co(ch,v);
		if(x<=SIZE&&x>=1&&y<=SIZE&&y>=1)
			return false;
		else
			return true;
	}
	public boolean Check_End(Move m,int ch)
	{
		int i=m.row;
		int j=m.col;
		if(j==SIZE&&(ch==0||ch==1))//左、左上的起點
			return true;
		if(i==SIZE&&(ch==1||ch==2||ch==3))//左上、上、右上的起點
			return true;
		if(j==1&&ch==3)
			return true;
		return false;
	}	
	public boolean Check_Start(Move m,int ch)
	{
		int i=m.row;
		int j=m.col;
		if(j==1&&(ch==0||ch==1))//左、左上的起點
			return true;
		if(i==1&&(ch==1||ch==2||ch==3))//左上、上、右上的起點
			return true;
		if(j==SIZE&&ch==3)//右上的起點
			return true;
		return false;
	}
	//判斷兩點有無衝突，也就是 a= O b=X or a=X b=O
	public boolean Conflict(int a,int b)
	{
		if(a==myColor&&b==opponentColor)
			return true;
		else if(a==opponentColor&&b==myColor)
			return true;
		else
			return false;
	}
	public void Debug_On()
	{
		f=true;
	}
	public void Debug_Off()
	{
		f=false;
	}
	//輸出盤面Debug用
	public void printPrice(int[][] P,int ch,String ss)
	{
		String[] s={"左方","左上","上方","右上"};
		
		OST.println("--"+ss+"-------"+s[ch]+"------------");
		for(int i=1;i<=SIZE;i++)
		{
			for(int j=1;j<=SIZE;j++)
				OST.printf("%2d ",P[i][j]);
			OST.println();
		}
	}
	public int MinOrMax(int c1,int c2)
	{
		if(c1==c2)
			return 1;
		else
			return -1;
	}
}