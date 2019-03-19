import java.util.*;
import java.lang.*;
import java.io.*;

class Price
{
	private int v;
	private int [][] opt=new int[4][45];
	Price(){}
	Price(Price p)
	{
		Copy(p);
	}
	void Copy(Price p)
	{
		int [][] x=p.opt;
		this.v=p.v;
		for(int i=0;i<4;i++)
			for(int j=0;j<40;j++)
				opt[i][j]=x[i][j];
	}
	//給予某一列其price
	void Set(int r,int x,int y,int v)
	{
		//更新整體
		int change = v-Get(r,x,y);
		this.v+=change;
		//更新個別
		
		if(r==0)//左
			opt[r][x]=v;
		else if(r==1||r==3)
		{
			if(x==1)//上
				opt[r][y]=v;
			else//右上
				opt[r][18+x]=v;
		}
		else if(r==2)//左上
			opt[r][y]=v;
	}
	//取得某一列其price
	int Get(int r,int x,int y)
	{
		if(r==0)
			return opt[r][x];
		else if(r==1||r==3)
		{
			if(x==1)
				return opt[r][y];
			else
				return opt[r][18+x];
		}
		else
			return opt[r][y];
	}
	int GetPrice()
	{
		return this.v;
	}
	void SetPrice(int v)
	{
		this.v=v;
	}
	void Print(PrintStream OST)
	{
		
	}
}
