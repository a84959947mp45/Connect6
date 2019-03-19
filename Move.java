import java.util.*;
import java.lang.*;
import java.io.*;

public class Move {
	public Move(int a,int b) {
		row=a;
		col=b;
	}
	int row;
	int col;
	public boolean check(int r,int c)
	{
		if(r==row&&c==col)
			return true;
		else 
			return false;
	}
	public String print()
	{
		char ss=(char)(col+'A'-1);
		String ans = "("+row+","+ss+")";
		return ans;
	}
	public boolean Equal(Move m)
	{
		if(m.row==this.row&&m.col==this.col)
			return true;
		return false;
	}
}
class TMove
{
	TMove(Move m1,Move m2)
	{
		move1=m1;
		move2=m2; 
	}
	boolean Equal(TMove tm)
	{
		if(move1.Equal(tm.move1)&&move2.Equal(tm.move2))
			return true;
		return false;
	}
	Move move1;
	Move move2;
}
class AegisMove
{
	int count;
	ArrayList<TMove>Aegis;
}